package com.hmdp;

import cn.hutool.core.lang.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xjh
 * @create 2023-03-20 16:47
 */
@SpringBootTest
@Slf4j
public class RedissonTest {


    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    private RLock lock;

    @BeforeEach
    void beforeEach() {
        lock = redissonClient.getLock("lock");
    }

    @Test
    public void method1() throws InterruptedException {
        RLock multiLock = redissonClient.getMultiLock(lock);
        multiLock.tryLock();
        boolean isLock = lock.tryLock(1, 3, TimeUnit.SECONDS);
        if (!isLock) {
            log.info("获取锁失败...1");
            return;
        }
        try {
            log.info("获取锁成功...1");
            method2();
            log.info("执行业务...1");
        } finally {
            log.info("释放锁...1");
            lock.unlock();
        }
    }

    void method2() throws InterruptedException {
        boolean isLock = lock.tryLock(1, TimeUnit.SECONDS);
        if (!isLock) {
            log.info("获取锁失败...2");
            return;
        }
        try {
            log.info("获取锁成功...2");
            log.info("执行业务...2");
        } finally {
            log.info("释放锁...2");
            lock.unlock();
        }
    }

    @Test
    void testScript() {
        DefaultRedisScript<Object> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("test.lua"));
        String s = UUID.randomUUID().toString(true);
        String threadId = Thread.currentThread().getId() + s;
        List<String> list = new ArrayList<>();
        list.add("lock");
        stringRedisTemplate.execute(redisScript, list, threadId, "3000");
    }
}