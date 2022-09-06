package com.tagging.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.tagging.dao.mapper.SystemConfigParamDao;
import com.tagging.dao.mapper.UserDao;
import com.tagging.entity.SystemConfigParam;
import com.tagging.entity.User;
import com.tagging.enums.ResultCodeEnum;
import com.tagging.exception.CMSException;
import com.tagging.service.LoginService;
import com.tagging.utils.RSAUtil;
//import jdk.nashorn.internal.ir.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Zeng
 * @date 2020/2/17 11:00
 */
public class TokenInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);
    @Autowired
    LoginService loginService;

    @Resource
    UserDao userDao;

    @Autowired
    SystemConfigParamDao systemConfigParamDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从请求头中获取token
        String token = request.getHeader("mark-Token");
        String path = request.getRequestURI();
        //执行认证
        if (token == null || token.equals("") || token.equals("undefined")) {
            //未登录
            logger.error("未登录tokenError" + token + "-path:" + path);
//            throw new CMSException(ResultCodeEnum.NO_TOKEN);
        }
        try {
            SystemConfigParam systemConfigParam = systemConfigParamDao.selectByPrimaryKey("00000000000001");
            if (systemConfigParam == null)
                throw new CMSException(401, "无法从数据库获取秘钥");
            token = RSAUtil.decrypt(token, RSAUtil.getPrivateKey(systemConfigParam.getParamSubCode()));
        } catch (Exception var20) {
            logger.error("tokenError" + var20.getMessage());
            throw new CMSException(404, "token解密错误");
        }
        //获取token中的userId
        String userId;
        try {
            userId = JWT.decode(token).getAudience().get(0);
        } catch (JWTDecodeException j) {
            //token异常!请重新登陆
            logger.error("tokenError" + "-path:" + path + j.getMessage());
            j.printStackTrace();
            throw new CMSException(ResultCodeEnum.TOKEN_EXECUTE_ERROR);
        }
        User user = userDao.queryById(userId);
        if (user == null) {
            //未找到该用户!
            throw new CMSException(ResultCodeEnum.USER_ERROR);
        }
        // 验证 token
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getUserPassword())).build();
        try {
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            //token异常!请重新登陆
            logger.error("tokenError" + "-path:" + path + e.getMessage());
            throw new CMSException(ResultCodeEnum.TOKEN_EXECUTE_ERROR);
        }
        return true;

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}