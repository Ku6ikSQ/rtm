package com.ttkhnvv.rtm.mapper;

import com.ttkhnvv.rtm.dto.user.UserResponse;
import com.ttkhnvv.rtm.entity.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}