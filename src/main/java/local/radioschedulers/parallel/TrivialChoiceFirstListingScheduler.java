package local.radioschedulers.parallel;

import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.serial.ListingScheduler;

public abstract class TrivialChoiceFirstListingScheduler extends ListingScheduler {
	private boolean decideTrivialChoicesFirst = false;

	public void setMakeTrivialChoicesFirst(boolean makeTrivialChoicesFirst) {
		this.decideTrivialChoicesFirst = makeTrivialChoicesFirst;
	}
	
	@Override
	protected Schedule createEmptySchedule(ScheduleSpace timeline) {
		Schedule s = super.createEmptySchedule(timeline);
		if (decideTrivialChoicesFirst) {
			for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
				Set<JobCombination> jcs = e.getValue();
				LSTTime t = e.getKey();
				if (jcs.size() == 1) {
					JobCombination jc = jcs.iterator().next();
					for (Job j : jc.jobs) {
						if (timeleft.get(j) > 0) {
							s.add(t, jc);
							timeleft.put(j, timeleft.get(j) - 1.
									/ Schedule.LST_SLOTS_PER_HOUR);
						}
					}
				}
			}
		}
		return s;
	}

}
