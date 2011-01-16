package local.radioschedulers.importer;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import local.radioschedulers.Job;
import local.radioschedulers.Proposal;

public class GeneratingProposalReader implements IProposalReader {

	List<Proposal> proposals = new ArrayList<Proposal>();
	
	public void fill() throws Exception {
		
		Proposal p1 = new Proposal();
		p1.id = "1";
		p1.name = "foo";
		p1.start = new Date(System.currentTimeMillis());
		p1.jobs = new ArrayList<Job>();
		proposals.add(p1);
		
		Proposal p2 = new Proposal();
		p2.id = "2";
		p2.name = "bar";
		p2.start = new Date(System.currentTimeMillis() +3 * 24*60*60*1000);
		p2.jobs = new ArrayList<Job>();
		proposals.add(p2);
		
		Proposal p3 = new Proposal();
		p3.id = "3";
		p3.name = "baz";
		p3.start = new Date(System.currentTimeMillis());
		p3.jobs = new ArrayList<Job>();
		proposals.add(p3);
		
		Job j = new Job();
		j.proposal = p1;
		j.hours = 100L;
		j.ra = -(5 + 35. / 60 + 30. / 60. / 60.);
		j.dec = +(5 + 35. / 60 + 30. / 60. / 60.);
		j.proposal.jobs.add(j);
		
		j = new Job();
		j.proposal = p1;
		j.hours = 40L;
		j.ra = -(5 + 25. / 60 + 30. / 60. / 60.);
		j.dec = +(5 + 45. / 60 + 30. / 60. / 60.);
		j.proposal.jobs.add(j);
		
		j = new Job();
		j.proposal = p1;
		j.hours = 10L;
		j.ra = -(5 + 25. / 60 + 30. / 60. / 60.);
		j.dec = +(5 + 35. / 60 + 30. / 60. / 60.);
		j.proposal.jobs.add(j);
		
		j = new Job();
		j.proposal = p2;
		j.hours = 100L;
		j.ra = -(12 + 35. / 60 + 30. / 60. / 60.);
		j.dec = +(15 + 35. / 60 + 30. / 60. / 60.);
		j.proposal.jobs.add(j);

		j = new Job();
		j.proposal = p3;
		j.hours = 40L;
		j.ra = -(20 + 35. / 60 + 30. / 60. / 60.);
		j.dec = +(12 + 35. / 60 + 30. / 60. / 60.);
		j.proposal.jobs.add(j);
	}

	/* (non-Javadoc)
	 * @see IProposalReader#readall()
	 */
	public Collection<Proposal> readall() throws SQLException {
		return proposals;
	}

}
