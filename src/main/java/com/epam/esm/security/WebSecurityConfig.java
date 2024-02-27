package com.epam.esm.security;

import static org.springframework.security.config.Customizer.withDefaults;

import com.epam.esm.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests((requests) -> requests
            //This means that every user, even unauthenticated ones, can access the "/" and "/home" paths.
            .requestMatchers("/api/generation/users","/error", "/login", "/api/login").permitAll()
            //This means for all other requests, the user must be authenticated to access them.
            // So for every other path than "/" and "/home", you must be authenticated.
            .anyRequest().authenticated()


            //create token
            //receive loggin using spring security
        ).authenticationManager(authenticationManager).httpBasic(withDefaults());

    return http.build();
  }
//  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http.csrf()            .disable()            .authorizeRequests()
//        .expressionHandler(webSecurityExpressionHandler())
//        .antMatchers(HttpMethod.GET, "/roleHierarchy")
//        .hasRole("STAFF");

    //TODO Use JdbcUserDetailsManager
  @Bean
  public UserDetailsService userDetailsService() {

        //UserDetails object is created using the User builder, an utility class in Spring Security
        //for creating UserDetails
        //User has an username a password and a role

    //User can have two roles: user or admin

    //sql details
    //get user details from database
    //make use of JdbcUserDetailsManager
        UserDetails user =
        User.builder()
            .username("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build();

        //ch
    return new InMemoryUserDetailsManager(user);
  }
  @Bean
  public PasswordEncoder passwordEncoder() {

    // this way of encoding passwords is better than using withDefaultPasswordEncoder() because
    // that method implies you are storing passwords in plain text, while this method
    // uses BCrypt strong hashing function
    return new BCryptPasswordEncoder();
  }
}
