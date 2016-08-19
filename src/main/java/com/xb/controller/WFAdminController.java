package com.xb.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.xb.base.BaseController;
import com.xb.persistent.RsModule;
import com.xb.service.IRsModuleService;
import com.xb.service.IRsWorkflowService;
import com.xb.service.IWfDefService;
import com.xb.utils.WfDataUtil;
import com.xb.vo.ModuleVO;
import com.xb.vo.WFDetailVO;

@Controller
@RequestMapping("/wfadmin")
public class WFAdminController extends BaseController {
	
	@Autowired
	IRsModuleService moduleService;
	@Autowired
	IRsWorkflowService wfService;
	@Autowired
	IWfDefService wfDefService;
	
	@RequestMapping("/")
	public String hello(HttpSession session){
		return home(session);
	}
	@RequestMapping("")
	public String hello2(HttpSession session){
		return home(session);
	}
	
	@RequestMapping("/modules")
	public String home(HttpSession session){
		return "wf-modules";
	}
	
	@RequestMapping(value="/module",method=RequestMethod.POST )
	@ResponseBody
	public Object createModule(@RequestBody ModuleVO moduleVO){
		RsModule module = new RsModule();
		module.setNAME(moduleVO.getModuleName());
		module.setCreatedBy("system");
		module.setCreatedDt(new Date());
		moduleService.insert(module);
		moduleVO.setModuleId(String.valueOf(module.getModId()));
		return moduleVO;
	}
	
	@RequestMapping(value="/modules/list",method=RequestMethod.GET )
	@ResponseBody
	public Object getAllModules(){
		RsModule parm = new RsModule();
		JSONObject page = new JSONObject();
		List<RsModule> list = moduleService.selectList(parm);
		page.put("records", list);
		return page;
	}
	
	@RequestMapping("/define/{moduleId}")
	public String viewWf4Module(@PathVariable String moduleId, HttpSession session, HttpServletRequest req){
		if(StringUtils.isEmpty(moduleId)){
			System.out.println("viewWf4Module(): moduleId is empty");
			return "";
		}
		RsModule moduleParm = new RsModule();
		moduleParm.setModId(moduleId);
		RsModule module = moduleService.selectOne(moduleParm);
		session.setAttribute("moduleId", moduleId);
		req.setAttribute("moduleName", module.getNAME());
		return "wf-define";
	}
	
	@RequestMapping(value="/module/{moduleId}/wf/{wfId}",method=RequestMethod.GET )
	@ResponseBody
	public Object getWfDtl4Module(@PathVariable String moduleId, @PathVariable String wfId, HttpSession session){
		if(StringUtils.isEmpty(moduleId)){
			System.out.println("viewWf4Module(): moduleId is empty");
			return "";
		}
		if(wfId=="init"){
			wfId = null;
		}
		return WfDataUtil.generateWfJson(wfService.getWF4Module(moduleId,wfId));
	}
	
	@RequestMapping(value="/module/{moduleId}/wf",method=RequestMethod.POST )
	@ResponseBody
	public Object createWF(@PathVariable String moduleId, @RequestBody JSONObject wfData){
		System.out.println(wfData);
		ModuleVO module = new ModuleVO();
		module.setModuleId(moduleId);
		module.setModuleName("moduleName:TODO");//TODO:
		WFDetailVO wfDetail = new WFDetailVO();
		wfDetail.setWfData(wfData);
		wfService.createWF4Module(module, wfDetail);
		return wfData;
	}
	

	@RequestMapping(value="/task", method=RequestMethod.GET )
	public Object viewTaskDtlPage2(HttpServletRequest req){
		String taskStr = req.getParameter("taskData");
		req.setAttribute("taskData", taskStr);
		return "taskProperties";
	}
}
