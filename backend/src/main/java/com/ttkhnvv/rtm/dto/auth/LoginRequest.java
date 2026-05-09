package com.ttkhnvv.rtm.dto.auth;

import com.ttkhnvv.rtm.validation.common.ValidEmail;
import com.ttkhnvv.rtm.validation.common.ValidPassword;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @ValidEmail
    private String email;
    @ValidPassword
    private String password;
}
