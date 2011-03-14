package local.radioschedulers;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class SolarDateRangeRequirements implements DateRequirements {
	private final LSTTime start;
	private final LSTTime end;
	@JsonIgnore
	private Long rangesize;
	// how many minutes per day are difference between LST and normal date
	// calculations
	public static int conversion = (24 * 60 - (23 * 60 + 56));

	@JsonCreator
	public SolarDateRangeRequirements(@JsonProperty("start") LSTTime start,
			@JsonProperty("end") LSTTime end) {
		this.start = start;
		this.end = end;
		this.rangesize = end.day - start.day;
	}

	public LSTTime getStart() {
		return start;
	}

	public LSTTime getEnd() {
		return end;
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
