package local.radioschedulers.alg.serial;

import java.util.List;
import java.util.Random;

import local.radioschedulers.JobCombination;

/**
 * select jobs in random order
 * 
 * @author Johannes Buchner
 */
public class RandomizedSelector extends JobSelector {

	private Random rng = new Random();

	@Override
	protected JobCombination doSelect(List<JobCombination> list) {
		return list.get(rng.nextInt(list.size()));
	}

}
