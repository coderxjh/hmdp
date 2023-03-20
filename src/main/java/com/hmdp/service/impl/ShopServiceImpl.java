package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xjh
 * @since 2022-12-22
 */
@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        String key = CACHE_SHOP_KEY + id;
        String lockKey = LOCK_SHOP_KEY + id;
        //缓存空对象解决缓存穿透
        //Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //互斥锁解决缓存击穿
        Shop shop = cacheClient.queryWithMutex(key, lockKey, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //逻辑过期解决缓存击穿
        //Shop shop = cacheClient.queryWithLogicalExpire(key, lockKey, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);
        //商铺是否为空
        if (shop == null) {
            //为空，返回错误信息
            return Result.fail("商铺不存在");
        }
        return Result.ok(shop);
    }


    //public static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 使用逻辑过期时间解决缓存击穿
     *
     * @param id 商铺id
     * @return 商铺信息
     */
    //public Shop queryWithLogicalExpire(Long id) {
    //    String key = CACHE_SHOP_KEY + id;
    //    //1.从redis中获取店铺信息
    //    String shopJson = stringRedisTemplate.opsForValue().get(key);
    //    //2.判断缓存是否命中
    //    if (StrUtil.isBlank(shopJson)) {
    //        //3.未命中，不是热点数据
    //        return null;
    //    }
    //    //4.命中，需要先把json序列化为对象
    //    RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
    //    Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
    //    LocalDateTime expireTime = redisData.getExpireTime();
    //    //5.判断是否过期
    //    if (expireTime.isAfter(LocalDateTime.now())) {
    //        //5.1未过期,返回商铺信息
    //        return shop;
    //    }
    //    //6.已过期，进行缓存重建
    //    //6.1先获取互斥锁
    //    String lockKey = LOCK_SHOP_KEY + id;
    //    boolean isLock = tryLock(lockKey);
    //    //6.2判断是否获取锁
    //    if (isLock) {
    //        //在拿到锁之后，对redis缓存进行二次校验
    //        shopJson = stringRedisTemplate.opsForValue().get(key);
    //        //判断缓存是否命中
    //        if (StrUtil.isBlank(shopJson)) {
    //            //未命中，不是热点数据
    //            return null;
    //        }
    //        //命中，需要先把json序列化为对象
    //        redisData = JSONUtil.toBean(shopJson, RedisData.class);
    //        shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
    //        expireTime = redisData.getExpireTime();
    //        //判断是否过期
    //        if (expireTime.isAfter(LocalDateTime.now())) {
    //            //5.1未过期,返回商铺信息
    //            return shop;
    //        }
    //        log.info("获取锁成功");
    //        CACHE_REBUILD_EXECUTOR.submit(() -> {
    //            try {
    //                //6.3 获取锁成功，开启异步线程,进行缓存重建
    //                saveShop2Redis(id, 20L);
    //            } catch (Exception e) {
    //                throw new RuntimeException(e);
    //            } finally {
    //                //释放锁
    //                unLock(lockKey);
    //            }
    //        });
    //    }
    //    //6.4无论是否获取锁,最后都要返回旧的缓存信息
    //    return shop;
    //}


    /**
     * 使用互斥锁解决缓存击穿
     *
     * @param id 用户id
     * @return 商铺信息
     */
    //public Shop queryWithMutex(Long id) {
    //    String key = CACHE_SHOP_KEY + id;
    //    //1.从redis中获取店铺信息
    //    String shopJson = stringRedisTemplate.opsForValue().get(key);
    //    //2.判断缓存是否命中
    //    if (StrUtil.isNotBlank(shopJson)) {
    //        //3.命中，将商铺信息返回给客户端
    //        return JSONUtil.toBean(shopJson, Shop.class);
    //    }
    //    //判断命中的是否是空值
    //    if (shopJson != null) {
    //        //空值，代表存储的是缓存穿透数据：null对象
    //        return null;
    //    }
    //    //4.没有命中，查询数据库，并进行缓存重建
    //    String lockKey = LOCK_SHOP_KEY + id;
    //    Shop shop = null;
    //    try {
    //        //4.1获取互斥锁
    //        boolean isLock = tryLock(lockKey);
    //        //4.2判断是否获取成功
    //        if (!isLock) {
    //            //4.3获取失败，进入休眠
    //            Thread.sleep(50);
    //            //休眠结束，继续请求
    //            return queryWithMutex(id);
    //        }
    //        //4.3在拿到锁之后，对redis缓存进行二次校验
    //        shopJson = stringRedisTemplate.opsForValue().get(key);
    //        //4.4缓存存在，则无需重建
    //        if (StrUtil.isNotBlank(shopJson)) {
    //            //命中，将商铺信息返回给客户端
    //            return JSONUtil.toBean(shopJson, Shop.class);
    //        }
    //        //存储的是缓存穿透数据
    //        if (shopJson != null) {
    //            //空值，代表存储的是缓存穿透数据：null对象
    //            return null;
    //        }
    //        log.info("获取锁成功");
    //        //4.5 获取锁且缓存未重建，查询数据库
    //        shop = getById(id);
    //        //模拟重建的延时
    //        Thread.sleep(200);
    //        if (shop == null) {
    //            //给redis设置缓存穿透数据库：null对象
    //            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
    //            //5.数据库也没有，返回错误信息
    //            return null;
    //        }
    //        //6 数据库数据存在，进行缓存重建
    //        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL + RandomUtil.randomLong(10), TimeUnit.MINUTES);
    //        //释放锁
    //    } catch (InterruptedException e) {
    //        throw new RuntimeException(e);
    //    } finally {
    //        unLock(lockKey);
    //    }
    //    //7.返回商铺信息
    //    return shop;
    //}

    /**
     * 使用缓存空对象解决缓存穿透问题
     *
     * @return 商铺信息
     */
    //public Shop queryWithPassThrough(Long id) {
    //    String key = CACHE_SHOP_KEY + id;
    //    //1.从redis中获取店铺信息
    //    String shopJson = stringRedisTemplate.opsForValue().get(key);
    //    //2.判断缓存是否命中
    //    if (StrUtil.isNotBlank(shopJson)) {
    //        //3.命中，将商铺信息返回给客户端
    //        return JSONUtil.toBean(shopJson, Shop.class);
    //    }
    //    //判断命中的是否是空值
    //    if (shopJson != null) {
    //        //空值，代表存储的是缓存穿透数据：null对象
    //        return null;
    //    }
    //    //4.没有命中，查询数据库
    //    Shop shop = getById(id);
    //    if (shop == null) {
    //        //给redis设置缓存穿透数据库：null对象
    //        stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
    //        //5.数据库也没有，返回错误信息
    //        return null;
    //    }
    //    //6 存在，将商铺信息保存到redis中
    //    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL + RandomUtil.randomLong(10), TimeUnit.MINUTES);
    //    //7.返回商铺信息
    //    return shop;
    //}


    //public void saveShop2Redis(Long id, Long expireTime) throws InterruptedException {
    //    //1.查询店铺数据
    //    Shop shop = getById(id);
    //    //2.封装逻辑过期时间
    //    RedisData<Shop> redisData = new RedisData<>();
    //    redisData.setData(shop);
    //    redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireTime));
    //    stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    //}

    /**
     * 获取互斥锁
     *
     * @param key 锁的key
     * @return 是否成功
     */
    //public boolean tryLock(String key) {
    //    Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
    //    return BooleanUtil.isTrue(flag);
    //}

    /**
     * 释放锁
     */
    //public void unLock(String key) {
    //    stringRedisTemplate.delete(key);
    //}
    @Override
    @Transactional
    public Result update(Shop shop) {
        //获取商铺id
        Long id = shop.getId();
        //判断id是否为空
        if (id == null) {
            // 为空，报错
            return Result.fail("商铺id不能为空");
        }
        // 更新数据库
        updateById(shop);
        // 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }
}
