package yoga1290.commons.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.nio.charset.Charset;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


@Slf4j
public abstract class AbstractClientRequestService<RequestT extends  Object, ResponseT extends  Object> implements IService {

    @Setter @Getter
    private String requestUrl;
    private String headerBasicUsername;
    private String headerBasicPassword;
    private String headerBearerToken;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    private ResponseT responseT;

    public AbstractClientRequestService(String requestUrl,
                                        String headerBasicUsername,
                                        String headerBasicPassword,
                                        RestTemplate restTemplate) {
        this.requestUrl = requestUrl;
        this.restTemplate = restTemplate;
        this.headerBasicUsername = headerBasicUsername;
        this.headerBasicPassword = headerBasicPassword;
    }

    public AbstractClientRequestService(String requestUrl,
                                        String headerBearerToken,
                                        RestTemplate restTemplate) {
        this.requestUrl = requestUrl;
        this.restTemplate = restTemplate;
        this.headerBearerToken = headerBearerToken;
    }

    public AbstractClientRequestService(String requestUrl,
                                        RestTemplate restTemplate) {
        this.requestUrl = requestUrl;
        this.restTemplate = restTemplate;
    }

    public AbstractClientRequestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<ResponseT> doPost(RequestT requestT) {
        return doPost(this.requestUrl, requestT);
    }
    public ResponseEntity<ResponseT> doPost(final String requestUrl, RequestT requestT) throws RestClientResponseException {
        String logStr;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<RequestT>> requestViolations = validator.validate(requestT, requestT.getClass());
        for (ConstraintViolation violation : requestViolations) {
            logStr = String.format("%s %s", violation.getPropertyPath().toString(), violation.getMessage());
            log.error(logStr);
        }

        HttpEntity<RequestT> entity = new HttpEntity<>(requestT, createHeaders());
        logStr = String.format("requestUrl: %s | HttpEntity: %s", requestUrl, entity);
        log.info(logStr);

        ResponseEntity<ResponseT> responseEntity =
                restTemplate.exchange(requestUrl, HttpMethod.POST, entity, (Class<ResponseT>) responseT.getClass());
        logStr = String.format("requestUrl: %s | HttpEntity: %s | ResponseEntity: %s",
                                                    requestUrl, entity, responseEntity);
        log.info(logStr);

        boolean hasResponseBody = responseEntity.getBody() != null;
        if (hasResponseBody) {
//            String responseStr = responseEntity.getBody();
//            ResponseT responseBody = objectMapper.readValue(responseStr, responseT.getClass());
//            Set<ConstraintViolation<ResponseT>> responseViolations = validator.validate(requestT, responseT.getClass());
            Set<ConstraintViolation<ResponseT>> responseViolations = validator.validate(responseEntity.getBody());
            boolean hasViolations = !responseViolations.isEmpty();
            String exceptionStr = "";
            for (ConstraintViolation violation : responseViolations) {
                logStr = String.format("%s %s", violation.getPropertyPath().toString(), violation.getMessage());
                exceptionStr += logStr;
                log.warn(logStr);
            }
            if (hasViolations) {
                throw new RestClientResponseException(exceptionStr,
                                BAD_REQUEST.value(),
                                BAD_REQUEST.name(),
                                responseEntity.getHeaders(),
                                responseEntity.getBody().toString().getBytes(), null);
            }
        }

        return responseEntity;
    }

    public ResponseEntity<ResponseT> doGet(final String requestUrl) throws RestClientResponseException {
        String logStr;

        HttpEntity<RequestT> entity = new HttpEntity<>(null, createHeaders());
        logStr = String.format("requestUrl: %s | HttpEntity: %s", requestUrl, entity);
        log.info(logStr);

        ResponseEntity<ResponseT> responseEntity =
                restTemplate.exchange(requestUrl, HttpMethod.POST, entity, (Class<ResponseT>) responseT.getClass());
        logStr = String.format("requestUrl: %s | HttpEntity: %s | ResponseEntity: %s",
                requestUrl, entity, responseEntity);
        log.info(logStr);

        boolean hasResponseBody = responseEntity.getBody() != null;
        if (hasResponseBody) {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<ResponseT>> responseViolations = validator.validate(responseEntity.getBody());
            boolean hasViolations = !responseViolations.isEmpty();
            String exceptionStr = "";
            for (ConstraintViolation violation : responseViolations) {
                logStr = String.format("%s %s", violation.getPropertyPath().toString(), violation.getMessage());
                exceptionStr += logStr;
                log.warn(logStr);
            }
            if (hasViolations) {
                throw new RestClientResponseException(exceptionStr,
                        BAD_REQUEST.value(),
                        BAD_REQUEST.name(),
                        responseEntity.getHeaders(),
                        responseEntity.getBody().toString().getBytes(), null);
            }
        }

        return responseEntity;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        boolean hasBasicAuth = headerBasicUsername != null &&
                                headerBasicPassword != null;
        boolean hasAuthToken = headerBearerToken != null;

        if (hasAuthToken) {
            String hvalue = String.format("bearer %s", headerBearerToken);
            httpHeaders.add(HttpHeaders.AUTHORIZATION, hvalue);
        }
        if (hasBasicAuth) {
            httpHeaders.setBasicAuth(headerBasicUsername, headerBasicPassword);
        }

        return httpHeaders;
    }
}
