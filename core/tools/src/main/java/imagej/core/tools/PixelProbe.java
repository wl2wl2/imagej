/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.core.tools;

import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.display.event.input.MsMovedEvent;
import imagej.event.StatusService;
import imagej.plugin.Plugin;
import imagej.tool.AbstractTool;
import imagej.tool.Tool;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.ops.function.Function;
import net.imglib2.type.numeric.real.DoubleType;
import ucar.units.SI;
import ucar.units.StandardUnitDB;
import ucar.units.Unit;

/**
 * Displays pixel values under the cursor.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "Probe", alwaysActive = true)
public class PixelProbe extends AbstractTool {

	private final PixelRecorder recorder = new PixelRecorder(false);

	// -- Tool methods --

	// NB - this tool does not consume the events by design
	
	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		final StatusService statusService =
				evt.getContext().getService(StatusService.class);
		final ImageDisplayService dispService =
				evt.getContext().getService(ImageDisplayService.class);
		final ImageDisplay disp = dispService.getActiveImageDisplay();
		if ((disp == null) || !recorder.record(evt)) {
			statusService.clearStatus();
			return;
		}
		final int xAxis = disp.getAxisIndex(Axes.X); 
		final int yAxis = disp.getAxisIndex(Axes.Y);
		final double xcal = disp.calibration(xAxis);
		final double ycal = disp.calibration(yAxis);
		final int channelIndex = disp.getAxisIndex(Axes.CHANNEL);
		final long cx = recorder.getCX();
		final long cy = recorder.getCY();
		ChannelCollection values = recorder.getValues();
		Dataset ds = dispService.getActiveDataset(disp);
		ImgPlus<?> imgPlus = ds.getImgPlus();
		StringBuilder builder = new StringBuilder();
		builder.append("x=");
		appendAxisValue(builder, cx, xcal, imgPlus.calibrationUnit(xAxis));
		builder.append(", y=");
		appendAxisValue(builder, cy, ycal, imgPlus.calibrationUnit(yAxis));
		builder.append(", value=");
		// single channel image
		if ((channelIndex == -1) ||
				(recorder.getDataset().dimension(channelIndex) == 1))
		{
			String valueStr = valueString(values.getChannelValue(0));
			builder.append(valueStr);
		}
		else { // has multiple channels
			int currChannel = disp.getIntPosition(channelIndex);
			String valueStr = valueString(values.getChannelValue(currChannel));
			builder.append(valueStr);
			builder.append(" from (");
			for (int i = 0; i < values.getChannelCount(); i++) {
				valueStr = valueString(values.getChannelValue(i));
				if (i > 0) builder.append(",");
				builder.append(valueStr);
			}
			builder.append(")");
		}
		statusService.showStatus(builder.toString());
	}
	
	// -- helpers --
	
	private String valueString(double value) {
		if (recorder.getDataset().isInteger())
			return String.format("%d",(long)value);
		return String.format("%f", value);
	}

	private void appendAxisValue(StringBuilder builder, long value, double scale,
		String unitName)
	{
		Function<Double, DoubleType> scalingFunc =
			new PowerScalingFunction(0.0, scale, 2);
		if (Double.isNaN(scale)) {
			builder.append(value);
			return;
		}
		Unit userUnit;
		try {
			userUnit = StandardUnitDB.instance().get(unitName);
		}
		catch (Exception e) {
			userUnit = null;
		}
		DoubleType output = new DoubleType();
		scalingFunc.compute((double) value, output);
		double scaledValue = output.getRealDouble();
		if (userUnit == null) {
			String calibratedVal = String.format("%.2f", scaledValue);
			builder.append(calibratedVal);
		}
		else { // userName != null
			double val;
			try {
				val = userUnit.convertTo(scaledValue, SI.METER);
			}
			catch (Exception e) {
				val = scaledValue;
			}
			builder.append(val);
			builder.append(" m");
		}
	}

	// NOTE that one could use any text specified equation as a scaling axis! It
	// might require minor edits.

	private class LinearScalingFunction implements Function<Double, DoubleType> {

		private final double offset, scale;

		public LinearScalingFunction(double offset, double scale) {
			this.offset = offset;
			this.scale = scale;
		}

		@Override
		public void compute(Double input, DoubleType output) {
			double value = offset + scale * input;
			output.setReal(value);
		}

		@Override
		public DoubleType createOutput() {
			return new DoubleType();
		}

		@Override
		public LinearScalingFunction copy() {
			return new LinearScalingFunction(offset, scale);
		}

	}

	// TODO - do we need an InverseLogScalingFunction?

	// NB - only works for input values >= 0. For values < 1 the output is large
	// and negative. Users need to think about best way to handle these
	// constraints for their particular case.

	private class LogScalingFunction implements Function<Double, DoubleType> {

		private final double offset, scale;

		public LogScalingFunction(double offset, double scale) {
			this.offset = offset;
			this.scale = scale;
		}

		@Override
		public void compute(Double input, DoubleType output) {
			double value = offset + scale * Math.log(input);
			output.setReal(value);
		}

		@Override
		public DoubleType createOutput() {
			return new DoubleType();
		}

		@Override
		public LogScalingFunction copy() {
			return new LogScalingFunction(offset, scale);
		}

	}

	private class PowerScalingFunction implements Function<Double, DoubleType> {

		private final double offset, scale, power;

		public PowerScalingFunction(double offset, double scale, double power) {
			this.offset = offset;
			this.scale = scale;
			this.power = power;
		}

		@Override
		public void compute(Double input, DoubleType output) {
			double value = offset + scale * Math.pow(input, power);
			output.setReal(value);
		}

		@Override
		public DoubleType createOutput() {
			return new DoubleType();
		}

		@Override
		public PowerScalingFunction copy() {
			return new PowerScalingFunction(offset, scale, power);
		}

	}

	// NB - can match Math.exp() behavior by passing Math.E as base.

	private class ExponentialScalingFunction implements
		Function<Double, DoubleType>
	{

		private final double offset, scale, base;

		public ExponentialScalingFunction(double offset, double scale, double base)
		{
			this.offset = offset;
			this.scale = scale;
			this.base = base;
		}

		@Override
		public void compute(Double input, DoubleType output) {
			double value = offset + scale * Math.pow(base, input);
			output.setReal(value);
		}

		@Override
		public DoubleType createOutput() {
			return new DoubleType();
		}

		@Override
		public ExponentialScalingFunction copy() {
			return new ExponentialScalingFunction(offset, scale, base);
		}

	}

}
