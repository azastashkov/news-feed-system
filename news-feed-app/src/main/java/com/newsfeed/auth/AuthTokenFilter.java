package com.newsfeed.auth;

import com.newsfeed.user.model.User;
import com.newsfeed.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Order(1)
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/v1/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authToken = request.getParameter("auth_token");
        if (authToken == null || authToken.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing auth_token");
            return;
        }

        Optional<User> user = userRepository.findByAuthToken(authToken);
        if (user.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid auth_token");
            return;
        }

        try {
            AuthContext.setCurrentUserId(user.get().getId());
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }
}
