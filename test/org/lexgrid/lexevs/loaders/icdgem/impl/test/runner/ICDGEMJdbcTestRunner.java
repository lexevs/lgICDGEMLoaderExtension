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
package org.lexgrid.lexevs.loaders.icdgem.impl.test.runner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Impl.dataAccess.ResourceManager;
import org.LexGrid.LexBIG.Impl.helpers.SQLConnectionInfo;
import org.LexGrid.LexBIG.Impl.testUtility.ServiceHolder;
import org.LexGrid.LexBIG.LexBIGService.LexBIGServiceManager;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.LexGrid.util.sql.DBUtility;
import org.LexGrid.util.sql.lgTables.SQLTableConstants;
import org.lexgrid.extension.loaders.icdgem.admin.LoadICDGEM;
import org.lexgrid.extension.loaders.icdgem.impl.ICDGEMLoaderImpl;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects.ICDGEMTestBasicDO;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects.ICDGEMTestJdbcDO;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects.ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects.ICDGEMTestJdbcSelectFromEntityDO;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects.ICDGEMTestJdbcSelectFromEntryStateDO;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.utils.ICDGEMTestConstants;

/**
 * This class will do a few data lookups to make sure expected data is 
 * in the database.
 * 
 * @author <A HREF="mailto:turk.michael@mayo.edu">Michael Turk</A>
 * @version subversion $Revision: $ checked in on $Date: $
 */
public class ICDGEMJdbcTestRunner {

    private final static String TABLE_PREFIX_PLACE_HOLDER = "$tablePrefix$";
    
    // select data from table 'entity' and check values
    // SELECT * FROM entity WHERE entityCode='003.29' 
    private static String _selectAllFromEntitySQL = "SELECT * FROM " + TABLE_PREFIX_PLACE_HOLDER + SQLTableConstants.TBL_ENTITY + " WHERE " + SQLTableConstants.TBLCOL_ENTITYCODE + "=?";
    
    // select data from table 'entryState' and check values
    // SELECT * FROM entityState WHERE entityStateId=7679
    private static String _selectAllEntryStateSQL = "SELECT * FROM " + TABLE_PREFIX_PLACE_HOLDER + SQLTableConstants.TBL_ENTRY_STATE + " WHERE " + SQLTableConstants.TBLCOL_ENTRYSTATEID + "=?";
    
    // select data from table 'entryState' and check values
    // SELECT * FROM entityAssnsToEntity WHERE targetEntityCode='003.29';
    // verify: sourceEntityCode=@, codingSchemeName=ICD-9-CM
    private static String _selectAllEntityAssnsToEntitySQL = "SELECT * FROM " + TABLE_PREFIX_PLACE_HOLDER + SQLTableConstants.TBL_ENTITY_ASSOCIATION_TO_ENTITY + " WHERE " + SQLTableConstants.TBLCOL_TARGETENTITYCODE + "=?";
    

    public ICDGEMJdbcTestRunner(String serverName) {
    }
    
    public static boolean selectFromEntityAssnsToEntity(
    		String csUri, 
    		String csVer,
    		String targetEntityCode,
    		String expectedSourceEntityCode,
    		String expectedCodingSchemeName){
    	boolean success = true;
    	Connection conn = null;
    	PreparedStatement psSelectFromEntityAssnsToEntity = null;
    	
        try {    
    		AbsoluteCodingSchemeVersionReference ref = new AbsoluteCodingSchemeVersionReference();
    		ref.setCodingSchemeURN(csUri);
    		ref.setCodingSchemeVersion(csVer);
            SQLConnectionInfo sci = ResourceManager.instance().getRegistry().getSQLConnectionInfoForCodeSystem(ref);
        	conn = DBUtility.connectToDatabase(sci.server, sci.driver, sci.username, sci.password);
        	
        	String tablePrefix = sci.prefix;
        	
            String selectAllEntityAssnsToEntitySQL = replaceTablePrefixHolder(_selectAllEntityAssnsToEntitySQL, tablePrefix);
        	
        	psSelectFromEntityAssnsToEntity = conn.prepareStatement(selectAllEntityAssnsToEntitySQL);
        	psSelectFromEntityAssnsToEntity.setString(1, targetEntityCode);
        	psSelectFromEntityAssnsToEntity.execute();
        	ResultSet rs = psSelectFromEntityAssnsToEntity.getResultSet();
        	rs.next();
        	String sourceEntityCodeValue = rs.getString(SQLTableConstants.TBLCOL_SOURCEENTITYCODE);
        	if(sourceEntityCodeValue.indexOf(expectedSourceEntityCode) == -1) {
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntityAssnsToEntity: test failure");
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntityAssnsToEntity: expected: " + expectedSourceEntityCode);
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntityAssnsToEntity: actual  : " + sourceEntityCodeValue);
        		success = false;
        	}
        	
        	String codingSchemeNameValue = rs.getString(SQLTableConstants.TBLCOL_CODINGSCHEMENAME);
        	if(codingSchemeNameValue.indexOf(expectedCodingSchemeName) == -1) {
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntityAssnsToEntity: test failure");
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntityAssnsToEntity: expected: " + expectedCodingSchemeName);
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntityAssnsToEntity: actual  : " + codingSchemeNameValue);        		
        		success = false;
        	}        	
        	
        } catch (Exception e) {
        	cleanUp(conn, psSelectFromEntityAssnsToEntity);
        	success = false;
        	System.out.println("ICDGEMJdbcTestRunner: selectFromEntityAssnsToEntity: exception: " + e.getMessage());
        	e.printStackTrace();
        }
        cleanUp(conn, psSelectFromEntityAssnsToEntity);
        return success;
    }
    
