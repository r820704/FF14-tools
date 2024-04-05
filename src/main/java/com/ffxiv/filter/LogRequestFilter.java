package com.ffxiv.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


@Component
@Order(1)
public class LogRequestFilter extends OncePerRequestFilter {

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger log = LogManager.getLogger(LogRequestFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 包装原始的HttpServletRequest
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        this.doFilter(wrappedRequest, response, filterChain);

        String uri = request.getRequestURI();

        // 从包装过的请求中获取缓存的请求体内容
        byte[] contentAsByteArray = wrappedRequest.getContentAsByteArray();
        if (contentAsByteArray.length > 0) {
            // 将请求体内容转换为字符串
            String content = new String(contentAsByteArray, wrappedRequest.getCharacterEncoding());
            log.info("Request URI: " + uri);
            log.info("Request Content: " + content);
        }

    }

    private String toRequestContent(InputStream is) throws IOException {
        HashMap<String, Object> map = objectMapper.readValue(is, new TypeReference<HashMap<String, Object>>() {});
        return objectMapper.writeValueAsString(map);
    }
}
