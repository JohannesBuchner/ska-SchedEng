package local.radioschedulers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LSTMapTest {

	private static final Logger log = Logger.getLogger(LSTMapTest.class);

	private Map<LSTTime, Integer> map;

	@Before
	public void setUp() throws Exception {
		this.map = new LSTMap<Integer>();
	}

	@Test
	public void testAdd() {
		this.map.clear();
		this.map.put(new LSTTime(0, 0), 3);
		this.map.put(new LSTTime(0, Schedule.LST_SLOTS_MINUTES), 5);
		this.map.put(new LSTTime(0, Schedule.LST_SLOTS_MINUTES * 2), 7);
		Assert.assertTrue(this.map.containsKey(new LSTTime(0,
				Schedule.LST_SLOTS_MINUTES)));
		Assert.assertTrue(this.map.containsValue(7));
		Assert.assertEquals(this.map.get(new LSTTime(0,
				Schedule.LST_SLOTS_MINUTES)), 5, 1e-5);
		Assert.assertEquals(this.map.size(), 3);

		Assert.assertNull(this.map.get(new LSTTime(0,
				Schedule.LST_SLOTS_MINUTES * 5)));
		Assert.assertFalse(this.map.containsKey(new LSTTime(0,
				Schedule.LST_SLOTS_MINUTES * 5)));

		Iterator<LSTTime> ks = this.map.keySet().iterator();
		Iterator<Integer> vs = this.map.values().iterator();
		Iterator<Entry<LSTTime, Integer>> es = this.map.entrySet().iterator();

		Assert.assertEquals(ks.next(), new LSTTime(0, 0));
		Assert.assertEquals(ks.next(), new LSTTime(0,
				Schedule.LST_SLOTS_MINUTES));
		Assert.assertEquals(ks.next(), new LSTTime(0,
				Schedule.LST_SLOTS_MINUTES * 2));
		Assert.assertFalse(ks.hasNext());

		Assert.assertEquals(3, vs.next(), 1e-5);
		Assert.assertEquals(5, vs.next(), 1e-5);
		Assert.assertEquals(7, vs.next(), 1e-5);
		Assert.assertFalse(vs.hasNext());

		Assert.assertEquals(es.next(), new SimpleEntry<LSTTime, Integer>(
				new LSTTime(0, 0), 3));
		Assert.assertEquals(es.next(), new SimpleEntry<LSTTime, Integer>(
				new LSTTime(0, Schedule.LST_SLOTS_MINUTES), 5));
		Assert.assertEquals(es.next(), new SimpleEntry<LSTTime, Integer>(
				new LSTTime(0, Schedule.LST_SLOTS_MINUTES * 2), 7));
		Assert.assertFalse(es.hasNext());

		Assert.assertNull(this.map.remove(new LSTTime(0,
				Schedule.LST_SLOTS_MINUTES * 5)));
		Assert.assertEquals(this.map.remove(new LSTTime(0,
				Schedule.LST_SLOTS_MINUTES)), 5, 1e-5);
		Assert.assertFalse(this.map.containsKey(new LSTTime(0,
				Schedule.LST_SLOTS_MINUTES)));
		Assert.assertEquals(this.map.size(), 3);

	}

	@Test
	public void testEquals() {
		LSTMap<Integer> othermap = new LSTMap<Integer>();
		map.put(new LSTTime(0, 0), 3);
		map.put(new LSTTime(0, Schedule.LST_SLOTS_MINUTES * 2), 7);
		othermap.put(new LSTTime(0, 0), 3);
		Assert.assertFalse(map.equals(othermap));
		othermap.put(new LSTTime(0, Schedule.LST_SLOTS_MINUTES * 2), 7);
		Assert.assertEquals(map, othermap);
		Assert.assertTrue(map.equals(othermap));
		Assert.assertTrue(othermap.equals(map));

		// changing value
		map.put(new LSTTime(0, Schedule.LST_SLOTS_MINUTES * 2), 4);
		Assert.assertFalse(map.equals(othermap));
		map.put(new LSTTime(0, Schedule.LST_SLOTS_MINUTES * 2), 7);
		Assert.assertEquals(map, othermap);

		// adding additional entry
		map.put(new LSTTime(0, Schedule.LST_SLOTS_MINUTES), 5);
		Assert.assertFalse(map.equals(othermap));
		map.remove(new LSTTime(0, Schedule.LST_SLOTS_MINUTES));
		Assert.assertEquals(map, othermap);
		// removing entry
		Assert.assertEquals(map.remove(new LSTTime(0,
				Schedule.LST_SLOTS_MINUTES * 2)), 7, 1e-5);
		Assert.assertFalse(map.equals(othermap));
	}

	@Test
	public void testMemoryUsage() {
		{
			log.debug("LSTMap");
			logMemoryUsage("l");
			log.debug("HashMap");
			logMemoryUsage("h");
			log.debug("TreeMap");
			logMemoryUsage("t");
		}
		{
			log.debug("HashMap");
			logMemoryUsage("h");
			log.debug("TreeMap");
			logMemoryUsage("t");
			log.debug("LSTMap");
			logMemoryUsage("l");
		}
	}

	private void logMemoryUsage(String s) {
		{
			if (s == "l")
				logMemoryUsage(new LSTMap<Integer>());
			if (s == "h")
				logMemoryUsage(new TreeMap<LSTTime, Integer>());
			if (s == "t")
				logMemoryUsage(new HashMap<LSTTime, Integer>());
		}
		System.gc();
	}

	public void logMemoryUsage(Map<LSTTime, Integer> map) {
		Runtime rt = Runtime.getRuntime();
		long diff;
		long base;
		LSTTime t;
		int i = 0;

		rt.gc();
		Thread.yield();
		rt.gc();
		Thread.yield();
		rt.gc();
		base = rt.freeMemory();

		for (LSTTimeIterator it = new LSTTimeIterator(new LSTTime(100, 0),
				ScheduleSpace.LST_SLOTS_MINUTES); it.hasNext();) {
			t = it.next();
			map.put(t, i++);
		}

		// rt.gc();
		diff = base - rt.freeMemory();
		log.debug("filled Map: " + diff);
		t = null;
		map = null;
		rt.gc();
	}
}
