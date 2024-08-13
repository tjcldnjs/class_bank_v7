package com.tenco.bank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.tenco.bank.handler.AuthInterceptor;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor // 그냥 적어주는거 ?
public class WebMvcConfig implements WebMvcConfigurer{
	
	@Autowired // DI
	private final AuthInterceptor authInterceptor;
	// @RequiredArgsConstructor <-- 생성자 대신에 사용가능 !!
	
	// 우리가 만들어 놓은 AuthInterceptor 를 등록해야 함.
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
			.addPathPatterns("/account/**")
			.addPathPatterns("/save/**")
			.addPathPatterns("/list/**")
			.addPathPatterns("/withdrawal/**")
			.addPathPatterns("/deposit/**")
			.addPathPatterns("/transfer/**")
			.addPathPatterns("/detail/**")
			.addPathPatterns("/auth/**");
	}
	
	
	@Bean // IoC 대상 (싱글톤 처리)
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
}
