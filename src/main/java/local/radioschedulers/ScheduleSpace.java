package local.radioschedulers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * A schedule space is a timeline that, for each time slot, defines a list of
 * JobCombinations that could be executed.
 * 
 * @author Johannes Buchner
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class ScheduleSpace implements
		Iterable<Entry<LSTTime, Set<JobCombination>>> {
	public static final int LST_SLOTS_MINUTES = 15;
	public static final int LST_SLOTS_PER_DAY = 60 * 24 / LST_SLOTS_MINUTES;

	@JsonProperty
	private LSTMap<Set<JobCombination>> possibles = new LSTMap<Set<JobCombination>>();

	private void createIfNeeded(LSTTime t) {
		if (!possibles.containsKey(t)) {
			possibles.put(t, new HashSet<JobCombination>());
		}
	}

	public void clear(LSTTime t) {
		possibles.remove(t);
	}

	public void add(LSTTime t, JobCombination jc) {
		createIfNeeded(t);
		possibles.get(t).add(jc);
	}

	public boolean isEmpty(LSTTime t) {
		if (possibles.containsKey(t))
			return possibles.get(t).isEmpty();
		else
			return true;
	}

	public Set<JobCombination> get(LSTTime t) {
		createIfNeeded(t);
		return possibles.get(t);
	}

	public LSTTime findLastEntry() {
		return possibles.lastKey();
	}

	Map<LSTTime, Set<JobCombination>> getPossibles() {
		return possibles;
	}

	void setPossibles(Map<LSTTime, Set<JobCombination>> possibles) {
		this.possibles.clear();
		this.possibles.putAll(possibles);
	}

	@JsonIgnore
	@Override
	public Iterator<Entry<LSTTime, Set<JobCombination>>> iterator() {
		return this.possibles.entrySet().iterator();
	}
}
