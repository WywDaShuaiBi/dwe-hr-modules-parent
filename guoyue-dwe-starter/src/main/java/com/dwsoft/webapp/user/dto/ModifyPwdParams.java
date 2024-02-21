package com.dwsoft.webapp.user.dto;

public class ModifyPwdParams {
	private String fdOldPassword;
	private String fdNewPassword;
	private String fdConfirmPassword;

	public String getFdOldPassword() {
		return fdOldPassword;
	}

	public void setFdOldPassword(String fdOldPassword) {
		this.fdOldPassword = fdOldPassword;
	}

	public String getFdNewPassword() {
		return fdNewPassword;
	}

	public void setFdNewPassword(String fdNewPassword) {
		this.fdNewPassword = fdNewPassword;
	}

	public String getFdConfirmPassword() {
		return fdConfirmPassword;
	}

	public void setFdConfirmPassword(String fdConfirmPassword) {
		this.fdConfirmPassword = fdConfirmPassword;
	}
}
