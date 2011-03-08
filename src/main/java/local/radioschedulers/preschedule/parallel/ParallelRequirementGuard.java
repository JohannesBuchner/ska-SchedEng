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
import local.radioschedulers.ResourceRequirement;
import local.radioschedulers.preschedule.RequirementGuard;

public class ParallelRequirementGuard extends RequirementGuard {

	private Integer maxParallel;

	/**
	 * infinite number of tasks are allowed in parallel as long as the
	 * ressources work out.
	 */
	public ParallelRequirementGuard() {
	}

	/**
	 * @param maxParallel
	 *            maximum number of tasks to allow in parallel
	 */
	public ParallelRequirementGuard(int maxParallel) {
		this.maxParallel = maxParallel;
	}

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
		if (maxParallel != null && list.size() > maxParallel)
			return false;
		Map<String, Integer> nresources = new HashMap<String, Integer>();
		Map<String, Set<Object>> res = new HashMap<String, Set<Object>>();

		for (Job j : list) {
			if (j instanceof JobWithResources) {
				JobWithResources jr = (JobWithResources) j;

				for (Entry<String, ResourceRequirement> r : jr.resources
						.entrySet()) {
					String key = r.getKey();
					Set<Object> req = r.getValue().possibles;
					Integer totalreq = r.getValue().numberrequired;
					if (!nresources.containsKey(key)) {
						nresources.put(key, totalreq);
						// add that this resource is needed
						res.put(key, new HashSet<Object>(req));
					} else {
						nresources.put(key, nresources.get(key) + totalreq);
						// add that this resource is needed
						res.get(key).addAll(req);

						/**
						 * we only need to check here, as
						 * 
						 * <ul>
						 * <li>one task by itself is compatible with itself</li>
						 * <li>resource sets are independent</li>
						 * </ul>
						 * 
						 * We check whether every task can get one item for
						 * itself. This is basically the pigeonhole principle:
						 * if we have more items to assign than we need, we
						 * succeed.
						 */
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
