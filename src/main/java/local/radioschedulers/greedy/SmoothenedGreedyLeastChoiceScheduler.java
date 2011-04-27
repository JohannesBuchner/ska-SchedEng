package local.radioschedulers.greedy;

import java.util.Map.Entry;

import org.apache.log4j.Logger;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.LSTTimeIterator;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.cpu.JobSelector;

/**
 * Takes the solution of {@link GreedyLeastChoiceScheduler}, and extends each
 * task onto empty fields.
 * 
 * @author Johannes Buchner
 */
public class SmoothenedGreedyLeastChoiceScheduler extends
		GreedyLeastChoiceScheduler {
	public SmoothenedGreedyLeastChoiceScheduler(JobSelector selector) {
		super(selector);
	}

	private static Logger log = Logger
			.getLogger(SmoothenedGreedyLeastChoiceScheduler.class);

	@Override
	public Schedule schedule(ScheduleSpace timeline) {
		Schedule s = super.schedule(timeline);
		timeleft.clear();
		super.updateChoices(timeline);
		Schedule s2 = new Schedule();
		for (Entry<LSTTime, JobCombination> e : s) {
			if (e.getValue() == null || isFinished(e.getValue()))
				continue;

			makeSimilar(e.getKey(), e.getValue(), s2, s, timeline);
		}
		return s2;
	}

	protected void makeSimilar(LSTTime t, JobCombination jc, Schedule s,
			Schedule fragmentedSchedule, ScheduleSpace timeline) {
		s.add(t, jc);
		reduceTimeleft(jc);

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
				if (fragmentedSchedule.isEmpty(tBack) && s.isEmpty(tBack)
						&& tBack.day >= t.day - 1
						&& timeline.get(tBack).contains(jc) && !isFinished(jc)) {
					log.debug("extending backwards @" + tBack);
					s.add(tBack, jc);
					reduceTimeleft(jc);
				} else {
					backward = false;
				}
			}
			if (forward) {
				LSTTime tFw = it.next();
				if (fragmentedSchedule.isEmpty(tFw) && s.isEmpty(tFw)
						&& it.hasNext() && timeline.get(tFw).contains(jc)
						&& !isFinished(jc)) {
					log.debug("extending forwards @" + tFw);
					s.add(tFw, jc);
					reduceTimeleft(jc);
				} else {
					forward = false;
				}
			}
		}
	}

	private void reduceTimeleft(JobCombination jc) {
		for (Job j : jc.jobs) {
			timeleft.put(j, timeleft.get(j) - 1. / Schedule.LST_SLOTS_PER_HOUR);
		}
	}

	private boolean isFinished(JobCombination jc) {
		for (Job j : jc.jobs)
			if (timeleft.get(j) < 0)
				return true;
		return false;
	}

}
