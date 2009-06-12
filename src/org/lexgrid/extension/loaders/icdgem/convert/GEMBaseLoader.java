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
package org.lexgrid.extension.loaders.icdgem.convert;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.LexGrid.LexBIG.DataModel.Core.LogEntry;
import org.LexGrid.LexBIG.DataModel.Core.types.CodingSchemeVersionStatus;
import org.LexGrid.LexBIG.DataModel.Core.types.LogLevel;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.CodingSchemeRendering;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.LoadStatus;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.types.ProcessState;
import org.LexGrid.LexBIG.Exceptions.LBInvocationException;
import org.LexGrid.LexBIG.Exceptions.LBParameterException;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.Impl.dataAccess.CleanUpUtility;
import org.LexGrid.LexBIG.Impl.dataAccess.ResourceManager;
import org.LexGrid.LexBIG.Impl.dataAccess.SQLInterfaceBase;
import org.LexGrid.LexBIG.Impl.dataAccess.SystemVariables;
import org.LexGrid.LexBIG.Impl.dataAccess.WriteLockManager;
import org.LexGrid.LexBIG.Impl.helpers.SQLConnectionInfo;
import org.LexGrid.LexBIG.Impl.internalExceptions.InternalException;
import org.LexGrid.LexBIG.Impl.internalExceptions.MissingResourceException;
import org.LexGrid.LexBIG.Impl.loaders.MessageDirector;
import org.LexGrid.LexBIG.Impl.logging.LgLoggerIF;
import org.LexGrid.LexBIG.Impl.logging.LoggerFactory;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.Preferences.loader.LoadPreferences.LoaderPreferences;
import org.LexGrid.LexBIG.Utility.Constructors;
import org.LexGrid.LexOnt.CodingSchemeManifest;
import org.LexGrid.util.SimpleMemUsageReporter;
import org.LexGrid.util.SimpleMemUsageReporter.Snapshot;
import org.LexGrid.util.sql.DBUtility;
import org.LexGrid.util.sql.lgTables.SQLTableConstants;
import org.LexGrid.util.sql.lgTables.SQLTableUtilities;
import org.lexgrid.extension.loaders.icdgem.utils.CodingScheme;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;

import edu.mayo.informatics.lexgrid.convert.exceptions.ConnectionFailure;
import edu.mayo.informatics.lexgrid.convert.exceptions.LgConvertException;
import edu.mayo.informatics.lexgrid.convert.formats.InputFormatInterface;
import edu.mayo.informatics.lexgrid.convert.formats.OptionHolder;
import edu.mayo.informatics.lexgrid.convert.formats.outputFormats.LexGridSQLOut;
import edu.mayo.informatics.lexgrid.convert.indexer.SQLIndexer;
import edu.mayo.informatics.lexgrid.convert.utility.Constants;
import edu.mayo.informatics.lexgrid.convert.utility.ManifestUtil;
import edu.mayo.informatics.lexgrid.convert.utility.URNVersionPair;
import edu.mayo.informatics.lexgrid.convert.utility.loaderPreferences.PreferenceLoaderFactory;
import edu.mayo.informatics.lexgrid.convert.utility.loaderPreferences.interfaces.PreferenceLoader;

/**
 * Common loader code.
 * 
 * -----Commentary (06/03/2009) -start-----
 * 
 * This class is a copy and modified version of org.LexGrid.LexBIG.Impl.loaders.BaseLoader.java.
 * We have this duplicate class so that we can use GEMConversionLauncher.java
 * which is itself a modifed version of ConversionLauncher.java.  We need  
 * these two  modified files to help keep the loader extension code
 * loosely coupled from the LexEVS code.
 * 
 * LexEVS ConversionLauncher.java has a block of if-else code that
 * has hard-coded Loader names.  This is fine for loaders that ship with
 * LexEVS but the extension code will have loaders added to it and so
 * we have our own class, GEMConversionLauncher.java to provide
 * the same function of ConversionLauncher.java.
 * 
 * And we need GEMBaseLoader.java so we can call GEMConversionLauncher. All 
 * loaders extend a BaseLoader class, and the one that ships with Lex will call 
 * the Lex ConversionLauncher.
 * 
 * As much as possible, code specific to the loaders that ship with LexEVS
 * has been removed.
 * 
 * 
 * -----Commentary (06/03/2009) -end------
 * 
 * @author <A HREF="mailto:turk.michael@mayo.edu">Michael Turk</A>
 * @version subversion $Revision: $ checked in on $Date: $
 */
