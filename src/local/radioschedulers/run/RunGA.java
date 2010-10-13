package local.radioschedulers.run;
import java.util.Collection;

import local.radioschedulers.Proposal;
import local.radioschedulers.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.PopulationGeneratingProposalReader;

public class RunGA {

	public static void main(String[] args) throws Exception {
		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();
		for (Proposal p : proposals)
			System.out.println(p.toString());
		
		GeneticAlgorithmScheduler scheduler = new GeneticAlgorithmScheduler();
		scheduler.schedule(proposals);
	}

	private static IProposalReader getProposalReader() throws Exception {
		//SqliteProposalReader pr = new SqliteProposalReader();
		PopulationGeneratingProposalReader pr = new PopulationGeneratingProposalReader();
		pr.fill();
		return pr;
	}

}
