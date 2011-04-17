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

/**
 * The genetic history inherits children with their parents properties,
 * while noting the parents influence.
 * 
 * @author Johannes Buchner
 *
 * @param <K> chromosome type
 * @param <V> property type
 */
public class GeneticHistory<K, V> {
	private static Logger log = Logger.getLogger(GeneticHistory.class);

	private Map<K, Map<V, Double>> properties = new HashMap<K, Map<V, Double>>();

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
