package local.radioschedulers;

/**
 * A noop implementation of DateRequirements
 *  
 * @author Johannes Buchner
 */
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
