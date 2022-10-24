/*******************************************************************************
 * Copyright (c) 2008, 2009, 2014, 2017 Profactor GmbH, fortiss GmbH
 * 				 2018 - 2020 Johannes Keppler University, Linz
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gerhard Ebenhofer, Alois Zoitl
 *       - initial API and implementation and/or initial documentation
 *   Alois Zoitl - Refactored class hierarchy of xml exporters
 *   			 - fixed coordinate system resolution conversion in in- and export
 *               - changed exporting the Saxx cursor api
 *******************************************************************************/
package org.eclipse.fordiac.ide.model.dataexport;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.fordiac.ide.model.CoordinateConverter;
import org.eclipse.fordiac.ide.model.LibraryElementTags;
import org.eclipse.fordiac.ide.model.datatype.helper.IecTypes;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterFBType;
import org.eclipse.fordiac.ide.model.libraryElement.Attribute;
import org.eclipse.fordiac.ide.model.libraryElement.Connection;
import org.eclipse.fordiac.ide.model.libraryElement.ConnectionRoutingData;
import org.eclipse.fordiac.ide.model.libraryElement.ErrorMarkerFBNElement;
import org.eclipse.fordiac.ide.model.libraryElement.FB;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.Group;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.InterfaceList;
import org.eclipse.fordiac.ide.model.libraryElement.Resource;
import org.eclipse.fordiac.ide.model.libraryElement.ResourceType;
import org.eclipse.fordiac.ide.model.libraryElement.ResourceTypeFB;
import org.eclipse.fordiac.ide.model.libraryElement.SubApp;
import org.eclipse.fordiac.ide.model.libraryElement.SubAppType;
import org.eclipse.fordiac.ide.model.typelibrary.impl.SubAppTypeEntryImpl;

class FBNetworkExporter extends CommonElementExporter {

	FBNetworkExporter(final CommonElementExporter parent) {
		super(parent);
	}

	void createFBNetworkElement(final FBNetwork fbNetwork) throws XMLStreamException {
		addStartElement(getMainElementName(fbNetwork));
		addFBNetworkElements(fbNetwork);
		addConnections(fbNetwork.getEventConnections(), LibraryElementTags.EVENT_CONNECTIONS_ELEMENT, fbNetwork);
		addConnections(fbNetwork.getDataConnections(), LibraryElementTags.DATA_CONNECTIONS_ELEMENT, fbNetwork);
		addConnections(fbNetwork.getAdapterConnections(), LibraryElementTags.ADAPTERCONNECTIONS_ELEMENT, fbNetwork);
		addEndElement();
	}

	private static String getMainElementName(final FBNetwork fbNetwork) {
		String elementName = LibraryElementTags.SUBAPPNETWORK_ELEMENT;
		if (((fbNetwork.eContainer() instanceof FBType) && !(fbNetwork.eContainer() instanceof SubAppType))
				|| (fbNetwork.eContainer() instanceof Resource) || (fbNetwork.eContainer() instanceof ResourceType)) {
			elementName = LibraryElementTags.FBNETWORK_ELEMENT;
		}
		return elementName;
	}

	private void addFBNetworkElements(final FBNetwork network) throws XMLStreamException {
		for (final FBNetworkElement fbnElement : network.getNetworkElements()) {
			if (!isExportableErrorMarker(fbnElement)) {
				continue;
			}
			final String nodeName = getFBNElementNodeName(fbnElement);
			if (nodeName != null) {
				addStartElement(nodeName);
				addFBNetworkElementAttributes(fbnElement);
				addFBNetworkElementChildren(fbnElement);
				addEndElement();
			}
		}
	}

	private void addFBNetworkElementAttributes(final FBNetworkElement fbnElement) throws XMLStreamException {
		addNameAttribute(fbnElement.getName());
		if (fbnElement.getType() != null) {
			addTypeAttribute(fbnElement.getType());
		}
		addCommentAttribute(fbnElement);
		addXYAttributes(fbnElement);
		if (fbnElement instanceof Group) {
			addGroupAttributes((Group) fbnElement);
		}
		// Saving only hidden pins
		addPinVisibilityAttribute(fbnElement);
	}

