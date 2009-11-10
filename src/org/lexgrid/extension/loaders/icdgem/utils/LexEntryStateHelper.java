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

import java.sql.Timestamp;

import org.LexGrid.commonTypes.Source;
import org.LexGrid.versions.EntryState;
import org.apache.commons.lang.StringUtils;

/**
 * Convenience methods to assist the ICD9 to EMF conversion.
 * 
 * @author Michael Turk (turk.michael@mayo.edu)
 */

public class LexEntryStateHelper {

    private int _entryStateId;
    private String _entryType;
    private String _owner;
    private String _status;
    private Timestamp _effectiveDate;
    private Timestamp _expirationDate;
    private String _revisionId;
    private String _previsionId;
    private String _changeType;
    private boolean _isActive;
    private int _relativeOrder;
    private EntryState _es;
        
    public LexEntryStateHelper(String entryType) {
//    	1, 'property', 'sampleOwner', 'sampleStatus', '2001-12-17 03:30:47', '2001-12-17 03:30:47', 'sampleRevB', 'sampleRevA', 'NEW', 1,
    	_entryStateId = NumGen.getNextEntryStateId();
    	_entryType = entryType;
    	_owner = "Default Owner";
    	_status = "Default Status";
    	_effectiveDate = new Timestamp(System.currentTimeMillis());
    	_expirationDate = new Timestamp(System.currentTimeMillis());
    	_revisionId = "Default Revision ID";
    	_previsionId = "Default pRevision ID";
    	_relativeOrder = -1;
    	_isActive = true;
    	_changeType = org.LexGrid.versions.types.ChangeType.NEW.toString();
    }
    
    public boolean getIsActive() {
    	return _isActive;
    }
    
    public Source getOwnerAsSourceObj() {
    	Source src = new Source();
        src.setContent(_owner);
        return src;
    }
    
    public EntryState getEntryStateObj() {
    	return _es;
    }
    
    public int getEntryStateId() {
    	return _entryStateId;
    }
    
    public String getEntryType() {
    	return _entryType;
    }
    
    public String getOwner() {
    	return _owner;
    }
    
    public String getStatus() {
    	return _status;
    }
    
    public Timestamp getEffectiveDate() {
    	return _effectiveDate;
    }
    
    public Timestamp getExpirationDate() {
    	return _expirationDate;
    }
    
    public String getRevisionId() {
    	return _revisionId;
    }
    
    public String getPrevisionId() {
    	return _previsionId;
    }
    
    public String getChangeType() {
    	return _changeType;
    }
    
    public int getRelativeOrder() {
    	return _relativeOrder;
    }
    
//    private String getCurDate() {
//    	String rv = null;
//    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//    	rv = formatter.format(new Date(System.currentTimeMillis()));
//    	return  rv;
//    }
    
}
