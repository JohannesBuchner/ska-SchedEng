package local.radioschedulers.alg.ga.watchmaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Schedule;

import org.uncommons.watchmaker.framework.EvaluatedCandidate;

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

	private Map<K, Map<V, Integer>> counts = new HashMap<K, Map<V, Integer>>();

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

	public void retain(Collection<K> keys) {
		List<K> toRemove = new ArrayList<K>();
		for (K key : counts.keySet()) {
			if (!keys.contains(key))
				toRemove.add(key);
		}
		for (K key : toRemove) {
			counts.remove(key);
		}
	}

	public void retain(List<EvaluatedCandidate<Schedule>> pop) {
		Set<K> toRemove = new HashSet<K>(counts.keySet());

		for (EvaluatedCandidate<Schedule> c : pop) {
			toRemove.remove(c.getCandidate());
		}
		for (K key : toRemove) {
			counts.remove(key);
		}
	}
}
