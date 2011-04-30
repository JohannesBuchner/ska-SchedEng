package local.radioschedulers.greedy;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.LSTTimeIterator;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.deciders.JobSelector;

import org.apache.log4j.Logger;

/**
 * Acts like {@link GreedyLeastChoiceScheduler}, but extends the task where
 * unassigned.
 * 
 * @author Johannes Buchner
 */
public class ExtendingLeastChoiceScheduler extends GreedyLeastChoiceScheduler {
	public ExtendingLeastChoiceScheduler(JobSelector selector) {
		super(selector);
	}

	private static Logger log = Logger
			.getLogger(ExtendingLeastChoiceScheduler.class);

	/*
	 * chooser that makes the neighboring slots similar.
	 */
	@Override
	protected void choose(LSTTime t, JobCombination jc, Schedule s) {
		// handle this slot
		log.debug("assigning @" + t + " :: " + jc);
		super.choose(t, jc, s);
		
		// if (timeslotsByChoice.containsKey(1))
		//	return;

		// find neighbors
		LSTTimeIterator itbw = new LSTTimeIterator(t, new LSTTime(t.day + 1,
				t.minute), -ScheduleSpace.LST_SLOTS_MINUTES);
		itbw.next();
		LSTTimeIterator it = new LSTTimeIterator(t, new LSTTime(t.day + 1,
				t.minute), ScheduleSpace.LST_SLOTS_MINUTES);
		it.next();
		// go back in time
		boolean backward = true;
		boolean forward = true;
		while (backward || forward) {
			if (backward) {
				LSTTime tBack = itbw.next();
				if (unassigned.contains(tBack) && tBack.day >= t.day - 1
						&& choices.get(tBack).contains(jc) && !isFinished(jc)) {
					log.debug("extending backwards @" + tBack);
					super.choose(tBack, jc, s);
				} else {
					backward = false;
				}
			}
			if (forward) {
				LSTTime tFw = it.next();
				if (unassigned.contains(tFw) && it.hasNext()
						&& choices.get(tFw).contains(jc) && !isFinished(jc)) {
					log.debug("extending forwards @" + tFw);
					super.choose(tFw, jc, s);
				} else {
					forward = false;
				}
			}
		}
	}

	private boolean isFinished(JobCombination jc) {
		for (Job j : jc.jobs)
			if (!possibleSlots.containsKey(j))
				return true;
		return false;
	}
}
