package yoga1290.coresystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ ImportCommons.class})
public class CommonsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommonsApplication.class, args);
	}

}
