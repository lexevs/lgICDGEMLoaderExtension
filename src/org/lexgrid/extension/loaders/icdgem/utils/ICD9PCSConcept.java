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

public class ICD9PCSConcept extends BaseConcept {
	
	public ICD9PCSConcept(String code, ICDGEMProperties props) {
		StringBuffer sb = new StringBuffer(code.length() + 1);    		
        sb.append(code.substring(0, 2));
        sb.append('.');
        sb.append(code.substring(2));    		
        super.init(sb.toString(), props.getIcd9CmLocalName());
	}
	
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Code: ");
    	sb.append(super.getCode());
    	sb.append(" ");
    	sb.append("Type: ");
    	sb.append("ICD-9 PCS");
        return sb.toString();
    }
}
