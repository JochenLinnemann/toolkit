/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.widget.tree;

import com.trollworks.toolkit.ui.image.StdImage;

public interface IconAccessor {
	/**
	 * @param row The {@link TreeRow} to operate on.
	 * @return The {@link StdImage} for the field, or <code>null</code>.
	 */
	StdImage getIcon(TreeRow row);
}
