package yoga1290.coresystem.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import yoga1290.coresystem.exceptions.Unauthorized;
import yoga1290.coresystem.services.JWTService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({ MockController.class })
public class ControllerIT {

    @LocalServerPort
    private int PORT;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JWTService jwtService;

    private final String PUBLIC_URI = "/sample-public-endpoint";
    private final String ATHORIZED_URI = "/sample-athorized-endpoint";
    private final String UNATHORIZED_URI = "/sample-unathorized-endpoint";

    @Test
    void publicEndpoint() throws Exception {
        String requestUrl = String.format("http://localhost:%s%s", PORT, PUBLIC_URI);
        RequestEntity requestEntity = RequestEntity.get(requestUrl).build();
        Class<? extends Object> responseType = Void.class;
        boolean unauthorized = false;
        try {
            ResponseEntity responseEntity = this.restTemplate.exchange(requestEntity, responseType);
            int actualHttpStatus = responseEntity.getStatusCodeValue();
            assertThat(actualHttpStatus).isNotEqualTo(HttpStatus.FORBIDDEN.value());
        } catch (Unauthorized unauthorizedException) {
            unauthorized = true;
        }
        assertThat(unauthorized).isEqualTo(false);
    }

    @Test
    void unathorizedEndpoint() throws Exception {
        String requestUrl = String.format("http://localhost:%s%s", PORT, UNATHORIZED_URI);
        RequestEntity requestEntity = RequestEntity.get(requestUrl).build();
        Class<? extends Object> responseType = Void.class;
        boolean unauthorized = false;
        try {
            ResponseEntity responseEntity = this.restTemplate.exchange(requestEntity, responseType);
            int actualHttpStatus = responseEntity.getStatusCodeValue();
            assertThat(actualHttpStatus).isEqualTo(HttpStatus.FORBIDDEN.value());
            unauthorized = true;
        } catch (Unauthorized unauthorizedException) {
            unauthorized = true;
        }
        assertThat(unauthorized).isEqualTo(true);
    }

    @Test
    void athorizedEndpoint() throws Exception {
        String requestUrl = String.format("http://localhost:%s%s", PORT, ATHORIZED_URI);
        Class<? extends Object> responseType = Void.class;
        boolean unauthorized = false;
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", mockJWT()));
            RequestEntity requestEntity = RequestEntity
                                            .get(requestUrl)
                                            .headers(httpHeaders)
                                            .build();

            ResponseEntity responseEntity = this.restTemplate.exchange(requestEntity, responseType);
            int actualHttpStatus = responseEntity.getStatusCodeValue();
            assertThat(actualHttpStatus).isEqualTo(HttpStatus.FORBIDDEN.value());
        } catch (Unauthorized unauthorizedException) {
            unauthorized = true;
        }
        assertThat(unauthorized).isEqualTo(false);
    }

    private String mockJWT() throws JsonProcessingException {
        return jwtService.issueJWT("user@email.com", "USER");
    }

}