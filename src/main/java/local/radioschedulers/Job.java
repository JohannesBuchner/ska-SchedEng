package local.radioschedulers;


/**
 * a unit of execution. Jobs have no interdependencies.
 * 
 * @author Johannes Buchner
 */
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

	public Job() {
	}

	public Job(Job j) {
		this.proposal = j.proposal;
		this.hours = j.hours;
		this.dec = j.dec;
		this.ra = j.ra;
		this.lstmax = j.lstmax;
		this.lstmin = j.lstmin;
	}

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

	public boolean isAvailable(LSTTime t) {
		return true && hasSufficientResources();
	}

	public boolean hasSufficientResources() {
		return true;
	}

}
