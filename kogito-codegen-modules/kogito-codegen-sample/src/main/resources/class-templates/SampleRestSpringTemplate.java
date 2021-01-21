import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/hello")
public class SampleResource {

    @Autowired
    SampleEngine engine;

    @GetMapping
    public String execute() {
        return engine.execute();
    }
}