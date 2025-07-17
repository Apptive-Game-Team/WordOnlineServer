package com.wordonline.server.websocket;

import com.wordonline.server.auth.config.JwtProvider;
import com.wordonline.server.auth.domain.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

@Slf4j
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith(JwtProvider.JWT_PREFIX)) {
                throw new IllegalArgumentException("Invalid Token");
            }
            token = token.substring(JwtProvider.JWT_PREFIX.length());
            if (jwtProvider.validateToken(token)) {
                PrincipalDetails principal = (PrincipalDetails) jwtProvider.getAuthentication(token).getPrincipal();
                accessor.setUser(principal);
            } else {
                throw new IllegalArgumentException("Invalid Token");
            }
        }

        return message;
    }
}
