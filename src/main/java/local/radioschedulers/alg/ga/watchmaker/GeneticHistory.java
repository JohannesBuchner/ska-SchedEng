package local.radioschedulers.alg.ga.watchmaker;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.google.common.collect.MapMaker;

/**
 * The genetic history inherits children with their parents properties, while
 * noting the parents influence.
 * 
 * @author Johannes Buchner
 * 
 * @param <K>
 *            chromosome type
 * @param <V>
 *            property type
 */
public class GeneticHistory<K, V> {
	private static Logger log = Logger.getLogger(GeneticHistory.class);

	/*
	 * weak key map; element is garbage-collected when key is not referenced
	 * anymore.
	 */
	private final transient ConcurrentMap<K, Map<V, Double>> properties = new MapMaker()
			.weakKeys().makeMap();

	public void initiated(K key, V property) {
		if (log.isDebugEnabled())
			log.debug("adding initial member with property '" + property + "'");
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
		if (properties.containsKey(parent) && !properties.get(parent).isEmpty()) {
			Map<V, Double> p = ensureKnown(newKey);
			for (Entry<V, Double> e : properties.get(parent).entrySet()) {
				if (log.isDebugEnabled())
					log.debug("handing over " + e.getKey() + " " + e.getValue()
							+ " --> " + e.getValue() * parts);
				if (p.containsKey(e.getKey())) {
					p.put(e.getKey(), p.get(e.getKey()) + e.getValue() * parts);
				} else {
					p.put(e.getKey(), e.getValue() * parts);
				}
			}
		}
	}

	public Map<V, Double> getProperties(K key) {
		return properties.get(key);
	}
}
