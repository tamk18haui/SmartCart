package com.gr6.SmartCart.modules.chat.config;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.security.JwtTokenProvider;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        if (accessor.getUser() != null) {
            return message;
        }

        String bearerToken = accessor.getFirstNativeHeader("Authorization");

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return message;
        }

        String token = bearerToken.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            return message;
        }

        String email = jwtTokenProvider.getEmailFromJwt(token);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return message;
        }

        Principal principal = new StompPrincipal(user.getUserId().toString());
        accessor.setUser(principal);

        return message;
    }
}