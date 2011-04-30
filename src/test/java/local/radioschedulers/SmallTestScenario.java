package local.radioschedulers;

import java.util.ArrayList;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.ScheduleSpace;

public class SmallTestScenario extends TestScenario {
	public SmallTestScenario() {
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
		JobCombination jA = getJobCombination();
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
		JobCombination jB = getJobCombination();
		jB.jobs.add(j);
		j = new Job();
		j.hours = 5. * ScheduleSpace.LST_SLOTS_MINUTES / 60.;
		j.proposal = pB;
		j.id = "C";
		pB.jobs.add(j);
		JobCombination jC = getJobCombination();
		jC.jobs.add(j);
		
		proposals.add(pA);
		proposals.add(pB);

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

	private JobCombination getJobCombination() {
		return new JobCombination() {
			@Override
			public int hashCode() {
				return jobs.iterator().next().id.hashCode();
			}
		};
	}
}
