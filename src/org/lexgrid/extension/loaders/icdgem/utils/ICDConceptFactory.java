/*
 * Copyright: (c) 2004-2009 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package org.lexgrid.extension.loaders.icdgem.utils;

import org.LexGrid.messaging.LgMessageDirectorIF;

public class ICDConceptFactory {
	
	public static BaseConcept createConcept(int conType, String code, ICDGEMProperties props) {
		BaseConcept rv = null;
		if(conType == ICDGEMConstants.CON_TYPE_10_CM) {
			rv = new ICD10CMConcept(code, props);
		} else if (conType == ICDGEMConstants.CON_TYPE_10_PCS) {
			rv = new ICD10PCSConcept(code, props);
		} else if(conType == ICDGEMConstants.CON_TYPE_9_CM) {
			rv = new ICD9CMConcept(code, props);
		} else if(conType == ICDGEMConstants.CON_TYPE_9_PCS) {
			rv = new ICD9PCSConcept(code, props);
		} else if(conType == ICDGEMConstants.CON_TYPE_ROOT) {
			rv = new RootConcept(props);
		} else {
			LgMessageDirectorIF md = props.getMessageDirector();
			md.error("ICDConceptFactory: createConcpet: invalid concept type identifier: " + conType);
			rv = null;
		}
		return rv;
	}
}
