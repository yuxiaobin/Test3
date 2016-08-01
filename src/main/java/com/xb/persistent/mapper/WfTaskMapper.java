package com.xb.persistent.mapper;

import com.xb.persistent.WfTask;
import com.xb.vo.TaskVO;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.mapper.AutoMapper;

/**
 *
 * WfTask 表数据库控制层接口
 *
 */
public interface WfTaskMapper extends AutoMapper<WfTask> {
	
	public List<TaskVO> getTasksInbox(@Param("userId") String userId);


}