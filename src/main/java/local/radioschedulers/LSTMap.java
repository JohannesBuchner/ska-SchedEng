package local.radioschedulers;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A replacement for {Hash,Tree}Map<LSTTime, V> that calculates the necessary
 * LSTTime objects. Saves memory in comparison (about 75% when V=Integer).
 * 
 * @author Johannes Buchner
 * 
 * @param <V>
 */
public class LSTMap<V> implements Map<LSTTime, V> {

	protected List<V> values = new ArrayList<V>();

	@Override
	public void clear() {
		values.clear();
	}

	public static int idFromTime(LSTTime t) {
		return (int) (t.minute / ScheduleSpace.LST_SLOTS_MINUTES + t.day
				* ScheduleSpace.LST_SLOTS_PER_DAY);
	}

	public static LSTTime timeFromId(int i) {
		return new LSTTime(i / ScheduleSpace.LST_SLOTS_PER_DAY,
				(i % ScheduleSpace.LST_SLOTS_PER_DAY)
						* ScheduleSpace.LST_SLOTS_MINUTES);
	}

	@Override
	public boolean containsKey(Object key) {
		int id = idFromTime((LSTTime) key);
		if (id >= 0 && id < values.size() && values.get(id) != null)
			return true;
		else
			return false;
	}

	@Override
	public boolean containsValue(Object value) {
		return values.contains(value);
	}

	@Override
	public Set<java.util.Map.Entry<LSTTime, V>> entrySet() {
		return new AbstractSet<java.util.Map.Entry<LSTTime, V>>() {

			@Override
			public Iterator<java.util.Map.Entry<LSTTime, V>> iterator() {
				final Iterator<V> it = values.iterator();
				return new Iterator<Entry<LSTTime, V>>() {

					private int i = 0;

					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@Override
					public java.util.Map.Entry<LSTTime, V> next() {
						V v = it.next();
						LSTTime k = timeFromId(i);
						i++;
						return new SimpleEntry<LSTTime, V>(k, v);
					}

					@Override
					public void remove() {
						throw new IllegalStateException("not implemented");
					}
				};
			}

			@Override
			public int size() {
				return values.size();
			}
		};
	}

	@Override
	public V get(Object key) {
		int id = idFromTime((LSTTime) key);
		if (id >= values.size()) {
			return null;
		}
		return values.get(idFromTime((LSTTime) key));
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public Set<LSTTime> keySet() {
		final int n = values.size();
		return new AbstractSet<LSTTime>() {

			@Override
			public Iterator<LSTTime> iterator() {
				return new Iterator<LSTTime>() {

					private int i = 0;

					@Override
					public boolean hasNext() {
						return i < n;
					}

					@Override
					public LSTTime next() {
						LSTTime k = timeFromId(i);
						i++;
						return k;
					}

					@Override
					public void remove() {
						throw new IllegalStateException("not implemented");
					}
				};
			}

			@Override
			public int size() {
				return values.size();
			}
		};
	}

	@Override
	public V put(LSTTime key, V value) {
		if (value == null)
			return remove(key);
		int i = idFromTime(key);
		while (i >= values.size())
			values.add(null);
		return values.set(i, value);
	}

	@Override
	public void putAll(Map<? extends LSTTime, ? extends V> m) {
		for (Entry<? extends LSTTime, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		int id = idFromTime((LSTTime) key);
		V oldval = null;
		if (id < values.size()) {
			oldval = values.set(id, null);
			if (id == values.size() - 1)
				while (!values.isEmpty()
						&& values.get(values.size() - 1) == null)
					values.remove(values.size() - 1);
		}
		return oldval;
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public Collection<V> values() {
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LSTMap<V> other = (LSTMap<V>) obj;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	public LSTMap<V> copy() {
		LSTMap<V> other = new LSTMap<V>();
		other.values.addAll(values);
		return other;
	}

	public LSTTime lastKey() {
		if (this.values.isEmpty())
			return timeFromId(0);
		else
			return timeFromId(this.values.size() - 1);
	}

}
