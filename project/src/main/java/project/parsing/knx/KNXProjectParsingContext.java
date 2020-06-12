package project.parsing.knx;

import java.io.File;
import java.nio.file.Path;

import project.parsing.domain.KNXProject;

public class KNXProjectParsingContext {

	private File knxProjectFile;

	private Path tempDirectory;

	private KNXProject knxProject;

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

}
