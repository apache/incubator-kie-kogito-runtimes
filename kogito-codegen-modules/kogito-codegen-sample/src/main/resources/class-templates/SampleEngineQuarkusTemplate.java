import org.kie.kogito.KogitoEngine;

@javax.enterprise.context.ApplicationScoped()
public class SampleEngine implements KogitoEngine {

    public String execute() {
        return $value$;
    }

}