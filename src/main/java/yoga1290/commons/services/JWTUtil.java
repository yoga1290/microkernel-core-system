package yoga1290.commons.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import yoga1290.commons.models.JwtPayload;
import java.util.Date;
import java.util.List;

@Service
public class JWTUtil {

    private String secret;
    private ObjectMapper objectMapper;

    public JWTUtil(ObjectMapper objectMapper,
                   @Value("${yoga1290.commons.jwt.secret:}")
                   String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret;
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(secret).build().parse(token);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public UserDetails userDetailsByJWT(String jwtToken) {
        boolean hasJWTToken = jwtToken != null;
        if (hasJWTToken) {
            try {
                // TODO: token rotation/handle expiry?
                Jwt<?, JwtPayload> jwtPayload = (Jwt<?, JwtPayload>) Jwts.parser().setSigningKey(secret).build().parse(jwtToken);
                List<String> userRoles = jwtPayload.getPayload().getRoles();
                return User.builder()
                        .roles(String.valueOf(userRoles))
                        .authorities(AuthorityUtils.createAuthorityList(userRoles))
                        .build();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return User.builder()
                .roles("PUBLIC")
                .authorities(AuthorityUtils.createAuthorityList("PUBLIC"))
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(false)
                .username("TODO") //TODO
                .password("")
                .build();
    }

    public String issueJWT(String... roles) throws JsonProcessingException {
        JwtPayload jwtPayload = JwtPayload.builder().roles(List.of(roles)).build();
        String payloadStr = new ObjectMapper().writeValueAsString(jwtPayload);

        String jwtToken = Jwts.builder()
                //.setSubject("")
                .content(payloadStr)
                .expiration(new Date())
                .signWith(SignatureAlgorithm.HS256, secret).compact();

        return jwtToken;
    }

}
