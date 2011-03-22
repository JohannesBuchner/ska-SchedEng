package local.radioschedulers.ga.watchmaker;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GeneticHistory<K, V> {

	Map<K, Map<V, Double>> properties = new HashMap<K, Map<V, Double>>();

	public void initiated(K key, V property) {
		ensureKnown(key).put(property, 1.);
	}

	private Map<V, Double> ensureKnown(K key) {
		if (!properties.containsKey(key)) {
			Map<V, Double> m = new HashMap<V, Double>();
			properties.put(key, m);
			return m;
		} else {
			return properties.get(key);
		}
	}

	public void derive(K newKey, K parent, Double parts) {
		Map<V, Double> p = ensureKnown(newKey);
		for (Entry<V, Double> e : properties.get(parent).entrySet()) {
			p.put(e.getKey(), e.getValue() * parts);
		}
	}
}
