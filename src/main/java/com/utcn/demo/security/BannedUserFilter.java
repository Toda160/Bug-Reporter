// src/main/java/com/utcn/demo/security/BannedUserFilter.java
package com.utcn.demo.security;

import com.utcn.demo.entity.User;
import com.utcn.demo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * After authentication, checks our User.isBanned flag and rejects banned users with 403.
 * Skips itself on /auth/** and /api/users/register so they can still see the ban message.
 */
public class BannedUserFilter extends OncePerRequestFilter {

    private final UserService userService;

    public BannedUserFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Don't block the login or registration endpoints
        return path.startsWith("/auth/") || path.equals("/api/users/register");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            User user = userService.getUserByUsername(username).orElse(null);

            if (user != null && user.getBanned()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Your account has been banned");
                return;
            }
        }

        // Not banned (or anonymous / not a UserDetails principal), carry on
        chain.doFilter(request, response);
    }
}
