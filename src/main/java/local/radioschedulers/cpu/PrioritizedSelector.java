package local.radioschedulers.cpu;

import java.util.Comparator;
import java.util.HashMap;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;

public class PrioritizedSelector extends ShortestFirstSelector {

	@Override
	protected Comparator<JobCombination> generateComparator(HashMap<Job, Double> timeleft) {
		return new Comparator<JobCombination>() {

			@Override
			public int compare(JobCombination o1, JobCombination o2) {
				Double p1 = o1.getPriority();
				Double p2 = o2.getPriority();

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

}
