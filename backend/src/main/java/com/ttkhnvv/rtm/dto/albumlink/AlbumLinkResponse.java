package com.ttkhnvv.rtm.dto.albumlink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlbumLinkResponse {
    private UUID albumId;
    private UUID platformId;
    private String url;
    private String platformName;
    private String platformLogoUrl;
}