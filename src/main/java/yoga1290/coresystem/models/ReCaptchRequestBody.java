package yoga1290.coresystem.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReCaptchRequestBody {

    @NotBlank
    String secret;

    @NotBlank
    String response;

    String remoteip;
}
