package imagej.script;

import imagej.log.LogService;
import imagej.plugin.PluginService;
import imagej.service.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 * A service discovering all available script languages and convenience methods
 * to interact with them.
 * 
 * @author Johannes Schindelin
 */
public interface ScriptService extends Service {

	/**
	 * The script service puts the current ImageJ context into the engine's bindings
	 * using this key. That way, scripts can access the context by accessing the global
	 * variable of that name.
	 */
	final static String CONTEXT = "IJ";

	PluginService getPluginService();

	LogService getLogService();

	/** Gets the index of available script languages. */
	ScriptLanguageIndex getIndex();

	List<ScriptEngineFactory> getLanguages();

	ScriptEngineFactory getByFileExtension(final String fileExtension);

	ScriptEngineFactory getByName(final String name);

	Object eval(final File file) throws FileNotFoundException, ScriptException;

	Object eval(final String filename, final Reader reader) throws IOException,
		ScriptException;

	boolean canHandleFile(final File file);

	boolean canHandleFile(final String fileName);

	void initialize(final ScriptEngine engine, final String fileName,
		final Writer writer, final Writer errorWriter);

	boolean isCompiledLanguage(ScriptEngineFactory currentLanguage);

	void parseParameters(BufferedReader in, List<String> inputs,
		Map<String, Class<?>> inputTypes, List<String> outputs,
		Map<String, Class<?>> outputTypes) throws IOException;

}
