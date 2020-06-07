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

import org.eclipse.fordiac.ide.model.CoordinateConverter;
import org.eclipse.fordiac.ide.model.LibraryElementTags;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterFBType;
import org.eclipse.fordiac.ide.model.libraryElement.Connection;
import org.eclipse.fordiac.ide.model.libraryElement.FB;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.InterfaceList;
import org.eclipse.fordiac.ide.model.libraryElement.Resource;
import org.eclipse.fordiac.ide.model.libraryElement.ResourceType;
import org.eclipse.fordiac.ide.model.libraryElement.ResourceTypeFB;
import org.eclipse.fordiac.ide.model.libraryElement.SubApp;
import org.eclipse.fordiac.ide.model.libraryElement.SubAppType;

class FBNetworkExporter extends CommonElementExporter {

	FBNetworkExporter(CommonElementExporter parent) {
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
		for (FBNetworkElement fbnElement : network.getNetworkElements()) {
			String nodeName = getFBNElementNodeName(fbnElement);
			if (null != nodeName) {
				addStartElement(nodeName);
				addNameAttribute(fbnElement.getName());
				if (null != fbnElement.getType()) {
					addTypeAttribute(fbnElement.getType());
				}
				addCommentAttribute(fbnElement);
				addXYAttributes(fbnElement);

				if ((fbnElement instanceof SubApp) && (null == ((SubApp) fbnElement).getType())) {
					// we have an untyped subapp therefore add the subapp contents to it
					createUntypedSubAppcontents((SubApp) fbnElement);
				}

				addAttributes(fbnElement.getAttributes());
				addParamsConfig(fbnElement.getInterface().getInputVars());
				addEndElement();
			}
		}
	}

	private static String getFBNElementNodeName(FBNetworkElement fbnElement) {
		if (!(fbnElement.getType() instanceof AdapterFBType)) {
			if ((fbnElement instanceof FB) && !(fbnElement instanceof ResourceTypeFB)) {
				return LibraryElementTags.FB_ELEMENT;
			}
			if (fbnElement instanceof SubApp) {
				return LibraryElementTags.SUBAPP_ELEMENT;
			}
		}
		return null;
	}

	private void createUntypedSubAppcontents(SubApp element) throws XMLStreamException {
		new SubApplicationTypeExporter(this).addInterfaceList(element.getInterface());
		if (null != element.getSubAppNetwork()) {
			// if mapped the subapp may be empty
			new FBNetworkExporter(this).createFBNetworkElement(element.getSubAppNetwork());
		}
	}

	private void addConnections(final List<? extends Connection> connections, final String connectionElementName,
			FBNetwork fbNetwork) throws XMLStreamException {
		if (!connections.isEmpty()) {
			addStartElement(connectionElementName);
			for (Connection connection : connections) {
				addConnection(connection, fbNetwork);
			}
			addEndElement();
		}
	}

	private void addConnection(final Connection connection, FBNetwork fbNetwork) throws XMLStreamException {
		addEmptyStartElement(LibraryElementTags.CONNECTION_ELEMENT);
		if ((connection.getSource() != null) && (connection.getSource().eContainer() instanceof InterfaceList)) {
			getWriter().writeAttribute(LibraryElementTags.SOURCE_ATTRIBUTE,
					getConnectionEndpointIdentifier(connection.getSource(), fbNetwork));
		}

		if ((connection.getDestination() != null)
				&& (connection.getDestination().eContainer() instanceof InterfaceList)) {
			getWriter().writeAttribute(LibraryElementTags.DESTINATION_ATTRIBUTE,
					getConnectionEndpointIdentifier(connection.getDestination(), fbNetwork));
		}
		addCommentAttribute(connection);
		addConnectionCoordinates(connection);
	}

	private static String getConnectionEndpointIdentifier(IInterfaceElement interfaceElement, FBNetwork fbNetwork) {
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
		if (0 != connection.getDx1()) {
			// only export connection routing information if not a straight line
			getWriter().writeAttribute(LibraryElementTags.DX1_ATTRIBUTE,
					CoordinateConverter.INSTANCE.convertTo1499XML(connection.getDx1()));
			if (0 != connection.getDx2()) {
				// only export the second two if a five segment connection
				getWriter().writeAttribute(LibraryElementTags.DX2_ATTRIBUTE,
						CoordinateConverter.INSTANCE.convertTo1499XML(connection.getDx2()));
				getWriter().writeAttribute(LibraryElementTags.DY_ATTRIBUTE,
						CoordinateConverter.INSTANCE.convertTo1499XML(connection.getDy()));
			}
		}
	}

}
