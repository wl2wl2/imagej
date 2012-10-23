package imagej.data.measure;

import imagej.data.measure.measurements.ElementCount;
import imagej.data.measure.measurements.Mean;
import imagej.data.measure.measurements.SampleStdDev;
import imagej.data.measure.measurements.SampleVariance;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealEquationFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.real.DoubleType;


public class TestIdeas {

	public static void main(String[] args) {
		Function<long[],DoubleType> func =
				new RealEquationFunction<DoubleType>("[x,y], 2*x + 3*y + 7", new DoubleType(), null);
		PointSet region = new HyperVolumePointSet(new long[]{25,25});
		NewMeasurementSet measures = new NewMeasurementSet();
		measures.addMeasure("mean", Mean.class);
		measures.addMeasure("stdev", SampleStdDev.class);
		measures.addMeasure("count", ElementCount.class);
		measures.addMeasure("var", SampleVariance.class);
		measures.doMeasurements(func, region);
		System.out.println("count = " + measures.getValue("count"));
		System.out.println("mean  = " + measures.getValue("mean"));
		System.out.println("stdev = " + measures.getValue("stdev"));
		System.out.println("var   = " + measures.getValue("var"));
		
		// TODO - current test defines them in an order that should easily work.
		// When dependency checking code done enter them in random orders and they
		// should automatically work. Make such an example here.
	}
}
