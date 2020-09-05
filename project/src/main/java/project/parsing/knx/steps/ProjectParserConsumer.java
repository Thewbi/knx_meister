package project.parsing.knx.steps;

import java.io.IOException;
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

import api.project.KNXGroupAddressStyle;
import api.project.KNXProject;
import project.parsing.knx.KNXProjectParsingContext;

public class ProjectParserConsumer implements Consumer<Path> {

	private static final String GROUP_ADDRESS_STYLE_ATTRIBUTE = "GroupAddressStyle";

	private static final String NAME_ATTRIBUTE = "Name";

	private static final String ID_ATTRIBUTE = "Id";

	private static final Logger LOG = LogManager.getLogger(ProjectParserConsumer.class);

	private KNXProjectParsingContext context;

	@Override
	public void accept(final Path path) {

		LOG.info("accept()");

		try {

			final KNXProject knxProject = new KNXProject();
			context.setKnxProject(knxProject);

			processXML(path, knxProject);

		} catch (final Exception e) {
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

	public KNXProjectParsingContext getContext() {
		return context;
	}

	public void setContext(final KNXProjectParsingContext context) {
		this.context = context;
	}

}
