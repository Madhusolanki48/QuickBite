package com.quickbite.orderservice.security;

import com.quickbite.orderservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/h2-console/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                String email = jwtService.extractUsername(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = new User(email, "", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,
                            null, user.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    if (jwtService.isTokenValid(token, user)) {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
