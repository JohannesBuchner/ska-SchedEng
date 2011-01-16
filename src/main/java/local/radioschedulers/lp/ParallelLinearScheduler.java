package local.radioschedulers.lp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.ResourceRequirements;
import local.radioschedulers.SpecificSchedule;

public class ParallelLinearScheduler implements IScheduler {
	public static final int LST_SLOTS = 24 * 4;
	public static final int LST_SLOTS_MINUTES = 24 * 60 / LST_SLOTS;

	public static final int NANTENNAS = 42;
	
	List<Job> jobs = new ArrayList<Job>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public SpecificSchedule schedule(Collection<Proposal> proposals, int ndays) {
		int njobs = 0;

		/**
		 * set the variables to be binary
		 */
		PrintStream varDefinition;
		PrintStream constraints;
		PrintStream costFunction;
		try {
			varDefinition = new PrintStream(new File("vardef.lp"));
			constraints = new PrintStream(new File("constraints.lp"));
			costFunction = new PrintStream(new File("costs.lp"));
		} catch (FileNotFoundException e1) {
			throw new IllegalStateException(e1);
		}

		/**
		 * 2) A job is on when it can do work, i.e. the source object is up:
		 */
		for (Proposal p : proposals) {
			for (Job j : p.jobs) {
				StringBuilder jobsum = new StringBuilder();
				log("job [" + j.lstmin + ".." + j.lstmax + "]: Proposal "
						+ p.id);
				for (int kd = 0; kd < ndays; kd++) {
					boolean goodday = true;
					
					JobWithResources jr = null;
					if (j instanceof JobWithResources) {
						jr = (JobWithResources) j;
						if (jr.date.requires(new LSTTime((long) kd, 0L)) == 0) {
							goodday = false;
						}
					}
					
					for (int kh = 0; kh < LST_SLOTS; kh++) {
						double hour = kh * LST_SLOTS_MINUTES / 60.;
						boolean inside = true;
						String varname = getVar(njobs, (kd * LST_SLOTS + kh));

						if (j.lstmax > j.lstmin)
							if (hour > j.lstmax || hour < j.lstmin)
								inside = false;
						if (j.lstmax < j.lstmin)
							if (hour < j.lstmin && hour > j.lstmax)
								inside = false;

						if (inside && goodday) {
							varDefinition.append("bin " + varname + ";\n");
							// log("minute = " + hour + " ::: inside range");
							/*
							 * // inside text.append(getVar(njobs, (kd *
							 * LST_SLOTS + kh / LST_SLOTS_MINUTES)) +
							 * " >= 0;\n"); }
							 */
						} else {
							constraints.append(varname + " = 0;\n");
							varDefinition.append("int " + varname + ";\n");
							// log("minute = " + hour + " ::: outside range");
						}

						/**
						 * 4) A job gets its hours:
						 */
						jobsum.append(getVar(njobs, (kd * LST_SLOTS + kh
								/ LST_SLOTS_MINUTES))
								+ " +");

					}
				}
				// TODO: this can be moved to the cost function
				/**
				 * all time for the job
				 */
				jobsum.append("0 <= " + j.hours * LST_SLOTS + ";\n");

				constraints.append(jobsum);

				jobs.add(j);

				njobs++;
			}
		}
		/**
		 * 1) No incompatible jobs are at the same time k, i.e. not requesting
		 * more resources than we have:
		 */
		for (int k = 0; k < ndays * LST_SLOTS; k++) {
			/* TODO: handle resources */
			for (int j = 0; j < jobs.size(); j++) {
				Integer antennaswanted;
				
				Job job = jobs.get(j);
				if (job instanceof JobWithResources) {
					JobWithResources jr = (JobWithResources) job;
					ResourceRequirements r = jr.resources.get("antennas");
					antennaswanted = r.totalRequired();
				}else {
					antennaswanted = NANTENNAS;
				}
				constraints.append(antennaswanted + " " + getVar(j, k) + " + ");
			}
			constraints.append("0 <= " + NANTENNAS + ";\n");
		}

		costFunction.append("max: ");
		for (int j = 0; j < jobs.size(); j++) {
			for (int k = 0; k <= ndays * LST_SLOTS; k++) {
				costFunction.append(jobs.get(j).proposal.priority + " "
						+ getVar(j, k) + " + ");
			}
		}
		costFunction.append("0;\n");
		// System.out.println(costFunction + "\n" + constraints + "\n"
		// + varDefinition);
		// System.out.println(varDefinition);
		// System.out.println(costFunction);

		costFunction.close();
		constraints.close();
		varDefinition.close();
		File lp = new File("schedule.lp");
		log("concat  ... ");
		catFilesF(lp, new File("costs.lp"), new File("constraints.lp"),
				new File("vardef.lp"));
		log("concat  done");

		SpecificSchedule s;
		try {
			log("solving ...");
			s = lpsolve(lp);
			log("solving done");
		} catch (IOException e) {
			throw new IllegalStateException("Install lp_solve", e);
		}
		return s;
	}

