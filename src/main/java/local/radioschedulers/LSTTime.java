package local.radioschedulers;

/**
 * Fundamental time unit, made up of hours and minutes. LST = Local Sidereal
 * Time
 * 
 * @author Johannes Buchner
 */
public class LSTTime implements Comparable<LSTTime> {
	public LSTTime(Long day, Long minute) {
		super();
		this.day = day;
		this.minute = minute;
	}

	public LSTTime(String s) {
		String[] parts = s.split(":", 2);
		this.day = Long.parseLong(parts[0]);
		this.minute = Long.parseLong(parts[1]);
	}

	public LSTTime(int i, int j) {
		this(Long.valueOf(i), Long.valueOf(j));
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
