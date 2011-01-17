package local.radioschedulers;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

/**
 * A schedule is a timeline that, for each time slot, assigns a JobCombination
 * that is planned to be executed.
 * 
 * @author Johannes Buchner
 */
public class Schedule implements Iterable<Entry<LSTTime, JobCombination>> {
	public static final int LST_SLOTS_MINUTES = 15;
	public static final int LST_SLOTS_PER_DAY = 60 * 24 / LST_SLOTS_MINUTES;

	private Map<LSTTime, JobCombination> schedule = new TreeMap<LSTTime, JobCombination>();

	public void clear(LSTTime t) {
		schedule.remove(t);
	}

	public void add(LSTTime t, JobCombination jc) {
		schedule.put(t, jc);
	}

	public boolean isEmpty(LSTTime t) {
		if (schedule.containsKey(t))
			return false;
		else
			return true;
	}

	public JobCombination get(LSTTime t) {
		return schedule.get(t);
	}

	public LSTTime getLastEntry() {
		if (schedule.isEmpty()) {
			return new LSTTime(0, 0);
		} else {
			return Collections.max(schedule.keySet());
		}
	}

	@Override
	public Iterator<Entry<LSTTime, JobCombination>> iterator() {
		return new Iterator<Entry<LSTTime, JobCombination>>() {
			LSTTime t = new LSTTime(0L, 0L);
			LSTTime lastEntry = getLastEntry();

			@Override
			public boolean hasNext() {
				if (t.day <= lastEntry.day)
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
