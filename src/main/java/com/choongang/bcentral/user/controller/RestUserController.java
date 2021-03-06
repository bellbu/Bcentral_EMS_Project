package com.choongang.bcentral.user.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.choongang.bcentral.user.service.AESUtil;
import com.choongang.bcentral.user.service.UserService;
import com.choongang.bcentral.user.vo.UserPageVo;
import com.choongang.bcentral.user.vo.UserVo;
import com.google.gson.Gson;

// [사용자관리] 비동기적 Controller
@RestController
@RequestMapping("/user/*")
public class RestUserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	AESUtil aes;

	@RequestMapping("getUserList")
	public HashMap<String, Object> getUserList(UserPageVo param, HttpSession session) {
		HashMap<String, Object> data = new HashMap<String, Object>();

		UserVo userVo = (UserVo) session.getAttribute("userInfo");
		param.setUser_no(userVo.getUser_no());
		
		if(param.getSearchWord() != null) {
			String searchWord = param.getSearchWord();
			searchWord = searchWord.replaceAll("\\\\" , "\\\\\\\\").replaceAll("%" , "\\\\%").replaceAll("_", "\\\\_");
			
			param.setSearchWord(searchWord);
		}
		
		ArrayList<UserVo> userList = userService.getUserList(param);
		
		int rows = param.getRows();
		int records = userService.getUserCount(param);
		int total = (int) Math.ceil((double) records / rows);

		data.put("rows", userList); //데이터
		data.put("records", records); // 데이터의 전체 개수  //viewrecords에 사용됨
		data.put("page", param.getPage()); //현재 페이지
		data.put("total", total); //총 페이지

		return data;
	}

	@RequestMapping("registerUser")
	public HashMap<String, Object> insertUser(UserVo param, HttpSession session) throws Exception {
		HashMap<String, Object> data = new HashMap<String, Object>();
		
		UserVo userVo = (UserVo) session.getAttribute("userInfo");
		param.setParent(userVo.getUser_no());
		userService.registerUser(param);

		data.put("result", "success");

		return data;
	}

	@RequestMapping("deleteUser")
	public HashMap<String, Object> deleteUser(Integer[] param,
											  Integer changeManager) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		System.out.println(new Gson().toJson(param));
		System.out.println(changeManager);
		
		userService.changeScheduleManager(param, changeManager);
		userService.deleteUser(param);

		data.put("result", "success");

		return data;
	}
	
	@RequestMapping("getUser")
	public HashMap<String, Object> getUser(int user_no) throws Exception {
		HashMap<String, Object> data = new HashMap<String, Object>();
		
		UserVo user = userService.getUser(user_no);
		String decryptphone = aes.decrypt(user.getPhone());
		user.setPhone(decryptphone);
		
		String decryptemail = aes.decrypt(user.getEmail());
		user.setEmail(decryptemail);
		
		data.put("user", user);
		
		return data;
	}
	
	@RequestMapping("updateUser")
	public HashMap<String, Object> updateUser(UserVo param) throws Exception{
		HashMap<String, Object> data = new HashMap<String, Object>();

		userService.updateUser(param);
		
		data.put("result", "success");

		return data;
	}

	@RequestMapping("isExistId")
	public HashMap<String, Object> isExistId(String id) {
		HashMap<String, Object> data = new HashMap<String, Object>();

		boolean result = userService.isExistId(id);

		data.put("result", result);

		return data;
	}
	
	@RequestMapping("getSelectUserList")
	public HashMap<String, Object> getSelectUserList(HttpSession session, @RequestBody ArrayList<Integer> userNos){
		HashMap<String, Object> data = new HashMap<String, Object>();
		
		UserVo userVo = (UserVo) session.getAttribute("userInfo");
		
		ArrayList<UserVo> userList = userService.getTotalUserList(userVo.getUser_no());
		
		ArrayList<UserVo> selectUserList = new ArrayList<UserVo>();
		System.out.println(userNos.size());
		if(!userNos.isEmpty()) {
			for(UserVo uVo : userList) {
				int cur_user_no = uVo.getUser_no();
				int count = 0;
				 
				for(Integer delete_no : userNos) {
					if(cur_user_no == delete_no) {
						count++;
					}
				}
				
				if(count == 0) {
					selectUserList.add(uVo);
				}
			}
		} else {
			selectUserList = userList;
		}
		
		data.put("selectUserList", selectUserList);
		
		return data;
	}
}
