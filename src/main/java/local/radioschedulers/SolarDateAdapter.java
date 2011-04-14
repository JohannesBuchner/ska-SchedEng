package local.radioschedulers;

import org.codehaus.jackson.annotate.JsonCreator;

public class SolarDateAdapter implements DateRequirements {
	// how many minutes per day are difference between LST and normal date
	// calculations. i.e. 4 minutes per day
	public static int conversion = (24 * 60 - (23 * 60 + 56));
	private DateRequirements parent;

	@JsonCreator
	public SolarDateAdapter(DateRequirements parent) {
		this.parent = parent;
	}

	@Override
	public Double requires(LSTTime t) {
		LSTTime tsolar = new LSTTime(t.day, t.minute);
		tsolar.minute = (t.minute - (t.day * conversion))
				% (Schedule.LST_SLOTS_PER_DAY * Schedule.LST_SLOTS_MINUTES);
		while (tsolar.minute < 0) {
			tsolar.minute += Schedule.LST_SLOTS_PER_DAY
					* Schedule.LST_SLOTS_MINUTES;
			tsolar.day -= 1;
		}
		return parent.requires(tsolar);
	}

	@Override
	public Integer totalRequired() {
		return parent.totalRequired();
	}
}
