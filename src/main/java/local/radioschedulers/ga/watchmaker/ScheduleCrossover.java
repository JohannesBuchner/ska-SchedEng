package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;

import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;

public class ScheduleCrossover extends AbstractCrossover<Schedule> {

	public ScheduleCrossover(int crossoverPoints, Probability probability) {
		super(crossoverPoints, probability);
	}

	@Override
	protected List<Schedule> mate(Schedule parent1, Schedule parent2,
			int numberOfCrossoverPoints, Random rng) {
		LSTTime last = parent1.findLastEntry();
		{
			LSTTime last2 = parent2.findLastEntry();
			if (last2.compareTo(last) > 1) {
				last = last2;
			}
		}
		Schedule mix1 = new Schedule();
		Schedule mix2 = new Schedule();

		LSTTime a = new LSTTime(rng.nextInt(last.day.intValue()), rng
				.nextInt(Schedule.LST_SLOTS_PER_DAY));
		LSTTime b = new LSTTime(rng.nextInt(last.day.intValue()), rng
				.nextInt(Schedule.LST_SLOTS_PER_DAY));
		if (!b.isAfter(a)) {
			LSTTime tmp = b;
			b = a;
			a = tmp;
		}

		LSTTime t = new LSTTime(0L, 0L);
		while (t.compareTo(last) <= 0) {
			if (t.isAfter(a) && t.isBefore(b)) {
				mix1.add(t, parent1.get(t));
				mix2.add(t, parent2.get(t));
			} else {
				mix1.add(t, parent2.get(t));
				mix2.add(t, parent1.get(t));
			}

			t.minute += Schedule.LST_SLOTS_MINUTES;

			if (t.minute >= 24 * 60) {
				t.day++;
				t.minute = 0L;
			}
		}

		ArrayList<Schedule> list = new ArrayList<Schedule>(2);
		list.add(mix1);
		list.add(mix2);
		return list;
	}
}
