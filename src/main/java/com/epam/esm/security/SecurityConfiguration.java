package com.epam.esm.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
      successHandler.setDefaultTargetUrl("/home");

      http.authorizeRequests(authorize -> authorize
              .requestMatchers("/", "/login**", "/error**").permitAll()
              .anyRequest().authenticated())
          .oauth2Login(oauthLogin -> oauthLogin
              .successHandler(successHandler));

      return http.build();
    }
}
