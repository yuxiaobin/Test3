package com.xb.persistent.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.mapper.CommonMapper;
import com.xb.persistent.WfTask;

/**
 *
 * WfTask 表数据库控制层接口
 *
 */
public interface WfTaskMapper extends CommonMapper<WfTask> {
	
//	public List<TaskVO> getTasksInbox(@Param("userId") String userId, @Param("likeUserId") String likeUserId);

	public List<WfTask> getTaskListWithStatus(@Param("instId") String instId);
	
	public List<WfTask> getEndTask4Recall(Map<String, Object> parmMap);

}