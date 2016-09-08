package com.xb.test.controller;

import static com.jayway.restassured.RestAssured.given;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.matchers.GreaterThan;
import org.mockito.internal.matchers.NotNull;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jayway.restassured.matcher.ResponseAwareMatcher;
import com.jayway.restassured.response.Response;
import com.xb.MyWorkflowApp;

@RunWith(SpringJUnit4ClassRunner.class)   // 1
@SpringApplicationConfiguration(classes = MyWorkflowApp.class)   // 2
@WebIntegrationTest("server.port:0")   // 4: random port
public class RecallNormalNextCommittedTest extends TestBase{
	
	private static final String refMkid = "ju-recall-next-committed";
	
	@Override
	public String getRefMkid() {
		return refMkid;
	}
	
	@Test
	public void testStartWF(){
		JSONObject parm = new JSONObject();
		parm.put("userId", "staff1");
		parm.put("gnmkId", refMkid);
		given().contentType("application/json")
        .request().body(parm.toJSONString())
        .when().post("/wfapi/start")
        .then()
        .body("return_code", new ResponseAwareMatcher<Response>() {
			@Override
			public Matcher<?> matcher(Response response) throws Exception {
				return new Equals(0);
			}
		})
        .body("wf_inst_num", new ResponseAwareMatcher<Response>() {
			@Override
			public Matcher<?> matcher(Response response) throws Exception {
				JSONObject json = JSONObject.parseObject(response.getBody().prettyPrint());
				instNum = json.getInteger("wf_inst_num");
				currTaskId = json.getString("curr_task_id");
				System.out.println("testStartWF():\tinstNum is "+instNum);
				return new GreaterThan<Integer>(0);
			}
		})
        ;
		testGetNextTask4Commit();
		testCommit();
		testGetNextTask4Commit();
		testCommit2Tx2();
		testGetNextTask4Commit();
		testCommit2End();
		testRecall();
//		
	}
	
	public void testGetNextTask4Commit(){
		JSONObject parm = new JSONObject();
		parm.put("wfInstNum", instNum);
		parm.put("gnmkId", refMkid);
		parm.put("optCode", "C");
		given().contentType("application/json")
        .request().body(parm.toJSONString())
        .when().post("/wfapi/tasks")
        .then()
        .body("records", new ResponseAwareMatcher<Response>() {
			@Override
			public Matcher<?> matcher(Response response) throws Exception {
				JSONObject json = JSONObject.parseObject(response.getBody().prettyPrint());
				JSONArray records = json.getJSONArray("records");
				nextTaskId4Commit = records.getJSONObject(0).getString("taskId");
				return NotNull.NOT_NULL;
			}
		})
        ;
	}
	
	public void testRecall(){
		JSONObject parm = new JSONObject();
		parm.put("userId", "staff1");
		parm.put("gnmkId", refMkid);
		parm.put("wfInstNum", instNum);
		parm.put("optCode", "RC");
		parm.put("comments", "junitTest: staff1 recall");
		given().contentType("application/json")
        .request().body(parm.toJSONString())
        .when().post("/wfapi/operate")
        .then()
        .body("return_code", new Equals(3));
	}
	
	public void testCommit(){
		JSONObject parm = new JSONObject();
		parm.put("userId", "staff1");
		parm.put("gnmkId", refMkid);
		parm.put("comments", "junitTest: staff1 commit");
		parm.put("nextTaskId", nextTaskId4Commit);
		parm.put("nextUserIds", "staff1");
		parm.put("optCode", "C");
		parm.put("wfInstNum", instNum);
		parm.put("currTaskId", currTaskId);
		given().contentType("application/json")
        .request().body(parm.toJSONString())
        .when().post("/wfapi/operate")
        .then()
        .body("return_code", new ResponseAwareMatcher<Response>() {
			@Override
			public Matcher<?> matcher(Response response) throws Exception {
				currTaskId = nextTaskId4Commit;
				return new Equals(0);
			}
		})
        ;
	}
	
	public void testCommit2Tx2(){
		JSONObject parm = new JSONObject();
		parm.put("userId", "staff1");
		parm.put("gnmkId", refMkid);
		parm.put("comments", "junitTest: staff1 commit");
		parm.put("nextTaskId", nextTaskId4Commit);
		parm.put("nextUserIds", "manager1");
		parm.put("optCode", "C");
		parm.put("wfInstNum", instNum);
		parm.put("currTaskId", currTaskId);
		given().contentType("application/json")
        .request().body(parm.toJSONString())
        .when().post("/wfapi/operate")
        .then()
        .body("return_code", new ResponseAwareMatcher<Response>() {
			@Override
			public Matcher<?> matcher(Response response) throws Exception {
				currTaskId = nextTaskId4Commit;
				return new Equals(0);
			}
		}) ;
	}
	
	public void testCommit2End(){
		JSONObject parm = new JSONObject();
		parm.put("userId", "manager1");
		parm.put("gnmkId", refMkid);
		parm.put("comments", "junitTest: manager1 commit tx2 to end");
		parm.put("nextTaskId", nextTaskId4Commit);
		parm.put("nextUserIds", "");
		parm.put("optCode", "C");
		parm.put("wfInstNum", instNum);
		parm.put("currTaskId", currTaskId);
		given().contentType("application/json")
		.request().body(parm.toJSONString())
		.when().post("/wfapi/operate")
		.then()
		.body("return_code", new ResponseAwareMatcher<Response>() {
			@Override
			public Matcher<?> matcher(Response response) throws Exception {
//				currTaskId = nextTaskId4Commit;
				return new Equals(0);
			}
		}) ;
	}

	
}
