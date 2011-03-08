package local.radioschedulers.preschedule.parallel;

import java.util.Collection;

import junit.framework.Assert;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.ResourceRequirement;
import local.radioschedulers.preschedule.RequirementGuard;

import org.junit.Before;
import org.junit.Test;

public class ParallelRequirementGuardTest {

	private RequirementGuard guard;

	@Before
	public void setUp() throws Exception {
		guard = new ParallelRequirementGuard();
	}

	private JobWithResources createJobWithAntennas(int nantennasRequired,
			int nantennasTotal) {
		JobWithResources jwr = new JobWithResources();
		ResourceRequirement rr = new ResourceRequirement();
		for (int i = 1; i <= nantennasTotal; i++) {
			rr.possibles.add(i);
		}
		rr.numberrequired = nantennasRequired;
		jwr.resources.put("antennas", rr);
		return jwr;
	}

	private Collection<Job> combineJobs(Job a, Job b) {
		JobCombination jc = new JobCombination();
		jc.jobs.add(a);
		jc.jobs.add(b);
		return jc.jobs;
	}

	@Test
	public void testCompatible() {
		JobWithResources jwr1;
		JobWithResources jwr2;
		jwr1 = createJobWithAntennas(3, 3);
		jwr2 = createJobWithAntennas(3, 3);
		Assert.assertFalse(guard.compatible(combineJobs(jwr1, jwr2)));

		jwr1 = createJobWithAntennas(1, 3);
		jwr2 = createJobWithAntennas(1, 3);
		Assert.assertTrue(guard.compatible(combineJobs(jwr1, jwr2)));

		jwr1 = createJobWithAntennas(1, 3);
		jwr2 = createJobWithAntennas(2, 3);
		Assert.assertTrue(guard.compatible(combineJobs(jwr1, jwr2)));

		jwr1 = createJobWithAntennas(42, 42);
		jwr2 = createJobWithAntennas(4, 42);
		Assert.assertFalse(guard.compatible(combineJobs(jwr1, jwr2)));

		// need antenna 1
		jwr1 = createJobWithAntennas(1, 3);
		jwr1.resources.get("antennas").possibles.remove(2);
		jwr1.resources.get("antennas").possibles.remove(3);
		// can't use 3
		jwr2 = createJobWithAntennas(2, 3);
		jwr2.resources.get("antennas").possibles.remove(3);
		Assert.assertFalse(guard.compatible(combineJobs(jwr1, jwr2)));
	}
}
