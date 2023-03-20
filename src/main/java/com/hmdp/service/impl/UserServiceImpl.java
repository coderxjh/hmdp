package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.验证手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 3.手机号符合,生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.保存验证码到redis,并设置验证码有效期
        String codeKey = LOGIN_CODE_KEY + phone;
        stringRedisTemplate.opsForValue().set(codeKey, code, LOGIN_CODE_TTL + RandomUtil.randomLong(10), TimeUnit.MINUTES);
        // 5.发送验证码(用日志代替)
        log.debug("已发送验证码:{}", code);
        // 返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 2.从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 不符合，返回错误信息
            return Result.fail("验证码错误");
        }
        // 3.根据手机号查询用户
        User user = query().eq("phone", phone).one();
        // 4.判断用户是否存在
        if (user == null) {
            // 5.用户不存在则创建新用户
            user = createUserWithPhone(phone);
        }
        // 6.保存用户到redis中
        // 6.1随机生成Token，作为登录令牌
        String token = UUID.randomUUID().toString();
        // 6.2将User对象转换为HashMap
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 6.3 存储到redis中
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 6.4设置token有效期，给有效期加个随机数，防止大量缓存同时间失效
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL + RandomUtil.randomLong(10), TimeUnit.MINUTES);
        // 7.返回token给客户端保存
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("用户" + RandomUtil.randomString(10));
        //将新用户保存到数据库
        save(user);
        return user;
    }
}