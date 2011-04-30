package local.radioschedulers.alg.ga.watchmaker.op;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import local.radioschedulers.LSTTime;
import local.radioschedulers.LSTTimeIterator;
import local.radioschedulers.Schedule;
import local.radioschedulers.alg.ga.watchmaker.GeneticHistory;
import local.radioschedulers.alg.ga.watchmaker.MutationCounter;

import org.apache.log4j.Logger;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;

public class ScheduleCrossover extends AbstractCrossover<Schedule> {

	private boolean singleCrossover = false;
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ScheduleCrossover.class);
	public GeneticHistory<Schedule, ?> history;
	private MutationCounter<Schedule, String> counter;
	private int maxdays = 0;

	public void setSingleCrossover(boolean singleCrossover) {
		this.singleCrossover = singleCrossover;
	}

	public boolean isSingleCrossover() {
		return singleCrossover;
	}

	/**
	 * crossover, limited to this many days
	 * 
	 * @param maxdays
	 */
	public void setMaxdays(int maxdays) {
		this.maxdays = maxdays;
	}

	public int getMaxdays() {
		return maxdays;
	}

	public void setHistory(GeneticHistory<Schedule, ?> history) {
		this.history = history;
	}

	public void setCounter(MutationCounter<Schedule, String> counter) {
		this.counter = counter;
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
			if (last2.isAfter(last)) {
				last = last2;
			}
		}
		Schedule mix1 = new Schedule();
		Schedule mix2 = new Schedule();

		LSTTime a = new LSTTime(rng.nextInt(last.day.intValue()), rng
				.nextInt(Schedule.MINUTES_PER_DAY));

		LSTTime b;
		if (isSingleCrossover()) {
			b = last;
		} else {
			if (getMaxdays() == 0) {
				b = new LSTTime(rng.nextInt(last.day.intValue()), rng
						.nextInt(Schedule.MINUTES_PER_DAY));
			} else {
				b = new LSTTime(a.day.intValue() + 1 + rng.nextInt(maxdays),
						rng.nextInt(Schedule.MINUTES_PER_DAY));
			}
		}
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
		if (counter != null) {
			counter.derive(mix1, parent1);
			counter.derive(mix2, parent1);
			counter.derive(mix1, parent2);
			counter.derive(mix2, parent2);
			counter.add(mix1, this.toString(), i);
			counter.add(mix2, this.toString(), n - i);
		}

		return list;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
