package local.radioschedulers.ga.wf;

import local.radioschedulers.ga.watchmaker.AbstractScheduleMutation;
import local.radioschedulers.ga.watchmaker.ScheduleSimilarPrevMutation;

import org.apache.log4j.Logger;

public class ScheduleKeepingMutationTest extends ScheduleMutationTest {
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ScheduleKeepingMutationTest.class);

	@Override
	protected AbstractScheduleMutation getOperator() {
		ScheduleSimilarPrevMutation op = new ScheduleSimilarPrevMutation(template, MUTATION_PROBABILITY);
		return op;
	}
}