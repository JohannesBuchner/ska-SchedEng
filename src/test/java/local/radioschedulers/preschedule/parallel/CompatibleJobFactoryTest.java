package local.radioschedulers.preschedule.parallel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.Proposal;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.SingleRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class CompatibleJobFactoryTest {

	@SuppressWarnings("unused")
	private static Logger log = Logger
			.getLogger(CompatibleJobFactoryTest.class);
	private CompatibleJobFactory cjf;
	private Set<Job> alljobs;
	int njobs;
	private Collection<Proposal> proposals;

	@Before
	public void setUp() throws Exception {
		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		proposals = gpr.readall();

		alljobs = new HashSet<Job>();
		for (Proposal p : proposals) {
			alljobs.addAll(p.jobs);
		}
		njobs = alljobs.size();
	}

	@Test
	public void testNoParallel() throws Exception {
		RequirementGuard req = new SingleRequirementGuard();
		cjf = new CompatibleJobFactory(alljobs, req);
		Collection<JobCombination> combinations = cjf.getCombinations();
		int ncomb = combinations.size();
		Assert.assertEquals("all single jobs", njobs, ncomb);
	}

	@Test
	public void testOneParallel() throws Exception {
		RequirementGuard req = new TwoParallelRequirementGuard();
		cjf = new CompatibleJobFactory(alljobs, req);
		Collection<JobCombination> combinations = cjf.getCombinations();
		int ncomb = combinations.size();
		Assert.assertEquals("tuples, singles ", (njobs * (njobs - 1)) / 2
				+ njobs, ncomb);
	}

	@Test
	public void testFullParallel() throws Exception {
		RequirementGuard req = new ParallelRequirementGuard();
		cjf = new CompatibleJobFactory(alljobs, req);
		Collection<JobCombination> combinations = cjf.getCombinations();
		int ncomb = combinations.size();
		Assert.assertEquals("full combination", (1 << njobs) - 1, ncomb);
	}

}
