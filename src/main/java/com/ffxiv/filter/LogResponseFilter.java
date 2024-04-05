package com.ffxiv.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Integer.MAX_VALUE)
public class LogResponseFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(LogRequestFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        doFilter(request, wrapper, filterChain);
        try {

            String uri = request.getRequestURI();
            int status = wrapper.getStatus();

            log.info("Request uri:"+uri+",Http status:"+status);
            log.info("Response content:"+new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8));
        } catch (Throwable e) {
            log.error("failed to log: " + e.getMessage());
        } finally {
            wrapper.copyBodyToResponse();
        }
    }
    
}
