package local.radioschedulers.cpu;

import java.util.Comparator;
import java.util.HashMap;

import local.radioschedulers.Job;

public class PrioritizedSelector extends ShortestFirstSelector {

	@Override
	protected Comparator<Job> generateComparator(HashMap<Job, Double> timeleft) {
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

}
