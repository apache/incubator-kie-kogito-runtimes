package org.jbpm.process.core.timer;

import java.util.Properties;

public class CalendarFactory {

    public static CalendarBean createCalendarBean(Properties calendarConfiguration ) {
        return CalendarBean.create(calendarConfiguration);
    }
}
