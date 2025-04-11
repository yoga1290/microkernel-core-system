package yoga1290.coresystem.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import yoga1290.coresystem.services.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private JWTService jwtTokenUtil;

    public JwtRequestFilter(JWTService jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        log.info(String.format("doFilterInternal | request: %s | response: %s", request, response));
        MDC.put(AOPLoggingConfig.URI, request.getRequestURI());
        MDC.put(AOPLoggingConfig.TRANSACTION_ID, request.getMethod());

        // Get authorization header and validate
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean hasAuthHeader = !isEmpty(header) && header.startsWith("Bearer ");

        UserDetails userDetails;
        String token = null;
        if (hasAuthHeader) {
            token = header.split(" ")[1].trim();
        }
        userDetails = jwtTokenUtil.userDetailsByJWT(token);
        log.info(String.format("Athenticating | user: %s | token: %s", userDetails, token));

        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(userDetails.getUsername(),
                                                    userDetails, userDetails.getAuthorities());


//        authentication.setDetails(
//                new WebAuthenticationDetailsSource().buildDetails(request)
//        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info(String.format("doFilterInternal | authType: %s | isAuthenticated: %s | principal: %s | authorities: %s",
                            request.getAuthType(),
                            SecurityContextHolder.getContext().getAuthentication().isAuthenticated(),
                            SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                            SecurityContextHolder.getContext().getAuthentication().getAuthorities() ));
        chain.doFilter(request, response);
    }

}
