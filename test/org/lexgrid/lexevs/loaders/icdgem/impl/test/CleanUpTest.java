/*
 * Copyright: (c) 2004-2007 Mayo Foundation for Medical Education and 
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
package org.lexgrid.lexevs.loaders.icdgem.impl.test;

import junit.framework.TestCase;

import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Impl.testUtility.ServiceHolder;
import org.LexGrid.LexBIG.LexBIGService.LexBIGServiceManager;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;


public class CleanUpTest extends TestCase {
   
	private void removeLoad(String uri, String version) throws LBException {
		ServiceHolder.configureForSingleConfig();
		LexBIGServiceManager lbsm = ServiceHolder.instance().getLexBIGService().getServiceManager(null);
		AbsoluteCodingSchemeVersionReference a = ConvenienceMethods.createAbsoluteCodingSchemeVersionReference(
				uri, version);
		lbsm.deactivateCodingSchemeVersion(a,null);
		lbsm.removeCodingSchemeVersion(a);		
	}
	
	public void testRemoveLoadIcd9to10cm()throws LBException {
		this.removeLoad("urn:oid:2.16.840.1.113883.6.100", "JUnit");
	}
	
	public void testRemoveLoadIcd9to10pcs()throws LBException {
		this.removeLoad("urn:oid:2.16.840.1.113883.6.101", "JUnit");
	}
	
	public void testRemoveLoadIcd10to9cm()throws LBException {
		this.removeLoad("urn:oid:2.16.840.1.113883.6.102", "JUnit");
	}

	public void testRemoveLoadIcd10to9pcs()throws LBException {
		this.removeLoad("urn:oid:2.16.840.1.113883.6.103", "JUnit");
	}
	
	
}



