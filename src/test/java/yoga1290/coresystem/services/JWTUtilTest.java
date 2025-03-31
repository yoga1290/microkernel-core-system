package yoga1290.coresystem.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import yoga1290.coresystem.models.JwtPayload;

@ExtendWith(SpringExtension.class)
public class JWTUtilTest {

    private JWTService jwtUtil;

    private final String MOCK_SECRET = "JWTSECRET123JWTSECRET123JWTSECRET123JWTSECRET123";
    private final String ROLE_PUBLIC = "PUBLIC";

    @BeforeEach
    public void setup() {
        jwtUtil = new JWTService(new ObjectMapper(), MOCK_SECRET);
    }

    @Test
    public void issueJWT_should_return_valid_JWT_token() throws JsonProcessingException {

        final String expectedJWT =  "eyJhbGciOiJIUzI1NiJ9."+
                                    "eyJyb2xlcyI6WyJQVUJMSUMiXSwidXNlckVtYWlsIjoiY29uZmlybWVkQGVtYWlsLmNvbSJ9." +
                                    "vCjX5dRukcKv17hcCu0ySeeRPGWZkUXKOn3xJdYhaYc";

        String actualJWT = jwtUtil.issueJWT("confirmed@email.com", ROLE_PUBLIC);
        Assertions.assertEquals(expectedJWT, actualJWT);
    }

    @Test
    public void validate_should_accept_valid_JWT_token_with_valid_ROLE() {

        final String EXPECTED_ROLE = ROLE_PUBLIC;
        jwtUtil.setIjwtValidator(new JWTService.IjwtValidator() {
            @Override
            public boolean validate(JwtPayload jwtPayload) {
                boolean hasValidRole = jwtPayload.getRoles().contains(EXPECTED_ROLE);
                Assert.isTrue(hasValidRole, "JWT Token does not have correct payload");
                return hasValidRole;
            }
        });
        final String mockJWToken =  "eyJhbGciOiJIUzI1NiJ9."+
                                    "eyJyb2xlcyI6WyJQVUJMSUMiXSwidXNlckVtYWlsIjoiY29uZmlybWVkQGVtYWlsLmNvbSJ9." +
                                    "vCjX5dRukcKv17hcCu0ySeeRPGWZkUXKOn3xJdYhaYc";

        boolean actualResult = jwtUtil.validate(mockJWToken);
        Assertions.assertTrue(actualResult);

    }

}
