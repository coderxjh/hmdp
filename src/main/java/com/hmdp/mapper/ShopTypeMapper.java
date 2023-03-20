package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.ShopType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xjh
 * @since 2022-12-22
 */
@Mapper
public interface ShopTypeMapper extends BaseMapper<ShopType> {

    List<ShopType> select();
}
