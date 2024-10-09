package yoga1290.commons.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import yoga1290.commons.models.JwtPayload;

@ExtendWith(SpringExtension.class)
public class JWTUtilTest {

    private JWTService jwtUtil;

    private final String MOCK_SECRET = "01234567890123456789012345678901234567890123456789";
    private final String ROLE_PUBLIC = "PUBLIC";

    @BeforeEach
    public void setup() {
        jwtUtil = new JWTService(new ObjectMapper(), MOCK_SECRET);
    }

    @Test
    public void issueJWT_should_return_valid_JWT_token() throws JsonProcessingException {

        final String expectedJWT = "eyJhbGciOiJIUzI1NiJ9." +
                                   "eyJyb2xlcyI6WyJQVUJMSUMiXX0." +
                                   "jlGuJpU735D8pn0T3pulnUicfH4gw0jpmS2mqVAM1Dg";

        String actualJWT = jwtUtil.issueJWT(ROLE_PUBLIC);
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
        final String mockJWToken = "eyJhbGciOiJIUzI1NiJ9." +
                                "eyJyb2xlcyI6WyJQVUJMSUMiXX0." +
                                "jlGuJpU735D8pn0T3pulnUicfH4gw0jpmS2mqVAM1Dg";

        boolean actualResult = jwtUtil.validate(mockJWToken);
        Assertions.assertTrue(actualResult);

    }

}
