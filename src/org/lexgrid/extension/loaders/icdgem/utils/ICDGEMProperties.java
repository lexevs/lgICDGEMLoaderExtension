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
 *      http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package org.lexgrid.extension.loaders.icdgem.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.LexGrid.LexBIG.DataModel.InterfaceElements.LoadStatus;
import org.LexGrid.LexBIG.Impl.loaders.MessageDirector;
import org.LexGrid.messaging.LgMessageDirectorIF;

/**
 * Globally used data from property file.
 * 
 * @author Michael Turk (turk.michael@mayo.edu)
 */

public class ICDGEMProperties {
    // loader values
    private String _loaderName;
    private String _loaderVersion;    
    private String _loaderDescription;
    private String _loaderTextDescription;
    
    // icdgem values
    private String _icdGemOid;
    private String _icdGemDescription;
    private String _icdGemLocalName;
    private String _icdGemSource;
    private String _icdGemCopyright;
    private int    _icdGemType;
    
    private LgMessageDirectorIF _md = null;
    private String _codingSchemeVersion = null;
    private Properties _props;
    
    public ICDGEMProperties(String icdGemType, String icdGemVersion, LgMessageDirectorIF md) {
    	if (md == null) {
        	LoadStatus loadStatus = new LoadStatus();
            loadStatus.setLoadSource(null); // doesn't apply
            loadStatus.setStartTime(new Date(System.currentTimeMillis()));
            _md = new MessageDirector("ICDGEMProperties", loadStatus); 
    	} else {
    		_md = md;
    	}
    	
    	if(icdGemVersion == null) {
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
        	_codingSchemeVersion = formatter.format(new Date(System.currentTimeMillis()));    		
    	}
    	
    	if(icdGemType == null) {
    		_md.error("ICDGEMProperties: <constructor>: icdGemType cannot be null.  Valid values are 'i10to9cm', 'i9to10cm', 'i10to9pcs', 'i9to10pcs'.");
    	} else {
        	if(icdGemType.equalsIgnoreCase(ICDGEMConstants.ICD10_TO_9_CM_DESC)) {
        		_icdGemType = ICDGEMConstants.ICD10_TO_9_CM; 
        	} else if(icdGemType.equalsIgnoreCase(ICDGEMConstants.ICD10_TO_9_PCS_DESC)) {
        		_icdGemType = ICDGEMConstants.ICD10_TO_9_PCS;
        	} else if(icdGemType.equalsIgnoreCase(ICDGEMConstants.ICD9_TO_10_CM_DESC)) {
        		_icdGemType = ICDGEMConstants.ICD10_TO_9_CM;
        	} else if(icdGemType.equalsIgnoreCase(ICDGEMConstants.ICD9_TO_10_PCS_DESC)) {
        		_icdGemType = ICDGEMConstants.ICD10_TO_9_PCS;
        	} else {
        		_md.error("ICDGEMProperties: <constructor>: unrecoginized icdGemType: '" + icdGemType + "' Valid values are 'cm' or 'pcs'.");	
        	}
    	}
    	readProperties();
    }
    
    private ICDGEMProperties() {
    	
    }
    
    public String getLoaderVersion() {
        return _loaderVersion;    	
    }
    
    public String getProperty(String propertyName) {
        return _props.getProperty(propertyName);    	
    }    

