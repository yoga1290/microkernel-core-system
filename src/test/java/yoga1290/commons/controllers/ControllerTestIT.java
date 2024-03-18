package yoga1290.commons.controllers;

import io.micrometer.core.ipc.http.HttpSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import yoga1290.commons.exceptions.Unauthorized;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerTestIT {

    @LocalServerPort
    private int PORT;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String URI = "/sample-endpoint-controller";

    @Test
    void SampleEndpointControllerShouldWork() throws Exception {
        String requestUrl = String.format("http://localhost:%s%s", PORT, URI);
        RequestEntity requestEntity = RequestEntity.get(requestUrl).build();
        Class<? extends Object> responseType = Void.class;
        boolean unauthorized = false;
        try {
            ResponseEntity responseEntity = this.restTemplate.exchange(requestEntity, responseType);
            System.out.println("responseEntity: "+ responseEntity);
            System.out.println("========================================");
            System.out.println("========================================");
            int actualHttpStatus = responseEntity.getStatusCodeValue();
            assertThat(actualHttpStatus).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
        } catch (Unauthorized unauthorizedException) {
            unauthorized = true;
        }

        assertThat(unauthorized).isEqualTo(true);


    }

}