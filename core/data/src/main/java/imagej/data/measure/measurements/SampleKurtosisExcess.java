package imagej.data.measure.measurements;

import imagej.data.measure.Measurement;


public class SampleKurtosisExcess implements Measurement {
	private SampleKurtosis sampKurt;
	
	public SampleKurtosisExcess(SampleKurtosis kurtosis) {
		this.sampKurt = kurtosis;
	}

	@Override
	public double getValue() {
		return sampKurt.getValue() - 3;
	}
}

