/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.data.autoscale;

import imagej.data.minmax.MinMaxMethod;

import java.util.List;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;

/**
 * Computes he data range from the entire set of values in an
 * {@link IterableInterval}.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = AutoscaleMethod.class, name = "Default")
public class DefaultAutoscaleMethod<T extends RealType<T>> extends AbstractAutoscaleMethod<T> {

	@Parameter
	private PluginService pluginService;

	@Override
	public DataRange getRange(IterableInterval<T> interval)
	{
		@SuppressWarnings("rawtypes")
		List<MinMaxMethod> lists =
			pluginService.createInstancesOfType(MinMaxMethod.class);
    @SuppressWarnings("unchecked")
		MinMaxMethod<T> minmax = lists.get(0);
		minmax.initialize(interval);
		minmax.process();
		double min = minmax.getMin().getRealDouble();
		double max = minmax.getMax().getRealDouble();
		// NB - never return a display range of zero
		if (min == max) max += 0.000000000000001;
		return new DataRange(min, max);
	}
}
