package local.radioschedulers.preschedule.date;

import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class SolarDateRangeRequirements implements DateRequirements {
	private final LSTTime start;
	private final LSTTime end;
	private Integer nweekdays;
	private Integer daynumber;
	@JsonIgnore
	private Long rangesize;
	// how many minutes per day are difference between LST and normal date
	// calculations. i.e. 4 minutes per day
	public static final int conversion = (24 * 60 - (23 * 60 + 56));

	@JsonCreator
	public SolarDateRangeRequirements(@JsonProperty("start") LSTTime start,
			@JsonProperty("end") LSTTime end,
			@JsonProperty("nweekdays") Integer nweekdays,
			@JsonProperty("daynumber") Integer daynumber) {
		this.start = start;
		this.end = end;
		this.rangesize = (end.day - start.day);
		if (nweekdays != null && daynumber != null) {
			this.rangesize /= nweekdays;
		}
		this.daynumber = daynumber;
		this.nweekdays = nweekdays;
	}

	public LSTTime getStart() {
		return start;
	}

	public LSTTime getEnd() {
		return end;
	}

	@JsonProperty("nweekdays") 
	public Integer getNweekdays() {
		return nweekdays;
	}

	@JsonProperty("daynumber") 
	public Integer getDaynumber() {
		return daynumber;
	}

	@Override
	public Double requires(LSTTime t) {
		if (end.day >= 0 && t.day > end.day || t.day < start.day)
			return 0.;

		if (daynumber != null && nweekdays != null) {
			if (((int) (t.day * (1 + conversion / 24 / 60))) % nweekdays != daynumber) {
				// wrong day
				return 0.;
			}
		}

		long a = (start.minute + (t.day * conversion))
				% (Schedule.MINUTES_PER_DAY);
		long e = (end.minute + (t.day * conversion))
				% (Schedule.MINUTES_PER_DAY);
		if (a > e) {
			if (t.minute >= a || t.minute < e)
				return 1. / rangesize;
			else
				return 0.;
		} else {
			if (t.minute < e && t.minute >= a)
				return 1. / rangesize;
			else
				return 0.;
		}
	}

	@Override
	public Integer totalRequired() {
		return rangesize.intValue();
	}
}
