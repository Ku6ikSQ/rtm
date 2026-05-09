package com.ttkhnvv.rtm.dto.albumartist;

import com.ttkhnvv.rtm.entity.albumartist.ArtistRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoleRequest {
    private ArtistRole role;
}
