package local.radioschedulers.ga.watchmaker;

import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class GeneticHistoryTest {
	private static Logger log = Logger.getLogger(GeneticHistoryTest.class);

	private GeneticHistory<Integer, String> history;

	@Before
	public void setUp() throws Exception {
		history = new GeneticHistory<Integer, String>();
	}

	@Test
	public void testDerive() throws Exception {
		history.initiated(1, "eins");
		history.initiated(2, "zwei");
		history.initiated(3, "drei");

		log.debug("deriving 4");
		history.derive(4, 1, 0.5);
		history.derive(4, 3, 0.5);

		Map<String, Double> props = history.getProperties(4);
		Assert.assertEquals(0.5, props.get("eins"), 0.001);
		Assert.assertEquals(0.5, props.get("drei"), 0.001);
		Assert.assertFalse(props.containsKey("zwei"));

		log.debug("deriving 5");
		history.derive(5, 4, 0.5);
		history.derive(5, 2, 0.5);

		props = history.getProperties(5);
		Assert.assertEquals(0.25, props.get("eins"), 0.001);
		Assert.assertEquals(0.5, props.get("zwei"), 0.001);
		Assert.assertEquals(0.25, props.get("drei"), 0.001);

		log.debug("deriving 6");
		history.derive(6, 5, 0.5);
		history.derive(6, 4, 0.5);

		props = history.getProperties(6);
		Assert.assertEquals((0.25 + 0.5) / 2, props.get("eins"), 0.001);
		Assert.assertEquals(0.25, props.get("zwei"), 0.001);
		Assert.assertEquals((0.25 + 0.5) / 2, props.get("drei"), 0.001);
	}

}
