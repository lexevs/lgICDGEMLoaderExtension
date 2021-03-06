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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
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
	
	// LexGrid values
	private String _lexGridVersion;
	private boolean _lexVerPost50;
	
    // loader values
    private String _loaderName;
    private String _loaderVersion;    
    private String _loaderDescription;
    private String _loaderTextDescription;
    private String _icd9CmLocalName;
    private String _icd9CmUri;
    private String _icd10CmLocalName;
    private String _icd10CmUri;
    private String _icd10PcsLocalName;
    private String _icd10PcsUri;
    
    // gem values
    private String _csUri;
    private String _csDescription;
    private String _csLocalName;
    private String _csSource;
    private String _csCopyright;
    private String _englishName;
    private String _englishUri;
    private String _textualPresentationName;
    private String _textualPresentationUri;
    private String _textPlainName;
    private String _textPlainUri;
    private int    _icdGemType;
    private int    _srcConType;
    private int    _tgtConType;
    
    private LgMessageDirectorIF _md = null;
    private String _codingSchemeVersion = null;
    private Properties _loaderProps;
    private Properties _gemProps;
    
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
    	} else {
    		_codingSchemeVersion = icdGemVersion;
    	}
    	
    	if(icdGemType == null) {
    		_md.error("ICDGEMProperties: <constructor>: icdGemType cannot be null.  Valid values are 'i10to9cm', 'i9to10cm', 'i10to9pcs', 'i9to10pcs'.");
    	} else {
        	if(icdGemType.equalsIgnoreCase(ICDGEMConstants.ICD10_TO_9_CM_DESC)) {
        		_icdGemType = ICDGEMConstants.ICD10_TO_9_CM;
    			_srcConType = ICDGEMConstants.CON_TYPE_10_CM;
    			_tgtConType = ICDGEMConstants.CON_TYPE_9_CM;
        		
        	} else if(icdGemType.equalsIgnoreCase(ICDGEMConstants.ICD10_TO_9_PCS_DESC)) {
        		_icdGemType = ICDGEMConstants.ICD10_TO_9_PCS;
    			_srcConType = ICDGEMConstants.CON_TYPE_10_PCS;
    			_tgtConType = ICDGEMConstants.CON_TYPE_9_PCS;			
        		
        	} else if(icdGemType.equalsIgnoreCase(ICDGEMConstants.ICD9_TO_10_CM_DESC)) {        		
        		_icdGemType = ICDGEMConstants.ICD9_TO_10_CM;
    			_srcConType = ICDGEMConstants.CON_TYPE_9_CM;
    			_tgtConType = ICDGEMConstants.CON_TYPE_10_CM;			
        		
        	} else if(icdGemType.equalsIgnoreCase(ICDGEMConstants.ICD9_TO_10_PCS_DESC)) {
    			_srcConType = ICDGEMConstants.CON_TYPE_9_PCS;
    			_tgtConType = ICDGEMConstants.CON_TYPE_10_PCS;			        		
        		_icdGemType = ICDGEMConstants.ICD9_TO_10_PCS;
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
    
    private void readProperties() {
    	
    	_gemProps = new java.util.Properties();
    	_loaderProps = new java.util.Properties();
    	ICDGEMProperties util = new ICDGEMProperties();
        try {
        	_loaderProps.load(util.getClass().getClassLoader().getResourceAsStream(ICDGEMConstants.PROPS_ICD_GEM_LOADER));
        	if(this._icdGemType == ICDGEMConstants.ICD10_TO_9_CM) {
        		_gemProps.load(util.getClass().getClassLoader().getResourceAsStream(ICDGEMConstants.PROPS_ICD10_TO_9_CM));
        	} else if (this._icdGemType == ICDGEMConstants.ICD10_TO_9_PCS) {
        		_gemProps.load(util.getClass().getClassLoader().getResourceAsStream(ICDGEMConstants.PROPS_ICD10_TO_9_PCS));
        	} else if (this._icdGemType == ICDGEMConstants.ICD9_TO_10_CM) {
        		_gemProps.load(util.getClass().getClassLoader().getResourceAsStream(ICDGEMConstants.PROPS_ICD9_TO_10_CM));
        	} else if (this._icdGemType == ICDGEMConstants.ICD9_TO_10_PCS) {
        		_gemProps.load(util.getClass().getClassLoader().getResourceAsStream(ICDGEMConstants.PROPS_ICD9_TO_10_PCS));
        	} else {
    			_md.error("ICDGEMProperties: readProperties: unrecoginized icdGemType: '" + _icdGemType + "'.");			
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// loader properties
		_loaderName = _loaderProps.getProperty("icdgem.loader.name");
		_loaderVersion = _loaderProps.getProperty("icdgem.loader.version");
		_loaderDescription = _loaderProps.getProperty("icdgem.loader.descr");
		_loaderTextDescription = _loaderProps.getProperty("icdgem.loader.text.input.descr");
		_icd9CmLocalName = _loaderProps.getProperty("icd9.cm.local.name"); 
		_icd9CmUri = _loaderProps.getProperty("icd9.cm.uri");		
		_icd10CmLocalName = _loaderProps.getProperty("icd10.cm.local.name"); 
		_icd10CmUri = _loaderProps.getProperty("icd10.cm.uri");
		_icd10PcsLocalName = _loaderProps.getProperty("icd10.pcs.local.name"); 
		_icd10PcsUri = _loaderProps.getProperty("icd10.pcs.uri");
		
		// GEM properties
		_csUri = _gemProps.getProperty("uri");
		_csDescription = _gemProps.getProperty("descr");
		_csLocalName = _gemProps.getProperty("localname");
		_csSource = _gemProps.getProperty("source");
		_csCopyright = _gemProps.getProperty("copyright");	
	    _englishName = _gemProps.getProperty("english.name");
	    _englishUri = _gemProps.getProperty("english.uri");
	    _textualPresentationName = _gemProps.getProperty("text.plain.name");
	    _textualPresentationUri = _gemProps.getProperty("text.plain.uri");
	    _textPlainName = _gemProps.getProperty("textual.presentation.name");
	    _textPlainUri = _gemProps.getProperty("textual.presentation.uri");
	    
	    // get LexGrid version
	    _lexGridVersion = this.findLexGidVersion();
		_md.info("ICD10Utilities: readProperties: _lexGridVersion: " + _lexGridVersion);
		this.setLexGridPost50(_lexGridVersion);
    }
    
    public String getTextualPresentationName() {
    	return _textualPresentationName;
    }
    
    public String getTextualPresentationUri() {
    	return _textualPresentationUri;
    }
    
    public String getTextPlainName() {
    	return _textPlainName;
    }
    
    public String getTextPlainUri() {
    	return _textPlainUri;
    }
    
    public String getEnglishName() {
    	return _englishName;
    }
    
    public String getEnglishUri() {
    	return _englishUri;
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
    
    public String getCsCopyright() {
        return _csCopyright;
    }        
    
    public String getCsSource() {
        return _csSource;
    }    
    
    
    public String getCsLocalName() {
        return _csLocalName;
    }    
    
    
    public String getCsDescription() {
        return _csDescription;
    }    
    
    public String getCsUri() {
        return _csUri;
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
    
    public String getIcd9CmLocalName() {
    	return this._icd9CmLocalName;
    }
    
    public String getIcd9CmUri() {
    	return this._icd9CmUri;
    }
        
    public String getIcd10CmLocalName() {
    	return this._icd10CmLocalName;
    }
    
    public String getIcd10CmUri() {
    	return this._icd10CmUri;
    }
        
    public String getIcd10PcsLocalName() {
    	return this._icd10PcsLocalName;
    }
    
    public String getIcd10PcsUri() {
    	return this._icd10PcsUri;
    }
    
    public int getSrcConType() {
    	return _srcConType;
    }
    
    public int getTgtConType() {
    	return _tgtConType;
    }
    
    public LgMessageDirectorIF getMessageDirector() {
    	return _md;
    }
    
    private void setLexGridPost50(String lexVer) {
    	//-----------------------------
    	// Expected possible versions:
    	//   build.version=5.0.0
    	//   build.version=5.1
    	//-----------------------------
    	StringBuffer num = new StringBuffer();
    	num.append(lexVer.charAt(0));
    	int majorVer = Integer.parseInt(num.toString());
    	num = new StringBuffer();
    	num.append(lexVer.charAt(2));
    	int minorVer = Integer.parseInt(num.toString());
    	if(majorVer == 5) {
    		if(minorVer > 0) {
    			_lexVerPost50 = true;
    		} else {
    			_lexVerPost50 = false;
    		}
    	} else {
    		_md.error("ICD10Utilities: setLexGridPre51: unexpected majorVer: " + majorVer);
    		_md.error("ICD10Utilities: setLexGridPre51: setting lexGridPre51 to false");
    		_md.error("ICD10Utilities: setLexGridPre51: version string: " + lexVer);
    	}
    }
    
    public boolean lexGridPost50() {
    	return _lexVerPost50;
    }
    
    private String findLexGidVersion() {
    	String rv = null;
    	Properties props;
		props = new Properties();
		try {
			// loading properties from the build.properties in the LexBXX
			// install
			props.load(new FileInputStream("../build.properties"));
		} catch (FileNotFoundException e) {
			// Running from webstart or eclipse so try the local directory
			try {
				props.load(new FileInputStream("build.properties"));
			} catch (FileNotFoundException e1) {
				// do nothing
		        try {
					props.load(this.getClass().getClassLoader().getResourceAsStream("build.properties"));
				} catch (Exception ex) {
					// do nothing
				}
			} catch (IOException e1) {
				// do nothing
			}
		} catch (IOException e) {
			// Do Nothing no properties to show
		}
		
		if(props.isEmpty()) {
			// last chance... check for system property, i.e.
			//   -DlexEvsVer=5.2
			String value = System.getProperty("lexEvsVer");
			if(value == null || value.length() == 0) {
				// tried our best to find the version but couldn't
				// default to 5.1
				_md.error("ICD10Utilities: setLexGridPre51: could not determine LexGrid version; assume LexGrid 5.1");
				rv = new String("5.1");
			} else {
				rv = value;
			}
		} else {
			rv = props.getProperty("build.version");
		}
		return rv;
    }
    
	public static void dumpSystemProperties() {
	    Properties props = System.getProperties();
	    Enumeration<Object> keys = props.keys();
	    String key;
	    System.out.println("-------");
	    while(keys.hasMoreElements()) {
	    	key = (String)keys.nextElement();
	    	String value = props.getProperty(key);
	    	System.out.println(key.toString() + " --- " + value.toString());
	    }
	    System.out.println("-------");
	}

    
}
