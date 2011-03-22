package local.radioschedulers;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * Fundamental time unit, made up of hours and minutes. LST = Local Sidereal
 * Time
 * 
 * @author Johannes Buchner
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class LSTTime implements Comparable<LSTTime> {
	@JsonCreator
	public LSTTime(@JsonProperty("day") Long day,
			@JsonProperty("minute") Long minute) {
		super();
		this.day = day;
		this.minute = minute;
		if (this.minute > Schedule.LST_SLOTS_MINUTES
				* Schedule.LST_SLOTS_PER_DAY)
			throw new IllegalArgumentException("minute argument too large: "
					+ this.minute);
	}

	public LSTTime(String s) {
		String[] parts = s.split(":", 2);
		this.day = Long.parseLong(parts[0]);
		this.minute = Long.parseLong(parts[1]);
		if (this.minute > Schedule.LST_SLOTS_MINUTES
				* Schedule.LST_SLOTS_PER_DAY)
			throw new IllegalArgumentException("minute argument too large: "
					+ this.minute);
	}

	public LSTTime(int day, int minute) {
		this(Long.valueOf(day), Long.valueOf(minute));
	}

	public Long day;
	public Long minute;

	@Override
	public int compareTo(LSTTime o) {
		int v = day.compareTo(o.day);
		if (v == 0)
			return minute.compareTo(o.minute);
		else
			return v;
	}

	@JsonIgnore
	public boolean isBefore(LSTTime o) {
		return compareTo(o) < 0;
	}
	@JsonIgnore
	public boolean isAfter(LSTTime o) {
		return compareTo(o) > 0;
	}

	@Override
	public String toString() {
		return day + ":" + minute;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + ((minute == null) ? 0 : minute.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LSTTime other = (LSTTime) obj;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.equals(other.day))
			return false;
		if (minute == null) {
			if (other.minute != null)
				return false;
		} else if (!minute.equals(other.minute))
			return false;
		return true;
	}

}
