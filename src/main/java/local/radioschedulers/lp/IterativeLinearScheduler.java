package local.radioschedulers.lp;

import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.IScheduler;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;

/**
 * Schedules 1 month after the other, holding the other sections free.
 * 
 * @author Johannes Buchner
 */
public class IterativeLinearScheduler implements IScheduler {
	private static Logger log = Logger
			.getLogger(IterativeLinearScheduler.class);

	private static final int SECTIONSIZE = 30;

	@Override
	public Schedule schedule(ScheduleSpace timeline) {
		Schedule s = new Schedule();
		LSTTime last = timeline.findLastEntry();

		for (int i = 0; i < last.day / SECTIONSIZE; i++) {
			ScheduleSpace timeline2 = getPartialScheduleSpace(timeline, i
					* SECTIONSIZE, (i + 1) * SECTIONSIZE, i * SECTIONSIZE,
					(i + 1) * SECTIONSIZE, s);

			ParallelLinearScheduler pls = new ParallelLinearScheduler();
			s = pls.schedule(timeline2);
			log.debug(i + " of " + last.day / SECTIONSIZE + " sections done.");
		}
		
		log.debug("done with first cut");

		// redo, allow shifting stuff around.
		for (int i = 0; i < last.day / SECTIONSIZE; i++) {
			for (int j = i + 1; j < last.day / SECTIONSIZE; j++) {
				ScheduleSpace timeline2 = getPartialScheduleSpace(timeline, i
						* SECTIONSIZE, (i + 1) * SECTIONSIZE, j * SECTIONSIZE,
						(j + 1) * SECTIONSIZE, s);

				ParallelLinearScheduler pls = new ParallelLinearScheduler();
				s = pls.schedule(timeline2);
				log.debug(i + " of " + last.day / SECTIONSIZE
						+ " sections done.");
			}
		}
		log.debug("done");

		return s;
	}

	private ScheduleSpace getPartialScheduleSpace(ScheduleSpace timeline,
			int startday, int endday, int startday2, int endday2, Schedule s) {
		ScheduleSpace partial = new ScheduleSpace();

		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			LSTTime t = e.getKey();

			if ((t.day >= startday && t.day < endday)
					|| (t.day >= startday2 && t.day < endday2)) {
				// variable zone, copy from timeline
				for (JobCombination jc : e.getValue()) {
					partial.add(t, jc);
				}
			} else {
				// copy
				if (!s.isEmpty(t))
					partial.add(t, s.get(t));
			}
		}
		return partial;
	}
}
