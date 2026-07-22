package likelion14th.lte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LteApplication {

	public static void main(String[] args) {
		SpringApplication.run(LteApplication.class, args);
	}

}
