
package one.microstream.storage.restclient.app.types;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.vaadin.flow.spring.annotation.EnableVaadin;


@SpringBootApplication
@EnableVaadin("one.microstream.storage.restclient.app.ui")
public class Application extends SpringBootServletInitializer
{
	public static void main(
		final String[] args
	)
	{
		SpringApplication.run(Application.class, args);
	}
	
}
