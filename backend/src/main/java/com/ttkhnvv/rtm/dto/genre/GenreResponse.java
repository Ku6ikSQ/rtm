package com.ttkhnvv.rtm.dto.genre;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenreResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private UUID parentId;
    private long albumCount;
}
