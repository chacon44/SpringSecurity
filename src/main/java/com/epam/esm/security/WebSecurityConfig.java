package com.epam.esm.security;

import com.epam.esm.configuration.PasswordEncoderConfig;
import com.epam.esm.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
//
//  @Autowired
//  UserDetailsServiceImpl userDetailsService;
//
//  @Autowired
//  PasswordEncoderConfig passwordEncoder;
//
//
//
//  @Bean
//  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http.csrf(AbstractHttpConfigurer::disable)
//        .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
//            authorizationManagerRequestMatcherRegistry.requestMatchers(HttpMethod.DELETE).hasRole("ADMIN")
//                .requestMatchers("/admin/**").hasAnyRole("ADMIN")
//                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
//                .requestMatchers("/login/**").permitAll()
//                .anyRequest().authenticated())
//        .httpBasic(Customizer.withDefaults())
//        .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(
//            SessionCreationPolicy.STATELESS));
//
//    return http.build();
//  }
//
//    //TODO Use JdbcUserDetailsManager
//  @Bean
//  public UserDetailsService userDetailsService() {
//
//        //UserDetails object is created using the User builder, an utility class in Spring Security
//        //for creating UserDetails
//        //User has an username a password and a role
//
//
//
//    //User can have two roles: user or admin
//
//    //sql details
//    //get user details from database
//    //make use of JdbcUserDetailsManager
//        UserDetails user =
//        User.builder()
//            .username("user")
//            .password(passwordEncoder.passwordEncoder().encode("password"))
//            .roles("USER")
//            .build();
//
//        //ch
//    return new InMemoryUserDetailsManager(user);
//  }
//
//
}
