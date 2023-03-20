package com.hmdp.service;

/**
 * @author xjh
 * @create 2023-03-18 22:43
 */
public interface ILock {

    /**
     * 尝试获取锁
     * @param timeoutSec 锁持有的超时时间，过期自动释放
     * @return true代表获取锁成功，false代表失败
     */
    boolean tryLock(long timeoutSec);

    void unLock();

}
