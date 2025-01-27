/*******************************************************************************
 * Copyright (c) 2020 Andrea Zoitl
 *               2021 Johannes Kepler University Linz
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrea Zoitl
 *      - externalized all translatable strings
 *    Melanie Winter - clean up
 *******************************************************************************/
package org.eclipse.fordiac.ide.fbtypeeditor.servicesequence;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("squid:S3008")  // tell sonar the java naming convention does not make sense for this class
public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.fordiac.ide.fbtypeeditor.servicesequence.messages"; //$NON-NLS-1$

	public static String CreateOutputPrimitiveCommand_NotAvailable;
	public static String InterfaceSelectorButton_Interface;
	public static String PrimitiveSection_CreateControls_PrimitiveSpecification;
	public static String PrimitiveSection_CreateEventSection_Event;
	public static String PrimitiveSection_CustomEvent;
	public static String PrimitiveSection_CreatePrimitiveSection_Interface;
	public static String PrimitiveSection_DataQualifyingToolTip;
	public static String ServiceInterfacePaletteFactory_DrawerName;
	public static String ServiceInterfacePaletteFactory_LeftInterface;
	public static String ServiceInterfacePaletteFactory_OutputPrimitive;
	public static String ServiceInterfacePaletteFactory_OutputPrimitive_Desc;
	public static String ServiceInterfacePaletteFactory_RightInterface;
	public static String ServiceInterfacePaletteFactory_ServiceTransaction;
	public static String ServiceInterfacePaletteFactory_ServiceTransaction_Desc;
	public static String ServiceSection_LeftInterface;
	public static String ServiceSection_Comment;
	public static String ServiceSection_Name;
	public static String ServiceSection_RightInterface;
	public static String ServiceSection_ServiceSequences;
	public static String ServiceSequenceEditor_Service;
	public static String ServiceSequenceSection_Index;
	public static String ServiceSequenceSection_InputPrimitive;
	public static String ServiceSequenceSection_OutputPrimitives;
	public static String ServiceSequenceSection_ServiceSequence;
	public static String ServiceSequenceSection_Transaction;
	public static String TransactionSection_CollectOutputPrimitiveGroupName_PrimitivesRightInterface;
	public static String TransactionSection_CollectOutputPrimitiveGroupName_AndLeftInterface;
	public static String TransactionSection_CreateTableLayout_Parameter;
	public static String TransactionSection_CreateTableLayout_Index;
	public static String TransactionSection_InputPrimitive;
	public static String TransactionSection_OutputPrimitives;
	public static String TransactionSection_Parameter;
	public static String RunServiceSequenceHandler_InconsistencyDetected;
	public static String RunServiceSequenceHandler_SequenceDoesNotMatchECC;
	public static String RunServiceSequenceHandler_SequenceMatchesECC;
	public static String RunServiceSequenceHandler_Success;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// empty private constructor
	}
}