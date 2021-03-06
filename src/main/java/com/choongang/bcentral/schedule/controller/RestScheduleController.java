package com.choongang.bcentral.schedule.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.choongang.bcentral.schedule.service.CheckValidationService;
import com.choongang.bcentral.schedule.service.ScheduleService;
import com.choongang.bcentral.schedule.vo.ScheduleVo;
import com.choongang.bcentral.schedule.vo.SetScheduleVo;
import com.choongang.bcentral.server.service.ServerService;
import com.choongang.bcentral.server.vo.PageVo;
import com.choongang.bcentral.server.vo.ServerVo;
import com.choongang.bcentral.user.service.UserService;
import com.choongang.bcentral.user.vo.UserVo;
import com.google.gson.Gson;

// [작업관리] 비동기 식 Controller
@RestController
@RequestMapping("/schedule/*")
public class RestScheduleController {
	
	@Autowired
	private ScheduleService scheduleService;
	
	@Autowired
	private CheckValidationService ckService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ServerService serverService;
	
	@RequestMapping("getList")
	public HashMap<String, Object> getList(Integer year, Integer month, HttpSession session){
		HashMap<String, Object> data = new HashMap<String, Object>();
		
		UserVo userVo = (UserVo) session.getAttribute("userInfo");
		
		data.put("scheduleList", scheduleService.getScheduleList(year, month, userVo.getUser_no())); 
		
		return data;
	}
	
	@RequestMapping("getUserList")
	public HashMap<String, Object> getUserList(HttpSession session){
		HashMap<String, Object> data = new HashMap<String, Object>();
		
		UserVo userVo = (UserVo) session.getAttribute("userInfo");
		
		data.put("userList", userService.getTotalUserList(userVo.getUser_no()));
		
		return data;
	}
	
	@RequestMapping("getScheduleInfo")
	public HashMap<String, Object> getScheduleInfo(Integer sc_no, HttpSession session, Authentication auth){
		HashMap<String, Object> data = new HashMap<String, Object>();
		
		System.out.println("Authentication에 등록되어 있는 ID : " + auth.getName() + ", 권한 : " + auth.getAuthorities());
		
		UserVo userVo = (UserVo) session.getAttribute("userInfo");
		
		System.out.println("userInfo에 등록되어 있는 ID : " + userVo.getName());
		
		ScheduleVo scVo = scheduleService.getScheduleInfo(sc_no);
		
		data.put("scheduleInfo", scVo);
		data.put("serverList", scheduleService.getServerList(sc_no));
		data.put("userList", userService.getTotalUserList(userVo.getUser_no()));
		data.put("start_time", scVo.getStart_time().toString());
		data.put("end_time", scVo.getEnd_time().toString());
		return data;
	}
	
	@RequestMapping("getServerList")
	public HashMap<String, Object> getServerList(PageVo param){
		HashMap<String, Object> data = new HashMap<String, Object>();
		int count = 0;
		
		if(param.getSearchWord() != null){
			String searchWord = param.getSearchWord();
			searchWord = searchWord.replaceAll("\\\\" , "\\\\\\\\").replaceAll("%" , "\\\\%").replaceAll("_", "\\\\_");			
			param.setSearchWord(searchWord);
		}
		
		ArrayList<ServerVo> serverList = serverService.getServerList(param);
		
		for(ServerVo vo : serverList) {
			int server_no = vo.getServer_no();
			count = vo.getCount();
			String status = serverService.getServerState(server_no);
			
			vo.setStatus(status);
		}
	
		int rows = param.getRows();
		int records = count;
		int total = (int) Math.ceil((double)records / rows);

		data.put("rows", serverList); // 데이터
		data.put("records", records); // 데이터의 전체 개수 (viewrecords 에 사용됨)
		data.put("page", param.getPage()); // 현재 페이지
		data.put("total", total); // 총 페이지
		
		return data;
	}
	
	@RequestMapping("regSchedule")
	public HashMap<String, Object> regSchedule(HttpServletRequest param, 
											   HttpSession session,
											   SetScheduleVo ssVo,
											   Integer selectManager,
											   String start_date, 
											   String end_date,
											   String start_time,
											   String end_time){
		HashMap<String, Object> data = new HashMap<String, Object>();
		
		/* 계층 구조를 위해서 scheduleVo에 입력되는 user_no 는 담당자 정보로 변경
		 * UserVo userVo = (UserVo) session.getAttribute("userInfo");
		 * 
		 * ssVo.setUser_no(userVo.getUser_no());
		 */
		
		ssVo.setUser_no(selectManager);
		
		System.out.println(new Gson().toJson(ssVo));
		
		int result = ckService.validateScheduleDate(ssVo);
		System.out.println("result : " + result);
		if(result == 0) {
			scheduleService.regSchedule(ssVo);
		}
		
		data.put("result", result);
		
		return data;
	}
	
	@RequestMapping("delSchedule")
	public HashMap<String, Object> delSchedule(Integer delete_radio, SetScheduleVo ssVo, String cur_date, HttpSession session){
		HashMap<String, Object> data = new HashMap<String, Object>();
		
		UserVo userVo = (UserVo) session.getAttribute("userInfo");
		
		ssVo.setUser_no(userVo.getUser_no());
		
		if(delete_radio == 0) {
			scheduleService.delCat0(ssVo.getSc_no());
			data.put("result", 0);
		} else if(delete_radio == 1) {
			scheduleService.delCat1(ssVo, cur_date);
			data.put("result", 0);
		} else if(delete_radio == 2) {
			scheduleService.delCat2(ssVo, cur_date);
			data.put("result", 0);
		} else {
			data.put("result", 1);
		}
		
		return data;
	}
	
	@RequestMapping("modSchedule")
	public HashMap<String, Object> modSchedule(SetScheduleVo ssVo, String cur_date, Integer changeManager){
		HashMap<String, Object> data = new HashMap<String, Object>();
		
		Gson gson = new Gson();
		System.out.println(gson.toJson(ssVo));
		System.out.println(cur_date);
		System.out.println(changeManager);
		
		ssVo.setUser_no(changeManager);
		
		scheduleService.modSchedule(ssVo);
		
		return data;
	}
	
	@RequestMapping("isExistTitle")
	public HashMap<String, Object> isExistTitle(String title){
		
		HashMap<String, Object> data = new HashMap<String, Object>();
		System.out.println(title);
		boolean result = scheduleService.isExistTitle(title);
		
		data.put("result", result);		
		
		return data;
	}
}
