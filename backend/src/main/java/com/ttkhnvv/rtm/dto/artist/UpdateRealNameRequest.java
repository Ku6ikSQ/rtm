package com.ttkhnvv.rtm.dto.artist;

import com.ttkhnvv.rtm.validation.artist.ValidRealName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRealNameRequest {
    @ValidRealName
    private String realName;
}