package com.alisms.controller;


import com.alibaba.fastjson.JSONObject;
import com.alisms.utils.NumberUtil;
import com.alisms.utils.RandomUtils;
import com.alisms.utils.SmsUtil;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码
 *
 * @author
 * @date 2018-11-09 10:02:20
 */

/**
 * 短信接口
 *
 * @param
 * @return
 * @throws
 */
@Controller
@RequestMapping("/app")
public class ValidateCodeController {

	private static final Logger logger = LoggerFactory
			.getLogger(ValidateCodeController.class);
	private static int expiresTime = 1 * 60; // 失效时时间设置${}

	//短信发送接口
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@PostMapping("/smtUser/getVerificationCode.do")
	@ResponseBody
	public JSONObject getVerificationCode(JSONObject parmas) throws IllegalAccessException, ClientException {
		HashMap<String, Object> map1 = new HashMap<>();
		String mobile = parmas.get("phone").toString();
		//判NULL
		if (StringUtils.isBlank(mobile)) {
		}
		//判断数据库是否有此用户
		/**
		 * 业务逻辑
		 */
		//假逻辑
		String resultUser = null;
		if (resultUser == null) {
		}
		//判断格式
		if (NumberUtil.isNumber(mobile)) {

		}
		//判断验证码是否重复
		if (StringUtils.isNotBlank(redisTemplate.opsForValue().get(mobile))) {
		}
		//获取验证码
		String code = RandomUtils.randomNumber();
		SendSmsResponse resultSms = SmsUtil.sendSms(mobile, code);
		//验证码返回参数逻辑
		/**
		 * 都为OK表示成功
		 */
		if (!resultSms.getCode().equals("OK") || !resultSms.getMessage().equals("OK")) {
		}
		//开始加入缓存
		// 将验证码以<key,value>形式缓存到redis
		redisTemplate.opsForValue().set(mobile, code, expiresTime,
				TimeUnit.SECONDS);
		// System.out.println(data);
		map1.put("verificationCode", code);
		JSONObject res = new JSONObject();
		res.put("val",map1);
		return res;
	}

	/**
	 * 验证短信验证接口
	 *
	 * @param
	 * @return
	 */
	@RequestMapping("/smtUser/checkVerificationCode.do")
	@ResponseBody
	public JSONObject checkVerificationCode(JSONObject params) {
		String mobile = params.get("phone").toString();
		String code = params.get("code").toString();
		//判空
		if (StringUtils.isEmpty(mobile)) {
		}
		//判空
		if (StringUtils.isEmpty(code)) {
		}
		//从redis获取数据
		String sm = redisTemplate.opsForValue().get(mobile);
		if (StringUtils.isEmpty(sm)) {
		}
		//比较验证码
		if (!code.equals(sm)) {
		}
		redisTemplate.delete(mobile);
		JSONObject result = new JSONObject();
		result.put("code", 200);
		return result;
	}


	/**
	 * 短信查询接口
	 *
	 * @param params
	 * @return
	 */
	@RequestMapping("/smtUser/querySms.do")
	@ResponseBody
	public JSONObject querySms(JSONObject params) {
		JSONObject result = new JSONObject();
		String bizld = params.get("bizld").toString();
		String phone = params.get("phone").toString();
		//判空
		if (StringUtils.isBlank(phone)) {

		}
		QuerySendDetailsResponse querySendDetailsResponse = null;
		try {
			querySendDetailsResponse = SmsUtil.querySendDetails(bizld, phone);
		} catch (ClientException e) {
			//逻辑
			e.printStackTrace();
		}
		List<QuerySendDetailsResponse.SmsSendDetailDTO> r = querySendDetailsResponse.getSmsSendDetailDTOs();
		result.put("code", 200);
		result.put("data", r);
		return result;
	}

	public static void main(String[] args) throws IllegalAccessException {

	}
}