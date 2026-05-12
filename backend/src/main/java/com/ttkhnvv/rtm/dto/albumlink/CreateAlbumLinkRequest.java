package com.ttkhnvv.rtm.dto.albumlink;

import com.ttkhnvv.rtm.validation.albumlink.ValidUrl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAlbumLinkRequest {
    @NotNull
    private UUID albumId;
    @NotNull
    private UUID platformId;
    @ValidUrl
    private String url;
}