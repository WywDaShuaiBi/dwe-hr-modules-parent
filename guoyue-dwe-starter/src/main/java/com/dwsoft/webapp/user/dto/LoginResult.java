package com.dwsoft.webapp.user.dto;

import java.util.List;

public class LoginResult {
	public final static int MAX_PASSWORD_TRY_TIMES = 5;
	private String loginType;
	private Boolean success;
	private Integer code;
	private String msg;
	private List<String> data;

	public String getLoginType() {
		return loginType;
	}
	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}
	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

	public static LoginResult getFailJsonResult(int errorCode, String errorMsg) {
		LoginResult result = new LoginResult();
		result.setSuccess(false);
		result.setCode(errorCode);
		result.setMsg(errorMsg);

		return result;
	}

	public static LoginResult getSuccessJsonResult(List<String> data) {
		LoginResult result = new LoginResult();
		result.setSuccess(true);
		result.setCode(0);
		result.setData(data);

		return result;
	}
}
