package com.tenco.bank.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tenco.bank.handler.exception.DataDeliveryException;

@ControllerAdvice // Ioc 대상 (싱글톤 패턴)
public class GlobalControllerAdvice {
	
	@ExceptionHandler (Exception.class)
	public void exception(Exception e) {
		System.out.println("==================");
		System.out.println(e.getClass().getName());
		System.out.println(e.getMessage());
		System.out.println("==================");
	}
	
	
	@ResponseBody
	@ExceptionHandler(DataDeliveryException.class)
	public String dataDataDeliveryException(DataDeliveryException e) {
		StringBuffer sb = new StringBuffer();
		sb.append(" <script>");
		sb.append(" alert('"+ e.getMessage() +"');");
		sb.append(" window.history.back();");
		sb.append(" </script>");
		return sb.toString();
	}
	
}
