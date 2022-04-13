/*******************************************************************************
 * Copyright (c) 2022 Martin Erich Jobst
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Martin Jobst - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.structuredtextalgorithm

import com.google.inject.Binder
import com.google.inject.name.Names
import org.eclipse.fordiac.ide.structuredtextalgorithm.resource.STAlgorithmResource
import org.eclipse.fordiac.ide.structuredtextalgorithm.scoping.STAlgorithmImportedNamespaceAwareLocalScopeProvider
import org.eclipse.fordiac.ide.structuredtextcore.converter.STCoreValueConverters
import org.eclipse.xtext.scoping.IScopeProvider
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
class STAlgorithmRuntimeModule extends AbstractSTAlgorithmRuntimeModule {
	override bindIValueConverterService() {
		return STCoreValueConverters;
	}

	override bindXtextResource() {
		return STAlgorithmResource
	}

	override configureIScopeProviderDelegate(Binder binder) {
		binder.bind(IScopeProvider).annotatedWith(Names.named(AbstractDeclarativeScopeProvider.NAMED_DELEGATE)).to(
			STAlgorithmImportedNamespaceAwareLocalScopeProvider)
	}
}
