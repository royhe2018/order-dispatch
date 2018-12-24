package com.sdkj.dispatch.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonUtil {
	private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);
	private static ObjectMapper om = new ObjectMapper();
	
	public static String convertObjectToJsonStr(Object target) {
		String result = null;
		try {
			result = om.writeValueAsString(target);
		}catch(Exception e) {
			logger.error("json convert exception", e);
		}
		return result;
	}
	
	public static Object convertStrToMap(String src) {
		Map<String, Object> result =null;
		try {
			result = om.readValue(src, new TypeReference<Map<String,Object>>() {
	        });
		}catch(Exception e) {
			logger.error("str covert to map exception", e);
		}
		return result;
	}
	
	public static JsonNode convertStrToJson(String src) {
		try {
			return om.readTree(src);
		}catch(Exception e){
			logger.error("str convert to json exception", e);
			return null;
		}
	}
	
	public static ArrayNode convertStrToJsonArray(String src) {
		try {
			JsonNode node = om.readTree(src);
			if(node.isArray()) {
				return (ArrayNode)node;
			}
			return null;
		}catch(Exception e){
			logger.error("str convert to json exception", e);
			return null;
		}
	}
	public static void main(String[] args) {
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Map<String,Object> item = new HashMap<String,Object>();
		item.put("name", "roy");
		item.put("age", 12);
		list.add(item);
		
		Map<String,Object> item1 = new HashMap<String,Object>();
		item1.put("name", "roy1");
		item1.put("age", 121);
		list.add(item1);
		String jsonStr = convertObjectToJsonStr(list);
		System.out.println(jsonStr);
		ArrayNode node = convertStrToJsonArray(jsonStr);
		for(int i=0;i<node.size();i++) {
			JsonNode nodeItem = node.get(i);
			System.out.println(nodeItem.get("name"));
		}
	}
}
