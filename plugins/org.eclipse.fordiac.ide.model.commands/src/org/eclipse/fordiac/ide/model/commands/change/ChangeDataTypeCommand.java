/*******************************************************************************
 * Copyright (c) 2008, 2022 Profactor GmbH, fortiss GmbH,
 *                          Johannes Kepler University Linz
 *               2023 Martin Erich Jobst
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alois Zoitl, Gerhard Ebenhofer
 *       - initial API and implementation and/or initial documentation
 *   Alois Zoitl - fixed issues in adapter update
 *               - moved adapter type handling to own adapter command
 *   Martin Jobst - add value validation
 *******************************************************************************/
package org.eclipse.fordiac.ide.model.commands.change;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fordiac.ide.model.commands.util.FordiacMarkerCommandHelper;
import org.eclipse.fordiac.ide.model.data.DataType;
import org.eclipse.fordiac.ide.model.errormarker.ErrorMarkerBuilder;
import org.eclipse.fordiac.ide.model.errormarker.FordiacMarkerHelper;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterDeclaration;
import org.eclipse.fordiac.ide.model.libraryElement.CompositeFBType;
import org.eclipse.fordiac.ide.model.libraryElement.ErrorMarkerDataType;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.InterfaceList;
import org.eclipse.fordiac.ide.model.libraryElement.LibraryElement;
import org.eclipse.fordiac.ide.model.libraryElement.SubApp;
import org.eclipse.fordiac.ide.model.libraryElement.SubAppType;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.fordiac.ide.model.typelibrary.TypeLibrary;
import org.eclipse.gef.commands.CompoundCommand;

public class ChangeDataTypeCommand extends AbstractChangeInterfaceElementCommand {
	private static final Pattern ARRAY_TYPE_DECLARATION_PATTERN = Pattern.compile("ARRAY\\s*\\[(.*)\\]\\s*OF\\s+(.+)"); //$NON-NLS-1$

	private final DataType dataType;
	private DataType oldDataType;
	private final CompoundCommand additionalCommands = new CompoundCommand();

	protected ChangeDataTypeCommand(final IInterfaceElement interfaceElement, final DataType dataType) {
		super(interfaceElement);
		this.dataType = dataType;
	}

	public static ChangeDataTypeCommand forTypeName(final IInterfaceElement interfaceElement, final String typeName) {
		final DataType dataType;
		if (interfaceElement instanceof AdapterDeclaration) {
			dataType = getTypeLibrary(interfaceElement).getAdapterTypeEntry(typeName).getType();
		} else {
			dataType = getTypeLibrary(interfaceElement).getDataTypeLibrary().getType(typeName);
		}
		return ChangeDataTypeCommand.forDataType(interfaceElement, dataType);
	}

	public static ChangeDataTypeCommand forDataType(final IInterfaceElement interfaceElement, final DataType dataType) {
		final ChangeDataTypeCommand result = new ChangeDataTypeCommand(interfaceElement, dataType);
		if (interfaceElement != null && interfaceElement.getFBNetworkElement() instanceof final SubApp subApp
				&& subApp.isMapped()) {
			result.getAdditionalCommands().add(new ChangeDataTypeCommand(
					subApp.getOpposite().getInterfaceElement(interfaceElement.getName()), dataType));
		}
		if (interfaceElement instanceof final AdapterDeclaration adapterDeclaration
				&& interfaceElement.eContainer() instanceof final InterfaceList interfaceList
				&& interfaceList.eContainer() instanceof final CompositeFBType compositeFBType
				&& !(compositeFBType instanceof SubAppType)) {
			result.getAdditionalCommands().add(new ChangeAdapterFBCommand(adapterDeclaration));
		}
		return result;
	}

	public static ChangeDataTypeCommand forTypeDeclaration(final IInterfaceElement interfaceElement,
			final String typeDeclaration) {
		if (interfaceElement instanceof final VarDeclaration varDeclaration) {
			final Matcher matcher = ARRAY_TYPE_DECLARATION_PATTERN.matcher(typeDeclaration.trim());
			final String arraySize;
			final String dataTypeName;
			if (matcher.matches()) {
				arraySize = matcher.group(1);
				dataTypeName = matcher.group(2);
			} else {
				arraySize = null;
				dataTypeName = typeDeclaration;
			}
			final ChangeDataTypeCommand result = ChangeDataTypeCommand.forTypeName(varDeclaration, dataTypeName);
			result.getAdditionalCommands().add(ChangeArraySizeCommand.forArraySize(varDeclaration, arraySize));
			return result;
		}
		return forTypeName(interfaceElement, typeDeclaration);
	}

	@Override
	protected void doExecute() {
		oldDataType = getInterfaceElement().getType();
		setNewType();
		if (oldDataType instanceof ErrorMarkerDataType) {
			getErrorMarkerUpdateCmds().add(FordiacMarkerCommandHelper
					.newDeleteMarkersCommand(FordiacMarkerHelper.findMarkers(getInterfaceElement())));
		}
		if (dataType instanceof final ErrorMarkerDataType errorMarkerDataType) {
			getErrorMarkerUpdateCmds().add(FordiacMarkerCommandHelper.newCreateMarkersCommand(ErrorMarkerBuilder
					.createErrorMarkerBuilder(errorMarkerDataType.getErrorMessage()).setTarget(getInterfaceElement())));
		}
		additionalCommands.execute();
	}

	@Override
	protected void doUndo() {
		additionalCommands.undo();
		getInterfaceElement().setType(oldDataType);
	}

	@Override
	protected void doRedo() {
		setNewType();
		additionalCommands.redo();
	}

	private void setNewType() {
		getInterfaceElement().setType(dataType);
	}

	public CompoundCommand getAdditionalCommands() {
		return additionalCommands;
	}

	protected static TypeLibrary getTypeLibrary(final IInterfaceElement interfaceElement) {
		if (EcoreUtil.getRootContainer(interfaceElement) instanceof final LibraryElement libraryElement) {
			return libraryElement.getTypeLibrary();
		}
		throw new IllegalArgumentException(
				"Could not determine type library for variable " + interfaceElement.getQualifiedName()); //$NON-NLS-1$
	}
}
