package com.example.campsjobs.Configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Service
public class JwtWebFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String username = null;
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
            username = jwtUtil.extractUsername(token);
            Integer userId = jwtUtil.extractUserId(token);  // Extract userId
            log.info("Extracted token for username: {}, User ID: {}", username,userId);
        } else {
            log.warn("Authorization header is missing or malformed");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails)) {
                    log.info("Valid token, setting authentication for: {}", username);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // 🔍 Log authenticated user and roles
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    log.info("🔹 Authenticated User: {}", authentication.getName());
                    log.info("🔹 User Roles: {}", authentication.getAuthorities());

                } else {
                    log.warn("Invalid token for username: {}", username);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token.");
                    return;
                }
            } catch (Exception e) {
                log.error("Error processing authentication for user: {}", username, e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
