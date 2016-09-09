package com.xb.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.framework.service.impl.CommonServiceImpl;
import com.xb.common.WFConstants;
import com.xb.persistent.WfAwt;
import com.xb.persistent.WfInstHist;
import com.xb.persistent.WfInstance;
import com.xb.persistent.mapper.WfInstHistMapper;
import com.xb.service.IWfAwtService;
import com.xb.service.IWfInstHistService;
import com.xb.service.IWfInstanceService;
import com.xb.vo.TaskOptVO;

/**
 *
 * WfInstHist 表数据服务层接口实现类
 *
 */
@Service
public class WfInstHistServiceImpl extends CommonServiceImpl<WfInstHistMapper, WfInstHist> implements IWfInstHistService {
	
	@Autowired
	IWfInstanceService instService;
	@Autowired
	IWfAwtService awtService;

	public List<WfInstHist> viewWfInstHistory(String instId){
		if(instId==null){
			System.out.println("viewWfInstHistory=====instId="+instId);
			return null;
		}
		WfInstHist histParm = new WfInstHist();
		histParm.setInstId(instId);
		List<WfInstHist>list= baseMapper.getInstHistByInstId(instId);
		
		WfInstance inst = instService.selectById(instId);
		String currentTaskOwner = inst.getCurrAssigners();
		
		List<WfAwt> awtList = awtService.getAwtListByInstId(instId);
		if(awtList!=null && !awtList.isEmpty()){
			WfAwt awt = awtList.get(0);
			String nextAssigners = "";
			WfInstHist awtHist = new WfInstHist();
			awtHist.setTaskDescpDisp(awt.getTaskDescpDisp());
			awtHist.setInstId(awt.getInstId());
			awtHist.setTaskBegin(awt.getAwtBegin());
			awtHist.setTaskEnd(awt.getAwtEnd());
			awtHist.setTaskOwner(currentTaskOwner==null?awt.getTaskOwner():currentTaskOwner);
			for(WfAwt awtTmp:awtList){
				nextAssigners += awtTmp.getAssignerId()+", ";
			}
			awtHist.setNextAssigner(nextAssigners);
			if(list == null){
				list = new ArrayList<WfInstHist>(1);
			}
			list.add(awtHist);
		}
		return list;
	}
	
	public List<WfInstHist> viewWfInstHistory(String rsWfId, Integer instNum){
		WfInstance instParm = new WfInstance();
		instParm.setRsWfId(rsWfId);
		instParm.setInstNum(instNum);
		WfInstance inst = instService.selectOne(instParm);
		if(inst!=null){
			return viewWfInstHistory(inst.getInstId());
		}
		return null;
	}
	
	public String createHistRecord(TaskOptVO optVO, WfAwt awt, String currUserId){
		String instId = awt.getInstId();
		synchronized (instId) {
			WfInstHist parm = new WfInstHist();
			parm.setInstId(instId);
			List<WfInstHist> histList = this.selectList(parm, "OPT_SEQ desc");
			int nextOptSeq = 1;
			WfInstHist prefHist = null;
			if(histList!=null && !histList.isEmpty()){
				prefHist = histList.get(0);
				nextOptSeq = prefHist.getOptSeq()+1;
			}
			
			WfInstHist hist = new WfInstHist();
			hist.setInstId(instId);
			hist.setNextAssigner(optVO.getNextAssigners());
			hist.setOptSeq(nextOptSeq);
			hist.setOptUser(currUserId);
			hist.setOptType(optVO.getOptCode());
			hist.setTaskId(awt.getTaskIdCurr());
			hist.setWfId(optVO.getWfId());
			if(awt.getTaskOwner()!=null && WFConstants.OptTypes.RECALL.equals(optVO.getOptCode())){
				hist.setTaskOwner(awt.getTaskOwner());
			}else{
				if(prefHist!=null){
					hist.setTaskOwner(prefHist.getNextAssigner());
				}else{
					hist.setTaskOwner(currUserId);
				}
			}
			hist.setTaskBegin(awt.getAwtBegin());
			hist.setTaskEnd(awt.getAwtEnd());
			hist.setTaskRend(new Date());
			hist.setOptComm(optVO.getComments());
			this.insert(hist);
			return hist.getHistId();
		}
	}

}