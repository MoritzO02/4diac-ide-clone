/*******************************************************************************
 * Copyright (c) 2012 - 2018 Profactor GmbH, AIT, fortiss GmbH
 * 							 Johannes Kepler University,
 *				 2021 Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gerhard Ebenhofer, Filip Andren, Alois Zoitl, Gerd Kainz
 *     - initial API and implementation and/or initial documentation
 *   Alois Zoitl - Harmonized deployment and monitoring
 *   Michael Oberlehner - added subapp monitoring
 *   Lukas Wais - clean up canBeMonitored
 *******************************************************************************/
package org.eclipse.fordiac.ide.monitoring;

import org.eclipse.fordiac.ide.deployment.monitoringbase.MonitoringBaseFactory;
import org.eclipse.fordiac.ide.deployment.monitoringbase.PortElement;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterDeclaration;
import org.eclipse.fordiac.ide.model.libraryElement.FB;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.Resource;
import org.eclipse.fordiac.ide.model.libraryElement.SubApp;
import org.eclipse.fordiac.ide.model.monitoring.MonitoringFactory;
import org.eclipse.fordiac.ide.model.monitoring.SubAppPortElement;
import org.eclipse.fordiac.ide.monitoring.model.SubAppPortHelper;
import org.eclipse.fordiac.ide.ui.errormessages.ErrorMessenger;

public final class MonitoringManagerUtils {

	private MonitoringManagerUtils() {
		throw new AssertionError(); // class should not be instantiated
	}

	public static boolean canBeMonitored(final IInterfaceElement ie, final boolean showError) {

		final FBNetworkElement fbNetworkElement = ie.getFBNetworkElement();

		if (fbNetworkElement instanceof SubApp) {
			final IInterfaceElement anchor = SubAppPortHelper.findAnchorInterfaceElement(ie);

			if (anchor == null) {
				if (showError) {
					ErrorMessenger.popUpErrorMessage(Messages.MonitoringManagerUtils_NoSubappAnchor);
				}
			} else {
				canBeMonitored(anchor, false);
			}
		}

		return fbNetworkElement != null && fbNetworkElement.getResource() != null;
	}

	public static boolean canBeMonitored(final FBNetworkElement obj) {
		// As a first solution try to find the first interface element and see if we
		// can monitor it. It is monitorable if it has a resource.
		final var ies = obj.getInterface().getAllInterfaceElements();
		return !ies.isEmpty() && canBeMonitored(ies.get(0), false);
	}

	public static PortElement createPortElement(final IInterfaceElement ie) {
		final FBNetworkElement obj = ie.getFBNetworkElement();

		if (obj instanceof FB || obj instanceof SubApp) {
			return createPortElement(obj, ie);
		}

		return null;

	}

	public static PortElement createPortElement(final FBNetworkElement fb, final IInterfaceElement ie) {
		PortElement p;
		if (ie instanceof AdapterDeclaration) {
			p = MonitoringFactory.eINSTANCE.createAdapterPortElement();
		} else if (fb instanceof SubApp) {
			p = createrSubAppPort(ie);

		} else {
			p = MonitoringBaseFactory.eINSTANCE.createPortElement();
		}

		final Resource res = fb.getResource();
		if (res == null) {
			return null;
		}

		p.setResource(res);

		if (fb instanceof FB || fb instanceof SubApp) {
			p.setFb(fb);
		}
		setupFBHierarchy(fb, p);
		p.setInterfaceElement(ie);
		return p;
	}

	public static PortElement createrSubAppPort(final IInterfaceElement ie) {
		final SubAppPortElement subAppPort = MonitoringFactory.eINSTANCE.createSubAppPortElement();
		final IInterfaceElement anchor = SubAppPortHelper.findAnchorInterfaceElement(ie);
		subAppPort.setAnchor(anchor);
		return subAppPort;
	}

	private static void setupFBHierarchy(final FBNetworkElement element, final PortElement p) {
		if (!element.isMapped() && element.getFbNetwork().eContainer() instanceof SubApp) {
			final SubApp subApp = (SubApp) element.getFbNetwork().eContainer();
			setupFBHierarchy(subApp, p);
			p.getHierarchy().add(subApp.getName());
		}
	}

}