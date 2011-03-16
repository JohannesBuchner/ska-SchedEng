package local.radioschedulers;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * A schedule is a timeline that, for each time slot, assigns a JobCombination
 * that is planned to be executed.
 * 
 * @author Johannes Buchner
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class Schedule implements Iterable<Entry<LSTTime, JobCombination>> {
	public static final int LST_SLOTS_MINUTES = 15;
	public static final int LST_SLOTS_PER_HOUR = 60 / LST_SLOTS_MINUTES;
	public static final int LST_SLOTS_PER_DAY = LST_SLOTS_PER_HOUR * 24;

	@JsonProperty
	private Map<LSTTime, JobCombination> content = new TreeMap<LSTTime, JobCombination>();

	public void clear(LSTTime t) {
		content.remove(t);
	}

	public void add(LSTTime t, JobCombination jc) {
		content.put(t, jc);
	}

	public boolean isEmpty(LSTTime t) {
		if (content.containsKey(t))
			return false;
		else
			return true;
	}

	public JobCombination get(LSTTime t) {
		return content.get(t);
	}

	public LSTTime findLastEntry() {
		if (content.isEmpty()) {
			return new LSTTime(0, 0);
		} else {
			return Collections.max(content.keySet());
		}
	}

	@JsonIgnore
	@Override
	public Iterator<Entry<LSTTime, JobCombination>> iterator() {
		return new Iterator<Entry<LSTTime, JobCombination>>() {
			LSTTime t = new LSTTime(0L, 0L);
			LSTTime lastEntry = findLastEntry();

			@Override
			public boolean hasNext() {
				if (t.compareTo(lastEntry) <= 0)
					return true;
				else
					return false;
			}

			@Override
			public Entry<LSTTime, JobCombination> next() {
				JobCombination jc = get(new LSTTime(t.day, t.minute));
				Entry<LSTTime, JobCombination> entry = new SimpleEntry<LSTTime, JobCombination>(
						new LSTTime(t.day, t.minute), jc);
				t.minute += LST_SLOTS_MINUTES;

				if (t.minute >= 24 * 60) {
					t.day++;
					t.minute = 0L;
				}
				return entry;
			}

			@Override
			public void remove() {
				throw new Error("Not implemented.");
			}
		};
	}
}
