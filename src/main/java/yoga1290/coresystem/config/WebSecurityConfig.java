package yoga1290.coresystem.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import yoga1290.coresystem.exceptions.Unauthorized;

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
    private WebSecurityProperties webSecurityProperties;
    public WebSecurityConfig(JwtRequestFilter jwtRequestFilter,
                             WebSecurityProperties webSecurityProperties) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.webSecurityProperties = webSecurityProperties;
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
        http.addFilterBefore(
                jwtRequestFilter,
                AuthorizationFilter.class
        );

        return http.build();
    }

    class AuthorizationManagerRequestMatcherRegistry implements Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> {

        private final List<String> roles;
        public AuthorizationManagerRequestMatcherRegistry(List<String> roles) {
            this.roles = roles;
        }

        @Override
        public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorizationManagerRequestMatcherRegistry) {
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
                        }
                    } catch (Exception e) {
                        log.error("bad <role,uri> pair format in \""+ roleItemStr +"\"", e);
                    }
                }
                authorizationManagerRequestMatcherRegistry.requestMatchers("/public/**").permitAll();
            } catch(Exception e) {
                log.warn(e.getMessage());
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

                                    String logStr = String.format("request: %s", request.toString());
//                                            System.out.println(logStr);
                                    log.error(logStr);
                                    ex.printStackTrace(); //TODO
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
