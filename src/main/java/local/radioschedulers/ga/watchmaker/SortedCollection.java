package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * This class returns, given a mapping function that provides a mapping to a
 * Comparable, a sorted Collection.
 * 
 * @author Johannes Buchner
 * 
 * @param <T>
 *            datatype of the Collection
 */
@SuppressWarnings("unchecked")
public class SortedCollection<T> implements Iterable<T> {

	public static interface MappingFunction<T, V extends Comparable> {
		public abstract V map(T item);
	}

	private Collection<T> sortedItems;

	/**
	 * Sort unsortedItems using the mapping function f (ascending). Result is
	 * stored and can be retrieved using iterator()
	 * 
	 * @param <V>
	 *            Datatype f maps to
	 * @param unsortedItems
	 *            unsorted collection
	 * @param f
	 *            mapping function
	 */
	public <V extends Comparable> SortedCollection(Collection<T> unsortedItems,
			final MappingFunction<T, V> f) {
		// Map<V, T> items = new TreeMap<V, T>();
		// for (T i : unsortedItems) {
		// items.put(f.map(i), i);
		// }
		// this.sortedItems = items.values();
		ArrayList<T> items = new ArrayList<T>(unsortedItems);
		Collections.sort(items, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return f.map(o1).compareTo(f.map(o2));
			}
		});
		this.sortedItems = items;
	}

	@Override
	public Iterator<T> iterator() {
		return sortedItems.iterator();
	}

	/**
	 * @return the top-ranked item
	 */
	public T first() {
		Iterator<T> it = sortedItems.iterator();
		if (it.hasNext())
			return it.next();
		else
			return null;
	}

}
