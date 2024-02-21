package com.dwsoft.webapp.user.dto;

import java.io.Serializable;
import java.util.List;

public class CurrentUserInfoDTO implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8836343273690185094L;
	
	private String id;
	private String name;//: 'Serati Ma',
	private String avatar;//: 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png',
	private String userid;//: '00000001',
	private String email;//: 'antdesign@alipay.com',
	private String deptId; //部门id
	private String signature;//: '海纳百川，有容乃大',
	private String position;//: '海纳百川，有容乃大',
	private String title;//: '交互专家',
	private String deptNames;//: '蚂蚁金服－某某某事业群－某某平台部－某某技术部－UED',
	private String phone;
	private Integer notifyCount;
	private Integer unreadCount;
	private Integer todoCount;
	private List<String> currentAuthority;
	
	private List<UserTagDTO> userTags;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	public String getDeptId() {
		return deptId;
	}

	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDeptNames() {
		return deptNames;
	}

	public void setDeptNames(String deptNames) {
		this.deptNames = deptNames;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Integer getNotifyCount() {
		return notifyCount;
	}

	public void setNotifyCount(Integer notifyCount) {
		this.notifyCount = notifyCount;
	}

	public Integer getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(Integer unreadCount) {
		this.unreadCount = unreadCount;
	}

	public Integer getTodoCount() {
		return todoCount;
	}

	public void setTodoCount(Integer todoCount) {
		this.todoCount = todoCount;
	}

	public List<UserTagDTO> getUserTags() {
		return userTags;
	}

	public void setUserTags(List<UserTagDTO> userTags) {
		this.userTags = userTags;
	}

	public List<String> getCurrentAuthority() {
		return currentAuthority;
	}

	public void setCurrentAuthority(List<String> currentAuthority) {
		this.currentAuthority = currentAuthority;
	}
	
}
