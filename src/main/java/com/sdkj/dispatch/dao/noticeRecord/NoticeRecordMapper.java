package com.sdkj.dispatch.dao.noticeRecord;

import java.util.List;
import java.util.Map;

import com.sdkj.dispatch.domain.po.NoticeRecord;

public interface NoticeRecordMapper {
	int saveNoticeRecord(NoticeRecord target);
	
	public List<NoticeRecord> findNoticeRecord(Map<String,Object> param);
}
