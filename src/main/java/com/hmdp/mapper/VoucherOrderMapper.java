package com.hmdp.mapper;

import com.hmdp.entity.VoucherOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xjh
 * @since 2022-12-22
 */
@Mapper
public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {

    int insertVoucherOrder(VoucherOrder voucherOrder);


    int selectCountByUserIdAndVoucherId(Long id, Long voucherId);
}
