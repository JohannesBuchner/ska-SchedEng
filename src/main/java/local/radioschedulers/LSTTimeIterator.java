package local.radioschedulers;

import java.util.Iterator;

public class LSTTimeIterator implements Iterator<LSTTime> {
	private LSTTime next;
	private LSTTime last;
	private final int minuteSteps;

	public LSTTimeIterator(LSTTime last, int minuteSteps) {
		this(new LSTTime(0, 0), last, minuteSteps);
	}

	public LSTTimeIterator(LSTTime start, LSTTime last, int minuteSteps) {
		this.last = last;
		this.minuteSteps = minuteSteps;
		this.next = new LSTTime(start.day, start.minute);
	}

	@Override
	public boolean hasNext() {
		if (minuteSteps > 0 && next.isBeforeOrEqual(last))
			return true;
		else if (minuteSteps < 0 && next.isAfterOrEqual(last))
			return true;
		else
			return false;
	}

	/**
	 * returns current value, then forwards internal current value
	 * 
	 * @return current value
	 */
	@Override
	public LSTTime next() {
		LSTTime entry = new LSTTime(next.day, next.minute);
		next.minute += minuteSteps;

		if (next.minute < 0) {
			next.day--;
			next.minute += 24 * 60;
		} else if (next.minute >= 24 * 60) {
			next.day++;
			next.minute -= 24 * 60;
		}
		return entry;
	}

	@Override
	public void remove() {
		throw new Error("Not implemented.");
	}

}
