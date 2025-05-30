package tech.realworks.yusuf.zaikabox.filter;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.realworks.yusuf.zaikabox.service.userService.AppUserDetailsService;
import tech.realworks.yusuf.zaikabox.util.JwtUtil;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;

    private static final List<String> WHITELISTS_URLs = List.of(
            "/api/v1/users/register", 
            "/api/v1/users/login", 
            "/api/v1/users/is-authenticated",
            "/api/v1/users/logout",
            "/api/v1/foods/**", 
            "/contact-us",
            "/send-reset-otp", 
            "/reset-password", 
            "/logout", 
            "/error"
    );

    private boolean isPathWhitelisted(String servletPath) {
        if (WHITELISTS_URLs.contains(servletPath)) {
            return true;
        }

        for (String pattern : WHITELISTS_URLs) {
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                if (servletPath.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, java.io.IOException {
        String servletPath = request.getServletPath();

        if (WHITELISTS_URLs.contains(servletPath) || isPathWhitelisted(servletPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;
        String email;

        // Extracting the JWT from the request header
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }

        // Extracting the JWT from the cookies
        if (jwt == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // Validating the JWT and setting the authentication in the security context
        if(jwt != null){
            email = jwtUtil.extractEmail(jwt);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = appUserDetailsService.loadUserByUsername(email);
                if(jwtUtil.validateToken(jwt, userDetails))
                {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
