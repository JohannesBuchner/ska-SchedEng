package local.radioschedulers.cpu;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import local.radioschedulers.DateRequirements;
import local.radioschedulers.Job;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.LSTTime;
import local.radioschedulers.ResourceRequirements;

/**
 * Makes sure the requirements are respected
 * 
 * @author Johannes Buchner
 */
public class RequirementGuard {

	protected Map<Job, Double> datePreference;

	/**
	 * remove jobs that do not like the day
	 * 
	 * @param list
	 * @param date
	 * @return
	 */
	public Collection<Job> pruneDate(Collection<Job> list, LSTTime date) {
		datePreference = new HashMap<Job, Double>();

		if (list.isEmpty())
			return list;

		for (Job j : list) {
			Double pref = 1.;
			if (j instanceof JobWithResources) {
				JobWithResources jr = (JobWithResources) j;
				DateRequirements d = jr.date;
				pref = d.requires(date);

				if (pref == 0) {
					datePreference.remove(j);
					break;
				}
			}
			datePreference.put(j, pref);
		}
		return datePreference.keySet();
	}

	/**
	 * add jobs in order of the list, but do not accept jobs that can not be
	 * given the resource required.
	 * 
	 * @param list
	 * @return
	 */
	public Collection<Job> prune(Collection<Job> list) {
		if (list.isEmpty())
			return list;

		Vector<Job> selected = new Vector<Job>();

		for (Job j : list) {
			if (j instanceof JobWithResources) {
				JobWithResources jr = (JobWithResources) j;
				// try using it
				selected.add(jr);
				if (!compatible(selected)) {
					// no
					selected.remove(jr);
				}
			} else {
				/**
				 * here we assume that if nothing is specified, all resources
				 * are needed, and this job has to be the only one running at
				 * this time
				 */
				if (selected.isEmpty())
					selected.add(j);
				break;
			}
		}
		return selected;
	}

	private boolean compatible(Vector<Job> list) {
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
