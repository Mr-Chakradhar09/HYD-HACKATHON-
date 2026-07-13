package com.inventory.inventory.config;

import com.inventory.inventory.entity.IdempotentApiRequest;
import com.inventory.inventory.repository.IdempotentApiRequestRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final IdempotentApiRequestRepository repository;

    public IdempotencyFilter(IdempotentApiRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);

        if (idempotencyKey == null || idempotencyKey.isBlank() || request.getMethod().equalsIgnoreCase("GET")) {
            // Not an idempotent request or is a GET, just pass through
            filterChain.doFilter(request, response);
            return;
        }

        // Check if we already processed this request
        Optional<IdempotentApiRequest> existingRequest = repository.findById(idempotencyKey);
        if (existingRequest.isPresent()) {
            log.info("Returning cached response for Idempotency-Key: {}", idempotencyKey);
            IdempotentApiRequest cached = existingRequest.get();
            response.setStatus(cached.getResponseStatus());
            response.setContentType("application/json");
            response.getWriter().write(cached.getResponseBody());
            return;
        }

        // Wrap the response so we can read its content after the filter chain completes
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Continue the filter chain
        filterChain.doFilter(request, responseWrapper);

        // After the controller finishes, capture the response and save it
        int status = responseWrapper.getStatus();
        String responseBody = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());

        // Save to DB
        try {
            repository.save(new IdempotentApiRequest(idempotencyKey, status, responseBody));
            log.info("Saved new Idempotency-Key: {}", idempotencyKey);
        } catch (Exception e) {
            log.error("Failed to save idempotency key: {}", e.getMessage());
        }

        // Actually copy the body to the real response
        responseWrapper.copyBodyToResponse();
    }
}
