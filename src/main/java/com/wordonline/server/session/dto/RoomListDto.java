package com.wordonline.server.session.dto;

import java.util.List;

public record RoomListDto(
        List<RoomInfoDto> rooms
) {

}
