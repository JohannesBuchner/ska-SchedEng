package local.radioschedulers.alg.serial;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;

/**
 * Jobs are continued if possible. If possible, additional jobs are selected
 * proportional to their priority.
 * 
 * @author Johannes Buchner
 */
public class KeepingPrioritizedSelector extends PrioritizedSelector {
	private JobCombination lastJobCombination;

	@Override
	protected Comparator<JobCombination> generateComparator(
			Map<Job, Double> timeleft) {
		return new Comparator<JobCombination>() {

			@Override
			public int compare(JobCombination o1, JobCombination o2) {
				// prefer the ones that were in the last
				if (lastJobCombination != null) {
					int v = compareContinuedCount(o1, o2);
					if (v != 0)
						return v;
				}

				// if that's the same, prefer higher priority
				// this already incorporates that more (adding additional jobs)
				// is better.
				Double p1 = o1.calculatePriority();
				Double p2 = o2.calculatePriority();
				return p2.compareTo(p1);
			}

			private int compareContinuedCount(JobCombination o1,
					JobCombination o2) {
				Integer continued1 = countContinuedJobs(lastJobCombination, o1);
				Integer continued2 = countContinuedJobs(lastJobCombination, o2);
				int v = continued2.compareTo(continued1);
				return v;
			}

			private Integer countContinuedJobs(JobCombination lastjc,
					JobCombination jc) {
				int count = 0;
				for (Job j : lastjc.jobs) {
					if (jc.jobs.contains(j))
						count++;
				}
				return count;
			}
		};
	}

	@Override
	protected JobCombination doSelect(List<JobCombination> jobs) {
		lastJobCombination = super.doSelect(jobs);
		return lastJobCombination;
	}
}
