/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.trollworks.toolkit.io;

import com.trollworks.toolkit.utility.Debug;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Provides standardized logging. */
public class Log {
	private static final String				SEPARATOR	= " | ";															//$NON-NLS-1$
	private static final SimpleDateFormat	FORMAT		= new SimpleDateFormat("yyyy.MM.dd" + SEPARATOR + "HH:mm:ss.SSS");	//$NON-NLS-1$ //$NON-NLS-2$
	private static PrintStream				OUT			= System.out;

	/**
	 * Implementors of this interface can be passed to the various logging methods to provide
	 * context.
	 */
	public interface Context {
		/** @return A human-readable logging context. */
		String getLogContext();
	}

	/**
	 * @param file The file to write the log data to. Pass in <code>null</code> to reset it to
	 *            {@link System#out}. Note that when {@link Debug#DEV_MODE} is <code>true</code>,
	 *            calling this method has no effect and logging is always performed to
	 *            {@link System#out}.
	 */
	public static final void setLogFile(File file) {
		if (!Debug.DEV_MODE) {
			if (file == null) {
				OUT = System.out;
			} else {
				try {
					OUT = new PrintStream(file);
				} catch (IOException exception) {
					error(exception);
				}
			}
		}
	}

	/**
	 * Logs an error.
	 *
	 * @param msg The message to log.
	 */
	public static final void error(String msg) {
		error(null, msg, null);
	}

	/**
	 * Logs an error.
	 *
	 * @param throwable The {@link Throwable} to log.
	 */
	public static final void error(Throwable throwable) {
		error(null, null, throwable);
	}

	/**
	 * Logs an error.
	 *
	 * @param context The {@link Context} this error occurred within.
	 * @param msg The message to log.
	 */
	public static final void error(Context context, String msg) {
		error(context, msg, null);
	}

	/**
	 * Logs an error.
	 *
	 * @param context The {@link Context} this error occurred within.
	 * @param throwable The {@link Throwable} to log.
	 */
	public static final void error(Context context, Throwable throwable) {
		error(context, null, throwable);
	}

	/**
	 * Logs an error.
	 *
	 * @param context The {@link Context} this error occurred within.
	 * @param msg The message to log.
	 * @param throwable The {@link Throwable} to log.
	 */
	public static final void error(Context context, String msg, Throwable throwable) {
		post('E', context, msg, throwable);
	}

	/**
	 * Logs a warning.
	 *
	 * @param msg The message to log.
	 */
	public static final void warn(String msg) {
		warn(null, msg, null);
	}

	/**
	 * Logs a warning.
	 *
	 * @param throwable The {@link Throwable} to log.
	 */
	public static final void warn(Throwable throwable) {
		warn(null, null, throwable);
	}

	/**
	 * Logs a warning.
	 *
	 * @param context The {@link Context} this warning occurred within.
	 * @param msg The message to log.
	 */
	public static final void warn(Context context, String msg) {
		warn(context, msg, null);
	}

	/**
	 * Logs a warning.
	 *
	 * @param context The {@link Context} this warning occurred within.
	 * @param throwable The {@link Throwable} to log.
	 */
	public static final void warn(Context context, Throwable throwable) {
		warn(context, null, throwable);
	}

	/**
	 * Logs a warning.
	 *
	 * @param context The {@link Context} this warning occurred within.
	 * @param msg The message to log.
	 * @param throwable The {@link Throwable} to log.
	 */
	public static final void warn(Context context, String msg, Throwable throwable) {
		post('W', context, msg, throwable);
	}

	/**
	 * Logs an informational message.
	 *
	 * @param msg The message to log.
	 */
	public static final void info(String msg) {
		info(null, msg, null);
	}

	/**
	 * Logs an informational message.
	 *
	 * @param throwable The {@link Throwable} to log.
	 */
	public static final void info(Throwable throwable) {
		info(null, null, throwable);
	}

	/**
	 * Logs an informational message.
	 *
	 * @param context The {@link Context} this info occurred within.
	 * @param msg The message to log.
	 */
	public static final void info(Context context, String msg) {
		info(context, msg, null);
	}

	/**
	 * Logs an informational message.
	 *
	 * @param context The {@link Context} this info occurred within.
	 * @param throwable The {@link Throwable} to log.
	 */
	public static final void info(Context context, Throwable throwable) {
		info(context, null, throwable);
	}

	/**
	 * Logs an informational message.
	 *
	 * @param context The {@link Context} this info occurred within.
	 * @param msg The message to log.
	 * @param throwable The {@link Throwable} to log.
	 */
	public static final void info(Context context, String msg, Throwable throwable) {
		post('I', context, msg, throwable);
	}

	private static final void post(char levelCode, Context context, String msg, Throwable throwable) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(levelCode);
		buffer.append(SEPARATOR);
		buffer.append(FORMAT.format(new Date()));
		buffer.append(SEPARATOR);
		if (context != null) {
			buffer.append(context.getLogContext());
			buffer.append(SEPARATOR);
		}
		if (msg != null) {
			buffer.append(msg);
		}
		if (throwable != null) {
			Debug.stackTrace(throwable, buffer);
		}
		OUT.println(buffer);
	}
}