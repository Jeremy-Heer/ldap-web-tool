package com.example.ldapwebtool.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class LdapAuthenticationProvider implements AuthenticationProvider {
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        // For this LDAP proxy, we don't validate credentials here.
        // We pass them through to the LDAP server in the service layer.
        // This allows the API to work with any LDAP server.
        
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            throw new BadCredentialsException("Username and password are required");
        }
        
        // Create authenticated token with the credentials
        // The actual LDAP authentication happens in the service layer
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            username, 
            password, 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        return authToken;
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
