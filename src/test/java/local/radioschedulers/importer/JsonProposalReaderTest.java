package local.radioschedulers.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;
import local.radioschedulers.Job;
import local.radioschedulers.Proposal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonProposalReaderTest {

	private JsonProposalReader reader;
	private ArrayList<Proposal> proposals;
	private File proposalFile;

	@Before
	public void setUp() throws Exception {
		proposals = new ArrayList<Proposal>();
		Proposal p = new Proposal();
		p.jobs = new ArrayList<Job>();
		p.name = "myproposal";
		p.id = p.name;
		p.priority = 3.;
		Job j = new Job();
		j.id = "myjob";
		j.hours = 60L;
		j.lstmax = 10.;
		j.lstmin = 4.;
		p.jobs.add(j);
		proposals.add(p);

		this.proposalFile = File.createTempFile("testproposal", ".json");
		reader = new JsonProposalReader(proposalFile);
	}

	@Test
	public void testReadWrite() throws Exception {
		reader.write(proposals);
		Collection<Proposal> readproposals = reader.readall();

		Assert.assertEquals(readproposals.size(), proposals.size());
		for (Proposal p : readproposals) {
			Proposal p2 = proposals.get(0);
			Assert.assertEquals(p.id, p2.id);
			Assert.assertEquals(p.name, p2.name);
			Assert.assertEquals(p.priority, p2.priority, 0.0001);
			Assert.assertEquals(p.jobs.size(), p2.jobs.size());
			Job j2 = p2.jobs.iterator().next();
			for (Job j : p.jobs) {
				Assert.assertEquals(j.id, j2.id);
				Assert.assertEquals(j.hours, j2.hours);
				Assert.assertEquals(j.lstmax, j2.lstmax);
				Assert.assertEquals(j.lstmin, j2.lstmin);
			}
		}
	}

	@After
	public void teardown() {
		this.proposalFile.deleteOnExit();
		this.proposalFile.delete();
	}
}
