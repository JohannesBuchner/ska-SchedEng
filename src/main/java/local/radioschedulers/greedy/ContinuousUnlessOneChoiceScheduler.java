package local.radioschedulers.greedy;

import local.radioschedulers.LSTTime;
import local.radioschedulers.deciders.JobSelector;
import local.radioschedulers.serial.SerialListingScheduler;

/**
 * Acts like {@link SerialListingScheduler}, until a timeslot has only one choice
 * left, at which point it acts like {@link GreedyLeastChoiceScheduler}.
 * 
 * @author Johannes Buchner
 */
public class ContinuousUnlessOneChoiceScheduler extends
		GreedyLeastChoiceScheduler {
	public ContinuousUnlessOneChoiceScheduler(JobSelector selector) {
		super(selector);
	}

	@Override
	protected LSTTime nextUnassignedSlot() {
		if (timeslotsByChoice.containsKey(1) || unassigned.isEmpty()) {
			return super.nextUnassignedSlot();
		} else {
			return unassigned.remove(0);
		}
	}
}
