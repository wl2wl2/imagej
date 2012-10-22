package imagej.data.measure.measurements;

import imagej.data.measure.Measurement;


public class PopulationSkew implements Measurement {

	private Moment2AboutMean m2;
	private Moment3AboutMean m3;
	
	public PopulationSkew(Moment2AboutMean m2, Moment3AboutMean m3) {
		this.m2 = m2;
		this.m3 = m3;
	}

	@Override
	public double getValue() {
		return m3.getValue() / Math.pow(m2.getValue(), 1.5);
	}
	
}

