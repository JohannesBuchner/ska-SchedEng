package local.radioschedulers.cpu;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import local.radioschedulers.Job;

public class RandomizedSelector extends JobSelector {
	@Override
	public Collection<Job> select(Collection<Job> list) {
		List<Job> jobs = pruneDone(list);
		Collections.shuffle(jobs);
		return super.select(jobs);
	}

}
