package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.*;

/**
 * @author xjh
 * @create 2023-03-13 17:59
 */
@Component
@Slf4j
public class CacheClient {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 将任意Java对象序列化为json并存储在string类型的key中，并且可以设置TTL过期时间
     *
     * @param key   缓存的key
     * @param value 被序列化的java对象
     * @param time  TTL过期时间
     * @param unit  过期时间单位
     */
    public <T> void set(String key, T value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * * 将任意Java对象序列化为json并存储在string类型的key中，并且可以设置逻辑过期时间，用于处理缓存击穿问题
     *
     * @param key   缓存的key
     * @param value 被序列化的java对象
     * @param time  逻辑过期时间
     * @param unit  过期时间单位
     */
    public <T> void setWithLogicalExpire(String key, T value, Long time, TimeUnit unit) {
        RedisData<T> redisData = new RedisData<>();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }


    /**
     * 根据指定的key查询缓存，并反序列化为指定类型，利用缓存空值的方式解决缓存穿透问题
     *
     * @param key        缓存信息的键
     * @param id         数据库的id
     * @param type       需要反序列化的java对象类型
     * @param dbFallback 查询数据库的回调接口
     * @param ttlTime    TTL过期时间
     * @param unit       过期时间单位
     * @param <R>        查询的数据库信息
     * @param <ID>       数据库id
     * @return 查询的数据库信息
     */
    public <R, ID> R queryWithPassThrough(String key, ID id, Class<R> type, Function<ID, R> dbFallback, Long ttlTime, TimeUnit unit) {
        //1.从redis中获取缓存数据
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存是否命中
        if (StrUtil.isNotBlank(json)) {
            //3.命中，将缓存数据返回给客户端
            return JSONUtil.toBean(json, type);
        }
        //判断命中的是否是空值
        if (json != null) {
            //空值，代表存储的是缓存穿透数据
            return null;
        }
        //4.没有命中，查询数据库
        R r = dbFallback.apply(id);
        if (r == null) {
            //给redis设置缓存穿透数据：空值
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            //5.数据库也没有，返回错误信息
            return null;
        }
        //6 存在，将数据写入缓存,TTL加入随机数，防止雪崩
        this.set(key, r, ttlTime + RandomUtil.randomLong(10), unit);
        //7.返回数据库信息
        return r;
    }


    /**
     * 根据指定的key查询缓存，并反序列化为指定类型，需要利用逻辑过期解决缓存击穿问题
     *
     * @param key           缓存的键
     * @param lockKey       互斥锁在缓存中的键
     * @param id            数据库的id
     * @param type          需要反序列化的java对象类型
     * @param dbFallback    查询数据库的回调接口
     * @param logicalExpire 逻辑过期时间
     * @param unit          过期时间单位
     * @param <R>           查询的数据库信息
     * @param <ID>          数据库id
     * @return 查询的数据库信息
     */
    public <R, ID> R queryWithLogicalExpire(String key, String lockKey, ID id, Class<R> type, Function<ID, R> dbFallback, Long logicalExpire, TimeUnit unit) {
        //1.从redis中获取缓存信息
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存是否命中
        if (StrUtil.isBlank(json)) {
            //3.未命中，不是热点数据
            return null;
        }
        //4.命中，需要先把json序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //5.1未过期,返回缓存信息
            return r;
        }
        //6.已过期，进行缓存重建
        //6.1先获取互斥锁
        boolean isLock = tryLock(lockKey);
        //6.2判断是否获取锁
        if (isLock) {
            log.info("获取锁成功");
            //6.3 获取锁成功，开启异步线程
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据库
                    R r1 = dbFallback.apply(id);
                    Thread.sleep(200);
                    // 重新设置缓存
                    this.setWithLogicalExpire(key, r1, logicalExpire, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //释放锁
                    unLock(lockKey);
                }
            });
        }
        //6.4无论是否获取锁,最后都要返回旧的缓存信息
        return r;
    }

    /**
     * @param key        缓存的键
     * @param lockKey    互斥锁在缓存中的键
     * @param id         数据库的id
     * @param type       需要反序列化的java对象类型
     * @param dbFallback 查询数据库的回调接口
     * @param time       TTL过期时间
     * @param unit       过期时间单位
     * @param <R>        查询的数据库信息
     * @param <ID>       数据库id
     * @return 查询的数据库信息
     */
    public <R, ID> R queryWithMutex(String key, String lockKey, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        //1.从redis中获取缓存信息
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存是否命中
        if (StrUtil.isNotBlank(json)) {
            //3.命中，将缓存信息返回给客户端
            return JSONUtil.toBean(json, type);
        }
        //判断命中的是否是空值
        if (json != null) {
            //空值，代表存储的是缓存穿透数据
            return null;
        }
        //4.没有命中，查询数据库，并进行缓存重建
        R r = null;
        try {
            //4.1获取互斥锁
            boolean isLock = tryLock(lockKey);
            //4.2判断是否获取成功
            if (!isLock) {
                //4.3获取失败，进入休眠
                Thread.sleep(50);
                //休眠结束，继续请求
                return queryWithMutex(key, lockKey, id, type, dbFallback, time, unit);
            }
            //4.3在拿到锁之后，对redis缓存进行二次校验
            json = stringRedisTemplate.opsForValue().get(key);
            //4.4缓存存在，则无需重建
            if (StrUtil.isNotBlank(json)) {
                //命中，将缓存信息返回给客户端
                return JSONUtil.toBean(json, type);
            }
            //存储的是缓存穿透数据
            if (json != null) {
                //空值，代表存储的是缓存穿透数据
                return null;
            }
            log.info("获取锁成功");
            //4.5 获取锁且缓存尚未重建，查询数据库
            r = dbFallback.apply(id);
            //模拟重建的延时
            Thread.sleep(200);
            if (r == null) {
                //给redis设置缓存穿透数据：空值
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                //5.数据库也没有，返回错误信息
                return null;
            }
            //6 存在，将数据写入缓存,TTL加入随机数，防止雪崩
            this.set(key, r, time + RandomUtil.randomLong(10), unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //释放锁
            unLock(lockKey);
        }
        //7.返回数据库信息
        return r;
    }

    /**
     * 获取互斥锁
     *
     * @param key 锁的key
     * @return 是否成功
     */
    public boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     *
     * @param key 锁的key
     */
    public void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    @Data
    static class RedisData<T> {
        private LocalDateTime expireTime;
        private T data;
    }

}