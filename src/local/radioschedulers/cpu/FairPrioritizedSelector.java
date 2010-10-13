package local.radioschedulers.cpu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import local.radioschedulers.Job;

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
	public Collection<Job> select(Collection<Job> list) {
		List<Job> jobs = pruneDone(list);
		if (jobs.isEmpty())
			return jobs;

		List<Job> selected = new ArrayList<Job>();
		while (!jobs.isEmpty()) {
			Double priototal = 0.;
			for (Job j : jobs) {
				if (j.proposal.mustcomplete) {
					selected.add(j);
					jobs.remove(j);
				} else {
					priototal += j.proposal.priority;
				}
			}
			// throw a coin (on which job it lands is proportional to the
			// priority)
			Double coin = r.nextDouble() * priototal;
			for (Job j : jobs) {
				coin -= j.proposal.priority;
				if (coin <= 0) {
					// pick this job
					selected.add(j);
					jobs.remove(j);
					break;
				}
			}
		}

		return selected;
	}
}
