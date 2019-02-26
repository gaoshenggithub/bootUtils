package com.cache.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface DepartmentMapper {

	@Select("select * from department where id = #{id}")
	public Map<String,Object> findDepartmentData(Integer id);
}
