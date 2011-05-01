package local.radioschedulers.alg.serial;

import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.SmallTestScenario;
import local.radioschedulers.TestScenario;
import local.radioschedulers.alg.ga.watchmaker.ScheduleFactoryTest;
import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class SerialLeastChoiceSchedulerTest2 {

	private static Logger log = Logger
			.getLogger(SerialLeastChoiceSchedulerTest2.class);
	private ScheduleSpace space;

	@Before
	public void setUp() throws Exception {
		TestScenario ts = new SmallTestScenario();
		space = ts.getSpace();
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
