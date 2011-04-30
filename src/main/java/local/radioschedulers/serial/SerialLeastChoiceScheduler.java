package local.radioschedulers.serial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;

/**
 * Scan ScheduleSpace and annotate how many possible slots are available for
 * each Job (= pressure).
 * 
 * Sort timeslots by number of jobs to choose from.
 * <ul>
 * <li>Go to the least choice timeslot that hasn't been assigned yet.
 * <li>If job reaches total hours, remove job from all choices.
 * <li>continue from 1 until no more jobs or timeslots available.
 * </ul>
 * 
 * This is basically how one can solve sudokus. Always resolve the one with only
 * 1 choice, and propagate the consequences.
 * 
 * @author Johannes Buchner
 */
public class SerialLeastChoiceScheduler extends SerialListingScheduler {
	public SerialLeastChoiceScheduler(JobSelector jobselector) {
		super(jobselector);
	}

	private static Logger log = Logger
			.getLogger(SerialLeastChoiceScheduler.class);

	protected Map<Integer, List<LSTTime>> timeslotsByChoice = new HashMap<Integer, List<LSTTime>>();

	protected ScheduleSpace choices;

	/**
	 * fill nchoices and timeslotsByChoice; and timeleft
	 * 
	 * @param timeline
	 * @param s
	 */
	protected void updateChoices(ScheduleSpace timeline) {
		choices = new ScheduleSpace();
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			LSTTime t = e.getKey();
			Set<JobCombination> jcs = e.getValue();
			List<LSTTime> l = timeslotsByChoice.get(jcs.size());
			if (l == null) {
				l = new ArrayList<LSTTime>();
				timeslotsByChoice.put(jcs.size(), l);
			}
			l.add(t);
			for (JobCombination jc : jcs) {
				choices.add(t, jc);
			}
		}
		for (Entry<Integer, List<LSTTime>> e : timeslotsByChoice.entrySet()) {
			log.debug(e.getKey() + " choices -- " + e.getValue().size()
					+ " timeslots.");
		}
	}

	@Override
	protected void beforeSchedule(ScheduleSpace timeline) {
		super.beforeSchedule(timeline);
		updateChoices(timeline);
	}

	@Override
	protected void choose(LSTTime t, JobCombination jc, Schedule s) {
		super.choose(t, jc, s);
		choices.clear(t);
		choices.add(t, jc);
	}

	@Override
	protected List<LSTTime> removeChoice(Job j, Schedule s) {
		List<LSTTime> slots = super.removeChoice(j, s);
		// reduce choices from the effected slots
		for (LSTTime t : slots) {
			Set<JobCombination> jcs = choices.get(t);
			// this slot certainly hasn't the same number of choices anymore
			// log.debug("  @" + t + " -- removing timeslotsByChoice entry of "
			// + jcs.size());

			if (timeslotsByChoice.containsKey(jcs.size())) {
				// might have been deleted before
				timeslotsByChoice.get(jcs.size()).remove(t);
			}

			choices.clear(t);
			int newnchoices = 0;
			for (JobCombination jc : jcs) {
				if (!jc.jobs.contains(j)) {
					// keep other choices
					choices.add(t, jc);
					newnchoices++;
				}
			}

			// this slot has this new number of choices now
			if (newnchoices > 0) {
				if (!timeslotsByChoice.containsKey(newnchoices)) {
					timeslotsByChoice
							.put(newnchoices, new ArrayList<LSTTime>());
				}
				timeslotsByChoice.get(newnchoices).add(t);
			} else {
				// lost this one
				unassignedTimeslots.remove(t);
			}
			// log.debug("  @" + t + " -- new timeslotsByChoice entry of "
			// + newnchoices);
		}
		return slots;
	}

	@Override
	protected LSTTime getNextUnassignedTimeslot() {
		ArrayList<Integer> keys = new ArrayList<Integer>(timeslotsByChoice
				.keySet());
		Collections.sort(keys);
		for (Integer k : keys) {
			if (k == 0)
				continue;
			List<LSTTime> slots = timeslotsByChoice.get(k);
			// sort by time.
			Collections.sort(slots);

			for (LSTTime t : slots) {
				slots.remove(t);
				if (slots.isEmpty())
					timeslotsByChoice.remove(k);
				unassignedTimeslots.remove(t);
				return t;
			}
		}
		return null;
	}

}
