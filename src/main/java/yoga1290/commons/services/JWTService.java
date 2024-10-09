package yoga1290.commons.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import yoga1290.commons.models.JwtPayload;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Service
public class JWTService {

    private SecretKey secretKey;
    private ObjectMapper objectMapper;

    //TODO:
    @Setter
    private IjwtValidator ijwtValidator = null;

    public JWTService(ObjectMapper objectMapper,
                   @Value("${yoga1290.commons.jwt.secret:}")
                   String secret) {
        this.objectMapper = objectMapper;
        this.secretKey = mapSecretB64StringToSecretKey(secret);
    }

    private JwtPayload parseJWToken(String jwtToken) throws IOException {

        Jwt<?, ?> jwtContentBytes = Jwts.parser()
                .verifyWith(secretKey).build()
                .parse(jwtToken);

        DefaultClaims defaultClaims = (DefaultClaims) jwtContentBytes.getPayload();
        String defaultClaimsStr = objectMapper.writeValueAsString(defaultClaims);
        return objectMapper.readValue(defaultClaimsStr, JwtPayload.class);
    }

    public boolean validate(String jwtToken) {
        try {
            JwtPayload jwtPayload = parseJWToken(jwtToken);
            return ijwtValidator.validate(jwtPayload);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public UserDetails userDetailsByJWT(String jwtToken) {
        boolean hasJWTToken = jwtToken != null;
        if (hasJWTToken) {
            try {
                JwtPayload jwtPayload = parseJWToken(jwtToken);
                List<String> userRoles = jwtPayload.getRoles();
                return User.builder()
                        .roles(String.valueOf(userRoles))
                        .authorities(AuthorityUtils.createAuthorityList(userRoles))
                        .build();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        UserDetails guestUserDetails = User.builder()
                    .roles("PUBLIC")
                    .authorities(AuthorityUtils.createAuthorityList("PUBLIC"))
                    .accountExpired(false)
                    .credentialsExpired(false)
                    .accountLocked(false)
                    .username("TODO") //TODO
                    .password("")
                    .build();
        System.out.println("guestUserDetails: "+ guestUserDetails.toString());
        return guestUserDetails;
    }

    public String issueJWT(String... roles) throws JsonProcessingException {
        JwtPayload jwtPayload = new JwtPayload();
        jwtPayload.setRoles(List.of(roles));
        String payloadStr = new ObjectMapper().writeValueAsString(jwtPayload);

        String jwtToken = Jwts.builder()
                .content(payloadStr)
                .signWith(secretKey, Jwts.SIG.HS256).compact();

        return jwtToken;
    }

    public static interface IjwtValidator {
        public boolean validate(JwtPayload jwtPayload);
    }


    private SecretKey mapSecretB64StringToSecretKey(String base64EncodedSecretKey) {
        byte[] secretKeyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }

}
