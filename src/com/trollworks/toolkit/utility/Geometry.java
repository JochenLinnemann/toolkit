/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.utility;

import java.awt.Point;
import java.awt.Rectangle;

/** Provides geometry-related utilities. */
public class Geometry {
	/**
	 * @param pt The {@link Point} to generate a string for.
	 * @return The string form.
	 */
	@SuppressWarnings("nls")
	public static final String toString(Point pt) {
		return pt.x + "," + pt.y;
	}

	/**
	 * @param encoded A string previously generated by {@link #toString(Point)}.
	 * @return A {@link Point} with the encoded string's contents.
	 */
	@SuppressWarnings("nls")
	public static final Point toPoint(String encoded) throws NumberFormatException {
		if (encoded == null) {
			throw new NumberFormatException("Not a point");
		}
		String[] parts = encoded.split(",", 2);
		if (parts.length != 2) {
			throw new NumberFormatException("Not a point");
		}
		return new Point(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
	}

	/**
	 * @param rect The {@link Rectangle} to generate a string for.
	 * @return The string form.
	 */
	@SuppressWarnings("nls")
	public static final String toString(Rectangle rect) {
		if (rect.width != 1 || rect.height != 1) {
			return rect.x + "," + rect.y + "," + rect.width + "," + rect.height;
		}
		return rect.x + "," + rect.y;
	}

	/**
	 * @param encoded A string previously generated by {@link #toString(Rectangle)}.
	 * @return A {@link Rectangle} with the encoded string's contents.
	 */
	@SuppressWarnings("nls")
	public static final Rectangle toRectangle(String encoded) throws NumberFormatException {
		if (encoded == null) {
			throw new NumberFormatException("Not a rectangle");
		}
		String[] parts = encoded.split(",", 4);
		if (parts.length != 2 && parts.length != 4) {
			throw new NumberFormatException("Not a rectangle");
		}
		return new Rectangle(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 1, parts.length > 2 ? Integer.parseInt(parts[3].trim()) : 1);
	}
}