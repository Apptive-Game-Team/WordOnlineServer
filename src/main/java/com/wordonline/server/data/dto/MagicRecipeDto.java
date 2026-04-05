package com.wordonline.server.data.dto;

import java.util.List;

public record MagicRecipeDto(
        long id,
        String name,
        List<String> cards
) {}
