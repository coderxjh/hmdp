package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.SeckillVoucher;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 Mapper 接口
 * </p>
 *
 * @author xjh
 * @since 2022-01-04
 */
@Mapper
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {

    Long insertSeckillVoucher(SeckillVoucher seckillVoucher);

    int updateStockByVoucherId(Long voucherId);

    SeckillVoucher selectSecVoucherById(Long voucherId);
}
