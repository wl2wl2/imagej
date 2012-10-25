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
import imagej.script.DefaultScriptService;
import imagej.script.ScriptService;
import imagej.service.ServiceHelper;

import java.io.StringReader;

import org.junit.Test;

public class RTest {

	@Test
	public void testBasic() throws Exception {
		// FIXME
		final ImageJ context = ImageJ.createContext();
		new ServiceHelper(context).createExactService(DefaultScriptService.class);
		final ScriptService scriptService = context.getService(ScriptService.class);
		new ServiceHelper(context).createExactService(DummyService.class);

		String script =
			"result = R.version.string\n";
		Object result = scriptService.eval("hello.r", new StringReader(script));
		System.out.println("result = " + result + " [" + result.getClass().getName() + "]");
//		assertEquals(context, dummy.context);
//		assertTrue(dummy.value.startsWith("R version "));
	}

}
