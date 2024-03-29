package local.radioschedulers.api;

import java.util.List;
import java.util.Set;

import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.alg.ga.ScheduleFitnessFunction;
import local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.alg.ga.watchmaker.WFScheduler;
import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;

public class SchedulePipeline {

	/**
	 * @param proposals
	 *            should contain priority information, jobs with hours, resource
	 *            and date information
	 * @param req
	 *            A requirement guard that can be called to determine if a
	 *            subset of jobs are compatible
	 * @return the ScheduleSpace
	 */
	public ScheduleSpace getScheduleSpace(Set<Proposal> proposals,
			RequirementGuard req, int ndays) {
		SimpleTimelineGenerator stg = new SimpleTimelineGenerator(req);
		return stg.schedule(proposals, ndays);
	}

	/**
	 * evolve and generate new schedules
	 * 
	 * @param space
	 *            the space to work in
	 * @param priorSchedules
	 *            existing schedules to use. These do not have to fit into the
	 *            ScheduleSpace, only the parts that fit will be used.
	 * @return schedules generated.
	 * @throws Exception
	 */
	public List<Schedule> generateSchedules(ScheduleSpace space,
			List<Schedule> priorSchedules) throws Exception {
		GeneticAlgorithmScheduler scheduler;
		try {
			scheduler = new WFScheduler(getFitnessFunction());
			scheduler.setPopulation(priorSchedules);
			scheduler.schedule(space);
			return scheduler.getPopulation();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private ScheduleFitnessFunction getFitnessFunction() {
		return new SimpleScheduleFitnessFunction();
	}
}
