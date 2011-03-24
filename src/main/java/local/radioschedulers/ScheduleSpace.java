package local.radioschedulers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * A schedule space is a timeline that, for each time slot, defines a list of
 * JobCombinations that could be executed.
 * 
 * @author Johannes Buchner
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class ScheduleSpace implements Iterable<Entry<LSTTime, Set<JobCombination>>> {
	public static final int LST_SLOTS_MINUTES = 15;
	public static final int LST_SLOTS_PER_DAY = 60 * 24 / LST_SLOTS_MINUTES;

	private Map<LSTTime, JobCombinationChoice> possibles = new TreeMap<LSTTime, JobCombinationChoice>();

	/*
	 * this stupid intermediate class is needed for JSON export. Sorry.
	 */
	@SuppressWarnings("serial")
	public static class JobCombinationChoice extends HashSet<JobCombination> {
	}
	
	private void createIfNeeded(LSTTime t) {
		if (!possibles.containsKey(t)) {
			possibles.put(t, new JobCombinationChoice());
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
		return Collections.max(possibles.keySet());
	}
	
	public Map<LSTTime, JobCombinationChoice> getPossibles() {
		return possibles;
	}
	
	@JsonIgnore
	@Override
	public Iterator<Entry<LSTTime, Set<JobCombination>>> iterator() {
		return new Iterator<Entry<LSTTime, Set<JobCombination>>>() {
			private Iterator<LSTTime> it = new LSTTimeIterator(findLastEntry(),
					Schedule.LST_SLOTS_MINUTES);

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Entry<LSTTime, Set<JobCombination>> next() {
				LSTTime t = it.next();
				return new SimpleEntry<LSTTime, Set<JobCombination>>(t, get(t));
			}

			@Override
			public void remove() {
				throw new Error("Not implemented.");
			}
		};
	}
}
