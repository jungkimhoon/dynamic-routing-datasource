package com.example.dynamicroutingdatasource.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${project.context-path}")
	private String contextPath;

	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor() {
		return new MethodValidationPostProcessor();
	}

	private ServletRegistrationBean<? extends Servlet> createServletRegistrationBean(
		ApplicationContext context, String name, String... urlMappings
	) {
		final DispatcherServlet dispatcherServlet = new DispatcherServlet();
		dispatcherServlet.setApplicationContext(context);

		final ServletRegistrationBean<DispatcherServlet> servletRegistrationBean =
				new ServletRegistrationBean<>(dispatcherServlet, urlMappings);
		servletRegistrationBean.setName(name);
		return servletRegistrationBean;
	}

	@Bean
	public ServletRegistrationBean<? extends Servlet> oneContextPath(ApplicationContext context) {
		return createServletRegistrationBean(context, "firstOne", "/*");
	}

	@Bean
	public ServletRegistrationBean<? extends Servlet> anotherContextPath(ApplicationContext context) {
		return createServletRegistrationBean(context, "secondOne", contextPath + "/*");
	}
}
