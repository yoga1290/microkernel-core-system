package yoga1290.coresystem.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import yoga1290.coresystem.exceptions.Unauthorized;
import yoga1290.coresystem.services.JWTService;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private JwtRequestFilter jwtRequestFilter;
    private JWTService jwtService;
    private WebSecurityProperties webSecurityProperties;
    public WebSecurityConfig(
                             JWTService jwtService,
                             WebSecurityProperties webSecurityProperties) {
        this.webSecurityProperties = webSecurityProperties;
//        this.jwtRequestFilter = jwtRequestFilter;
        this.jwtService= jwtService;
        System.out.println("INITIALIZING SECURITY " + webSecurityProperties.getRoles().toString());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http = http.csrf(new Customizer<CsrfConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CsrfConfigurer<HttpSecurity> httpSecurityCsrfConfigurer) {
                httpSecurityCsrfConfigurer.disable();
            }
        });

        handleException(http);
        setStateless(http);

        // Our private endpoints
        try {
            List<String> roles = webSecurityProperties.getRoles();
            System.out.println("====== ROLES: " + roles.toString());
            http = http.authorizeHttpRequests(new AuthorizationManagerRequestMatcherRegistry(roles));
        } catch(Exception e) {
            log.warn(e.getMessage());
        }
//      Add JWT token filter
        http.addFilterBefore(
                new JwtRequestFilter(jwtService),
                UsernamePasswordAuthenticationFilter.class
        );
        return http.build();
    }

    class AuthorizationManagerRequestMatcherRegistry
            implements Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> {

        private final List<String> roles;
        public AuthorizationManagerRequestMatcherRegistry(List<String> roles) {
            this.roles = roles;
        }

        @Override
        public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
                                              authorizationManagerRequestMatcherRegistry) {
            try {

                for (String roleItemStr : roles) {
                    try {
                        String[] roleItem = roleItemStr.split(",");
                        String role = roleItem[0];
                        String uri = roleItem[1];

                        boolean isPublicRole = "PUBLIC".equals(role);
                        if (isPublicRole) {
                            authorizationManagerRequestMatcherRegistry.requestMatchers(uri).permitAll();
                        } else {
                            authorizationManagerRequestMatcherRegistry.requestMatchers(uri).access(
                                                                        new RequestAuthorizationManager(role));
                        }
                    } catch (Exception e) {
                        log.error("bad <role,uri> pair format in \""+ roleItemStr +"\"", e);
                    }
                }
                authorizationManagerRequestMatcherRegistry.requestMatchers("/public/**").permitAll();
            } catch(Exception e) {
                log.error(String.format("AuthorizationManagerRequestMatcherRegistry | exception: %e",
                                            e.getMessage()));
            }
        }
    }

    private void setStateless(HttpSecurity http) throws Exception {

        // Set session management to stateless
        http
            .sessionManagement(new Customizer<SessionManagementConfigurer<HttpSecurity>>() {
                @Override
                public void customize(SessionManagementConfigurer<HttpSecurity> httpSecuritySessionManagementConfigurer) {
                    httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                }
            });

        http.sessionManagement(new Customizer<SessionManagementConfigurer<HttpSecurity>>() {
            @Override
            public void customize(SessionManagementConfigurer<HttpSecurity> httpSecuritySessionManagementConfigurer) {
                // avoid any session fixation attacks
                httpSecuritySessionManagementConfigurer.sessionFixation().none();
            }
        });
    }

    class RequestAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

        private final String ROLE;
        public RequestAuthorizationManager(String roleFilter) {
            ROLE = roleFilter;
        }

        @Override
        public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {

            log.info("RequestAuthorizationManager | authentication: {}", authentication);
            log.info("RequestAuthorizationManager | authentication: {}", authentication);

            boolean hasAccess = false;
            for (GrantedAuthority grantedAuthority : authentication.get().getAuthorities()) {
                hasAccess = hasAccess || ROLE.equals(grantedAuthority.getAuthority());

                log.info("RequestAuthorizationManager | request: {} | GrantedAuthority: {} == {} | hasAccess: {}",
                                            object.getRequest(),
                                            grantedAuthority.getAuthority(),
                                            ROLE,
                                            hasAccess);
            }
            return new AuthorizationDecision(hasAccess);
        }

        @Override
        public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
            log.info("RequestAuthorizationManager | verify | authentication: {}", authentication);
        }
    }

    private void handleException(HttpSecurity http) throws Exception {
        // Set unauthorized requests exception handler
        http
            .exceptionHandling(new Customizer<ExceptionHandlingConfigurer<HttpSecurity>>() {

                @Override
                public void customize(ExceptionHandlingConfigurer<HttpSecurity> httpSecurityExceptionHandlingConfigurer) {
                    httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(
                            (request, response, ex) -> {

                                String logStr = String.format("Principal: %s | auth type: %s | session: %s | exception: %s",
                                                                request.getUserPrincipal(),
                                                                request.getAuthType(),
                                                                request.getSession(),
                                                                ex.getMessage());
                                log.error(logStr);
                                ex.printStackTrace(); //TODO
                                throw new Unauthorized(ex); //TODO
                            }
                    );
                }
            });
    }
}
