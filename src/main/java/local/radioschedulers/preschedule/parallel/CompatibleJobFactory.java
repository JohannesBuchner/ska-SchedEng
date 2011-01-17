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

public class CompatibleJobFactory {
	private static Logger log = Logger.getLogger(CompatibleJobFactory.class);

	private Collection<Job> base;
	private List<JobCombination> combinations;
	private RequirementGuard guard;

	public CompatibleJobFactory(Collection<Job> base, RequirementGuard req) {
		this.base = base;
		this.guard = req;
		generateAllCombinations();
	}

	private void generateAllCombinations() {
		combinations = new ArrayList<JobCombination>();

		// the idle-job
		log.debug("adding idle job");
		combinations.add(new JobCombination());

		// the single jobs
		log.debug("adding single jobs");
		List<JobCombination> extensions = new ArrayList<JobCombination>();
		for (Job j : base) {
			JobCombination c = new JobCombination();
			c.jobs.add(j);
			extensions.add(c);
		}
		combinations.addAll(extensions);

		// enrich
		while (true) {
			// this could be improved, but hey
			log.debug("extending ...");
			extensions = extend(combinations, base);
			log.debug("extending ... got " + extensions.size() + " extensions");
			if (extensions.size() == 0)
				break;
			else
				combinations.addAll(extensions);
		}
	}

	/**
	 * calculate a x b (crossproduct), without repetition
	 * 
	 * @param a
	 * @param b
	 * @return crossproduct of a x b
	 */
	private List<JobCombination> extend(Collection<JobCombination> a,
			Collection<Job> b) {
		List<JobCombination> r = new ArrayList<JobCombination>();

		for (JobCombination la : a) {
			for (Job j : b) {
				if (la.jobs.contains(j))
					continue;

				JobCombination lab = new JobCombination();
				lab.jobs.addAll(la.jobs);
				lab.jobs.add(j);
				if (guard.compatible(lab.jobs)) {
					if (!a.contains(lab))
						r.add(lab);
				}
			}
		}

		return r;
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

	public List<JobCombination> getCombinations() {
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

		System.out.println("Possibles:");
		for (Job j : alljobs) {
			for (long minute = Math.round(j.lstmin) * 60;; minute = (minute + LST_SLOTS_MINUTES)
					% (24 * 60)) {
				if (j.lstmin < j.lstmax) {
					if (minute < Math.round(j.lstmin) * 60
							|| minute > Math.round(j.lstmax) * 60)
						break;
				} else {
					if (minute < Math.round(j.lstmin) * 60
							&& minute > Math.round(j.lstmax) * 60)
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
				System.out.println("@" + t + " : " + j);
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
