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
public class JobSelector {

	protected Map<Job, Double> timeleft;
	protected Map<Job, List<LSTTime>> possibles;

	public JobSelector() {
	}

	public void setTimeleft(Map<Job, Double> timeleft) {
		this.timeleft = timeleft;
	}

	/**
	 * Brings the given list in order of preference
	 * 
	 * @param list
	 *            must not be modified
	 * @return ordered list
	 */
	public Collection<JobCombination> select(Collection<JobCombination> list) {
		return pruneDone(list);
	}

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
