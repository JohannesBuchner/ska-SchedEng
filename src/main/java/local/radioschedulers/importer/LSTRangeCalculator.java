package local.radioschedulers.importer;

public class LSTRangeCalculator {

	private Double locationLatitude;
	private Double minimumAltitude;

	private Double ra;
	private Double dec;
	private Double lstmin;
	private Double lstmax;

	public void calculate() throws Exception {
		Double acosterm = (Math.sin(minimumAltitude) - Math
				.sin(locationLatitude)
				* Math.sin(dec))
				/ (Math.cos(locationLatitude) * Math.cos(dec));

		if (acosterm < -1) {
			// circumpolar!
			lstmin = 0.;
			lstmax = 23.99;
			return;
		}
		if (Math.abs(acosterm) > 1)
			throw new Exception(" " + ra + "/" + dec
					+ " never rises at latitude " + locationLatitude);
		lstmin = mod24((ra - Math.acos(acosterm)) / Math.PI * 12);
		lstmax = mod24((ra + Math.acos(acosterm)) / Math.PI * 12);
	}

	private Double mod24(double d) {
		d = d + 24;
		while (d > 24) {
			d -= 24;
		}
		return d;
	}

	public Double getLstmin() {
		return lstmin / 12 * Math.PI;
	}

	public Double getLstmax() {
		return lstmax / 12 * Math.PI;
	}

	public Double getLstminHours() {
		return lstmin;
	}

	public Double getLstmaxHours() {
		return lstmax;
	}

	public void setLocationLatitude(Double locationLatitude) {
		this.locationLatitude = locationLatitude;
	}

	public void setLocationLatitudeDegrees(Double locationLatitude) {
		this.locationLatitude = locationLatitude / 180 * Math.PI;
	}

	public void setMinimumAltitude(Double minimumAltitude) {
		this.minimumAltitude = minimumAltitude;
	}

	public void setMinimumAltitudeDegrees(Double minimumAltitude) {
		this.minimumAltitude = minimumAltitude / 180 * Math.PI;
	}

	public void setRa(Double ra) {
		this.ra = ra;
	}

	public void setDec(Double dec) {
		this.dec = dec;
	}

	public void setRaHours(Double ra) {
		this.ra = ra / 12 * Math.PI;
	}

	public void setDecDegrees(Double dec) {
		this.dec = dec / 180 * Math.PI;
	}
}
