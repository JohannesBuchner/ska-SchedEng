package local.radioschedulers;

import java.util.Collection;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A proposal is a collection of jobs
 *  
 * @author Johannes Buchner.
 */
public class Proposal implements Comparable<Proposal> {
	/**
	 * some id
	 */
	public String id;
	/**
	 * name telling about the project
	 */
	public String name;
	/**
	 * at which point became we aware of this proposal
	 */
	public Date start;
	@JsonIgnore
	public Collection<Job> jobs;

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
