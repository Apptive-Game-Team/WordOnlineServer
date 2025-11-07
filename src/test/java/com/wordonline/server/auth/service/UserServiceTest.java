package com.wordonline.server.auth.service;

import com.wordonline.server.auth.domain.User;
import com.wordonline.server.auth.domain.UserStatus;
import com.wordonline.server.auth.dto.UserDetailResponseDto;
import com.wordonline.server.auth.dto.UserResponseDto;
import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.matching.client.AccountClient;
import com.wordonline.server.matching.dto.AccountMemberResponseDto;
import com.wordonline.server.service.LocalizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeckService deckService;

    @Mock
    private AccountClient accountClient;

    @Mock
    private LocalizationService localizationService;

    @Test
    @DisplayName("사용자 조회 테스트 - 사용자가 존재할 경우")
    void getUser_existingUser() {
        // given
        long userId = 1L;
        User user = new User(userId, "testuser", UserStatus.Online, 1L);
        given(userRepository.findUserById(userId)).willReturn(Optional.of(user));

        // when
        UserResponseDto userResponseDto = userService.getUser(userId);

        // then
        assertThat(userResponseDto.id()).isEqualTo(userId);
        assertThat(userResponseDto.selectedDeckId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자 조회 테스트 - 사용자가 존재하지 않을 경우")
    void getUser_newUser() {
        // given
        long userId = 1L;
        User user = new User(userId, "newuser", UserStatus.Online, 1L);
        given(userRepository.findUserById(userId))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(user));
        given(userRepository.saveUser(userId)).willReturn(userId);
        given(deckService.initializeCard(userId)).willReturn(1L);
        lenient().when(localizationService.getMessage(any(String.class))).thenReturn("Can't Register");
        lenient().when(localizationService.getMessage(any(String.class), any())).thenReturn("User not found");

        // when
        UserResponseDto userResponseDto = userService.getUser(userId);

        // then
        assertThat(userResponseDto.id()).isEqualTo(userId);
        assertThat(userResponseDto.selectedDeckId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자 상세 정보 조회 테스트")
    void getUserDetail() {
        // given
        long memberId = 1L;
        AccountMemberResponseDto accountDto = new AccountMemberResponseDto("test@example.com", "testuser");
        given(accountClient.getMember(memberId)).willReturn(accountDto);

        // when
        UserDetailResponseDto userDetail = userService.getUserDetail(memberId);

        // then
        assertThat(userDetail.id()).isEqualTo(memberId);
        assertThat(userDetail.name()).isEqualTo("testuser");
        assertThat(userDetail.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("사용자 삭제 테스트")
    void deleteUser() {
        // given
        long userId = 1L;
        given(userRepository.deleteById(userId)).willReturn(true);

        // when
        boolean result = userService.deleteUser(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자 상태 변경 테스트 - 매칭중")
    void markMatching() {
        // given
        long userId = 1L;
        User user = new User(userId, "testuser", UserStatus.Online, 1L);
        given(userRepository.findUserById(userId)).willReturn(Optional.of(user));

        // when
        userService.markMatching(userId);

        // then
        verify(userRepository).updateStatus(userId, UserStatus.OnMatching);
    }

    @Test
    @DisplayName("사용자 상태 변경 테스트 - 게임중")
    void markPlaying() {
        // given
        long userId = 1L;
        User user = new User(userId, "testuser", UserStatus.Online, 1L);
        given(userRepository.findUserById(userId)).willReturn(Optional.of(user));

        // when
        userService.markPlaying(userId);

        // then
        verify(userRepository).updateStatus(userId, UserStatus.OnPlaying);
    }

    @Test
    @DisplayName("사용자 상태 변경 테스트 - 온라인")
    void markOnline() {
        // given
        long userId = 1L;
        User user = new User(userId, "testuser", UserStatus.OnMatching, 1L);
        given(userRepository.findUserById(userId)).willReturn(Optional.of(user));

        // when
        userService.markOnline(userId);

        // then
        verify(userRepository).updateStatus(userId, UserStatus.Online);
    }

    @Test
    @DisplayName("사용자 상태 조회 테스트")
    void getStatus() {
        // given
        long userId = 1L;
        User user = new User(userId, "testuser", UserStatus.OnPlaying, 1L);
        given(userRepository.findUserById(userId)).willReturn(Optional.of(user));

        // when
        UserStatus status = userService.getStatus(userId);

        // then
        assertThat(status).isEqualTo(UserStatus.OnPlaying);
    }

    @Test
    @DisplayName("사용자 상태 조회 실패 테스트 - 사용자를 찾을 수 없음")
    void getStatus_userNotFound() {
        // given
        long userId = 1L;
        given(userRepository.findUserById(userId)).willReturn(Optional.empty());
        given(localizationService.getMessage(any(String.class), any())).willReturn("User not found: " + userId);

        // when & then
        assertThatThrownBy(() -> userService.getStatus(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: " + userId);
    }
}
