package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import com.hmdp.service.ILock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author xjh
 * @create 2023-03-18 22:45
 */
public class SimpleRedisLock implements ILock {

    private final StringRedisTemplate stringRedisTemplate;
    private final String name;

    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    public SimpleRedisLock(StringRedisTemplate stringRedisTemplate, String name) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.name = name;
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        //获取线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        //获取锁
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent("lock:" + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(success);
    }

    @Override
    public void unLock() {
        //执行lua脚本
        Long execute = stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList("lock:" + name), ID_PREFIX + Thread.currentThread().getId());
        System.out.println("释放锁："+execute);
    }
//@Override
//public void unLock() {
//    //获取线程标识
//    String threadId = ID_PREFIX + Thread.currentThread().getId();
//    //获取锁的标识
//    String s = stringRedisTemplate.opsForValue().get("lock:" + name);
//    //判断标识释放相同
//    if (threadId.equals(s)) {
//        //相同则释放
//        stringRedisTemplate.delete("lock:" + name);
//    }
//}
}