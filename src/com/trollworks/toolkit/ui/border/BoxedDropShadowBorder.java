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

package com.trollworks.toolkit.ui.border;

import com.trollworks.toolkit.ui.TextDrawing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.SwingConstants;
import javax.swing.border.Border;

/** A border consisting of a frame, optional drop shadow, and optional title. */
public class BoxedDropShadowBorder implements Border {
	private String	mTitle;
	private Font	mFont;
	private boolean	mDropShadow;

	/** Creates a new border with a drop shadow and without a title. */
	public BoxedDropShadowBorder() {
		mDropShadow = true;
	}

	/**
	 * Creates a new border without a title.
	 *
	 * @param hasDropShadow <code>true</code> if a drop shadow should be used.
	 */
	public BoxedDropShadowBorder(boolean hasDropShadow) {
		mDropShadow = hasDropShadow;
	}

	/**
	 * Creates a new border with a title and drop shadow.
	 *
	 * @param font The font to use.
	 * @param title The title to use.
	 */
	public BoxedDropShadowBorder(Font font, String title) {
		this(font, title, true);
	}

	/**
	 * Creates a new border with a title.
	 *
	 * @param font The font to use.
	 * @param title The title to use.
	 * @param hasDropShadow <code>true</code> if a drop shadow should be used.
	 */
	public BoxedDropShadowBorder(Font font, String title, boolean hasDropShadow) {
		super();
		mFont = font;
		mTitle = title;
		mDropShadow = hasDropShadow;
	}

	public boolean hasDropShadow() {
		return mDropShadow;
	}

	public void setHasDropShadow(boolean hasDropShadow) {
		mDropShadow = hasDropShadow;
	}

	/** @return The title. */
	public String getTitle() {
		return mTitle;
	}

	/** @param title The new title. */
	public void setTitle(String title) {
		mTitle = title;
	}

	@Override
	public Insets getBorderInsets(Component component) {
		int rightBottom = mDropShadow ? 3 : 1;
		Insets insets = new Insets(1, 1, rightBottom, rightBottom);
		if (mTitle != null) {
			insets.top += TextDrawing.getPreferredSize(mFont, mTitle).height;
		}
		return insets;
	}

	@Override
	public boolean isBorderOpaque() {
		return !mDropShadow;
	}

	@Override
	public void paintBorder(Component component, Graphics gc, int x, int y, int width, int height) {
		Color savedColor = gc.getColor();
		int rightBottomIndent = 1;
		if (mDropShadow) {
			gc.setColor(Color.lightGray);
			gc.drawLine(x + width - 2, y + 2, x + width - 2, y + height - 1);
			gc.drawLine(x + width - 1, y + 2, x + width - 1, y + height - 1);
			gc.drawLine(x + 2, y + height - 2, x + width - 1, y + height - 2);
			gc.drawLine(x + 2, y + height - 1, x + width - 1, y + height - 1);
			rightBottomIndent = 3;
		}
		gc.setColor(Color.black);
		gc.drawRect(x, y, width - rightBottomIndent, height - rightBottomIndent);
		if (mTitle != null) {
			Font savedFont = gc.getFont();
			gc.setFont(mFont);
			int th = TextDrawing.getPreferredSize(mFont, mTitle).height;
			Rectangle bounds = new Rectangle(x, y, width - rightBottomIndent, th + 2);
			gc.fillRect(x, y, width - rightBottomIndent, th + 1);
			gc.setColor(Color.white);
			TextDrawing.draw(gc, bounds, mTitle, SwingConstants.CENTER, SwingConstants.TOP);
			gc.setFont(savedFont);
		}
		gc.setColor(savedColor);
	}
}
