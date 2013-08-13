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

package imagej;

import fiji.PerformanceProfiler;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Launches ImageJ.
 * 
 * @author Curtis Rueden
 */
public final class Main {

	private Main() {
		// prevent instantiation of utility class
	}

public static long cumulative, counter;
static void patch() {
	try {
		ClassPool cp = ClassPool.getDefault();
		CtClass c = cp.get("imagej.command.CommandInfo");
		//final String millis = "java.lang.System.currentTimeMillis();";
		final String millis = "System.nanoTime();";
		for (CtBehavior b : c.getDeclaredConstructors()) {
			b.insertBefore("imagej.Main.cumulative -= " + millis);
			b.insertAfter("imagej.Main.cumulative +=  " + millis);
		}
		c = cp.get("imagej.command.DefaultCommandService");
		//CtBehavior b = c.getDeclaredMethod("wrapAsCommand");
		CtBehavior b = c.getDeclaredMethod("addCommands");
		b.instrument(new ExprEditor() {
			@Override
			public void edit(MethodCall call) throws CannotCompileException {
System.err.println("call: " + call.getMethodName());
				if (call.getMethodName().equals("wrapAsCommand")) {
System.err.println("Yes");
					//call.replace("$_ = wrapAsCommand($1); imagej.Main.cumulative += " + millis);
				}
			}
		});

		c = cp.get("imagej.module.DefaultModuleService");
		b = c.getDeclaredMethod("addModules");
		b.insertBefore("imagej.Main.cumulative -= " + millis);
		b.insertAfter("imagej.Main.cumulative += " + millis);

		c = cp.get("org.scijava.plugin.PluginIndex");
		for (CtBehavior b2 : c.getDeclaredConstructors()) {
			b2.insertBefore("imagej.Main.cumulative -= " + millis);
			b2.insertAfter("imagej.Main.cumulative +=  " + millis);
		}

		c.toClass();
	} catch (Throwable t) {
		t.printStackTrace();
	}
}
	/**
	 * Launches a new instance of ImageJ, displaying the default user interface.
	 * <p>
	 * This method is provided merely for convenience. If you do not want to
	 * display a user interface, construct the ImageJ instance directly instead:
	 * </p>
	 * {@code
	 * final ImageJ ij = new ImageJ();<br/>
	 * ij.console().processArgs(args); // if you want to pass any arguments
	 * }
	 * 
	 * @param args The arguments to pass to the new ImageJ instance.
	 * @return The newly launched ImageJ instance.
	 */
	public static ImageJ launch(final String... args) {
Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
//patch();
long start = System.nanoTime();
//PerformanceProfiler.setActive(true);
		final ImageJ ij = new ImageJ();
//PerformanceProfiler.report(System.err);
long end = System.nanoTime();
System.err.println("Cumulative: " + ((end - start)/1e6));
System.err.println("Cumulative2: " + (cumulative/1e6));
//if (true) return null;

		// parse command line arguments
		ij.console().processArgs(args);

		// display the user interface
		ij.ui().showUI();

		return ij;
	}

	public static void main(final String... args) throws Throwable {
//if (PerformanceProfiler.startProfiling(Main.class.getName(), args)) return;
//PerformanceProfiler.setActive(false);
		launch(args);
	}

}
