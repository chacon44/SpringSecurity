package com.epam.esm.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @GetMapping("/login")
  public String getLoginPage(Model model) {
    return "login";
  }

  @GetMapping("/home")
  public String getHomePage(Model model, OAuth2AuthenticationToken authentication) {
//    if (authentication != null) {
//      String name = authentication.getPrincipal().getAttribute("name");
//      model.addAttribute("username", name);
//    }
    return "home";
  }
}
