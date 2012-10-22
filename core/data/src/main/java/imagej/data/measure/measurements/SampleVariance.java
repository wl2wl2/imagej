package imagej.data.measure.measurements;

import imagej.data.measure.Measurement;


public class SampleVariance implements Measurement {

	private PopulationVariance popVar;
	private ElementCount numElems;
	
	public SampleVariance(PopulationVariance popVar, ElementCount numElems) {
		this.popVar = popVar;
		this.numElems = numElems;
	}
	
	@Override
	public double getValue() {
		double n = numElems.getValue();
		return n / (n-1) * popVar.getValue();
	}
	
}

