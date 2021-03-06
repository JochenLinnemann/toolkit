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

package com.trollworks.toolkit.io.xml.helper;

import com.trollworks.toolkit.annotation.XmlDefault;
import com.trollworks.toolkit.io.xml.XmlGenerator;
import com.trollworks.toolkit.io.xml.XmlParserContext;

import java.lang.reflect.Field;

import javax.xml.stream.XMLStreamException;

public class XmlGenericHelper implements XmlObjectHelper {
	public static final XmlGenericHelper SINGLETON = new XmlGenericHelper();

	private XmlGenericHelper() {
	}

	@Override
	public boolean canHandleClass(Class<?> clazz) {
		return true;
	}

	@Override
	public void emitAsAttribute(XmlGenerator xml, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
		Object value = field.get(obj);
		if (value != null) {
			String stringValue = value.toString();
			XmlDefault def = field.getAnnotation(XmlDefault.class);
			if (def != null) {
				xml.addAttributeNot(name, stringValue, def.value());
			} else {
				xml.addAttribute(name, stringValue);
			}
		}
	}

	@Override
	public void loadAttributeValue(XmlParserContext context, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
		Object instance = null;
		XmlDefault def = field.getAnnotation(XmlDefault.class);
		String value = context.getParser().getAttribute(name, def != null ? def.value() : null);
		if (value != null && !value.isEmpty()) {
			Class<?> type = field.getType();
			try {
				instance = type.getConstructor(String.class, XmlParserContext.class).newInstance(value, context);
			} catch (NoSuchMethodException exception) {
				instance = type.getConstructor(String.class).newInstance(value);
			}
		}
		field.set(obj, instance);
	}

	@Override
	public void emitAsTag(XmlGenerator xml, String tag, Object obj) throws XMLStreamException {
		// Unused
	}
}
