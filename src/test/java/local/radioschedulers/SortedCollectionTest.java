package local.radioschedulers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import local.radioschedulers.ga.watchmaker.SortedCollection;
import local.radioschedulers.ga.watchmaker.SortedCollection.MappingFunction;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class SortedCollectionTest {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(SortedCollectionTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAddSeveral() throws Exception {
		List<Integer> items = new ArrayList<Integer>();
		items.add(1);
		items.add(3);
		items.add(2);
		items.add(5);
		SortedCollection<Integer> coll = new SortedCollection<Integer>(items,
				new MappingFunction<Integer, Integer>() {

					@Override
					public Integer map(Integer item) {
						return item;
					}
				});
		Iterator<Integer> it = coll.iterator();
		Assert.assertEquals(1, it.next().intValue());
		Assert.assertEquals(2, it.next().intValue());
		Assert.assertEquals(3, it.next().intValue());
		Assert.assertEquals(5, it.next().intValue());
		Assert.assertFalse(it.hasNext());
	}

	@Test
	public void testAllSameValue() throws Exception {
		List<Integer> items = new ArrayList<Integer>();
		items.add(1);
		items.add(3);
		items.add(2);
		items.add(5);
		SortedCollection<Integer> coll = new SortedCollection<Integer>(items,
				new MappingFunction<Integer, Integer>() {

					@Override
					public Integer map(Integer item) {
						return 0;
					}
				});
		Iterator<Integer> it = coll.iterator();
		Assert.assertEquals(1, it.next().intValue());
		Assert.assertEquals(3, it.next().intValue());
		Assert.assertEquals(2, it.next().intValue());
		Assert.assertEquals(5, it.next().intValue());
		Assert.assertFalse(it.hasNext());
	}
}
