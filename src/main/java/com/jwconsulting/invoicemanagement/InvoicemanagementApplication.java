package com.jwconsulting.invoicemanagement;

import com.jwconsulting.invoicemanagement.exception.ApiException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Base64;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class InvoicemanagementApplication {

	private static final int STRENGTH = 12;

	public static void main(String[] args) {
		SpringApplication.run(InvoicemanagementApplication.class, args);
		/**String secret = "";
		String base64Encoded = Base64.getEncoder().encodeToString(secret.getBytes());
		System.out.println(base64Encoded);**/
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder () {
		return new BCryptPasswordEncoder(STRENGTH);
	}
}
