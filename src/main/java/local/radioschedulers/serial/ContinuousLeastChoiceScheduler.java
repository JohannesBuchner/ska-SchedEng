package local.radioschedulers.serial;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.LSTTimeIterator;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;

/**
 * Acts like {@link SerialLeastChoiceScheduler}, but extends the task where
 * unassigned.
 * 
 * @author Johannes Buchner
 */
public class ContinuousLeastChoiceScheduler extends SerialLeastChoiceScheduler {
	public ContinuousLeastChoiceScheduler(JobSelector selector) {
		super(selector);
	}

	private static Logger log = Logger
			.getLogger(ContinuousLeastChoiceScheduler.class);

	protected List<LSTTime> neighbors = new ArrayList<LSTTime>();
	protected LSTTime lastSlot;
	protected JobCombination lastJc;

	@Override
	protected LSTTime getNextUnassignedTimeslot() {
		boolean canContinue = false;
		if (!neighbors.isEmpty()) {
			// probe neighbors for unassigned
			canContinue = true;
			for (Job j : lastJc.jobs) {
				if (!possibleSlots.containsKey(j)) {
					canContinue = false;
				}
			}
			if (!canContinue) {
				log.debug("can not continue, " + lastJc + " ran out of hours.");
				neighbors.clear();
			}
		}

		if (lastSlot != null && neighbors.isEmpty()) {
			// get last choice
			Set<JobCombination> jcs = super.choices.get(lastSlot);
			if (!jcs.isEmpty()) {
				lastJc = jcs.iterator().next();
				neighbors = findNeighborsLike(lastJc);
				log.debug("found " + neighbors.size() + " neighbors");
			}
		}

		if (!neighbors.isEmpty()) {
			lastSlot = neighbors.remove(0);
			log.debug("@" + lastSlot + " using neighbor");
		} else {
			lastSlot = super.getNextUnassignedTimeslot();
			log.debug("@" + lastSlot + " using least choice");
		}
		unassignedTimeslots.remove(lastSlot);
		return lastSlot;
	}

	protected List<LSTTime> findNeighborsLike(JobCombination jc) {
		List<LSTTime> neighbors = new ArrayList<LSTTime>();
		LSTTimeIterator itbw = new LSTTimeIterator(lastSlot, new LSTTime(
				lastSlot.day + 1, lastSlot.minute),
				-ScheduleSpace.LST_SLOTS_MINUTES);
		itbw.next();
		LSTTimeIterator it = new LSTTimeIterator(lastSlot, new LSTTime(
				lastSlot.day + 1, lastSlot.minute),
				ScheduleSpace.LST_SLOTS_MINUTES);
		it.next();
		// go back in time
		boolean backward = true;
		boolean forward = true;
		while (backward || forward) {
			if (backward) {
				LSTTime tBack = itbw.next();
				if (unassignedTimeslots.contains(tBack) && tBack.day >= lastSlot.day - 1
						&& choices.get(tBack).contains(jc)) {
					neighbors.add(tBack);
				} else {
					backward = false;
				}
			}
			if (forward) {
				LSTTime t = it.next();
				if (unassignedTimeslots.contains(t) && it.hasNext()
						&& choices.get(t).contains(jc)) {
					neighbors.add(t);
				} else {
					forward = false;
				}
			}
		}
		log.debug("@" + lastSlot + " ... extended to " + itbw.next() + ".."
				+ it.next());
		return neighbors;
	}
}
