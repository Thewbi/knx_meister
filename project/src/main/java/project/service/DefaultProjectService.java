package project.service;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.configuration.ConfigurationManager;
import api.exception.ProjectParsingException;
import api.project.KNXProject;
import api.project.ProjectService;
import project.parsing.ProjectParser;
import project.parsing.knx.KNXProjectParsingContext;

public class DefaultProjectService implements ProjectService {

    private static final Logger LOG = LogManager.getLogger(DefaultProjectService.class);

    private ConfigurationManager configurationManager;

    private ProjectParser<KNXProjectParsingContext> projectParser;

    private KNXProject knxProject;

    @Override
    public void parseProjectFile() throws IOException, ProjectParsingException {

        final File projectFile = new File(
                configurationManager.getPropertyAsString(ConfigurationManager.PROJECT_FILE_KEY));

        LOG.info("Parsing file: '{}'", projectFile.getAbsoluteFile());
        knxProject = projectParser.parse(projectFile);
    }

    public void setConfigurationManager(final ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void setProjectParser(final ProjectParser<KNXProjectParsingContext> projectParser) {
        this.projectParser = projectParser;
    }

    @Override
    public KNXProject getProject() {
        return knxProject;
    }

}
