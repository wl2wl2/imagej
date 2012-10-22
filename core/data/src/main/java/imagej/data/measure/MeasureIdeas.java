package imagej.data.measure;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealConstantFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;


/*

want to be able to build without passing func everywhere

so in general it's
  Mean mean = new Mean(new Sum(), new ElemCount());
  and then
  mean.calculate(function, region, output)

does this make construction get messy passing all these temps around?
    in general the construction is very difficult
	
	could have a MeasureSet that starts out empty
	set.add(Mean.class);
	  the set figures out what things the Mean needs (like Sum and ElemCount),
	  creates them, and passes them to Mean's constructor
	  (like curtis said we've done this kind of work for our plugins already)
  then calc all measures in the set (order does not matter)
  
  btw how would you construct a WeightedSum? How does the ctor know about
  the weights? Do you first register them with the set? What if you have
  two different set of weights in the set?
  
  could values get cached? this would solve Aivar's chained function idea.
  but the cost of generating a key and caching and retrieving might be
  too prohibitive as it is.
  
  anyhow chaining idea might be supportable by having all measures support
  the preproc(), visit(), postproc(). the dependencies of functions could
  be figured out and all of same level of dep can share the one visit. The
  MeasurementSet::measure() method would start iters for each level one at
  a time starting with the first. This is simple and would work.

  note: I can measure a weighted average by providing weights externally.
  But I can't think of any method that allows a new Measure to base itself
  on a weighted average that can somehow provide weights with no arg ctor

  what if you set up measure set empty. provide it with a Weights class that
  has to be a Measurement. Then when other Measurements are added if they
  need a Weights in their constructor they just use it. This would allow a
  single set of Weights but not more than one. We need a general solution.

*/

// TODO
// 1) construct all measures from the base user specified measures
//    and hatch new or reuse existing as needed
// 2) build set of DAGs and then populate levels with measures

public class MeasureIdeas {

	public class MeasurementSet {
		
		// -- instance variables --
		
		private List<Measurement> measurements;
		private Map<String,Measurement> namedMeasurements;
		private List<List<SamplingMeasurement>> samplingLevels;
		
		// -- constructor --
		
		public MeasurementSet() {
			this.measurements = new ArrayList<Measurement>();
			this.namedMeasurements = new HashMap<String,Measurement>();
			this.samplingLevels = new ArrayList<List<SamplingMeasurement>>();
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
				throw new IllegalStateException("can't create an instance");
			}
			Constructor<? extends Measurement> constructor = constructors[0]; 
			Class<? extends Measurement>[] paramTypes =
					(Class<? extends Measurement>[]) constructor.getParameterTypes();
			List<Measurement> measures = new ArrayList<Measurement>();
			for (Class<? extends Measurement> c : paramTypes) {
				measures.add(obtain(c));
			}
			try {
				Measurement measure = constructor.newInstance(measures);
				measurements.add(measure);
				return measure;
			}
			catch (Exception e) {
				throw new IllegalStateException("no constructor found for class "+clazz);
			}
		}
	}
}
