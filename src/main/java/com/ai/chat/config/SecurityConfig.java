package com.ai.chat.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ai.chat.models.AppUser;
import com.ai.chat.repository.UserRepository;

@Configuration
public class SecurityConfig {
	private final UserRepository user_repo;
	public SecurityConfig (UserRepository ur) {
		this.user_repo=ur;
	}
	
	@Bean
	public UserDetailsService userDetailsService() {
		return username -> {
			AppUser user = user_repo.findByUsername(username)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));
			
			return org.springframework.security.core.userdetails.User
					.withUsername(user.getUsername())
					.password(user.getPassword())
					.roles("USER")
					.build();
		};
				
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService());
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

	    CorsConfiguration configuration = new CorsConfiguration();

	    configuration.setAllowedOrigins(List.of(
	            "http://localhost:3000",
	            "https://aichat-9lc2-fw8m5790o-jiya-s-projects5.vercel.app/"
	    ));

	    configuration.setAllowedMethods(List.of(
	            "GET",
	            "POST",
	            "PUT",
	            "DELETE",
	            "OPTIONS"
	    ));

	    configuration.setAllowedHeaders(List.of("*"));

	    configuration.setAllowCredentials(true);

	    UrlBasedCorsConfigurationSource source =
	            new UrlBasedCorsConfigurationSource();

	    source.registerCorsConfiguration("/**", configuration);

	    return source;
	}
	
	@Bean
	   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	    .csrf(csrf -> csrf.disable())
	    .cors(cors -> {})
	    .authorizeHttpRequests(auth -> auth
	    .requestMatchers(
	    "/api/auth/register",
	    "/api/auth/login",
	    "/api/auth/me"
	    ).permitAll()
	    .anyRequest().authenticated()
	    )
	    .formLogin(form -> form
	    .loginProcessingUrl("/api/auth/login")
	    .successHandler((req, res, auth) -> res.setStatus(200))
	    .failureHandler((req, res, ex) -> res.setStatus(401))
	    )
	    .logout(logout -> logout
	    .logoutUrl("/api/auth/logout")
	    .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
	    );

	    return http.build();
	   }

}
