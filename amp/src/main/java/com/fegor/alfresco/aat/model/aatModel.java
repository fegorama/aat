package com.fegor.alfresco.aat.model;

import org.alfresco.service.namespace.QName;

public interface aatModel {

	/*
	 * Namespace model
	 */
	public static final String NAMESPACE_AAT_CONTENT_MODEL = "http://www.fegorsoft.com/model/aat/1.0";

	/*
	 * Aspects
	 */
	public static final QName ASPECT_TRANSCRIBER = QName.createQName(
			NAMESPACE_AAT_CONTENT_MODEL, "Transcriber");

	/*
	 * Properties
	 */
	public static final QName PROP_HYPHOTESIS = QName.createQName(
			NAMESPACE_AAT_CONTENT_MODEL, "Hyphotesis");	
	public static final QName PROP_WORDS = QName.createQName(
			NAMESPACE_AAT_CONTENT_MODEL, "Words");
	public static final QName PROP_FRAMES = QName.createQName(
			NAMESPACE_AAT_CONTENT_MODEL, "Frames");	
}
