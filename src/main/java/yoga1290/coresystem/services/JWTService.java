package yoga1290.coresystem.services;

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
import yoga1290.coresystem.models.JwtPayload;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Service
public class JWTService {

    private SecretKey secretKey;
    private ObjectMapper objectMapper;
    private Long expiry = 15 * 60 * 1000L;

    //TODO:
    @Setter
    private IjwtValidator ijwtValidator = null;

    public JWTService(
               @Value("${yoga1290.coresystem.jwt.secret:}")
               String secret,
               @Value("${yoga1290.coresystem.jwt.exp:900000}")
               Long expiry) {
        this.secretKey = mapSecretB64StringToSecretKey(secret);
        this.expiry = expiry;
        this.objectMapper = new ObjectMapper();
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
        boolean hasJWTToken = jwtToken != null && !jwtToken.isEmpty();
        if (hasJWTToken) {
            try {
                JwtPayload jwtPayload = parseJWToken(jwtToken);
                List<String> userRoles = jwtPayload.getRoles();

                boolean hasUnexpiredToken = null != jwtPayload.getExp() &&
                                      System.currentTimeMillis() <= jwtPayload.getExp();
                if (hasUnexpiredToken) {
                    return User.builder()
                            .username(jwtPayload.getUserEmail())
                            .roles(userRoles.toArray(new String[userRoles.size()]))
                            .authorities(AuthorityUtils.createAuthorityList(userRoles))
                            .build();

                }
            } catch (Exception e) {
                e.printStackTrace(); //TODO
                System.out.println("userDetailsByJWT: " + e.getMessage());
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
        return guestUserDetails;
    }

    public String issueJWT(String userEmail, String... roles) throws JsonProcessingException {
        JwtPayload jwtPayload = JwtPayload.builder()
                                    .userEmail(userEmail)
                                    .roles(List.of(roles)).build();
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