    /*
     * readProperties()
     * read values from loaderEx.properties
     * 
     */    
    private void readProperties() {
    	
    	_props = new java.util.Properties();
    	ICDGEMProperties util = new ICDGEMProperties();
        try {
			_props.load(util.getClass().getClassLoader().getResourceAsStream(ICDGEMConstants.PROP_FILE_NAME));
		} catch (IOException e) {
			e.printStackTrace();
		}
		_loaderName = _props.getProperty(ICDGEMConstants.PROP_LOADER_NAME);
		_loaderVersion = _props.getProperty(ICDGEMConstants.PROP_LOADER_VERSION);
		_loaderDescription = _props.getProperty(ICDGEMConstants.PROP_LOADER_DESCR);
		_loaderTextDescription = _props.getProperty(ICDGEMConstants.PROP_LOADER_TEXT_INPUT_DESCR);
		
		if(this._icdGemType == ICDGEMConstants.ICD10_TO_9_CM) {
			_icdGemOid = _props.getProperty(ICDGEMConstants.PROP_ICD10_TO_9_CM_OID);
			_icdGemDescription = _props.getProperty(ICDGEMConstants.PROP_ICD10_TO_9_CM_DESCR);
			_icdGemLocalName = _props.getProperty(ICDGEMConstants.PROP_ICD10_TO_9_CM_LOCAL_NAME);
			_icdGemSource = _props.getProperty(ICDGEMConstants.PROP_ICD10_TO_9_CM_SOURCE);
			_icdGemCopyright = _props.getProperty(ICDGEMConstants.PROP_ICD10_TO_9_CM_COPYRIGHT);			
		} else if (this._icdGemType == ICDGEMConstants.ICD10_TO_9_PCS) {
			_icdGemOid = _props.getProperty(ICDGEMConstants.PROP_ICD10_TO_9_PCS_OID);
			_icdGemDescription = _props.getProperty(ICDGEMConstants.PROP_ICD10_TO_9_PCS_DESCR);
			_icdGemLocalName = _props.getProperty(ICDGEMConstants.PROP_ICD10_TO_9_PCS_LOCAL_NAME);
			_icdGemSource = _props.getProperty(ICDGEMConstants.PROP_ICD10_TO_9_PCS_SOURCE);
			_icdGemCopyright = _props.getProperty(ICDGEMConstants.PROP_ICD10_TO_9_PCS_COPYRIGHT);			
		} else if (this._icdGemType == ICDGEMConstants.ICD9_TO_10_CM) {
			_icdGemOid = _props.getProperty(ICDGEMConstants.PROP_ICD9_TO_10_CM_OID);
			_icdGemDescription = _props.getProperty(ICDGEMConstants.PROP_ICD9_TO_10_CM_DESCR);
			_icdGemLocalName = _props.getProperty(ICDGEMConstants.PROP_ICD9_TO_10_CM_LOCAL_NAME);
			_icdGemSource = _props.getProperty(ICDGEMConstants.PROP_ICD9_TO_10_CM_SOURCE);
			_icdGemCopyright = _props.getProperty(ICDGEMConstants.PROP_ICD9_TO_10_CM_COPYRIGHT);			
		} else if (this._icdGemType == ICDGEMConstants.ICD9_TO_10_PCS) {
			_icdGemOid = _props.getProperty(ICDGEMConstants.PROP_ICD9_TO_10_PCS_OID);
			_icdGemDescription = _props.getProperty(ICDGEMConstants.PROP_ICD9_TO_10_PCS_DESCR);
			_icdGemLocalName = _props.getProperty(ICDGEMConstants.PROP_ICD9_TO_10_PCS_LOCAL_NAME);
			_icdGemSource = _props.getProperty(ICDGEMConstants.PROP_ICD9_TO_10_PCS_SOURCE);
			_icdGemCopyright = _props.getProperty(ICDGEMConstants.PROP_ICD9_TO_10_PCS_COPYRIGHT);			
		} else {
			_md.error("ICDGEMProperties: readProperties: unrecoginized icdGemType: '" + _icdGemType + "'.");			
		}
    }
    
    public int getIcdGemType() {
    	return _icdGemType;
    }
    
    public void setIcdGemVersion(String version) {
    	_codingSchemeVersion = version;
    }
    
    public String getIcdGemVersion() {
    	if(_codingSchemeVersion == null) {
    		// SQLTableConstants.TBLCOLVAL_MISSING
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
        	_codingSchemeVersion = formatter.format(new Date(System.currentTimeMillis()));
    	}
    	return _codingSchemeVersion;
    }
    
    public String getIcdGemCopyright() {
        return _icdGemCopyright;
    }        
    
    public String getIcdGemSource() {
        return _icdGemSource;
    }    
    
    
    public String getIcdGemLocalName() {
        return _icdGemLocalName;
    }    
    
    
    public String getIcdGemDescription() {
        return _icdGemDescription;
    }    
    
    public String getIcdGemOid() {
        return _icdGemOid;
    }
    
    public String getLoaderTextDescrption() {
        return _loaderTextDescription;
    }
    
    public String getLoaderDescription() {
        return _loaderDescription;    	
    }
    
    public String getLoaderName() {
        return _loaderName;
    }            
}