    public static boolean selectFromEntity(
    		String csUri, 
    		String csVer,
    		String targetEntityCode,
    		String expectedEntityDescription
    		){
    	boolean success = true;
    	Connection conn = null;
    	
    	PreparedStatement psSelectFromEntity = null;
    	
        try {    
    		AbsoluteCodingSchemeVersionReference ref = new AbsoluteCodingSchemeVersionReference();
    		ref.setCodingSchemeURN(csUri);
    		ref.setCodingSchemeVersion(csVer);
            SQLConnectionInfo sci = ResourceManager.instance().getRegistry().getSQLConnectionInfoForCodeSystem(ref);
        	conn = DBUtility.connectToDatabase(sci.server, sci.driver, sci.username, sci.password);
        	
        	String tablePrefix = sci.prefix;
        	
            String selectFromEntity = replaceTablePrefixHolder(_selectAllFromEntitySQL, tablePrefix);
        	
        	psSelectFromEntity = conn.prepareStatement(selectFromEntity);
        	psSelectFromEntity.setString(1, targetEntityCode);
        	psSelectFromEntity.execute();
        	ResultSet rs = psSelectFromEntity.getResultSet();
        	rs.next();
        	String entityDescriptionValue = rs.getString(SQLTableConstants.TBLCOL_ENTITYDESCRIPTION);
        	if(entityDescriptionValue.indexOf(expectedEntityDescription) == -1) {
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntity: test failure");
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntity: expected: " + expectedEntityDescription);
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntity: actual  : " + entityDescriptionValue);        		
        		success = false;
        	}
        } catch (Exception e) {
        	cleanUp(conn, psSelectFromEntity);
        	success = false;
        	System.out.println("ICD10JdbcLookupTest: selectFromEntity: exception: " + e.getMessage());
        	e.printStackTrace();
        }
        cleanUp(conn, psSelectFromEntity);
        return success;
    }
        
    public static boolean selectFromEntryState(    		
    		String csUri, 
    		String csVer,
    		String targetEntityCode,
    		String expectedEntryType
    		){
    	boolean success = true;
    	Connection conn = null;
    	PreparedStatement psSelectFromEntity = null;
    	PreparedStatement psSelectFromEntryState = null;
    	
        try {    
    		AbsoluteCodingSchemeVersionReference ref = new AbsoluteCodingSchemeVersionReference();
    		ref.setCodingSchemeURN(csUri);
    		ref.setCodingSchemeVersion(csVer);
            SQLConnectionInfo sci = ResourceManager.instance().getRegistry().getSQLConnectionInfoForCodeSystem(ref);
        	conn = DBUtility.connectToDatabase(sci.server, sci.driver, sci.username, sci.password);
        	
        	String tablePrefix = sci.prefix;
            String selectFromEntity = replaceTablePrefixHolder(_selectAllFromEntitySQL, tablePrefix);
        	
        	psSelectFromEntity = conn.prepareStatement(selectFromEntity);
        	psSelectFromEntity.setString(1, targetEntityCode);
        	psSelectFromEntity.execute();
        	ResultSet rs1 = psSelectFromEntity.getResultSet();
        	rs1.next();
        	int entryStateId = rs1.getInt(SQLTableConstants.TBLCOL_ENTRYSTATEID);
        	
        	String selectFromEntryState = replaceTablePrefixHolder(_selectAllEntryStateSQL, tablePrefix);
        	psSelectFromEntryState = conn.prepareStatement(selectFromEntryState);
        	psSelectFromEntryState.setInt(1, entryStateId);
        	psSelectFromEntryState.execute();
        	
        	ResultSet rs2 = psSelectFromEntryState.getResultSet();
        	rs2.next();
        	String actualValue = rs2.getString(SQLTableConstants.TBLCOL_ENTRYTYPE);
        	
        	if(actualValue.indexOf(expectedEntryType) == -1) {
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntryState: test failure");
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntryState: expected: " + expectedEntryType);
        		System.out.println("ICDGEMJdbcTestRunner: selectFromEntryState: actual  : " + actualValue);        		
        		success = false;
        	}
        } catch (Exception e) {
        	cleanUp(conn, psSelectFromEntity);
        	cleanUp(conn, psSelectFromEntryState);
        	success = false;
        	System.out.println("ICD10JdbcLookupTest: selectFromEntryState: exception: " + e.getMessage());
        	e.printStackTrace();
        }
        cleanUp(conn, psSelectFromEntity);
        cleanUp(conn, psSelectFromEntryState);
        return success;
    }
    
