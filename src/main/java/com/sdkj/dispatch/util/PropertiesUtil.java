package com.sdkj.dispatch.util;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtil {
	private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
	private static Properties fileUploadPro =new Properties();
	static{
		try{
			 InputStream in = PropertiesUtil.class.getResourceAsStream("/application-dispatch.properties");//  
			 fileUploadPro.load(in);  
	         in.close();
		}catch(Exception e){
			logger.error("adfds", e);
		}

	}
	public static String getValue(String key){
		return fileUploadPro.getProperty(key);
	}
}
