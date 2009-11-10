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
package org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects;

import java.util.Properties;

import org.lexgrid.lexevs.loaders.icdgem.impl.test.properties.ICDGEMTestPropertiesFactory;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.utils.ICDGEMTestConstants;

public class ICDGEMTestBasicDO {
	public static final int I9_10_CM   = 1;
	public static final int I10_9_CM   = 2;
	public static final int I9_10_PCS  = 3;
	public static final int I10_9_PCS  = 4;
	
	private String _csUri;
	private String _csVer;
	
	private ICDGEMTestBasicDO(String csUri, String csVer) 
	{
		_csUri = csUri;
		_csVer = csVer;
	}
	
	public static ICDGEMTestBasicDO createObj(int type) {
		switch(type) {
			case I9_10_CM: return dataForI9To10Cm();
			case I10_9_CM: return dataForI10To9Cm();
			case I9_10_PCS: return dataForI9To10Pcs();
			case I10_9_PCS: return dataForI10To9Pcs();
			default: System.out.println("ICDGEMTestBasicDO: createObj: invalid switch: " + type ); return null;
		}
	}
	
	private static ICDGEMTestBasicDO dataForI9To10Cm() {
		Properties mapProps = ICDGEMTestPropertiesFactory.getProperties(ICDGEMTestPropertiesFactory.I9_10_CM);
		String csUri = mapProps.getProperty(ICDGEMTestConstants.URI);
		String csVer = ICDGEMTestConstants.JUNIT;
		return new ICDGEMTestBasicDO(csUri, csVer);
	}
	
	private static ICDGEMTestBasicDO dataForI10To9Cm() {
		Properties mapProps = ICDGEMTestPropertiesFactory.getProperties(ICDGEMTestPropertiesFactory.I10_9_CM);
		String csUri = mapProps.getProperty(ICDGEMTestConstants.URI);
		String csVer = ICDGEMTestConstants.JUNIT;
		return new ICDGEMTestBasicDO(csUri, csVer);
	}
	
	private static ICDGEMTestBasicDO dataForI9To10Pcs() {
		Properties mapProps = ICDGEMTestPropertiesFactory.getProperties(ICDGEMTestPropertiesFactory.I9_10_PCS);
		String csUri = mapProps.getProperty(ICDGEMTestConstants.URI);
		String csVer = ICDGEMTestConstants.JUNIT;
		return new ICDGEMTestBasicDO(csUri, csVer);
	}
	
	private static ICDGEMTestBasicDO dataForI10To9Pcs() {
		Properties mapProps = ICDGEMTestPropertiesFactory.getProperties(ICDGEMTestPropertiesFactory.I10_9_PCS);
		String csUri = mapProps.getProperty(ICDGEMTestConstants.URI);
		String csVer = ICDGEMTestConstants.JUNIT;
		return new ICDGEMTestBasicDO(csUri, csVer);
	}
	
	public String getCsUri() {
		return _csUri;
	}
	public String getCsVer() {
		return _csVer;
	}
	

}
