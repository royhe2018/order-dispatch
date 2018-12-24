package com.sdkj.dispatch.dao.user;

import java.util.List;
import java.util.Map;

import com.sdkj.dispatch.domain.po.User;

public interface UserMapper {
    int deleteById(Long id);

    int insert(User record);

    User findSingleUser(Map<String,Object> param);
    List<User> findUserList(Map<String,Object> param);
    int updateById(User record);
}