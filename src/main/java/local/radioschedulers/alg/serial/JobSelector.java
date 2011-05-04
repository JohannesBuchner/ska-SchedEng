package local.radioschedulers.alg.serial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;

/**
 * Class to order a list of jobs by execution preference
 * 
 * {@link #setTimeleft(HashMap)} has to be called after the constructor, before
 * using select
 * 
 * @author Johannes Buchner
 */
public abstract class JobSelector {

	protected Map<Job, Double> timeleft;
	protected Map<Job, List<LSTTime>> possibles;

	public JobSelector() {
	}

	public void setTimeleft(Map<Job, Double> timeleft) {
		this.timeleft = timeleft;
	}

	/**
	 * Selects a non-completed job from the list by preference
	 * 
	 * @param list
	 *            must not be modified
	 * @return ordered list
	 */
	public JobCombination select(Collection<JobCombination> list) {
		List<JobCombination> unfinished = pruneDone(list);
		if (unfinished.isEmpty())
			return null;
		if (unfinished.size() == 1)
			return unfinished.get(0);
		else
			return doSelect(unfinished);
	}

	/**
	 * Selects a job from the list by preference
	 * 
	 * @param list
	 *            list of jobs to choose from. is guarranteed to contain at
	 *            least 2 items
	 * @return preferred job
	 */
	protected abstract JobCombination doSelect(List<JobCombination> list);

	/**
	 * returns elements in the list that have not completed yet.
	 */
	protected List<JobCombination> pruneDone(Collection<JobCombination> list) {
		List<JobCombination> selected = new ArrayList<JobCombination>();
		for (JobCombination jc : list) {
			boolean complete = false;
			for (Job j : jc.jobs) {
				if (!timeleft.containsKey(j) || timeleft.get(j) <= 0) {
					complete = true;
				}
			}
			if (!complete)
				selected.add(jc);
		}
		return selected;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public void setPossibles(Map<Job, List<LSTTime>> possibles) {
		this.possibles = possibles;
	}

}
