package com.example.campsjobs.Configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtWebFilter jwtFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://127.0.0.1:5500", "http://localhost:5500")); // Allow both localhost & 127.0.0.1
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow all necessary methods
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true); // Allow credentials like cookies, authorization headers
        configuration.setExposedHeaders(List.of("Authorization")); // Expose the Authorization header

        System.out.println("CORS Configuration Applied ✅");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all endpoints
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Apply CORS configuration
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/api/v1/camps-jobs/login",
                                        "/api/v1/camps-jobs/forgot-password",
                                        "/api/v1/camps-jobs/reset-password",
                                        "/api/v1/camps-jobs/validate-token",
                                        "/api/v1/camps-jobs/email/**",
                                        "/api/v1/camps-jobs/fullname",
                                        "/api/v1/camps-jobs/register",
                                        "/api/job/all-jobs",
                                        "/api/job/info/**",
                                        "/api/job-seeker/save",
                                        "/api/employer/save",
                                        "/api/employer/employer-name",
                                        "/api/application/download/**",
                                        "/sms/send",
                                        "/email/send",
                                        "/email/status/send",
                                        "/api/application/job-seeker-email/**",
                                        "/api/contact/send").permitAll()
                                .requestMatchers("/api/job/post-job").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/job/applications/count/**").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/job/details/**").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/job/update/**").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/job/delete/**").hasAnyAuthority("ROLE_EMPLOYER", "ROLE_ADMIN") // Allow both roles
                                .requestMatchers("/api/job/my-jobs").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/application/updateStatus/**").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/application/employer-applications").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/application/download/**").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/employer/applications").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/employer/getEmpId/**").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/employer/details/**").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/employer/update/**").hasAuthority("ROLE_EMPLOYER")
                                .requestMatchers("/api/job-seeker/get-all-seekers").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/v1/camps-jobs/get-all-users").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/job/update-job-status/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/admin/stats/employers/count").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/admin/stats/employers/count").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/admin/stats/applications/count").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/admin/stats/jobs/count").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/admin/stats/users/new").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/admin/stats/jobs/top").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/admin/stats/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/application/apply/**").hasAuthority("ROLE_JOBSEEKER")
                                .requestMatchers("/api/applications/my-applications?**").hasAuthority("ROLE_JOBSEEKER")
                                .requestMatchers("/api/application/seeker/**").hasAuthority("ROLE_JOBSEEKER")
                                .requestMatchers("/api/application/check/**").hasAuthority("ROLE_JOBSEEKER")
                                .requestMatchers("/api/application/seeker/delete/**").hasAuthority("ROLE_JOBSEEKER")
                                .requestMatchers("/api/job-seeker/user/**").hasAuthority("ROLE_JOBSEEKER")
                                .requestMatchers("/api/job-seeker/update/**").hasAuthority("ROLE_JOBSEEKER")
                                .requestMatchers("/api/job-seeker/seeker/details/**").hasAuthority("ROLE_JOBSEEKER")
                                .anyRequest().authenticated() // Any other request needs to be authenticated
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .addFilterBefore(corsFilter(), UsernamePasswordAuthenticationFilter.class) // Add the CORS filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public OncePerRequestFilter corsFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                System.out.println("CORS Filter: Handling request for " + request.getRequestURI());
                response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
                response.setHeader("Access-Control-Allow-Credentials", "true");

                if (request.getMethod().equals("OPTIONS")) {
                    System.out.println("CORS Filter: Handling OPTIONS request");
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}