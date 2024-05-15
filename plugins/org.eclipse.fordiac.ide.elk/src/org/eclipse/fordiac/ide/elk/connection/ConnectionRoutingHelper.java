/*******************************************************************************
 * Copyright (c) 2022 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Daniel Lindhuber
 *     - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.elk.connection;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.elk.alg.libavoid.options.LibavoidMetaDataProvider;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.graph.ElkBendPoint;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkGraphFactory;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.fordiac.ide.application.editparts.AbstractFBNElementEditPart;
import org.eclipse.fordiac.ide.application.editparts.CommentEditPart;
import org.eclipse.fordiac.ide.application.editparts.ConnectionEditPart;
import org.eclipse.fordiac.ide.application.editparts.EditorWithInterfaceEditPart;
import org.eclipse.fordiac.ide.application.editparts.GroupEditPart;
import org.eclipse.fordiac.ide.application.editparts.SubAppForFBNetworkEditPart;
import org.eclipse.fordiac.ide.application.editparts.UnfoldedSubappContentEditPart;
import org.eclipse.fordiac.ide.application.editparts.UntypedSubAppInterfaceElementEditPart;
import org.eclipse.fordiac.ide.elk.FordiacLayoutData;
import org.eclipse.fordiac.ide.elk.helpers.FordiacGraphBuilder;
import org.eclipse.fordiac.ide.gef.editparts.AbstractPositionableElementEditPart;
import org.eclipse.fordiac.ide.gef.editparts.InterfaceEditPart;
import org.eclipse.fordiac.ide.gef.editparts.ValueEditPart;
import org.eclipse.fordiac.ide.model.libraryElement.SubApp;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;

public final class ConnectionRoutingHelper {

	protected static ElkGraphFactory factory = ElkGraphFactory.eINSTANCE;

	private static Set<GroupEditPart> groups = new HashSet<>();

	public static void buildGraph(final ConnectionLayoutMapping mapping) {
		groups.clear();
		if (mapping.getParentElement() instanceof EditorWithInterfaceEditPart) {
			addEditorPins(mapping);
		} else if (mapping.isExpandedSubapp()) {
			addSubAppPins(mapping);
		}

		for (final Object child : ((GraphicalEditPart) mapping.getParentElement()).getChildren()) {
			handleChild(mapping, child);
		}

		for (final GroupEditPart group : groups) {
			// add groups last to ensure non colliding ids with connections
			final ElkNode node = createBasicNode(mapping, group);
			node.setProperty(LibavoidMetaDataProvider.IS_CLUSTER, Boolean.TRUE);
		}

		processConnections(mapping);
		groups.clear();
	}

	private static void addSubAppPins(final ConnectionLayoutMapping mapping) {
		final SubAppForFBNetworkEditPart subapp = ((UnfoldedSubappContentEditPart) mapping.getParentElement())
				.getParent();
		final List<UntypedSubAppInterfaceElementEditPart> pins = subapp.getChildren().stream()
				.filter(UntypedSubAppInterfaceElementEditPart.class::isInstance)
				.map(UntypedSubAppInterfaceElementEditPart.class::cast).toList();

		for (final UntypedSubAppInterfaceElementEditPart pin : pins) {
			addPort(pin, mapping, true);
			saveConnections(mapping, pin);
		}
	}

	private static void addEditorPins(final ConnectionLayoutMapping mapping) {
		final EditorWithInterfaceEditPart editor = (EditorWithInterfaceEditPart) mapping.getParentElement();
		final List<InterfaceEditPart> pins = editor.getChildren().stream().filter(InterfaceEditPart.class::isInstance)
				.map(InterfaceEditPart.class::cast).toList();

		for (final InterfaceEditPart pin : pins) {
			addPort(pin, mapping, true);
			saveConnections(mapping, pin);
		}
	}

	private static void handleChild(final ConnectionLayoutMapping mapping, final Object child) {
		if (child instanceof final GroupEditPart group) {
			processGroup(mapping, group);
		}
		if (child instanceof final CommentEditPart comment) {
			processComment(mapping, comment);
		}
		if (child instanceof final AbstractFBNElementEditPart fbnEl) {
			processBlock(mapping, fbnEl);
		}
		if (child instanceof final ValueEditPart value) {
			processValue(mapping, value);
		}
	}

	private static void processGroup(final ConnectionLayoutMapping mapping, final GroupEditPart group) {
		groups.add(group);
		group.getContentEP().getChildren().forEach(child -> ConnectionRoutingHelper.handleChild(mapping, child));
	}

	private static void processComment(final ConnectionLayoutMapping mapping, final CommentEditPart comment) {
		final ElkNode node = factory.createElkNode();
		final ElkNode layoutGraph = mapping.getLayoutGraph();

		final Rectangle bounds = comment.getFigure().getBounds();
		// translate from absolute to relative
		node.setLocation(bounds.x - layoutGraph.getX(), bounds.y - layoutGraph.getY());
		node.setDimensions(bounds.preciseWidth(), bounds.preciseHeight());
		layoutGraph.getChildren().add(node);
	}

	private static void processBlock(final ConnectionLayoutMapping mapping, final AbstractFBNElementEditPart block) {
		createBasicNode(mapping, block);

		if (isExpandedSubApp(block)) {
			final SubAppForFBNetworkEditPart subapp = (SubAppForFBNetworkEditPart) block;
			final UnfoldedSubappContentEditPart content = (UnfoldedSubappContentEditPart) subapp.getContentEP();
			mapping.getExpandedSubapps().add(content);
		}

		for (final Object child : block.getChildren()) {
			if (child instanceof final InterfaceEditPart ie) {
				addPort(ie, mapping, false);
				// target connections would be inside the subapp and do not need to be saved
				if (isExpandedSubAppInterface(ie) && !ie.isInput()) {
					continue;
				}
				saveConnections(mapping, ie);
			}
		}
	}

	private static void processValue(final ConnectionLayoutMapping mapping, final ValueEditPart value) {
		if (value.getModel().getParentIE().getInputConnections().isEmpty()) {
			// only add the value if the pin does not have connections
			final ElkNode node = factory.createElkNode();
			final ElkNode layoutGraph = mapping.getLayoutGraph();

			final Rectangle bounds = value.getFigure().getBounds();
			node.setLocation(bounds.x - layoutGraph.getX(), bounds.y - layoutGraph.getY()); // translate from absolute
																							// to relative
			node.setDimensions(bounds.preciseWidth(), bounds.preciseHeight());
			layoutGraph.getChildren().add(node);
		}
	}

	private static ElkNode createBasicNode(final ConnectionLayoutMapping mapping,
			final AbstractPositionableElementEditPart block) {
		final ElkNode node = factory.createElkNode();
		final ElkNode layoutGraph = mapping.getLayoutGraph();

		final Rectangle bounds = block.getFigure().getBounds();
		node.setLocation(bounds.x - layoutGraph.getX(), bounds.y - layoutGraph.getY()); // translate from absolute to
																						// relative
		node.setDimensions(bounds.preciseWidth(), bounds.preciseHeight());

		node.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);

		layoutGraph.getChildren().add(node);

		// save for connection processing (ports need to know their parent)
		mapping.getReverseMapping().put(block, node);

		return node;
	}

	private static boolean isExpandedSubAppInterface(final InterfaceEditPart ep) {
		return isExpandedSubApp(ep.getParent());
	}

	private static boolean isExpandedSubApp(final EditPart ep) {
		if (ep instanceof final SubAppForFBNetworkEditPart subAppEP) {
			final SubApp model = subAppEP.getModel();
			return model.isUnfolded();
		}
		return false;
	}

	private static void processConnections(final ConnectionLayoutMapping mapping) {
		for (final ConnectionEditPart conn : mapping.getConnections()) {
			final InterfaceEditPart source = (InterfaceEditPart) conn.getSource();
			final ElkPort sourcePort = getPort(source, mapping);
			Assert.isNotNull(sourcePort, MessageFormat.format("Source port for pin: {0} should not be null!", //$NON-NLS-1$
					source.getModel().getQualifiedName()));

			final InterfaceEditPart target = (InterfaceEditPart) conn.getTarget();
			final ElkPort destinationPort = getPort(target, mapping);
			Assert.isNotNull(destinationPort, MessageFormat.format("Destination port for pin: {0} should not be null!", //$NON-NLS-1$
					target.getModel().getQualifiedName()));

			final ElkEdge edge = factory.createElkEdge();
			mapping.getLayoutGraph().getContainedEdges().add(edge);
			edge.getSources().add(sourcePort);
			edge.getTargets().add(destinationPort);

			mapping.getGraphMap().put(edge, conn);
		}
	}

	private static ElkPort getPort(final InterfaceEditPart interfaceEditPart, final ConnectionLayoutMapping mapping) {
		return (ElkPort) mapping.getReverseMapping().get(interfaceEditPart);
	}

	private static void addPort(final InterfaceEditPart interfaceEditPart, final ConnectionLayoutMapping mapping,
			final boolean isGraphPin) {
		mapping.getReverseMapping().computeIfAbsent(interfaceEditPart,
				ie -> createPort(interfaceEditPart, mapping, isGraphPin));
	}

	private static ElkPort createPort(final InterfaceEditPart ie, final ConnectionLayoutMapping mapping,
			final boolean isGraphPin) {
		final EditPart parent = ie.getParent();
		final ElkNode parentNode = (ElkNode) mapping.getReverseMapping().get(parent);

		final ElkPort port = factory.createElkPort();
		mapping.getGraphMap().put(port, ie.getModel());

		final boolean isInput = ie.getModel().isIsInput(); // use the model to always get the right information
		final Rectangle ieBounds = ie.getFigure().getBounds();
		final ElkNode layoutGraph = mapping.getLayoutGraph();
		double y = ieBounds.getCenter().preciseY() - layoutGraph.getY();

		final ElkNode dummyNode = factory.createElkNode();
		dummyNode.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
		dummyNode.setDimensions(1, 1); // a min size is needed that the layout alg uses the node.
		dummyNode.getPorts().add(port);
		layoutGraph.getChildren().add(dummyNode);
		mapping.getPortParentMapping().put(port, dummyNode);

		if (isGraphPin) {
			// Dummy node is needed to get better layout results for expanded subapps.
			port.setProperty(CoreOptions.PORT_SIDE, isInput ? PortSide.EAST : PortSide.WEST);
			final double x = isInput ? 0 : layoutGraph.getWidth();
			dummyNode.setLocation(x, y);
		} else {
			// Dummy node ensures that connections are treated as hyperedges.
			port.setProperty(CoreOptions.PORT_SIDE, isInput ? PortSide.WEST : PortSide.EAST);
			final double x = isInput ? 0 : parentNode.getWidth();
			y -= parentNode.getY();
			dummyNode.setLocation(parentNode.getX() + x, parentNode.getY() + y);
		}

		return port;
	}

	private static void saveConnections(final ConnectionLayoutMapping mapping, final InterfaceEditPart ie) {
		if (mapping.getParentElement() instanceof final UnfoldedSubappContentEditPart unfSubappContentEP
				&& unfSubappContentEP.getParent() == ie.getParent() && ie.getModel().isIsInput()) {
			// we have an unfolded subapp input pin. for this we don't want the connection
			// targets as these are outside
			return;
		}
		ie.getTargetConnections().stream().filter(ConnectionEditPart.class::isInstance)
				.filter(con -> FordiacGraphBuilder.isVisible((ConnectionEditPart) con))
				.forEach(con -> mapping.getConnections().add((ConnectionEditPart) con));
	}

	public static FordiacLayoutData calculateConnections(final ConnectionLayoutMapping mapping) {
		mapping.getLayoutGraph().getContainedEdges().forEach(edge -> processConnection(mapping, edge));
		return mapping.getLayoutData();
	}

	private static void processConnection(final ConnectionLayoutMapping mapping, final ElkEdge edge) {

		if (edge.getSources().isEmpty() || edge.getTargets().isEmpty() || edge.getSections().isEmpty()) {
			// do not really know why this happens, these lists should normally never be
			// empty
			// probably has something to do with editor pins
			return;
		}

		final ConnectionEditPart connEp = (ConnectionEditPart) mapping.getGraphMap().get(edge);
		final ElkPort startPort = (ElkPort) edge.getSources().get(0);
		final ElkPort endPort = (ElkPort) edge.getTargets().get(0);
		final ElkEdgeSection elkEdgeSection = edge.getSections().get(0);
		final List<ElkBendPoint> bendPoints = elkEdgeSection.getBendPoints();

		mapping.getLayoutData().addConnectionPoints(connEp.getModel(),
				createPointList(mapping, startPort, endPort, bendPoints));
	}

	private static PointList createPointList(final ConnectionLayoutMapping mapping, final ElkPort startPort,
			final ElkPort endPort, final List<ElkBendPoint> bendPoints) {
		// needs to translate coordinates back from relative to absolute

		final PointList list = new PointList();

		final ElkNode startNode = (ElkNode) mapping.getPortParentMapping().get(startPort);
		final ElkNode layoutGraph = startNode.getParent();
		final int startX = (int) (startPort.getX() + startNode.getX() + layoutGraph.getX());
		final int startY = (int) (startPort.getY() + startNode.getY() + layoutGraph.getY());
		list.addPoint(startX, startY);

		for (final ElkBendPoint point : bendPoints) {
			list.addPoint((int) (point.getX() + layoutGraph.getX()), (int) (point.getY() + layoutGraph.getY()));
		}

		final ElkNode endNode = (ElkNode) mapping.getPortParentMapping().get(endPort);
		final int endX = (int) (endPort.getX() + endNode.getX() + layoutGraph.getX());
		final int endY = (int) (endPort.getY() + endNode.getY() + layoutGraph.getY());
		list.addPoint(endX, endY);

		return list;
	}

	private ConnectionRoutingHelper() {
		throw new UnsupportedOperationException("Utility Class should not be instantiated!"); //$NON-NLS-1$
	}
}
