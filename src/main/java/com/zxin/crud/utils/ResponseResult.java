package com.zxin.crud.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用的返回的类
 */
public class ResponseResult {

	//状态码   100-成功    200-失败
	private int code;
	//提示信息
	private String msg;
	
	//用户要返回给浏览器的数据
	private Map<String, Object> returnDataMap = new HashMap<>();

	public static ResponseResult success(){
		ResponseResult result = new ResponseResult();
		result.setCode(100);
		result.setMsg("处理成功！");
		return result;
	}
	
	public static ResponseResult fail(){
		ResponseResult result = new ResponseResult();
		result.setCode(200);
		result.setMsg("处理失败！");
		return result;
	}

	// 支持链式操作
	public ResponseResult add(String key, Object value){
		this.getReturnDataMap().put(key, value);
		return this;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Map<String, Object> getReturnDataMap() {
		return returnDataMap;
	}

	public void setReturnDataMap(Map<String, Object> returnDataMap) {
		this.returnDataMap = returnDataMap;
	}
}
