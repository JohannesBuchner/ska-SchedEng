package local.radioschedulers;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class GoodBadDateRangeRequirements implements DateRequirements {

	private ArrayList<Integer> good = new ArrayList<Integer>();
	private ArrayList<Integer> bad = new ArrayList<Integer>();

	@JsonIgnore
	private int ngooddays;

	public ArrayList<Integer> getGood() {
		return good;
	}

	public ArrayList<Integer> getBad() {
		return bad;
	}

	@JsonCreator
	GoodBadDateRangeRequirements(@JsonProperty("good") ArrayList<Integer> good,
			@JsonProperty("bad") ArrayList<Integer> bad) {
		this.good = good;
		this.bad = bad;
		updateNgooddays();
	}

	public GoodBadDateRangeRequirements(int ndays) {
		this(0, ndays);
	}

	public GoodBadDateRangeRequirements(int goodstart, int goodend) {
		addGoodRange(goodstart, goodend);
	}

	private void updateNgooddays() {
		int count = 0;
		for (int i = 0; i < this.good.size(); i += 2) {
			for (int d = this.good.get(i); d < this.good.get(i + 1); d++) {
				count++;
				for (int j = 0; j < this.bad.size(); j += 2) {
					if (d >= this.bad.get(j) && d <= this.bad.get(j)) {
						count--;
						break;
					}
				}
			}
		}
		this.ngooddays = count;
	}

	public void addGoodRange(int start, int end) {
		this.good.add(start);
		this.good.add(end);
		updateNgooddays();
	}

	public void addBadRange(int start, int end) {
		this.bad.add(start);
		this.bad.add(end);
		updateNgooddays();
	}

	@Override
	public Double requires(LSTTime t) {
		// must not be in bad ranges
		for (int i = 0; i < this.bad.size(); i += 2) {
			if (t.day >= this.bad.get(i) && t.day <= this.bad.get(i + 1)) {
				return 0.;
			}
		}
		// must be in good ranges
		for (int i = 0; i < this.good.size(); i += 2) {
			if (t.day >= this.good.get(i) && t.day <= this.good.get(i + 1)) {
				return 1. / ngooddays * ScheduleSpace.LST_SLOTS_PER_DAY;
			}
		}
		return 0.;
	}

	@Override
	public Integer totalRequired() {
		return ngooddays;
	}

}
