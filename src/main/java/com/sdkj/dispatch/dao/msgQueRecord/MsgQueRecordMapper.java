package com.sdkj.dispatch.dao.msgQueRecord;

import java.util.Map;

import com.sdkj.dispatch.domain.po.MsgQueRecord;

public interface MsgQueRecordMapper {

    int insert(MsgQueRecord record);

    MsgQueRecord findMsgQueRecord(Map<String,Object> param);
    
    int updateById(MsgQueRecord record);
    
}