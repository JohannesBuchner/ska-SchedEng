package local.radioschedulers.ga;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.cpu.CPULikeScheduler;
import local.radioschedulers.cpu.FairPrioritizedSelector;
import local.radioschedulers.cpu.PrioritizedSelector;
import local.radioschedulers.cpu.RandomizedSelector;
import local.radioschedulers.cpu.ShortestFirstSelector;
import local.radioschedulers.exporter.HtmlExport;
import local.radioschedulers.lp.LinearScheduler2;
import local.radioschedulers.parallel.ParallelRequirementGuard;

public abstract class GeneticAlgorithmScheduler implements IScheduler {
	protected HashMap<LSTTime, Vector<Job>> possibles = new HashMap<LSTTime, Vector<Job>>();
	protected HashMap<Job, Double> timeleft = new HashMap<Job, Double>();
	private int ndays;

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public Schedule schedule(Collection<Proposal> proposals, int ndays) {
		this.ndays = ndays;
		Collection<Schedule> s = getStartSchedules(proposals);

		Schedule bestschedule;
		try {
			bestschedule = evolveSchedules(s);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return bestschedule;
	}

	protected abstract Schedule evolveSchedules(Collection<Schedule> s) throws Exception;

	protected Collection<Schedule> getStartSchedules(
			Collection<Proposal> proposals) {
		List<Schedule> schedules = new ArrayList<Schedule>();

		List<IScheduler> schedulers = new ArrayList<IScheduler>();

		schedulers.add(new CPULikeScheduler(new FairPrioritizedSelector(),
				new ParallelRequirementGuard()));
		schedulers.add(new CPULikeScheduler(new PrioritizedSelector(),
				new ParallelRequirementGuard()));
		schedulers.add(new CPULikeScheduler(new ShortestFirstSelector(),
				new ParallelRequirementGuard()));

		CPULikeScheduler rand = new CPULikeScheduler(new RandomizedSelector(),
				new ParallelRequirementGuard());
		schedulers.add(rand);
		schedulers.add(rand);
		schedulers.add(rand);
		schedulers.add(rand);

		schedulers.add(new LinearScheduler2());

		for (IScheduler s : schedulers) {
			log("scheduling using " + s);

			Schedule schedule = s.schedule(proposals, ndays);
			log("scheduling done");
			schedules.add(schedule);
			File f = new File("schedule" + schedules.size() + ".html");
			HtmlExport ex = new HtmlExport(f, s.toString());
			try {
				ex.export(schedule);

				Desktop d = Desktop.getDesktop();
				d.open(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return schedules;
	}

	private void log(String string) {
		System.out.println("DEBUG " + string);
	}
}
