package local.radioschedulers.preschedule.parallel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.Proposal;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.importer.IProposalReader;
import local.radioschedulers.importer.RandomGeneratingProposalReader;
import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.SingleRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class FastRecursiveCompatibleJobFactoryTest {

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
		getProposals(gpr);
	}

	private void getProposals(IProposalReader gpr) throws Exception {
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
		cjf = new FastRecursiveCompatibleJobFactory(alljobs, req);
		Collection<JobCombination> combinations = cjf.getCombinations();
		int ncomb = combinations.size();
		Assert.assertEquals("all single jobs", njobs, ncomb);
	}

	@Test
	public void testOneParallel() throws Exception {
		RequirementGuard req = new TwoParallelRequirementGuard();
		cjf = new FastRecursiveCompatibleJobFactory(alljobs, req);
		Collection<JobCombination> combinations = cjf.getCombinations();
		int ncomb = combinations.size();
		Assert.assertEquals("tuples, singles ", (njobs * (njobs - 1)) / 2
				+ njobs, ncomb);
	}

	@Test
	public void testFullParallel() throws Exception {
		RequirementGuard req = new ParallelRequirementGuard();
		cjf = new FastRecursiveCompatibleJobFactory(alljobs, req);
		Collection<JobCombination> combinations = cjf.getCombinations();
		int ncomb = combinations.size();
		Assert.assertEquals("full combination", (1 << njobs) - 1, ncomb);
	}

	@Test
	public void testFullParallelStress() throws Exception {
		RandomGeneratingProposalReader pr = new RandomGeneratingProposalReader();
		pr.fill(15);
		getProposals(pr);
		
		RequirementGuard req = new ParallelRequirementGuard();
		log.debug("getting combinations...");
		cjf = new FastRecursiveCompatibleJobFactory(alljobs, req);
		Collection<JobCombination> combinations = cjf.getCombinations();
		int ncomb = combinations.size();
		log.debug("got " + ncomb + " combinations");
		Assert.assertTrue("full combination " + ncomb + " > " + njobs, ncomb > njobs);
	}

}
