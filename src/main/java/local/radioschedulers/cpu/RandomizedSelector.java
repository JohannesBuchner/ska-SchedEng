package local.radioschedulers.cpu;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import local.radioschedulers.JobCombination;

/**
 * select jobs in random order
 * 
 * @author Johannes Buchner
 */
public class RandomizedSelector extends JobSelector {
	@Override
	public Collection<JobCombination> select(Collection<JobCombination> list) {
		List<JobCombination> jobs = pruneDone(list);
		if (jobs.size() > 1) {
			// lets avoid the idle job
			JobCombination idle = new JobCombination();
			boolean hasIdle = jobs.remove(idle);
			Collections.shuffle(jobs);
			if (hasIdle)
				jobs.add(idle);
		}
		return super.select(jobs);
	}

}
