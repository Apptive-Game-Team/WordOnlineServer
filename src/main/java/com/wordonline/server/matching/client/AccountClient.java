package com.wordonline.server.matching.client;

import com.wordonline.server.service.LocalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.wordonline.server.matching.dto.AccountMemberResponseDto;

@Service
public class AccountClient {

    private final RestClient restClient;
    private final LocalizationService localizationService;

    public AccountClient(RestClient.Builder builder, 
                        @Value("${team6515.server.account.url}") String accountServerUrl,
                        LocalizationService localizationService) {
        this.restClient = builder.baseUrl(accountServerUrl)
                .build();
        this.localizationService = localizationService;
    }

    public AccountMemberResponseDto getMember(long memberId) {
        ResponseEntity<AccountMemberResponseDto> entity = restClient.get().uri("/api/members/" + memberId)
                .retrieve()
                .toEntity(AccountMemberResponseDto.class);

        if (entity.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException(localizationService.getMessage("error.member.not.found"));
        }

        return entity.getBody();
    }
}
