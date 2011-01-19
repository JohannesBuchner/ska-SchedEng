package local.radioschedulers.cpu;

import java.util.Comparator;
import java.util.HashMap;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;

/**
 * select descending by priority
 * @author Johannes Buchner
 */
public class PrioritizedSelector extends ShortestFirstSelector {

	protected Comparator<JobCombination> generateComparator(
			HashMap<Job, Double> timeleft) {
		return new Comparator<JobCombination>() {

			@Override
			public int compare(JobCombination o1, JobCombination o2) {
				Double p1 = o1.calculatePriority();
				Double p2 = o2.calculatePriority();

				return p2.compareTo(p1);
			}
		};
	}

}
