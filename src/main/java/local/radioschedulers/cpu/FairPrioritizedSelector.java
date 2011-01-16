package local.radioschedulers.cpu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;

/**
 * Jobs are selected proportional to their priority.
 * 
 * Proposals with mustcomplete are always done first.
 * 
 * @author Johannes Buchner
 */
public class FairPrioritizedSelector extends JobSelector {

	private Random r = new Random();

	protected static Comparator<Job> generateComparator() {
		return new Comparator<Job>() {

			@Override
			public int compare(Job o1, Job o2) {
				Double p1 = o1.proposal.priority;
				Double p2 = o2.proposal.priority;

				if (p1 == null)
					if (p2 == null)
						return 0;
					else
						return 1;
				else if (p2 == null)
					return -1;
				else
					return p1.compareTo(p2);
			}
		};
	}

	@Override
	public Collection<JobCombination> select(Collection<JobCombination> list) {
		List<JobCombination> jobs = pruneDone(list);
		if (jobs.isEmpty())
			return jobs;

		List<JobCombination> selected = new ArrayList<JobCombination>();
		while (!jobs.isEmpty()) {
			Double priototal = 0.;
			for (JobCombination jc : jobs) {
				for (Job j : jc.jobs) {
					if (j.proposal.mustcomplete) {
						selected.add(jc);
						jobs.remove(jc);
						break;
					} else {
						priototal += j.proposal.priority;
					}
				}
			}
			// throw a coin (on which job it lands is proportional to the
			// priority)
			Double coin = r.nextDouble() * priototal;
			for (JobCombination jc : jobs) {
				coin -= jc.getPriority();
				if (coin <= 0) {
					// pick this job
					selected.add(jc);
					jobs.remove(jc);
					break;
				}
			}
		}

		return selected;
	}
}
