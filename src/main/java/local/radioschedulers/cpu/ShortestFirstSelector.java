package local.radioschedulers.cpu;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;

public class ShortestFirstSelector extends JobSelector {

	@Override
	public void setTimeleft(HashMap<Job, Double> timeleft) {
		super.setTimeleft(timeleft);
		this.cmp = generateComparator(timeleft);
	}
	
	protected Comparator<JobCombination> cmp;

	protected Comparator<JobCombination> generateComparator(final HashMap<Job, Double> timeleft) {
		return new Comparator<JobCombination>() {
			@Override
			public int compare(JobCombination o1, JobCombination o2) {
				Double t1 = timeleft.get(o1);
				Double t2 = timeleft.get(o2);

				if (t1 == null)
					if (t2 == null)
						return 0;
					else
						return 1;
				else if (t2 == null)
					return -1;
				else
					return t1.compareTo(t2);
			}
		};
	};

	public Collection<JobCombination> select(Collection<JobCombination> list) {
		List<JobCombination> jobs = pruneDone(list);
		Collections.sort(jobs, cmp);
		return super.select(jobs);
	};
}
