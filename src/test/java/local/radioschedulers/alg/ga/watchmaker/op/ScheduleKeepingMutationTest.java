package local.radioschedulers.alg.ga.watchmaker.op;

import local.radioschedulers.alg.ga.watchmaker.op.AbstractScheduleMutation;
import local.radioschedulers.alg.ga.watchmaker.op.ScheduleSimilarPrevMutation;

import org.apache.log4j.Logger;

public class ScheduleKeepingMutationTest extends ScheduleMutationTest {
	@SuppressWarnings("unused")
	private static Logger log = Logger
			.getLogger(ScheduleKeepingMutationTest.class);

	@Override
	protected AbstractScheduleMutation getOperator() {
		ScheduleSimilarPrevMutation op = new ScheduleSimilarPrevMutation(
				template, mutationProbability);
		return op;
	}
}
