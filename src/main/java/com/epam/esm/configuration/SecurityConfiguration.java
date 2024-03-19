package com.epam.esm.configuration;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.epam.esm.security.AuthoritiesFromUsernameConverter;
import com.epam.esm.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Autowired
  private UserDetailsServiceImpl userDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Bean
  @Order(1)
  public SecurityFilterChain basicAuthFilterChain(HttpSecurity http) throws Exception {

    http
        .securityMatcher("/api/login")
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(Customizer.withDefaults())
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(DELETE).hasRole("ADMIN")
            .requestMatchers(PATCH).hasRole("ADMIN")
            .requestMatchers(POST).hasRole("ADMIN")
            .requestMatchers(PUT).hasRole("ADMIN")
            .requestMatchers(GET).hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated())
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS));

    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain githubOAuth2SecurityFilterChain(HttpSecurity http) throws Exception {

    http
        .securityMatcher("/api/github/login", "/oauth2/**", "/login/oauth2/code/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorizeRequests -> authorizeRequests
            .requestMatchers(DELETE).hasRole("ADMIN")
            .requestMatchers(PATCH).hasRole("ADMIN")
            .requestMatchers(POST).hasRole("ADMIN")
            .requestMatchers(PUT).hasRole("ADMIN")
            .requestMatchers(GET).hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated())
        .oauth2Login(withDefaults());

    return http.build();
  }

  @Bean
  @Order(3)
  public SecurityFilterChain permitAllSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/unsecured/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  @Bean
  @Order(4)
  public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder, JwtAuthenticationConverter converter) throws Exception {
    http
        .securityMatcher("/api/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(DELETE).hasRole("ADMIN")
            .requestMatchers(PATCH).hasRole("ADMIN")
            .requestMatchers(POST).hasRole("ADMIN")
            .requestMatchers(PUT).hasRole("ADMIN")
            .requestMatchers(GET).hasAnyRole("USER", "ADMIN")
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .decoder(jwtDecoder)
                .jwtAuthenticationConverter(converter)
            )
        );
    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter converter(
      AuthoritiesFromUsernameConverter authoritiesFromUsernameConverter) {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesFromUsernameConverter);
    return jwtAuthenticationConverter;
  }
}
