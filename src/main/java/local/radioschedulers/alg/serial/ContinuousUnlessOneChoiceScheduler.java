package local.radioschedulers.alg.serial;

import local.radioschedulers.LSTTime;

/**
 * Acts like {@link SerialListingScheduler}, until a timeslot has only one
 * choice left, at which point it acts like {@link SerialLeastChoiceScheduler}.
 * 
 * @author Johannes Buchner
 */
public class ContinuousUnlessOneChoiceScheduler extends
		SerialLeastChoiceScheduler {
	public ContinuousUnlessOneChoiceScheduler(JobSelector selector) {
		super(selector);
	}

	@Override
	protected LSTTime getNextUnassignedTimeslot() {
		if (timeslotsByChoice.containsKey(1) || unassignedTimeslots.isEmpty()) {
			return super.getNextUnassignedTimeslot();
		} else {
			return unassignedTimeslots.remove(0);
		}
	}
}
