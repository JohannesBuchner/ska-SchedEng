package local.radioschedulers.cpu;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import local.radioschedulers.Job;

public class PrioritizedScheduler extends RoundRobinScheduler {
	private Comparator<Job> cmp = new Comparator<Job>() {
		
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
	};;;

	@Override
	protected Job selectJob(Vector<Job> list) {
		Collections.sort(list, cmp );
		
		for (Job j : list) {
			if (timeleft.containsKey(j) && timeleft.get(j) > 0) {
				return j;
			}
		}
		return null;
	}
}
