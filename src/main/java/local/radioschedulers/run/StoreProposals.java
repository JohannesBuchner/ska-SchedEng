package local.radioschedulers.run;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import local.radioschedulers.Job;
import local.radioschedulers.Proposal;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.JsonProposalReader;
import local.radioschedulers.importer.PopulationGeneratingProposalReader;

import org.apache.log4j.Logger;

public class StoreProposals {
	private static int ndays = 365 / 4;
	private static double oversubscriptionFactor = 0.2;
	private static Logger log = Logger.getLogger(StoreProposals.class);

	public static void main(String[] args) throws Exception {
		if (args.length == 1)
			oversubscriptionFactor = Double.parseDouble(args[0]);
		PropertiesContext.addReplacement("ndays", ndays + "");
		PropertiesContext.addReplacement("oversubs", oversubscriptionFactor
				+ "");

		IProposalReader pr = getProposalReader();
		Collection<Proposal> proposals = pr.readall();

		JsonProposalReader json = new JsonProposalReader(new File(
				PropertiesContext.proposalsFilename()));
		json.write(proposals);

		checkProposals(proposals, json.readall());
	}

	private static IProposalReader getProposalReader() throws Exception {
		try {
			File f = new File(PropertiesContext.proposalsFilename());
			if (f.exists()) {
				return new JsonProposalReader(f);
			}
		} catch (Exception e) {
			log.error(e);
		}

		PopulationGeneratingProposalReader pr = new PopulationGeneratingProposalReader();
		pr.fill((int) (ndays * oversubscriptionFactor));
		return pr;
	}

	private static void checkProposals(Collection<Proposal> proposals,
			Collection<Proposal> proposals2) {
		if (proposals.size() != proposals2.size())
			log.warn("proposals are different!");
		List<Proposal> a = new ArrayList<Proposal>(proposals);
		List<Proposal> b = new ArrayList<Proposal>(proposals2);
		// for (Proposal p : propso)
		for (int i = 0; i < proposals.size(); i++) {
			// log.debug("Original Proposal " + i + " -- " + a.get(i).getClass()
			// + " -- " + a.get(i));
			// log.debug("Read     Proposal " + i + " -- " + b.get(i).getClass()
			// + " -- " + b.get(i));
			Proposal p1 = a.get(i);

			Proposal p2 = b.get(i);
			if (p1.jobs.size() != p2.jobs.size())
				log.warn("jobs of proposal are different!" + p1 + " vs " + p2);

			List<Job> c = new ArrayList<Job>(p1.jobs);
			List<Job> d = new ArrayList<Job>(p2.jobs);
			for (int j = 0; j < p1.jobs.size(); j++) {
				Job j1 = c.get(j);
				Job j2 = d.get(j);

				if (!j1.hours.equals(j2.hours)) {
					log.warn("job hours are different! " + j1 + " vs " + j2);
				}
				if (Double.compare(j1.lstmax, j2.lstmax) != 0
						|| Double.compare(j1.lstmin, j2.lstmin) != 0) {
					log.warn("job lst range is different! " + j1 + " vs " + j2);
				}

			}
		}

	}
}
