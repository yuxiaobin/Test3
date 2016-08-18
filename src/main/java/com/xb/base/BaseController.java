package com.xb.base;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


public class BaseController {
	
	protected static final String SESSION_USERINFO = "USERINFO";

	
	@SuppressWarnings("unchecked")
	public Map<String,Object> getUserInfo(HttpSession session){
		return (Map<String, Object>) session.getAttribute(SESSION_USERINFO);
	}
	/*
	 * @ExceptionHandler({MyException.class})
	 * 
	 * @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	 * 
	 * @ResponseBody public ErrorResponse myError(MyException e) { ErrorJSON
	 * error = new ErrorJSON(e.getMessage()); return error; ErrorResponse error
	 * = new ErrorResponse(); error.setMessage(e.getErrorMsg());
	 * error.setExceptionTrace(ExceptionUtils.stackTrace(e)); return error; }
	 */

	/**
	 * 只有这段代码起作用！！！
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorResponseJson error(Throwable e) {
		ErrorResponseJson error = new ErrorResponseJson();
		System.out.println("===================exception handler =====================");
		e.printStackTrace();
		error.setMessage("System is unavailable. ");
		error.setErrorCode("500");

		return error;
	}
	/*
		
		
		*/

}
