package local.radioschedulers.greedy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.LSTTime;
import local.radioschedulers.ga.watchmaker.SortedCollection.MappingFunction;

public abstract class JobSortCriterion {
	protected Map<Job, Collection<LSTTime>> possibleSlots = new HashMap<Job, Collection<LSTTime>>();

	public void setPossibleSlots(Map<Job, Collection<LSTTime>> possibleSlots) {
		this.possibleSlots = possibleSlots;
	}

	public abstract MappingFunction<Job, Double> getSortFunction();

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
