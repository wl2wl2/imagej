package imagej.data.measure;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.ops.function.Function;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;


public class NewMeasurementSet {
	
	// -- instance variables --
	
	private List<Measurement> measurements;
	private Map<String,Measurement> namedMeasurements;
	private List<List<SamplingMeasurement>> samplingLevels;
	
	// -- constructor --
	
	public NewMeasurementSet() {
		this.measurements = new ArrayList<Measurement>();
		this.namedMeasurements = new HashMap<String,Measurement>();
		this.samplingLevels = new ArrayList<List<SamplingMeasurement>>();
		samplingLevels.add(new ArrayList<SamplingMeasurement>());
	}

	// -- public api --
	
	// named measures are the retrievable measures (added here)
	//   there are numerous other measures that get hatched and computed but
	//   not retrievable
	
	public void addMeasure(String name,
		Class<? extends Measurement> measuringClass, Object... params)
	{
		// allocate Measurement passing correct values to ctor
		// update dependency graph
		// if measureClass already present use it else allocate a new one
		Measurement measurement = obtain(measuringClass);
		namedMeasurements.put(name, measurement);
	}

	// Getting a correct value for a measure depends upon its parents being
	// calced. At some level it all falls back to sampling the data at approp
	// times. So we make sure that parents are all sampled in the right order.
	
	public <T extends RealType<T>>
	void doMeasurements(Function<long[],T> func, PointSet region)
	{
		T tmp = func.createOutput();
		PointSetIterator iter = region.createIterator();
		for (List<SamplingMeasurement> level : samplingLevels) {
			for (SamplingMeasurement measuring : level) {
				measuring.preprocess(region.getOrigin());
			}
			iter.reset();
			while (iter.hasNext()) {
				long[] pos = iter.next();
				func.compute(pos, tmp);
				for (SamplingMeasurement measuring : level) {
					measuring.dataValue(pos, tmp.getRealDouble());
				}
			}
			for (SamplingMeasurement measuring : level) {
				measuring.postprocess();
			}
		}
	}
	
	public double getValue(String name) {
		Measurement m = namedMeasurements.get(name);
		if (m == null) return Double.NaN;
		return m.getValue();
	}

	// -- private helpers --
	
	private Measurement lookupMeasurement(Class<? extends Measurement> clazz) {
		for (Measurement m : measurements) {
			if (m.getClass() == clazz) return m;
		}
		return null;
	}
	
	private Measurement obtain(Class<? extends Measurement> clazz)
	{
		Measurement m = lookupMeasurement(clazz);
		if (m != null) return m;
		
		Constructor<? extends Measurement>[] constructors =
				(Constructor<? extends Measurement>[]) clazz.getConstructors();
		try {
			if (constructors.length == 0) return clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage());
		}
		Constructor<? extends Measurement> constructor = constructors[0]; 
		Class<? extends Measurement>[] paramTypes =
				(Class<? extends Measurement>[]) constructor.getParameterTypes();
		Object[] measures = new Object[paramTypes.length];
		int i = 0;
		for (Class<? extends Measurement> c : paramTypes) {
			measures[i++] = obtain(c);
		}
		try {
			Measurement measure = constructor.newInstance(measures);
			measurements.add(measure);
			if (measure instanceof SamplingMeasurement)
				samplingLevels.get(0).add((SamplingMeasurement)measure);
			return measure;
		}
		catch (Exception e) {
			throw new IllegalStateException(e.getMessage());
		}
	}
}
