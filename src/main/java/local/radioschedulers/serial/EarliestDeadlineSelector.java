package local.radioschedulers.serial;

import java.util.Comparator;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;

/**
 * Select task with least number of possible timeslots left. If same, maximize
 * priority
 * 
 * @author Johannes Buchner
 */
public class EarliestDeadlineSelector extends PrioritizedSelector {

	@Override
	protected Comparator<JobCombination> generateComparator(
			Map<Job, Double> timeleft) {
		return new Comparator<JobCombination>() {

			@Override
			public int compare(JobCombination o1, JobCombination o2) {
				Integer n1 = getEarliestDeadline(o1);
				Integer n2 = getEarliestDeadline(o2);

				int r = n1.compareTo(n2);
				if (r != 0)
					return r;
				return Double.compare(o2.calculatePriority(), o1
						.calculatePriority());
			}

			private Integer getEarliestDeadline(JobCombination jc) {
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
