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

import imagej.command.DynamicCommand;
import imagej.log.LogService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.script.ScriptService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * Command for executing an R script.
 * 
 * @author Curtis Rueden
 */
@Plugin(label = "Run R Script")
public class RunRScript extends DynamicCommand {

	@Parameter
	private LogService log;

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private String script;

	private final List<String> inputs = new ArrayList<String>();
	private final Map<String, Class<?>> inputTypes =
		new HashMap<String, Class<?>>();

	private final List<String> outputs = new ArrayList<String>();
	private final Map<String, Class<?>> outputTypes =
		new HashMap<String, Class<?>>();

	// -- Module methods --

	@Override
	public void initialize() {
		try {
			// parse input and output parameters from the script
			scriptService.parseParameters(
				new BufferedReader(new StringReader(script)), inputs, inputTypes,
				outputs, outputTypes);

			// register input and output parameters with the module
			for (final String input : inputs) {
				addInput(input, inputTypes.get(input));
			}
			for (final String output : outputs) {
				addOutput(output, outputTypes.get(output));
			}
		}
		catch (final IOException exc) {
			// FIXME
			throw new RuntimeException(exc);
		}
	}

	// -- Runnable methods --

	@Override
	public void run() {
		try {
			final RConnection c = new RConnection();

			// pass input values to the script
			for (final String name : inputs) {
				final Class<?> type = inputTypes.get(name);
				final Object value = get(name);
				set(c, name, type, value);
			}

			// execute script
			final REXP result = c.eval(script);

			// populate output value
			// FIXME: support multiple output values
			if (outputs.size() > 0) {
				final String outputName = outputs.get(0);
				final Object convertedValue = get(outputTypes.get(outputName), result);
				put(outputName, convertedValue);
			}
		}
		catch (final RserveException e) {
			// FIXME
			throw new RuntimeException(e);
		}
	}

	// -- Helper methods --

	/** Assigns the given value to a variable in R. */
	private void set(final RConnection c, final String name,
		final Class<?> type, final Object value)
	{
		try {
			RUtils.setVar(c, name, type, value);
		}
		catch (final RserveException exc) {
			log.error(exc);
		}
		catch (final REngineException exc) {
			log.error(exc);
		}
	}

	/** Extracts a value of the given type from the specified R variable. */
	private Object get(final Class<?> type, final REXP value) {
		try {
			return RUtils.getVar(type, value);
		}
		catch (final REXPMismatchException e) {
			throw new RuntimeException(e);
		}
	}

}
