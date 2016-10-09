package com.xb.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xb.base.BaseController;
import com.xb.common.BusinessException;
import com.xb.common.WFConstants;
import com.xb.persistent.RsWorkflow;
import com.xb.service.IRsWorkflowService;
import com.xb.service.IWfInstHistService;
import com.xb.service.IWfInstanceService;
import com.xb.service.IWfTaskService;
import com.xb.vo.TaskOptVO;

/**
 * 跟task相关的都在此controller
 * 跟wf相关放在WFController
 * @author yuxiaobin
 *
 */
@Controller
@RequestMapping("/task")
public class WFTaskController extends BaseController {
	
	private static Logger log = LogManager.getLogger(WFApiController.class);
	
	private static final String WARN_MSG_BUZ_STATUS = "Invalid buzStatus[%s] found for refMkid=%s, parse buzStatus ignored ";
	
	@Autowired
	IWfTaskService taskService;
	@Autowired
	IWfInstHistService instHistService;
	@Autowired
	IWfInstanceService instService;
	@Autowired
	IRsWorkflowService rsWfService;
	
	@RequestMapping("/process")
	@ResponseBody
	public Object processTask(@RequestBody TaskOptVO optVO, HttpSession session){
		JSONObject result = new JSONObject();
		try {
			taskService.processTask(optVO, getCurrUserId(session));
			result.put("message", "success");
		} catch (BusinessException e) {
			result.put("message", e.getErrorMsg());
		}
		return result;
	}
	
	
	@RequestMapping("/loadprocess")
	public Object loadProcessTask(HttpSession session, HttpServletRequest req){
		
		String instNum = req.getParameter("instNum");
		String refMkid = req.getParameter(PARM_REF_MKID);
		req.setAttribute("instNum", instNum);
		req.setAttribute(PARM_REF_MKID, refMkid);
		String optCode = req.getParameter("optCode");
		if(WFConstants.OptTypes.TRACK.equals(optCode)){
			return "wf-popup-track";
		}else{
			req.setAttribute("optCode", optCode);
			//提交，退回，否决等操作事务页面
			TaskOptVO optVO = new TaskOptVO();
			optVO.setRefMkid(refMkid);
			optVO.setInstNum(Integer.parseInt(instNum));
			optVO.setOptCode(optCode);
			req.setAttribute("TX_PR_CHOICES",taskService.getCurrentTaskByRefNum(optVO).getTxPrChoices());
			return "wf-popup-opt";
		}
	}
	
	@RequestMapping("/next/tasks")
	@ResponseBody
	public Object getNextTasks(@RequestBody TaskOptVO optVO, HttpSession session){
		String currUserId = getCurrUserId(session);
		if(!StringUtils.isEmpty(currUserId)){
			optVO.setCurrUserId(currUserId);
		}
		JSONObject result = new JSONObject();
		result.put("records", taskService.getNextTasksByOptCode(optVO));
		return result;
	}
	
	@RequestMapping("/next/usergroups")
	@ResponseBody
	public Object getNextAssigners(@RequestBody TaskOptVO optVO, HttpSession session){
		String currUserId = getCurrUserId(session);
		if(!StringUtils.isEmpty(currUserId)){
			optVO.setCurrUserId(currUserId);
		}
		JSONObject result = new JSONObject();
		result.put("result", taskService.getNextAssignersByOptCode(optVO));
		return result;
	}
	
	@RequestMapping(value="/buzStatus",method=RequestMethod.GET )
	@ResponseBody
	public Object getBuzStatus(HttpServletRequest req	){
		String refMkid = req.getParameter(PARM_REF_MKID);
		if(StringUtils.isEmpty(refMkid)){
			return new JSONArray(0);
		}
		RsWorkflow res = rsWfService.selectById(refMkid);
		String buzStatusSet = res.getBuzStatusSet();
		if(StringUtils.isEmpty(buzStatusSet)){
			log.warn(String.format(WARN_MSG_BUZ_STATUS, "buzStatusSet is empty",refMkid));
			return new JSONArray(0);
		}
		String[] array = buzStatusSet.split(";");
		JSONArray result = new JSONArray(array.length);
		JSONObject json = null;
		for(String str:array){
			String[] arr = str.split(":");
			if(arr.length!=2){
				log.warn(String.format(WARN_MSG_BUZ_STATUS, str,refMkid));
				continue;
			}
			json = new JSONObject();
			json.put("value", arr[0]);
			json.put("descp", arr[1]);
			result.add(json);
		}
		return result;
	}
	
	@RequestMapping(value="/options",method=RequestMethod.GET )
	@ResponseBody
	public Object getTaskOptions(HttpServletRequest req	){
		TaskOptVO optVO = new TaskOptVO();
		String refMkid = req.getParameter(PARM_REF_MKID);
		String instNum = req.getParameter("instNum");
		if(NumberUtils.isNumber(instNum)){
			optVO.setInstNum(NumberUtils.toInt(instNum));
		}
		else{
			System.err.println("getTaskOptions(): invalid instNum="+instNum);
			return new JSONArray();
		}
		optVO.setRefMkid(refMkid);
		return taskService.getTaskOptions(optVO, true);
	}
	
	@RequestMapping(value="/options/nogroup",method=RequestMethod.GET )
	@ResponseBody
	public Object getTaskOptionsNoGroup(HttpServletRequest req	){
		TaskOptVO optVO = new TaskOptVO();
		String refMkid = req.getParameter(PARM_REF_MKID);
		String instNum = req.getParameter("instNum");
		if(NumberUtils.isNumber(instNum)){
			optVO.setInstNum(NumberUtils.toInt(instNum));
		}
		else{
			System.err.println("getTaskOptions(): invalid instNum="+instNum);
			return new JSONArray();
		}
		optVO.setRefMkid(refMkid);
		return taskService.getTaskOptions(optVO, false);
	}
	
}
