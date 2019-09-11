package scouter2.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

public class DateUtil {

	public static final long MILLIS_PER_SECOND = 1000;
	public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
	public static final long MILLIS_PER_FIVE_MINUTE = 5 * 60 * MILLIS_PER_SECOND;
	public static final long MILLIS_PER_TEN_MINUTE = 10 * MILLIS_PER_MINUTE;
	public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
	public static final int SECONDS_PER_DAY = (int) (MILLIS_PER_DAY / 1000);

	public final static DateTimeHelper helper = DateTimeHelper.getDefault();

	public static String datetime(long time) {
		return helper.datetime(time);
	}

	public static String timestamp(long time) {
		return helper.timestamp(time);
	}

	public static String yyyymmdd(long time) {
		return helper.yyyymmdd(time);
	}

	public static String ymdhms(long time) {
		return helper.yyyymmdd(time) + helper.hhmmss(time);
	}

	public static String hhmmss(long time) {
		return helper.hhmmss(time);
	}

	public static String hhmmss2(long time) {
		return helper.hhmmss2(time);
	}

	public static String hhmm(long now) {
		return helper.hhmm(now);
	}

	public static String yyyymmdd() {
		return helper.yyyymmdd(System.currentTimeMillis());
	}

	public static String getLogTime(long time) {
		return helper.logtime(time);
	}

	public static long yyyymmdd(String date) {
		return helper.yyyymmdd(date);
	}

	public static long hhmm(String date) {
		return helper.hhmm(date);
	}

	public static long getTime(String date, String format) {
		if (format.equals("yyyyMMdd"))
			return helper.yyyymmdd(date);
		try {
			SimpleDateFormat sdf = parsers.get(format);
			if (sdf == null) {
				sdf = new SimpleDateFormat(format);
				parsers.put(format, sdf);
			}
			synchronized (sdf) {
				return sdf.parse(date).getTime();
			}
		} catch (ParseException e) {
			return 0;
		}
	}

	private static Hashtable<String, SimpleDateFormat> parsers = new Hashtable();

	public static String format(long stime, String format) {
		if (format.equals("yyyyMMdd"))
			return helper.yyyymmdd(stime);

		SimpleDateFormat sdf = parsers.get(format);
		if (sdf == null) {
			sdf = new SimpleDateFormat(format);
			parsers.put(format, sdf);
		}
		synchronized (sdf) {
			return sdf.format(new Date(stime));
		}
	}

	public static String format(long stime, String format, Locale locale) {
		if (format.equals("yyyyMMdd"))
			return helper.yyyymmdd(stime);

		SimpleDateFormat sdf = parsers.get(format + locale.getCountry());
		if (sdf == null) {
			sdf = new SimpleDateFormat(format, locale);
			parsers.put(format + locale.getCountry(), sdf);
		}
		synchronized (sdf) {
			return sdf.format(new Date(stime));
		}
	}

	public static long parse(String date, String format) {
		if (format.equals("yyyyMMdd"))
			return helper.yyyymmdd(date);

		SimpleDateFormat sdf = parsers.get(format);
		if (sdf == null) {
			sdf = new SimpleDateFormat(format);
			parsers.put(format, sdf);
		}
		synchronized (sdf) {
			try {
				return sdf.parse(date).getTime();
			} catch (ParseException e) {
				return helper.getBaseTime();
			}
		}
	}

	public static boolean isSameDay(Date date, Date date2) {
		return helper.getDayUnit(date.getTime()) == helper.getDayUnit(date2.getTime());
	}

	public static boolean isToday(long time) {
		return helper.getDayUnit(time) == helper.getDayUnit(System.currentTimeMillis());
	}

	public static long truncDay(long timestamp) {
		return reverseDayUnit(helper.getDayUnit(timestamp));
	}

	public static long truncHour(long timestamp) {
		return reverseHourUnit(helper.getHourUnit(timestamp));
	}

	public static int getHour(Date date) {
		return helper.getHour(date.getTime());
	}

	public static int getMin(Date date) {
		return helper.getMM(date.getTime());
	}

	public static int getHour(long time) {
		return helper.getHour(time);
	}

	public static int getMin(long time) {
		return helper.getMM(time);
	}

	public static String timestamp() {
		return helper.timestamp(System.currentTimeMillis());
	}

	public static String timestampFileName() {
		return helper.timestampFileName(System.currentTimeMillis());
	}

	public static int getDateMillis(long time) {
		return helper.getDayMillis(time);
	}

	public static long getTimeUnit(long time) {
		return helper.getTimeUnit(time);
	}

	public static long getDayUnit(long time) {
		return helper.getDayUnit(time);
	}

	public static long reverseDayUnit(long dateUnit) {
		return helper.reverseDayUnit(dateUnit);
	}

	public static long getHourUnit(long time) {
		return helper.getHourUnit(time);
	}

	public static int getSecUnit(long time) {
		return helper.getSecUnit(time);
	}

	public static long reverseHourUnit(long unit) {
		return helper.reverseHourUnit(unit);
	}

	public static long getMinuteUnit(long time) {
		return helper.getMinuteUnit(time);
	}

	public static long reverseMinuteUnit(long unit) {
		return helper.reverseMinuteUnit(unit);
	}

	public static long reverseSecUnit(long unit) {
		return helper.reverseSecUnit(unit);
	}

	public static long now() {
		return System.currentTimeMillis();
	}

	public static void main(String[] args) {
		System.out.println(getSecUnit(System.currentTimeMillis()));
	}
}