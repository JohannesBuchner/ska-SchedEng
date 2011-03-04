package local.radioschedulers.importer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Histogram<T extends Comparable<T>> implements Iterable<Bin<T>> {

	private List<Bin<T>> bins = new ArrayList<Bin<T>>();

	public void addBin(T low, T high) {
		this.bins.add(new Bin<T>(low, high, 0.));
	}

	public void addItem(T item) {
		addItem(item, 1.);
	}

	public void addItem(T item, Double weight) {
		Bin<T> bin = this.getBin(item);
		if (bin == null)
			throw new NoSuchElementException("No bin found for item " + item);
		bin.increase(weight);
	}

	private Bin<T> getBin(T item) {
		for (Bin<T> bin : bins) {
			if (item.compareTo(bin.low) >= 0 && item.compareTo(bin.high) < 0) {
				return bin;
			}
		}
		return null;
	}

	public void normalize() {
		Double sum = 0.;
		for (Bin<T> bin : bins) {
			sum+=bin.getCount();
		}
		for (Bin<T> bin : bins) {
			bin.setCount(bin.getCount() / sum);
		}
	}

	@Override
	public Iterator<Bin<T>> iterator() {
		return bins.iterator();
	}
}
