package org.opensingular.server.studio.spring;

import org.apache.commons.lang3.StringUtils;
import org.opensingular.lib.support.spring.util.AutoScanDisabled;
import org.opensingular.server.commons.config.IServerContext;
import org.opensingular.server.p.commons.config.PSpringSecurityInitializer;
import org.opensingular.server.studio.user.SharedUserDetails;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collections;

public class StudioSpringSecurityInitializer extends PSpringSecurityInitializer {

    @Override
    protected <T extends WebSecurityConfigurerAdapter> Class<T> getSpringSecurityConfigClass(IServerContext context) {
        return (Class<T>) StudioSecurity.class;
    }

    @AutoScanDisabled
    @Configuration
    @EnableWebMvc
    @Order(106)
    public static class StudioSecurity extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .headers()
                    .frameOptions()
                    .sameOrigin()
                    .and()
                    .csrf().disable()
                    .authorizeRequests()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .formLogin().permitAll().loginPage("/login")
                    .and()
                    .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/");

        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(new AbstractUserDetailsAuthenticationProvider() {
                @Override
                protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

                }

                @Override
                protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
                    if (StringUtils.isNotBlank(username)) {
                        return new SharedUserDetails(username, Collections.emptyList(), username);
                    }
                    throw new BadCredentialsException("Não foi possivel autenticar o usuario informado");
                }
            });
        }

    }


}