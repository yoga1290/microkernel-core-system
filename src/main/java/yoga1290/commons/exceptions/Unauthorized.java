package yoga1290.commons.exceptions;

import org.springframework.security.core.AuthenticationException;

public class Unauthorized extends RuntimeException {
    public Unauthorized(AuthenticationException ex) {
        super(ex);
    }
}
