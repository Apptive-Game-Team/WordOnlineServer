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

import javax.naming.AuthenticationException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static AtomicInteger atomicInteger = new AtomicInteger(123);

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    public AuthResponseDto registerAndLogin(UserRegisterRequestDto userRegisterRequestDto) throws AuthenticationException {

        User user = User.createWithPasswordPlain(
                userRegisterRequestDto.email(), userRegisterRequestDto.name(), userRegisterRequestDto.passwordPlain()
        );

        userService.registerUser(user);

        return login(new UserLoginRequestDto(userRegisterRequestDto.email(), userRegisterRequestDto.passwordPlain()));
    }

    public AuthResponseDto login(UserLoginRequestDto userLoginRequestDto) throws AuthenticationException {

        User user = userRepository.findUserByEmail(userLoginRequestDto.email())
                .orElseThrow(() -> new AuthenticationException("Not Found User"));

        if (!user.validatePassword(userLoginRequestDto.passwordPlain())) {
            throw new AuthenticationException("Not Found User");
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
