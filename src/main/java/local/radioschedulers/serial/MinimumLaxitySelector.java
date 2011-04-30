package local.radioschedulers.serial;

import java.util.Comparator;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;

public class MinimumLaxitySelector extends PrioritizedSelector {

	@Override
	protected Comparator<JobCombination> generateComparator(
			Map<Job, Double> timeleft) {
		return new Comparator<JobCombination>() {

			@Override
			public int compare(JobCombination o1, JobCombination o2) {
				Integer n1 = getMinimumDeadline(o1);
				Integer n2 = getMinimumDeadline(o2);

				return n1.compareTo(n2);
			}

			private Integer getMinimumDeadline(JobCombination jc) {
				Integer n = null;
				for (Job j : jc.jobs) {
					if (n == null || possibles.get(j).size() < n)
						n = possibles.get(j).size();
				}
				return n;
			}
		};
	}
}
