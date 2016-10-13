package com.xb.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.framework.service.impl.CommonServiceImpl;
import com.xb.common.BusinessException;
import com.xb.common.WFConstants;
import com.xb.persistent.WfAwt;
import com.xb.persistent.WfInstance;
import com.xb.persistent.WfTask;
import com.xb.persistent.mapper.WfAwtMapper;
import com.xb.service.IWfAwtService;
import com.xb.service.IWfInstHistService;
import com.xb.service.IWfInstanceService;
import com.xb.service.IWfTaskService;
import com.xb.vo.TaskOptVO;

/**
 *
 * WfAwt 表数据服务层接口实现类
 *
 */
@Service
public class WfAwtServiceImpl extends CommonServiceImpl<WfAwtMapper, WfAwt> implements IWfAwtService {
	
	private static Logger log = LogManager.getLogger(WfAwtServiceImpl.class);
	
	@Autowired
	IWfInstanceService instService;
	@Autowired
	IWfInstHistService histService;
	@Autowired
	IWfTaskService taskService;

	/**
	 * 根据登录用户，获取待办事宜。
	 */
	public List<WfAwt> getAwtByUserId(String userId){
		
		return baseMapper.getAwtByUserId(userId);
	}
	
	public WfAwt getAwtByParam(String refMkid, int instNum, String currUserId){
		Map<String,Object> parmMap = new HashMap<String,Object>();
		parmMap.put(WFConstants.ApiParams.PARM_REFMK_ID, refMkid);
		parmMap.put("instNum", instNum);
		parmMap.put("currUserId", currUserId);
		List<WfAwt> awtList = baseMapper.getAwtByParam(parmMap);
		if(awtList!=null && !awtList.isEmpty()){
			for(WfAwt awt:awtList){
				String completeFlag = awt.getCompleteFlag();
				if(completeFlag==null || completeFlag.equals("N")){
					return awt;
				}
			}
			return awtList.get(0);
		}
		return null;
	}
	
	private boolean renew4Commit(WfAwt prev, WfTask currtask, WfInstance wfInst, String currUserId){
		String instId = prev.getInstId();
		if(WFConstants.TxTypes.COUNTERSIGN.equals(currtask.getTxType())){
			prev.setCompleteFlag("Y");
			this.updateById(prev);
			WfAwt parm = new WfAwt();
			parm.setInstId(instId);
			parm.setCompleteFlag("N");
			int incompletedCount = this.selectCount(parm);
			if(incompletedCount==0){
				return true;//没有未完成的待办事宜
			}
			
			String csOptJson = currtask.getSignChoices();
			JSONObject csOpt = (JSONObject) JSONObject.parse(csOptJson);
			parm.setCompleteFlag("Y");
			int completedCount = this.selectCount(parm);
			if(Boolean.TRUE.toString().equals(csOpt.getString("AllHandledThenGo"))){
				parm.setCompleteFlag(null);
				int allCount = this.selectCount(parm);
				if(allCount!=completedCount){
					updateCurrAssigners4CS(wfInst, currUserId);//not finished, return
					return false;
				}
			}
			else{
				if(Boolean.TRUE.toString().equals(csOpt.getString("PartHandledThenGo"))){
					int AtLeastHandled = csOpt.getIntValue("AtLeastHandled");
					if(AtLeastHandled==0){
						AtLeastHandled = 1;
					}
					if(completedCount<AtLeastHandled){
						updateCurrAssigners4CS(wfInst, currUserId);
						return false;
					}
				}
			}
		}else{
			wfInst.setOptUsersPre(currUserId+",");
		}
		return true;
	}
	
	public boolean renewReject(){
		return true;
	}
	
