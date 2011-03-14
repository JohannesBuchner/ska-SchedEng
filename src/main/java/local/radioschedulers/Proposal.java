package local.radioschedulers;

import java.util.Collection;
import java.util.Date;

/**
 * A proposal is a collection of jobs
 * 
 * @author Johannes Buchner.
 */
public class Proposal implements Comparable<Proposal> {
	/**
	 * (optional) id
	 */
	public String id;

	/**
	 * (optional) name telling about the project
	 */
	public String name;

	/**
	 * (optional) at which point became we aware of this proposal
	 */
	public Date start;

	/**
	 * Jobs this Proposal is composed of
	 */
	public Collection<JobWithResources> jobs;

	/**
	 * how much is completing the project worth
	 **/
	public double priority;

	/**
	 * We are committed to this project. It has to be done (highest priority).
	 **/
	public boolean mustcomplete;

	@Override
	public String toString() {
		return "Proposal " + name + "(" + id + ", prio " + priority + ") from "
				+ start + " jobs: " + jobs.size();
	}

	@Override
	public int compareTo(Proposal o) {
		return id.compareTo(o.id);
	}
}
