package scouter2.common.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class DateTimeHelper {
	private static class Day {
		public Day(String date, String day, long time) {
			this.date = date;
			this.wday = day;
			this.time = time;
		}

		public final String date;
		public final String wday;
		public final long time;
	}

	public static final int MILLIS_PER_SECOND = 1000;
	public static final int MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
	public static final int MILLIS_PER_FIVE_MINUTE = 5 * 60 * MILLIS_PER_SECOND;
	public static final int MILLIS_PER_TEN_MINUTE = 10 * MILLIS_PER_MINUTE;
	public static final int MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
	public static final int MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

	private DateTimeHelper(TimeZone zone) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			sdf.setTimeZone(zone);
			BASE_TIME = sdf.parse("20000101").getTime();
			open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static String wday[] = { "Mon", "Tue", "Wed", "Thr", "Fri", "Sat", "Sun" };
	static int mdayLen[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	private long BASE_TIME;
	private Day[][][] table = new Day[100][][];
	private Day[] dateTable = new Day[40000];

	public long getBaseTime() {
		return BASE_TIME;
	}

	private void open() throws Exception {
		long mtime = BASE_TIME;
		int seq = 0;
		int wdayIdx = 5;// 20000101:Saturday
		for (int year = 0; year < 100; year++) {
			boolean isYun = isYun(year);
			table[year] = new Day[12][];
			for (int mm = 0; mm < 12; mm++) {
				int monLen = mdayLen[mm];
				if (mm == 1 && isYun) {
					monLen++;
				}
				table[year][mm] = new Day[monLen];
				for (int dd = 0; dd < monLen; dd++) {
					String yyyyMMdd = String.format("%d%02d%02d", (year + 2000), mm + 1, dd + 1);

					dateTable[seq] = new Day(yyyyMMdd, wday[wdayIdx], mtime);
					table[year][mm][dd] = dateTable[seq];

					wdayIdx = wdayIdx == 6 ? 0 : wdayIdx + 1;
					seq++;
					mtime += MILLIS_PER_DAY;
				}
			}
		}

	}

	static boolean isYun(int year) {
		if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
			return true;
		else
			return false;
	}

	public long yyyymmdd(String yyyyMMdd) {
		int year = Integer.parseInt(yyyyMMdd.substring(0, 4));
		int mm = Integer.parseInt(yyyyMMdd.substring(4, 6));
		int dd = Integer.parseInt(yyyyMMdd.substring(6, 8));
		if (year >= 2100)
			return table[99][11][30].time + MILLIS_PER_DAY;
		if (year < 2000)
			return BASE_TIME;
		return table[year - 2000][mm - 1][dd - 1].time;
	}

	public long hhmm(String date) {
		if (date == null)
			return 0;
		int h = Integer.parseInt(date.substring(0, 2));
		int m = Integer.parseInt(date.substring(2, 4));
		return h * MILLIS_PER_HOUR + m * MILLIS_PER_MINUTE;
	}

	public String getWeekDay(String yyyyMMdd) {
		int year = Integer.parseInt(yyyyMMdd.substring(0, 4));
		int mm = Integer.parseInt(yyyyMMdd.substring(4, 6));
		int dd = Integer.parseInt(yyyyMMdd.substring(6, 8));
		if (year >= 2100)
			return table[99][11][30].wday;
		if (year < 2000)
			return table[0][0][0].wday;
		return table[year - 2000][mm - 1][dd - 1].wday;
	}

	public String yyyymmdd(long time) {
		int idx = (int) ((time - BASE_TIME) / MILLIS_PER_DAY);
		if (idx < 0)
			idx = 0;
		return dateTable[idx].date;
	}

	public String datetime(long time) {
		if (time < BASE_TIME)
			return "20000101 00:00:00";
		int idx = (int) ((time - BASE_TIME) / MILLIS_PER_DAY);
		long dtime = (time - BASE_TIME) % MILLIS_PER_DAY;
		int hh = (int) (dtime / MILLIS_PER_HOUR);
		dtime = (int) (dtime % MILLIS_PER_HOUR);
		int mm = (int) (dtime / MILLIS_PER_MINUTE);
		dtime = (int) (dtime % MILLIS_PER_MINUTE);
		int ss = (int) (dtime / MILLIS_PER_SECOND);

		StringBuffer sb = new StringBuffer();
		sb.append(dateTable[idx].date).append(" ");
		sb.append(mk2(hh)).append(":");
		sb.append(mk2(mm)).append(":");
		sb.append(mk2(ss));
		return sb.toString();
	}

	public String timestamp(long time) {
		if (time < BASE_TIME)
			return "20000101 00:00:00.000";
		int idx = (int) ((time - BASE_TIME) / MILLIS_PER_DAY);
		long dtime = (time - BASE_TIME) % MILLIS_PER_DAY;
		int hh = (int) (dtime / MILLIS_PER_HOUR);
		dtime = (int) (dtime % MILLIS_PER_HOUR);
		int mm = (int) (dtime / MILLIS_PER_MINUTE);
		dtime = (int) (dtime % MILLIS_PER_MINUTE);
		int ss = (int) (dtime / MILLIS_PER_SECOND);
		int sss = (int) (dtime % 1000);

		StringBuffer sb = new StringBuffer();
		sb.append(dateTable[idx].date).append(" ");
		sb.append(mk2(hh)).append(":");
		sb.append(mk2(mm)).append(":");
		sb.append(mk2(ss));
		sb.append(".").append(mk3(sss));
		return sb.toString();

	}

	private String mk2(int n) {
		switch (n) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			return "0" + n;
		default:
			return Integer.toString(n);
		}
	}

	private String mk3(int n) {
		switch (n) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			return "00" + n;
		default:
			return n < 100 ? "0" + n : Integer.toString(n);
		}
	}

	public String timestampFileName(long time) {
		if (time < BASE_TIME)
			return "20000101_000000_000";
		int idx = (int) ((time - BASE_TIME) / MILLIS_PER_DAY);
		long dtime = (time - BASE_TIME) % MILLIS_PER_DAY;
		int hh = (int) (dtime / MILLIS_PER_HOUR);
		dtime = (int) (dtime % MILLIS_PER_HOUR);
		int mm = (int) (dtime / MILLIS_PER_MINUTE);
		dtime = (int) (dtime % MILLIS_PER_MINUTE);
		int ss = (int) (dtime / MILLIS_PER_SECOND);
		int sss = (int) (dtime % 1000);
		StringBuffer sb = new StringBuffer();
		sb.append(dateTable[idx].date).append("_");
		sb.append(mk2(hh));
		sb.append(mk2(mm));
		sb.append(mk2(ss));
		sb.append("_").append(mk3(sss));
		return sb.toString();

	}

	public String ymdhms(long time) {
		if (time < BASE_TIME)
			return "20000101000000";
		int idx = (int) ((time - BASE_TIME) / MILLIS_PER_DAY);
		long dtime = (time - BASE_TIME) % MILLIS_PER_DAY;
		int hh = (int) (dtime / MILLIS_PER_HOUR);
		dtime = (int) (dtime % MILLIS_PER_HOUR);
		int mm = (int) (dtime / MILLIS_PER_MINUTE);
		dtime = (int) (dtime % MILLIS_PER_MINUTE);
		int ss = (int) (dtime / MILLIS_PER_SECOND);
		StringBuffer sb = new StringBuffer();
		sb.append(dateTable[idx].date);
		sb.append(mk2(hh));
		sb.append(mk2(mm));
		sb.append(mk2(ss));
		return sb.toString();
	}

	public String logtime(long time) {
		if (time < BASE_TIME)
			return "00:00:00.000";
		long dtime = (time - BASE_TIME) % MILLIS_PER_DAY;
		int hh = (int) (dtime / MILLIS_PER_HOUR);
		dtime = (int) (dtime % MILLIS_PER_HOUR);
		int mm = (int) (dtime / MILLIS_PER_MINUTE);
		dtime = (int) (dtime % MILLIS_PER_MINUTE);
		int ss = (int) (dtime / MILLIS_PER_SECOND);
		int sss = (int) (dtime % 1000);
		StringBuffer sb = new StringBuffer();
		sb.append(mk2(hh)).append(":");
		sb.append(mk2(mm)).append(":");
		sb.append(mk2(ss));
		sb.append(".").append(mk3(sss));
		return sb.toString();
	}

	public String hhmmss(long time) {
		if (time < BASE_TIME)
			return "000000";
		long dtime = (time - BASE_TIME) % MILLIS_PER_DAY;
		int hh = (int) (dtime / MILLIS_PER_HOUR);
		dtime = (int) (dtime % MILLIS_PER_HOUR);
		int mm = (int) (dtime / MILLIS_PER_MINUTE);
		dtime = (int) (dtime % MILLIS_PER_MINUTE);
		int ss = (int) (dtime / MILLIS_PER_SECOND);
		return String.format("%02d%02d%02d", hh, mm, ss);
	}

	public String hhmmss2(long time) {
		if (time < BASE_TIME)
			return "000000";
		long dtime = (time - BASE_TIME) % MILLIS_PER_DAY;
		int hh = (int) (dtime / MILLIS_PER_HOUR);
		dtime = (int) (dtime % MILLIS_PER_HOUR);
		int mm = (int) (dtime / MILLIS_PER_MINUTE);
		dtime = (int) (dtime % MILLIS_PER_MINUTE);
		int ss = (int) (dtime / MILLIS_PER_SECOND);
		return String.format("%02d:%02d:%02d", hh, mm, ss);
	}

	public String hhmm(long time) {
		if (time < BASE_TIME)
			return "0000";
		long dtime = (time - BASE_TIME) % MILLIS_PER_DAY;
		int hh = (int) (dtime / MILLIS_PER_HOUR);
		dtime = (int) (dtime % MILLIS_PER_HOUR);
		int mm = (int) (dtime / MILLIS_PER_MINUTE);

		return String.format("%02d%02d", hh, mm);
	}

	public int getDayMillis(long time) {
		if (time < BASE_TIME)
			return 0;
		long dtime = (time - BASE_TIME) % MILLIS_PER_DAY;
		return (int) dtime;
	}

	public int getHour(long time) {
		return getDayMillis(time) / MILLIS_PER_HOUR;
	}

	public int getMM(long time) {
		int dtime = getDayMillis(time) % MILLIS_PER_HOUR;
		return (dtime / MILLIS_PER_MINUTE);
	}

	public long getTimeUnit(long time) {
		return (time - BASE_TIME);
	}

	public long getDayUnit(long time) {
		return (time - BASE_TIME) / MILLIS_PER_DAY;
	}

	public long getMinuteUnit(long time) {
		return (time - BASE_TIME) / MILLIS_PER_MINUTE;
	}

	public long getHourUnit(long time) {
		return (time - BASE_TIME) / MILLIS_PER_HOUR;
	}

	public int getSecUnit(long time) {
		return (int) ((time - BASE_TIME) / MILLIS_PER_SECOND);
	}

	public long reverseDayUnit(long dayUnit) {
		return dayUnit * MILLIS_PER_DAY + BASE_TIME;
	}

	public long reverseHourUnit(long unit) {
		return (unit * MILLIS_PER_HOUR) + BASE_TIME;
	}

	public long reverseMinuteUnit(long unit) {
		return (unit * MILLIS_PER_MINUTE) + BASE_TIME;
	}

	public long reverseSecUnit(long unit) {
		return (unit * MILLIS_PER_SECOND) + BASE_TIME;
	}

	public long getDateUnit() {
		return getDayUnit(System.currentTimeMillis());
	}

	private static Map<String, DateTimeHelper> _table = new HashMap<String, DateTimeHelper>();

	public static DateTimeHelper getDefault() {
		return getDateTimeHelper(TimeZone.getDefault());
	}

	public static synchronized DateTimeHelper getDateTimeHelper(TimeZone timezone) {
		DateTimeHelper helper = _table.get(timezone.getID());
		if (helper == null) {
			helper = new DateTimeHelper(timezone);
			;
			_table.put(timezone.getID(), helper);
		}
		return helper;
	}
}
