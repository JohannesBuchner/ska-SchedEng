package local.radioschedulers;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Ignore;

import local.radioschedulers.Proposal;
import local.radioschedulers.ScheduleSpace;

@Ignore
public class TestScenario {

	protected ScheduleSpace space;
	protected Collection<Proposal> proposals = new ArrayList<Proposal>();

	public TestScenario() {
		super();
	}

	public Collection<Proposal> getProposals() {
		return proposals;
	}

	public ScheduleSpace getSpace() {
		return space;
	}

}