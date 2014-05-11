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

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.collections.FilteredIterator;
import com.trollworks.toolkit.ui.image.IconSet;
import com.trollworks.toolkit.ui.image.ToolkitIcon;
import com.trollworks.toolkit.ui.layout.FlexRow;
import com.trollworks.toolkit.ui.menu.StdMenuBar;
import com.trollworks.toolkit.ui.menu.edit.Undoable;
import com.trollworks.toolkit.ui.menu.file.FileProxy;
import com.trollworks.toolkit.ui.menu.window.WindowMenu;
import com.trollworks.toolkit.ui.preferences.MenuKeyPreferences;
import com.trollworks.toolkit.ui.print.PageOrientation;
import com.trollworks.toolkit.ui.print.PrintManager;
import com.trollworks.toolkit.utility.PathUtils;
import com.trollworks.toolkit.utility.undo.StdUndoManager;
import com.trollworks.toolkit.utility.units.LengthUnits;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JToolBar;

/** Provides a base OS-level window. */
public class AppWindow extends BaseWindow implements Comparable<AppWindow>, Undoable {
	private static IconSet						DEFAULT_WINDOW_ICONSET	= null;
	private static final ArrayList<AppWindow>	WINDOW_LIST				= new ArrayList<>();
	private ToolkitIcon							mMenuIcon;
	private PrintManager						mPrintManager;
	private StdUndoManager						mUndoManager;
	private boolean								mIsPrinting;

	/** @return The top-most window. */
	public static AppWindow getTopWindow() {
		if (!WINDOW_LIST.isEmpty()) {
			return WINDOW_LIST.get(0);
		}
		return null;
	}

	/** Creates a new {@link AppWindow}. */
	public AppWindow() {
		this(null, null, null, false);
	}

	/**
	 * Creates a new {@link AppWindow}.
	 *
	 * @param title The window title. May be <code>null</code>.
	 */
	public AppWindow(String title) {
		this(title, null, null, false);
	}

	/**
	 * Creates a new {@link AppWindow}.
	 *
	 * @param title The window title. May be <code>null</code>.
	 * @param iconset The window {@link IconSet}.
	 */
	public AppWindow(String title, IconSet iconset) {
		this(title, iconset, null, false);
	}

	/**
	 * Creates a new {@link AppWindow}.
	 *
	 * @param title The title of the window.
	 * @param iconset The window {@link IconSet}.
	 * @param gc The graphics configuration to use.
	 * @param undecorated Whether to create an undecorated window, without menus.
	 */
	public AppWindow(String title, IconSet iconset, GraphicsConfiguration gc, boolean undecorated) {
		super(title, gc);
		if (undecorated) {
			setUndecorated(true);
		}
		if (!undecorated) {
			MenuKeyPreferences.loadFromPreferences();
			setJMenuBar(new StdMenuBar());
		}
		if (iconset == null) {
			iconset = DEFAULT_WINDOW_ICONSET;
		}
		if (iconset != null) {
			setIconImages(iconset.toList());
			setMenuIcon(iconset.getIcon(16));
		}
		mUndoManager = new StdUndoManager();
		enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
		WINDOW_LIST.add(this);
	}

