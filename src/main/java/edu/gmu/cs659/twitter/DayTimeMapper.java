package edu.gmu.cs659.twitter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import twitter4j.GeoLocation;

public class DayTimeMapper {

	public static final DayTimeMapper SINGLETON = new DayTimeMapper();
	
	public static DayTimeMapper getDayTimeMapper() {
		return SINGLETON;
	}

	private DayTimeMapper() {
		
	}

	public DayTimePeriod getPeriod(long time, GeoLocation location) {
		return getPeriod(new Date(time), location);
	}

	public DayTimePeriod getPeriod(Date date, GeoLocation location) {
		//TODO: Use service to look up time zone from location
		return getPeriod(date, TimeZone.getDefault());
	}

	public DayTimePeriod getPeriod(Date date, String timezone) {
		if(timezone == null) {
			return getPeriod(date, TimeZone.getDefault());			
		} else {
			return getPeriod(date, TimeZone.getTimeZone(timezone));			
		}
	}

	public DayTimePeriod getPeriod(Date date, TimeZone timezone) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);

		cal.setTimeZone(timezone);
		
		switch(cal.get(Calendar.HOUR_OF_DAY)) {
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			return DayTimePeriod.LATENIGHT;
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
			return DayTimePeriod.MORNING;
		case 11:
		case 12:
		case 13:
		case 14:
				return DayTimePeriod.LATE_MORNING_AFTERNOON;
		case 15:
		case 16:
		case 17:
		case 18:
		case 19:
			return DayTimePeriod.EVENING;
		case 20:
		case 21:
		case 22:
		case 23:
		case 0:
		default:
			return DayTimePeriod.NIGHT;
		}
	}	
}
