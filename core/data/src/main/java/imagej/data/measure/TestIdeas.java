package imagej.data.measure;

import imagej.data.measure.measurements.ElementCount;
import imagej.data.measure.measurements.Mean;
import imagej.data.measure.measurements.SampleStdDev;
import net.imglib2.ops.function.Function;
import net.imglib2.ops.function.real.RealConstantFunction;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.type.numeric.integer.UnsignedByteType;


public class TestIdeas {

	public static void main(String[] args) {
		Function<long[],UnsignedByteType> func =
				new RealConstantFunction<long[], UnsignedByteType>(
						new UnsignedByteType(), 73.0);
		PointSet region = new HyperVolumePointSet(new long[]{25,25});
		NewMeasurementSet measures = new NewMeasurementSet();
		measures.addMeasure("count", ElementCount.class);
		measures.addMeasure("mean", Mean.class);
		measures.addMeasure("stdev", SampleStdDev.class);
		measures.doMeasurements(func, region);
		System.out.println("count = " + measures.getValue("count"));
		System.out.println("mean  = " + measures.getValue("mean"));
		System.out.println("stdev = " + measures.getValue("stdev"));
		
		// TODO - current test defines them in an order that should easily work.
		// When dependency checking code done enter them in random orders and they
		// should automatically work. Make such an example here.
	}
}
