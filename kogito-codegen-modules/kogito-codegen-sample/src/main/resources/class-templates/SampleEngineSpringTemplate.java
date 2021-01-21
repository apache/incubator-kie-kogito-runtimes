import org.kie.kogito.KogitoEngine;

@org.springframework.stereotype.Component
@org.springframework.web.context.annotation.ApplicationScope
public class SampleEngine implements KogitoEngine {

    public String execute() {
        return $value$;
    }
}