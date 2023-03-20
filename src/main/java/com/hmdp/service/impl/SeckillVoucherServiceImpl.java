package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.service.ISeckillVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * @author xjh
 * @since 2022-01-04
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

    @Autowired
    private SeckillVoucherMapper seckillVoucherMapper;

    @Override
    public Long insertSeckillVoucher(SeckillVoucher seckillVoucher) {
        return seckillVoucherMapper.insertSeckillVoucher(seckillVoucher);
    }

    @Override
    public int editStockByVoucherId(Long voucherId) {
        return seckillVoucherMapper.updateStockByVoucherId(voucherId);
    }

    @Override
    public SeckillVoucher getSecVoucherById(Long voucherId) {
        return seckillVoucherMapper.selectSecVoucherById(voucherId);
    }
}