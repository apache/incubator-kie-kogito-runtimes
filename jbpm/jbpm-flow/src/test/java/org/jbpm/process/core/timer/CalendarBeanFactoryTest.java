package org.jbpm.process.core.timer;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalendarBeanFactoryTest {

    @Test
    void testCreateCalendarBean() {
        // This test relies on src/test/resources/calendar.properties.
        // Checked values comes from it
        CalendarBean calendarBean = CalendarBeanFactory.createCalendarBean();
        assertNotNull(calendarBean);
        assertEquals(10, calendarBean.getStartHour());
        assertEquals(16, calendarBean.getEndHour());
        assertEquals(6, calendarBean.getHoursInDay());
        assertEquals(5, calendarBean.getDaysPerWeek());
        assertEquals(List.of(Calendar.SATURDAY, Calendar.SUNDAY), calendarBean.getWeekendDays());
        assertEquals(List.of(), calendarBean.getHolidays());
        assertEquals(TimeZone.getDefault().getID(), calendarBean.getTimezone());
    }
}
