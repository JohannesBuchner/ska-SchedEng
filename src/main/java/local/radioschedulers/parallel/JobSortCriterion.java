package local.radioschedulers.parallel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.LSTTime;
import local.radioschedulers.ga.watchmaker.SortedCollection.MappingFunction;

public abstract class JobSortCriterion {
	protected Map<Job, List<LSTTime>> possibleSlots = new HashMap<Job, List<LSTTime>>();

	public void setPossibleSlots(Map<Job, List<LSTTime>> possibleSlots) {
		this.possibleSlots = possibleSlots;
	}

	public abstract MappingFunction<Job, Double> getSortFunction();

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
