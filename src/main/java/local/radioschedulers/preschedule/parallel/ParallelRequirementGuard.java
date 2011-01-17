package local.radioschedulers.preschedule.parallel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.LSTTime;
import local.radioschedulers.ResourceRequirements;
import local.radioschedulers.preschedule.RequirementGuard;

public class ParallelRequirementGuard extends RequirementGuard {

	/**
	 * ensures that all the elements in the JobCombination like the date
	 * 
	 * @see RequirementGuard#isDateCompatible(Job, LSTTime)
	 * 
	 * @param jc
	 * @param date
	 * @return
	 */
	public boolean isDateCompatible(JobCombination jc, LSTTime date) {
		boolean all = true;
		for (Job j : jc.jobs) {
			all = all & super.isDateCompatible(j, date);
		}
		return all;
	}

	@Override
	public boolean compatible(Collection<Job> list) {
		Map<String, Integer> nresources = new HashMap<String, Integer>();
		Map<String, Set<Integer>> res = new HashMap<String, Set<Integer>>();

		for (Job j : list) {
			if (j instanceof JobWithResources) {
				JobWithResources jr = (JobWithResources) j;

				for (Entry<String, ResourceRequirements> r : jr.resources
						.entrySet()) {
					String key = r.getKey();
					Map<Integer, Double> req = r.getValue().requirements;
					Integer totalreq = r.getValue().totalRequired();
					if (!nresources.containsKey(r.getKey())) {
						nresources.put(key, totalreq);
						res.put(key, new HashSet<Integer>(req.keySet()));
					} else {

						nresources.put(key, nresources.get(key) + totalreq);
						res.get(key).addAll(req.keySet());

						if (res.get(key).size() < nresources.get(key)) {
							// shortage.
							return false;
						}
					}
				}
			}
		}
		return true;
	}

}
