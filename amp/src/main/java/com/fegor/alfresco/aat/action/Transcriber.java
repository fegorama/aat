/* 
 * aat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package com.fegor.alfresco.aat.action;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import com.fegor.alfresco.aat.model.aatModel;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;

/**
 * Transcriber Action
 * 
 * @author fegor
 * 
 */
public class Transcriber extends ActionExecuterAbstractBase {

	private final Logger logger = Logger.getLogger(Transcriber.class);

	/*
	 * Services
	 */
	private ContentService contentService;
	private NodeService nodeService;

	/*
	 * Params for transcriber
	 */
	private String store;
	private String acousticModelPath;
	private String dictionaryPath;
	private String languageModelPath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl
	 * (org.alfresco.service.cmr.action.Action,
	 * org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		if (actionedUponNodeRef != null) {
			try {
				this.transcriberFile(actionedUponNodeRef);
			} catch (ContentIOException e) {
				logger.error(this.getClass().getName()
						+ "Problem for content in Transcriber Action: " + e);
				e.printStackTrace();
			} catch (UnsupportedAudioFileException e) {
				logger.error(this.getClass().getName()
						+ "Unsupported Audio File in Transcriber Action: " + e);
				e.printStackTrace();
			} catch (IOException e) {
				logger.error(this.getClass().getName()
						+ "Problem for IO in Transcriber Action: " + e);
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#
	 * addParameterDefinitions(java.util.List)
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> arg0) {
	}

	/**
	 * Transcribe file for nodeRef
	 * 
	 * @param nodeRef
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 * @throws ContentIOException
	 */
	public void transcriberFile(NodeRef nodeRef) throws ContentIOException,
			UnsupportedAudioFileException, IOException {
		ContentReader contentReader = this.contentService.getReader(nodeRef,
				ContentModel.PROP_CONTENT);

		if (contentReader != null) {
			String contentUrl = contentReader.getContentUrl();
			String contentPath = contentUrl.replaceFirst("store:/", this.store);

			if (contentReader != null) {
				if (logger.isDebugEnabled()) {
					logger.debug(this.getClass().getName()
							+ ": Audio Transcriber for indexing: ");
					logger.debug(this.getClass().getName() + "      [NodeRef: "
							+ nodeRef.getId() + "]");
					logger.debug(this.getClass().getName() + "      [File: "
							+ contentPath + "]");
					logger.debug(this.getClass().getName() + "      [Type: "
							+ contentReader.getMimetype() + "]");
				}

				try {
					Configuration configuration = new Configuration();
					configuration.setAcousticModelPath(this.acousticModelPath);
					configuration.setDictionaryPath(this.dictionaryPath);
					configuration.setLanguageModelPath(this.languageModelPath);

					StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(
							configuration);

					recognizer.startRecognition(contentReader
							.getContentInputStream());

					ArrayList<String> arlHyphotesis = new ArrayList<String>();
					ArrayList<String> arlWords = new ArrayList<String>();
					ArrayList<Integer> arlFrames = new ArrayList<Integer>();
					String hyphotesis = null;
					SpeechResult result;

					while ((result = recognizer.getResult()) != null) {
						hyphotesis = result.getHypothesis();
						System.out.format("Hypothesis: %s\n", hyphotesis);
						arlHyphotesis.add(hyphotesis);

						System.out
								.println("List of recognized words and their times:");
						for (WordResult r : result.getWords()) {
							arlWords.add(r.toString());
							System.out.println(r);
						}

						System.out.println("Best 3 hypothesis:");
						for (String s : result.getNbest(3)) {
							System.out.println(s);
						}
						System.out.println("Lattice contains "
								+ result.getLattice().getNodes().size()
								+ " nodes");
					}

					recognizer.stopRecognition();

					// If arlWords not empty, add Aspect for tokens/words
					// persistence and indexing
					if (!arlWords.isEmpty()) {
						this.addAspect(nodeRef, arlHyphotesis, arlWords,
								arlFrames);
					}
				} catch (IOException e) {
					logger.error(this.getClass().getName()
							+ "Problem when loading Transcriber: " + e);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Add Aspect Transcriber is not assigned
	 * 
	 * @param nodeRef
	 */
	private void addAspect(NodeRef nodeRef, ArrayList<String> arlHyphotesis,
			ArrayList<String> arlWords, ArrayList<Integer> arlFrames) {
		HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>(
				1, 1.0f);
		properties.put(aatModel.PROP_HYPHOTESIS, arlHyphotesis);
		properties.put(aatModel.PROP_WORDS, arlWords);
		properties.put(aatModel.PROP_FRAMES, arlFrames);
		if (!nodeService.hasAspect(nodeRef, aatModel.ASPECT_TRANSCRIBER)) {
			nodeService.addAspect(nodeRef, aatModel.ASPECT_TRANSCRIBER,
					properties);
			if (logger.isDebugEnabled()) {
				logger.debug(this.getClass().getName()
						+ ": Aspect Transcriber added to " + nodeRef.getId());
			}
		} else {
			nodeService.setProperties(nodeRef, properties);
			if (logger.isDebugEnabled()) {
				logger.debug(this.getClass().getName()
						+ ": Update properties for Transcriber to "
						+ nodeRef.getId());
			}
		}
	}

	/**
	 * @param contentService
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * @param nodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * @param store
	 */
	public void setStore(String store) {
		this.store = store;
	}

	/**
	 * @param acousticModelPath
	 */
	public void setAcousticModelPath(String acousticModelPath) {
		this.acousticModelPath = acousticModelPath;
	}

	/**
	 * @param dictionaryPath
	 */
	public void setDictionaryPath(String dictionaryPath) {
		this.dictionaryPath = dictionaryPath;
	}

	/**
	 * @param languageModelPath
	 */
	public void setLanguageModelPath(String languageModelPath) {
		this.languageModelPath = languageModelPath;
	}
}
