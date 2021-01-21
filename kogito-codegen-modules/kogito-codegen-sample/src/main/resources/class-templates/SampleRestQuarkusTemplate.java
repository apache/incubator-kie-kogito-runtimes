import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.inject.Inject;

@Path("/hello")
public class SampleResource {

    @Inject
    SampleEngine engine;

    @GET
    public String execute() {
        return engine.execute();
    }
}