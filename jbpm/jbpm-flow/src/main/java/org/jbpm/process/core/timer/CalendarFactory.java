package org.jbpm.process.core.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Properties;

import static org.jbpm.process.core.constants.CalendarConstants.BUSINESS_CALENDAR_PATH;

public class CalendarFactory {

    private static final Logger logger = LoggerFactory.getLogger(CalendarFactory.class);

    public static CalendarBean createCalendarBean() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(BUSINESS_CALENDAR_PATH);
        if (Objects.nonNull(resource)) {
            Properties calendarConfiguration = new Properties();
            try (InputStream is = resource.openStream()) {
                calendarConfiguration.load(is);
                return new CalendarBean(calendarConfiguration);
            } catch (IOException e) {
                String errorMessage = "Error while loading properties for business calendar";
                logger.error(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        } else {
            String errorMessage = String.format("Missing %s", BUSINESS_CALENDAR_PATH);
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }
     public static Calendar getCalendar() {
        return new GregorianCalendar();
     }
}
