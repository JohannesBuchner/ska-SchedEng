package local.radioschedulers.alg.ga.watchmaker;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;

/**
 * The mutation counter keeps track of to how many genes an operator was
 * applied, for each individual, over its lifetime. The counters are inherited
 * to the children through {@link #derive(Object, Object)}.
 * 
 * @author Johannes Buchner
 * 
 * @param <K>
 *            chromosome type
 * @param <V>
 *            MutationOperator type
 */
public class MutationCounter<K, V> {

	/*
	 * weak key map; element is garbage-collected when key is not referenced
	 * anymore.
	 */
	private final transient ConcurrentMap<K, Map<V, Integer>> counts = new MapMaker()
			.weakKeys().makeMap();

	private Map<V, Integer> ensureKnown(K key) {
		if (!counts.containsKey(key)) {
			Map<V, Integer> m = new HashMap<V, Integer>();
			counts.put(key, m);
			return m;
		} else {
			return counts.get(key);
		}
	}

	public void derive(K newKey, K parent) {
		if (counts.containsKey(parent) && !counts.get(parent).isEmpty()) {
			Map<V, Integer> p = ensureKnown(newKey);
			for (Entry<V, Integer> e : counts.get(parent).entrySet()) {
				if (p.containsKey(e.getKey())) {
					p.put(e.getKey(), p.get(e.getKey()) + e.getValue());
				} else {
					p.put(e.getKey(), e.getValue());
				}
			}
		}
	}

	public void add(K newKey, V op, Integer count) {
		Map<V, Integer> p = ensureKnown(newKey);
		Integer oldcount = p.get(op);
		if (oldcount == null) {
			oldcount = 0;
		}
		p.put(op, oldcount + count);
	}

	public Map<V, Integer> getProperties(K key) {
		return counts.get(key);
	}
}
