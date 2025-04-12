package yoga1290.coresystem.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
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
    private AuthenticationManager authenticationManager;

    public JwtRequestFilter(
            JWTService jwtTokenUtil,
            AuthenticationManager authenticationManager)
            throws Exception {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        log.info(String.format("doFilterInternal | request: %s | response: %s", request, response));
        MDC.put(AOPLoggingConfig.URI, request.getRequestURI());
        MDC.put(AOPLoggingConfig.TRANSACTION_ID, request.getMethod());

//        /*
        // Get authorization header and validate
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean hasAuthHeader = !isEmpty(header) && header.startsWith("Bearer ");

        UserDetails userDetails;
        String token = null;
        if (hasAuthHeader) {
            token = header.split(" ")[1].trim();
        }
        userDetails = jwtTokenUtil.userDetailsByJWT(token);


//        PreAuthenticatedAuthenticationToken authentication =
//                        new PreAuthenticatedAuthenticationToken(userDetails.getUsername(),
//                                                        userDetails,
//                                                        userDetails.getAuthorities());

        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(userDetails.getUsername(),
                    userDetails,
                    userDetails.getAuthorities());

        log.info(String.format("Athenticating | user: %s | token: %s", userDetails, token));

        authentication.setDetails(userDetails);
        this.authenticationManager.authenticate(authentication);
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        this.securityContextHolderStrategy.setContext(context);


//        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(userDetails.getUsername(),
//                                                    userDetails, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //*/

 /*
        SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
        WebAuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();

        // Reverse Engeered from AnonymousAuthenticationFilter.doFilter but with custom AnonymousAuthenticationToken:
        // see https://github.com/spring-projects/spring-security/blob/15c2b156f19826bcebf4cc8af9e2511d84bb8673/web/src/main/java/org/springframework/security/web/authentication/AnonymousAuthenticationFilter.java#L95
        Supplier<SecurityContext> deferredContext = securityContextHolderStrategy.getDeferredContext();
        Supplier<SecurityContext> securityContext = this.defaultWithAnonymous(
                                                        (HttpServletRequest)request,
                                                        deferredContext,
                                                        securityContextHolderStrategy);
        securityContextHolderStrategy.setDeferredContext(securityContext);

        SecurityContextHolder.getContextHolderStrategy().setContext(securityContext.get());
        SecurityContextHolder.getContext().setAuthentication(
                                securityContext.get().getAuthentication());
//*/

//        authentication.setDetails(
//                new WebAuthenticationDetailsSource().buildDetails(request)
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info(String.format("doFilterInternal | authType: %s | Authentication: %s",
                            request.getAuthType(),
                            SecurityContextHolder.getContext().getAuthentication() ));
        chain.doFilter(request, response);
    }

    private Supplier<SecurityContext> defaultWithAnonymous(HttpServletRequest request,
                                                           Supplier<SecurityContext> currentDeferredContext,
                                                           SecurityContextHolderStrategy securityContextHolderStrategy) {
        return SingletonSupplier.of(() -> {
            SecurityContext currentContext = (SecurityContext)currentDeferredContext.get();
            return this.defaultWithAnonymous(request, currentContext, securityContextHolderStrategy);
        });
    }

    private SecurityContext defaultWithAnonymous(HttpServletRequest request,
                                                    SecurityContext currentContext,
                                                    SecurityContextHolderStrategy securityContextHolderStrategy) {
        Authentication currentAuthentication = currentContext.getAuthentication();
        if (currentAuthentication == null) {
            Authentication anonymous = this.createAuthentication(request);
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.of(() -> "Set SecurityContextHolder to " + anonymous));
            } else {
                this.logger.debug("Set SecurityContextHolder to anonymous SecurityContext");
            }

            SecurityContext anonymousContext = securityContextHolderStrategy.createEmptyContext();
            anonymousContext.setAuthentication(anonymous);
            return anonymousContext;
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.of(() -> "Did not set SecurityContextHolder since already authenticated " + currentAuthentication));
            }

            return currentContext;
        }
    }

    private Authentication createAuthentication(HttpServletRequest request) {
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

        return authentication;
    }

}
