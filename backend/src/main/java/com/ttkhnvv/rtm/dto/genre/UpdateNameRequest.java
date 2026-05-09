package com.ttkhnvv.rtm.dto.genre;

import com.ttkhnvv.rtm.validation.genre.ValidGenreName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNameRequest {
    @ValidGenreName
    private String name;
}
