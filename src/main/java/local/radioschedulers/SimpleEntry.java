package local.radioschedulers;

import java.util.Map.Entry;

public class SimpleEntry<K, V> implements Entry<K, V> {

	private K k;
	private V v;

	public SimpleEntry(K k, V v) {
		this.k = k;
		this.v = v;
	}

	@Override
	public K getKey() {
		return this.k;
	}

	@Override
	public V getValue() {
		return this.v;
	}

	@Override
	public V setValue(V value) {
		this.v = value;
		return this.v;
	}

	@Override
	public String toString() {
		return SimpleEntry.class + "(" + getKey() + "," + getValue() + ")";
	}
}
