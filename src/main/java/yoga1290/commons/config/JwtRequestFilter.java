package yoga1290.commons.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import yoga1290.commons.services.JWTUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JWTUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    public JwtRequestFilter(JWTUtil jwtTokenUtil,
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
        } else {
            userDetails = jwtTokenUtil.userDetailsByJWT(null);
        }


//        System.out.println(">>>>>>>>>>> JwtRequestFilter.userDetails "+userDetails.toString());
//        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(
//                                                            userDetails.toString(),
//                                                            userDetails,
//                                                            userDetails.getAuthorities());

//        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(
//                userDetails.toString(),
//                userDetails,
//                userDetails.getAuthorities());

//        if (!hasAuthHeader) {
//            chain.doFilter(request, response);
//            return;
//        }

        // Get jwt token and validate
//        final String token = header.split(" ")[1].trim();
//        if (!jwtTokenUtil.validate(token)) {
//            chain.doFilter(request, response);
//            return;
//        }
        //TODO: handle expired tokens

        // Get user identity and set it on the spring security context
//        UserDetails userDetails = userDetailsService
//                .loadUserByUsername(jwtTokenUtil.getUsername(token));



//        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(null,
//                userDetails, userDetails.getAuthorities());

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
