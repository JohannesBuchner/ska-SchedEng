package local.radioschedulers.preschedule;

import java.util.Collection;

import local.radioschedulers.Job;

public class SingleRequirementGuard extends RequirementGuard {

	@Override
	public boolean compatible(Collection<Job> list) {
		if (list.size() > 1)
			return false;
		else
			return true;
	}
}
