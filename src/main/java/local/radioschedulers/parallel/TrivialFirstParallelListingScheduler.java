package local.radioschedulers.parallel;

import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.greedy.JobSortCriterion;

public class TrivialFirstParallelListingScheduler extends ParallelListingScheduler {

	public TrivialFirstParallelListingScheduler(JobSortCriterion sortFunction) {
		super(sortFunction);
	}

	@Override
	protected void fillTimeleft(ScheduleSpace timeline, Schedule s) {
		super.fillTimeleft(timeline, s);
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			Set<JobCombination> jcs = e.getValue();
			LSTTime t = e.getKey();
			if (jcs.size() == 1) {
				handleTrivialChoice(t, jcs.iterator().next(), s);
			}
		}
	}

	@Override
	protected boolean handleTrivialChoice(LSTTime t, JobCombination jc,
			Schedule s) {
		for (Job j : jc.jobs) {
			if (timeleft.get(j) > 0) {
				s.add(t, jc);
				timeleft.put(j, timeleft.get(j) - 1.
						/ Schedule.LST_SLOTS_PER_HOUR);
			}
		}
		return true;
	}
}
