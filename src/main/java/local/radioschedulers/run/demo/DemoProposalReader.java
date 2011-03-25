package local.radioschedulers.run.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import local.radioschedulers.Job;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.Proposal;
import local.radioschedulers.ResourceRequirement;
import local.radioschedulers.importer.IProposalReader;

public class DemoProposalReader implements IProposalReader {

	List<Proposal> proposals = new ArrayList<Proposal>();

	public DemoProposalReader() {
		JobWithResources jwr = new JobWithResources();
		ResourceRequirement req = new ResourceRequirement();
		req.possibles.add("12m");
		req.numberrequired = 1;
		jwr.resources.put("antennas", req);
		req = new ResourceRequirement();
		req.possibles.add("A");
		req.possibles.add("B");
		req.numberrequired = 1;
		jwr.resources.put("backends", req);
		proposals.add(createSimpleProposal("Less Important", 1., 2., 20., 100L,
				jwr));

		jwr = new JobWithResources();
		req = new ResourceRequirement();
		req.possibles.add("12m");
		req.numberrequired = 1;
		jwr.resources.put("antennas", req);
		req = new ResourceRequirement();
		req.possibles.add("A");
		req.numberrequired = 1;
		jwr.resources.put("backends", req);
		proposals.add(createSimpleProposal("Very Important", 5., 1., 14., 60L,
				jwr));
	}

	private static int id = 0;

	private static int getNextId() {
		return ++id;
	}

	private static String getNextIdAsString() {
		return Integer.toString(getNextId());
	}

	public static Proposal createSimpleProposal(String name, double prio,
			double startlst, double endlst, Long totalhours, Job j) {
		Proposal p = new Proposal();
		p.id = getNextIdAsString();
		p.name = name + " " + p.id;
		p.priority = prio;
		p.jobs = new ArrayList<Job>();
		j.hours = totalhours;
		j.lstmax = endlst;
		j.lstmin = startlst;
		j.id = p.name;
		j.proposal = p;
		p.jobs.add(j);
		return p;
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
