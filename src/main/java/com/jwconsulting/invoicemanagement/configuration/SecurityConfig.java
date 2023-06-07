package com.jwconsulting.invoicemanagement.configuration;

import com.jwconsulting.invoicemanagement.filter.CustomAuthorizationFilter;
import com.jwconsulting.invoicemanagement.handler.CustomAccessDeniedHandler;
import com.jwconsulting.invoicemanagement.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)    //only admins in our application will have access to certain methods
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final BCryptPasswordEncoder encoder;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final CustomAuthorizationFilter customAuthorizationFilter;
    private static final String[] PUBLIC_URLS = { "/user/register/**", "/user/login/**", "/user/verify/code/**", "/user/reset/password/**", "/user/verify/password/**" };
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().disable(); //disable cors so we can put in our own cors config
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeHttpRequests().requestMatchers(PUBLIC_URLS).permitAll();
        http.authorizeHttpRequests().requestMatchers(HttpMethod.DELETE, "/user/delete/**").hasAnyAuthority("DELETE:USER");    //if they have our specified authority of deletion users then they have permissions
        http.authorizeHttpRequests().requestMatchers(HttpMethod.DELETE, "/customer/delete/**").hasAnyAuthority("DELETE:CUSTOMER");
        http.exceptionHandling().accessDeniedHandler(customAccessDeniedHandler).authenticationEntryPoint(customAuthenticationEntryPoint);
        http.authorizeHttpRequests().anyRequest().authenticated();
        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Function to configure an authentication provider.
     * Tells it how to load user details and how to hash passwords.
     * Wraps the config inside an Authentication Manager, so spring can use it to handle authentication inside the application.
     **/
    @Bean
    public AuthenticationManager authenticationManager () {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();   //this object will do the check of the username and password
        authProvider.setUserDetailsService(userDetailsService);   //passing in the user spring needs to handle authentication
        authProvider.setPasswordEncoder(encoder);   //passing in what password encoder to use to check the password so the hashing algorithm is the same as when the password was originally stored
        return new ProviderManager(authProvider);   //ProviderManager is an impl of AuthenticationManager that can manage multiple AuthenticationProviders. We are using it to manage our DAOAuthProvider.
    }
}
