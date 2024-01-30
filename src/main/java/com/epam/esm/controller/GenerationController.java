package com.epam.esm.controller;

import com.epam.esm.service.GenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generation")
public class GenerationController {

  //TODO document this class, not service

  @Autowired
  private GenerationService dataGenerationService;

  @PostMapping("/users")
  public void generateUsers() {
    dataGenerationService.generateUsers();
  }
  @PostMapping("/tags")
  public void generateTags() {
    dataGenerationService.generateTags();
  }
  @PostMapping("/certificates")
  public void generateCertificates() {
    dataGenerationService.generateCertificates();
  }
  @PostMapping("/deleteData")
  public void deleteData() {
    dataGenerationService.deleteData();
  }
}
