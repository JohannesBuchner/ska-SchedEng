package local.radioschedulers.cpu;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;

/**
 * Jobs are continued if possible. If possible, additional jobs are selected
 * proportional to their priority.
 * 
 * @author Johannes Buchner
 */
public class KeepingPrioritizedSelector extends PrioritizedSelector {

	private static JobCombination lastJobCombination;

	protected Comparator<JobCombination> generateComparator(
			HashMap<Job, Double> timeleft) {
		return new Comparator<JobCombination>() {

			@Override
			public int compare(JobCombination o1, JobCombination o2) {
				// prefer the ones that were in the last
				if (lastJobCombination != null) {
					boolean continued1 = lastJobCombination.jobs.contains(o1);
					boolean continued2 = lastJobCombination.jobs.contains(o2);
					if (continued1 && !continued2)
						return -1;
					else if (!continued1 && continued2)
						return 1;
				}

				// if that's the same, prefer higher priority
				// this already incorporates that more (adding additional jobs)
				// is better.
				Double p1 = o1.calculatePriority();
				Double p2 = o2.calculatePriority();
				return p1.compareTo(p2);
			}
		};
	}

	@Override
	public Collection<JobCombination> select(Collection<JobCombination> list) {
		Collection<JobCombination> l = super.select(list);
		Iterator<JobCombination> it = l.iterator();
		if (it.hasNext()) {
			lastJobCombination = it.next();
		}
		return l;
	}
}
