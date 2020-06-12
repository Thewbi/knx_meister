package project.parsing.knx.steps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import project.parsing.domain.KNXProject;
import project.parsing.knx.KNXGroupAddressStyle;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class ReadProjectParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final String GROUP_ADDRESS_STYLE_ATTRIBUTE = "GroupAddressStyle";
	private static final String NAME_ATTRIBUTE = "Name";
	private static final String ID_ATTRIBUTE = "Id";
	private static final Logger LOG = LogManager.getLogger(ReadProjectParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		// find project.xml
		Files.walk(context.getTempDirectory()).filter(Files::isRegularFile)
				.filter(p -> p.getFileName().endsWith("project.xml")).findFirst().ifPresent(new Consumer<Path>() {

					@Override
					public void accept(final Path path) {

						try {

							final KNXProject knxProject = new KNXProject();
							context.setKnxProject(knxProject);
							processXML(path, knxProject);

						} catch (ParserConfigurationException | SAXException | IOException e) {
							LOG.error(e.getMessage(), e);
							context.setKnxProject(null);
						}
					}

					private void processXML(final Path path, final KNXProject knxProject)
							throws ParserConfigurationException, SAXException, IOException {

						final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
						final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
						final Document document = documentBuilder.parse(path.toFile());

						final Element projectElement = retrieveProjectElement(document);
						knxProject.setId(projectElement.getAttribute(ID_ATTRIBUTE));

						final Element projectInformationElement = retrieveProjectInformationElement(projectElement);
						knxProject.setName(projectInformationElement.getAttribute(NAME_ATTRIBUTE));

						final String attribute = projectInformationElement.getAttribute(GROUP_ADDRESS_STYLE_ATTRIBUTE);
						knxProject.setGroupAddressStyle(KNXGroupAddressStyle.fromString(attribute));
					}

					private Element retrieveProjectElement(final Document document) {

						final NodeList elementsByTagName = document.getElementsByTagName("Project");
						final Node projectNode = elementsByTagName.item(0);
						final Element projectElement = (Element) projectNode;

						return projectElement;
					}

					private Element retrieveProjectInformationElement(final Element projectElement) {
						return (Element) projectElement.getChildNodes().item(1);
					}

				});
	}

}
