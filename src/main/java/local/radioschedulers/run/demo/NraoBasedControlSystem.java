package local.radioschedulers.run.demo;

import java.io.IOException;
import java.io.InputStream;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.ResourceRequirement;

/**
 * we just want to
 * 
 * @author user
 */
public class NraoBasedControlSystem implements ControlSystem {

	@Override
	public void execute(JobCombination jc) {
		if (jc == null) {
			System.out.println("idling ...");
		} else {
			// if this was serious, we would assign resources between jobs now.
			try {
				for (Job j : jc.jobs) {
					System.out.println("running " + j);
					JobWithResources jwr = (JobWithResources) j;

					System.out.println("\tbackends used: "
							+ jwr.resources.get("backends").numberrequired
							+ " of " + jwr.resources.get("backends").possibles);
					System.out.println("\tantennas used: "
							+ jwr.resources.get("antennas").numberrequired
							+ " of " + jwr.resources.get("antennas").possibles);

					ProcessBuilder pb = new ProcessBuilder("bash",
							"/home/user/workspace/arrayscheduling/callsched",
							getTemplateFile(j), getAntennas(jwr.resources
									.get("antennas")));
					runProcess(pb, "sched/drudg");
					pb = new ProcessBuilder("bash",
							"/home/user/workspace/arrayscheduling/execute");
					runProcess(pb, "executing task");

				}
			} catch (IOException e) {
				throw new RuntimeException("lets ponder about this here...", e);
			} catch (InterruptedException e) {
				throw new RuntimeException("lets ponder about this here...", e);
			}
		}
		try {
			Thread.sleep(1000 / 4);
		} catch (InterruptedException e) {
		}

	}

	private Process runProcess(ProcessBuilder pb, String name)
			throws IOException, InterruptedException {
		System.out.print(name + "...");
		Process p = pb.start();
		p.getOutputStream().close();
		InputStream is = p.getInputStream();
		InputStream es = p.getErrorStream();
		p.waitFor();
		{
			//while (is.available() > 0) {
			//	System.out.write(is.read());
			//}
			while (es.available() > 0) {
				System.err.write(es.read());
			}
			Thread.sleep(100);
		}

		if (p.exitValue() != 0) {
			throw new IOException("error running " + name + " ... exitvalue: "
					+ p.exitValue());
		}
		System.out.println("done.");
		return p;
	}

	private String getAntennas(ResourceRequirement resourceRequirement) {
		int count = 0;
		String s = "";
		for (Object o : resourceRequirement.possibles) {
			s = s + o.toString();
			count++;
			if (count >= resourceRequirement.numberrequired)
				break;
			s = s + ",";
		}
		return s;
	}

	private String getTemplateFile(Job j) {
		return "/home/user/workspace/arrayscheduling/" + j.id + ".key";
	}

}
