package gatech.edu.FHIRController;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableAutoConfiguration
@ComponentScan("gatech.edu.common.FHIR.client")
@ComponentScan("gatech.edu.FHIRController.Controller")
@ComponentScan("gatech.edu.FHIRController.PHCRClient")
@SpringBootApplication
public class ApplicationTest extends SpringBootServletInitializer{

	private static final Logger log = LoggerFactory.getLogger(ApplicationTest.class);

	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ApplicationTest.class);
	}
	public static void main(String[] args) {
		SpringApplication.run(ApplicationTest.class);
	}

	
}
