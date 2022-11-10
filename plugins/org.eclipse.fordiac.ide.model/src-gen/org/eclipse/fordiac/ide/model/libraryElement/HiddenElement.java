/**
 * *******************************************************************************
 * Copyright (c) 2008 - 2018 Profactor GmbH, TU Wien ACIN, fortiss GmbH
 *               2022 Martin Erich Jobst
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *    Gerhard Ebenhofer, Alois Zoitl, Ingo Hegny, Monika Wenger, Martin Jobst
 *      - initial API and implementation and/or initial documentation
 * *******************************************************************************
 */
package org.eclipse.fordiac.ide.model.libraryElement;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Hidden Element</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see org.eclipse.fordiac.ide.model.libraryElement.LibraryElementPackage#getHiddenElement()
 * @model
 * @generated
 */
public interface HiddenElement extends ConfigurableObject {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model visibleDataType="org.eclipse.emf.ecore.xml.type.Boolean"
	 * @generated
	 */
	void setVisible(boolean visible);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation" dataType="org.eclipse.emf.ecore.xml.type.Boolean"
	 * @generated
	 */
	boolean isVisible();

} // HiddenElement