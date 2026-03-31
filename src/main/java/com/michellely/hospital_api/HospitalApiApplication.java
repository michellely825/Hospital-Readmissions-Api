// starts the server (spring boot)

package com.michellely.hospital_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;
import org.springframework.boot.web.servlet.MultipartConfigFactory;

import jakarta.servlet.MultipartConfigElement;
import javax.sql.DataSource;

@SpringBootApplication
public class HospitalApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HospitalApiApplication.class, args);
	}

	// Register the memory/connection monitoring filter for all requests
	@Bean
	public FilterRegistrationBean<MemoryMonitoringFilter> memoryMonitoringFilter(DataSource dataSource) {
		MemoryMonitoringFilter filter = new MemoryMonitoringFilter(dataSource);
		FilterRegistrationBean<MemoryMonitoringFilter> registration = new FilterRegistrationBean<>(filter);
		registration.addUrlPatterns("/*");
		registration.setOrder(1); // run first so timing covers the full request
		return registration;
	}

	// Limit multipart/form-data upload sizes to prevent large payloads from
	// consuming excessive heap memory
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.ofMegabytes(10));
		factory.setMaxRequestSize(DataSize.ofMegabytes(10));
		return factory.createMultipartConfig();
	}

}
