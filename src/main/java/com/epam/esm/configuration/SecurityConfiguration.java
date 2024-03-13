package com.epam.esm.configuration;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

//import com.epam.esm.service.JwtTokenService;
//import com.epam.esm.service.UserDetailsServiceImpl;
import com.epam.esm.service.JwtTokenService;
import com.epam.esm.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Autowired
  private JwtTokenService tokenService;

  @Autowired
  PasswordEncoder passwordEncoder;


  //TODO do filterchain for basic auth, and other one for token authentication
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
            authorizationManagerRequestMatcherRegistry
                .requestMatchers(HttpMethod.DELETE).hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH).hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST).hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT).hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .requestMatchers("/login/**", "/api/**").permitAll()
                .anyRequest().authenticated())
        .sessionManagement(sessionManagement ->
            sessionManagement
                .sessionCreationPolicy(STATELESS))
        .authenticationManager(authenticationManager)
        .httpBasic(Customizer.withDefaults())
    ;

    return http.build();
  }
}
