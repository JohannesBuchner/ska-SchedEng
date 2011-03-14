package local.radioschedulers;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.NoClass;

/**
 * A noop implementation of DateRequirements
 *  
 * @author Johannes Buchner
 */
@JsonSerialize(as = NoClass.class)
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
