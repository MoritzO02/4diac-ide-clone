/*******************************************************************************
 * Copyright (c) 2008, 2009, 2011, 2015 Profactor GbmH, TU Wien,
 * 				 2018 Johannes Kepler University
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Gerhard Ebenhofer, Alois Zoitl
 *     - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.gef.policies;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.swt.graphics.Color;

/**
 * The Class HighlightEditPolicy.
 */
public class HighlightEditPolicy extends org.eclipse.gef.editpolicies.GraphicalEditPolicy {

	private Color revertColor;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#eraseTargetFeedback(org
	 * .eclipse.gef.Request)
	 */
	@Override
	public void eraseTargetFeedback(final Request request) {
		if (revertColor != null) {
			setContainerBackground(revertColor);
			revertColor = null;
		}
	}

	private Color getContainerBackground() {
		return getContainerFigure().getBackgroundColor();
	}

	private IFigure getContainerFigure() {
		return getHost().getFigure();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getTargetEditPart(org
	 * .eclipse.gef.Request)
	 */
	@Override
	public EditPart getTargetEditPart(final Request request) {
		return request.getType().equals(RequestConstants.REQ_SELECTION_HOVER) ? getHost() : null;
	}

	private void setContainerBackground(final Color c) {
		getContainerFigure().setBackgroundColor(c);
	}

	protected void showHighlight() {
		if (revertColor == null) {
			revertColor = getContainerBackground();
			setContainerBackground(ColorConstants.lightGray);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#showTargetFeedback(org
	 * .eclipse.gef.Request)
	 */
	@Override
	public void showTargetFeedback(final Request request) {
		showHighlight();
	}
}