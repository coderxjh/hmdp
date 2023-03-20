package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private VoucherOrderMapper voucherOrderMapper;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public Result seckillVoucher(Long voucherId) throws InterruptedException {
        //1.查询优惠券
        SeckillVoucher seckillVoucher = seckillVoucherService.getSecVoucherById(voucherId);
        //2.判断秒杀是否开始
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始");
        }
        //3.判断秒杀是否结束
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束");
        }
        //4.判断是否有库存
        if (seckillVoucher.getStock() < 1) {
            return Result.fail("库存不足");
        }
        Long userId = UserHolder.getUser().getId();
        //创建锁对象
        RLock lock = redissonClient.getLock("lock:voucher-order:" + userId);
        ////获取锁对象
        boolean isLock = lock.tryLock(1,2, TimeUnit.SECONDS);
        //加锁失败
        if (!isLock) {
            return Result.fail("一人只能下一单");
        }
        try {
            //生成代理对象（事务）
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        //5.实现一人一单
        Long userId = UserHolder.getUser().getId();
        log.info("用户信息：{}",UserHolder.getUser());
        //5.1查询订单
        int count = voucherOrderMapper.selectCountByUserIdAndVoucherId(userId, voucherId);
        //5.2判断订单是否存在
        if (count > 0) {
            //用户已经购买过一次
            return Result.fail("用户已经下过订单");
        }
        //6.扣减库存
        int flag = seckillVoucherService.editStockByVoucherId(voucherId);
        if (flag < 1) {
            return Result.fail("库存不足");
        }
        //7.生成订单
        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setUserId(UserHolder.getUser().getId());
        flag = voucherOrderMapper.insertVoucherOrder(voucherOrder);
        log.info("生成订单数量：{}", flag);
        return Result.ok(orderId);
    }
}