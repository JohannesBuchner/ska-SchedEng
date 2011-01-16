package local.radioschedulers;

public class NoDateRequirements implements DateRequirements {
	public static final int NDAYS = 365;

	@Override
	public Double requires(LSTTime t) {
		return 1. / NDAYS;
	}

	@Override
	public Integer totalRequired() {
		return NDAYS;
	}

}
