package local.radioschedulers.cpu;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import local.radioschedulers.Job;

public class ShortestFirstScheduler extends RoundRobinScheduler {
	Comparator<Job> cmp = new Comparator<Job>() {

		@Override
		public int compare(Job o1, Job o2) {
			Double t1 = timeleft.get(o1);
			Double t2 = timeleft.get(o2);

			if (t1 == null)
				if (t2 == null)
					return 0;
				else
					return 1;
			else if (t2 == null)
				return -1;
			else
				return t1.compareTo(t2);
		}
	};

	@Override
	protected Job selectJob(Vector<Job> list) {
		Collections.sort(list, cmp);
		
		for (Job j : list) {
			if (timeleft.containsKey(j) && timeleft.get(j) > 0) {
				return j;
			}
		}
		return null;
	}
}
