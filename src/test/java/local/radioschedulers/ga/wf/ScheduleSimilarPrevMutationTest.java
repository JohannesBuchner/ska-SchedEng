package local.radioschedulers.ga.wf;

import local.radioschedulers.alg.ga.watchmaker.AbstractScheduleMutation;
import local.radioschedulers.alg.ga.watchmaker.ScheduleSimilarPrevMutation;

import org.apache.log4j.Logger;

public class ScheduleSimilarPrevMutationTest extends ScheduleMutationTest {
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ScheduleSimilarPrevMutationTest.class);

	@Override
	protected AbstractScheduleMutation getOperator() {
		ScheduleSimilarPrevMutation op = new ScheduleSimilarPrevMutation(template, MUTATION_PROBABILITY);
		return op;
	}
}
