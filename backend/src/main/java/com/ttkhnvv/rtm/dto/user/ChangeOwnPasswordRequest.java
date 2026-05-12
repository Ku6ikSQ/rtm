package com.ttkhnvv.rtm.dto.user;

import com.ttkhnvv.rtm.validation.common.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeOwnPasswordRequest {
    @NotBlank
    String currentPassword;

    @ValidPassword
    String newPassword;
}
