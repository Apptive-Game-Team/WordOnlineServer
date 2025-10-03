package com.wordonline.server.auth.dto;

import com.wordonline.server.matching.dto.AccountMemberResponseDto;

public record UserDetailResponseDto(long id, String name, String email) {

    public UserDetailResponseDto(long id, AccountMemberResponseDto accountMemberResponseDto) {
        this(id, accountMemberResponseDto.name(), accountMemberResponseDto.email());
    }
    
}
