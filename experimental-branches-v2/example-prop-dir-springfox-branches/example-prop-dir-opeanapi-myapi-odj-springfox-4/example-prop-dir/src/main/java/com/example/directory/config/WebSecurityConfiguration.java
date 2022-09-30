package com.example.directory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/",
                "/**",
                "/user/register",
                "/api",
                "/api/**",
                "/swagger-ui/**",
                "/v3/api-docs/**"
        );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
//                .and().apply(new JwtTokenConfigurer(tokenProvider))
                .antMatchers("/").permitAll()
                .antMatchers("/**").permitAll()
                .antMatchers("/user/register").permitAll()
                .antMatchers("/api").permitAll()
                .antMatchers("/api/**").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/v3/api-docs/**").permitAll()
                .anyRequest().permitAll();
//                .anyRequest().authenticated();
//                http.apply(new JwtTokenConfigurer(tokenProvider));
    }
}
