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

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.TextDrawing;
import com.trollworks.toolkit.ui.scale.Scale;
import com.trollworks.toolkit.utility.text.Text;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

/** A simple label replacement that is scalable. */
public class Label extends JComponent implements PropertyChangeListener {
	/** The property key that is monitored for error messages. */
	public static final String	ERROR_MESSAGE_KEY		= "Error Message";		//$NON-NLS-1$
	private String				mText					= "";					//$NON-NLS-1$
	private int					mHorizontalAlignment	= SwingConstants.LEFT;
	private int					mVerticalAlignment		= SwingConstants.CENTER;
	private JComponent			mRefersTo;
	private Color				mColor					= Color.BLACK;
	private boolean				mUsePreferredSizeOnly;

	/** Create a new, empty label. */
	public Label() {
		setForeground(mColor);
	}

	/**
	 * Create a new label.
	 *
	 * @param usePreferredSizeOnly <code>true</code> if only the preferred size is to be reported
	 *            for min/max.
	 */
	public Label(boolean usePreferredSizeOnly) {
		this();
		mUsePreferredSizeOnly = usePreferredSizeOnly;
	}

	/**
	 * Create a new label.
	 *
	 * @param text The text to use.
	 */
	public Label(String text) {
		this();
		setText(text);
	}

	/**
	 * Create a new label.
	 *
	 * @param text The text to use.
	 * @param horizontalAlignment The horizontal alignment to use.
	 */
	public Label(String text, int horizontalAlignment) {
		this(text);
		setHorizontalAlignment(horizontalAlignment);
	}

	/**
	 * Create a new label.
	 *
	 * @param text The text to use.
	 * @param usePreferredSizeOnly <code>true</code> if only the preferred size is to be reported
	 *            for min/max.
	 */
	public Label(String text, boolean usePreferredSizeOnly) {
		this(text);
		mUsePreferredSizeOnly = usePreferredSizeOnly;
	}

	/**
	 * Create a new label.
	 *
	 * @param text The text to use.
	 * @param horizontalAlignment The horizontal alignment to use.
	 * @param usePreferredSizeOnly <code>true</code> if only the preferred size is to be reported
	 *            for min/max.
	 */
	public Label(String text, int horizontalAlignment, boolean usePreferredSizeOnly) {
		this(text, usePreferredSizeOnly);
		setHorizontalAlignment(horizontalAlignment);
	}

	/** @return The text. */
	public String getText() {
		return mText;
	}

	/** @param text The text to use. */
	public void setText(String text) {
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		if (!mText.equals(text)) {
			mText = text;
			invalidate();
		}
	}

	/** @return The horizontal alignment. */
	public int getHorizontalAlignment() {
		return mHorizontalAlignment;
	}

	/** @param alignment The horizontal alignment to use. */
	public void setHorizontalAlignment(int alignment) {
		if (mHorizontalAlignment != alignment) {
			mHorizontalAlignment = alignment;
			repaint();
		}
	}

	/** @return The vertical alignment. */
	public int getVerticalAlignment() {
		return mVerticalAlignment;
	}

	/** @param alignment The vertical alignment to use. */
	public void setVerticalAlignment(int alignment) {
		if (mVerticalAlignment != alignment) {
			mVerticalAlignment = alignment;
			repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(Scale.get(this).scale(getFont()));
		TextDrawing.draw(g, GraphicsUtilities.getLocalInsetBounds(this), mText, mHorizontalAlignment, mVerticalAlignment);
	}

	@Override
	public Dimension getMinimumSize() {
		if (mUsePreferredSizeOnly) {
			return getPreferredSize();
		}
		return super.getMinimumSize();
	}

	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		Insets insets = getInsets();
		Dimension size = TextDrawing.getPreferredSize(Scale.get(this).scale(getFont()), mText);
		size.width += insets.left + insets.right;
		size.height += insets.top + insets.bottom;
		return size;
	}

	@Override
	public Dimension getMaximumSize() {
		if (mUsePreferredSizeOnly) {
			return getPreferredSize();
		}
		return super.getMaximumSize();
	}

	/** @return The {@link JComponent} that is being paired with. */
	public JComponent getRefersTo() {
		return mRefersTo;
	}

	/** @param refersTo The {@link JComponent} to pair with. */
	public void setRefersTo(JComponent refersTo) {
		if (mRefersTo != refersTo) {
			if (mRefersTo != null) {
				mRefersTo.removePropertyChangeListener(TOOL_TIP_TEXT_KEY, this);
				mRefersTo.removePropertyChangeListener(ERROR_MESSAGE_KEY, this);
			}
			mRefersTo = refersTo;
			if (mRefersTo != null) {
				mRefersTo.addPropertyChangeListener(TOOL_TIP_TEXT_KEY, this);
				mRefersTo.addPropertyChangeListener(ERROR_MESSAGE_KEY, this);
			}
			adjustToLink();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		adjustToLink();
	}

	private void adjustToLink() {
		String tooltip;
		Color color;
		if (mRefersTo != null) {
			tooltip = (String) mRefersTo.getClientProperty(ERROR_MESSAGE_KEY);
			if (tooltip == null) {
				tooltip = mRefersTo.getToolTipText();
				color = mColor;
			} else {
				color = Color.RED;
			}
		} else {
			tooltip = null;
			color = mColor;
		}
		setToolTipText(Text.wrapPlainTextForToolTip(tooltip));
		super.setForeground(color);
	}

	@Override
	public void setForeground(Color color) {
		mColor = color;
		if (mRefersTo == null || mRefersTo.getClientProperty(ERROR_MESSAGE_KEY) == null) {
			super.setForeground(color);
		}
	}

	/**
	 * Sets/clears the error message that a {@link Label} will respond to.
	 *
	 * @param comp The {@link JComponent} to set the message on.
	 * @param msg The error message or <code>null</code>.
	 */
	public static void setErrorMessage(JComponent comp, String msg) {
		Object old = comp.getClientProperty(ERROR_MESSAGE_KEY);
		if (old == null ? msg != null : !old.equals(msg)) {
			comp.putClientProperty(ERROR_MESSAGE_KEY, msg);
		}
	}
}
