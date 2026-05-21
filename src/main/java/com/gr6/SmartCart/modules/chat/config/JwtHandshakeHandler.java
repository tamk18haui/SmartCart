package com.gr6.SmartCart.modules.chat.config;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.security.JwtTokenProvider;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token = getTokenFromQuery(request.getURI());

        if (token == null || token.isBlank()) {
            return null;
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return null;
        }

        String email = jwtTokenProvider.getEmailFromJwt(token);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return null;
        }

        return new StompPrincipal(user.getUserId().toString());
    }

    private String getTokenFromQuery(URI uri) {
        String query = uri.getQuery();

        if (query == null || query.isBlank()) {
            return null;
        }

        String[] params = query.split("&");

        for (String param : params) {
            String[] pair = param.split("=", 2);

            if (pair.length == 2 && "token".equals(pair[0])) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }

        return null;
    }
}