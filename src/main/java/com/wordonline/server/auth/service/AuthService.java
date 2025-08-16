package com.wordonline.server.auth.service;

import com.wordonline.server.auth.config.JwtProvider;
import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.domain.User;
import com.wordonline.server.auth.dto.AuthResponseDto;
import com.wordonline.server.auth.dto.UserLoginRequestDto;
import com.wordonline.server.auth.dto.UserRegisterRequestDto;
import com.wordonline.server.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final static String LOGIN_FAIL_MESSAGE = "이메일 또는 비밀번호가 잘못됐습니다.";
    private final static String EMAIL_REDUNDANT = "사용 중인 Email입니다.";

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    public AuthResponseDto registerAndLogin(UserRegisterRequestDto userRegisterRequestDto) {

        if (userRepository.findUserByEmail(userRegisterRequestDto.email()).isPresent()) {
            throw new IllegalArgumentException(EMAIL_REDUNDANT);
        }

        User user = User.createWithPasswordPlain(
                userRegisterRequestDto.email(), userRegisterRequestDto.name(), userRegisterRequestDto.passwordPlain()
        );

        userService.registerUser(user);

        return login(new UserLoginRequestDto(userRegisterRequestDto.email(), userRegisterRequestDto.passwordPlain()));
    }

    public AuthResponseDto login(UserLoginRequestDto userLoginRequestDto) {

        User user = userRepository.findUserByEmail(userLoginRequestDto.email())
                .orElseThrow(() -> new IllegalArgumentException(LOGIN_FAIL_MESSAGE));

        if (!user.validatePassword(userLoginRequestDto.passwordPlain())) {
            throw new IllegalArgumentException(LOGIN_FAIL_MESSAGE);
        }

        PrincipalDetails principal = new PrincipalDetails(user.getId());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());

        String jwtToken = jwtProvider.createToken(authentication);

        return new AuthResponseDto(jwtToken);
    }

}
