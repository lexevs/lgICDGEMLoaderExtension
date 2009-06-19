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

public abstract class BaseConcept {
    private String _code;
    private String _sourceCodingScheme;

    public void init(String code, String sourceCodingScheme) {
    	_code = code;
    	_sourceCodingScheme = sourceCodingScheme;
    }
    
    public String getCode() {
    	return _code;
    }
    
    public String getSourceCodingScheme() {
    	return _sourceCodingScheme;
    }
    
    public boolean equals(Object o) {
        if ((o instanceof ICD10CMConcept) == true || (o instanceof ICD10PCSConcept == true) ||
        	(o instanceof ICD9CMConcept) == true || (o instanceof ICD9PCSConcept == true)) {
        	BaseConcept bc = (BaseConcept)o;
        	if((bc.getCode().equalsIgnoreCase(this.getCode()) == true) && 
        		(bc.getSourceCodingScheme().equalsIgnoreCase(this.getSourceCodingScheme()) == true)) {
        		return true;
        	} else {
        		return false;
        	}
        } else {
        	return false;
        }
           
    	
    }
}