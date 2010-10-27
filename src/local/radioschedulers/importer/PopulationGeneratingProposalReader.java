package local.radioschedulers.importer;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import local.radioschedulers.Job;
import local.radioschedulers.Proposal;

public class PopulationGeneratingProposalReader implements IProposalReader {

	List<Proposal> proposals = new ArrayList<Proposal>();

	public void fill(int ndays) throws Exception {
		int i = 1;
		long totalhours = 0;
		Random r = new Random();

		/**
		 * give a year's worth of proposals
		 */
		for (i = 0; totalhours < ndays * 24; i++) {

			Proposal p = new Proposal();
			p.id = Integer.toString(i);
			p.name = "Proposal" + p.id;
			int startday = r.nextInt(6);
			p.start = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 13
					* startday);
			p.priority = r.nextDouble();
			p.jobs = new ArrayList<Job>();
			proposals.add(p);

			Job j = new Job();
			j.dec = r.nextDouble() * 360 - 180;
			j.ra = r.nextDouble() * 24 - 12;
			j.hours = 0L;
			while (j.hours < 4)
				j.hours = Math.round((1. / (r.nextDouble() * 200 + 1)) * 6000);
			totalhours += j.hours;
			j.proposal = p;
			
			// TODO: calculate
			j.lstmin = r.nextDouble() * 24;
			j.lstmax = (j.lstmin + 7) % 24;
			// TODO: add surveys

			// TODO: add resource requirements
			
			// TODO: something
			
			System.out.println(j);
			p.jobs.add(j);
		}
		System.out.println(i - 1 + " proposals issued.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IProposalReader#readall()
	 */
	public Collection<Proposal> readall() throws SQLException {
		return proposals;
	}
}
