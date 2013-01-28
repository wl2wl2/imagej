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

/**
 * Sets the units of a {@link ImageDisplay}. The units can differ from those of
 * the underlying {@link Dataset}s.
 * <p>
 * The existence of display space units and data space units facilitates later
 * conversions. Data space units are configured in {@link SetDatasetUnits}.
 * 
 * @author Barry DeZonia
 */
@Plugin(menuPath = "Image>Units>Set Display Units", initializer = "init")
public class SetDisplayUnits extends DynamicCommand {

	// -- Parameters --

	@Parameter
	private ImageDisplay display;

	// -- instance variables --

	private Axis[] axes;


	// -- Command methods --

	@Override
	public void run() {
		for (int i = 0; i < axes.length; i++) {
			String unitName = (String) getInput(axes[i].getLabel());
			axes[i].setUnit(unitName);
		}
	}

	// -- initializer --

	protected void init() {
		axes = display.getAxes();
		for (int i = 0; i < axes.length; i++) {
			final DefaultModuleItem<String> axisItem =
				new DefaultModuleItem<String>(this, axes[i].getLabel(), String.class);
			axisItem.setLabel(axes[i].getLabel());
			axisItem.setValue(this, axes[i].getUnit());
			axisItem.setPersisted(false);
			addInput(axisItem);
		}
	}
}
