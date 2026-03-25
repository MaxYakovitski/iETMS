package com.mayak.ietms.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Servlet filter that limits the number of login attempts per IP address.
 *
 * <p>Applies only to {@code POST /api/auth/login} requests. Each IP address
 * is allowed up to 5 attempts per minute. Exceeding this limit results in
 * a {@code 429 Too Many Requests} response.
 *
 * <p>Uses a Caffeine cache to store per-IP token buckets. Entries expire
 * after 5 minutes of inactivity, preventing unbounded memory growth.
 *
 * <p>The client IP is taken from {@code HttpServletRequest#getRemoteAddr()}
 * to prevent spoofing via the {@code X-Forwarded-For} header.
 */

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();

    /**
     * Creates a new token bucket allowing 5 requests per minute.
     *
     * @return a configured {@link Bucket} instance
     */
    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Intercepts login requests and enforces the rate limit.
     *
     * <p>Non-login requests pass through without any processing.
     * If the IP address has exceeded the allowed number of attempts,
     * the request is rejected with {@code 429 Too Many Requests}.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (!"/api/auth/login".equals(request.getRequestURI()) || !"POST".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        Bucket bucket = buckets.get(ip, k -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"code\":\"too_many_requests\",\"message\":\"Too many login attempts. Please try again later.\"}"
            );
        }
    }
}