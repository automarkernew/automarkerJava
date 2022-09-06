package com.tagging.dao.mapper;

import com.tagging.dao.base.BaseDao;
import com.tagging.dto.user.UserQueryRsp;
import com.tagging.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDao extends BaseDao<User> {
    public List<UserQueryRsp> query(
            @Param("offset") long offset,
            @Param("queryCount") int queryCount,
            @Param("userName") String userName,
            @Param("userPhone") String userPhone,
            @Param("userEmail") String userEmail
    );

    public User queryById(
            @Param("userId") String userId
    );
    public long count(
            @Param("userName") String userName,
            @Param("userPhone") String userPhone,
            @Param("userEmail") String userEmail
    );

    List<User> verify(@Param("userName") String userName,
                      @Param("userPassword") String userPassword);

    List<User> queryByUserName(@Param("userName") String userName);
}
