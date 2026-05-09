package com.ttkhnvv.rtm.dto.auth;

import com.ttkhnvv.rtm.validation.common.ValidEmail;
import com.ttkhnvv.rtm.validation.common.ValidPassword;
import com.ttkhnvv.rtm.validation.common.ValidUsername;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @ValidUsername
    private String username;
    @ValidEmail
    private String email;
    @ValidPassword
    private String password;
}


