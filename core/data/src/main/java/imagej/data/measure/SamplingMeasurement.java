package imagej.data.measure;


public interface SamplingMeasurement extends Measurement {
	void preprocess(long[] origin);
	void dataValue(long[] position, double value);
	void postprocess();
}

