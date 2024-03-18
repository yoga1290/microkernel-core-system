package yoga1290.commons.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
public abstract class AbstractEndpoint<RequestModel, ResponseModel> {

    @RequestMapping
    public abstract ResponseEntity<ResponseModel> doPost(@Valid @RequestBody RequestModel requestModel);

}
