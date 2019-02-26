package com.cache.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cache.mapper.service.DepartmentService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/app")
public class DepartmentController {
	@Autowired
	private DepartmentService departmentService;
	@RequestMapping("findDepartmentData.do")
	public JSONObject findDepartmentData(Integer id){
		Map<String, Object> departmentData = departmentService.findDepartmentData(id);
		JSONObject result = new JSONObject();
		result.put("a",departmentData);
		return result;
	}
}
