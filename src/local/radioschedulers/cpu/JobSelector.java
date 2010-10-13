package local.radioschedulers.cpu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import local.radioschedulers.Job;

/**
 * Class to order a list of jobs by execution preference
 * 
 * {@link #setTimeleft(HashMap)} has to be called after the constructor, before
 * using select
 * 
 * @author Johannes Buchner
 */
public class JobSelector {

	protected HashMap<Job, Double> timeleft;

	public JobSelector() {
	}

	public void setTimeleft(HashMap<Job, Double> timeleft) {
		this.timeleft = timeleft;
	}

	/**
	 * Brings the given list in order of preference
	 * 
	 * @param list
	 *            must not be modified
	 * @return ordered list
	 */
	public Collection<Job> select(Collection<Job> list) {
		return pruneDone(list);
	}

	/**
	 * returns elements in the list that have not completed yet.
	 */
	protected List<Job> pruneDone(Collection<Job> list) {
		List<Job> selected = new ArrayList<Job>();
		for (Job j : list) {
			if (timeleft.containsKey(j) && timeleft.get(j) > 0) {
				selected.add(j);
			}
		}
		return selected;
	}

}
