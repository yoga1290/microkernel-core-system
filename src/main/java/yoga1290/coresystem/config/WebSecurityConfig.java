package yoga1290.coresystem.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import yoga1290.coresystem.exceptions.Unauthorized;
import yoga1290.coresystem.services.JWTService;

import java.util.Collection;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
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
//        log.info("INITIALIZING SECURITY");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

//        http = http.cors().and().csrf().disable();
        http = http.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
                httpSecurityCorsConfigurer.disable();
            }
        });

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
//        expressionInterceptUrlRegistry.anyRequest().authenticated();

//      Add JWT token filter
// see https://github.com/spring-projects/spring-security/blob/15c2b156f19826bcebf4cc8af9e2511d84bb8673/config/src/main/java/org/springframework/security/config/annotation/web/builders/FilterOrderRegistration.java#L85C7-L85C34
        AuthenticationManager authenticationManager = new AuthenticationManager() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                log.info("AuthenticationManager | authentication: ", authentication);
                return authentication;
            }
        };
        AuthenticationProvider authenticationProvider = new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                log.info("AuthenticationProvider | authentication: ", authentication);
//                SecurityContextHolder.getContext()
                return authentication;
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return true;
            }
        };

        http.authenticationManager(authenticationManager);
        http.authenticationProvider(authenticationProvider);
//        http.securityContext(httpSecuritySecurityContextConfigurer -> {
//            httpSecuritySecurityContextConfigurer.securityContextRepository()
//        });

//        /*
        http.userDetailsService(username -> {

            log.info(String.format("UserDetailsService | loadByUsername: %s", username));
            //TODO
            return new UserDetails() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return List.of(new GrantedAuthority() {
                        @Override
                        public String getAuthority() {
                            return "USER";
                        }
                    });
                }

                @Override
                public String getPassword() {
                    return "";
                }

                @Override
                public String getUsername() {
                    return "tempo";
                }
            };
        });
// */

//        http.authenticationManager(authentication -> authentication);
//        http.authenticationProvider(new AnonymousAuthenticationProvider())

        http.addFilterBefore(
                new JwtRequestFilter(jwtService, authenticationManager),

                // https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html#servlet-authentication-abstractprocessingfilter
//                AbstractAuthenticationProcessingFilter.class

//                SecurityContextHolderAwareRequestFilter.class
//                AnonymousAuthenticationFilter.class
//                SecurityContextHolderFilter.class
                BasicAuthenticationFilter.class
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
                            authorizationManagerRequestMatcherRegistry.requestMatchers(uri).hasRole(role);
                            authorizationManagerRequestMatcherRegistry.requestMatchers(uri).hasAuthority(role);
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
        http = http
                .sessionManagement(new Customizer<SessionManagementConfigurer<HttpSecurity>>() {
                    @Override
                    public void customize(SessionManagementConfigurer<HttpSecurity> httpSecuritySessionManagementConfigurer) {
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    }
                });
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and();
    }

    private void handleException(HttpSecurity http) throws Exception {
        // Set unauthorized requests exception handler
        http = http
                .exceptionHandling(new Customizer<ExceptionHandlingConfigurer<HttpSecurity>>() {

                    @Override
                    public void customize(ExceptionHandlingConfigurer<HttpSecurity> httpSecurityExceptionHandlingConfigurer) {
                        httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(
                                (request, response, ex) -> {

                                    String logStr = String.format("Principal: %s | auth type: %s | exception: %s",
                                                                    request.getUserPrincipal(),
                                                                    request.getAuthType(),
                                                                    ex.getMessage());
                                    log.error(logStr);


                                    ex.printStackTrace(); //TODO
//                                    response.setStatus(HttpStatus.FORBIDDEN.value());
                                    throw new Unauthorized(ex); //TODO
                                    //                            response.sendError(
                                    //                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    //                                    ex.getMessage()
                                    //                            );
                                }
                        );
                    }
                });
    }
}
