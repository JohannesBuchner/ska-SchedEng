package local.radioschedulers.preschedule.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.preschedule.RequirementGuard;

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

		HashMap<LSTTime, Set<Job>> possibles = new HashMap<LSTTime, Set<Job>>();

		for (Job j : alljobs) {
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
				// log.debug("@" + t + " : " + j);
			}
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