package local.radioschedulers.preschedule.parallel;

import java.util.Collection;

import local.radioschedulers.Job;
import local.radioschedulers.preschedule.RequirementGuard;

public class TwoParallelRequirementGuard extends RequirementGuard {

	@Override
	public boolean compatible(Collection<Job> list) {
		if (list.size() <= 2)
			return true;
		else
			return false;
	}

}
