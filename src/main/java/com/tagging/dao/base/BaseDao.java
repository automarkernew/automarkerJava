package com.tagging.dao.base;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface BaseDao<T> extends Mapper<T>, MySqlMapper<T> {
}