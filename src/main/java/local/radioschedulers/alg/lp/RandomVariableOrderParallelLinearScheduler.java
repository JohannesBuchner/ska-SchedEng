package local.radioschedulers.alg.lp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import local.radioschedulers.LSTTime;
import local.radioschedulers.ScheduleSpace;

/**
 * Should not be used. Only to demonstrate that a different variable order
 * introduces fragmentation.
 * 
 * 
 * @author Johannes Buchner
 */
public class RandomVariableOrderParallelLinearScheduler extends ParallelLinearScheduler {
	@Override
	protected Iterator<LSTTime> getIterator(ScheduleSpace scheduleTemplate) {
		final LSTTime last = scheduleTemplate.findLastEntry();
		final long n = (last.day + 1) * ScheduleSpace.LST_SLOTS_PER_DAY;
		final ArrayList<Integer> a = new ArrayList<Integer>();
		for (int i= 0; i < n ; i++) {
			a.add(i);
		}
		Collections.shuffle(a);
		
		return new Iterator<LSTTime>() {
			Iterator<Integer> it = a.iterator();
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public LSTTime next() {
				Integer i = it.next();
				return new LSTTime(i / ScheduleSpace.LST_SLOTS_PER_DAY, (i % ScheduleSpace.LST_SLOTS_PER_DAY) * ScheduleSpace.LST_SLOTS_MINUTES);
			}

			@Override
			public void remove() {
				throw new Error("Not implemented.");
			}
			
		};
	}
}
