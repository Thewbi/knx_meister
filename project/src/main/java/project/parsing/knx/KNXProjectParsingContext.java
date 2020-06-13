package project.parsing.knx;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import project.parsing.domain.KNXDatapointSubtype;
import project.parsing.domain.KNXGroupAddress;
import project.parsing.domain.KNXProject;

public class KNXProjectParsingContext {

	private File knxProjectFile;

	private Path tempDirectory;

	private KNXProject knxProject;

	private KNXGroupAddress knxGroupAddress;

	private final Map<String, Map<String, String>> languageStoreMap = new HashMap<>();

	private final Map<String, KNXDatapointSubtype> datapointSubtypeMap = new HashMap<>();

	public File getKnxProjectFile() {
		return knxProjectFile;
	}

	public void setKnxProjectFile(final File knxProjectFile) {
		this.knxProjectFile = knxProjectFile;
	}

	public Path getTempDirectory() {
		return tempDirectory;
	}

	public void setTempDirectory(final Path tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public KNXProject getKnxProject() {
		return knxProject;
	}

	public void setKnxProject(final KNXProject knxProject) {
		this.knxProject = knxProject;
	}

	public KNXGroupAddress getKnxGroupAddress() {
		return knxGroupAddress;
	}

	public void setKnxGroupAddress(final KNXGroupAddress knxGroupAddress) {
		this.knxGroupAddress = knxGroupAddress;
	}

	public Map<String, Map<String, String>> getLanguageStoreMap() {
		return languageStoreMap;
	}

	public Map<String, KNXDatapointSubtype> getDatapointSubtypeMap() {
		return datapointSubtypeMap;
	}

}
