package com.ttkhnvv.rtm.dto.review;

import com.ttkhnvv.rtm.validation.review.ValidScore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateScoreRequest {
    @ValidScore
    private Integer score;
}