	private void catFilesF(File out, File... in) {

		try {
			out.delete();
			FileChannel dstChannel = new FileOutputStream(out, true).getChannel();
			for (File f : in) {
				FileChannel srcChannel = new FileInputStream(f).getChannel();
				dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
				srcChannel.close();
			}
			dstChannel.close();
		} catch (IOException e) {
			throw new IllegalStateException("bad copying", e);
		}

	}

	/**
	 * parses a lp statement in LP format (see lp_solve) and returns a Schedule.
	 * 
	 * Calls and uses lp_solve internally to do this, so lp_solve has to be
	 * installed.
	 * 
	 * @param lpText
	 * @return schedule
	 * @throws IOException
	 */
	protected SpecificSchedule lpsolve(File lp) throws IOException {
		List<String> cmd = new ArrayList<String>();
		cmd.add("lp_solve");
		cmd.add(lp.getAbsolutePath());

		ProcessBuilder pb = new ProcessBuilder(cmd);
		log("launching lp_solve");
		Process p = pb.start();
		LineNumberReader is = new LineNumberReader(new InputStreamReader(p
				.getInputStream()));
		return parseLpsolve(is);
	}

	private SpecificSchedule parseLpsolve(LineNumberReader is) throws IOException {
		String line;
		line = is.readLine();
		if (line.length() != 0)
			throw new IOException("unexpected response: " + line);

		line = is.readLine();
		if (!line.startsWith("Value of objective function: "))
			throw new IOException("unexpected response: " + line);

		line = is.readLine();
		if (line.length() != 0)
			throw new IOException("unexpected response: " + line);

		line = is.readLine();
		if (!line.startsWith("Actual values of the variables:"))
			throw new IOException("unexpected response: " + line);

		log("parsing output ...");
		SpecificSchedule s = new SpecificSchedule();

		while (true) {
			line = is.readLine();
			if (line == null)
				break;

			// log(line);
			String parts[] = line.split(" [ ]*", 2);
			String varname = parts[0];
			String varnameparts[] = varname.split("_", 3);
			int j = Integer.parseInt(varnameparts[1]);
			int k = Integer.parseInt(varnameparts[2]);
			long day = k / LST_SLOTS;
			long minute = (k % LST_SLOTS) * LST_SLOTS_MINUTES;
			Double value = Double.parseDouble(parts[1]);
			LSTTime t = new LSTTime(day, minute);
			// log("parsed as j " + j + ", k " + k + ", day " + day +
			// ", minute "
			// + minute + ", value " + value);
			// log(t + " : " + j);
			if (value != 0) {
				JobCombination jc = new JobCombination();
				jc.jobs.add(jobs.get(j));
				s.add(t, jc);
			}
		}
		log("parsing output done");
		return s;
	}

	private void log(String string) {
		System.out.println("DEBUG: " + string);
	}

	private String getVar(int j, int k) {
		return "x_" + j + "_" + k;
	}
}
