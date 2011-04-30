package local.radioschedulers.preschedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import local.radioschedulers.Job;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.LSTTime;
import local.radioschedulers.preschedule.date.DateRequirements;

/**
 * Makes sure the requirements are respected
 * 
 * @author Johannes Buchner
 */
public abstract class RequirementGuard {

	/**
	 * checks that the date is ok for the job
	 * 
	 * @param j
	 * @param date
	 * @return
	 */
	public boolean isDateCompatible(Job j, LSTTime date) {
		Double pref = 1.;
		if (j instanceof JobWithResources) {
			JobWithResources jr = (JobWithResources) j;
			DateRequirements d = jr.date;
			pref = d.requires(date);

			if (pref == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * remove jobs that do not like the day
	 * 
	 * @param list
	 * @param date
	 * @return
	 */
	public Collection<Job> pruneDate(Collection<Job> list, LSTTime date) {
		if (list.isEmpty())
			return list;

		List<Job> good = new ArrayList<Job>();

		for (Job j : list) {
			if (isDateCompatible(j, date))
				good.add(j);
		}
		return good;
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

	/**
	 * checks if the given list of jobs is compatible
	 * 
	 * @param list
	 * @return whether the jobs can be executed simultaneously (i.e. the
	 *         resources are not overcommitted).
	 */
	public abstract boolean compatible(Collection<Job> list);
}
