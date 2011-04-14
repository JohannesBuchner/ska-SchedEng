package local.radioschedulers.importer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import local.radioschedulers.Job;
import local.radioschedulers.Proposal;

public class SeedsGeneratingProposalReader implements IProposalReader {

	List<Proposal> proposals = new ArrayList<Proposal>();
	
	public Proposal createSimpleProposal(String name, double prio, double startlst, double endlst, double totalhours) {
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
	
	public void fill() throws Exception {
		proposals.add(createSimpleProposal("Orion Sched1", 10., 1., 6., 80L));
		proposals.add(createSimpleProposal("Taurus Sched2", 5., 5., 12., 120L));
		proposals.add(createSimpleProposal("Gemini Sched3", 7., 3., 20., 30L));
	}

	/* (non-Javadoc)
	 * @see IProposalReader#readall()
	 */
	public Collection<Proposal> readall() {
		return proposals;
	}

}
