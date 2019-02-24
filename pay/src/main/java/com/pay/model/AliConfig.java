package com.pay.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class AliConfig {

	// 1.商户appid
	@Value("${ali.pay.APPID}")
	public String APPID;

	//2.私钥 pkcs8格式的
	@Value("${ali.pay.RSA_PRIVATE_KEY}")
	public String RSA_PRIVATE_KEY;

	//3.支付宝公钥
	@Value("${ali.pay.ALIPAY_PUBLIC_KEY}")
	public String ALIPAY_PUBLIC_KEY;

	//4.
	//服务器异步通知页面路径 需http://或者https://格式的完整路径，
	// 不能加?id=123这类自定义参数，必须外网可以正常访问
	//异步回调
	@Value("${ali.pay.NOTIFY_URL}")
	public String NOTIFY_URL;

	//5.页面跳转同步通知页面路径 需http://或者https://格式的完整路径，
	// 不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
	//public   String RETURN_URL = "http://192.168.31.248:9528/app/alipay/return_url.do";

	// 6.请求支付宝的网关地址
	@Value("${ali.pay.URL}")
	public String URL;

	// 7.编码
	@Value("${ali.pay.CHARSET}")
	public String CHARSET;

	// 8.返回格式
	@Value("${ali.pay.FORMAT}")
	public String FORMAT;

	// 9.加密类型
	@Value("${ali.pay.SIGNTYPE}")
	public String SIGNTYPE;

	public String getAPPID() {
		return APPID;
	}

	public void setAPPID(String APPID) {
		this.APPID = APPID;
	}

	public String getRSA_PRIVATE_KEY() {
		return RSA_PRIVATE_KEY;
	}

	public void setRSA_PRIVATE_KEY(String RSA_PRIVATE_KEY) {
		this.RSA_PRIVATE_KEY = RSA_PRIVATE_KEY;
	}

	public String getALIPAY_PUBLIC_KEY() {
		return ALIPAY_PUBLIC_KEY;
	}

	public void setALIPAY_PUBLIC_KEY(String ALIPAY_PUBLIC_KEY) {
		this.ALIPAY_PUBLIC_KEY = ALIPAY_PUBLIC_KEY;
	}

	public String getNOTIFY_URL() {
		return NOTIFY_URL;
	}

	public void setNOTIFY_URL(String NOTIFY_URL) {
		this.NOTIFY_URL = NOTIFY_URL;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String URL) {
		this.URL = URL;
	}

	public String getCHARSET() {
		return CHARSET;
	}

	public void setCHARSET(String CHARSET) {
		this.CHARSET = CHARSET;
	}

	public String getFORMAT() {
		return FORMAT;
	}

	public void setFORMAT(String FORMAT) {
		this.FORMAT = FORMAT;
	}

	public String getSIGNTYPE() {
		return SIGNTYPE;
	}

	public void setSIGNTYPE(String SIGNTYPE) {
		this.SIGNTYPE = SIGNTYPE;
	}

	@Override
	public String toString() {
		return "AliConfig{" +
				"APPID='" + APPID + '\'' +
				", RSA_PRIVATE_KEY='" + RSA_PRIVATE_KEY + '\'' +
				", ALIPAY_PUBLIC_KEY='" + ALIPAY_PUBLIC_KEY + '\'' +
				", NOTIFY_URL='" + NOTIFY_URL + '\'' +
				", URL='" + URL + '\'' +
				", CHARSET='" + CHARSET + '\'' +
				", FORMAT='" + FORMAT + '\'' +
				", SIGNTYPE='" + SIGNTYPE + '\'' +
				'}';
	}
}
