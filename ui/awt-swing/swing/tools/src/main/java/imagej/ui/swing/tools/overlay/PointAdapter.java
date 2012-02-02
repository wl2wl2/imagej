//
// PointAdapter.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ui.swing.tools.overlay;

import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.PointOverlay;
import imagej.ext.MouseCursor;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.Tool;
import imagej.ui.swing.overlay.JHotDrawOverlayAdapter;
import imagej.ui.swing.tools.AngleTool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import net.imglib2.RealPoint;

import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.AbstractHandle;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.geom.Geom;

/**
 * TODO
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "Point", description = "Point overlays",
	iconPath = "/icons/tools/point.png", priority = PointAdapter.PRIORITY,
	enabled = true)
@JHotDrawOverlayAdapter(priority = PointAdapter.PRIORITY)
public class PointAdapter extends AbstractJHotDrawOverlayAdapter<PointOverlay> {

	public static final int PRIORITY = AngleTool.PRIORITY - 1;

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof PointOverlay)) return false;
		return (figure == null) || (figure instanceof PointFigure);
	}

	@Override
	public PointOverlay createNewOverlay() {
		return new PointOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		final PointFigure figure = new PointFigure();
		figure.set(AttributeKeys.STROKE_COLOR, getDefaultStrokeColor());
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView overlayView, final Figure figure) {
		super.updateFigure(overlayView, figure);
		assert figure instanceof PointFigure;
		final PointFigure point = (PointFigure) figure;
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof PointOverlay;
		final PointOverlay pointOverlay = (PointOverlay) overlay;
		point.set(
				pointOverlay.getPoint().getDoublePosition(0),
				pointOverlay.getPoint().getDoublePosition(1)
				);
	}

	@Override
	public void updateOverlay(final Figure figure, final OverlayView overlayView)
	{
		super.updateOverlay(figure, overlayView);
		assert figure instanceof PointFigure;
		final PointFigure point = (PointFigure) figure;
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof PointOverlay;
		final PointOverlay pointOverlay = (PointOverlay) overlay;
		pointOverlay.setPoint(new RealPoint(new double[] {
			point.getX(), point.getY() }));
	}
	
	@Override
	public MouseCursor getCursor() {
		return MouseCursor.CROSSHAIR;
	}

	private class PointFigure extends AbstractAttributedFigure {
		protected Rectangle2D.Double bounds;
		
		/** Creates a new instance. */
		public PointFigure() {
			this(0, 0);
		}
		
		public PointFigure(double x, double y) {
			bounds = new Rectangle2D.Double(x, y, 1, 1); 
		}

		public void set(double x, double y) {
			bounds.x = x;
			bounds.y = y;
		}
		
		public double getX() { return bounds.x; }
		
		public double getY() { return bounds.y; }

		// DRAWING
		@Override
		protected void drawFill(Graphics2D g) {
			Rectangle2D.Double r = (Rectangle2D.Double) bounds.clone();
			double grow = AttributeKeys.getPerpendicularFillGrowth(this);
			Geom.grow(r, grow, grow);
			g.fill(r);
		}
		
		@Override
		protected void drawStroke(Graphics2D g) {
			Rectangle2D.Double r = (Rectangle2D.Double) bounds.clone();
			double grow = AttributeKeys.getPerpendicularDrawGrowth(this);
			Geom.grow(r, grow, grow);
			g.draw(r);
		}
		
		// SHAPE AND BOUNDS
		@Override
		public Rectangle2D.Double getBounds() {
			Rectangle2D.Double b = (Rectangle2D.Double) bounds.clone();
			return b;
		}
		
		@Override public Rectangle2D.Double getDrawingArea() {
			Rectangle2D.Double r = (Rectangle2D.Double) bounds.clone();
			double grow = AttributeKeys.getPerpendicularHitGrowth(this) + 1d;
			Geom.grow(r, grow, grow);
			return r;
		}
		
		/**
		 * Checks if a Point2D.Double is inside the figure.
		 */
		@Override
		public boolean contains(Point2D.Double p) {
			Rectangle2D.Double r = (Rectangle2D.Double) bounds.clone();
			double grow = AttributeKeys.getPerpendicularHitGrowth(this) + 1d;
			Geom.grow(r, grow, grow);
			return r.contains(p);
		}
		
		@Override
		public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
			bounds.x = anchor.x;
			bounds.y = anchor.y;
			bounds.width = 1;
			bounds.height = 1;
		}
		
		/**
		 * Moves the Figure to a new location.
		 * @param tx the transformation matrix.
		 */
		@Override
		public void transform(AffineTransform tx) {
			Point2D.Double anchor = new Point2D.Double(bounds.x, bounds.y);
			tx.transform(anchor,anchor);
			setBounds(anchor,anchor);
		}
		
		@Override
		public void restoreTransformTo(Object geometry) {
			bounds.setRect( (Rectangle2D.Double) geometry );
		}
		
		@Override
		public Object getTransformRestoreData() {
			return bounds.clone();
		}
		
		@Override
		public PointFigure clone() {
			PointFigure that = (PointFigure) super.clone();
			that.bounds = (Rectangle2D.Double) this.bounds.clone();
			return that;
		}
		
		// EVENT HANDLING
		
		@SuppressWarnings("synthetic-access")
		@Override
		public List<Handle> createHandles(int detailLevel) {
			Handle handle = new PointHandle(this);
			return Arrays.asList(handle);
		}
		
		@Override
		public void draw(Graphics2D g) {
			Color origC = g.getColor();
			int ctrX = (int) getX();
			int ctrY = (int) getY();
			g.setColor(Color.yellow);
			g.drawLine(ctrX-1, ctrY-1, ctrX-1, ctrY+1);
			g.drawLine(ctrX,   ctrY-1, ctrX,   ctrY+1);
			g.drawLine(ctrX+1, ctrY-1, ctrX+1, ctrY+1);
			g.setColor(Color.black);
			g.drawLine(ctrX-2, ctrY-2, ctrX+2, ctrY-2);
			g.drawLine(ctrX-2, ctrY-2, ctrX-2, ctrY+2);
			g.drawLine(ctrX+2, ctrY+2, ctrX+2, ctrY-2);
			g.drawLine(ctrX+2, ctrY+2, ctrX-2, ctrY+2);
			g.setColor(Color.white);
			g.drawLine(ctrX+3, ctrY,   ctrX+6, ctrY);
			g.drawLine(ctrX-3, ctrY,   ctrX-6, ctrY);
			g.drawLine(ctrX,   ctrY+3, ctrX,   ctrY+6);
			g.drawLine(ctrX,   ctrY-3, ctrX,   ctrY-6);
			g.setColor(origC);
		}
	}
	
	private class PointHandle extends AbstractHandle {

		private PointFigure figure;
		
		private PointHandle(PointFigure fig) {
			super(fig);
			figure = fig;
		}

		@Override
		public void trackEnd(Point anchor, Point lead, int modifiers) {
			double currX = figure.getX();
			double currY = figure.getY();
			double dx = lead.x - anchor.x;
			double dy = lead.y - anchor.y;
			figure.set(currX + dx, currY + dy);
		}

		@Override
		public void trackStart(Point anchor, int modifiers) {
			// do nothing
		}

		@Override
		public void trackStep(Point anchor, Point lead, int modifiers) {
			// do nothing
		}

		@Override
		protected Rectangle basicGetBounds() {
			Rectangle rect = new Rectangle();
			Rectangle2D.Double bounds = figure.getBounds();
			rect.x = (int) bounds.x;
			rect.y = (int) bounds.y;
			rect.width = (int) bounds.width;
			rect.height = (int) bounds.height;
			return rect;
		}
	}
}