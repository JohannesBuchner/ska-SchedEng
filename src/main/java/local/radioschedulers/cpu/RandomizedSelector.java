package local.radioschedulers.cpu;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import local.radioschedulers.JobCombination;

public class RandomizedSelector extends JobSelector {
	@Override
	public Collection<JobCombination> select(Collection<JobCombination> list) {
		List<JobCombination> jobs = pruneDone(list);
		Collections.shuffle(jobs);
		return super.select(jobs);
	}

}
