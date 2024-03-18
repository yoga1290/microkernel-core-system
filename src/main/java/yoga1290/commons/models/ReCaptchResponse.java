package yoga1290.commons.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


//see https://developers.google.com/recaptcha/docs/verify
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReCaptchResponse {

    @NotNull
    @Pattern(regexp = "true")
    boolean success;

    @JsonProperty("challenge_ts")
    String timestamp;

}
