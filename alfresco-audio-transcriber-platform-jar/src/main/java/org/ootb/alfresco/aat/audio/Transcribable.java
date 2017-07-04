package org.ootb.alfresco.aat.audio;

import java.util.HashMap;

import edu.cmu.sphinx.api.Configuration;

public interface Transcribable {

	Configuration configuration(HashMap properties);
	
}
