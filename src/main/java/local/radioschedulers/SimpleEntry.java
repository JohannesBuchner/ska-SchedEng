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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((k == null) ? 0 : k.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
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
		SimpleEntry<K, V> other = (SimpleEntry<K, V>) obj;
		if (k == null) {
			if (other.k != null)
				return false;
		} else if (!k.equals(other.k))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}
}
