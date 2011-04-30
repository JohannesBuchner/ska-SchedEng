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
	public static final int LST_SLOTS_MINUTES = ScheduleSpace.LST_SLOTS_MINUTES;
	public static final int LST_SLOTS_PER_HOUR = 60 / LST_SLOTS_MINUTES;
	public static final int MINUTES_PER_DAY = 60 * 24;

	@JsonProperty
	private Map<LSTTime, JobCombination> content = new TreeMap<LSTTime, JobCombination>();

	public void clear(LSTTime t) {
		content.remove(t);
	}

	public void add(LSTTime t, JobCombination jc) {
		if (jc == null)
			throw new NullPointerException();
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
			private Iterator<LSTTime> it = new LSTTimeIterator(findLastEntry(),
					Schedule.LST_SLOTS_MINUTES);

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Entry<LSTTime, JobCombination> next() {
				LSTTime t = it.next();
				return new SimpleEntry<LSTTime, JobCombination>(t, get(t));
			}

			@Override
			public void remove() {
				throw new Error("Not implemented.");
			}
		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Schedule other = (Schedule) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		return true;
	}

}
