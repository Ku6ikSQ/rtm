package com.ttkhnvv.rtm.dto.user;


import com.ttkhnvv.rtm.validation.ValidEmail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEmailRequest {
    @ValidEmail
    String email;
}
