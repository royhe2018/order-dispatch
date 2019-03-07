package com.sdkj.dispatch.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aliyun.openservices.ons.api.Message;
import com.sdkj.dispatch.dao.msgQueRecord.MsgQueRecordMapper;
import com.sdkj.dispatch.domain.po.MsgQueRecord;
import com.sdkj.dispatch.util.DateUtilLH;

@Service
@Transactional
public class MsgQueRecordServiceImpl {
	Logger logger = LoggerFactory.getLogger(MsgQueRecordServiceImpl.class);
	
	@Autowired
	private MsgQueRecordMapper msgQueRecordMapper;
	
	public void updateMsgQueRecord(Message message) {
		try {
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("messageId",message.getMsgID());
			MsgQueRecord msgQueRecord =null;
			for(int i=0;i<3;i++){
				msgQueRecord =msgQueRecordMapper.findMsgQueRecord(param);
				if(msgQueRecord==null){
					logger.info(message.getMsgID()+" is null");
					Thread.sleep(500);
				}
			}
			if(msgQueRecord!=null) {
				msgQueRecord.setConsumeTime(DateUtilLH.getCurrentTime());
				Date startTime = DateUtilLH.convertStr2Date(msgQueRecord.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
				Date consumeTime = DateUtilLH.convertStr2Date(msgQueRecord.getConsumeTime(), "yyyy-MM-dd HH:mm:ss");
				msgQueRecord.setUseTime((int)(consumeTime.getTime()-startTime.getTime()));
				msgQueRecordMapper.updateById(msgQueRecord);
			}else {
				logger.info(message.getMsgID()+" not record");
			}
		}catch(Exception e) {
			logger.error("更新消息队列消费时间异常", e);
		}
	}
}
