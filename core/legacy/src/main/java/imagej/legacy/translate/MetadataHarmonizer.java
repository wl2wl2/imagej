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

package imagej.legacy.translate;

import ij.ImagePlus;
import ij.measure.Calibration;
import imagej.data.Dataset;
import net.imglib2.Axis;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;

/**
 * Synchronizes metadata bidirectionally between a {@link Dataset} and an
 * {@link ImagePlus}.
 * 
 * @author Barry DeZonia
 */
public class MetadataHarmonizer implements DataHarmonizer {

	/** Sets a {@link Dataset}'s metadata to match a given {@link ImagePlus}. */
	@Override
	public void updateDataset(final Dataset ds, final ImagePlus imp) {
		ds.setName(imp.getTitle());
		// copy calibration info where possible
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		final ImgPlus<?> imgPlus = ds.getImgPlus();
		final Calibration cal = imp.getCalibration();
		Axis axis;
		if (xIndex >= 0) {
			axis = imgPlus.axis(xIndex);
			axis.setScale(cal.pixelWidth);
			axis.setOffset(cal.xOrigin);
			axis.setUnit(cal.getXUnit());
		}
		if (yIndex >= 0) {
			axis = imgPlus.axis(yIndex);
			axis.setScale(cal.pixelHeight);
			axis.setOffset(cal.yOrigin);
			axis.setUnit(cal.getYUnit());
		}
		// TODO - remove this next case?
		if (cIndex >= 0) {
			imgPlus.axis(cIndex).setScale(1);
		}
		if (zIndex >= 0) {
			axis = imgPlus.axis(zIndex);
			axis.setScale(cal.pixelDepth);
			axis.setOffset(cal.zOrigin);
			axis.setUnit(cal.getZUnit());
		}
		if (tIndex >= 0) {
			axis = imgPlus.axis(tIndex);
			axis.setScale(cal.frameInterval);
			axis.setOffset(0);
			axis.setUnit(cal.getTimeUnit());
		}
		// no need to ds.update() - these calls should track that themselves
	}

	/** Sets an {@link ImagePlus}' metadata to match a given {@link Dataset}. */
	@Override
	public void updateLegacyImage(final Dataset ds, final ImagePlus imp) {
		imp.setTitle(ds.getName());
		// copy calibration info where possible
		final Calibration cal = imp.getCalibration();
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		Axis axis;
		if (xIndex >= 0) {
			axis = ds.axis(xIndex);
			cal.pixelWidth = axis.getScale();
			cal.xOrigin = axis.getOffset();
			cal.setXUnit(axis.getUnit());
		}
		if (yIndex >= 0) {
			axis = ds.axis(yIndex);
			cal.pixelHeight = axis.getScale();
			cal.yOrigin = axis.getOffset();
			cal.setYUnit(axis.getUnit());
		}
		if (cIndex >= 0) {
			// nothing to set on IJ1 side
		}
		if (zIndex >= 0) {
			axis = ds.axis(zIndex);
			cal.pixelDepth = axis.getScale();
			cal.zOrigin = axis.getOffset();
			cal.setZUnit(axis.getUnit());
		}
		if (tIndex >= 0) {
			axis = ds.axis(tIndex);
			cal.frameInterval = axis.getScale();
			cal.setTimeUnit(axis.getUnit());
		}
	}
}
