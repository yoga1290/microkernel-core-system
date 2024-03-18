package yoga1290.commons.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import org.springframework.boot.autoconfigure.security.*;

import java.util.Date;

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

    public String getJWT(Object obj) throws JsonProcessingException {

        String payload = objectMapper.writeValueAsString(obj);

        String jwtToken = Jwts.builder()
                                //.setSubject("")
                                .setPayload(payload)
                                .setIssuedAt(new Date())
                                .signWith(SignatureAlgorithm.HS256, secret).compact();

        return jwtToken;
    }

    public String getUsername(String token) {
        return ""; //TODO
    }

}
