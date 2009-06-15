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
package org.lexgrid.lexevs.loaders.icdgem.impl.test;

import junit.framework.TestCase;

import org.lexgrid.extension.loaders.icdgem.admin.LoadICDGEM;
import org.lexgrid.extension.loaders.icdgem.impl.ICDGEMLoaderImpl;

import org.LexGrid.LexBIG.DataModel.InterfaceElements.types.ProcessState;
import org.LexGrid.LexBIG.Exceptions.LBException;

/**
 * This class tests the ICD GEM loader via JUnit.
 * 
 * @author <A HREF="mailto:turk.michael@mayo.edu">Michael Turk</A>
 * @version subversion $Revision: $ checked in on $Date: $
 */
public class ICDGEMTest extends TestCase {
    
    public ICDGEMTest(String serverName) {
        super(serverName);
    }
    
    public void runIt(String[] args) throws Exception {
    	LoadICDGEM runner = new LoadICDGEM();
//    	try {
			runner.run(args);
			ICDGEMLoaderImpl icdGemLoader = runner.getLoader();
	        while (icdGemLoader.getStatus().getEndTime() == null) {
	            Thread.sleep(500);
	        }	        
	        assertTrue(icdGemLoader.getStatus().getState().getType() == ProcessState.COMPLETED_TYPE);
	        assertFalse(icdGemLoader.getStatus().getErrorsLogged().booleanValue());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}    	
    }
/*    
    public void testLoadICD10To9Cm() throws Exception {
    	String[] args = {"-in", "resources/testData/icdgem/small_2009_I10gem.txt",
    			"-type", "i10to9cm", "-ver", "JUnit", "-t", "JUnitTest" };
    	this.runIt(args);
    }
*/
    
    public void testLoadICD9To10Cm() throws Exception {
    	String[] args = {"-in", "resources/testData/icdgem/super_small_2009_I9gem.txt",
    			"-type", "i9to10cm", "-ver", "JUnit", "-t", "JUnitTest" };
    	this.runIt(args);    }
/*    
    public void testLoadICD9To10Pcs() throws Exception {
    	String[] args = {"-in", "resources/testData/icdgem/small_gem_i9pcs.txt",
    			"-type", "i9to10pcs", "-ver", "JUnit", "-t", "JUnitTest" };
    	this.runIt(args);    }
    
    public void testLoadICD10To9Pcs() throws Exception {
    	String[] args = {"-in", "resources/testData/icdgem/small_gem_pcsi9.txt",
    			"-type", "i10to9pcs", "-ver", "JUnit", "-t", "JUnitTest" };
    	this.runIt(args);
    }
*/    
}