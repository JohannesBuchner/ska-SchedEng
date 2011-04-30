package local.radioschedulers.alg.serial;

import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

public class SerialListingSchedulersParallelTest extends SerialListingSchedulersTest {

	@Override
	protected RequirementGuard getRequirementGuard() {
		return new ParallelRequirementGuard();
	}
}