	/**
	 * 撤回操作：优先取awt.optUserPrev=currUserId, 
	 * 		如果存在：																						//简单事物，上一步就是该用户做的操作
	 * 				判断awt.taskIdPrev是否为null：
	 * 						为null表示已经撤回过，无法再执行撤回操作<END>；
	 * 						不为null，判断awt.taskIdPrev是否与awt.currTaskId相同，
	 * 								同：表示要撤回的是forward操作
	 * 								不同：撤回操作将跨事物节点：新建awt(设置taskIdPrev=null),更新inst.optdUsersPrev:剔除currUserId(防止再转交后的再撤回)
	 * 		如果不存在：
	 * 				查看wf_inst.optdUsersPrev是否包含currUserId：
	 * 						如果没有：直接抛异常，无法撤回<END>;
	 * 						如果有：判断prevTask是否可撤回： 
	 * 								不可->抛异常<END>;
	 * 								可->撤回：新建awt(设置taskIdPrev=null), 更新inst.optdUsersPrev:剔除currUserId & 更新prevTaskId,删除currTaskId的awt<END>; 
	 * 	
	 * @param instId
	 * @param currUserId
	 * @return
	 * @throws Exception 
	 */
	private boolean renewRecall(WfInstance inst, WfTask nextTask, String currUserId) throws BusinessException{
		String instId = inst.getInstId();
		synchronized (instId+"_rc") {
			WfAwt awtParm = new WfAwt();
			awtParm.setInstId(instId);
			awtParm.setOptUsersPre(currUserId);
			WfAwt awt = this.selectOne(awtParm);
			if(awt!=null){
				String prevTaskId = awt.getTaskIdPre();
				if(prevTaskId==null){
					log.error("renewRecall(): prefTaskId is null for instId="+instId+", optUsersPre="+currUserId+", recall is not allowed");
					throw new BusinessException("RECALL-ERROR","Recall is not allowed");
				}
				checkTaskRecallOptions(prevTaskId);
				if(awt.getTaskIdCurr().equals(awt.getTaskIdPre())){
					awt.setAssignerId(currUserId);
					awt.setTaskIdPre(null);
					awt.setOptUsersPre(currUserId);
					this.updateById(awt);
				}else{
					awtParm.setOptUsersPre(null);
					this.deleteSelective(awtParm);
					
					awt.setWfAwtId(null);//新建一条awt
					awt.setTaskIdCurr(inst.getTaskIdPre());
					awt.setAssignerId(currUserId);
					awt.setOptUsersPre(currUserId);
					awt.setTaskIdPre(null);
					Date beginDate = new Date();
					awt.setAwtBegin(beginDate);
					awt.setAwtEnd(calculateDate(beginDate, nextTask.getTimeLimitTp(), nextTask.getTimeLimit()));
					awt.setAwtAlarm(nextTask.getAlarmTime()==null?null:calculateDate(beginDate, nextTask.getAlarmTimeTp(), nextTask.getAlarmTime()));
					this.insert(awt);
					removeUserFromOptUserPrev(inst, currUserId, inst.getTaskIdPre(), true);
				}
			}
			else{
				String optdUserPre = inst.getOptUsersPre();
				if(optdUserPre==null || !optdUserPre.contains(currUserId)){
					log.error("renewRecall(): optUsersPre="+optdUserPre+", not contains currUserId="+currUserId+", recall is not allowed");
					throw new BusinessException("RECALL-ERROR","Recall is not allowed");
				}
				String prevTaskId = inst.getTaskIdPre();
				String currTaskId = inst.getTaskIdCurr();
				if(StringUtils.isEmpty(prevTaskId)){
					log.error("renewRecall(): prevTaskId is empty, recall is not allowed");
					throw new BusinessException("RECALL-ERROR","Recall is not allowed");
				}
				checkTaskRecallOptions(prevTaskId);
				boolean refreshCurrAssigner = false;
				if(!prevTaskId.equals(currTaskId)){//相等的情况，只有会签撤回(且已经有人撤回了); 不相等，说明第一个撤回，删除当前任务相关的awt
					awtParm.setOptUsersPre(null);
					awtParm.setTaskIdCurr(currTaskId);
					this.deleteSelective(awtParm);
					refreshCurrAssigner = true;
				}
				
				awt = new WfAwt();//新建一条awt
				awt.setTaskIdCurr(prevTaskId);
				awt.setInstId(instId);
				awt.setAssignerId(currUserId);
				awt.setOptUsersPre(currUserId);
				awt.setTaskIdPre(null);
				Date beginDate = new Date();
				awt.setAwtBegin(beginDate);
				awt.setAwtEnd(calculateDate(beginDate, nextTask.getTimeLimitTp(), nextTask.getTimeLimit()));
				awt.setAwtAlarm(nextTask.getAlarmTime()==null?null:calculateDate(beginDate, nextTask.getAlarmTimeTp(), nextTask.getAlarmTime()));
				this.insert(awt);
				
				removeUserFromOptUserPrev(inst, currUserId, prevTaskId, refreshCurrAssigner);
			}
			return false;
		}
	}
	
