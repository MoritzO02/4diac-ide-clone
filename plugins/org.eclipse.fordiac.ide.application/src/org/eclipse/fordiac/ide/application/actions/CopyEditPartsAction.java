/*******************************************************************************
 * Copyright (c) 2008 - 2012, 2016, 2017 Profactor GmbH, TU Wien ACIN, fortiss GmbH
 * 				 2019 Johanes Kepler University Linz
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gerhard Ebenhofer, Alois Zoitl, Monika Wenger
 *     - initial API and implementation and/or initial documentation
 *   Alois Zoitl - Changed copy to a selection action and accomodated changes
 *                 needed for cut
 *******************************************************************************/
package org.eclipse.fordiac.ide.application.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.fordiac.ide.application.Messages;
import org.eclipse.fordiac.ide.application.actions.CopyPasteMessage.CopyStatus;
import org.eclipse.fordiac.ide.application.commands.ConnectionReference;
import org.eclipse.fordiac.ide.model.libraryElement.Connection;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * The Class CopyEditPartsAction.
 */
public class CopyEditPartsAction extends SelectionAction {

	/** The templates. */

	/**
	 * Instantiates a new copy edit parts action.
	 *
	 * @param editor the editor
	 */
	public CopyEditPartsAction(final IEditorPart editor) {
		super(editor);
		setId(ActionFactory.COPY.getId());
		setText(Messages.CopyEditPartsAction_Text);
		final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
	}

	@Override
	protected boolean calculateEnabled() {
		for (final Object obj : getSelectedObjects()) {
			if (obj instanceof EditPart) {
				final Object model = ((EditPart) obj).getModel();
				if (model instanceof FBNetworkElement) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		final List<Object> templates = getSelectedTemplates();

		final CopyPasteMessage info = new CopyPasteMessage(CopyStatus.COPY, templates);
		Clipboard.getDefault().setContents(info);
	}

	protected List<Object> getSelectedTemplates() {
		final Set<Connection> connectionSet = new HashSet<>();
		final List<Object> templates = new ArrayList<>();
		for (final Object obj : getSelectedObjects()) {
			if (obj instanceof EditPart) {
				final Object model = ((EditPart) obj).getModel();
				if (model instanceof FBNetworkElement) {
					templates.add(model);
					templates.addAll(getAllFBNElementConnections((FBNetworkElement) model, connectionSet));
				}
			}
		}
		return templates;
	}

	private static Collection<ConnectionReference> getAllFBNElementConnections(final FBNetworkElement model,
			final Set<Connection> connectionSet) {
		final List<ConnectionReference> connections = new ArrayList<>();

		for (final IInterfaceElement elem : model.getInterface().getAllInterfaceElements()) {
			getConnectionList(elem).stream().filter(conn -> !connectionSet.contains(conn)).forEach(conn -> {
				connectionSet.add(conn);
				connections.add(new ConnectionReference(conn));
			});
		}
		return connections;
	}

	private static EList<Connection> getConnectionList(final IInterfaceElement elem) {
		return elem.isIsInput() ? elem.getInputConnections() : elem.getOutputConnections();
	}

}
