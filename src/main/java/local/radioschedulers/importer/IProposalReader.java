package local.radioschedulers.importer;
import java.util.Collection;

import local.radioschedulers.Proposal;

public interface IProposalReader {

	public abstract Collection<Proposal> readall() throws Exception;

}