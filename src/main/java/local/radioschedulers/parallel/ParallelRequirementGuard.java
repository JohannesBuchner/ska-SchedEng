package local.radioschedulers.parallel;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;

public class ParallelRequirementGuard extends RequirementGuard {
	public boolean isDateCompatible(JobCombination jc, LSTTime date) {
		boolean all = true;
		for (Job j : jc.jobs) {
			all = all & super.isDateCompatible(j, date);
		}
		return all;
	}

}