public class GEMBaseLoader {

    private String loaderName;
    private String loaderVersion;
    private String loaderDescription;
    private String providerName = "MAYO";

    protected boolean inUse = false;
    protected MessageDirector messageDirector;
    protected LoadStatus loadStatus;
    protected AbsoluteCodingSchemeVersionReference[] acsvr = new AbsoluteCodingSchemeVersionReference[0];
    protected InputFormatInterface inputFormatInterface;
    protected LexGridSQLOut lexGridSqlOut;
    protected OptionHolder optionHolder = new OptionHolder();
    CodingSchemeManifest codingSchemeManifest;
    URI codingSchemeManifestURI;
    LoaderPreferences loaderPreferences;
    ICDGEMProperties _props;

    protected LgLoggerIF getLogger() {
        return LoggerFactory.getLogger();
    }

    protected void baseLoad(boolean async, ICDGEMProperties props) throws LBInvocationException {
        SQLConnectionInfo sci;
        _props = props;
        try {
            sci = ResourceManager.instance().getSQLConnectionInfoForLoad();
            lexGridSqlOut = new LexGridSQLOut(sci.username, sci.password, sci.server, sci.driver, sci.prefix);
            lexGridSqlOut.testConnection();

        } catch (ConnectionFailure e) {
            inUse = false;
            String id = getLogger().error("Problem connecting to the sql server", e);
            throw new LBInvocationException("There was a problem connecting to the internal sql server", id);
        }

        loadStatus.setState(ProcessState.PROCESSING);
        loadStatus.setStartTime(new Date(System.currentTimeMillis()));
        messageDirector = new MessageDirector(getName(), loadStatus);

        // mcturk test 6/3/2009
//        String id2 = getLogger().error("GEMBaseLoader: baseLoad: testing early exit.");
        //throw new LBInvocationException("GEMBaseLoader: baseLoad: testing early exit.", id2);

//  commented out for early testing - mcturk 6/3/2009        
        if (async) {
            Thread conversion = new Thread(new DoConversion(sci));
            conversion.start();
        } else {
            new DoConversion(sci).run();
        }
    }

    /**
     * Reindexes the specified registered code system - otherwise, if no code
     * system is specified, reindexes all registered code systems.
     * 
     * @param codingSchemeVersion
     * @throws LBInvocationException
     * @throws LBParameterException
     */
    protected void reindexCodeSystem(AbsoluteCodingSchemeVersionReference codingSchemeVersion, boolean async)
            throws LBInvocationException, LBParameterException {
        setInUse();
        ArrayList<AbsoluteCodingSchemeVersionReference> temp = new ArrayList<AbsoluteCodingSchemeVersionReference>();

        if (codingSchemeVersion == null) {
            // They want me to reindex all of the code systems...
            LexBIGService lbs = LexBIGServiceImpl.defaultInstance();
            CodingSchemeRendering[] csr = lbs.getSupportedCodingSchemes().getCodingSchemeRendering();
            for (int i = 0; i < csr.length; i++) {
                temp.add(Constructors.createAbsoluteCodingSchemeVersionReference(csr[i].getCodingSchemeSummary()));
            }

        } else if (codingSchemeVersion.getCodingSchemeURN() == null
                || codingSchemeVersion.getCodingSchemeURN().length() == 0
                || codingSchemeVersion.getCodingSchemeVersion() == null
                || codingSchemeVersion.getCodingSchemeVersion().length() == 0) {
            inUse = false;
            throw new LBParameterException(
                    "If you supply a codingSchemeVersion, it needs to contain both the coding scheme and the version");
        } else {
            SQLConnectionInfo sci = ResourceManager.instance().getRegistry().getSQLConnectionInfoForCodeSystem(
                    codingSchemeVersion);
            if (sci == null) {
                inUse = false;
                String id = getLogger().error("Couldn't map urn / version to internal name");
                throw new LBInvocationException("There was an unexpected internal error", id);
            }

            temp.add(codingSchemeVersion);
        }

        loadStatus = new LoadStatus();
        loadStatus.setLoadSource(null); // doesn't apply
        loadStatus.setStartTime(new Date(System.currentTimeMillis()));
        messageDirector = new MessageDirector(getName(), loadStatus);

        if (async) {
            Thread reIndex = new Thread(new ReIndex(temp));
            reIndex.start();
        } else {
            new ReIndex(temp).run();
        }

    }

