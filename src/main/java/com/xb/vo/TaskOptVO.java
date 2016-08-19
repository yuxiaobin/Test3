package com.xb.vo;

public class TaskOptVO {

	private String rsWfId;
	private Integer instNum;
	
	private String comments;
	private String nextAssigners;
	private String nextTaskId;//当前选择的事务节点ID. Next Task ID
	private String optCode;//C:提交，V:否决，RJ:退回，RD:撤回
	
	private String refMkid;
	/**********以上为处理事务时必填字段*************/
	
//	private String instId;
	private String prevInstHistId;
	private String currUserId;
	private boolean nextEndTaskFlag;
	
	private String wfId;//userd when create hist
	private String moduleId;//For start workflow

	public String getWfId() {
		return wfId;
	}
	public void setWfId(String wfId) {
		this.wfId = wfId;
	}
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	public String getCurrUserId() {
		return currUserId;
	}
	public void setCurrUserId(String currUserId) {
		this.currUserId = currUserId;
	}
	public boolean isNextEndTaskFlag() {
		return nextEndTaskFlag;
	}
	public void setNextEndTaskFlag(boolean nextEndTaskFlag) {
		this.nextEndTaskFlag = nextEndTaskFlag;
	}
	public String getPrevInstHistId() {
		return prevInstHistId;
	}
	public void setPrevInstHistId(String prevInstHistId) {
		this.prevInstHistId = prevInstHistId;
	}
	
	public String getOptCode() {
		return optCode;
	}
	public void setOptCode(String optCode) {
		this.optCode = optCode;
	}
//	public String getInstId() {
//		return instId;
//	}
//	public void setInstId(String instId) {
//		this.instId = instId;
//	}
	public String getRsWfId() {
		return rsWfId;
	}
	public void setRsWfId(String rsWfId) {
		this.rsWfId = rsWfId;
	}
	public Integer getInstNum() {
		return instNum;
	}
	public void setInstNum(Integer instNum) {
		this.instNum = instNum;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getNextAssigners() {
		return nextAssigners;
	}
	public void setNextAssigners(String nextAssigners) {
		this.nextAssigners = nextAssigners;
	}
	public String getNextTaskId() {
		return nextTaskId;
	}
	public void setNextTaskId(String nextTaskId) {
		this.nextTaskId = nextTaskId;
	}
	
	public String getRefMkid() {
		return refMkid;
	}
	public void setRefMkid(String refMkid) {
		this.refMkid = refMkid;
	}
	@Override
	public String toString() {
		return "TaskOptVO [rsWfId=" + rsWfId + ", instNum=" + instNum + ", comments=" + comments + ", nextAssigners="
				+ nextAssigners + ", nextTaskId=" + nextTaskId + ", optCode=" + optCode + ", refMkid=" + refMkid
				+ ", prevInstHistId=" + prevInstHistId
				+ ", nextEndTaskFlag=" + nextEndTaskFlag + "]";
	}
	
}
