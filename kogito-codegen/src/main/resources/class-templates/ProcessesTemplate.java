package $Package$;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.kie.kogito.process.ProcessData;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.Processes;

public class ApplicationProcesses implements Processes {

    Object processes;
        
    private Map<String, Process<? extends ProcessData>> mappedProcesses = new HashMap<>();

    @javax.annotation.PostConstruct
    public void setup() {
        
        for (Process<? extends ProcessData> process : processes) {
            mappedProcesses.put(process.id(), process);
        }
    }
    
    public Process<? extends ProcessData> processById(String processId) {
        return mappedProcesses.get(processId);
    }
    
    public Collection<String> processIds() {
        return mappedProcesses.keySet();
    }
}
