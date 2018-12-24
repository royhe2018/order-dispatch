package com.sdkj.dispatch;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {
	private static ApplicationContext factory=null;
	public static void main(String[] args){
		factory=new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
	}
	
	public static Object getBean(String beanId){
		return factory.getBean(beanId);
	}
}
