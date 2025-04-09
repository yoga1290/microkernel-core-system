package yoga1290.coresystem.models;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtPayload {

    List<String> roles;
    String userEmail;

    // https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.4
    Long exp;

}
