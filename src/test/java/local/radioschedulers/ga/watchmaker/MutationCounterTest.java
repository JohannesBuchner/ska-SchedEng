package local.radioschedulers.ga.watchmaker;

import java.util.Map;

import junit.framework.Assert;
import local.radioschedulers.alg.ga.watchmaker.MutationCounter;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class MutationCounterTest {
	private static Logger log = Logger.getLogger(MutationCounterTest.class);

	private MutationCounter<Integer, String> history;

	@Before
	public void setUp() throws Exception {
		history = new MutationCounter<Integer, String>();
	}

	@Test
	public void testDerive() throws Exception {
		history.add(1, "op1", 10);
		history.add(1, "op2", 20);
		history.add(2, "op1", 37);
		history.add(2, "op2", 67);
		history.add(2, "op3", 1);
		history.add(3, "op1", 3);
		history.add(3, "op2", 3);

		log.debug("deriving 4");
		history.derive(4, 1);
		history.derive(4, 3);

		Map<String, Integer> props = history.getProperties(4);
		Assert.assertEquals(10 + 3, (int) props.get("op1"));
		Assert.assertEquals(20 + 3, (int) props.get("op2"));
		Assert.assertFalse(props.containsKey("op3"));

		log.debug("deriving 5");
		history.derive(5, 4);
		history.derive(5, 2);

		props = history.getProperties(5);
		Assert.assertEquals(10 + 3 + 37, (int) props.get("op1"));
		Assert.assertEquals(20 + 3 + 67, (int) props.get("op2"));
		Assert.assertEquals(1, (int) props.get("op3"));
	}

}
