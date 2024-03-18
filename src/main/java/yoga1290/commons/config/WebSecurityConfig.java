package yoga1290.commons.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import yoga1290.commons.exceptions.Unauthorized;

import java.util.Arrays;
import java.util.Iterator;
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

        http = http.cors().and().csrf().disable();
        handleException(http);
        setStateless(http);

        // Set permissions on endpoints
//        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
//                expressionInterceptUrlRegistry =
//                http.authorizeRequests()
//                        // Our public endpoints
//                        .regexMatchers(HttpMethod.GET, "/api/public/.*").permitAll()
//                        .regexMatchers(HttpMethod.GET, "/api/public/test").permitAll()
//                        .regexMatchers(HttpMethod.GET, "/public/test").permitAll();
////                        .antMatchers(HttpMethod.GET, "/api/public/**").permitAll();
////                            .antMatchers(HttpMethod.POST, "/api/book/search").permitAll();

        // Our private endpoints
        try {
            List<String> roles = webSecurityProperties.getRoles();
            System.out.println("====== ROLES: " + roles.toString());
            http.authorizeHttpRequests(new AuthorizationManagerRequestMatcherRegistry(roles));
        } catch(Exception e) {
            log.warn(e.getMessage());
        }
//        expressionInterceptUrlRegistry.anyRequest().authenticated();


        // Add JWT token filter
        http.addFilterBefore(
                jwtRequestFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    class AuthorizationManagerRequestMatcherRegistry implements Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> {

        private final List<String> roles;
        public AuthorizationManagerRequestMatcherRegistry(List<String> roles) {
            this.roles = roles;
            System.out.println("1111 ====== ROLES: " + this.roles.toString());
        }

        @Override
        public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorizationManagerRequestMatcherRegistry) {
//            authorizationManagerRequestMatcherRegistry.requestMatchers("/api/public/**").permitAll();
            try {
                log.info("22222 roles: ", this.roles.toString());
                System.out.println("22222 ====== ROLES: " + this.roles.toString());

                for (String roleItemStr : roles) {
                    try {
                        System.out.println("=======roleItemStr: "+ roleItemStr);
                        String[] roleItem = roleItemStr.split(",");
                        String role = roleItem[0];
                        String uri = roleItem[1];


                        System.out.println("=======uri: "+ uri);


                        boolean isPublicRole = "PUBLIC".equals(role);
                        if (isPublicRole) {
                            authorizationManagerRequestMatcherRegistry.requestMatchers(uri).permitAll();
                        } else {
                            authorizationManagerRequestMatcherRegistry.requestMatchers(uri).hasRole(role);
                        }
                    } catch (Exception e) {
                        log.error("bad <role,uri> pair format", e);
                    }
                }
            } catch(Exception e) {
                log.warn(e.getMessage());
            }
        }
    }
    /*
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http = http.cors().and().csrf().disable();

        handleException(http);

        setStateless(http);

        // Set permissions on endpoints
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
                expressionInterceptUrlRegistry =
                    http.authorizeRequests()
                            // Our public endpoints
                            .antMatchers("/api/public/**").permitAll()
                            .antMatchers("/api/public/test").permitAll()
                            .antMatchers("/public/test").permitAll()
                            .antMatchers(HttpMethod.GET, "/api/public/**").permitAll();
//                            .antMatchers(HttpMethod.POST, "/api/book/search").permitAll();

        // Our private endpoints
        try {
            Iterator<String> rolesIt = webSecurityProperties.getRoles().iterator();
            while (rolesIt.hasNext()) {
                try {
                    String[] roleItem = rolesIt.next().split(",");
                    String role = roleItem[0];
                    String uri = roleItem[1];

                    boolean isPublicRole = "PUBLIC".equals(role);
                    if (isPublicRole) {
                        expressionInterceptUrlRegistry.antMatchers(uri).permitAll();
                    } else {
                        expressionInterceptUrlRegistry.antMatchers(uri).hasRole(role);
                    }
                } catch (Exception e) {
                    log.error("bad <role,uri> pair format", e);
                }
            }
        } catch(Exception e) {
            log.warn(e.getMessage());
        }
        expressionInterceptUrlRegistry.anyRequest().authenticated();


        // Add JWT token filter
        http.addFilterBefore(
                jwtRequestFilter,
                UsernamePasswordAuthenticationFilter.class
        );
    } //*/

    private void setStateless(HttpSecurity http) throws Exception {
        // Set session management to stateless
        http = http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and();
    }

    private void handleException(HttpSecurity http) throws Exception {
        // Set unauthorized requests exception handler
        http = http
                .exceptionHandling()
                .authenticationEntryPoint(
                        (request, response, ex) -> {

                            String logStr = String.format("request: %s", request.toString());
                            System.out.println(logStr);
                            throw new Unauthorized(ex);
//                            response.sendError(
//                                    HttpServletResponse.SC_UNAUTHORIZED,
//                                    ex.getMessage()
//                            );
                        }
                )
                .and();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");

        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
