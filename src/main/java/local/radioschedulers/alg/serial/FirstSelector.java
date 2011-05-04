package local.radioschedulers.alg.serial;

import java.util.List;

import local.radioschedulers.JobCombination;

/**
 * Simply selects the first available JobCombination
 * 
 * @author Johannes Buchner
 */
public class FirstSelector extends JobSelector {
	@Override
	protected JobCombination doSelect(List<JobCombination> list) {
		return list.get(0);
	}
}
