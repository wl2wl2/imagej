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

package imagej.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract superclass of {@link Module} implementations.
 * <p>
 * By default, input and output values are stored in a {@link HashMap}.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractModule implements Module {

	private final HashMap<String, Object> values;

	/** Table indicating resolved inputs. */
	private final HashSet<String> resolvedInputs;

	private MethodRef initializerRef;

	public AbstractModule() {
		values = new HashMap<String, Object>();
		resolvedInputs = new HashSet<String>();
	}

	// -- Module methods --

	@Override
	public void preview() {
		// do nothing by default
	}

	@Override
	public void cancel() {
		// do nothing by default
	}

	@Override
	public void initialize() {
		final Object delegateObject = getDelegateObject();
		if (initializerRef == null) {
			final String initializer = getInfo().getInitializer();
			initializerRef = new MethodRef(delegateObject.getClass(), initializer);
		}
		initializerRef.execute(delegateObject);
	}

	@Override
	public Object getDelegateObject() {
		return this;
	}

	@Override
	public Map<String, Object> getInputs() {
		return createMap(getInfo().inputs());
	}

	@Override
	public Map<String, Object> getOutputs() {
		return createMap(getInfo().outputs());
	}

	@Override
	public boolean isResolved(final String name) {
		return resolvedInputs.contains(name);
	}

	@Override
	public void setResolved(final String name, final boolean resolved) {
		if (resolved) resolvedInputs.add(name);
		else resolvedInputs.remove(name);
	}

	// -- Map methods --

	/** Gets the number of items in this module. */
	@Override
	public int size() {
		int size = 0;
		for (@SuppressWarnings("unused")
		final ModuleItem<?> item : getInfo().items())
		{
			size++;
		}
		return size;
	}

	/** Checks whether this module has no items. */
	@Override
	public boolean isEmpty() {
		return !getInfo().items().iterator().hasNext();
	}

	/** Checks whether this module has an item with the given name. */
	@Override
	public boolean containsKey(final Object name) {
		for (final ModuleItem<?> item : getInfo().items()) {
			if (item.getName().equals(name)) return true;
		}
		return false;
	}

	/** Checks whether given value is assigned to any module item. */
	@Override
	public boolean containsValue(final Object value) {
		return values.containsValue(value);
	}

	/** Gets the value of the module item with the given name. */
	@Override
	public Object get(final Object name) {
		return values.get(name);
	}

	/** Sets the value of the module item with the given name. */
	@Override
	public Object put(final String name, final Object value) {
		return values.put(name, value);
	}

	/** Clears the value for the module item with the given name. */
	@Override
	public Object remove(final Object name) {
		return values.remove(name);
	}

	/** Sets the values of the module items in the given table. */
	@Override
	public void putAll(final Map<? extends String, ? extends Object> m) {
		values.putAll(m);
	}

	/** Clears the values of all of this module's items. */
	@Override
	public void clear() {
		values.clear();
	}

	/** Gets the set of item names for this module. */
	@Override
	public Set<String> keySet() {
		final HashSet<String> set = new HashSet<String>();
		for (final ModuleItem<?> item : getInfo().items()) {
			set.add(item.getName());
		}
		return set;
	}

	/** Gets a collection of the values contained in the module. */
	@Override
	public Collection<Object> values() {
		return values.values();
	}

	/** Gets the set of (name, value) pairs for module items with values. */
	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return values.entrySet();
	}

	// -- Helper methods --

	private Map<String, Object> createMap(final Iterable<ModuleItem<?>> items) {
		final Map<String, Object> map = new HashMap<String, Object>();
		for (final ModuleItem<?> item : items) {
			final String name = item.getName();
			final Object value = get(name);
			map.put(name, value);
		}
		return map;
	}

}
