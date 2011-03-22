package local.radioschedulers;

import java.util.HashSet;
import java.util.Set;

/**
 * A compatible combination of jobs, i.e. jobs that can be executed in parallel
 * 
 * @author Johannes Buchner
 * 
 */
public class JobCombination {
	public Set<Job> jobs = new HashSet<Job>();

	public double calculatePriority() {
		if (jobs.size() == 0) {
			throw new IllegalStateException("do not create an idle job");
		}

		double expprio = 0;
		for (Job j : jobs) {
			expprio = expprio + Math.exp(j.proposal.priority);
		}
		return Math.log(expprio);
	}

	@Override
	public String toString() {
		return jobs.toString();
	}

}
