package com.internship.kyc_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // 1. PUBLIC PAGES (Anyone can access these to log in or register!)
                        .requestMatchers("/login", "/register", "/login.html", "/register.html", "/api/auth/register").permitAll()

                        // 2. ADMIN ENDPOINTS
                        .requestMatchers("/uploads/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/kyc/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/kyc").hasRole("ADMIN")

                        // 3. USER & ADMIN ENDPOINTS
                        .requestMatchers(HttpMethod.POST, "/api/kyc/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/kyc/*").hasAnyRole("USER", "ADMIN")

                        // 4. EVERYTHING ELSE (Requires you to be signed in)
                        .anyRequest().authenticated()
                )
                // 5. CUSTOM LOGIN UI
                .formLogin(form -> form
                        .loginPage("/login")               // Points to your AuthController
                        .loginProcessingUrl("/login")      // Where your HTML form POSTs the data
                        .defaultSuccessUrl("/", true)      // Go to index.html after success
                        .permitAll()
                )
                // 6. LOGOUT CONFIGURATION
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout") // Shows a nice message if you want
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Maps the /uploads URL path to the uploads/ directory on disk.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads").toAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    /**
     * Password encoder used by the UserRegistrationService
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // NOTE: The old UserDetailsService @Bean has been DELETED
    // because UserRegistrationService handles it now!
}