	private void checkTaskRecallOptions(String recallTaskId) throws BusinessException{
		WfTask recallTask = taskService.selectById(recallTaskId);
		if(recallTask==null){
			log.error("renewRecall(): no wfTask record found for prevTaskId"+recallTaskId+", recall is not allowed");
			throw new BusinessException("RECALL-ERROR","Recall is not allowed");
		}
		JSONObject txChoices = recallTask.getTxChoicesJson();
		Boolean allowReCall = null;
		if(txChoices!=null){
			allowReCall = txChoices.getBoolean("AllowReCall");
		}
		if(allowReCall==null || !allowReCall){
			log.error("renewRecall(): preTask setting AllowReCall is null or false, recall is not allowed");
			throw new BusinessException("RECALL-ERROR","Recall is not allowed");
		}
	}
	
	public void renewAwt(WfAwt prev, WfTask currtask, WfTask nextTask,  TaskOptVO optVO, String currUserId) throws BusinessException{
		if(nextTask!=null && WFConstants.TaskTypes.E.getTypeCode().equals(nextTask.getTaskType())){
			optVO.setNextEndTaskFlag(true);
		}else{
			optVO.setNextEndTaskFlag(false);
		}
		WfInstance wfInst = instService.selectById(prev.getInstId());
		
		String optCode = optVO.getOptCode();
		boolean needNextStep = false;
		switch (optCode) {
		case WFConstants.OptTypes.COMMIT:
			needNextStep = renew4Commit(prev, currtask, wfInst, currUserId);
			break;
		case WFConstants.OptTypes.REJECT:
			needNextStep = renewReject();
			break;
		case WFConstants.OptTypes.FORWARD:
			needNextStep = renew4Forward(wfInst, optVO, currUserId);
			break;
		case WFConstants.OptTypes.LET_ME_DO:
			needNextStep = renew4LetMeDo(wfInst, currtask, currUserId);
			break;
		case WFConstants.OptTypes.RECALL:
			needNextStep = renewRecall(wfInst, nextTask, currUserId);
			break;
		default:
			break;
		}
		if(needNextStep){
			clearAwtUpdateInstGoNextStep(wfInst, optVO, nextTask, currUserId);
		}
	}
	
	private boolean renew4Forward(WfInstance wfInst, TaskOptVO optVO, String currUserId) throws BusinessException{
		WfAwt awtParm = new WfAwt();
		awtParm.setInstId(wfInst.getInstId());
		awtParm.setAssignerId(currUserId);
		WfAwt awt = this.selectOne(awtParm);
		if(awt==null){
			log.error("renew4Forward(): no awt found for instId="+wfInst.getInstId()+", currUserid="+currUserId);
			return false;
		}
		
		String nextAssigners = optVO.getNextAssigners();
		String currAssigners = wfInst.getCurrAssigners();
		wfInst.setCurrAssigners(currAssigners.replace(currUserId+",", nextAssigners+","));
		String optdUsers = wfInst.getOptUsersPre();
		if(optdUsers!=null && optdUsers.contains(currUserId)){
			wfInst.setOptUsersPre(optdUsers.replace(currUserId+",", ""));
		}
		instService.updateById(wfInst);
		
		String[] nextAssignerArray = nextAssigners.split(",");
		WfAwt awtNew = null;
		List<WfAwt> awtNewList = new ArrayList<WfAwt>(nextAssignerArray.length);
		try{
			for(String assinger: nextAssignerArray){
				if(!StringUtils.isEmpty(assinger)){
					awtParm.setAssignerId(assinger);
					awtNew = this.selectOne(awtParm);
					if(awtNew!=null){
						continue;
					}
					awtNew = awt.clone();
					awtNew.setAssignerId(assinger);
					awtNew.setOptUsersPre(currUserId);
					awtNew.setTaskIdPre(awt.getTaskIdCurr());
					awtNew.setWfAwtId(null);
					awtNewList.add(awtNew);
				}
			}
		}catch(CloneNotSupportedException e){
			log.error("renew4Forward(): CloneNotSupportedException ", e);
			throw new BusinessException("Error when create Awt for forward task");
		}
		this.deleteById(awt.getWfAwtId());
		if(!awtNewList.isEmpty()){
			for(WfAwt awtTmp:awtNewList){
				this.insert(awtTmp);
			}
//			this.insertBatch(awtNewList);
		}
		return false;
	}
	
