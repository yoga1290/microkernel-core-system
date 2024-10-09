package yoga1290.commons.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import yoga1290.commons.models.ReCaptchResponse;

// see https://developers.google.com/recaptcha/docs/verify
public class ReCaptchaRequestService
        extends AbstractClientRequestService<Object, ReCaptchResponse> {

    private String secret;
    public ReCaptchaRequestService(
                            @Value("${yoga1290.commons.captcha.secret:}")
                            String secret,
                            RestTemplate restTemplate) {
        super(null, restTemplate);
        this.secret = secret;
    }

    public void verify(String recapResponse) throws RestClientResponseException {

        String requestUrl = String.format("https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s",
                                            secret, recapResponse);
        setRequestUrl(requestUrl);
        doPost(null);
    }

}
