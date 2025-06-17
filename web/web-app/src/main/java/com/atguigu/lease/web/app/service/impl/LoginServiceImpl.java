package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.CodeUtil;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.SmsService;
import com.atguigu.lease.web.app.service.UserInfoService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SmsService smsService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserInfoService userInfoService;

    @Override
    public void getCode(String phone) {
        String randomCode = CodeUtil.getRandomCode(6);
        String Key = RedisConstant.APP_LOGIN_PREFIX+phone;

        Boolean hasKey = stringRedisTemplate.hasKey(Key);
        if(hasKey){
            Long expire = stringRedisTemplate.getExpire(Key,TimeUnit.SECONDS);//剩余时间
            if(RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC-expire<RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC){
                throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
        }

        smsService.sendCode(phone,randomCode);
        stringRedisTemplate.opsForValue().set(Key,randomCode,RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);
    }

    @Override
    public String login(LoginVo loginVo) {
        if(loginVo.getPhone()==null) throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        if(loginVo.getCode()==null) throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        String key = RedisConstant.APP_LOGIN_PREFIX+loginVo.getPhone();
        String code = stringRedisTemplate.opsForValue().get(key);
        if(!code.equals(loginVo.getCode())) throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);

        //.判断用户是否存在,不存在则注册（创建用户）
        LambdaQueryWrapper<UserInfo> queryWrapper =  new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getPhone,loginVo.getPhone());
        UserInfo userInfo = userInfoService.getOne(queryWrapper);
        if(userInfo==null){
            //注册
            userInfo = new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfo.setNickname("用户-"+userInfo.getPhone().substring(6));
            userInfoService.save(userInfo);
        }
        //4.判断用户是否被禁
        if(userInfo.getStatus().equals(BaseStatus.DISABLE)){
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }
        //5.创建并返回TOKEN
        return JwtUtil.CreateToken(userInfo.getId(),loginVo.getPhone());
    }

    @Override
    public UserInfoVo getLoginUserById(Long userId) {
        LambdaQueryWrapper<UserInfo> query = new LambdaQueryWrapper<>();
        query.eq(UserInfo::getId, userId);
        UserInfo userInfo = userInfoService.getOne(query);
        UserInfoVo userInfoVo = new UserInfoVo(userInfo.getNickname(),userInfo.getAvatarUrl());
        return userInfoVo;
    }


}
