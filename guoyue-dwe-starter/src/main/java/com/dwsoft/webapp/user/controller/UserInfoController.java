package com.dwsoft.webapp.user.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.dwsoft.core.authority.integration.DefaultPermissionEvaluator;
import com.dwsoft.core.dto.JsonResult;
import com.dwsoft.core.dto.SimpleTreeData;
import com.dwsoft.core.dto.SimpleUserAuthority;
import com.dwsoft.core.role.Role;
import com.dwsoft.core.security.UserUtil;
import com.dwsoft.core.security.ltpa.ILTPATokenHandler;
import com.dwsoft.core.security.userdetails.InternalUser;
import com.dwsoft.core.security.userdetails.UserAuthInfo;
import com.dwsoft.core.security.userdetails.WebAppUser;
import com.dwsoft.core.security.userdetails.WebAppUserDetailService;
import com.dwsoft.webapp.sys.auth.service.ISysAuthRoleService;
import com.dwsoft.webapp.sys.org.entity.SysOrgElement;
import com.dwsoft.webapp.sys.org.entity.SysOrgPerson;
import com.dwsoft.webapp.sys.org.interfaces.ISysOrgCoreService;
import com.dwsoft.webapp.sys.org.service.ISysOrgElementService;
import com.dwsoft.webapp.sys.org.service.ISysOrgPersonService;
import com.dwsoft.webapp.sys.org.vo.simple.SysOrgElementSimpleVo;
import com.dwsoft.webapp.sys.org.wrapper.simple.SysOrgElementSimpleWrapper;
import com.dwsoft.webapp.user.dto.CurrentUserInfoDTO;
import com.dwsoft.webapp.user.dto.LoginParams;
import com.dwsoft.webapp.user.dto.LoginResult;
import com.dwsoft.webapp.user.dto.ModifyPwdParams;
import com.dwsoft.webapp.user.dto.access.ProtectedResourceDTO;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "user", description = "个人用户相关接口", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequestMapping("/user")
public class UserInfoController {

	@Autowired
	private ISysOrgPersonService sysOrgPersonService;
	@Autowired
	private ILTPATokenHandler tokenHandler;
	@Autowired
	protected ISysOrgCoreService sysOrgCoreService;
	@Autowired
	private ISysOrgElementService sysOrgElementService;
	@Autowired
	protected ISysAuthRoleService sysAuthRoleService;
	
	@Autowired
	protected WebAppUserDetailService webAppUserDetailService;
	
	@Autowired
	protected DefaultPermissionEvaluator defaultPermissionEvaluator;

