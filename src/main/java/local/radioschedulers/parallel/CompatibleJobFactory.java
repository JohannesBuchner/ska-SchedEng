package local.radioschedulers.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.TimeLine;

public class CompatibleJobFactory {
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
		combinations.add(new JobCombination());

		// the single jobs
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
			extensions = extend(extensions, base);
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
					r.add(lab);
				}
			}
		}

		return r;
	}

	// TODO: I guess this could use some caching
	public List<JobCombination> getCombinations(List<Job> jobs) {
		List<JobCombination> combo = new ArrayList<JobCombination>();
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

	public static final int LST_SLOTS = 24 * 4;
	public static final int LST_SLOTS_MINUTES = 24 * 60 / LST_SLOTS;

	public TimeLine getPossibleTimeLine(Collection<Job> alljobs) {
		TimeLine timeline;

		HashMap<LSTTime, Vector<Job>> possibles = new HashMap<LSTTime, Vector<Job>>();

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
				Vector<Job> list;
				if (possibles.containsKey(t)) {
					list = possibles.get(t);
				} else {
					list = new Vector<Job>();
					possibles.put(t, list);
				}
				list.add(j);
				System.out.println("@" + t + " : " + j);
				possibles.put(t, list);
			}
		}

		CompatibleJobFactory compatibles = new CompatibleJobFactory(alljobs,
				guard);

		timeline = new TimeLine();
		for (Entry<LSTTime, Vector<Job>> e : possibles.entrySet()) {
			LSTTime k = e.getKey();
			Vector<Job> v = e.getValue();

			timeline.possibles.put(k, compatibles.getCombinations(v));
		}

		return timeline;

	}
}
