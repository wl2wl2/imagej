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

import imagej.plugin.Plugin;
import imagej.script.AbstractScriptEngineFactory;
import imagej.script.ScriptLanguage;

import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;

/**
 * An adapter of the R interpreter to ImageJ's scripting interfaces.
 * 
 * @author Curtis Rueden
 * @see ScriptEngine
 */
@Plugin(type = ScriptLanguage.class)
public class R extends AbstractScriptEngineFactory {

	@Override
	public List<String> getExtensions() {
		return Arrays.asList("r");
	}

	@Override
	public String getEngineName() {
		return "Rserve";
	}

	@Override
	public String getLanguageName() {
		return "R";
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new RScriptEngine();
	}

}