	private void addGroupAttributes(final Group group) throws XMLStreamException {
		getWriter().writeAttribute(LibraryElementTags.WIDTH_ATTRIBUTE,
				CoordinateConverter.INSTANCE.convertTo1499XML(group.getWidth()));
		getWriter().writeAttribute(LibraryElementTags.HEIGHT_ATTRIBUTE,
				CoordinateConverter.INSTANCE.convertTo1499XML(group.getHeight()));
	}

	private void addFBNetworkElementChildren(final FBNetworkElement fbnElement) throws XMLStreamException {
		if (isUntypedSubapp(fbnElement)) {
			// we have an untyped subapp therefore add the subapp contents to it
			createUntypedSubAppContents((SubApp) fbnElement);
		}

		addAttributes(fbnElement.getAttributes());
		if (!isUntypedSubapp(fbnElement)) {
			// for untyped subapp initial values are stored in the vardeclarations
			addParamsConfig(fbnElement.getInterface().getInputVars());
			addErrorMarkerParamsConfig(fbnElement.getInterface().getErrorMarker());
			addPinComments(fbnElement.getInterface().getAllInterfaceElements());
		}

		if (fbnElement instanceof SubApp) {
			addSubappHeightAndWidthAttributes((SubApp) fbnElement);
		}

		if (fbnElement.isInGroup()) {
			addGroupAttribute(fbnElement.getGroup());
		}
	}

	private void addSubappHeightAndWidthAttributes(final SubApp subApp) throws XMLStreamException {
		if (subApp.getWidth() != 0) {
			addAttributeElement(LibraryElementTags.WIDTH_ATTRIBUTE, IecTypes.ElementaryTypes.LREAL.getName(),
					CoordinateConverter.INSTANCE.convertTo1499XML(subApp.getWidth()), null);
		}
		if (subApp.getHeight() != 0) {
			addAttributeElement(LibraryElementTags.HEIGHT_ATTRIBUTE, IecTypes.ElementaryTypes.LREAL.getName(),
					CoordinateConverter.INSTANCE.convertTo1499XML(subApp.getHeight()), null);
		}
	}

	private static boolean isUntypedSubapp(final FBNetworkElement fbnElement) {
		return (fbnElement instanceof SubApp) && (!((SubApp) fbnElement).isTyped());
	}

	private static String getFBNElementNodeName(final FBNetworkElement fbnElement) {
		if (!(fbnElement.getType() instanceof AdapterFBType)) {
			if ((fbnElement instanceof FB) && !(fbnElement instanceof ResourceTypeFB))
			{
				return LibraryElementTags.FB_ELEMENT;
			}

			if(fbnElement instanceof ErrorMarkerFBNElement) {
				if (fbnElement.getTypeEntry() instanceof SubAppTypeEntryImpl) {
					return LibraryElementTags.SUBAPP_ELEMENT;
				}
				return LibraryElementTags.FB_ELEMENT;
			}

			if (fbnElement instanceof SubApp) {
				return LibraryElementTags.SUBAPP_ELEMENT;
			}

			if (fbnElement instanceof Group) {
				return LibraryElementTags.GROUP_ELEMENT;
			}
		}
		return null;
	}

	private void createUntypedSubAppContents(final SubApp element) throws XMLStreamException {
		new SubApplicationTypeExporter(this).addInterfaceList(element.getInterface());
		if (null != element.getSubAppNetwork()) {
			// if mapped the subapp may be empty
			new FBNetworkExporter(this).createFBNetworkElement(element.getSubAppNetwork());
		}
	}

	private void addConnections(final List<? extends Connection> connections, final String connectionElementName,
			final FBNetwork fbNetwork) throws XMLStreamException {
		if (!connections.isEmpty()) {
			addStartElement(connectionElementName);
			for (final Connection connection : connections) {
				addConnection(connection, fbNetwork);
			}
			addEndElement();
		}
	}

