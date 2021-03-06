package com.xb.test.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.xb.MyWorkflowApp;

@RunWith(SpringJUnit4ClassRunner.class)   // 1
@SpringApplicationConfiguration(classes = MyWorkflowApp.class)   // 2
@WebIntegrationTest("server.port:0")   // 4: random port
public class RecallNormalNextCommittedTest extends TestBase4Normal{
	
	private static final String refMkid = "ju-recall-next-committed";
	
	@Override
	public String getRefMkid() {
		return refMkid;
	}
	
	@Test
	public void testNormalTxCommit_Commit_Recall(){
		startWf();
		commitTask(TEST_STAFF1, TEST_MANAGER1);
		commitTask(TEST_MANAGER1, "");
		recallFail(TEST_STAFF1);
	}
	
}
