package local.radioschedulers.preschedule.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTMap;
import local.radioschedulers.LSTTime;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.preschedule.RequirementGuard;

import org.apache.log4j.Logger;

public abstract class CompatibleJobFactory {
	static Logger log = Logger.getLogger(CompatibleJobFactory.class);

	protected List<Job> base;
	protected Set<JobCombination> combinations;
	protected RequirementGuard guard;
	protected Map<Set<Job>, Set<JobCombination>> combinationsCache = new HashMap<Set<Job>, Set<JobCombination>>();

	public CompatibleJobFactory(Collection<Job> base, RequirementGuard req) {
		this.base = new ArrayList<Job>(base);
		this.guard = req;
		generateAllCombinations();
	}

	protected abstract void generateAllCombinations();

	public Collection<JobCombination> getCombinations() {
		return combinations;
	}

	public Set<JobCombination> getCombinations(Set<Job> jobs) {
		Set<JobCombination> result = combinationsCache.get(jobs);
		if (result == null) {
			result = getCombinationsInternal(jobs);
			combinationsCache.put(jobs, result);
		}
		return result;
	}

	protected abstract Set<JobCombination> getCombinationsInternal(Set<Job> jobs);

	public ScheduleSpace getPossibleTimeLine(Collection<Job> alljobs) {
		ScheduleSpace timeline;

		Map<LSTTime, Set<Job>> possibles = new LSTMap<Set<Job>>();

		for (Job j : alljobs) {
			/**
			 * This smart piece of code does not iterate through all slots a
			 * day can have. It starts at the first slot a job can be placed,
			 * and continues until the last slot (by lstmin/lstmax).
			 */
			for (int slot = 0; slot < ScheduleSpace.LST_SLOTS_PER_DAY; slot++) {
				long minute = (((int) Math.ceil(j.lstmin * 60
						/ ScheduleSpace.LST_SLOTS_MINUTES)
						* ScheduleSpace.LST_SLOTS_MINUTES + slot
						* ScheduleSpace.LST_SLOTS_MINUTES) % (ScheduleSpace.LST_SLOTS_PER_DAY * ScheduleSpace.LST_SLOTS_MINUTES));

				if (j.lstmin < j.lstmax) {
					if (minute < Math.round(j.lstmin * 60)
							|| minute >= Math.round(j.lstmax * 60))
						break;
				} else {
					if (minute >= Math.round(j.lstmax * 60)
							&& minute < Math.round(j.lstmin * 60))
						break;
				}

				LSTTime t = new LSTTime(0L, minute);
				Set<Job> list;
				if (possibles.containsKey(t)) {
					list = possibles.get(t);
				} else {
					list = new HashSet<Job>();
					possibles.put(t, list);
				}
				list.add(j);
			}
		}

		for (Entry<LSTTime, Set<Job>> e : possibles.entrySet()) {
			if (e.getValue() == null)
				possibles.put(e.getKey(), new HashSet<Job>());
		}

		timeline = new ScheduleSpace();
		for (Entry<LSTTime, Set<Job>> e : possibles.entrySet()) {
			LSTTime t = e.getKey();
			Set<Job> jobs = e.getValue();

			for (JobCombination jc : getCombinations(jobs)) {
				timeline.add(t, jc);
			}
		}

		return timeline;

	}

}