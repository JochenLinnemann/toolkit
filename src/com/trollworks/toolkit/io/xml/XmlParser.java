/*
 * Copyright (c) 1998-2015 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.io.xml;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.annotation.XmlAttr;
import com.trollworks.toolkit.annotation.XmlDirectChild;
import com.trollworks.toolkit.annotation.XmlTag;
import com.trollworks.toolkit.utility.Introspection;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.text.Numbers;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/** Provides simple XML parsing. */
public class XmlParser implements AutoCloseable {
	@Localize("The tag '%s' is from an older version and cannot be loaded.")
	@Localize(locale = "ru", value = "Тег '%s' относится к более старой версии и не может быть загружен.")
	@Localize(locale = "de", value = "Das Tag '%s' ist von einer älteren Version und kann nicht geladen werden.")
	@Localize(locale = "es", value = "La etiqueta '%s' es de una versión anterior y no puede cargarse.")
	private static String		TOO_OLD;
	@Localize("The tag '%s' is from a newer version and cannot be loaded.")
	@Localize(locale = "ru", value = "Тег '%s' относится к более новой версии и не может быть загружен.")
	@Localize(locale = "de", value = "Das Tag '%s' ist von einer neueren Version und kann nicht geladen werden.")
	@Localize(locale = "es", value = "La etiqueta '%s' es de una versión demasiado nueva y no puede cargarse.")
	private static String		TOO_NEW;
	@Localize("Unable to create object for collection tag '%s'.")
	@Localize(locale = "ru", value = "Невозможно создать объект для получения тэга '%s'.")
	@Localize(locale = "de", value = "Kann Objekt für Sammlungs-Tag '%s' nicht erstellen.")
	@Localize(locale = "es", value = "Imposible crear el objeto para la colección de etiquetas '%s'.")
	private static String		UNABLE_TO_CREATE_OBJECT_FOR_COLLECTION;
	@Localize("Only one direct child is permitted.")
	private static String		ONLY_ONE_DIRECT_CHILD_PERMITTED;
	@Localize("The direct child must be a collection.")
	private static String		DIRECT_CHILD_MUST_BE_COLLECITON;

	static {
		Localization.initialize();
	}

	private static final String	SEPARATOR	= "\u0000";				//$NON-NLS-1$
	private XMLStreamReader		mReader;
	private int					mDepth;
	private String				mMarker;

