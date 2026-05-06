package com.ttkhnvv.rtm.dto.auth;

import com.ttkhnvv.rtm.validation.ValidToken;
import jakarta.validation.Constraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRequest {
    @ValidToken
    private String refreshToken;
}
