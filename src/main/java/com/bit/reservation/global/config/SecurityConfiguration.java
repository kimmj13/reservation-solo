package com.bit.reservation.global.config;

import com.bit.reservation.global.security.auth.filter.JwtAuthenticationFilter;
import com.bit.reservation.global.security.auth.filter.JwtVerificationFilter;
import com.bit.reservation.global.security.auth.handler.MemberAccessDeniedHandler;
import com.bit.reservation.global.security.auth.handler.MemberAuthenticationEntryPoint;
import com.bit.reservation.global.security.auth.handler.MemberAuthenticationFailureHandler;
import com.bit.reservation.global.security.auth.handler.MemberAuthenticationSuccessHandler;
import com.bit.reservation.global.security.auth.jwt.JwtTokenizer;
import com.bit.reservation.global.security.utils.CustomAuthorityUtils;
import com.bit.reservation.global.security.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final RedisUtils redisUtils;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().sameOrigin()
                .and()
                .csrf().disable()
                .cors(withDefaults())
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new MemberAuthenticationEntryPoint())
                .accessDeniedHandler(new MemberAccessDeniedHandler())
                .and()
                .apply(new CustomFilterConfigurer())
                .and()
                .authorizeHttpRequests(auth -> auth
                        .antMatchers(HttpMethod.GET, "/api/doctors", "/api/reservation", "/api/users", "/api/hospital-rate").hasAnyRole("ADMIN")
                        .antMatchers(HttpMethod.PUT, "/api/hospital/**").hasAnyRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/api/estimate/*/hospital/*", "/api/estimate/hospital/*").hasAnyRole("HOSPITAL", "ADMIN")
                        .antMatchers("/api/estimate/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/**").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/users/sign-up", "/api/hospital").permitAll()
                        .antMatchers(HttpMethod.PATCH, "/api/auth/password/**").permitAll()
                        .antMatchers("/api/doctors/**").hasRole("OK")
                        .antMatchers("/api/doctors/**", "/api/hospital-notice/**").hasAnyRole("HOSPITAL", "ADMIN")
                        .antMatchers("/api/hospital-rate/**", "/api/users/**").hasAnyRole("USER")
                        .antMatchers("/h2/**").permitAll()
                        .antMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);

            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer, redisUtils, authorityUtils);
            jwtAuthenticationFilter.setFilterProcessesUrl("/api/auth/login");
            jwtAuthenticationFilter.setAuthenticationSuccessHandler(new MemberAuthenticationSuccessHandler());
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new MemberAuthenticationFailureHandler());

            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils, redisUtils);

            builder.addFilter(jwtAuthenticationFilter)
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class);
        }
    }

}
