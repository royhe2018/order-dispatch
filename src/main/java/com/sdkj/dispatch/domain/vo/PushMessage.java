package com.sdkj.dispatch.domain.vo;

import java.util.HashMap;
import java.util.Map;

import com.sdkj.dispatch.util.JsonUtil;

public class PushMessage {
	private String messageType;
	private Map<String,Object> message;
	public PushMessage() {
		message = new HashMap<String,Object>();
	}
	
	public String getMessageType() {
		return this.messageType;
	}
	
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
	public Map<String, Object> getMessage() {
		return message;
	}

	public void setMessage(Map<String, Object> message) {
		this.message = message;
	}

	public void addMessage(String name,String value) {
		message.put(name, value);
	}
	
	@Override
	public String toString() {
		return JsonUtil.convertObjectToJsonStr(this);
	}
}
