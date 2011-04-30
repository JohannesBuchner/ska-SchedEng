package local.radioschedulers.preschedule.parallel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.preschedule.RequirementGuard;

import org.apache.log4j.Logger;

public class SimpleCompatibleJobFactory extends CompatibleJobFactory {
	public SimpleCompatibleJobFactory(Collection<Job> base, RequirementGuard req) {
		super(base, req);
	}

	private static Logger log = Logger
			.getLogger(SimpleCompatibleJobFactory.class);

	private Set<JobCombination> all;

	@Override
	protected void generateAllCombinations() {
		combinations = new HashSet<JobCombination>();
		all = new HashSet<JobCombination>();

		// the single jobs
		log.debug("adding single jobs");
		Set<JobCombination> extensions = new HashSet<JobCombination>();
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
			extensions = extend(extensions, base);
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
	 * @param productSize
	 * @return crossproduct of a x b
	 */
	private Set<JobCombination> extend(Collection<JobCombination> a,
			Collection<Job> b) {
		Set<JobCombination> successful = new HashSet<JobCombination>();

		log.debug("extension size: " + a.size());
		log.debug("base size: " + b.size());
		int ncombinations = 0;
		for (JobCombination la : a) {
			for (Job j : b) {
				if (la.jobs.contains(j)) {
					// log.debug("already in there");
					continue;
				}

				JobCombination lab = new JobCombination();
				lab.jobs.addAll(la.jobs);
				lab.jobs.add(j);
				if (all.add(lab)) {
					// log.debug("adding " + la.jobs.size()
					// + " from existing + 1 == " + lab.jobs.size());
					ncombinations++;
					if (!a.contains(lab) && guard.compatible(lab.jobs)) {
						guard.compatible(lab.jobs);
						successful.add(lab);
					}
					// } else {
					// log.debug("skipping because already done");
				}
			}
		}
		log.debug("tried out " + ncombinations);

		return successful;
	}

	@Override
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
}
