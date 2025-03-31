package yoga1290.coresystem.exceptions;

import org.springframework.security.core.AuthenticationException;

public class Unauthorized extends RuntimeException {
    public Unauthorized(AuthenticationException ex) {
        super(ex);
    }
}
