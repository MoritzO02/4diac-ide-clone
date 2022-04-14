/*******************************************************************************
 * Copyright (c) 2015, 2020 fortiss GmbH, Primetals Technologies Austria GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alois Zoitl - initial API and implementation and/or initial documentation
 *               - copied and modified from systemmanagement.ui
 *******************************************************************************/
package org.eclipse.fordiac.ide.subapptypeeditor.providers;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.ViewerNotification;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.SubAppType;
import org.eclipse.fordiac.ide.model.libraryElement.provider.FBNetworkItemProvider;
import org.eclipse.fordiac.ide.model.libraryElement.provider.SubAppTypeItemProvider;
import org.eclipse.fordiac.ide.model.typelibrary.TypeEntry;

public class TypedSubAppItemProvider extends SubAppTypeItemProvider {

	protected FBNetworkItemProvider subAppNetworkItemProvider = null;

	public TypedSubAppItemProvider(final AdapterFactory adapterFactory) {
		super(adapterFactory);
		subAppNetworkItemProvider = new FBNetworkItemProvider(adapterFactory) {

			@Override
			public void fireNotifyChanged(final Notification notification) {
				final FBNetwork network = (FBNetwork) notification.getNotifier();
				final Notification wrappedNotification = ViewerNotification.wrapNotification(notification,
						network.eContainer());
				super.fireNotifyChanged(wrappedNotification);
			}

		};
	}

	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(final Object object) {
		final FBNetwork fbNetwork = getFBNetwork(object);
		return (null != fbNetwork) ? subAppNetworkItemProvider.getChildrenFeatures(fbNetwork) : Collections.emptyList();
	}

	@Override
	public Object getParent(final Object object) {
		final Object parent = super.getParent(object);
		return parent instanceof TypeEntry ? null : parent;
	}

	@Override
	public Collection<?> getChildren(final Object object) {
		final FBNetwork fbNetwork = getFBNetwork(object);
		return (null != fbNetwork) ? subAppNetworkItemProvider.getChildren(fbNetwork) : Collections.emptyList();
	}

	@Override
	public boolean hasChildren(final Object object) {
		final FBNetwork fbNetwork = getFBNetwork(object);
		return ((null != fbNetwork) && subAppNetworkItemProvider.hasChildren(fbNetwork));
	}

	private FBNetwork getFBNetwork(final Object object) {
		final FBNetwork subAppNetwork = ((SubAppType) object).getFBNetwork();
		if (null != subAppNetwork && !subAppNetwork.eAdapters().contains(subAppNetworkItemProvider)) {
			// register to the subappnetwork changes so that the viewer is updated
			subAppNetwork.eAdapters().add(subAppNetworkItemProvider);
		}
		return subAppNetwork;
	}
}
