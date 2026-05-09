package com.ttkhnvv.rtm.dto.genre;

import lombok.Data;

import java.util.UUID;

@Data
public class GenreFilter {
    private String name;
    private UUID parentId;
}
