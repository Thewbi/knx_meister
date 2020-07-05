package project.parsing.knx;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import api.project.KNXGroupAddress;
import api.project.KNXProject;

public class KNXProjectParsingContext {

	/** the file the project was loaded from */
	private File knxProjectFile;

	/** the temporary directory where the project was extracted into */
	private Path tempDirectory;

	/** the project */
	private KNXProject knxProject;

	/** the root node of the tree of KNXGroupAddresses */
	private KNXGroupAddress knxGroupAddress;

	private final Map<String, String> formatRefMap = new HashMap<>();

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

	public Map<String, String> getFormatRefMap() {
		return formatRefMap;
	}

}
