package com.topwatch.back_topwatch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/api/v1")
public class UserController {

    @GetMapping(value = "health")
    public String healthCheck(){
        return "Hola mundo";
    }

}
