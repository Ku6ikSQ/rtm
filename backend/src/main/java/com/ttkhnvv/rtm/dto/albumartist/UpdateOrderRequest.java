package com.ttkhnvv.rtm.dto.albumartist;

import com.ttkhnvv.rtm.validation.albumartist.ValidOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequest {
    @ValidOrder
    private Integer order;
}
