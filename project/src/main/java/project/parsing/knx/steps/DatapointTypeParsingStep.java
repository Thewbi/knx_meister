package project.parsing.knx.steps;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import api.exception.ProjectParsingException;
import api.project.KNXDatapointSubtype;
import api.project.KNXDatapointType;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class DatapointTypeParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(DatapointTypeParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException, ProjectParsingException {

		final Path tempDirectory = context.getTempDirectory();
		final Path path = tempDirectory.resolve("knx_master.xml");

		try {

			final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			final Document document = documentBuilder.parse(path.toFile());

			processLanguages(context, document);
			processDatapoints(context, document);

			LOG.info("done");

		} catch (final ParserConfigurationException | SAXException e) {
			LOG.error(e.getMessage(), e);
			throw new ProjectParsingException(e);
		}
	}

	private void processDatapoints(final KNXProjectParsingContext context, final Document document) {

		final NodeList datapointTypesNodeList = document.getElementsByTagName("DatapointTypes");
		final Element datapointTypesElement = (Element) datapointTypesNodeList.item(0);

		for (int i = 0; i < datapointTypesElement.getChildNodes().getLength(); i++) {

			final Node datapointTypeNode = datapointTypesElement.getChildNodes().item(i);
			if (!(datapointTypeNode instanceof Element)) {
				continue;
			}

			final Element datapointTypeElement = (Element) datapointTypeNode;
			String id = datapointTypeElement.getAttribute("Id");
			String text = datapointTypeElement.getAttribute("Text");
			final String name = datapointTypeElement.getAttribute("Name");

			final KNXDatapointType knxDatapointType = new KNXDatapointType();
			knxDatapointType.setId(id);
			knxDatapointType.setText(text);
			knxDatapointType.setName(name);

			final Node datapointSubtypesNode = datapointTypeElement.getChildNodes().item(1);

			for (int j = 0; j < datapointSubtypesNode.getChildNodes().getLength(); j++) {

				final Node datapointSubtypeNode = datapointSubtypesNode.getChildNodes().item(j);
				if (!(datapointSubtypeNode instanceof Element)) {
					continue;
				}

				final Element datapointSubtypeElement = (Element) datapointSubtypeNode;

				id = datapointSubtypeElement.getAttribute("Id");
				text = datapointSubtypeElement.getAttribute("Text");
				final String number = datapointSubtypeElement.getAttribute("Number");
				final String format = retrieveFormat(context, datapointSubtypeElement);

				final KNXDatapointSubtype knxDatapointSubtype = new KNXDatapointSubtype();
				knxDatapointSubtype.setKnxDatapointType(knxDatapointType);
				knxDatapointSubtype.setId(id);
				knxDatapointSubtype.setText(text);
				knxDatapointSubtype.setNumber(number);
				knxDatapointSubtype.setFormat(format);

				context.getKnxProject().getDatapointSubtypeMap().put(id, knxDatapointSubtype);
			}
		}
	}

	private String retrieveFormat(final KNXProjectParsingContext context, final Element datapointSubtypeElement) {

		final Element formatElement = (Element) datapointSubtypeElement.getChildNodes().item(1);
		final Element formatDescriptorElement = (Element) formatElement.getChildNodes().item(1);
		final String width = formatDescriptorElement.getAttribute("Width");

		final String formatNodeName = formatDescriptorElement.getNodeName();

		String result = "";

		if (StringUtils.equalsIgnoreCase(formatNodeName, "RefType")) {

			final String refId = formatDescriptorElement.getAttribute("RefId");

			if (!context.getFormatRefMap().containsKey(refId)) {
				throw new RuntimeException("Unkonwn reference " + refId);
			}

			result = context.getFormatRefMap().get(refId);

		} else {

			final String id = formatDescriptorElement.getAttribute("Id");

			if (StringUtils.isNotBlank(width)) {
				result = formatNodeName + width;
			} else {
				result = formatNodeName;
			}

			context.getFormatRefMap().put(id, result);
		}

		return result;
	}

	private void processLanguages(final KNXProjectParsingContext context, final Document document) {

		// device instance and COM objects within the device instance
		final NodeList deviceInstanceNodeList = document.getElementsByTagName("Languages");
		final Element languagesElement = (Element) deviceInstanceNodeList.item(0);
		for (int i = 0; i < languagesElement.getChildNodes().getLength(); i++) {

			final Node item = languagesElement.getChildNodes().item(i);

			if (!(item instanceof Element)) {
				continue;
			}

			final Element languageElement = (Element) item;

			final String identifierAttribute = languageElement.getAttribute("Identifier");

			final Map<String, String> languageMap = new HashMap<>();
			context.getKnxProject().getLanguageStoreMap().put(identifierAttribute, languageMap);

			for (int j = 0; j < languagesElement.getChildNodes().getLength(); j++) {

				final Node tempNode = languageElement.getChildNodes().item(j);
				if (!(tempNode instanceof Element)) {
					continue;
				}

				final Element translationUnitElement = (Element) tempNode;

				for (int k = 0; k < translationUnitElement.getChildNodes().getLength(); k++) {

					final Node tempNodeA = translationUnitElement.getChildNodes().item(k);
					if (!(tempNodeA instanceof Element)) {
						continue;
					}

					final Element translationElementElement = (Element) tempNodeA;
					final Element translationElement = (Element) translationElementElement.getChildNodes().item(1);

					languageMap.put(translationElementElement.getAttribute("RefId"),
							translationElement.getAttribute("Text"));
				}
			}

		}
	}

}
