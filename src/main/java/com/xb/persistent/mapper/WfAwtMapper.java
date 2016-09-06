package com.xb.persistent.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.mapper.CommonMapper;
import com.xb.persistent.WfAwt;

/**
 *
 * WfAwt 表数据库控制层接口
 *
 */
public interface WfAwtMapper extends CommonMapper<WfAwt> {

	public List<WfAwt> getAwtByUserId(@Param("userId") String userId);
	
	public List<WfAwt> getAwtByParam(Map<String, Object> parmMap);
	
	public List<WfAwt> getAwt4Recall(Map<String, Object> parmMap);
}