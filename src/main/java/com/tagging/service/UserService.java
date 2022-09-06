package com.tagging.service;

import com.github.wujun234.uid.UidGenerator;
import com.tagging.common.Constants;
import com.tagging.dao.mapper.UserDao;
import com.tagging.dto.QuerySummaryRsp;
import com.tagging.dto.user.*;
import com.tagging.entity.User;
import com.tagging.enums.DataStatusEnum;
import com.tagging.enums.VideoInformation.IsMotedEnum;
import com.tagging.exception.CMSException;
import com.tagging.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class UserService {

    @Resource
    UserDao userDao;

    @Resource
    UidGenerator cachedUidGenerator;

    public void insert(UserInsertReq req){
        User user = new User();
        user.setUserId(String.valueOf(cachedUidGenerator.getUID()));
        user.setUserName(req.getUserName());
        user.setUserPassword(req.getUserPassword());
        user.setUserPhone(req.getUserPhone());
        user.setUserEmail(req.getUserEmail());
        user.setSpecCode01("");
        user.setSpecCode02("");
        user.setSpecCode03("");
        user.setCreateTimestamp(DataUtils.getSysTimeByFormat());
        user.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
        user.setDataStatus(DataStatusEnum.AVAILABLE.getState());
        user.setVersion(1);
        user.setRemarks("");
        user.setReserve("");
        userDao.insert(user);
    }

    public List<UserQueryRsp> query(long offset, int queryCount, UserQueryReq req){
        return userDao.query(
                offset,
                queryCount,
                req.getUserName(),
                req.getUserPhone(),
                req.getUserEmail()
        );
    }

    public QuerySummaryRsp count(UserQueryReq req){
        QuerySummaryRsp querySummaryRsp = new QuerySummaryRsp();
        long num = userDao.count(
                req.getUserName(),
                req.getUserPhone(),
                req.getUserEmail()
        );
        querySummaryRsp.setDataAmount(num);
        return querySummaryRsp;
    };

    public void delete(UserDeleteReq req){
        User user = userDao.queryById(req.getUserId());
        user.setDataStatus(DataStatusEnum.DELETED.getState());
        user.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
        user.setVersion(user.getVersion() + 1);
        userDao.updateByPrimaryKeySelective(user);
    }

    public void update(UserUpdateReq req){
        User user = userDao.queryById(req.getUserId());
        if(user == null){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "该用户不存在或已被删除");
        } else {
            user.setUserName(req.getUserName());
            user.setUserPhone(req.getUserPhone());
            user.setUserEmail(req.getUserEmail());
            user.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
            user.setVersion(user.getVersion() + 1);
            userDao.updateByPrimaryKeySelective(user);
        }
    }

    public void updatePassword(UserUpdatePasswordReq req){
        User user = userDao.queryById(req.getUserId());
        if(user == null){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "该用户不存在或已被删除");
        } else {
            //判断密码是否正确
            if (!user.getUserPassword().equals(req.getUserPassword())) {
                throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "输入的密码错误,请尝试重新输入");
            } else {
                user.setUserPassword(req.getNewPassword());
                user.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
                user.setVersion(user.getVersion() + 1);
                userDao.updateByPrimaryKeySelective(user);
            }
        }
    }

}
