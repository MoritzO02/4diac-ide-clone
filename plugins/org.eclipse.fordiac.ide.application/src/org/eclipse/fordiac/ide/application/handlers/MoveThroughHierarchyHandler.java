/*******************************************************************************
 * Copyright (c) 2024 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Sebastian Hollersbacher - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.application.handlers;

import static org.eclipse.fordiac.ide.model.ui.editors.HandlerHelper.getCommandStack;
import static org.eclipse.fordiac.ide.model.ui.editors.HandlerHelper.getFBNetwork;
import static org.eclipse.fordiac.ide.model.ui.editors.HandlerHelper.openEditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fordiac.ide.application.Messages;
import org.eclipse.fordiac.ide.application.commands.MoveElementsFromSubAppCommand;
import org.eclipse.fordiac.ide.model.libraryElement.Application;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.INamedElement;
import org.eclipse.fordiac.ide.model.libraryElement.SubApp;
import org.eclipse.fordiac.ide.model.libraryElement.TypedSubApp;
import org.eclipse.fordiac.ide.model.libraryElement.UntypedSubApp;
import org.eclipse.fordiac.ide.model.ui.editors.HandlerHelper;
import org.eclipse.fordiac.ide.ui.imageprovider.FordiacImage;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class MoveThroughHierarchyHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IEditorPart editor = HandlerUtil.getActiveEditor(event);

		if (null != editor) {
			final ISelection selection = HandlerUtil.getCurrentSelection(event);
			final List<FBNetworkElement> fbelements = HandlerHelper.getSelectedFBNElements(selection);

			if (!fbelements.isEmpty()) {

				final Object result = openDialog(fbelements.get(0));

				FBNetwork destinationNetwork = null;
				if (result instanceof final TreeNode node) {
					if (node.getValue() instanceof final SubApp subapp) {
						destinationNetwork = subapp.getSubAppNetwork();
					} else if (node.getValue() instanceof final Application app) {
						destinationNetwork = app.getFBNetwork();
					}
				}

				if (destinationNetwork != null) {
					final Point destination = getDestination(editor, fbelements, destinationNetwork);
					final MoveElementsFromSubAppCommand cmd = new MoveElementsFromSubAppCommand(fbelements, destination,
							destinationNetwork);
					getCommandStack(editor).execute(cmd);

					final GraphicalViewer viewer = HandlerHelper.openEditor(destinationNetwork.eContainer())
							.getAdapter(GraphicalViewer.class);
					HandlerHelper.selectElement(fbelements.get(0), viewer);
				}
			}
		}
		return Status.OK_STATUS;
	}

	private static Object openDialog(final FBNetworkElement fbElement) {
		final List<TreeNode> nodeList = new ArrayList<>();
		EObject container = fbElement.eContainer();
		while (container != null) {
			if (container instanceof Application || container instanceof SubApp) {
				nodeList.add(new TreeNode(container));
			}
			container = container.eContainer();
		}
		Collections.reverse(nodeList);
		final ElementTreeSelectionDialog dia = new ElementTreeSelectionDialog(Display.getCurrent().getActiveShell(),
				new TreeNodeLabelProvider(), new TreeNodeContentProvider());
		dia.setInput(nodeList.toArray(new TreeNode[0]));
		dia.setTitle(Messages.MoveElementDialogTitle);
		dia.open();
		return dia.getResult() != null ? dia.getResult()[0] : null;
	}

	@Override
	public void setEnabled(final Object evaluationContext) {
		final ISelection selection = (ISelection) HandlerUtil.getVariable(evaluationContext,
				ISources.ACTIVE_CURRENT_SELECTION_NAME);

		final List<FBNetworkElement> fbElements = HandlerHelper.getSelectedFBNElements(selection);
		if ((!fbElements.isEmpty()) && (fbElements.get(0).getFbNetwork().eContainer() instanceof SubApp)) {
			// we are inside of a subapp
			final FBNetwork parent = fbElements.get(0).getFbNetwork();
			final boolean sameParentNetwork = fbElements.stream().allMatch(el -> parent.equals(el.getFbNetwork()));
			setBaseEnabled(sameParentNetwork);
			return;
		}

		setBaseEnabled(false);
	}

	private static GraphicalViewer getViewer(final FBNetwork subappNetwork, final IEditorPart parent) {
		if (!getFBNetwork(parent).equals(subappNetwork)) {
			// source subapp editor, subapp content is not open
			return HandlerHelper.getViewer(openEditor(subappNetwork.eContainer()));
		}
		return HandlerHelper.getViewer(parent);
	}

	private static Point getDestination(final IEditorPart editor, final List<FBNetworkElement> fbelements,
			final FBNetwork destinationNetwork) {
		final GraphicalViewer viewer = getViewer(destinationNetwork, editor);
		viewer.flush();

		EObject obj = fbelements.get(0).getFbNetwork().eContainer();
		while (obj.eContainer() != null && obj.eContainer() != destinationNetwork) {
			obj = obj.eContainer();
		}

		final GraphicalEditPart ep = (GraphicalEditPart) viewer.getEditPartRegistry().get(obj);

		final Rectangle bounds = ep.getFigure().getBounds().getCopy();
		final int destX = bounds.x;
		final int destY = bounds.y + bounds.height + 20;

		return new Point(destX, destY);
	}

	public static class TreeNodeLabelProvider extends LabelProvider implements IStyledLabelProvider {

		@Override
		public String getText(final Object element) {
			if (element instanceof final TreeNode treeNode && treeNode.getValue() instanceof final INamedElement e) {
				return e.getName();
			}
			return element.toString();
		}

		@Override
		public Image getImage(final Object element) {
			if (element instanceof final TreeNode node) {
				if (node.getValue() instanceof UntypedSubApp) {
					return FordiacImage.ICON_SUB_APP.getImage();
				}
				if (node.getValue() instanceof TypedSubApp) {
					return FordiacImage.ICON_SUB_APP_TYPE.getImage();
				}
				if (node.getValue() instanceof Application) {
					return FordiacImage.ICON_APPLICATION.getImage();
				}
			}
			return super.getImage(element);
		}

		@Override
		public StyledString getStyledText(final Object element) {
			return null;
		}
	}
}
