package local.radioschedulers.deciders;

import java.util.Collection;
import java.util.List;

import local.radioschedulers.JobCombination;

/**
 * Simply selects the first available JobCombination
 * @author Johannes Buchner
 */
public class FirstSelector extends JobSelector {
	@Override
	public Collection<JobCombination> select(Collection<JobCombination> list) {
		List<JobCombination> jobs = pruneDone(list);
		for (int i = 1; i < jobs.size(); i++) {
			jobs.remove(i);
		}
		return super.select(jobs);
	}

}
