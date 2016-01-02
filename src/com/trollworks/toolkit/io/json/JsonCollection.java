/*
 * Copyright (c) 1998-2016 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.io.json;

/** Common base class for JSON collections. */
public abstract class JsonCollection {
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		appendTo(buffer);
		return buffer.toString();
	}

	/** @param out The {@link Appendable} to store a text representation into. */
	public abstract void appendTo(Appendable out);
}
