package yoga1290.coresystem.config;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.function.SingletonSupplier;
import org.springframework.web.filter.OncePerRequestFilter;
import yoga1290.coresystem.services.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Supplier;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private JWTService jwtTokenUtil;

    public JwtRequestFilter(
            JWTService jwtTokenUtil) {
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


        PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(userDetails.getUsername(),
                    userDetails,
                    userDetails.getAuthorities());
        authentication.setAuthenticated(true);
        authentication.setDetails(userDetails);

        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        securityContextHolderStrategy.setContext(context);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
}
