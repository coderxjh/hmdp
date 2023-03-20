package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xjh
 * @create 2023-03-19 5:22
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redisClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://1.15.117.81:6379").setPassword("dj1314520@");
        //创建RedissonClient对象
        return Redisson.create(config);
    }
}
