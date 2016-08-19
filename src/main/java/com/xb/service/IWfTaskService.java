package com.xb.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.framework.service.ICommonService;
import com.xb.persistent.WfAwt;
import com.xb.persistent.WfTask;
import com.xb.vo.TaskOptVO;
import com.xb.vo.WFDetailVO;

/**
 *
 * WfTask 表数据服务层接口
 *
 */
public interface IWfTaskService extends ICommonService<WfTask> {
	
	public List<WfAwt> getTasksInbox(String userId);
	
	public void startWF4Module(String moduleId, String rsWfId, String userId);
	
	@Deprecated
	public void processTask(String instId, String userId, String opt);
	
	public void processTask(TaskOptVO optVO, String currUserId);
	/**
	 * 获取当前工作流状态
	 * @param histId
	 * @return
	 */
	public WFDetailVO getWFStatus(String instId);
	
	public WFDetailVO getWFStatus(String rsWfId, Integer instNum);
	
	public void batchCreateTasksWithAssigners(List<WfTask> taskList);
	
	public List<WfTask> selectTasksWithAssigners(String wfId);
	
	/**
	 * 根据操作的类型（提交/退回等等）获取下一步的任务节点
	 * @param optVO
	 * @return
	 */
	public List<WfTask> getNextTasksByOptCode(TaskOptVO optVO);
	
	/**
	 * 根据操作的类型（提交/退回等等）获取下一步的任务节点Assigners
	 * @param optVO:rsWfId,instNum
	 * @return
	 */
	public JSONObject getNextAssignersByOptCode(TaskOptVO optVO);
	
}