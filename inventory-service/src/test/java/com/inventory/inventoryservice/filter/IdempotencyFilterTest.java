package com.inventory.inventoryservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdempotencyFilterTest {

    private IdempotencyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new IdempotencyFilter();
    }

    @Test
    void testGetRequestBypassesFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/inventory");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        filter.doFilterInternal(request, response, (req, res) -> {
            ((MockHttpServletResponse) res).setStatus(200);
        });
        
        assertEquals(200, response.getStatus());
    }

    @Test
    void testPostRequestWithoutHeaderBypassesFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/inventory");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        filter.doFilterInternal(request, response, (req, res) -> {
            ((MockHttpServletResponse) res).setStatus(201);
        });
        
        assertEquals(201, response.getStatus());
    }

    @Test
    void testPostRequestWithHeaderIsCached() throws ServletException, IOException {
        String idempotencyKey = "test-key-123";
        
        // First request
        MockHttpServletRequest request1 = new MockHttpServletRequest("POST", "/api/v1/inventory");
        request1.addHeader("Idempotency-Key", idempotencyKey);
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        
        filter.doFilterInternal(request1, response1, (req, res) -> {
            ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) res;
            wrapper.setStatus(201);
            wrapper.setContentType("application/json");
            wrapper.getWriter().write("{\"status\":\"created\"}");
        });
        
        assertEquals(201, response1.getStatus());
        assertEquals("{\"status\":\"created\"}", response1.getContentAsString());

        // Second request with same key
        MockHttpServletRequest request2 = new MockHttpServletRequest("POST", "/api/v1/inventory");
        request2.addHeader("Idempotency-Key", idempotencyKey);
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        
        filter.doFilterInternal(request2, response2, (req, res) -> {
            // This should NOT be executed because it's cached!
            ((MockHttpServletResponse) res).setStatus(500);
        });
        
        // Should return cached 201 response instead of 500
        assertEquals(201, response2.getStatus());
        assertEquals("{\"status\":\"created\"}", response2.getContentAsString());
    }
}
