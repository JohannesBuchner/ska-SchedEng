package local.radioschedulers;

import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

public class CPUSchedulersParallelTest extends CPUSchedulersTest {

	@Override
	protected RequirementGuard getRequirementGuard() {
		return new ParallelRequirementGuard();
	}
}
