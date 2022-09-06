package com.tagging.service;

import com.github.wujun234.uid.UidGenerator;
import com.tagging.dao.mapper.SystemConfigParamDao;
import com.tagging.dao.mapper.UserDao;
import com.tagging.dto.user.RegisterReq;
import com.tagging.dto.user.TokenRsp;
import com.tagging.dto.user.UserLoginReq;
import com.tagging.dto.user.UserLoginRsp;
import com.tagging.entity.SystemConfigParam;
import com.tagging.entity.User;
import com.tagging.enums.DataStatusEnum;
import com.tagging.enums.ResultCodeEnum;
import com.tagging.exception.CMSException;
import com.tagging.utils.DataUtils;
import com.tagging.utils.RSAUtil;
import com.tagging.utils.SaltUtil;
import com.tagging.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LoginService {
    @Resource
    UserDao userDao;
    @Resource
    UidGenerator cachedUidGenerator;
    @Autowired
    SystemConfigParamDao systemConfigParamDao;
    public UserLoginRsp findUser(UserLoginReq userLoginReq) throws Exception {

        String userName = userLoginReq.getUserName();
        List<User> userInfo = userDao.queryByUserName(userName);
        String userPassword = userLoginReq.getUserPassword();
        if(userInfo.size() == 1 && userInfo.get(0).getDataStatus().equals("A")){
            String memberSalt = userInfo.get(0).getUserSalt();
            if (memberSalt.equals("")){
                throw new CMSException(ResultCodeEnum.UNREGISTERED);
            }
            String DbPassword = TokenUtil.md5(userPassword,memberSalt);
            List<User> users = userDao.verify(userName,DbPassword);
            if(users.size() == 1){
                SystemConfigParam systemConfigParam = systemConfigParamDao.selectByPrimaryKey("00000000000002");
                if(systemConfigParam==null)
                    throw new CMSException(401,"无法从数据库获取秘钥");
                //secretInfo.setPrivateKey(systemConfigParam.getParamCode());
                //获取token
                TokenRsp tokenRsp = TokenUtil.getToken(users.get(0), 30L *24*60*60*1000,30*60*1000);
                try {

                    tokenRsp.setToken(RSAUtil.encrypt(tokenRsp.getToken(), RSAUtil.getPublicKey(systemConfigParam.getParamSubCode())));
                    //System.out.println("加密后"+tokenRsp.getToken());
                } catch (Exception j) {
                    throw new CMSException(401,"authority的token加密出错");

                }
                //登录时效为7天
                TokenRsp refreshTokenRsp = TokenUtil.getToken(users.get(0),7*24*60*60*1000,7*24*60*60*1000);
                try {
                    refreshTokenRsp.setToken(RSAUtil.encrypt(refreshTokenRsp.getToken(), RSAUtil.getPublicKey(systemConfigParam.getParamSubCode())));
                } catch (Exception j) {
                    throw new CMSException(401,"authority的token加密出错");

                }
                UserLoginRsp userLoginRsp = new UserLoginRsp();
                userLoginRsp.setToken(tokenRsp);
                userLoginRsp.setRefreshToken(refreshTokenRsp);
                return userLoginRsp;
            }else{
                return null;
            }
        }else{
            return null;
        }

    }

/*    public UserRefreshTokenRsp refreshToken(User user){
        SystemConfigParam systemConfigParam = systemConfigParamDao.selectByPrimaryKey("00000000000002");
        if(systemConfigParam==null)
            throw new CMSException(401,"无法从数据库获取秘钥");
        //获取token
        TokenRsp tokenRsp = TokenUtil.getToken(user,60*60*1000,30*60*1000);
        try {

            tokenRsp.setToken(RSAUtil.encrypt(tokenRsp.getToken(), RSAUtil.getPublicKey(systemConfigParam.getParamSubCode())));
            //System.out.println("加密后"+tokenRsp.getToken());
        } catch (Exception j) {
            throw new CMSException(401,"authority的token加密出错");

        }
        UserRefreshTokenRsp userRefreshTokenRsp = new UserRefreshTokenRsp();

        userRefreshTokenRsp.setToken(tokenRsp);
        return userRefreshTokenRsp;
    }*/
    public void register(RegisterReq registerReq){
        String userId = String.valueOf(cachedUidGenerator.getUID());
        String userName = registerReq.getUserName();
        String userPassword = registerReq.getUserPassword();
        String userPhone = registerReq.getUserPhone();
        String salt = SaltUtil.salt(6);
        String DbPassword = TokenUtil.md5(userPassword,salt);
        User registerData = new User();
        registerData.setUserName(userName);
        registerData.setUserPassword(DbPassword);
        registerData.setUserSalt(salt);

        // 加 if 判断重复注册
//        User temp = new User();
//        temp.setUserName(userName);
//        List<User> tempList = userDao.select(temp);
        List<User> tempList = userDao.queryByUserName(userName);
        if(!tempList.isEmpty()){
            throw new CMSException(410,"重复注册");
        }
        // 查找username是否
        registerData.setUserId(userId);
        registerData.setUserPhone(userPhone);
        registerData.setUserEmail(registerReq.getUserEmail());
        registerData.setSpecCode01("");
        registerData.setSpecCode02("");
        registerData.setSpecCode03("");
        registerData.setCreateTimestamp(DataUtils.getSysTimeByFormat());
        registerData.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
        registerData.setDataStatus(DataStatusEnum.AVAILABLE.getState());
        registerData.setVersion(1);
        registerData.setRemarks("");
        registerData.setReserve("");
        userDao.insert(registerData);
    }
}