    private static String replaceTablePrefixHolder(String sqlString, String tablePrefix) {
    	return sqlString.replace(TABLE_PREFIX_PLACE_HOLDER, tablePrefix);
    }
    
    private static void cleanUp(Connection conn, PreparedStatement ps) {
    	if(conn != null) {
    		try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    	if(ps != null) {
    		try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    }

//-----------------------------------------------------------------------------
// Code below this point is for testing this class.                           -
// -start block -                                                             -    
//-----------------------------------------------------------------------------
    
    public static void loadICD10(String[] args) throws InterruptedException, LBException {
    	LoadICDGEM runner = new LoadICDGEM();
    	try {
			runner.run(args);
			ICDGEMLoaderImpl icdGemLoader = runner.getLoader();
	        while (icdGemLoader.getStatus().getEndTime() == null) {
	            Thread.sleep(500);
	        }	        
//	        assertTrue(icdGemLoader.getStatus().getState().getType() == ProcessState.COMPLETED_TYPE);
//	        assertFalse(icdGemLoader.getStatus().getErrorsLogged().booleanValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void removeCodingScheme(String csUri, String csVersion) {
    	System.out.println("ICD10JdbcLookupTest: removeCodingScheme: remove coding scheme: uri: " + csUri + " ver: " + csVersion);
        ServiceHolder.configureForSingleConfig();
		LexBIGServiceManager lbsm;
		try {
			lbsm = ServiceHolder.instance().getLexBIGService().getServiceManager(null);
			AbsoluteCodingSchemeVersionReference a = ConvenienceMethods.createAbsoluteCodingSchemeVersionReference(
					csUri,csVersion);
			lbsm.deactivateCodingSchemeVersion(a,null);
			lbsm.removeCodingSchemeVersion(a);
			System.out.println("ICD10JdbcLookupTest: removeCodingScheme: coding scheme removed successfully");
		} catch (LBException e) {
			e.printStackTrace();
		}		
    }

    public static void loadIt(String[] args) throws Exception {
    	LoadICDGEM runner = new LoadICDGEM();
		runner.run(args);
		ICDGEMLoaderImpl icdGemLoader = runner.getLoader();
        while (icdGemLoader.getStatus().getEndTime() == null) {
            Thread.sleep(500);
        }	        
    }
    
    public static void doJdbcTest(ICDGEMTestJdbcDO dataObj) {
    	
    	ICDGEMTestJdbcSelectFromEntityDO fromEntity = dataObj.getSelectFromEntityDO();
    	boolean results = ICDGEMJdbcTestRunner.selectFromEntity(
    			fromEntity.getCsUri(),
    			fromEntity.getCsVer(),
    			fromEntity.getTargetEntityCode(),
    			fromEntity.getExpectedEntityDescription());
    	System.out.println("ICDGEMJdbcTestRunner: doJdbcTest: selectFromEntity result: " + results);
    	
    	ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO fromEntityAssnsToEntity = dataObj.getSelectFromEntityAssnsToEntityDO();
    	results = ICDGEMJdbcTestRunner.selectFromEntityAssnsToEntity(
    			fromEntityAssnsToEntity.getCsUri(), 
    			fromEntityAssnsToEntity.getCsVer(), 
    			fromEntityAssnsToEntity.getTargetEntityCode(), 
    			fromEntityAssnsToEntity.getExpectedSourceEntityCode(), 
    			fromEntityAssnsToEntity.getExpectedCodingSchemeName());
    	System.out.println("ICDGEMJdbcTestRunner: doJdbcTest: selectFromEntityAssnsToEntity result: " + results);
    	
    	ICDGEMTestJdbcSelectFromEntryStateDO fromEntryStateDO = dataObj.getSelectFromEntryStateDO();
    	results = ICDGEMJdbcTestRunner.selectFromEntryState(
    			fromEntryStateDO.getCsUri(), 
    			fromEntryStateDO.getCsVer(), 
    			fromEntryStateDO.getTargetEntityCode(), 
    			fromEntryStateDO.getExpectedEntryType());
    	System.out.println("ICDGEMJdbcTestRunner: doJdbcTest: selectFromEntryState result: " + results);
    }
    
    public void doTestRun(
    		ICDGEMTestJdbcDO dataObj,
    		String[] args
    		) 
    {
    	System.out.println("ICDGEMJdbcTestRunner: doTestRun: entry");
    	// do the loads
    	try {
	    	ICDGEMJdbcTestRunner.loadIt(args);    	
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	// run the cm jdbc tests
		ICDGEMJdbcTestRunner.doJdbcTest(dataObj);
    	
        // remove the coding schemes
		ICDGEMTestBasicDO basicDO = dataObj.getBasicDO();
		ICDGEMJdbcTestRunner.removeCodingScheme(basicDO.getCsUri(), basicDO.getCsVer());
        System.out.println("ICDGEMJdbcTestRunner: doTestRun: exit");
    }
    
    
    public static void main(String[] args) throws LBException, InterruptedException {
    	/*
    	 * LoadICDGEM.bat -in "file:///C:/mayo/lexbig/test501/test/resources/testData/icdgem/2009_I10gem.txt" -type i10to9cm -ver v2009 -t test -a
    	 */
    	
    	// create program parameter arrays
    	String[] i9to10cmArgs = {"-in", "resources/testData/icdGem/small_2009_I9gem.txt",
    			"-type", "i9to10cm", "-ver", ICDGEMTestConstants.JUNIT, "-t", "JUnitTest" };

    	String[] i10to9cmArgs = {"-in", "resources/testData/icdGem/small_2009_I10gem.txt",
    			"-type", "i10to9cm", "-ver", ICDGEMTestConstants.JUNIT, "-t", "JUnitTest" };    	
    	
    	String[] i9to10pcsArgs = {"-in", "resources/testData/icdGem/small_gem_i9pcs.txt",
    			"-type", "i9to10pcs", "-ver", ICDGEMTestConstants.JUNIT, "-t", "JUnitTest" };    	
    	
    	String[] i10to9pcsArgs = {"-in", "resources/testData/icdGem/small_gem_pcsi9.txt",
    			"-type", "i10to9pcs", "-ver", ICDGEMTestConstants.JUNIT, "-t", "JUnitTest" };    	
    	
    	// create test data objects
    	ICDGEMTestJdbcDO i9to10cmDataObj = ICDGEMTestJdbcDO.createObj(ICDGEMTestJdbcDO.I9_10_CM);
    	ICDGEMTestJdbcDO i10to9cmDataObj = ICDGEMTestJdbcDO.createObj(ICDGEMTestJdbcDO.I10_9_CM);
    	ICDGEMTestJdbcDO i9to10pcsDataObj = ICDGEMTestJdbcDO.createObj(ICDGEMTestJdbcDO.I9_10_PCS);
    	ICDGEMTestJdbcDO i10to9pcsDataObj = ICDGEMTestJdbcDO.createObj(ICDGEMTestJdbcDO.I10_9_PCS);

    	// create test runner
    	ICDGEMJdbcTestRunner myTest = new ICDGEMJdbcTestRunner(null);
    	
    	// run tests
    	myTest.doTestRun(i9to10cmDataObj, i9to10cmArgs);
    	myTest.doTestRun(i10to9cmDataObj, i10to9cmArgs);
    	myTest.doTestRun(i9to10pcsDataObj, i9to10pcsArgs);
    	myTest.doTestRun(i10to9pcsDataObj, i10to9pcsArgs);
    	
        System.out.println("ICD10JdbcLookupTest: main: done.");
        
    }
//-----------------------------------------------------------------------------
// -end block -                                                               -    
//-----------------------------------------------------------------------------
    
}