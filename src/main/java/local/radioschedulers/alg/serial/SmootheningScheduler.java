package local.radioschedulers.alg.serial;

import java.util.Map.Entry;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.LSTTimeIterator;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;

/**
 * Takes the solution of another Scheduler, and extends each task onto empty
 * fields.
 * 
 * @author Johannes Buchner
 */
public class SmootheningScheduler extends ListingScheduler {
	private IScheduler scheduler;

	public SmootheningScheduler(IScheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public String toString() {
		return this.scheduler.toString() + " (smoothened)";
	}

	private static Logger log = Logger.getLogger(SmootheningScheduler.class);

	@Override
	protected Schedule createEmptySchedule(ScheduleSpace timeline) {
		return scheduler.schedule(timeline);
	}

	@Override
	protected Schedule doSchedule(ScheduleSpace timeline, Schedule s) {
		timeleft.clear();
		super.fillTimeleft(timeline);
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
		LSTTimeIterator itbw = new LSTTimeIterator(t, new LSTTime(0, 0),
				-ScheduleSpace.LST_SLOTS_MINUTES);
		itbw.next();
		LSTTimeIterator it = new LSTTimeIterator(t, new LSTTime(t.day + 1,
				t.minute), ScheduleSpace.LST_SLOTS_MINUTES);
		it.next();
		// go back in time
		boolean backward = true;
		boolean forward = true;
		while (backward || forward) {
			backward = makeSimilar(jc, s, timeline, itbw, backward);
			forward = makeSimilar(jc, s, timeline, it, forward);
		}
	}

	private boolean makeSimilar(JobCombination jc, Schedule s,
			ScheduleSpace timeline, LSTTimeIterator it, boolean enabled) {
		if (enabled) {
			if (!it.hasNext()) {
				return false;
			}
			LSTTime t = it.next();
			if (s.isEmpty(t) && timeline.get(t).contains(jc) && !isFinished(jc)) {
				if (log.isDebugEnabled())
					log.debug("extending @" + t);
				s.add(t, jc);
				reduceTimeleft(jc);
			} else {
				enabled = false;
			}
		}
		return enabled;
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
