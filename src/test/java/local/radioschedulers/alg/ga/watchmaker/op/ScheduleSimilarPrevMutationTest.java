package local.radioschedulers.alg.ga.watchmaker.op;

import org.apache.log4j.Logger;

public class ScheduleSimilarPrevMutationTest extends ScheduleMutationTest {
	private static Logger log = Logger
			.getLogger(ScheduleSimilarPrevMutationTest.class);

	@Override
	protected AbstractScheduleMutation getOperator() {
		ScheduleSimilarPrevMutation op = new ScheduleSimilarPrevMutation(
				template, mutationProbability, false);
		return op;
	}
}
