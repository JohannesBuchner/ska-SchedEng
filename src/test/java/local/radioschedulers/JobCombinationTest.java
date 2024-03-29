package local.radioschedulers;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JobCombinationTest {

	private Job j1;
	private Job j2;

	@Before
	public void setUp() throws Exception {
		Proposal p1 = new Proposal();
		p1.priority = 1.;
		p1.id = "p1";
		j1 = new Job();
		j1.hours = 100.;
		j1.proposal = p1;
		p1.jobs = new ArrayList<Job>();
		p1.jobs.add(j1);

		Proposal p2 = new Proposal();
		p2.priority = 2.;
		p2.id = "p2";
		j2 = new Job();
		j2.proposal = p2;
		j2.hours = 200.;
		p2.jobs = new ArrayList<Job>();
		p2.jobs.add(j2);

	}

	@Test
	public void testCalculatePriority() {
		JobCombination jc = new JobCombination();
		jc.jobs.add(j1);
		Assert.assertEquals(j1.proposal.priority, jc.calculatePriority(), 1e-2);
	}

	@Test
	public void testCalculatePriority2() {
		JobCombination jc = new JobCombination();
		jc.jobs.add(j1);
		jc.jobs.add(j2);
		Assert.assertTrue(jc.calculatePriority() > j1.proposal.priority);
		Assert.assertTrue(jc.calculatePriority() > j2.proposal.priority);
	}

	@Test
	public void testCalculatePriority0() {
		try {
			JobCombination jc = new JobCombination();
			jc.calculatePriority();
			Assert.fail();
		} catch (IllegalStateException e) {

		}
	}

	@Test
	public void testEquals() throws Exception {
		JobCombination jc1 = new JobCombination();
		jc1.jobs.add(j1);
		jc1.jobs.add(j2);
		JobCombination jc2 = new JobCombination();
		jc2.jobs.add(j2);
		Assert.assertNotSame(jc1, jc2);
		Assert.assertFalse(jc1.equals(jc2));
		jc2.jobs.add(j1);
		Assert.assertNotSame(jc1, jc2);
		Assert.assertEquals(jc1, jc2);

	}
}
