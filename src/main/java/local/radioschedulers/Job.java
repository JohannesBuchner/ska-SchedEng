package local.radioschedulers;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * a unit of execution. Jobs have no interdependencies.
 * 
 * @author Johannes Buchner
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class Job implements Comparable<Job> {
	/**
	 * (optional) id
	 */
	public String id;

	/**
	 * reference to the proposal this job belongs to
	 */
	@JsonIgnore
	public Proposal proposal;

	/**
	 * total hours needed
	 */
	public Double hours;

	/**
	 * start of LST time that can be used
	 */
	public Double lstmin;

	/**
	 * end of LST time that can be used
	 */
	public Double lstmax;

	public Job() {
	}

	public Job(Job j) {
		this.proposal = j.proposal;
		this.hours = j.hours;
		this.id = j.id;
		this.lstmax = j.lstmax;
		this.lstmin = j.lstmin;
	}

	@Override
	public String toString() {
		return "Job " + id + " [" + hours + " in " + lstmin + ".."
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
