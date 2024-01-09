package com.epam.esm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
//@Profile({"!h2"})
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
    System.out.println("Hello world!");
  }
}