	/**
	 * Creates a new {@link XmlParser}.
	 *
	 * @param stream The {@link InputStream} to read from.
	 */
	public XmlParser(InputStream stream) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		mReader = factory.createXMLStreamReader(stream);
	}

	/**
	 * Load the XML data into an object.
	 *
	 * @param obj The object to load the data into.
	 * @param context The {@link XmlParserContext} to use. Pass in <code>null</code> to have a new
	 *            one created.
	 */
	@SuppressWarnings("unchecked")
	public void loadTagIntoObject(Object obj, XmlParserContext context) throws XMLStreamException {
		try {
			if (context == null) {
				context = new XmlParserContext(this);
			}
			String marker = getMarker();
			Class<?> tagClass = obj.getClass();
			int version = getIntegerAttribute(XmlGenerator.ATTR_VERSION, 0);
			if (version > XmlGenerator.getVersionOfTag(tagClass)) {
				throw new XMLStreamException(String.format(TOO_NEW, getCurrentTag()), getLocation());
			}
			if (version < XmlGenerator.getMinimumLoadableVersionOfTag(tagClass)) {
				throw new XMLStreamException(String.format(TOO_OLD, getCurrentTag()), getLocation());
			}
			if (version != 0) {
				context.pushVersion(version);
			}
			Set<String> unmatchedAttributes = new HashSet<>();
			for (int i = getAttributeCount(); --i > 0;) {
				unmatchedAttributes.add(getAttributeName(i));
			}
			unmatchedAttributes.remove(XmlGenerator.ATTR_VERSION);
			for (Field field : Introspection.getFieldsWithAnnotation(tagClass, true, XmlAttr.class)) {
				Introspection.makeFieldAccessible(field);
				XmlAttr attr = field.getAnnotation(XmlAttr.class);
				String name = attr.value();
				unmatchedAttributes.remove(name);
				Class<?> type = field.getType();
				if (type == boolean.class) {
					field.setBoolean(obj, isAttributeSet(name, false));
				} else if (type == int.class) {
					field.setInt(obj, getIntegerAttribute(name, 0));
				} else if (type == long.class) {
					field.setLong(obj, getLongAttribute(name, 0));
				} else if (type == short.class) {
					field.setShort(obj, (short) getIntegerAttribute(name, 0));
				} else if (type == double.class) {
					field.setDouble(obj, getDoubleAttribute(name, 0.0));
				} else if (type == float.class) {
					field.setFloat(obj, (float) getDoubleAttribute(name, 0.0));
				} else if (type == char.class) {
					String charStr = getAttribute(name);
					field.setChar(obj, charStr == null || charStr.isEmpty() ? 0 : charStr.charAt(0));
				} else if (type == String.class) {
					field.set(obj, getAttribute(name, "")); //$NON-NLS-1$
				} else if (type == UUID.class) {
					field.set(obj, UUID.fromString(getAttribute(name, ""))); //$NON-NLS-1$
				} else {
					Constructor<?> constructor = type.getConstructor(String.class);
					field.set(obj, constructor.newInstance(getAttribute(name, ""))); //$NON-NLS-1$
				}
			}
			if (obj instanceof AttributesLoadedListener) {
				((AttributesLoadedListener) obj).xmlAttributesLoaded(context, unmatchedAttributes);
			}
			Map<String, Field> subTags = new HashMap<>();
			for (Field field : Introspection.getFieldsWithAnnotation(tagClass, true, XmlTag.class)) {
				subTags.put(field.getAnnotation(XmlTag.class).value(), field);
			}
			Field[] direct = Introspection.getFieldsWithAnnotation(tagClass, true, XmlDirectChild.class);
			if (direct.length > 1) {
				throw new XMLStreamException(ONLY_ONE_DIRECT_CHILD_PERMITTED);
			}
			if (direct.length == 1) {
				if (!Collection.class.isAssignableFrom(direct[0].getType())) {
					throw new XMLStreamException(DIRECT_CHILD_MUST_BE_COLLECITON);
				}
				Introspection.makeFieldAccessible(direct[0]);
			}
			String tag;
			while ((tag = nextTag(marker)) != null) {
				Field field = subTags.get(tag);
				if (field != null) {
					Introspection.makeFieldAccessible(field);
					Class<?> type = field.getType();
					if (String.class == type) {
						field.set(obj, getText());
					} else if (Collection.class.isAssignableFrom(type)) {
						loadCollection(obj, context, field);
					} else {
						Object fieldObj = null;
						if (obj instanceof ObjectCreator) {
							fieldObj = ((ObjectCreator) obj).createObjectForXmlTag(context, tag);
						}
						if (fieldObj == null) {
							fieldObj = type.newInstance();
						}
						loadTagIntoObject(fieldObj, context);
						field.set(obj, fieldObj);
					}
				} else if (direct.length == 1 && (!(obj instanceof DirectChildTagManager) || ((DirectChildTagManager) obj).isDirectChildTag(context, tag))) {
					Type genericType = direct[0].getGenericType();
					if (genericType instanceof ParameterizedType) {
						genericType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
					} else {
						genericType = null;
					}
					Object fieldObj = null;
					if (XmlGenerator.TAG_STRING.equals(tag)) {
						fieldObj = getText();
					} else {
						if (genericType != null) {
							Class<?> cls = Class.forName(genericType.getTypeName());
							fieldObj = cls.newInstance();
						} else {
							throw new XMLStreamException(String.format(UNABLE_TO_CREATE_OBJECT_FOR_COLLECTION, tag), getLocation());
						}
						loadTagIntoObject(fieldObj, context);
					}
					((Collection<Object>) ensureCollectionIsAllocated(obj, direct[0], null)).add(fieldObj);
				} else if (obj instanceof UnmatchedTagProcessor) {
					((UnmatchedTagProcessor) obj).processUnmatchedXmlTag(context, tag);
				} else {
					skip();
				}
			}
			if (obj instanceof TagLoadedListener) {
				((TagLoadedListener) obj).xmlLoaded(context);
			}
			if (version != 0) {
				context.popVersion();
			}
		} catch (XMLStreamException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new XMLStreamException(exception);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadCollection(Object obj, XmlParserContext context, Field field) throws XMLStreamException, IllegalArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException {
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType) {
			genericType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
		} else {
			genericType = null;
		}
		Object collection = null;
		String marker = getMarker();
		String tag;
		while ((tag = nextTag(marker)) != null) {
			Object fieldObj = null;
			if (obj instanceof ObjectCreator) {
				fieldObj = ((ObjectCreator) obj).createObjectForXmlTag(context, tag);
			}
			if (fieldObj == null) {
				if (XmlGenerator.TAG_STRING.equals(tag)) {
					fieldObj = getText();
				} else if (genericType != null) {
					Class<?> cls = Class.forName(genericType.getTypeName());
					fieldObj = cls.newInstance();
				} else {
					throw new XMLStreamException(String.format(UNABLE_TO_CREATE_OBJECT_FOR_COLLECTION, tag), getLocation());
				}
			}
			if (!(fieldObj instanceof String)) {
				loadTagIntoObject(fieldObj, context);
			}
			if (collection == null) {
				collection = ensureCollectionIsAllocated(obj, field, null);
			}
			if (collection instanceof Collection) {
				((Collection<Object>) collection).add(fieldObj);
			}
		}
	}

	private static Object ensureCollectionIsAllocated(Object obj, Field field, Object collection) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		if (collection == null) {
			collection = field.get(obj);
			if (collection == null) {
				Class<?> type = field.getType();
				if (type == List.class) {
					collection = new ArrayList<>();
				} else if (type == Set.class) {
					collection = new HashSet<>();
				} else {
					collection = type.newInstance();
				}
				field.set(obj, collection);
			}
		}
		return collection;
	}

	/** @return The current line:column position. */
	public Location getLocation() {
		return mReader.getLocation();
	}

	/** @return A marker for determining if you've come to the end of a specific tag. */
	public String getMarker() {
		return mMarker;
	}

	/** @return The current tag's name, or <code>null</code>. */
	public String getCurrentTag() {
		return mReader.getLocalName();
	}

	/**
	 * Advances to the next position.
	 *
	 * @return The next tag's name, or <code>null</code>.
	 */
	public String nextTag() throws XMLStreamException {
		return nextTag(null);
	}

	/**
	 * Advances to the next position.
	 *
	 * @param marker If this is not <code>null</code>, when an end tag matches this marker return
	 *            <code>null</code>.
	 * @return The next tag's name, or <code>null</code>.
	 */
	public String nextTag(String marker) throws XMLStreamException {
		while (mReader.hasNext()) {
			switch (mReader.next()) {
				case XMLStreamConstants.START_ELEMENT:
					String name = mReader.getLocalName();
					mMarker = mDepth++ + SEPARATOR + name;
					return name;
				case XMLStreamConstants.END_ELEMENT:
					mMarker = --mDepth + SEPARATOR + mReader.getLocalName();
					if (marker != null && marker.equals(mMarker)) {
						return null;
					}
					break;
				case XMLStreamConstants.START_DOCUMENT:
					mMarker = null;
					if (marker != null) {
						return null;
					}
					break;
				case XMLStreamConstants.END_DOCUMENT:
					mMarker = null;
					return null;
				default:
					break;
			}
		}
		return null;
	}

	/** Skips the end of the current tag, bypassing its children. */
	public void skip() throws XMLStreamException {
		skip(getMarker());
	}

	/** @param marker Up to the end of the tag this marker came from will be skipped. */
	public void skip(String marker) throws XMLStreamException {
		while (nextTag(marker) != null) {
			// Intentionally empty
		}
	}

	/**
	 * @param name The name of the attribute to check.
	 * @return Whether the attribute is present.
	 */
	public boolean hasAttribute(String name) {
		return getAttribute(name) != null;
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @return The attribute value, or <code>null</code>.
	 */
	public String getAttribute(String name) {
		return mReader.getAttributeValue(null, name);
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @param def The default value to use if the attribute value isn't present.
	 * @return The value of the attribute.
	 */
	public String getAttribute(String name, String def) {
		String value = getAttribute(name);
		return value != null ? value : def;
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @return Whether or not the attribute is present and set to a 'true' value.
	 */
	public boolean isAttributeSet(String name) {
		return Numbers.extractBoolean(getAttribute(name));
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
	 * @return Whether or not the attribute is present and set to a 'true' value.
	 */
	public boolean isAttributeSet(String name, boolean def) {
		return Numbers.extractBoolean(getAttribute(name, Boolean.toString(def)));
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @return The value of the attribute.
	 */
	public int getIntegerAttribute(String name) {
		return Numbers.extractInteger(getAttribute(name), 0, false);
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
	 * @return The value of the attribute.
	 */
	public int getIntegerAttribute(String name, int def) {
		return Numbers.extractInteger(getAttribute(name), def, false);
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
	 * @param min The minimum value to return.
	 * @param max The maximum value to return.
	 * @return The value of the attribute.
	 */
	public int getIntegerAttribute(String name, int def, int min, int max) {
		return Numbers.extractInteger(getAttribute(name), def, min, max, false);
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @return The value of the attribute.
	 */
	public long getLongAttribute(String name) {
		return Numbers.extractLong(getAttribute(name), 0, false);
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
	 * @return The value of the attribute.
	 */
	public long getLongAttribute(String name, long def) {
		return Numbers.extractLong(getAttribute(name), def, false);
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
	 * @param min The minimum value to return.
	 * @param max The maximum value to return.
	 * @return The value of the attribute.
	 */
	public long getLongAttribute(String name, long def, long min, long max) {
		return Numbers.extractLong(getAttribute(name), def, min, max, false);
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @return The value of the attribute.
	 */
	public double getDoubleAttribute(String name) {
		return Numbers.extractDouble(getAttribute(name), 0, false);
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
	 * @return The value of the attribute.
	 */
	public double getDoubleAttribute(String name, double def) {
		return Numbers.extractDouble(getAttribute(name), def, false);
	}

	/**
	 * @param name The name of the attribute to retrieve.
	 * @param def The default value to use if the attribute value isn't present or cannot be
	 *            converted.
	 * @param min The minimum value to return.
	 * @param max The maximum value to return.
	 * @return The value of the attribute.
	 */
	public double getDoubleAttribute(String name, double def, double min, double max) {
		return Numbers.extractDouble(getAttribute(name), def, min, max, false);
	}

	/** @return The number of attributes. */
	public int getAttributeCount() {
		return mReader.getAttributeCount();
	}

	/**
	 * @param index The index of the attribute.
	 * @return The attribute value.
	 */
	public String getAttributeName(int index) {
		return mReader.getAttributeLocalName(index);
	}

	/**
	 * @param index The index of the attribute.
	 * @return The attribute value.
	 */
	public String getAttributeValue(int index) {
		return mReader.getAttributeValue(index);
	}

	/** @return The text of the current element. */
	public String getText() throws XMLStreamException {
		String text = mReader.getElementText();
		mMarker = --mDepth + SEPARATOR + mReader.getLocalName();
		return text;
	}

	/** Closes this {@link XmlParser}. No further reading can be attempted with it. */
	@Override
	public void close() throws XMLStreamException {
		if (mReader != null) {
			try {
				mReader.close();
			} finally {
				mReader = null;
			}
		}
	}

	/**
	 * Objects that are being loaded by the {@link XmlParser} that wish to be notified when their
	 * attributes have been loaded should implement this interface.
	 */
	public interface AttributesLoadedListener {
		/**
		 * Called after the XML tag attributes have been fully loaded into the object, just prior to
		 * loading any sub-tags that may be present.
		 *
		 * @param context The {@link XmlParserContext} for this object.
		 * @param unmatchedAttributes A {@link Set} of attribute names found in the XML that had no
		 *            matching {@link XmlAttr}-marked fields.
		 */
		void xmlAttributesLoaded(XmlParserContext context, Set<String> unmatchedAttributes) throws XMLStreamException;
	}

	/**
	 * Objects that are being loaded by the {@link XmlParser} that wish to be notified when they
	 * have been loaded should implement this interface.
	 */
	public interface TagLoadedListener {
		/**
		 * Called after the XML tag has been fully loaded into the object, just prior to the version
		 * being popped off the stack and control being returned to the caller.
		 *
		 * @param context The {@link XmlParserContext} for this object.
		 */
		void xmlLoaded(XmlParserContext context) throws XMLStreamException;
	}

	/**
	 * Objects that are being loaded by the {@link XmlParser} that wish to control the object
	 * creation process for their fields should implement this interface.
	 */
	public interface ObjectCreator {
		/**
		 * Called to create an object for an XML tag.
		 *
		 * @param context The {@link XmlParserContext} for this object.
		 * @param tag The tag to return an object for.
		 * @return The newly created object, or <code>null</code> if a new instance of the field's
		 *         data type should be created (i.e. when there is no need to use a sub-class and
		 *         the default no-args constructor can be used).
		 */
		Object createObjectForXmlTag(XmlParserContext context, String tag) throws XMLStreamException;
	}

	/**
	 * Objects that are being loaded by the {@link XmlParser} that wish to control whether how tags
	 * are added to a direct child should implement this interface.
	 */
	public interface DirectChildTagManager {
		/**
		 * Called when a field has been marked with {@link XmlDirectChild} and no {@link XmlTag}
		 * -marked fields match the specified tag.
		 *
		 * @param context The {@link XmlParserContext} for this object.
		 * @param tag The tag name that will be processed.
		 * @return <code>true</code> if the tag should be treated as matching the
		 *         {@link XmlDirectChild} or <code>false</code> if a call to
		 *         {@link UnmatchedTagProcessor#processUnmatchedXmlTag(XmlParserContext, String)}
		 *         should be made instead.
		 */
		boolean isDirectChildTag(XmlParserContext context, String tag) throws XMLStreamException;
	}

	/**
	 * Objects that are being loaded by the {@link XmlParser} that wish to control how unmatched
	 * tags are handled should implement this interface.
	 */
	public interface UnmatchedTagProcessor {
		/**
		 * Called to process an XML sub-tag that had no matching {@link XmlTag}-marked fields. Upon
		 * return from this method, the {@link XmlParser} should have been advanced past the current
		 * tag's contents, either by calling {@link XmlParser#skip()} or appropriate parsing of
		 * sub-tags.
		 *
		 * @param context The {@link XmlParserContext} for this object.
		 * @param tag The tag name that will be processed.
		 */
		void processUnmatchedXmlTag(XmlParserContext context, String tag) throws XMLStreamException;
	}
}
