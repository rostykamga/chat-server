package com.andy.chatserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http
			.authorizeRequests()
				.antMatchers("/", "/login", "/logout").permitAll()
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.permitAll()
				.and()
			.logout()
				.permitAll();		
	}
	
	@SuppressWarnings("deprecation")
	@Bean
	public UserDetailsService users() {
		UserBuilder userBuilder = User.withDefaultPasswordEncoder();
		
		UserDetails user1 = userBuilder
				.username("user1")
				.password("password1")
				.roles("USER", "ADMIN")
				.build();
		
		UserDetails user2 = userBuilder
				.username("user2")
				.password("password2")
				.roles("USER", "ADMIN")
				.build();
		
		return new InMemoryUserDetailsManager(user1, user2);
	}
}
