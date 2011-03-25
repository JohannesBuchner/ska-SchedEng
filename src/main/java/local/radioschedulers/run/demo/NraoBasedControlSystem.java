package local.radioschedulers.run.demo;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.JobWithResources;

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
			for (Job j : jc.jobs) {
				System.out.println("running Job " + j);
				JobWithResources jwr = (JobWithResources) j;
				
				System.out.println("\tbackends used:"
						+ jwr.resources.get("backends").numberrequired + " of "
						+ jwr.resources.get("backends").possibles);
				System.out.println("\tantennas used: "
						+ jwr.resources.get("antennas").numberrequired + " of "
						+ jwr.resources.get("antennas").possibles);
			}
		}
		try {
			Thread.sleep(1000 / 4);
		} catch (InterruptedException e) {
		}

	}

}
