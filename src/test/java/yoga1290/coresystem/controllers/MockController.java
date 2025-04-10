package yoga1290.coresystem.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import yoga1290.coresystem.exceptions.Unauthorized;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Controller
public class MockController {

    @GetMapping("/sample-unathorized-endpoint")
    public ResponseEntity mockUnathorizedEndpoint() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sample-athorized-endpoint")
    public ResponseEntity mockAthorizedEndpoint() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sample-public-endpoint")
    public ResponseEntity mockPublicEndpoint() {
        return ResponseEntity.ok().build();
    }

}