package com.epam.esm.controller;

import com.epam.esm.dto.DemoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/unsecured")
public class UnsecuredController {

    @GetMapping("/demo")
    public DemoResponse getDemo() {
        return new DemoResponse("Anyone can access this");
    }

    @PostMapping("/demo")
    public DemoResponse postDemo() {
        return new DemoResponse("Anyone can POST to this");
    }
}
