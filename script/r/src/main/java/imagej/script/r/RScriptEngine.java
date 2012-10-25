/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package imagej.script.r;

import imagej.ImageJ;
import imagej.command.CommandService;
import imagej.script.AbstractScriptEngine;

import java.util.concurrent.ExecutionException;

import javax.script.ScriptException;

/**
 * Scripting engine for R, using <a
 * href="http://www.rforge.net/Rserve/">Rserve</a>.
 */
public class RScriptEngine extends AbstractScriptEngine {

	public RScriptEngine() {
		// FIXME
		engineScopeBindings = new RBindings();
	}

	@Override
	public Object eval(final String script) throws ScriptException {
//		final ImageJ context = getIJContext();
		final ImageJ context = ImageJ.getContext(); // FIXME
		final CommandService commandService =
			context.getService(CommandService.class);
		try {
			commandService.run(RunRScript.class, "script", script).get();
			return null;
		}
		catch (final InterruptedException e) {
			throw new ScriptException(e);
		}
		catch (final ExecutionException e) {
			throw new ScriptException(e);
		}
	}

}
