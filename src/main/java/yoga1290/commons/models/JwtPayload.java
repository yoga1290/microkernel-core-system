package yoga1290.commons.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import java.util.List;

@Data
@Builder
public class JwtPayload extends OAuth2ResourceServerProperties.Jwt {

    List<String> roles;

}