	private void removeUserFromOptUserPrev(WfInstance wfInst, String currUserId, String currTaskId, boolean refreshCurrAssigners){
		String optdUsers = wfInst.getOptUsersPre();
		if(optdUsers!=null && optdUsers.contains(currUserId)){
			wfInst.setOptUsersPre(optdUsers.replace(currUserId+",", ""));
		}
		String currAssigners = wfInst.getCurrAssigners();
		if(currAssigners==null || refreshCurrAssigners){
			currAssigners = currUserId+",";
		}else{
			currAssigners+=currUserId;
		}
		wfInst.setCurrAssigners(currAssigners);
		wfInst.setTaskIdCurr(currTaskId);
		wfInst.setWfStatus(WFConstants.WFStatus.IN_PROCESS);
		instService.updateById(wfInst);
	}
	
	private boolean renew4LetMeDo(WfInstance wfInst,WfTask currtask, String currUserId){
		WfAwt awtParm = new WfAwt();
		awtParm.setInstId(wfInst.getInstId());
		awtParm.setTaskIdCurr(currtask.getTaskId());
		List<WfAwt> awtList = this.selectList(awtParm);
		WfAwt currUserAwt = null;
		if(awtList==null || awtList.isEmpty()){
			log.error("renew4LetMeDo(): no awtList find for wfInstId="+wfInst.getInstId()+", currTaskId="+currtask.getTaskId()+", let me do ignored!");
			return false;
		}
		List<String> deleteIdList = new ArrayList<String>(awtList.size());
		for(WfAwt awt: awtList){
			if(!awt.getAssignerId().equals(currUserId)){
				deleteIdList.add(awt.getWfAwtId());
			}else{
				currUserAwt = awt;
			}
		}
		if(currUserAwt==null){
			log.debug("renew4LetMeDo(): currUserAwt is null for currUserId="+currUserId+", instId="+wfInst.getInstId());
			currUserAwt = awtList.get(0);
			currUserAwt.setAssignerId(currUserId);
			currUserAwt.setWfAwtId(null);
			this.insert(currUserAwt);
		}
		if(!deleteIdList.isEmpty()){
			this.deleteBatchIds(deleteIdList);
		}
		return true;
	}
	
