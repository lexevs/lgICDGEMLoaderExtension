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
package org.lexgrid.extension.loaders.icdgem.impl;

import java.net.URI;

import org.LexGrid.LexBIG.DataModel.InterfaceElements.LoadStatus;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Exceptions.LBParameterException;
import org.LexGrid.messaging.LgMessageDirectorIF;
import org.lexgrid.extension.loaders.icdgem.convert.GEMBaseLoader;
import org.lexgrid.extension.loaders.icdgem.inputFormat.ICDGEMText;
import org.lexgrid.extension.loaders.icdgem.interfaces.ICDGEMLoader;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;

import edu.mayo.informatics.lexgrid.convert.exceptions.ConnectionFailure;
import edu.mayo.informatics.lexgrid.convert.formats.Option;

/**
 * Class to load ICD GEM source text file into the LexGrid database.
 * 
 * @author <A HREF="mailto:turk.michael@mayo.edu">Michael Turk</A>
 * @version subversion $Revision: $ checked in on $Date: $
 */
public class ICDGEMLoaderImpl extends GEMBaseLoader implements ICDGEMLoader {
    private static final long serialVersionUID = 5405545553067402762L;
    private LgMessageDirectorIF messagesDirector;
    private ICDGEMProperties _props;

    public ICDGEMLoaderImpl() {
    	_props = null;
    	messagesDirector = super.getLogger();
    	messagesDirector.debug("ICDGEMLoaderImpl: ICDGEMLoaderImpl(): entry");
        messagesDirector.debug("ICDGEMLoaderImpl: ICDGEMLoaderImpl(): exit");
    }
    
    
    public ICDGEMLoaderImpl(ICDGEMProperties props) {
    	_props = props;
    	messagesDirector = super.getLogger();
    	
    	messagesDirector.debug("ICDGEMLoaderImpl: ICDGEMLoaderImpl(props): entry");
    	String ver = _props.getLoaderVersion();
    	messagesDirector.info("ICDGEMLoaderImpl: ICDGEMLoaderImpl(props): Loader version: " + ver);
        super.setName(_props.getLoaderName());
        super.setDescription(_props.getLoaderDescription());
        messagesDirector.debug("ICDGEMLoaderImpl: ICDGEMLoaderImpl(props): exit");
    }

    public void load(URI icd10inputFile, boolean stopOnErrors, boolean async) throws LBException {
        messagesDirector.debug("ICDGEMLoaderImpl: load: entry");
        messagesDirector.debug("ICDGEMLoaderImpl: load: async: " + async);        
        messagesDirector.debug("ICDGEMLoaderImpl: load: file: " + getStringFromURI(icd10inputFile));                
        messagesDirector.debug("ICDGEMLoaderImpl: load: stopOnErrors: " + stopOnErrors);        
        messagesDirector.debug("ICDGEMLoaderImpl: load: codingSchemeVersion: " + _props.getIcdGemVersion());
        setInUse();
        try {
        	inputFormatInterface = new ICDGEMText(getStringFromURI(icd10inputFile), _props);
        	inputFormatInterface.testConnection();
        } catch (ConnectionFailure e) {
            inUse = false;
            throw new LBParameterException("ICDGEMLoaderImpl: load: path to input file appears to be invalid - " + e);
        }
        loadStatus = new LoadStatus();
        loadStatus.setLoadSource(getStringFromURI(icd10inputFile));

        optionHolder.add(new Option(Option.FAIL_ON_ERROR, new Boolean(stopOnErrors)));
        optionHolder.add(new Option(Option.DO_WITH_EMF, new Boolean(false)));

        baseLoad(async, _props);
        messagesDirector.debug("ICDGEMLoaderImpl: load: exit");
    }

    public void finalize() throws Throwable {
        getLogger().loadLogDebug("ICDGEMLoaderImpl: finalize: freeing ICDGEMLoaderImpl");                 
        super.finalize();
    }

}