package project.parsing.knx.steps;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import project.parsing.domain.KNXComObject;
import project.parsing.domain.KNXDeviceInstance;
import project.parsing.domain.KNXGroupAddress;
import project.parsing.domain.KNXProject;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class ReadProjectInstallationsParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(ReadProjectInstallationsParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		final Path tempDirectory = context.getTempDirectory();
		final KNXProject knxProject = context.getKnxProject();

		final Path path = tempDirectory.resolve(knxProject.getId()).resolve("0.xml");

		try {
			final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			final Document document = documentBuilder.parse(path.toFile());

			// device instance and COM objects within the device instance
			final NodeList deviceInstanceNodeList = document.getElementsByTagName("DeviceInstance");
			for (int i = 0; i < deviceInstanceNodeList.getLength(); i++) {

				final Element deviceInstanceElement = (Element) deviceInstanceNodeList.item(i);
				final KNXDeviceInstance deviceInstance = convertKNXDeviceInstance(deviceInstanceElement, knxProject);
				knxProject.getDeviceInstances().add(deviceInstance);
			}

			// group addresses
			final NodeList groupRangesNodeList = document.getElementsByTagName("GroupRanges");
			final Element groupRangesElement = (Element) groupRangesNodeList.item(0);

//			int groupAddressLevels = 0;
//			switch (knxProject.getGroupAddressStyle()) {
//			case TWOLEVEL:
//				groupAddressLevels = 2;
//				break;
//			case THREELEVEL:
//				groupAddressLevels = 3;
//				break;
//			default:
//				throw new RuntimeException(
//						"KNXGroupAddressStyle " + knxProject.getGroupAddressStyle() + " not supported!");
//			}
//
//			final List<String> addressComponents = new ArrayList<>();
//			final List<KNXGroupAddress> knxGroupAddresses = new ArrayList<>();

			final KNXGroupAddress knxGroupAddress = new KNXGroupAddress();

			for (int i = 0; i < groupRangesElement.getChildNodes().getLength(); i++) {
				final Node item = groupRangesElement.getChildNodes().item(i);
				if (!(item instanceof Element)) {
					continue;
				}
				recurseGroupAddresses((Element) item, knxGroupAddress);
			}
			knxGroupAddress.sortChildren();
			knxGroupAddress.assignAddresses();

			knxGroupAddress.dump();

		} catch (final ParserConfigurationException | SAXException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private void recurseGroupAddresses(final Element element, final KNXGroupAddress knxGroupAddress) {

		if (element.getChildNodes().getLength() == 0) {

			final KNXGroupAddress childKNXGroupAddress = new KNXGroupAddress();
			childKNXGroupAddress.setId(element.getAttribute("Id"));
			childKNXGroupAddress.setName(element.getAttribute("Name"));
			childKNXGroupAddress.setAddress(Integer.parseInt(element.getAttribute("Address")));
			childKNXGroupAddress.setRangeStart(0);
			childKNXGroupAddress.setDataPointType(element.getAttribute("DatapointType"));

			knxGroupAddress.getKNXGroupAddresses().add(childKNXGroupAddress);
			childKNXGroupAddress.setParentKNXGroupAddress(knxGroupAddress);
			return;
		}

		final KNXGroupAddress childKNXGroupAddress = new KNXGroupAddress();
		childKNXGroupAddress.setId(element.getAttribute("Id"));
		childKNXGroupAddress.setName(element.getAttribute("Name"));
		childKNXGroupAddress.setAddress(0);
		childKNXGroupAddress.setRangeStart(Integer.parseInt(element.getAttribute("RangeStart")));

		knxGroupAddress.getKNXGroupAddresses().add(childKNXGroupAddress);
		childKNXGroupAddress.setParentKNXGroupAddress(knxGroupAddress);

		for (int i = 0; i < element.getChildNodes().getLength(); i++) {

			final Node node = element.getChildNodes().item(i);

			if (!(node instanceof Element)) {
				continue;
			}

			final Element childElement = (Element) node;

			recurseGroupAddresses(childElement, childKNXGroupAddress);
		}
	}

	private KNXDeviceInstance convertKNXDeviceInstance(final Element deviceInstanceElement,
			final KNXProject knxProject) {

		final String address = retrieveAddress(deviceInstanceElement, knxProject);
		final List<KNXComObject> comObjects = retrieveCOMObjects(deviceInstanceElement);

		final KNXDeviceInstance knxDeviceInstance = new KNXDeviceInstance();
		knxDeviceInstance.setAddress(address);
		knxDeviceInstance.getComObjects().addAll(comObjects);

		return knxDeviceInstance;
	}

	private List<KNXComObject> retrieveCOMObjects(final Element deviceInstanceElement) {

		// group object tree
		final Element groupObjectTreeElement = (Element) deviceInstanceElement.getElementsByTagName("GroupObjectTree")
				.item(0);
		final String groupObjectInstances = groupObjectTreeElement.getAttribute("GroupObjectInstances");
		final String[] split = groupObjectInstances.split(" ");
		final Set<String> groupObjectInstancesSet = new HashSet<>(Arrays.asList(split));

		// COM objects
		final List<KNXComObject> comObjectList = new ArrayList<>();

		final NodeList comObjectInstanceRefsNodeList = deviceInstanceElement
				.getElementsByTagName("ComObjectInstanceRefs");
		final Element objectInstanceRefs = (Element) comObjectInstanceRefsNodeList.item(0);
		if (objectInstanceRefs != null) {
			for (int i = 0; i < objectInstanceRefs.getChildNodes().getLength(); i++) {

				final Node item = objectInstanceRefs.getChildNodes().item(i);

				if (item instanceof Element) {

					final KNXComObject convertCOMObject = convertCOMObject(item);

					// if the devices id is part of the GroupObjectTree, the COM object is flagged
					// as a group object and can be displayed to the user using the flag
					if (groupObjectInstancesSet.contains(convertCOMObject.getId())) {
						convertCOMObject.setGroupObject(true);
					}
					comObjectList.add(convertCOMObject);
				}
			}
		}

		return comObjectList;
	}

	private KNXComObject convertCOMObject(final Node node) {

		final Element element = (Element) node;

		final KNXComObject comObject = new KNXComObject();
		comObject.setId(element.getAttribute("RefId"));
		comObject.setText(element.getAttribute("Text"));
		if (element.hasAttribute("Links")) {
			comObject.setGroupAddressLink(element.getAttribute("Links"));
		}

		return comObject;
	}

	private String retrieveAddress(final Element deviceInstanceElement, final KNXProject knxProject) {

		final int addressLevels = 3;

//		int groupAddressLevels = 0;
//		switch (knxProject.getGroupAddressStyle()) {
//		case TWOLEVEL:
//			groupAddressLevels = 2;
//			break;
//		case THREELEVEL:
//			groupAddressLevels = 3;
//			break;
//		default:
//			throw new RuntimeException("KNXGroupAddressStyle " + knxProject.getGroupAddressStyle() + " not supported!");
//		}

		final List<Element> parentElements = new ArrayList<>();
		Element currentElement = deviceInstanceElement;
		for (int i = 0; i < addressLevels; i++) {
			parentElements.add(0, currentElement);
			currentElement = (Element) currentElement.getParentNode();
		}
		final StringBuffer stringBuffer = new StringBuffer();
		for (final Element element : parentElements) {
			if (stringBuffer.length() != 0) {
				stringBuffer.append(".");
			}
			stringBuffer.append(element.getAttribute("Address"));
		}
		return stringBuffer.toString();
	}

}
