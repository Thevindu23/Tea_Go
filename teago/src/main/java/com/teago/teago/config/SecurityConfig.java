package com.teago.teago.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean public org.springframework.security.core.userdetails.UserDetailsService userDetailsService(com.teago.teago.Repositories.UserRepository users) {
        return username -> {
            var user = users.findByUsername(username).orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Invalid credentials"));
            String role = user.getRole() == com.teago.teago.models.User.Role.LandOwner ? "LAND_OWNER" :
                user.getRole() == com.teago.teago.models.User.Role.FactoryOwner ? "FACTORY_OWNER" : "UNAVAILABLE";
            return org.springframework.security.core.userdetails.User.withUsername(username).password(user.getPassword()).roles(role).build();
        };
    }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public HttpSessionSecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
    @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.securityContext(c -> c.securityContextRepository(securityContextRepository()))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error",
                    "/api/auth/csrf", "/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/dashboard-owner.html").hasRole("LAND_OWNER")
                .requestMatchers("/dashboard-factory.html").hasRole("FACTORY_OWNER")
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req,res,ex) -> {
                    res.setStatus(401); res.setContentType("application/json");
                    res.getWriter().write("{\"message\":\"Please sign in\"}");
                })
                .accessDeniedHandler((req,res,ex) -> {
                    res.setStatus(403); res.setContentType("application/json");
                    res.getWriter().write("{\"message\":\"Access denied or expired security token\"}");
                }))
            .formLogin(f -> f.disable()).httpBasic(b -> b.disable()).logout(l -> l.disable());
        return http.build();
    }
}