    /*
     * A loader class can only safely do one thing at a time.
     */
    protected void setInUse() throws LBInvocationException {
        if (inUse) {
            throw new LBInvocationException(
                    "This loader is already in use.  Construct a new loader to do two operations at the same time", "");
        }
        inUse = true;
    }

    private class DoConversion implements Runnable {
        private SQLConnectionInfo sci_;

        public DoConversion(SQLConnectionInfo sci) {
            sci_ = sci;
        }

        @SuppressWarnings("null")
        public void run() {
            SystemVariables sv = ResourceManager.instance().getSystemVariables();
            URNVersionPair[] locks = null;
            URNVersionPair[] loadedCodingSchemes = null;
            try {
                // Construct the loaders in order to check the versions.
                CodingScheme codingScheme = GEMConversionLauncher.startConversion(inputFormatInterface, lexGridSqlOut, null, optionHolder,
                        messageDirector, _props);

                if (loadStatus.getErrorsLogged() != null && !loadStatus.getErrorsLogged().booleanValue()) {
                    // make sure this exact terminology isn't already loaded -
                    // also, get a lock
                    // so no one else can start loading.
                	loadedCodingSchemes = new URNVersionPair[1];
                	loadedCodingSchemes[0] = new URNVersionPair(codingScheme.getCsId(), codingScheme.getRepresentsVersion());

                    locks = isTerminologyAlreadyLoaded(loadedCodingSchemes, sci_, false);
                    if (locks == null) {
                        // delete the DB we just created - end in error.
                        messageDirector.fatal("Cannot load a terminology that is already loaded.");
                        loadStatus.setState(ProcessState.FAILED);
                    }
                }

                // Actually do the load
//                if (loadStatus.getErrorsLogged() != null && !loadStatus.getErrorsLogged().booleanValue()) {
//                    loadedCodingSchemes = ExConversionLauncher.finishConversion(inputFormatInterface, lexGridSqlOut, loadedCodingSchemes, optionHolder,
//                            messageDirector);
//                }

//                String[] codingSchemeNames = new String[loadedCodingSchemes.length];
//                for (int i = 0; i < loadedCodingSchemes.length; i++) {
//                  codingSchemeNames[i] = loadedCodingSchemes[i].getUrn();
//                	codingSchemeNames[i] = loadedCodingSchemes[i].
//                }
                
                String[] codingSchemeNames = {codingScheme.getCsName()};

                // Recheck the version
                if (loadStatus.getErrorsLogged() != null && !loadStatus.getErrorsLogged().booleanValue()) {
                    locks = isTerminologyAlreadyLoaded(loadedCodingSchemes, sci_, true);
                    if (locks == null) {
                        // delete the DB we just created - end in error.
                        messageDirector.fatal("Cannot load a terminology that is already loaded.");
                        loadStatus.setState(ProcessState.FAILED);
                    }
                }

                if (loadStatus.getErrorsLogged() != null && !loadStatus.getErrorsLogged().booleanValue()) {

                    messageDirector.info("Finished loading the DB");
                    Snapshot snap = SimpleMemUsageReporter.snapshot();
                    messageDirector.info("Read Time : " + SimpleMemUsageReporter.formatTimeDiff(snap.getTimeDelta(null))
                            + " Heap Usage: " + SimpleMemUsageReporter.formatMemStat(snap.getHeapUsage())
                            + " Heap Delta:" + SimpleMemUsageReporter.formatMemStat(snap.getHeapUsageDelta(null)));

                    doTransitiveAndIndex(codingSchemeNames, sci_);
                    messageDirector.info("After Indexing");
                    snap = SimpleMemUsageReporter.snapshot();
                    messageDirector.info("Read Time : " + SimpleMemUsageReporter.formatTimeDiff(snap.getTimeDelta(null))
                            + " Heap Usage: " + SimpleMemUsageReporter.formatMemStat(snap.getHeapUsage())
                            + " Heap Delta:" + SimpleMemUsageReporter.formatMemStat(snap.getHeapUsageDelta(null)));

                    if (sv.getAutoLoadSingleDBMode()) {
                        // some databases (access in particular) won't see new
                        // tables unless you open a new connection to them.
                        // if we are in single db mode, there may already be
                        // open connections. close them.
                        SQLInterfaceBase sib = ResourceManager.instance().getSQLInterfaceBase(sci_.username,
                                sci_.password, sci_.server, sci_.driver);
                        sib.closeUnusedConnections();
                    }

                    register(sci_);

                    loadStatus.setState(ProcessState.COMPLETED);
                    messageDirector.info("Load process completed without error");
                }

            } catch (Exception e) {
                loadStatus.setState(ProcessState.FAILED);
                messageDirector.fatal("Failed while running the conversion", e);
            } finally {
                if (loadStatus.getState() == null || loadStatus.getState().getType() != ProcessState.COMPLETED_TYPE) {
                    loadStatus.setState(ProcessState.FAILED);
                    try {
                        getLogger().warn("Load failed.  Removing temporary resources...");
                        CleanUpUtility.removeUnusedDatabase(sv.getAutoLoadSingleDBMode() ? lexGridSqlOut.getTablePrefix()
                                : sci_.dbName);
                        CleanUpUtility.removeUnusedIndex(sv.getAutoLoadSingleDBMode() ? lexGridSqlOut.getTablePrefix()
                                : sci_.dbName);
                    } catch (LBParameterException e) {
                        // do nothing - means that the requested delete item
                        // didn't exist.
                    } catch (Exception e) {
                        getLogger().warn("Problem removing temporary resources", e);
                    }
                }
                if (locks != null) {
                    for (int i = 0; i < locks.length; i++) {
                        try {
							unlock(locks[i]);
						} catch (LBInvocationException e) {
	                        getLogger().warn("Problem releasing lock: ", e);
						} catch (LBParameterException e) {
							getLogger().warn("Problem releasing lock: ", e);
						}
                    }
                }
                loadStatus.setEndTime(new Date(System.currentTimeMillis()));
                inUse = false;
            }

        }
    }

