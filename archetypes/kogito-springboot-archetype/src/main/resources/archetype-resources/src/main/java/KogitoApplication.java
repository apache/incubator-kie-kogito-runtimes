package ${package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.kie.kogito.**", "${package}.**", "${groupId}.**"})
public class KogitoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KogitoApplication.class, args);
    }

}