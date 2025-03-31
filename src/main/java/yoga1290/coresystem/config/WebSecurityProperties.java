package yoga1290.coresystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties("yoga1290.coresystem.security")
public class WebSecurityProperties {

    // yoga1290.commons.security.roles.0=<ROLE>,<URI>
    List<String> roles = Arrays.asList("PUBLIC, /public");
}
