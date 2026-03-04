package com.b2bplatform.auth.controller;

import com.b2bplatform.auth.service.OperatorServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginPageController {
    
    private final OperatorServiceClient operatorServiceClient;
    
    @GetMapping(value = {"/login", "/login/"})
    public String loginPage(Model model) {
        log.debug("GET /login - Default login page");
        model.addAttribute("operatorCode", "");
        model.addAttribute("operatorName", "B2B Gaming Platform");
        return "login";
    }
    
    @GetMapping("/login/{operatorCode}")
    public Mono<String> loginPageForOperator(@PathVariable String operatorCode, Model model) {
        log.debug("GET /login/{} - Operator-specific login page", operatorCode);
        
        return operatorServiceClient.getOperatorByCode(operatorCode)
            .map(operator -> {
                model.addAttribute("operatorCode", operatorCode);
                model.addAttribute("operatorName", operator.get("name"));
                return "login";
            })
            .defaultIfEmpty("error")
            .onErrorReturn("error");
    }
}
