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
		if (jobs.size() == 0)
			return 0;

		double expprio = 0;
		for (Job j : jobs) {
			expprio = expprio + Math.exp(j.proposal.priority);
		}
		return Math.log(expprio);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobs == null) ? 0 : jobs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobCombination other = (JobCombination) obj;
		if (jobs == null) {
			if (other.jobs != null)
				return false;
		} else if (!jobs.equals(other.jobs))
			return false;
		return true;
	}

}
