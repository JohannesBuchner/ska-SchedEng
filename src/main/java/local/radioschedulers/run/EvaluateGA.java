package local.radioschedulers.run;

import java.io.PrintStream;
import java.util.Collection;

import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.HeuristicsScheduleCollector;
import local.radioschedulers.ga.ScheduleFitnessFunction;
import local.radioschedulers.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.ga.jgap.JGAPScheduler;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.PopulationGeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;

public class EvaluateGA {
	private static int ndays = 365 / 4;
	private static double oversubscriptionFactor = 3;

	private static Logger log = Logger.getLogger(EvaluateGA.class);

	public static void main(String[] args) throws Exception {
		oversubscriptionFactor = Double.parseDouble(args[1]);
		int maxParallel = Integer.parseInt(args[2]);
		
		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();
		for (Proposal p : proposals) {
			System.out.println(p.toString());
		}
		ScheduleFitnessFunction f = getFitnessFunction();

		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard(maxParallel));
		ScheduleSpace template = tlg.schedule(proposals, ndays);

		log.debug("created schedule space");
		if (true)
			return;

		GeneticAlgorithmScheduler scheduler = new JGAPScheduler(f);
		scheduler.setNumberOfGenerations(1);
		scheduler.setEliteSize(0);
		scheduler.setCrossoverProbability(0);
		scheduler.setMutationProbability(0);
		scheduler.setPopulationSize(30);
		scheduler.setPopulation(HeuristicsScheduleCollector
				.getStartSchedules(template));

		PrintStream p = new PrintStream("ga-populations.txt");
		for (int i = 0; i < 10000 / scheduler.getPopulationSize(); i++) {
			scheduler.schedule(template);
			for (Schedule s : scheduler.getPopulation()) {
				double v = f.evaluate(s);
				p.println(i + "\t" + v);
			}
		}
	}

	private static ScheduleFitnessFunction getFitnessFunction() {
		return new SimpleScheduleFitnessFunction();
	}

	private static IProposalReader getProposalReader() throws Exception {
		// SqliteProposalReader pr = new SqliteProposalReader();
		PopulationGeneratingProposalReader pr = new PopulationGeneratingProposalReader();
		pr.fill((int) (ndays * oversubscriptionFactor));
		return pr;
	}

}
