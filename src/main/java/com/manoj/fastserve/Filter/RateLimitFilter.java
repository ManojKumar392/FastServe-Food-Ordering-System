package com.manoj.fastserve.Filter;

import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {

        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String path = request.getRequestURI();


        // only protect login
        if(path.equals("/users/login")) {

            String ip = request.getRemoteAddr();


            Bucket bucket =
                    buckets.computeIfAbsent(
                            ip,
                            key -> createBucket()
                    );


            if(!bucket.tryConsume(1)) {

                response.setStatus(429);
                response.getWriter()
                        .write("Too many login attempts. Try later.");

                return;
            }
        }


        filterChain.doFilter(request,response);
    }
}