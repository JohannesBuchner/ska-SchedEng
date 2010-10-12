package local.radioschedulers.cpu;
import java.util.Collections;
import java.util.Vector;

import local.radioschedulers.Job;

public class RandomizedScheduler extends RoundRobinScheduler {
	@Override
	protected Job selectJob(Vector<Job> list) {
		Collections.shuffle(list);
		
		for (Job j : list) {
			if (timeleft.containsKey(j) && timeleft.get(j) > 0) {
				return j;
			}
		}
		return null;
	}
}
