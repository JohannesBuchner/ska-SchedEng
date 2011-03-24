package local.radioschedulers.ga.watchmaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import local.radioschedulers.LSTTime;
import local.radioschedulers.LSTTimeIterator;
import local.radioschedulers.Schedule;

import org.apache.log4j.Logger;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;

public class ScheduleCrossover extends AbstractCrossover<Schedule> {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ScheduleCrossover.class);
	public GeneticHistory<Schedule, ?> history;

	public void setHistory(GeneticHistory<Schedule, ?> history) {
		this.history = history;
	}

	public ScheduleCrossover(int crossoverPoints, Probability probability) {
		super(crossoverPoints, probability);
	}

	@Override
	protected List<Schedule> mate(Schedule parent1, Schedule parent2,
			int numberOfCrossoverPoints, Random rng) {
		LSTTime last = parent1.findLastEntry();
		{
			LSTTime last2 = parent2.findLastEntry();
			if (last2.compareTo(last) > 0) {
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

		int i = 0;
		int n = 0;
		for (LSTTimeIterator it = new LSTTimeIterator(last,
				Schedule.LST_SLOTS_MINUTES); it.hasNext();) {
			LSTTime t = it.next();
			if (t.isAfter(a) && t.isBefore(b)) {
				mix1.add(t, parent1.get(t));
				mix2.add(t, parent2.get(t));
				i++;
			} else {
				mix1.add(t, parent2.get(t));
				mix2.add(t, parent1.get(t));
			}
			n++;
		}

		ArrayList<Schedule> list = new ArrayList<Schedule>(2);
		list.add(mix1);
		list.add(mix2);
		if (history != null) {
			history.derive(mix1, parent1, i * 1. / n);
			history.derive(mix2, parent1, 1 - i * 1. / n);
			history.derive(mix1, parent2, 1 - i * 1. / n);
			history.derive(mix2, parent2, i * 1. / n);
		}

		return list;
	}
}