	private void addConnection(final Connection connection, final FBNetwork fbNetwork) throws XMLStreamException {

		final boolean hasAttributes = !connection.getAttributes().isEmpty();
		if (hasAttributes) {
			addStartElement(LibraryElementTags.CONNECTION_ELEMENT);
		} else {
			addEmptyStartElement(LibraryElementTags.CONNECTION_ELEMENT);
		}

		if (isExportableConnectionEndpoint(connection.getSource())) {
			getWriter().writeAttribute(LibraryElementTags.SOURCE_ATTRIBUTE,
					getConnectionEndpointIdentifier(connection.getSource(), fbNetwork));
		}

		if (isExportableConnectionEndpoint(connection.getDestination())) {
			getWriter().writeAttribute(LibraryElementTags.DESTINATION_ATTRIBUTE,
					getConnectionEndpointIdentifier(connection.getDestination(), fbNetwork));
		}
		addCommentAttribute(connection);
		addConnectionCoordinates(connection);

		if (hasAttributes) {
			addAttributes(connection.getAttributes());
			addEndElement();
		}

	}

	private static boolean isExportableConnectionEndpoint(final IInterfaceElement endPoint) {
		return (endPoint != null) && isExportableErrorMarker(endPoint.getFBNetworkElement())
				&& (endPoint.eContainer() instanceof InterfaceList);
	}

	public static boolean isExportableErrorMarker(final FBNetworkElement fbNetworkElement) {
		return !((fbNetworkElement instanceof ErrorMarkerFBNElement) && (fbNetworkElement.getTypeEntry() == null));
	}

	private static String getConnectionEndpointIdentifier(final IInterfaceElement interfaceElement, final FBNetwork fbNetwork) {
		String retVal = ""; //$NON-NLS-1$
		if ((null != interfaceElement.getFBNetworkElement())
				&& (interfaceElement.getFBNetworkElement().getFbNetwork() == fbNetwork)) {
			// this is here to detect that interface elements of subapps
			retVal = interfaceElement.getFBNetworkElement().getName() + "."; ////$NON-NLS-1$
		}

		retVal += interfaceElement.getName();
		return retVal;
	}

	private void addConnectionCoordinates(final Connection connection) throws XMLStreamException {
		final ConnectionRoutingData routingData = connection.getRoutingData();
		if (0 != routingData.getDx1()) {
			// only export connection routing information if not a straight line
			getWriter().writeAttribute(LibraryElementTags.DX1_ATTRIBUTE,
					CoordinateConverter.INSTANCE.convertTo1499XML(routingData.getDx1()));
			if (0 != routingData.getDx2()) {
				// only export the second two if a five segment connection
				getWriter().writeAttribute(LibraryElementTags.DX2_ATTRIBUTE,
						CoordinateConverter.INSTANCE.convertTo1499XML(routingData.getDx2()));
				getWriter().writeAttribute(LibraryElementTags.DY_ATTRIBUTE,
						CoordinateConverter.INSTANCE.convertTo1499XML(routingData.getDy()));
			}
		}
	}

	private void addGroupAttribute(final Group group) throws XMLStreamException {
		addAttributeElement(LibraryElementTags.GROUP_NAME, IecTypes.ElementaryTypes.STRING.getName(), group.getName(),
				null);
	}

	private void addPinComments(final EList<IInterfaceElement> allInterfaceElements) throws XMLStreamException {
		for (final IInterfaceElement ie : allInterfaceElements) {
			if (!ie.getComment().isBlank()) {
				addAttributeElement(LibraryElementTags.PIN_COMMENT, IecTypes.ElementaryTypes.STRING.getName(),
						ie.getName() + ":" + ie.getComment(), null); //$NON-NLS-1$
			}

		}
	}

	private void addPinVisibilityAttribute(final FBNetworkElement fbnElement) throws XMLStreamException {
		for (final IInterfaceElement ie : fbnElement.getInterface().getAllInterfaceElements()) {
			// If the pin is hidden, add the attribute to the .sys file
			if (!ie.isVisible()) {
				Attribute visibilityAttribute = ie.getAttribute(LibraryElementTags.ELEMENT_VISIBLE);
				if (visibilityAttribute != null) {
					addAttributeElement(visibilityAttribute.getName(), visibilityAttribute.getType().getName(),
							ie.getName() + ":" + visibilityAttribute.getValue(), //$NON-NLS-1$
							visibilityAttribute.getComment());
				}
			}
		}
	}

}
