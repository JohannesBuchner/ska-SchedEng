package local.radioschedulers;

import java.util.ArrayList;
import java.util.Map.Entry;

import local.radioschedulers.alg.serial.ContinuousLeastChoiceScheduler;
import local.radioschedulers.alg.serial.ContinuousUnlessOneChoiceScheduler;
import local.radioschedulers.alg.serial.ExtendingLeastChoiceScheduler;
import local.radioschedulers.alg.serial.PrioritizedSelector;
import local.radioschedulers.alg.serial.SerialLeastChoiceScheduler;
import local.radioschedulers.ga.wf.ScheduleFactoryTest;
import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class GreedyLeastChoiceSchedulerTest2 {

	private static Logger log = Logger
			.getLogger(GreedyLeastChoiceSchedulerTest2.class);
	private ScheduleSpace space;

	@Before
	public void setUp() throws Exception {
		// low-priority, harder to place
		Proposal pA = new Proposal();
		pA.id = "A";
		pA.name = "A";
		pA.priority = 1;
		Job j = new Job();
		j.hours = 5. * ScheduleSpace.LST_SLOTS_MINUTES / 60.;
		j.proposal = pA;
		j.id = "A";
		pA.jobs = new ArrayList<Job>();
		pA.jobs.add(j);
		JobCombination jA = new JobCombination();
		jA.jobs.add(j);

		// high-priority
		Proposal pB = new Proposal();
		pB.id = "B";
		pB.name = "B";
		pB.priority = 2;
		pB.jobs = new ArrayList<Job>();
		j = new Job();
		j.hours = 5. * ScheduleSpace.LST_SLOTS_MINUTES / 60.;
		j.proposal = pB;
		j.id = "B";
		pB.jobs.add(j);
		JobCombination jB = new JobCombination();
		jB.jobs.add(j);
		j = new Job();
		j.hours = 5. * ScheduleSpace.LST_SLOTS_MINUTES / 60.;
		j.proposal = pB;
		j.id = "C";
		pB.jobs.add(j);
		JobCombination jC = new JobCombination();
		jC.jobs.add(j);

		int t = ScheduleSpace.LST_SLOTS_MINUTES;
		space = new ScheduleSpace();
		int i = 0;
		for (i = 0; i < 18; i++)
			space.add(new LSTTime(0, i * t), jA);

		space.add(new LSTTime(0, 0), jB);
		for (i = 1; i < 6; i++) {
			space.add(new LSTTime(0, i * t), jB);
			space.add(new LSTTime(0, i * t), jC);
		}
		for (i = 12; i < 18; i++) {
			space.add(new LSTTime(0, i * t), jC);
			space.add(new LSTTime(0, i * t), jB);
		}
		space.add(new LSTTime(0, 18 * t), jC);
	}

	protected RequirementGuard getRequirementGuard() {
		return new ParallelRequirementGuard();
	}

	@Test
	public void testLeastChoiceScheduler() throws Exception {
		SerialLeastChoiceScheduler scheduler = new SerialLeastChoiceScheduler(
				new PrioritizedSelector());
		Schedule s = scheduler.schedule(space);
		ScheduleFactoryTest.assertScheduleIsWithinTemplate(s, space, 0);

		for (Entry<LSTTime, JobCombination> e : s) {
			log.debug("@" + e.getKey() + " -- " + e.getValue());
		}
	}

	@Test
	public void testContinuousUnlessOneChoiceScheduler() throws Exception {
		ContinuousUnlessOneChoiceScheduler scheduler = new ContinuousUnlessOneChoiceScheduler(
				new PrioritizedSelector());
		Schedule s = scheduler.schedule(space);
		ScheduleFactoryTest.assertScheduleIsWithinTemplate(s, space, 0);

		for (Entry<LSTTime, JobCombination> e : s) {
			log.debug("@" + e.getKey() + " -- " + e.getValue());
		}
	}

	@Test
	public void testExtendingLeastChoiceScheduler() throws Exception {
		ExtendingLeastChoiceScheduler scheduler = new ExtendingLeastChoiceScheduler(
				new PrioritizedSelector());
		Schedule s = scheduler.schedule(space);
		ScheduleFactoryTest.assertScheduleIsWithinTemplate(s, space, 0);

		for (Entry<LSTTime, JobCombination> e : s) {
			log.debug("@" + e.getKey() + " -- " + e.getValue());
		}
	}
	@Test
	public void testContinuousLeastChoiceScheduler() throws Exception {
		ContinuousLeastChoiceScheduler scheduler = new ContinuousLeastChoiceScheduler(
				new PrioritizedSelector());
		Schedule s = scheduler.schedule(space);
		ScheduleFactoryTest.assertScheduleIsWithinTemplate(s, space, 0);

		for (Entry<LSTTime, JobCombination> e : s) {
			log.debug("@" + e.getKey() + " -- " + e.getValue());
		}
	}
}
