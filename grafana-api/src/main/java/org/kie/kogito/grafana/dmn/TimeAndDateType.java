package org.kie.kogito.grafana.dmn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.kie.kogito.grafana.model.panel.common.YAxis;

public class TimeAndDateType extends AbstractDmnType {

    public TimeAndDateType() {
        super(LocalDateTime.class, "date and time");
        addFunctions(new TreeMap<>());
        List<YAxis> yaxes = new ArrayList<>();
        yaxes.add(new YAxis("dateTimeAsIso", true));
        yaxes.add(new YAxis("ms", false));
        setYAxes(yaxes);
    }
}
