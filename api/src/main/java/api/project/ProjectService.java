package api.project;

import java.io.IOException;

import api.exception.ProjectParsingException;

public interface ProjectService {

    void parseProjectFile() throws IOException, ProjectParsingException;

    KNXProject getProject();

}
