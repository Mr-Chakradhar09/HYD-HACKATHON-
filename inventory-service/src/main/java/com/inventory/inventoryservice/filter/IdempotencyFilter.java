package com.inventory.inventoryservice.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    // Cache to store Responses: Key is Idempotency-Key, Value is CachedResponse
    private final Cache<String, CachedResponse> responseCache = Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(10000)
            .build();

    // Cache to track in-progress requests
    private final Cache<String, Boolean> inProgressCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        if (HttpMethod.GET.matches(method) || HttpMethod.OPTIONS.matches(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if already cached
        CachedResponse cachedResponse = responseCache.getIfPresent(idempotencyKey);
        if (cachedResponse != null) {
            // Return cached response
            response.setStatus(cachedResponse.status);
            if (cachedResponse.contentType != null) {
                response.setContentType(cachedResponse.contentType);
            }
            if (cachedResponse.body != null && cachedResponse.body.length > 0) {
                response.getOutputStream().write(cachedResponse.body);
            }
            return;
        }

        // Check if currently in progress
        if (inProgressCache.getIfPresent(idempotencyKey) != null) {
            response.setStatus(409); // Conflict
            response.getWriter().write("Duplicate request in progress for idempotency key: " + idempotencyKey);
            return;
        }

        // Mark as in progress
        inProgressCache.put(idempotencyKey, true);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, responseWrapper);

            // If successful (2xx), cache the response
            int status = responseWrapper.getStatus();
            if (status >= 200 && status < 300) {
                CachedResponse cacheObj = new CachedResponse(
                        status,
                        responseWrapper.getContentType(),
                        responseWrapper.getContentAsByteArray()
                );
                responseCache.put(idempotencyKey, cacheObj);
            }
        } finally {
            inProgressCache.invalidate(idempotencyKey);
            responseWrapper.copyBodyToResponse();
        }
    }

    private static class CachedResponse {
        final int status;
        final String contentType;
        final byte[] body;

        CachedResponse(int status, String contentType, byte[] body) {
            this.status = status;
            this.contentType = contentType;
            this.body = body;
        }
    }
}
