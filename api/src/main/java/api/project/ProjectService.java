package api.project;

import java.io.IOException;

import api.exception.ProjectParsingException;

public interface ProjectService {

    KNXProject parseProjectFile() throws IOException, ProjectParsingException;

    KNXProject getProject();

}
