package edu.drexel.aig.formatregindex;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.indexing.wrapper.IndexerWrapper;
import org.irods.jargon.indexing.wrapper.event.FileEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileIdentificationIndexer extends IndexerWrapper {
    private static final Logger log = Logger.getLogger(FileIdentificationIndexer.class.getName());

    public static final String FORMAT_REG_AVU_ATTRIB = "http://www.nationaldesignrepository.org/ontologies/formatregistry.owl#hasFormat";
    public static final String FORMAT_REG_AVU_UNIT = "http://www.w3.org/2001/XMLSchema#anyURI";

    private static final String PROPERTIES_RESOURCE_PATH = "/config.properties";

    private final IRODSFileSystem fileSystem = IRODSFileSystem.instance();
    private final IRODSAccessObjectFactory accessFactory = fileSystem.getIRODSAccessObjectFactory();
    private final IRODSAccount account;
    private final FormatRegistryClient client = new FormatRegistryClient();
    private final String collectionPath;

    public FileIdentificationIndexer() throws JargonException, IOException {
        final Properties props = new Properties();
        props.load(getClass().getResourceAsStream(PROPERTIES_RESOURCE_PATH));

        account = new IRODSAccount(
                props.getProperty("irods.host", "localhost"),
                Integer.parseInt(props.getProperty("irods.port", "1247")),
                props.getProperty("irods.user", "rods"),
                props.getProperty("irods.password"),
              // leave out for now mcc  props.getProperty("irods.home"),
		"",
                props.getProperty("irods.zone", "tempZone"),
                props.getProperty("irods.resc", "demoResc")
        );

        final String collectionProp = props.getProperty("collection.path", "/");
        if(collectionProp.endsWith("/")) {
            collectionPath = collectionProp;
        } else {
            collectionPath = collectionProp + "/";
        }

        accessFactory.authenticateIRODSAccount(account);
    }

    @Override
    protected void onStartup() {
        log.info(">>>>>>>>> format registry indexer startup");
    }

    @Override
    protected void onShutdown() {
        log.info("<<<<<<<<<< format registry indexer shutdown");
    }

    @Override
    protected void onFileAdd(FileEvent fileEvent) {
        log.info(">>>>> File Add");
        log.info("fileEvent: " + fileEvent);
        final String path = fileEvent.getIrodsAbsolutePath();
        if (! path.startsWith(collectionPath)) {
            return;
        }

        try {
            final InputStream in = accessFactory.getIRODSFileFactory(account).instanceIRODSFileInputStream(path);
            final List<String> matches = client.results(new File(path).getName(), in);
            if (matches.size() >= 1) {
                final String formatType = matches.get(0);
		log.info("match!!!:" +  formatType);
		log.info("adding AVU for format");
                accessFactory.getDataObjectAO(account).addAVUMetadata(path, new AvuData(FORMAT_REG_AVU_ATTRIB, formatType, FORMAT_REG_AVU_UNIT));
            }
        } catch (JargonException ex) {
            log.log(Level.SEVERE, "Unable to open stream to irods object " + path, ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Unable to open stream to irods object " + path, ex);
        } finally {
		accessFactory.closeSessionAndEatExceptions();
	}	
    }
}