	private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class);

	private UserAuthInfo getUserAuthInfo(SysOrgPerson person) throws Exception {
		UserAuthInfo authInfo = sysOrgCoreService.getUserAuthInfo(person);
		return authInfo;
	}

	private SimpleTreeData createTreeItem(String key, String text) {
		SimpleTreeData treeItem = new SimpleTreeData();
		treeItem.setKey(key);
		treeItem.setValue(key);
		treeItem.setText(text);
		treeItem.setTitle(text);
		return treeItem;
	}

	private boolean isExist(List<SimpleTreeData> list, String category) {
		boolean flag = false;
		for (SimpleTreeData simpleTreeData : list) {
			if (category.equals(simpleTreeData.getTitle())) {
				flag = true;
			}
		}
		return flag;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/getRoleTreeDataByName")
	public List<SimpleTreeData> getRoleTreeDataByName(HttpServletRequest request,
			@ApiParam(value = "fdId", required = true) @RequestParam(value = "fdId", required = true) String fdId)
			throws Exception {
		List<SimpleTreeData> retList = new ArrayList<SimpleTreeData>();

		Optional<SysOrgPerson> Opt = sysOrgPersonService.findById(fdId);

		SysOrgPerson orgPerson = Opt.get();

		UserAuthInfo authInfo = getUserAuthInfo(orgPerson);

		List<Role> roles = sysAuthRoleService.getRolesByOrgIds(authInfo.getAuthOrgIds());

		for (Role role : roles) {
			String name = role.getRoleName();
			if (name.contains("_")) {
				String category = name.substring(1, name.indexOf("_"));
				if (!isExist(retList, category)) {
					SimpleTreeData simpleTreeData = createTreeItem(category, category);
					retList.add(simpleTreeData);
				}
			} else {
				String category = name.substring(1, name.length() - 1);
				SimpleTreeData simpleTreeData = createTreeItem(category, category);
				retList.add(simpleTreeData);
			}
		}

		for (SimpleTreeData simpleTreeData : retList) {
			List<SimpleTreeData> childList = new ArrayList<SimpleTreeData>();

			for (Role role : roles) {
				SimpleTreeData children = new SimpleTreeData();
				if (role.getRoleName().contains(simpleTreeData.getTitle())) {
					children = createTreeItem(role.getAlias(), role.getRoleName());
					childList.add(children);
				}
			}
			simpleTreeData.setChildren(childList);
		}

		return retList;
	}

	public List<SysOrgElementSimpleVo> getDeptList(String fdId) throws Exception {

		Optional<SysOrgPerson> Opt = sysOrgPersonService.findById(fdId);
		SysOrgPerson orgPerson = Opt.get();

		UserAuthInfo authInfo = getUserAuthInfo(orgPerson);

		Set<String> ids = authInfo.getAuthOrgIds();
		
		List<SysOrgElement> results = sysOrgElementService.findAllById(ids);
		
		return results.stream().map((item) -> {
			return SysOrgElementSimpleWrapper.instance().entity2Vo(item);
		}).collect(Collectors.toList());

	}

	@SuppressWarnings("unchecked")
	@GetMapping("/getDeptName")
	public List<?> getDeptName(HttpServletRequest request,
			@ApiParam(value = "fdId", required = true) @RequestParam(value = "fdId", required = true) String fdId)
			throws Exception {

		List<SysOrgElementSimpleVo> list = getDeptList(fdId);

		List<String> retList = list.stream().map((item -> item.getFdName() + " ;")).collect(Collectors.toList());

		return retList;
	}

	@GetMapping(value = "/currentUserInfo")
	@ApiOperation("获取权限验证配置")
	@ResponseBody
	public JsonResult<CurrentUserInfoDTO> currentUserInfo() {
		CurrentUserInfoDTO userInfo = new CurrentUserInfoDTO();
		try {
			SysOrgPerson person = (SysOrgPerson) UserUtil.getInternalUser().get();
			if (person == null) {
				userInfo.setName("匿名用户");
			} else {
				userInfo.setDeptNames("");
				userInfo.setEmail(person.getFdEmail());
				userInfo.setName(person.getFdName());
				userInfo.setUserid(person.getFdId());
				userInfo.setId(person.getFdId());
				if (person.getFdParent() != null) {
					userInfo.setDeptId(person.getFdParent().getFdId());
				}
				UserAuthInfo authInfo = UserUtil.getWebAppUser().getUserAuthInfo();
				userInfo.setCurrentAuthority(Lists.newArrayList(authInfo.getAuthRoleAliases()));
			}
		} catch (Exception e) {
			logger.error("发生错误", e);
			return JsonResult.getFailJsonResult(99, "未知错误");
		}

		return JsonResult.getSuccessJsonResult(userInfo);
	}
	
	private WebAppUser loadWebUserByPerson(SysOrgPerson person){
		try {
			InternalUser user = null;
			if(person != null) {
				user = new InternalUser(person);
				user.setFdLoginName(person.getFdLoginName());
				user.setFdName(person.getFdName());
				user.setFdUserId(person.getFdId());
			} 
			if (user != null) {
				if(logger.isDebugEnabled()) {
					logger.debug("loadUserByUsername person Name:"
							+ user.getFdName());	
				}
			}
			return webAppUserDetailService.buildWebAppUser(user);
		} catch (UsernameNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@PostMapping(value = "/login")
	@ApiOperation("登录验证")
	@ResponseBody
	public LoginResult login(HttpServletRequest request,
			@RequestBody LoginParams loginParams,
			HttpServletResponse response) {
		
		List<String> authorities = new ArrayList<String>();
		String username = loginParams.getUsername();
		String encodedPassword = loginParams.getPassword();
		
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(encodedPassword)) {
			String password = encodedPassword;
			SysOrgPerson person;
			try {
				person = (SysOrgPerson) sysOrgCoreService.loginByLoginName(username, password);
				if (person != null) {
					WebAppUser webUser = loadWebUserByPerson(person);

					UserAuthInfo authInfo = webUser.getUserAuthInfo();

					authorities.addAll(Lists.newArrayList(authInfo.getAuthRoleAliases()));

					tokenHandler.setTokenForUser(response, person.getFdLoginName());
					
					return LoginResult.getSuccessJsonResult(authorities);
				}
				
				return LoginResult.getFailJsonResult(91, "用户名或密码不正确");
			} catch (Exception e) {
				logger.error("发生错误", e);
				return LoginResult.getFailJsonResult(99, "未知错误");
			}
		}
		
		return LoginResult.getFailJsonResult(99, "");
	}

	@GetMapping(value = "/logout")
	@ApiOperation("登出")
	@ResponseBody
	public JsonResult<SimpleUserAuthority> logout(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		SimpleUserAuthority retObj = new SimpleUserAuthority();
		retObj.getCurrentAuthority().add("guest");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		tokenHandler.logout(request, response, authentication);

		if (authentication != null) {
			new SecurityContextLogoutHandler().logout(request, response, authentication);
		}

		return JsonResult.getSuccessJsonResult(retObj);
	}
	
	@PostMapping(value = "/canAccessResources")
	@ApiOperation("评估当前用户是否有权限访问指定的资源")
	@ResponseBody
	public JsonResult<List<Boolean>> canAccessResources(HttpServletRequest request,
			HttpServletResponse response, @RequestBody ProtectedResourceDTO[] resources) {
		List<Boolean> results = new ArrayList<>();
		
		for (int i = 0; i < resources.length; i++) {
			ProtectedResourceDTO protectedResource = resources[i];
			
			boolean hasPermission = false;
			
			if (StringUtils.isNotBlank(protectedResource.getModelId())
					&& StringUtils.isNotBlank(protectedResource.getModelName())
					&& StringUtils.isNotBlank(protectedResource.getActionType())) {
				
				// read write等基础权限，会由defaultPermissionEvaluator来评估，
				// 一些特殊的权限，比如 print,download等，defaultPermissionEvaluator会调用对应的模块的service去评估
				try {
					hasPermission = defaultPermissionEvaluator.hasPermission(null, protectedResource.getModelId(),
							protectedResource.getModelName(), protectedResource.getActionType());
				} catch (Exception e) {
					logger.error("评估当前用户是否有权限访问指定的资源时发生错误", e);
				}
			}
			results.add(hasPermission);
		}
		
		return JsonResult.getSuccessJsonResult(results);
	}
	
	
	@PostMapping(value = "/modifyPasswd")
	@ApiOperation("修改密码")
	@ResponseBody
	public JsonResult<String> modifyPasswd(HttpServletRequest request, HttpServletResponse response, @RequestBody ModifyPwdParams params)
			throws IOException {
		
		String userId = UserUtil.getInternalUser().getFdUserId();
		String oldPassword = params.getFdOldPassword();
		
		if (StringUtils.isNotBlank(oldPassword)) {
			try {
				SysOrgPerson person1 = (SysOrgPerson) sysOrgCoreService.loginByUserId(userId,
						params.getFdOldPassword());
				if (person1 != null) {
					if(StringUtils.equals(params.getFdNewPassword(), params.getFdConfirmPassword())) {
						boolean updated = sysOrgPersonService.updatePersonPassword(userId, params.getFdNewPassword());
						
						if(updated) {
							return JsonResult.ok("ok");
						}
						
						return JsonResult.getFailJsonResult("修改密码失败!");
					} else {
						return JsonResult.getFailJsonResult("两次密码输入不一致!");
					}
				}
				
				return JsonResult.getFailJsonResult("旧密码不正确!");
			} catch (Exception e) {
				logger.error("发生错误", e);
				return JsonResult.getFailJsonResult(99, "未知错误");
			}
		}
		return JsonResult.getFailJsonResult("旧密码不正确!");
	}
}
