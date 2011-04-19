package local.radioschedulers.greedy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.cpu.JobSelector;

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
 * @author Johannes Buchner
 */
public class GreedyLeastChoiceScheduler implements IScheduler {
	private static Logger log = Logger
			.getLogger(GreedyLeastChoiceScheduler.class);

	protected Map<Job, List<LSTTime>> possibleSlots = new HashMap<Job, List<LSTTime>>();
	protected Map<Integer, List<LSTTime>> timeslotsByChoice = new HashMap<Integer, List<LSTTime>>();
	protected List<LSTTime> unassigned = new LinkedList<LSTTime>();

	protected ScheduleSpace choices;

	/**
	 * how many hours are left for this job
	 */
	protected HashMap<Job, Double> timeleft = new HashMap<Job, Double>();
	protected JobSelector selector;

	public GreedyLeastChoiceScheduler(JobSelector selector) {
		this.selector = selector;
		selector.setTimeleft(timeleft);
	}

	/**
	 * fill nchoices and timeslotsByChoice; and timeleft
	 * 
	 * @param timeline
	 * @param s
	 */
	protected void updateChoices(ScheduleSpace timeline, Schedule s) {
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
				for (Job j : jc.jobs) {
					if (!timeleft.containsKey(j)) {
						timeleft.put(j, j.hours);
					}

					l = possibleSlots.get(j);
					if (l == null) {
						l = new ArrayList<LSTTime>();
						possibleSlots.put(j, l);
					}
					l.add(t);
				}
			}
			if (!choices.isEmpty(t))
				unassigned.add(t);
		}
		for (Entry<Integer, List<LSTTime>> e : timeslotsByChoice.entrySet()) {
			log.debug(e.getKey() + " choices -- " + e.getValue().size()
					+ " timeslots.");
		}
	}

	protected void choose(LSTTime t, JobCombination jc, Schedule s) {
		s.add(t, jc);
		for (JobCombination jc1 : choices.get(t)) {
			for (Job j : jc1.jobs) {
				possibleSlots.get(j).remove(t);
			}
		}

		choices.clear(t);
		choices.add(t, jc);
		for (Job j : jc.jobs) {
			timeleft.put(j, timeleft.get(j) - 1. / Schedule.LST_SLOTS_PER_HOUR);
			if (timeleft.get(j) <= 0) {
				removeChoice(j, s);
			}
		}
	}

	protected void removeChoice(Job j, Schedule s) {
		log.debug("done scheduling " + j);
		timeleft.remove(j);
		// this only contains the other slots, because we removed the
		// current before
		Collection<LSTTime> slots = possibleSlots.remove(j);
		log.debug("  removing choices from other slots " + slots.size());

		// reduce choices from the effected slots
		for (LSTTime t : slots) {
			Set<JobCombination> jcs = choices.get(t);
			// this slot certainly hasn't the same number of choices anymore
			// log.debug("  @" + t + " -- removing timeslotsByChoice entry of "
			// + jcs.size());
			timeslotsByChoice.get(jcs.size()).remove(t);

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
				unassigned.remove(t);
			}
			// log.debug("  @" + t + " -- new timeslotsByChoice entry of "
			// + newnchoices);
		}

	}

	protected LSTTime nextUnassignedSlot() {
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
				unassigned.remove(t);
				return t;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public Schedule schedule(ScheduleSpace timeline) {
		Schedule s = new Schedule();

		// Sort timeslots by number of jobs to choose from.
		updateChoices(timeline, s);

		while (!possibleSlots.isEmpty()) {
			LSTTime t = nextUnassignedSlot();
			if (t == null)
				break;
			JobCombination jc = select(t);
			// log.debug("choosing @" + t + " -- " + jc);
			if (jc != null)
				choose(t, jc, s);
		}

		return s;
	}

	private JobCombination select(LSTTime t) {
		Set<JobCombination> jcs = choices.get(t);
		Collection<JobCombination> jc = selector.select(jcs);
		Iterator<JobCombination> it = jc.iterator();
		if (!it.hasNext())
			return null;
		return jc.iterator().next();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " with jobselector "
				+ selector.toString() + " instance " + hashCode();
	}
}
