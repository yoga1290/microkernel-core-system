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

}
