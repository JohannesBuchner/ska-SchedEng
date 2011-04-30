/**
 * 
 */
package local.radioschedulers.ga.fitness;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import local.radioschedulers.Job;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;

public class NormalizedScheduleFitnessFunction extends
		SimpleScheduleFitnessFunction {
	private static Logger log = Logger
			.getLogger(NormalizedScheduleFitnessFunction.class);

	protected Double normalization = 1.;

	public void setupNormalization(ScheduleSpace timeline,
			Collection<Proposal> proposals) {
		Map<Double, Double> priorityHours = new TreeMap<Double, Double>();

		for (Proposal p : proposals) {
			for (Job j : p.jobs) {
				if (priorityHours.containsKey(p.priority)) {
					priorityHours.put(p.priority
							+ priorityHours.get(p.priority), j.hours);
				} else {
					priorityHours.put(p.priority, j.hours);
				}
			}
		}
		LSTTime end = timeline.findLastEntry();
		Double totalMinutes = end.day * 1. * ScheduleSpace.LST_SLOTS_MINUTES
				* ScheduleSpace.LST_SLOTS_PER_DAY + end.minute;
		Double value = 0.;
		for (Entry<Double, Double> e : priorityHours.entrySet()) {
			Double prio = e.getKey();
			Double minutes = e.getValue();
			Double part = Math.min(totalMinutes, minutes);
			// "schedule" prio for part minutes
			if (prio * part > 0) {
				value += prio * part;
			}
			totalMinutes -= part;
			if (totalMinutes <= 0)
				break;
		}
		if (value <= 0) {
			throw new IllegalStateException(
					"got a zero normalization value, perhaps no jobs or "
							+ "negative priorities/durations");
		}
		this.normalization = value;
		log.info("normalization: " + this.normalization);
	}

	@Override
	public double evaluate(Schedule s) {
		return super.evaluate(s) / normalization;
	}

}