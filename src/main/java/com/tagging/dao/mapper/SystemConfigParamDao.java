package com.tagging.dao.mapper;
import com.tagging.dao.base.BaseDao;
import com.tagging.entity.SystemConfigParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemConfigParamDao extends BaseDao<SystemConfigParam>{
}
