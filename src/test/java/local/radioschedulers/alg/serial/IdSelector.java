package local.radioschedulers.alg.serial;

import java.util.Comparator;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;

/**
 * select in name/id in reverse order
 * 
 * @author Johannes Buchner
 */
public class IdSelector extends ShortestFirstSelector {

	@Override
	protected Comparator<JobCombination> generateComparator(
			Map<Job, Double> timeleft) {
		return new Comparator<JobCombination>() {

			@Override
			public int compare(JobCombination o1, JobCombination o2) {
				String p1 = o1.jobs.iterator().next().id;
				String p2 = o2.jobs.iterator().next().id;

				return p2.compareTo(p1);
			}
		};
	}

}
