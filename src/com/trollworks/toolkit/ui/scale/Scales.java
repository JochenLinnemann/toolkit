/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.scale;

/** Some standard scales. */
public enum Scales {
	ACTUAL_SIZE(1, "100%"), //$NON-NLS-1$
	HALF_AGAIN_SIZE(1.5, "150%"), //$NON-NLS-1$
	DOUBLE_SIZE(2, "200%"); //$NON-NLS-1$

	private Scale	mScale;
	private String	mTitle;

	private Scales(double scale, String title) {
		mScale = new Scale(scale);
		mTitle = title;
	}

	@Override
	public String toString() {
		return mTitle;
	}

	/** @return The scale. */
	public Scale getScale() {
		return mScale;
	}
}
