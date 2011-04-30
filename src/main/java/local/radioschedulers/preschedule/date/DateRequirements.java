package local.radioschedulers.preschedule.date;

import local.radioschedulers.LSTTime;

import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface DateRequirements {
	/**
	 * @param t
	 *            time
	 * @return 0 if this time is not wanted. >=0 if it is wanted. The sum of all
	 *         time requirements has to be 1. A preference can be expressed this
	 *         way.
	 */
	public Double requires(LSTTime t);

	/**
	 * @return total number of acceptable days
	 */
	public Integer totalRequired();
}
