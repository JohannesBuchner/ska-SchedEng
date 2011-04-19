package local.radioschedulers.greedy;

import local.radioschedulers.LSTTime;
import local.radioschedulers.cpu.CPULikeScheduler;
import local.radioschedulers.cpu.JobSelector;

import org.apache.log4j.Logger;

/**
 * Acts like {@link CPULikeScheduler}, until a timeslot has only one choice
 * left, at which point it acts like {@link GreedyLeastChoiceScheduler}.
 * 
 * @author Johannes Buchner
 */
public class MixedModeUnlessOneChoiceScheduler extends
		GreedyLeastChoiceScheduler {
	public MixedModeUnlessOneChoiceScheduler(JobSelector selector) {
		super(selector);
	}

	private static Logger log = Logger
			.getLogger(MixedModeUnlessOneChoiceScheduler.class);

	@Override
	protected LSTTime nextUnassignedSlot() {
		if (timeslotsByChoice.containsKey(1) || unassigned.isEmpty()) {
			return super.nextUnassignedSlot();
		} else {
			return unassigned.remove(0);
		}
	}
}
