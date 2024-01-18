/*******************************************************************************
 * Copyright (c) 2019 Johannes Kepler University Linz
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alois Zoitl - initial API and implementation and/or initial documentation
 *   Fabio Gandolfi - refactored this class to be the base class of the followConnection handlers
 *******************************************************************************/
package org.eclipse.fordiac.ide.application.handlers;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.fordiac.ide.application.Messages;
import org.eclipse.fordiac.ide.application.editparts.TargetInterfaceElementEditPart;
import org.eclipse.fordiac.ide.gef.editparts.InterfaceEditPart;
import org.eclipse.fordiac.ide.model.libraryElement.CFBInstance;
import org.eclipse.fordiac.ide.model.libraryElement.Connection;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.SubApp;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.fordiac.ide.model.ui.editors.HandlerHelper;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class FollowConnectionHandler extends AbstractHandler {

	private static class OppositeSelectionDialog extends PopupDialog {

		private final List<IInterfaceElement> opposites;
		private final GraphicalViewer viewer;

		public OppositeSelectionDialog(final Shell parent, final List<IInterfaceElement> opposites,
				final GraphicalViewer viewer) {
			super(parent, INFOPOPUPRESIZE_SHELLSTYLE, true, false, false, false, false,
					Messages.FBPaletteViewer_SelectConnectionEnd, null);
			this.opposites = opposites;
			this.viewer = viewer;
		}

		@Override
		protected void adjustBounds() {
			super.adjustBounds();
			final Point pt = getShell().getDisplay().getCursorLocation();
			final Rectangle rect = getShell().getBounds();
			rect.x = pt.x;
			rect.y = pt.y;
			rect.width += 6;
			rect.height += 6;
			getShell().setBounds(rect);
		}

		@Override
		protected Control createTitleMenuArea(final Composite parent) {

			final Composite titleAreaComposite = (Composite) super.createTitleMenuArea(parent);

			final GridData gdLabel = new GridData(GridData.FILL);
			gdLabel.horizontalIndent = 5;
			titleAreaComposite.setLayoutData(gdLabel);
			return titleAreaComposite;
		}

		@Override
		protected Control createDialogArea(final Composite parent) {

			final Composite dialogArea = (Composite) super.createDialogArea(parent);
			final ListViewer listViewer = new ListViewer(dialogArea, SWT.SIMPLE);
			listViewer.setContentProvider(new ArrayContentProvider());
			listViewer.setLabelProvider(new LabelProvider() {

				@Override
				public String getText(final Object element) {
					if (element instanceof final IInterfaceElement iElem) {
						String retVal = ""; //$NON-NLS-1$
						if (null != iElem.getFBNetworkElement()) {
							retVal = iElem.getFBNetworkElement().getName() + "."; //$NON-NLS-1$
						}
						return retVal + iElem.getName();
					}
					return super.getText(element);
				}
			});
			listViewer.setInput(opposites.toArray());

			listViewer.addSelectionChangedListener(
					event -> HandlerHelper.selectElement(event.getStructuredSelection().getFirstElement(), viewer));

			// on enter close the view
			listViewer.getControl().addKeyListener(new KeyListener() {

				@Override
				public void keyReleased(final KeyEvent e) {
					// we only want to close the window on enter presses
				}

				@Override
				public void keyPressed(final KeyEvent e) {
					if (e.character == SWT.CR) {
						dialogArea.getShell().close();
					}
				}
			});

			final GridData gd = new GridData(GridData.CENTER);
			gd.horizontalIndent = 3;
			gd.verticalIndent = 2;
			dialogArea.setLayoutData(gd);

			listViewer.setSelection(new StructuredSelection(listViewer.getElementAt(0)), true);
			return dialogArea;
		}

	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		return Status.CANCEL_STATUS;
	}

	protected static FBNetwork getFBNetwork(final IEditorPart editor) {
		final FBNetwork network = editor.getAdapter(FBNetwork.class);
		if (null == network) {
			// we have a viewer
			final FBNetworkElement element = editor.getAdapter(FBNetworkElement.class);
			if (element instanceof final SubApp subApp) {
				return subApp.getSubAppNetwork();
			}
			if (element instanceof final CFBInstance cfbInstance) {
				return cfbInstance.getCfbNetwork();
			}
		}
		return network;
	}

	@Override
	public void setEnabled(final Object evaluationContext) {
		final ISelection selection = (ISelection) HandlerUtil.getVariable(evaluationContext,
				ISources.ACTIVE_CURRENT_SELECTION_NAME);
		final IEditorPart editor = (IEditorPart) HandlerUtil.getVariable(evaluationContext,
				ISources.ACTIVE_EDITOR_NAME);

		if (selection != null && ((IStructuredSelection) selection).size() == 1) {
			setBaseEnabled(editor != null
					&& ((IStructuredSelection) selection).getFirstElement() instanceof InterfaceEditPart);
		} else {
			setBaseEnabled(false);
		}

	}

	protected List<IInterfaceElement> getConnectionOposites(final ISelection selection, final FBNetwork fbNetwork) {
		if (selection instanceof final IStructuredSelection structuredSelection && !selection.isEmpty()
				&& (structuredSelection.size() == 1)
				&& (structuredSelection.getFirstElement() instanceof final InterfaceEditPart iep)) {
			// only if only one element is selected
			if (useTargetPins(iep)) {
				return getTargetPins(iep);
			}
			final IInterfaceElement ie = iep.getModel();
			final EList<Connection> connList = getConnectionList(ie, fbNetwork);
			return connList.stream().map(con -> (con.getSource().equals(ie) ? con.getDestination() : con.getSource()))
					.toList();
		}
		return Collections.emptyList();
	}

	private boolean useTargetPins(final InterfaceEditPart iep) {
		return !iep.getChildren().isEmpty()
				&& ((iep.getModel().isIsInput() && isLeft()) || (!iep.getModel().isIsInput() && !isLeft()));
	}

	protected abstract boolean isLeft();

	private static List<IInterfaceElement> getTargetPins(final InterfaceEditPart iep) {
		return iep.getChildren().stream().filter(TargetInterfaceElementEditPart.class::isInstance)
				.map(ep -> (TargetInterfaceElementEditPart) ep).map(ep -> ep.getModel().getRefElement()).toList();
	}

	private static EList<Connection> getConnectionList(final IInterfaceElement ie, final FBNetwork fbNetwork) {
		if (isInsideSubappOrViewer(ie, fbNetwork) || isInsideTopType(ie)) {
			// we have a subapp/cfb interface element and we are inside of the subapp/cfb
			return ie.isIsInput() ? ie.getOutputConnections() : ie.getInputConnections();
		}
		return ie.isIsInput() ? ie.getInputConnections() : ie.getOutputConnections();
	}

	private static boolean isInsideTopType(final IInterfaceElement ie) {
		return ie.eContainer().eContainer() instanceof FBType;
	}

	protected static boolean isInsideSubappOrViewer(final IInterfaceElement ie, final FBNetwork fbNetwork) {
		final FBNetworkElement fbnElement = ie.getFBNetworkElement();
		return ((fbnElement instanceof SubApp) || (fbnElement instanceof CFBInstance))
				&& (!fbNetwork.equals(fbnElement.eContainer()));
	}

	protected static void showOppositeSelectionDialog(final List<IInterfaceElement> opposites,
			final ExecutionEvent event, final GraphicalViewer viewer) throws ExecutionException {

		final OppositeSelectionDialog dialog = new OppositeSelectionDialog(HandlerUtil.getActiveShellChecked(event),
				opposites, viewer);
		dialog.open();
	}

	protected IInterfaceElement getInternalOppositePin(final ISelection selection) {
		final InterfaceEditPart pin = (InterfaceEditPart) ((IStructuredSelection) selection).getFirstElement();

		if (hasOpposites(pin)) {
			if (pin.isEvent()) {
				return getInternalOppositeEventPin(pin);
			}
			if (pin.isVariable()) {
				if (((VarDeclaration) pin.getModel()).isInOutVar()) {
					return getInternalOppositeVarInOutPin(pin);
				}
				return getInternalOppositeVarPin(pin);
			}
			return getInternalOppositePlugOrSocketPin(pin);
		}
		return null;
	}

	protected abstract IInterfaceElement getInternalOppositeEventPin(final InterfaceEditPart pin);

	protected abstract IInterfaceElement getInternalOppositeVarPin(final InterfaceEditPart pin);

	protected abstract IInterfaceElement getInternalOppositeVarInOutPin(final InterfaceEditPart pin);

	protected abstract IInterfaceElement getInternalOppositePlugOrSocketPin(final InterfaceEditPart pin);

	@SuppressWarnings("static-method")
	protected boolean hasOpposites(final InterfaceEditPart pin) {
		return false;
	}

	protected static IInterfaceElement calcInternalOppositePin(final EList<? extends IInterfaceElement> source,
			final EList<? extends IInterfaceElement> destination, final InterfaceEditPart pin) {

		final int sourceIndex = source.stream().filter(IInterfaceElement::isVisible).toList().indexOf(pin.getModel());
		final var visibleDestinations = destination.stream().filter(IInterfaceElement::isVisible).toList();

		if (sourceIndex == -1) {
			return visibleDestinations.get(0);
		}

		if ((visibleDestinations.size() - 1) < sourceIndex) {
			return visibleDestinations.get(visibleDestinations.size() - 1);
		}
		return visibleDestinations.get(sourceIndex);
	}
}
