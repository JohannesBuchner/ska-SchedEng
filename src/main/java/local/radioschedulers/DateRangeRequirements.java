package local.radioschedulers;

public class DateRangeRequirements implements DateRequirements {
	private final LSTTime end;
	private final LSTTime start;
	private final Long rangesize;

	public DateRangeRequirements(LSTTime start, LSTTime end) {
		this.start = start;
		this.end = end;
		this.rangesize = end.day - start.day;
	}

	@Override
	public Double requires(LSTTime t) {
		if (t.day <= end.day && t.day >= start.day) {
			return 1. / rangesize;
		} else {
			return 0.;
		}
	}

	@Override
	public Integer totalRequired() {
		return rangesize.intValue();
	}
}
