package com.tonpackage.xmlparser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XmlparserApplication {

	public static void main(String[] args) {
		SpringApplication.run(XmlparserApplication.class, args);
	}

}
