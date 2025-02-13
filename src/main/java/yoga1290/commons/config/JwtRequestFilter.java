package yoga1290.commons.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import yoga1290.commons.services.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JWTService jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    public JwtRequestFilter(JWTService jwtTokenUtil,
                            UserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        MDC.put(AOPLoggingConfig.URI, request.getRequestURI());
        MDC.put(AOPLoggingConfig.TRANSACTION_ID, request.getMethod());

        // Get authorization header and validate
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean hasAuthHeader = !isEmpty(header) && header.startsWith("Bearer ");

        UserDetails userDetails;
        if (hasAuthHeader) {
            final String token = header.split(" ")[1].trim();
            userDetails = jwtTokenUtil.userDetailsByJWT(token);
            //TODO: MDC.put(.., ..)
            response.setHeader(HttpHeaders.AUTHORIZATION, token);
        } else {
            userDetails = jwtTokenUtil.userDetailsByJWT(null);
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                                                    userDetails, null,
                                                                    userDetails.getAuthorities()
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

}
