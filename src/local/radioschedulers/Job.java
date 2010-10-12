package local.radioschedulers;
public class Job implements Comparable<Job> {
	public Proposal proposal;
	/**
	 * total hours needed
	 */
	public Long hours;
	/**
	 * declination in degrees
	 */
	public Double dec;
	/**
	 * rect ascention in hours
	 */
	public Double ra;
	/**
	 * minimum LST range
	 */
	public Double lstmin;
	public Double lstmax;
	
	@Override
	public String toString() {
		return "Job " + hours + " (" + ra + "," + dec + ")=[" + lstmin + ".."
				+ lstmax + "] of " + proposal;
	}

	@Override
	public int compareTo(Job o) {
		int v = proposal.compareTo(o.proposal);
		if (v == 0) {
			return hours.compareTo(o.hours);
		} else
			return v;

	}

}
