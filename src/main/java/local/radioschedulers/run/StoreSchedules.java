package local.radioschedulers.run;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.ParallelizedHeuristicsScheduleCollector;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.JsonProposalReader;
import local.radioschedulers.importer.JsonScheduleReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;

public class StoreSchedules {
	private static int ndays = 365 / 4;
	private static double oversubscriptionFactor = 0.2;
	private static Logger log = Logger.getLogger(StoreSchedules.class);

	public static void main(String[] args) throws Exception {
		if (args.length > 1)
			oversubscriptionFactor = Double.parseDouble(args[0]);
		int maxParallel = 4;
		if (args.length > 2)
			maxParallel = Integer.parseInt(args[1]);

		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();
		log.debug("creating schedule space");
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard(maxParallel));
		ScheduleSpace template = tlg.schedule(proposals, ndays);

		log.debug("created schedule space");

		log.debug("creating heuristic initial population");
		Map<IScheduler, Schedule> schedules2 = ParallelizedHeuristicsScheduleCollector
				.getStartSchedules(template);
		Map<String, Schedule> schedules = new HashMap<String, Schedule>();
		for (Entry<IScheduler, Schedule> e : schedules2.entrySet()) {
			schedules.put(e.getKey().toString(), e.getValue());
		}
		log.debug("created heuristic initial population");

		JsonScheduleReader json = new JsonScheduleReader(new File(
				"schedules.json"), new File("schedulespace.json"), proposals);
		json.write(template);
		json.write(schedules);

		ScheduleSpace space = json.readspace();
		if (!space.findLastEntry().equals(template.findLastEntry()))
			log.error("findLastEntry different");

		Map<String, Schedule> schedules3 = json.readall();
		for (Entry<String, Schedule> s : schedules3.entrySet()) {
			String name = s.getKey();
			Schedule schedule = s.getValue();
			Schedule origschedule = schedules.get(name);

			if (!schedule.findLastEntry().equals(origschedule.findLastEntry()))
				log.error("findLastEntry different for '" + name + "'");

		}
	}

	private static IProposalReader getProposalReader() throws Exception {
		// SqliteProposalReader pr = new SqliteProposalReader();
		// PopulationGeneratingProposalReader pr = new
		// PopulationGeneratingProposalReader();
		// pr.fill((int) (ndays * oversubscriptionFactor));
		JsonProposalReader pr = new JsonProposalReader(new File(
				"proposals_testset_ndays-" + ndays + "_oversubs-"
						+ oversubscriptionFactor + ".json"));
		return pr;
	}

}
