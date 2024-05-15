/**
 * Copyright (c) 2022, 2024 Martin Erich Jobst
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Martin Jobst - initial API and implementation and/or initial documentation
 */
package org.eclipse.fordiac.ide.model.eval.value;

import org.eclipse.fordiac.ide.model.libraryElement.INamedElement;

public interface Value {
	/**
	 * Get the type of the value
	 *
	 * @return The type
	 */
	INamedElement getType();

	/**
	 * Returns a string representation of the value, with optional pretty
	 * formatting.
	 *
	 * @param pretty whether to use pretty formatting
	 * @see java.lang.Object#toString()
	 * @implNote defaults to {@link #toString()} if no optional pretty formatting is
	 *           supported
	 */
	default String toString(final boolean pretty) {
		return toString();
	}
}
