package com.sdkj.dispatch.domain.po;

public class NoticeRecord {
	private Integer id;
	private Integer orderId;
	private String messageType;
	private String noticeUserIds;
	private String noticeRegisterIds;
	private String content;
	private String extraMessage;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getOrderId() {
		return orderId;
	}
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getNoticeUserIds() {
		return noticeUserIds;
	}
	public void setNoticeUserIds(String noticeUserIds) {
		this.noticeUserIds = noticeUserIds;
	}
	public String getNoticeRegisterIds() {
		return noticeRegisterIds;
	}
	public void setNoticeRegisterIds(String noticeRegisterIds) {
		this.noticeRegisterIds = noticeRegisterIds;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getExtraMessage() {
		return extraMessage;
	}
	public void setExtraMessage(String extraMessage) {
		this.extraMessage = extraMessage;
	}
	
}