	/** Call to create the toolbar for this window. */
	protected final void createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		FlexRow row = new FlexRow();
		row.setInsets(new Insets(2, 5, 2, 5));
		createToolBarContents(toolbar, row);
		row.apply(toolbar);
		add(toolbar, BorderLayout.NORTH);
	}

	/**
	 * Called to create the toolbar contents for this window.
	 *
	 * @param toolbar The {@link JToolBar} to add items to.
	 * @param row The {@link FlexRow} layout to add items to.
	 */
	protected void createToolBarContents(JToolBar toolbar, FlexRow row) {
		// Does nothing by default.
	}

	/** @return The default window icons. */
	public static IconSet getDefaultWindowIcons() {
		return DEFAULT_WINDOW_ICONSET;
	}

	/** @param iconset The new default window {@link IconSet}. */
	public static void setDefaultWindowIcons(IconSet iconset) {
		DEFAULT_WINDOW_ICONSET = iconset;
	}

	/** @return The menu icon representing this window. */
	public ToolkitIcon getMenuIcon() {
		return mMenuIcon;
	}

	/** @param icon The menu icon representing this window. */
	public void setMenuIcon(ToolkitIcon icon) {
		mMenuIcon = icon;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			WindowMenu.update();
		}
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		if (isVisible()) {
			WindowMenu.update();
		}
	}

	@Override
	public void toFront() {
		if (!isClosed()) {
			Component focus;

			if (getExtendedState() == ICONIFIED) {
				setExtendedState(NORMAL);
			}
			super.toFront();
			if (!isActive() || !isFocused()) {
				focus = getMostRecentFocusOwner();
				if (focus != null) {
					focus.requestFocus();
				} else {
					requestFocus();
				}
			}
		}
	}

	/** @return The {@link PrintManager} for this window. */
	public final PrintManager getPrintManager() {
		if (mPrintManager == null) {
			mPrintManager = createPageSettings();
		}
		return mPrintManager;
	}

	/** @param printManager The {@link PrintManager} to use. */
	public void setPrintManager(PrintManager printManager) {
		mPrintManager = printManager;
	}

	/** @return The default page settings for this window. May return <code>null</code>. */
	@SuppressWarnings("static-method")
	protected PrintManager createPageSettings() {
		try {
			return new PrintManager(PageOrientation.PORTRAIT, 0.5, LengthUnits.INCHES);
		} catch (Exception exception) {
			return null;
		}
	}

	/** Called after the page setup has changed. */
	public void adjustToPageSetupChanges() {
		// Does nothing by default.
	}

	/** @return <code>true</code> if the window is currently printing. */
	public boolean isPrinting() {
		return mIsPrinting;
	}

	/** @param printing <code>true</code> if the window is currently printing. */
	public void setPrinting(boolean printing) {
		mIsPrinting = printing;
	}

	/** @param event The {@link MouseWheelEvent}. */
	protected void processMouseWheelEventSuper(MouseWheelEvent event) {
		super.processMouseWheelEvent(event);
	}

	@Override
	public void dispose() {
		if (!isClosed()) {
			WINDOW_LIST.remove(this);
			if (WINDOW_LIST.isEmpty()) {
				for (PaletteWindow window : getWindows(PaletteWindow.class)) {
					window.setAppWindow(null);
				}
			}
		}
		super.dispose();
		WindowMenu.update();
	}

	/**
	 * @param windowClass The window class to return.
	 * @param <T> The window type.
	 * @return The current visible windows, in order from top to bottom.
	 */
	public static <T extends AppWindow> ArrayList<T> getActiveWindows(Class<T> windowClass) {
		ArrayList<T> list = new ArrayList<>();
		for (T window : new FilteredIterator<>(WINDOW_LIST, windowClass)) {
			if (window.isShowing()) {
				list.add(window);
			}
		}
		return list;
	}

	@Override
	public void windowGainedFocus(WindowEvent event) {
		if (event.getWindow() == this) {
			WINDOW_LIST.remove(this);
			WINDOW_LIST.add(0, this);
			for (PaletteWindow window : getWindows(PaletteWindow.class)) {
				window.setAppWindow(this);
			}
		}
		super.windowGainedFocus(event);
	}

	/** @return The window's {@link StdUndoManager}. */
	@Override
	public StdUndoManager getUndoManager() {
		return mUndoManager;
	}

	/**
	 * @param file The backing file to look for.
	 * @return The {@link AppWindow} associated with the specified backing file.
	 */
	public static AppWindow findWindow(File file) {
		String fullPath = PathUtils.getFullPath(file);
		for (AppWindow window : AppWindow.getAllWindows()) {
			if (window instanceof FileProxy) {
				File wFile = ((FileProxy) window).getBackingFile();
				if (wFile != null) {
					if (PathUtils.getFullPath(wFile).equals(fullPath)) {
						return window;
					}
				}
			}
		}
		return null;
	}

	/** @return A list of all {@link AppWindow}s created by this application. */
	public static ArrayList<AppWindow> getAllWindows() {
		return getWindows(AppWindow.class);
	}

	/** @return The title to be used for this window in the window menu. */
	protected String getTitleForWindowMenu() {
		return getTitle();
	}

	@Override
	public int compareTo(AppWindow other) {
		if (this != other) {
			String title = getTitleForWindowMenu();
			String otherTitle = other.getTitleForWindowMenu();
			if (title != null) {
				if (otherTitle == null) {
					return 1;
				}
				return title.compareTo(otherTitle);
			}
			if (otherTitle != null) {
				return -1;
			}
		}
		return 0;
	}
}
