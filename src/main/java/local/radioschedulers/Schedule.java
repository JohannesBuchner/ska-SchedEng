package local.radioschedulers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Schedule {
	private Map<LSTTime, List<Job>> schedule = new TreeMap<LSTTime, List<Job>>();

	private void createIfNeeded(LSTTime t) {
		if (!schedule.containsKey(t)) {
			schedule.put(t, new ArrayList<Job>(1));
		}
	}

	public void clear(LSTTime t) {
		createIfNeeded(t);
		schedule.get(t).clear();
	}

	public void add(LSTTime t, Job j) {
		createIfNeeded(t);
		schedule.get(t).add(j);
	}

	public boolean isEmpty(LSTTime t) {
		if (schedule.containsKey(t))
			return schedule.get(t).isEmpty();
		else
			return true;
	}

	public List<Job> get(LSTTime t) {
		createIfNeeded(t);
		return schedule.get(t);
	}
	
	public LSTTime getLastEntry() {
		return Collections.max(schedule.keySet());	
	}
}
