package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Schedule;

import org.apache.log4j.Logger;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;

public class MutationCounter<K, V> {
	private static Logger log = Logger.getLogger(MutationCounter.class);

	private Map<K, Map<V, Integer>> properties = new HashMap<K, Map<V, Integer>>();

	private Map<V, Integer> ensureKnown(K key) {
		if (!properties.containsKey(key)) {
			Map<V, Integer> m = new HashMap<V, Integer>();
			properties.put(key, m);
			return m;
		} else {
			return properties.get(key);
		}
	}

	public void derive(K newKey, K parent) {
		if (properties.containsKey(parent) && !properties.get(parent).isEmpty()) {
			Map<V, Integer> p = ensureKnown(newKey);
			for (Entry<V, Integer> e : properties.get(parent).entrySet()) {
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
		return properties.get(key);
	}

	public void retain(Collection<K> keys) {
		List<K> toRemove = new ArrayList<K>();
		for (K key : properties.keySet()) {
			if (!keys.contains(key))
				toRemove.add(key);
		}
		for (K key : toRemove) {
			properties.remove(key);
		}
	}

	public void retain(List<EvaluatedCandidate<Schedule>> pop) {
		Set<K> toRemove = new HashSet<K>(properties.keySet());

		for (EvaluatedCandidate<Schedule> c : pop) {
			toRemove.remove(c.getCandidate());
		}
		for (K key : toRemove) {
			properties.remove(key);
		}
	}
}
