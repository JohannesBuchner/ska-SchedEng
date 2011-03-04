/**
 * 
 */
package local.radioschedulers.importer;

public class Bin<T> {
	T low;
	T high;
	private Double count;

	public Bin(T low, T high, Double count) {
		this.low = low;
		this.high = high;
		this.count = count;
	}

	public Double getCount() {
		return this.count;
	}
	
	public void setCount(Double count) {
		this.count = count;
	}

	public T getLow() {
		return this.low;
	}

	public T getHigh() {
		return this.high;
	}

	public void increase(Double weight) {
		this.count += weight;
	}
}