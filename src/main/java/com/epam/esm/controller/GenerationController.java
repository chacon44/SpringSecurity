package com.epam.esm.controller;

import com.epam.esm.service.GenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/generation")
public class GenerationController {

  @Autowired
  private GenerationService dataGenerationService;

  /**
   * Generate random users
   */
  @PostMapping("/users")
  public void generateUsers() {
    dataGenerationService.generateUsers();
  }

  /**
   * Generate random tags
   */
  @PostMapping("/tags")
  public void generateTags() {
    dataGenerationService.generateTags();
  }

  /**
   * Generate random certificates
   */
  @PostMapping("/certificates")
  public void generateCertificates() {
    dataGenerationService.generateCertificates();
  }
  @PostMapping("/deleteData")
  public void deleteData() {
    dataGenerationService.deleteData();
  }
}
