package ${package};

import javax.enterprise.context.ApplicationScoped;

/**
 * HelloWorldService
 */
@ApplicationScoped
public class HelloWorldService {

    public void helloWorld() {   
        System.out.println("Hello World");
    }
    
}