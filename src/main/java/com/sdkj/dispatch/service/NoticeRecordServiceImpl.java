package com.sdkj.dispatch.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sdkj.dispatch.dao.noticeRecord.NoticeRecordMapper;
import com.sdkj.dispatch.domain.po.NoticeRecord;

@Service
@Transactional
public class NoticeRecordServiceImpl {
	Logger logger = LoggerFactory.getLogger(NoticeRecordServiceImpl.class);
	@Autowired
	private NoticeRecordMapper noticeRecordMapper;
	
	public void saveNoticeRecord(NoticeRecord target){
		try{
			noticeRecordMapper.saveNoticeRecord(target);
		}catch(Exception e){
			logger.error("保存发送通知记录异常", e);
		}
	}
	
	public List<NoticeRecord> findNoticeRecord(Map<String,Object> param){
		return noticeRecordMapper.findNoticeRecord(param);
	}
}
