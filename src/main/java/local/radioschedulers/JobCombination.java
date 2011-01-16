package local.radioschedulers;

import java.util.ArrayList;
import java.util.List;

public class JobCombination extends JobWithResources {
	public List<Job> jobs = new ArrayList<Job>();

	public double getPriority() {
		double expprio = 0;
		for (Job j : jobs) {
			expprio = Math.exp(expprio) + Math.exp(j.proposal.priority);
		}
		return Math.log(expprio);
	}
}
