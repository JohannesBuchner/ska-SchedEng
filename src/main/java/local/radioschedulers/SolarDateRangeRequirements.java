package local.radioschedulers;

public class SolarDateRangeRequirements implements DateRequirements {
	private final LSTTime end;
	private final LSTTime start;
	private final Long rangesize;
	// how many minutes per day are difference between LST and normal date
	// calculations
	public final static int conversion = (24 * 60 - (23 * 60 + 56));

	public SolarDateRangeRequirements(LSTTime start, LSTTime end) {
		this.start = start;
		this.end = end;
		this.rangesize = end.day - start.day;
	}

	@Override
	public Double requires(LSTTime t) {
		if (t.day <= end.day && t.day >= start.day) {
			long a = (start.minute + (t.day * conversion)
					/ Schedule.LST_SLOTS_MINUTES)
					% Schedule.LST_SLOTS_PER_DAY;
			long e = (end.minute + (t.day * conversion)
					/ Schedule.LST_SLOTS_MINUTES)
					% Schedule.LST_SLOTS_PER_DAY;
			if (a > e) {
				long tmp = e;
				e = a;
				a = tmp;
			}
			if (t.minute < e && t.minute >= a)
				return 1. / rangesize;
		}
		return 0.;
	}

	@Override
	public Integer totalRequired() {
		return rangesize.intValue();
	}
}
