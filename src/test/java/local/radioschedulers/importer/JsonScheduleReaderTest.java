package local.radioschedulers.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import junit.framework.Assert;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonScheduleReaderTest {

	private JsonScheduleReader reader;
	private File schedFile;
	private File spaceFile;
	private Collection<Proposal> proposals;
	private ScheduleSpace space;
	private Schedule schedule;
	private Map<String, Schedule> schedules;

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

		JobCombination jc = new JobCombination();
		jc.jobs.add(j);

		space = new ScheduleSpace();
		space.add(new LSTTime(0, 0), jc);
		space.add(new LSTTime(0, 1), jc);

		schedule = new Schedule();
		schedule.add(new LSTTime(0, 0), jc);
		schedule.add(new LSTTime(0, 1), jc);

		schedules = new HashMap<String, Schedule>();
		schedules.put("mysched", schedule);

		schedFile = File.createTempFile("testsched", ".json");
		spaceFile = File.createTempFile("testspace", ".json");

		reader = new JsonScheduleReader(schedFile, spaceFile, proposals);
	}

	@Test
	public void testSpace() throws Exception {
		reader.write(space);
		ScheduleSpace readspace = reader.readspace();
		Assert.assertTrue(readspace.findLastEntry().equals(
				space.findLastEntry()));
		for (Entry<LSTTime, Set<JobCombination>> e : readspace) {
			Set<JobCombination> ref = space.get(e.getKey());
			Assert.assertTrue(ref.equals(e.getValue()));
			Assert.assertEquals(ref, e.getValue());
		}

	}

	@Test
	public void testSchedule() throws Exception {
		reader.writeone(schedule);
		Schedule readschedule = reader.readone();
		Assert.assertTrue(readschedule.findLastEntry().equals(
				schedule.findLastEntry()));
	}


	@Test
	public void testSchedules() throws Exception {
		reader.write(schedules);
		Map<String, Schedule> readschedules = reader.readall();
		Schedule readschedule = readschedules.get("mysched");
		Assert.assertTrue(readschedule.findLastEntry().equals(
				schedule.findLastEntry()));
	}

	@After
	public void teardown() {
		// schedFile.deleteOnExit();
		// spaceFile.deleteOnExit();
		// schedFile.delete();
		// spaceFile.delete();
	}
}
