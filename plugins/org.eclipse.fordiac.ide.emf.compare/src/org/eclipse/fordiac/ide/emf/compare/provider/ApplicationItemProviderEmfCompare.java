/*******************************************************************************
 * Copyright (c) 2023 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Fabio Gandolfi - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.emf.compare.provider;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.fordiac.ide.model.libraryElement.Application;
import org.eclipse.fordiac.ide.systemmanagement.ui.providers.ApplicationItemProviderForSystem;

public class ApplicationItemProviderEmfCompare extends ApplicationItemProviderForSystem {

	public ApplicationItemProviderEmfCompare(final AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public boolean hasChildren(final Object object) {
		return true;
	}

	@Override
	public Collection<?> getChildren(final Object object) {
		return Collections.singletonList(((Application) object).getFBNetwork());
	}

}