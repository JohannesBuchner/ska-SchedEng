package local.radioschedulers.alg.lp;

import java.util.Iterator;

import local.radioschedulers.LSTTime;
import local.radioschedulers.ScheduleSpace;

/**
 * Should not be used. Only to demonstrate that a different variable order
 * introduces fragmentation.
 * 
 * @author Johannes Buchner
 */
public class DayVariableOrderParallelLinearScheduler extends
		ParallelLinearScheduler {
	protected Iterator<LSTTime> getIterator(ScheduleSpace scheduleTemplate) {
		final LSTTime last = scheduleTemplate.findLastEntry();
		return new Iterator<LSTTime>() {
			LSTTime current = new LSTTime(0, 0);

			@Override
			public boolean hasNext() {
				return current.isBefore(last);
			}

			@Override
			public LSTTime next() {
				current.day += 1;
				if (current.day > last.day) {
					current.day = 0L;
					current.minute = current.minute
							+ ScheduleSpace.LST_SLOTS_MINUTES;
				}
				return current;
			}

			@Override
			public void remove() {
				throw new Error("Not implemented.");
			}

		};
	}
}
