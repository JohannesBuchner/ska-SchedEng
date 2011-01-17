package local.radioschedulers.run;

import java.util.Collection;

import local.radioschedulers.Proposal;
import local.radioschedulers.SchedulePossibilities;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.ga.ScheduleFitnessFunction;
import local.radioschedulers.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.ga.jgap.JGAPScheduler;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.PopulationGeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

public class RunGA {
	private static int ndays = 365 / 4;

	public static void main(String[] args) throws Exception {
		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();
		for (Proposal p : proposals) {
			System.out.println(p.toString());
		}
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		SchedulePossibilities template = tlg.schedule(proposals, ndays);
		GeneticAlgorithmScheduler scheduler = new JGAPScheduler(
				getFitnessFunction());
		scheduler.schedule(template);
	}

	private static ScheduleFitnessFunction getFitnessFunction() {
		return new SimpleScheduleFitnessFunction();
	}

	private static IProposalReader getProposalReader() throws Exception {
		// SqliteProposalReader pr = new SqliteProposalReader();
		PopulationGeneratingProposalReader pr = new PopulationGeneratingProposalReader();
		pr.fill(ndays * 4);
		return pr;
	}

}
