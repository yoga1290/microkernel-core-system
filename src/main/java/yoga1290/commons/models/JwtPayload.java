package yoga1290.commons.models;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtPayload {

    List<String> roles;

}
