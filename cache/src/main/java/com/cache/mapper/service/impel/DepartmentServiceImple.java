package com.cache.mapper.service.impel;

import com.cache.mapper.DepartmentMapper;
import com.cache.mapper.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DepartmentServiceImple implements DepartmentService {
	@Autowired
	private DepartmentMapper departmentMapper;

	@Override
	public Map<String, Object> findDepartmentData(Integer id) {
		Map<String, Object> departmentData = departmentMapper.findDepartmentData(id);
		System.out.println(departmentData);
		return departmentData;
	}
}
