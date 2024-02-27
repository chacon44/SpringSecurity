package com.epam.esm.security;

import com.epam.esm.service.GithubUserService;
import java.util.Collection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthoritiesFromUsernameConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final UserDetailsService userDetailsService;
    private final GithubUserService githubUserService;

    public AuthoritiesFromUsernameConverter(UserDetailsService userDetailsService,
                                            GithubUserService githubUserService) {
        this.userDetailsService = userDetailsService;
        this.githubUserService = githubUserService;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        String username = source.getSubject();
        if (githubUserService.isGithubUser(username)) {
            return githubUserService.getGithubUserAuthorities();
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
