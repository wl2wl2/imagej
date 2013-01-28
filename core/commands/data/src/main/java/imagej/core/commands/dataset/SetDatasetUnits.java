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

package imagej.core.commands.dataset;

import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.module.DefaultModuleItem;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import net.imglib2.Axis;

// TODO - this dialog could be redesigned. One could choose axis type (linear,
// log, etc.). There would be Configure buttons per axis and it would fire the
// correct kind of dialog for the axis type. Right now this version supports
// linear only and is like IJ1.

// TODO - Make the dialog prettier (i.e. unitname, scale, offset on one line)

/**
 * Sets the calibration for the axes of a {@link Dataset}. The units can differ
 * from those of the containing {@link ImageDisplay}.
 * <p>
 * The existence of display space units and data space units facilitates later
 * conversions. Data space units are configured in {@link SetDisplayUnits}.
 * 
 * @author Barry DeZonia
 */
@Plugin(menuPath = "Image>Units>Set Data Units", initializer = "init")
public class SetDatasetUnits extends DynamicCommand {

	// -- Parameters --

	@Parameter
	private Dataset dataset;

	// -- instance variables --

	private Axis[] axes;


	// -- Command methods --

	@Override
	public void run() {
		for (Axis axis : axes) {
			String unitName = (String) getInput(unitLabel(axis));
			double offset = (Double) getInput(offsetLabel(axis));
			double scale = (Double) getInput(scaleLabel(axis));
			axis.setUnit(unitName);
			axis.setOffset(offset);
			axis.setScale(scale);
		}
	}

	// -- initializer --

	protected void init() {
		axes = dataset.getAxes();
		setupUnitItems();
		setupOffsetItems();
		setupScaleItems();
	}

	// -- helpers --

	private void setupUnitItems() {
		for (Axis axis : axes) {
			final DefaultModuleItem<String> axisItem =
				new DefaultModuleItem<String>(this, unitLabel(axis), String.class);
			axisItem.setLabel(unitLabel(axis));
			axisItem.setValue(this, axis.getUnit());
			axisItem.setPersisted(false);
			addInput(axisItem);
		}
	}

	private void setupOffsetItems() {
		for (Axis axis : axes) {
			final DefaultModuleItem<Double> axisItem =
				new DefaultModuleItem<Double>(this, offsetLabel(axis), Double.class);
			axisItem.setLabel(offsetLabel(axis));
			axisItem.setValue(this, axis.getOffset());
			axisItem.setPersisted(false);
			addInput(axisItem);
		}
	}

	private void setupScaleItems() {
		for (Axis axis : axes) {
			final DefaultModuleItem<Double> axisItem =
				new DefaultModuleItem<Double>(this, scaleLabel(axis), Double.class);
			axisItem.setLabel(scaleLabel(axis));
			axisItem.setValue(this, axis.getScale());
			axisItem.setPersisted(false);
			addInput(axisItem);
		}
	}

	private String unitLabel(Axis axis) {
		return axis.getLabel() + " unit";
	}

	private String offsetLabel(Axis axis) {
		return axis.getLabel() + " offset";
	}

	private String scaleLabel(Axis axis) {
		return axis.getLabel() + " scale";
	}
}
