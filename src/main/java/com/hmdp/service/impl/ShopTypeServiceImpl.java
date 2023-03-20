package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xjh
 * @since 2022-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private ShopTypeMapper shopTypeMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryAndCache() {
        //1.从redis中获取商铺类型数据
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE_KEY);
        //2.判断数据是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //3.存在，返回给客户端
            List<ShopType> shopTypes = JSONUtil.toList(shopJson, ShopType.class);
            return Result.ok(shopTypes);
        }
        //4.不存在，查询数据库
        List<ShopType> shopTypes = shopTypeMapper.select();
        if (shopTypes == null || shopTypes.size() == 0) {
            //5.数据库也没有,报错
            return Result.fail("还没设置商铺类型");
        }
        //6.数据库有，保存数据到redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE_KEY + RandomUtil.randomLong(10), JSONUtil.toJsonStr(shopTypes));
        return Result.ok(shopTypes);
    }
}