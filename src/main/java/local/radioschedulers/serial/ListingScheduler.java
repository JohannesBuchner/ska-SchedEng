package local.radioschedulers.serial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

public abstract class ListingScheduler implements IScheduler {
	/**
	 * how many hours are left for this job
	 */
	protected Map<Job, Double> timeleft = new HashMap<Job, Double>();
	/**
	 * which timeslots are left for this job
	 */
	protected Map<Job, List<LSTTime>> possibleSlots = new HashMap<Job, List<LSTTime>>();

	protected void fillPossibles(ScheduleSpace timeline) {
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			if (e.getValue() != null) {
				for (JobCombination jc : e.getValue()) {
					for (Job j : jc.jobs) {
						List<LSTTime> l = possibleSlots.get(j);
						if (l == null) {
							l = new ArrayList<LSTTime>();
							possibleSlots.put(j, l);
						}
						l.add(e.getKey());
					}
				}
			}
		}
	}
	protected void fillTimeleft(ScheduleSpace timeline) {
		HashSet<JobCombination> alljobCombinations = new HashSet<JobCombination>();
		Set<Job> alljobs = new HashSet<Job>();
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			alljobCombinations.addAll(e.getValue());
		}
		for (JobCombination jc : alljobCombinations) {
			alljobs.addAll(jc.jobs);
		}
		for (Job j : alljobs) {
			timeleft.put(j, (double) j.hours);
		}
	}

	@Override
	public Schedule schedule(ScheduleSpace timeline) {
		beforeSchedule(timeline);
		return doSchedule(timeline, createEmptySchedule(timeline));
	}

	protected void beforeSchedule(ScheduleSpace timeline) {
		fillTimeleft(timeline);
		fillPossibles(timeline);
	}

	protected abstract Schedule doSchedule(ScheduleSpace timeline, Schedule schedule);

	protected Schedule createEmptySchedule(ScheduleSpace timeline) {
		return new Schedule();
	}
	
}