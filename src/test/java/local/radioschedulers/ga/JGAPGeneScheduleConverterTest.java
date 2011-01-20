package local.radioschedulers.ga;

import java.util.Collection;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.cpu.CPULikeScheduler;
import local.radioschedulers.cpu.RandomizedSelector;
import local.radioschedulers.ga.jgap.JGAPGeneScheduleConverter;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.SingleRequirementGuard;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.impl.DefaultConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JGAPGeneScheduleConverterTest {

	private Collection<Proposal> proposals;
	private ScheduleSpace template;
	private int ndays = 10;
	private Schedule schedule;
	private Configuration conf;

	@Before
	public void setup() throws Exception {
		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		proposals = gpr.readall();
		Assert.assertTrue(proposals.size() > 0);
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		CPULikeScheduler scheduler = new CPULikeScheduler(
				new RandomizedSelector(), new SingleRequirementGuard());
		schedule = scheduler.schedule(template);
		conf = new DefaultConfiguration();
	}

	@Test
	public void testConverter() throws Exception {
		JGAPGeneScheduleConverter conv = new JGAPGeneScheduleConverter(conf);
		IChromosome chromo = conv.getChromosomeFromSpecificSchedule(template,
				schedule);
		int i = 0;
		for (Gene g : chromo.getGenes()) {
			LSTTime t = new LSTTime(i / Schedule.LST_SLOTS_PER_DAY, i
					* Schedule.LST_SLOTS_MINUTES);
			JobCombination scheduleJc = schedule.get(t);
			Assert.assertNotNull("Schedule should not be empty at " + t,
					scheduleJc);

			JobCombination geneJc = (JobCombination) g.getAllele();
			Assert.assertNotNull(geneJc);

			Assert.assertEquals(scheduleJc, geneJc);
			i++;
		}

		Schedule s = JGAPGeneScheduleConverter
				.getScheduleFromChromosome(chromo);
		i = 0;
		for (Entry<LSTTime, JobCombination> e : s) {
			LSTTime t = e.getKey();
			JobCombination jc = e.getValue();
			JobCombination scheduleJc = schedule.get(t);
			JobCombination geneJc = (JobCombination) chromo.getGene(i)
					.getAllele();

			Assert.assertEquals(scheduleJc, geneJc);
			Assert.assertEquals(jc, scheduleJc);
			// it would be odd if the next would fail
			Assert.assertEquals(jc, geneJc);

			i++;
		}

	}

}
