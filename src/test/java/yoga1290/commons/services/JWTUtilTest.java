package yoga1290.commons.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
public class JWTUtilTest {

    @InjectMocks
    private JWTUtil jwtUtil;

    private final String MOCK_SECRET = "01234567890123456789012345678901234567890123456789";
    private final String MOCK_USER_ID = "MOCK_USER_ID";

    @BeforeEach
    public void setup() {
        jwtUtil = new JWTUtil(new ObjectMapper(), MOCK_SECRET);
    }

    @Test
    public void validate_should_accept_JWT_token() {
        final String expectedJWT = "eyJhbGciOiJIUzI1NiJ9.eyJrZXkiOiAidmFsdWUifQ.mAkROzvg1ArI037AFf2OyVJoC43P7jSvdH_u3NE6Pi4";
//        OAuth2ResourceServerProperties.Jwt
        String actualJWT = Jwts.builder()
                            .setPayload("{\"key\": \"value\"}")
                            .signWith(SignatureAlgorithm.HS256, MOCK_SECRET).compact();
        boolean actualResult = jwtUtil.validate(actualJWT);
        Assertions.assertEquals(expectedJWT, actualJWT);
        Assertions.assertTrue(actualResult);
    }

}
