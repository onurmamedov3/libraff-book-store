package az.azal.libraff_book_store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LibraffBookStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraffBookStoreApplication.class, args);
	}

}
