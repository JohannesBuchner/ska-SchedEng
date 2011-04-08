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
import local.radioschedulers.LSTTime;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.preschedule.RequirementGuard;

import org.apache.log4j.Logger;

public class FastRecursiveCompatibleJobFactory {
	private static Logger log = Logger
			.getLogger(FastRecursiveCompatibleJobFactory.class);

	private List<Job> base;
	private Set<JobCombination> combinations;
	private RequirementGuard guard;

	public FastRecursiveCompatibleJobFactory(Collection<Job> base,
			RequirementGuard req) {
		this.base = new ArrayList<Job>(base);
		this.guard = req;
		generateAllCombinations();
	}

	private void generateAllCombinations() {
		combinations = new HashSet<JobCombination>();

		for (int i = 0; i < base.size(); i++) {
			JobCombination jc = new JobCombination();
			jc.jobs.add(base.get(i));
			combinations.add(jc);
			expand(jc, i + 1, 1);
		}
	}

	private void expand(JobCombination jc, int i, int nelements) {
		// log.debug(" nelements:" + nelements + " i:" + i);
		for (; i < base.size(); i++) {
			JobCombination jc1 = new JobCombination();
			jc1.jobs.addAll(jc.jobs);
			jc1.jobs.add(base.get(i));
			if (guard.compatible(jc1.jobs)) {
				// good. recurse into it
				expand(jc1, i + 1, nelements + 1);
				combinations.add(jc1);
			}
		}
	}

	protected Set<JobCombination> getCombinationsInternal(Set<Job> jobs) {
		Set<JobCombination> combo = new HashSet<JobCombination>();
		for (JobCombination c : combinations) {
			boolean allthere = true;
			for (Job j : c.jobs) {
				if (!jobs.contains(j)) {
					allthere = false;
					break;
				}
			}
			if (allthere)
				combo.add(c);
		}
		return combo;
	}

	public Collection<JobCombination> getCombinations() {
		return combinations;
	}

	private Map<Set<Job>, Set<JobCombination>> combinationsCache = new HashMap<Set<Job>, Set<JobCombination>>();

	public Set<JobCombination> getCombinations(Set<Job> jobs) {
		Set<JobCombination> result = combinationsCache.get(jobs);
		if (result == null) {
			result = getCombinationsInternal(jobs);
			combinationsCache.put(jobs, result);
		}
		return result;
	}

	public static final int LST_SLOTS = 24 * 4;
	public static final int LST_SLOTS_MINUTES = 24 * 60 / LST_SLOTS;

	public ScheduleSpace getPossibleTimeLine(Collection<Job> alljobs) {
		ScheduleSpace timeline;

		HashMap<LSTTime, Set<Job>> possibles = new HashMap<LSTTime, Set<Job>>();

		log.debug("Possibles:");
		for (Job j : alljobs) {
			for (int slot = 0; slot < LST_SLOTS; slot++) {
				long minute = ((int) (j.lstmin * 60 + slot * LST_SLOTS_MINUTES) % (LST_SLOTS * LST_SLOTS_MINUTES));

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
