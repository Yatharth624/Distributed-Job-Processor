package com.jobprocessor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final Set<String> VALID_API_KEYS = Set.of("admin-key-123", "user-key-456", "test-key-789");

    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Bypass security checks for UI static resources, health, metrics, H2 console
        if (path.equals("/") || path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")
                || path.startsWith("/h2-console") || path.startsWith("/actuator") || path.startsWith("/api/v1/metrics")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate X-API-KEY
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null || !VALID_API_KEYS.contains(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Invalid or missing X-API-KEY header. Valid demo keys: admin-key-123, user-key-456\"}");
            return;
        }

        // Check Rate Limiter
        if (!rateLimiterService.tryAcquire(apiKey)) {
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded (Max 60 requests/min per API Key)\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
