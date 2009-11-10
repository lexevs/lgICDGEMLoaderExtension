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
package org.lexgrid.lexevs.loaders.icdgem.impl.test.properties;

import java.io.IOException;
import java.util.Properties;

import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMConstants;

public class ICDGEMTestPropertiesFactory {
	
	public static final int GEM_LOADER = 0;
	public static final int I9_10_CM   = 1;
	public static final int I10_9_CM   = 2;
	public static final int I9_10_PCS  = 3;
	public static final int I10_9_PCS  = 4;
	
	public static Properties getProperties(int propType)
	{
		Properties myProps =  new java.util.Properties();
		String fileName = null;
		switch(propType) {
			case GEM_LOADER: fileName = new String(ICDGEMConstants.PROPS_ICD_GEM_LOADER); break;
			case I9_10_CM: fileName = new String(ICDGEMConstants.PROPS_ICD9_TO_10_CM); break;
			case I10_9_CM: fileName = new String(ICDGEMConstants.PROPS_ICD10_TO_9_CM); break;
			case I9_10_PCS: fileName = new String(ICDGEMConstants.PROPS_ICD9_TO_10_PCS); break;
			case I10_9_PCS: fileName = new String(ICDGEMConstants.PROPS_ICD10_TO_9_PCS); break;
			default: System.out.println("ICDGEMTestPropertiesFactory: getProperties: Invalid case: " + propType); break;
		}
		DoNothing localClass = new DoNothing();
        try {
        	myProps.load(localClass.getClass().getClassLoader().getResourceAsStream(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return myProps;
	}	
}