	/**
	 * Clear Current Awt,  update Instance. Go next step: insert new Awt if needed.
	 * @param wfInst
	 * @param optVO
	 * @param nextTask
	 */
	private void clearAwtUpdateInstGoNextStep(WfInstance wfInst, TaskOptVO optVO, WfTask nextTask, String currUserId){
		String optdUsers = wfInst.getOptUsersPre();
		if(StringUtils.isEmpty(optdUsers)){
			wfInst.setOptUsersPre(currUserId+",");
		}else{
			if(!optdUsers.contains(currUserId)){
				wfInst.setOptUsersPre(optdUsers+currUserId+",");
			}
		}
		String nextTaskId = nextTask.getTaskId();
		String instId = wfInst.getInstId();
		WfAwt parm = new WfAwt();
		parm.setInstId(instId);
		parm.setCompleteFlag(null);
		this.deleteSelective(parm);
		//create new awt(s) with next taskId
		String nextAssigners = optVO.getNextAssigners();
		if(nextAssigners!=null && !optVO.isNextEndTaskFlag()){
			String[] nextAssignersArr = nextAssigners.split(",");
			WfAwt awt = null;
			Date beginDate = new Date();
			Date limitDate = calculateDate(beginDate, nextTask.getTimeLimitTp(), nextTask.getTimeLimit());
			Date alarmDate = nextTask.getAlarmTime()==null?null:calculateDate(beginDate, nextTask.getAlarmTimeTp(), nextTask.getAlarmTime());
			String optUsersPre = wfInst.getOptUsersPre();
			if(optUsersPre!=null && optUsersPre.indexOf(",")==optUsersPre.length()-1){
				optUsersPre = optUsersPre.substring(0,optUsersPre.length()-1);
			}
			for(String assigner : nextAssignersArr){
				if(!StringUtils.isEmpty(assigner)){
					awt = new WfAwt();
					/**
					 * 当任务流转到下一个节点，下一个节点的awt数据：设置optUsersPre&taskIdPre
					 */
					awt.setOptUsersPre(optUsersPre);
					awt.setTaskIdPre(wfInst.getTaskIdCurr());
					awt.setAssignerId(assigner);
					awt.setAwtBegin(beginDate);
					awt.setAwtEnd(limitDate);
					awt.setAwtAlarm(alarmDate);
					awt.setInstId(instId);
					awt.setTaskIdCurr(nextTaskId);
					this.insert(awt);
				}
			}
		}
		/**
		 * 当事务流转到下一个节点时，重置currTaskid&optdUsers
		 * 为上一步操作人（可能是多个）和上一个任务节点
		 */
		
		wfInst.setCurrAssigners(nextAssigners);
		wfInst.setTaskIdPre(wfInst.getTaskIdCurr());
		wfInst.setTaskIdCurr(nextTaskId);
		if(optVO.isNextEndTaskFlag()){
			wfInst.setWfStatus(WFConstants.WFStatus.DONE);
		}
		instService.updateById(wfInst);
	}
	
	private Date calculateDate(Date beginDate, String timeType, Integer amount){
		Date limitDate = null;
		timeType = timeType==null?"":timeType;
		amount = amount==null?0:amount;
		switch (timeType) {
		case "M":
			limitDate = DateUtils.addMinutes(beginDate, amount);
			break;
		case "H":
			limitDate = DateUtils.addHours(beginDate, amount);
			break;
		case "D":
			limitDate = DateUtils.addDays(beginDate, amount);
			break;
		default:
//			limitDate = beginDate;
			break;
		}
		return limitDate;
	}
	
	private void updateCurrAssigners4CS(WfInstance wfInst, String currUserId){
		String optdUsers = wfInst.getOptUsersPre();
		if(optdUsers==null){
			optdUsers = currUserId+",";
		}else{
			if(!optdUsers.contains(currUserId)){
				optdUsers += currUserId+",";
			}
		}
		/**
		 * 事务未流转，更新该task的处理人
		 */
		wfInst.setOptUsersPre(optdUsers);
		String currAssigners4Inst = wfInst.getCurrAssigners();
		if(currAssigners4Inst!=null){
			if(currAssigners4Inst.contains(currUserId+",")){
				wfInst.setCurrAssigners(currAssigners4Inst.replace(currUserId+",", ""));
				instService.updateById(wfInst );//更新当前处理人
				wfInst.setCurrAssigners(currAssigners4Inst);
			}
		}
		
	}

	@Override
	public List<WfAwt> getAwtListByInstId(String instId) {
		Map<String,Object> parmMap = new HashMap<String,Object>();
		parmMap.put("instId", instId);
		return baseMapper.getAwtByParam(parmMap);
	}

	@Override
	public List<WfAwt> getAwt4Recall(String refMkid, int instNum, String currRecallUser) {
		Map<String,Object> parmMap = new HashMap<String,Object>();
		parmMap.put(WFConstants.ApiParams.PARM_REFMK_ID, refMkid);
		parmMap.put("instNum", instNum);
		parmMap.put("currRecallUser", currRecallUser);
		return baseMapper.getAwt4Recall(parmMap);
	}
}