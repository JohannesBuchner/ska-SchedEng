package local.radioschedulers.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import local.radioschedulers.Job;
import local.radioschedulers.Proposal;

public class RandomGeneratingProposalReader implements IProposalReader {

	List<Proposal> proposals = new ArrayList<Proposal>();

	public Proposal createSimpleProposal(String name, double prio,
			double startlst, double endlst, double totalhours) {
		Proposal p = new Proposal();
		p.id = name;
		p.name = name;
		p.priority = prio;
		p.jobs = new ArrayList<Job>();
		Job j = new Job();
		j.hours = totalhours;
		j.lstmax = endlst;
		j.lstmin = startlst;
		j.id = name;
		j.proposal = p;
		p.jobs.add(j);
		return p;
	}

	public void fill(int n) throws Exception {
		Random r = new Random();
		for (int i = 0; i < n; i++) {

			proposals.add(createSimpleProposal("Prop" + i, r.nextInt(50) / 10.,
					r.nextFloat() * 24., r.nextFloat() * 24., (long) r
							.nextInt(10000)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IProposalReader#readall()
	 */
	public Collection<Proposal> readall() {
		return proposals;
	}

}
