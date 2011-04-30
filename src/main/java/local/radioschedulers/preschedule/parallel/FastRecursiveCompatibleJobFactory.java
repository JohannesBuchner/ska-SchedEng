package local.radioschedulers.preschedule.parallel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.preschedule.RequirementGuard;

import org.apache.log4j.Logger;

public class FastRecursiveCompatibleJobFactory extends CompatibleJobFactory {
	public FastRecursiveCompatibleJobFactory(Collection<Job> base,
			RequirementGuard req) {
		super(base, req);
	}

	static Logger log = Logger
			.getLogger(FastRecursiveCompatibleJobFactory.class);

	protected void generateAllCombinations() {
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
}
