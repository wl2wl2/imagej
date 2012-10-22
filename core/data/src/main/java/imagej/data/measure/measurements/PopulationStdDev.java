package imagej.data.measure.measurements;

import imagej.data.measure.Measurement;


public class PopulationStdDev implements Measurement {

	private PopulationVariance popVar;
	
	public PopulationStdDev(PopulationVariance popVar) {
		this.popVar = popVar;
	}
	
	@Override
	public double getValue() {
		return Math.sqrt(popVar.getValue());
	}

}

