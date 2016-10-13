package com.xb.service;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.framework.service.ICommonService;
import com.xb.common.BusinessException;
import com.xb.persistent.WfAwt;
import com.xb.persistent.WfCustVars;
import com.xb.persistent.WfInstance;
import com.xb.persistent.WfTask;
import com.xb.vo.TaskOptVO;
import com.xb.vo.TaskVO;
import com.xb.vo.WFDetailVO;

/**
 *
 * WfTask 表数据服务层接口
 *
 */
public interface IWfTaskService extends ICommonService<WfTask> {
	
	public List<WfAwt> getTasksInbox(String userId);
	
	public JSONObject startWF4Module(String refMkid, String userId);
	
	/**
	 * API : 根据refMkid启动工作流
	 * @param refMkid
	 * @param userId
	 * @return JSONObject{ instNum:xxx, currTaskId:xxx }
	 */
	public JSONObject startWFByRefMkid(String refMkid, String userId);
	
	public JSONObject processTask(TaskOptVO optVO, String currUserId) throws BusinessException;
	/**
	 * 获取当前工作流状态
	 * @param histId
	 * @return
	 */
	public WFDetailVO getWFStatus(String instId);
	
	public WFDetailVO getWFStatus(String refMkid, Integer instNum);
	
	public void batchCreateTasksWithAssigners(List<WfTask> taskList, List<WfCustVars> custVarList);
	
	public List<WfTask> selectTasksWithAssigners(String wfId);
	
	/**
	 * 根据操作的类型（提交/退回等等）获取下一步的任务节点
	 * @param optVO
	 * @return
	 */
	public List<TaskVO> getNextTasksByOptCode(TaskOptVO optVO);
	
	/**
	 * 根据操作的类型（提交/退回等等）获取下一步的任务节点Assigners
	 * @param optVO:refMkid,instNum
	 * @return {
	 * 		users:[ //事务定义对应用户
	 * 			{id:xxx,name:xxx,defSelMod:true/false, checkFlag:true/false},... 
	 * 		], 
	 * 		groups:[ //事务定义的对应用户组
	 * 			{id:xxx,name:xxx,defSelMod:true/false, checkFlag:true/false},... 
	 * 		],
	 * 		prevProcessers:[//退回时，上一步实际操作人
	 * 			{id:xxx,name:xxx, checkFlag:true/false},... 
	 * 		]
	 * 	},
	 */
	public JSONObject getNextAssignersByOptCode(TaskOptVO optVO);
	
	/**
	 * 根据refMkid,instNum等参数获取当前待办事宜对应的task
	 * @param optVO
	 * @return
	 */
	public WfTask getCurrentTaskByRefNum(TaskOptVO optVO);
	
	/**
	 * 根据refMkid&instNum 获取当前task的可操作菜单列表
	 * 
	 * @param optVO {refMkid, instNum}
	 * @param needGroup	是否需要分组
	 * @return
	 */
	public JSONArray getTaskOptions(TaskOptVO optVO, boolean needGroup);
	
	/**
	 * 撤回操作：优先取awt.optUserPrev=currUserId, 
	 * 		如果存在：																						//简单事物，上一步就是该用户做的操作
	 * 				判断awt.taskIdPrev是否为null：
	 * 						为null表示已经撤回过，无法再执行撤回操作<END>；
	 * 						不为null，判断awt.taskIdPrev->task.recallOption 是否允许撤回<END>
	 * 		如果不存在：
	 * 				查看wf_inst.optdUsersPrev是否包含currUserId：
	 * 						如果没有：无法撤回<END>;
	 * 						如果有：判断prevTask是否可撤回<END>; 
	 * @param instance
	 * @param optUserId
	 * @return
	 */
	public boolean checkAllowRecall(WfInstance instance, String optUserId);
}