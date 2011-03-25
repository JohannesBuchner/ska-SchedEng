package local.radioschedulers;

import java.util.Iterator;

public class LSTTimeIterator implements Iterator<LSTTime> {
	private LSTTime current;
	private LSTTime last;
	private final int minuteSteps;

	public LSTTimeIterator(LSTTime last, int minuteSteps) {
		this.last = last;
		this.minuteSteps = minuteSteps;
		this.current = new LSTTime(0, 0);
	}

	@Override
	public boolean hasNext() {
		if (current.compareTo(last) <= 0)
			return true;
		else
			return false;
	}

	@Override
	public LSTTime next() {
		LSTTime entry = new LSTTime(current.day, current.minute);
		current.minute += minuteSteps;

		if (current.minute >= 24 * 60) {
			current.day++;
			current.minute = 0L;
		}
		return entry;
	}

	public LSTTime prev() {
		LSTTime entry = new LSTTime(current.day, current.minute);
		current.minute -= minuteSteps;

		if (current.minute < 0) {
			current.day--;
			current.minute += 24 * 60;
		}
		return entry;
	}

	@Override
	public void remove() {
		throw new Error("Not implemented.");
	}

}