    protected void doTransitiveAndIndex(String[] codingSchemes, SQLConnectionInfo sci) throws Exception {
        doTransitiveTable(codingSchemes, sci);
        doIndex(codingSchemes, sci);
    }

    /**
     * Build root (or tail) nodes.
     * 
     * @param codingSchemes
     * @param associations
     * @param sci
     * @param root
     *            - true for root nodes, false for tail nodes.
     * @throws Exception
     */
    protected void buildRootNode(String[] codingSchemes, String[] associations, SQLConnectionInfo sci, boolean root)
            throws Exception {
        messageDirector.info("Building the root node");
        Connection c = null;
        try {
            c = DBUtility.connectToDatabase(sci.server, sci.driver, sci.username, sci.password);
            SQLTableUtilities stu = new SQLTableUtilities(c, sci.prefix);
            for (int i = 0; i < codingSchemes.length; i++) {
                stu.addRootRelationNode(codingSchemes[i], associations, stu.getNativeRelation(codingSchemes[i]), root);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        messageDirector.info("Finished building root node");
    }

    protected void doIndex(String[] codingSchemes, SQLConnectionInfo sci) throws Exception {
        Snapshot snap1 = SimpleMemUsageReporter.snapshot();
        messageDirector.info("Building the index");

        buildIndex(codingSchemes, sci, messageDirector);
        Snapshot snap2 = SimpleMemUsageReporter.snapshot();
        messageDirector.info("Finished indexing.   Time to index: "
                + SimpleMemUsageReporter.formatTimeDiff(snap2.getTimeDelta(snap1)) + "   Heap usage: "
                + SimpleMemUsageReporter.formatMemStat(snap2.getHeapUsage()) + "   Heap delta: "
                + SimpleMemUsageReporter.formatMemStat(snap2.getHeapUsageDelta(snap1)));

    }

    protected void doTransitiveTable(String[] codingSchemes, SQLConnectionInfo sci) throws Exception {
        Snapshot snap1 = SimpleMemUsageReporter.snapshot();
        messageDirector.info("Loading transitive expansion table");
        Connection c = null;
        try {
            c = DBUtility.connectToDatabase(sci.server, sci.driver, sci.username, sci.password);
            SQLTableUtilities stu = new SQLTableUtilities(c, sci.prefix);
            for (int i = 0; i < codingSchemes.length; i++) {
                stu.computeTransitivityTable(codingSchemes[i], messageDirector);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        Snapshot snap2 = SimpleMemUsageReporter.snapshot();
        messageDirector.info("Finished building transitive expansion.   Time taken: "
                + SimpleMemUsageReporter.formatTimeDiff(snap2.getTimeDelta(snap1)) + "   Heap usage: "
                + SimpleMemUsageReporter.formatMemStat(snap2.getHeapUsage()) + "   Heap delta: "
                + SimpleMemUsageReporter.formatMemStat(snap2.getHeapUsageDelta(snap1)));

    }

    protected void register(SQLConnectionInfo sci) throws Exception {
        ResourceManager rm = ResourceManager.instance();
        // put this into the resource managers hash tables..
        acsvr = ResourceManager.instance().readTerminologiesFromServer(sci);

        rm.rereadAutoLoadIndexes();

        // put into the registry file
        for (int i = 0; i < acsvr.length; i++) {
            String urn = acsvr[i].getCodingSchemeURN();

            rm.getRegistry().addNewItem(urn, acsvr[i].getCodingSchemeVersion(), "inactive", sci.server, null,
                    sci.dbName, sci.prefix);
            WriteLockManager.instance().releaseLock(acsvr[i].getCodingSchemeURN(), acsvr[i].getCodingSchemeVersion());
        }
        messageDirector.info("Updated the registry.");
    }

    private class ReIndex implements Runnable {
        ArrayList<AbsoluteCodingSchemeVersionReference> codingSchemeVersion_;

        public ReIndex(ArrayList<AbsoluteCodingSchemeVersionReference> codingSchemeVersion) {
            codingSchemeVersion_ = codingSchemeVersion;
        }

        public void run() {
            loadStatus.setState(ProcessState.PROCESSING);
            for (int i = 0; i < codingSchemeVersion_.size(); i++) {
                boolean success = false;

                CodingSchemeVersionStatus status = null;
                try {
                    SQLConnectionInfo sci = ResourceManager.instance().getRegistry().getSQLConnectionInfoForCodeSystem(
                            codingSchemeVersion_.get(i));
                    String csn = ResourceManager.instance().getInternalCodingSchemeNameForUserCodingSchemeName(sci.urn,
                            sci.version);

                    status = ResourceManager.instance().getRegistry().getStatus(
                            codingSchemeVersion_.get(i).getCodingSchemeURN(),
                            codingSchemeVersion_.get(i).getCodingSchemeVersion());
                    ResourceManager.instance().setPendingStatus(codingSchemeVersion_.get(i));
                    WriteLockManager.instance().acquireLock(codingSchemeVersion_.get(i).getCodingSchemeURN(),
                            codingSchemeVersion_.get(i).getCodingSchemeVersion());

                    messageDirector.info("beginning index process for " + csn);
                    buildIndex(new String[] { csn }, sci, messageDirector);

                    // make sure that searches wont hit on a cache from the old
                    // index.
                    // If the index was missing outright - this can fail with a
                    // missing resource exception. recover
                    try {
                        ResourceManager.instance().getIndexInterface(csn, sci.version).reopenIndex(csn, sci.version);
                    } catch (MissingResourceException e) {
                        ResourceManager.instance().rereadAutoLoadIndexes();
                    }

                    success = true;
                    messageDirector.info("Finished indexing " + csn);

                } catch (Exception e) {
                    loadStatus.setState(ProcessState.FAILED);
                    messageDirector.fatal("Failed while running the conversion", e);
                } finally {
                    try {
                        WriteLockManager.instance().releaseLock(codingSchemeVersion_.get(i).getCodingSchemeURN(),
                                codingSchemeVersion_.get(i).getCodingSchemeVersion());
                        if (status != null) {
                            // restore the state if we had a successful reindex.
                            if (status.getType() == CodingSchemeVersionStatus.ACTIVE_TYPE && success) {
                                ResourceManager.instance().getRegistry().activate(codingSchemeVersion_.get(i));
                            } else if (status.getType() == CodingSchemeVersionStatus.INACTIVE_TYPE && success) {
                                ResourceManager.instance().deactivate(codingSchemeVersion_.get(i), null);
                            }
                        }
                    } catch (Exception e) {
                        messageDirector.fatal("Failed while running the conversion", e);
                    }
                    if (!success) {
                        loadStatus.setState(ProcessState.FAILED);
                    }

                }
            }
            if (loadStatus.getState().getType() == ProcessState.PROCESSING_TYPE) {
                loadStatus.setState(ProcessState.COMPLETED);
            }
            loadStatus.setEndTime(new Date(System.currentTimeMillis()));
            inUse = false;
        }
    }

    /**
     * @param finalCheck
     *            - if this is set to true, a failure to lookup the version of
     *            the new coding scheme will return an exception, if false an
     *            empty URNVersionPair[] is returned so processing can continue
     *            until the finalCheck since we are unable to determine whether
     *            the scheme has been loaded.
     * 
     *            returns null if already loaded. Otherwise, returns information
     *            that will be needed to remove the lock that this method
     *            acquires.
     */
    protected URNVersionPair[] isTerminologyAlreadyLoaded(URNVersionPair[] codingSchemes, SQLConnectionInfo sci,
            boolean finalCheck) throws InternalException {
        PreparedStatement getCodingSchemeVersion = null;
        Connection connection = null;

        try {
            connection = DBUtility.connectToDatabase(sci.server, sci.driver, sci.username, sci.password);
            SQLTableConstants stc = new SQLTableUtilities(connection, sci.prefix).getSQLTableConstants();

            getCodingSchemeVersion = connection.prepareStatement("Select " + SQLTableConstants.TBLCOL_REPRESENTSVERSION
                    + ", " + SQLTableConstants.TBLCOL_CODINGSCHEMEURI + " from " + stc.getTableName(SQLTableConstants.CODING_SCHEME)
                    + " Where " + stc.registeredNameOrCSURI + " = ?"); // mctfix?

            for (int i = 0; i < codingSchemes.length; i++) {
                String version = codingSchemes[i].getVersion();
                String urn = "";
                boolean alreadyLoaded = true;
                if (version != null) {
                    version = codingSchemes[i].getVersion();
                    urn = codingSchemes[i].getUrn();
                } else {
                    getCodingSchemeVersion.setString(1, codingSchemes[i].getUrn());
                    ResultSet results = getCodingSchemeVersion.executeQuery();

                    if (results.next()) {
                        version = results.getString(SQLTableConstants.TBLCOL_REPRESENTSVERSION);
                        urn = results.getString(stc.registeredNameOrCSURI);
                        //urn = results.getString(SQLTableConstants.TBLCOL_CODINGSCHEMEURI); // mctfix?
                    } else {
                        // If this isn't the final check, the database may not
                        // have been created yet
                        throw new InternalException(
                                "There was a problem checking if the terminology was already loaded - no results from query.");
                    }
                }

                WriteLockManager.instance().acquireLock(urn, version);
                try {
                    // make sure that no other VM has registered this code
                    // system (since the last automatic update)
                    WriteLockManager.instance().checkForRegistryUpdates();
                    ResourceManager.instance().getInternalCodingSchemeNameForUserCodingSchemeName(
                            codingSchemes[i].getUrn(), version);
                    // the expected path here is to throw an exception - so this
                    // line won't
                    // be executed unless it is already loaded.
                    WriteLockManager.instance().releaseLock(urn, version);
                } catch (LBParameterException e) {
                    // this is a good thing, means that is hasn't been loaded.
                    alreadyLoaded = false;
                    codingSchemes[i] = new URNVersionPair(urn, version);
                    // Get a lock on this code system, so no one else can try to
                    // load it.

                }

                if (alreadyLoaded) {
                    return null;
                }

            }

            // if I finish checking without finding one that was already loaded
            // - then return false
            return codingSchemes;
        } catch (LBInvocationException e) {
            // this means that another VM has a lock, and is already loading
            // this terminiology.
            return null;
        }

        catch (Exception e) {
            if (finalCheck)
                throw new InternalException("There was a problem checking if the terminology was already loaded.", e);
            else
                return new URNVersionPair[0];

        } finally {
            try {
                if (getCodingSchemeVersion != null) {
                    getCodingSchemeVersion.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // do nothing;
            }
        }

    }

    public LoadStatus getStatus() {
        return loadStatus;
    }

    public LogEntry[] getLog(LogLevel level) {
        if (messageDirector == null) {
            return new LogEntry[] {};
        }
        return messageDirector.getLogEntries(level);
    }

    public void clearLog() {
        if (messageDirector != null) {
            messageDirector.clearMessages();
        }
    }

    public AbsoluteCodingSchemeVersionReference[] getCodingSchemeReferences() {
        return acsvr;
    }

    public String getName() {
        return loaderName;
    }
    
    public void setName(String name) {
    	loaderName = name;
    }

    public String getDescription() {
        return loaderDescription;
    }
    
    public void setDescription(String description) {
    	loaderDescription = description;
    }

    public String getVersion() {
        return loaderVersion;
    }

    public String getProvider() {
        return providerName;
    }

    protected void lock(URNVersionPair lockInfo) throws LBInvocationException, LBParameterException {
        if (lockInfo != null && lockInfo.getUrn() != null && lockInfo.getVersion() != null) {
            WriteLockManager.instance().acquireLock(lockInfo.getUrn(), lockInfo.getVersion());
            WriteLockManager.instance().checkForRegistryUpdates();
        }
    }

    protected void unlock(URNVersionPair lockInfo) throws LBInvocationException, LBParameterException {
        if (lockInfo != null && lockInfo.getUrn() != null && lockInfo.getVersion() != null) {
            WriteLockManager.instance().releaseLock(lockInfo.getUrn(), lockInfo.getVersion());
        }
    }

    private void buildIndex(String[] codingSchemes, SQLConnectionInfo sci, MessageDirector md) throws Exception {
        SystemVariables sv = ResourceManager.instance().getSystemVariables();
        if (sci.server.indexOf("jdbc:mysql") != -1) {
            // mysql gets results in stages, has to rerun the query multiple
            // times.
            // use a much larger batch size.
            Constants.mySqlBatchSize = 50000;
        } else {
            Constants.mySqlBatchSize = 10000;
        }
        new SQLIndexer((sv.getAutoLoadSingleDBMode() ? sci.prefix : sci.dbName), sv.getAutoLoadIndexLocation(),
                sci.username, sci.password, sci.server, sci.driver, sci.prefix, codingSchemes, md, sv.isNormEnabled(),
                true, true, true);
    }

    public String getStringFromURI(URI uri) throws LBParameterException {
        if ("file".equals(uri.getScheme()))

        {
            File temp = new File(uri);
            return temp.getAbsolutePath();
        } else {
            inUse = false;
            throw new LBParameterException("Currently only supports file based URI's", "uri", uri.toString());
        }

    }

    /**
     * Set the CodingSchemeManifest that would be used to modify the ontology
     * content. Once the ontology is loaded from the source, the manifest would
     * then be applied to modify the loaded content.
     * 
     * @param csm
     */
    public void setCodingSchemeManifest(CodingSchemeManifest codingSchemeManifest) {
        this.codingSchemeManifest = codingSchemeManifest;
    }

    /**
     * Get the CodingSchemeManifest that would be used to modify the ontology
     * content. Once the ontology is loaded from the source, the manifest would
     * then be applied to modify the loaded content.
     * 
     * @param csm
     */
    public CodingSchemeManifest getCodingSchemeManifest() {
        return codingSchemeManifest;
    }

    /**
     * Set the URI of the codingSchemeManifest that would be used to modify the
     * ontology content. The codingSchemeManifest object is set using the
     * content pointed by the URI. Once the ontology is loaded from the source,
     * the manifest would then be applied to modify the loaded content.
     * 
     * @param csm
     */
    public void setCodingSchemeManifestURI(URI codingSchemeManifestURI) {
        this.codingSchemeManifestURI = codingSchemeManifestURI;
        ManifestUtil util = new ManifestUtil(codingSchemeManifestURI, messageDirector);

        codingSchemeManifest = util.getManifest();

    }

    /**
     * Get the URI of the codingSchemeManifest that would be used to modify the
     * ontology content. Once the ontology is loaded from the source, the
     * manifest would then be applied to modify the loaded content.
     * 
     * @param csm
     */
    public URI getCodingSchemeManifestURI() {
        return codingSchemeManifestURI;
    }

    /**
     * Returns the current LoaderPreferences object.
     * 
     * @return The current LoaderPreferences
     */
    public LoaderPreferences getLoaderPreferences() {
        return loaderPreferences;
    }

    /**
     * Sets the Loader's LoaderPreferences.
     * 
     * @param loaderPreferences
     *            The LoaderPreference object to be loaded. It is recommended
     *            that all subclasses override and check if the
     *            LoaderPreferences object is valid for the particular loader.
     * @throws LBParameterException
     */
    public void setLoaderPreferences(LoaderPreferences loaderPreferences) throws LBParameterException {
        this.loaderPreferences = loaderPreferences;
    }

    /**
     * Sets the Loader's LoaderPreferences.
     * 
     * @param loaderPreferences
     *            The LoaderPreference XML URI to be loaded.
     * @throws LBParameterException
     */
    public void setLoaderPreferences(URI loaderPreferencesUri) throws LBParameterException {
        try {
            PreferenceLoader prefLoader = PreferenceLoaderFactory.createPreferenceLoader(loaderPreferencesUri);
            if (!prefLoader.validate()) {
                throw new LBParameterException("Error",
                        "Preferences File is not a valid LoaderPreference file for the Source Format you are trying to load.");
            } else {
                this.loaderPreferences = prefLoader.load();
            }
        } catch (LgConvertException e) {
            throw new LBParameterException("Could not create Preference Loader from the URI specified.");
        }

    }

}