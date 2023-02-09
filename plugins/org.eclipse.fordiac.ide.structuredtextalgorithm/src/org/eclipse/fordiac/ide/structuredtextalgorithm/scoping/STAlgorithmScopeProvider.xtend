/*******************************************************************************
 * Copyright (c) 2022-2023 Martin Erich Jobst
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
package org.eclipse.fordiac.ide.structuredtextalgorithm.scoping

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.fordiac.ide.model.libraryElement.FB
import org.eclipse.fordiac.ide.model.libraryElement.LibraryElementPackage
import org.eclipse.fordiac.ide.model.typelibrary.DataTypeLibrary
import org.eclipse.fordiac.ide.structuredtextalgorithm.stalgorithm.STAlgorithmPackage
import org.eclipse.fordiac.ide.structuredtextalgorithm.stalgorithm.STAlgorithmSource
import org.eclipse.fordiac.ide.structuredtextalgorithm.stalgorithm.STMethod
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STBuiltinFeatureExpression
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STCorePackage
import org.eclipse.xtext.resource.IEObjectDescription

import static extension org.eclipse.emf.ecore.util.EcoreUtil.*

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
class STAlgorithmScopeProvider extends AbstractSTAlgorithmScopeProvider {
	override getScope(EObject context, EReference reference) {
		if (reference == STAlgorithmPackage.Literals.ST_METHOD__RETURN_TYPE) {
			val globalScope = super.getScope(context, reference)
			return scopeFor(DataTypeLibrary.nonUserDefinedDataTypes, globalScope)
		} else if (reference == STCorePackage.Literals.ST_FEATURE_EXPRESSION__FEATURE) {
			switch (receiver : context.receiver) {
				STBuiltinFeatureExpression: {
					switch (receiver.feature) {
						case THIS: {
							val source = context.rootContainer
							if (source instanceof STAlgorithmSource) {
								return scopeFor(source.elements.filter(STMethod), super.getScope(context, reference))
							}
						}
					}
				}
			}
		}
		return super.getScope(context, reference)
	}

	override protected isApplicableForVariableReference(IEObjectDescription description) {
		val clazz = description.EClass
		super.isApplicableForVariableReference(description) &&
			!(LibraryElementPackage.eINSTANCE.getVarDeclaration().isSuperTypeOf(clazz) &&
				description.EObjectOrProxy.eContainer?.eContainer instanceof FB); // ensure the VarDeclaration is not in an internal FB
	}

	override protected isApplicableForFeatureReference(IEObjectDescription description) {
		val clazz = description.EClass
		super.isApplicableForFeatureReference(description) &&
			!(LibraryElementPackage.eINSTANCE.getVarDeclaration().isSuperTypeOf(clazz) &&
				description.EObjectOrProxy.eContainer?.eContainer instanceof FB) && // ensure the VarDeclaration is not in an internal FB
			!LibraryElementPackage.eINSTANCE.algorithm.isSuperTypeOf(clazz) && // reject Algorithm from library element
			!LibraryElementPackage.eINSTANCE.method.isSuperTypeOf(clazz) && // reject Method from library element
			!STAlgorithmPackage.eINSTANCE.STAlgorithm.isSuperTypeOf(clazz); // reject STAlgorithm
	}

	def protected STMethod getMethod(EObject context) {
		if(context instanceof STMethod) context else context?.eContainer?.method
	}
}
