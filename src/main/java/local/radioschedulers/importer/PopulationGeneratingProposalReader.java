package local.radioschedulers.importer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import local.radioschedulers.Job;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.ResourceRequirement;
import local.radioschedulers.Schedule;
import local.radioschedulers.SolarDateRangeRequirements;

import org.apache.log4j.Logger;

public class PopulationGeneratingProposalReader implements IProposalReader {
	private static Logger log = Logger
			.getLogger(PopulationGeneratingProposalReader.class);

	private Random r = new Random();

	private static int id = 0;

	private static int getCurrentId() {
		return id;
	}

	private static int getNextId() {
		return ++id;
	}

	private static String getNextIdAsString() {
		return Integer.toString(getNextId());
	}

	public Proposal createSimpleProposal(String name, double prio,
			double startlst, double endlst, Long totalhours) {
		Proposal p = new Proposal();
		p.id = getNextIdAsString();
		p.name = name + " " + p.id;
		p.priority = prio;
		p.jobs = new ArrayList<Job>();
		Job j = newJob();
		j.hours = totalhours;
		j.lstmax = endlst;
		j.lstmin = startlst;
		j.id = name;
		j.proposal = p;
		p.jobs.add(j);
		return p;
	}

	private void needsFullArray(JobWithResources j) {
		needsAntennas(j, 42);
	}

	private void needsAntennas(JobWithResources j, int nantennas) {
		ResourceRequirement rr = new ResourceRequirement();
		for (Integer i = 0; i < nantennas; i++)
			rr.possibles.add(i);
		rr.numberrequired = nantennas;
		j.resources.put("antennas", rr);
	}

	private void needsOneAntenna(JobWithResources j) {
		ResourceRequirement rr = new ResourceRequirement();
		rr.possibles.add(r.nextInt(42));
		rr.numberrequired = 1;
		j.resources.put("antennas", rr);
	}

	public static int NANTENNAS = 42;

	public static Set<Integer> allAntennas = new HashSet<Integer>();
	static {
		for (Integer i = 0; i < NANTENNAS; i++)
			allAntennas.add(i);
	}

	/**
	 * if false, any subset of antennas of sufficient size can be chosen.
	 */
	public static boolean SPECIFIC_RANDOM_GROUP = false;

	private void attachAntennaRequirementsFromProbabilityModel(
			JobWithResources j) {
		ResourceRequirement rr = new ResourceRequirement();
		int n = nantennaProbabilityModel();
		// TODO: a specific antenna could be selected for maintenance tasks.
		if (SPECIFIC_RANDOM_GROUP) {
			ArrayList<Integer> a = new ArrayList<Integer>(allAntennas);
			Collections.shuffle(a);
			rr.possibles.addAll(a.subList(0, n));
		} else {
			rr.possibles.addAll(allAntennas);
		}
		rr.numberrequired = n;
		j.resources.put("antennas", rr);
	}

	private int nantennaProbabilityModel() {
		double d = r.nextDouble() * 100;
		if (d < 5) {
			return 1;
		} else {
			double v = d - 5;
			for (int i = 1; i < NANTENNAS; i *= 2) {
				v--;
				if (v < 0) {
					log.debug(" ### drew " + d + " --> nantennas = " + i);
					return i;
				}
			}
			return NANTENNAS;
		}
	}

	public Proposal createDaytimeProposal(String name, double prio,
			int startday, int endday, int starthour, int endhour,
			Long totalhours) {
		Proposal p = new Proposal();
		p.id = getNextIdAsString();
		p.name = name + " " + p.id;
		p.priority = prio;
		p.jobs = new ArrayList<Job>();
		JobWithResources jwr = newJob();
		jwr.date = new SolarDateRangeRequirements(new LSTTime(startday,
				starthour * Schedule.LST_SLOTS_PER_HOUR), new LSTTime(endday,
				(endhour) * Schedule.LST_SLOTS_PER_HOUR));

		Job j = jwr;
		j.hours = totalhours;
		j.lstmax = 0.;
		j.lstmin = 23.99;
		j.id = name;
		j.proposal = p;
		p.jobs.add(j);
		return p;
	}

	public Proposal createRandomProposal() {
		Proposal p = new Proposal();
		p.id = getNextIdAsString();
		p.name = "Proposal" + p.id;
		p.priority = r.nextDouble() * 5;
		p.jobs = new ArrayList<Job>();

		Job j = newJob();
		j.lstmin = r.nextDouble() * 24;
		j.lstmax = j.lstmin + 6 + r.nextInt(4);
		if (j.lstmax > 24)
			j.lstmax -= 24;
		j.hours = 0L;
		while (j.hours < 4)
			j.hours = Math.round((1. / (r.nextDouble() * 200 + 1)) * 6000);
		j.proposal = p;

		p.jobs.add(j);

		return p;
	}

	public Proposal createFullSkyProposal(String name, double prio,
			Long totalhours) {
		Proposal p = new Proposal();
		p.id = getNextIdAsString();
		p.name = name;
		p.priority = prio;
		p.jobs = new ArrayList<Job>();
		int n = 8;
		for (int i = 0; i < n; i++) {
			Job j = newJob();
			j.hours = totalhours / n;
			j.lstmin = ((i * 24. / n) % 24) * 1.;
			j.lstmax = (((i + 1) * 24. / n) % 24) * 1.;
			j.id = name + i;
			j.proposal = p;
			p.jobs.add(j);
		}
		return p;
	}

	private JobWithResources newJob() {
		JobWithResources j = new JobWithResources();
		attachAntennaRequirementsFromProbabilityModel(j);
		return j;
	}

	public static double PROPORTION_FULLSKY = 0.23;
	public static double PROPORTION_MILKYWAY = 0.4;
	public static double PROPORTION_DAYTIME = 0.16 * (16 - 9) / 24.;
	public static double PROPORTION_RANDOM = 0.32;

	List<Proposal> proposals = new ArrayList<Proposal>();

	public void fill(int ndays) throws Exception {
		long totalhours = 0;

		while (totalhours < ndays * 24 * PROPORTION_FULLSKY) {
			int hours = 100 + r.nextInt(1000);
			totalhours += hours;
			proposals.add(createFullSkyProposal("FullSky", r.nextDouble() * 5,
					hours * 1L));
		}

		totalhours = 0;
		while (totalhours < ndays * 24 * PROPORTION_MILKYWAY) {
			int startlst = 8 + r.nextInt(4);
			int hours = r.nextInt(50) + 4;
			totalhours += hours;
			proposals.add(createSimpleProposal("Galaxy", r.nextDouble() * 5,
					startlst, startlst + 8, hours * 1L));
		}

		totalhours = 0;
		while (totalhours < ndays * 24 * PROPORTION_RANDOM) {
			Proposal p = createRandomProposal();
			Job j = (Job) p.jobs.toArray()[0];
			proposals.add(p);
			Long hours = j.hours;
			totalhours += hours;
		}

		totalhours = 0;
		while (totalhours < ndays * 24 * PROPORTION_DAYTIME) {
			int starthour = 9;
			int endhour = 16;
			int hours = r.nextInt(1) * 10 + r.nextInt(10) + 1;
			totalhours += hours;

			proposals.add(createDaytimeProposal("Maintainance",
					r.nextDouble() * 5, 0, ndays, starthour, endhour,
					hours * 1L));
		}

		log.debug(getCurrentId() + " proposals issued.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IProposalReader#readall()
	 */
	public Collection<Proposal> readall() throws SQLException {
		return proposals;
	}
}
