package com.sdkj.dispatch.util;

public class Constant {
	
	public static final String MQ_TAG_DISPATCH_ORDER = "DISPATCH";
	public static final String MQ_TAG_TAKED_ORDER = "TAKED";
	public static final String MQ_TAG_STOWAGE_ORDER = "STOWAGE";
	public static final String MQ_TAG_SIGN_RECEIVE_ORDER = "RECEIVE";
	public static final String MQ_TAG_CANCLE_ORDER = "CANCLE";
	public static final String MQ_TAG_CANCLE_TAKED_ORDER = "CANCLE_TAKED";
	public static final String MQ_TAG_ARRIVE_ROUTE_POINT = "ARRIVE_POINT";
	public static final String MQ_TAG_LEAVE_ROUTE_POINT = "LEAVE_POINT";
	
	/**
	 * 订单状态0未接单；1已接单；2司机到达装货点，3装货完成；4运途中；5终点到达；6已签收
	 */
	public static final int ORDER_STATUS_WEIJIEDAN=0;
	public static final int ORDER_STATUS_JIEDAN=1;
	public static final int ORDER_STATUS_SIJIDAODAZHUANGHUO=2;
	public static final int ORDER_STATUS_ZHUANGHUO=3;
	public static final int ORDER_STATUS_YUNTUZHONG=4;
	public static final int ORDER_STATUS_SIJIDAODASHOUHUO=5;
	public static final int ORDER_STATUS_YIQIANSHOU=6;
	/**
	 * 用户类型1客户，2司机
	 */
	public static final int USER_TYPE_CUSTOMER=1;
	public static final int USER_TYPE_DRIVER=2;
}
