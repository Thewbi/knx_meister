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

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import api.exception.ProjectParsingException;
import api.project.KNXComObject;
import api.project.KNXDeviceInstance;
import api.project.KNXGroupAddress;
import api.project.KNXProject;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

/**
 * The file 0.xml contains the elements KNX > Project > Installations >
 * Installation.
 *
 * This step reads
 * <ul>
 * <li />DeviceInstance
 * <li />GroupAddress
 * </ul>
 * XML elements from the Installation element.
 *
 */
public class ReadProjectInstallationsParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final int HEX_RADIX = 16;
	private static final Logger LOG = LogManager.getLogger(ReadProjectInstallationsParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException, ProjectParsingException {

		final Path tempDirectory = context.getTempDirectory();
		final KNXProject knxProject = context.getKnxProject();

		// retrieve the 0.xml file from the project folder
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

			context.setKnxGroupAddress(knxGroupAddress);

		} catch (final ParserConfigurationException | SAXException e) {
			LOG.error(e.getMessage(), e);
			throw new ProjectParsingException(e);
		}
	}

	private void recurseGroupAddresses(final Element element, final KNXGroupAddress knxGroupAddress) {

		// handle a leaf
		if (element.getChildNodes().getLength() == 0) {

			final KNXGroupAddress childKNXGroupAddress = new KNXGroupAddress();
			childKNXGroupAddress.setId(element.getAttribute("Id"));
			childKNXGroupAddress.setName(element.getAttribute("Name"));

			final String address = element.getAttribute("Address");
			final int addressAsInt = StringUtils.isNotBlank(address) ? Integer.parseInt(address) : -1;
			childKNXGroupAddress.setAddress(addressAsInt);
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

		final KNXDeviceInstance knxDeviceInstance = new KNXDeviceInstance();
		knxDeviceInstance.setId(deviceInstanceElement.getAttribute("Id"));

		parseGroupObjectTreeElement(deviceInstanceElement, knxDeviceInstance);

		final String address = retrieveAddress(deviceInstanceElement, knxProject);
		final List<KNXComObject> comObjects = retrieveCOMObjects(knxDeviceInstance, deviceInstanceElement, knxProject);

		// product refid contains the manufacturer code which is needed to find the
		// manufacturer sub folder and the application program xml file within that
		// manufacturer sub folder.
		// From the application program xml file, the ComObjectRef names are parsed
		knxDeviceInstance.setProductRefId(deviceInstanceElement.getAttribute("ProductRefId"));
		knxDeviceInstance.setAddress(address);

		for (final KNXComObject knxComObject : comObjects) {

			LOG.info("PUT_B into knxDeviceInstance " + knxDeviceInstance.getAddress() + " " + knxComObject.getId() + " "
					+ knxComObject.getNumber() + " " + knxComObject.getNumber());
			knxDeviceInstance.getComObjects().put(knxComObject.getId(), knxComObject);
		}

		return knxDeviceInstance;
	}

	private void parseGroupObjectTreeElement(final Element deviceInstanceElement,
			final KNXDeviceInstance knxDeviceInstance) {

		// group object tree (optional element)
		final Element groupObjectTreeElement = (Element) deviceInstanceElement.getElementsByTagName("GroupObjectTree")
				.item(0);

		if (groupObjectTreeElement == null) {
			return;
		}

		final String groupObjectInstances = groupObjectTreeElement.getAttribute("GroupObjectInstances");
		final String[] split = groupObjectInstances.split(" ");
		final Set<String> groupObjectInstancesSet = new HashSet<>(Arrays.asList(split));

		knxDeviceInstance.setGroupObjectInstancesSet(groupObjectInstancesSet);
	}

	private List<KNXComObject> retrieveCOMObjects(final KNXDeviceInstance knxDeviceInstance,
			final Element deviceInstanceElement, final KNXProject knxProject) {

		// COM objects
		final List<KNXComObject> comObjectList = new ArrayList<>();

		final NodeList comObjectInstanceRefsNodeList = deviceInstanceElement
				.getElementsByTagName("ComObjectInstanceRefs");
		final Element objectInstanceRefs = (Element) comObjectInstanceRefsNodeList.item(0);
		if (objectInstanceRefs != null) {
			for (int i = 0; i < objectInstanceRefs.getChildNodes().getLength(); i++) {

				final Node item = objectInstanceRefs.getChildNodes().item(i);

				if (item instanceof Element) {

					final KNXComObject convertCOMObject = convertCOMObject(item, knxProject);

					// if the device's id is part of the GroupObjectTree, the COM object is flagged
					// as a group object and can be displayed to the user using the flag
					final Set<String> groupMap = knxDeviceInstance.getGroupObjectInstancesSet();
					if (SetUtils.emptyIfNull(groupMap).contains(convertCOMObject.getId())) {
						convertCOMObject.setGroupObject(true);
					}
					comObjectList.add(convertCOMObject);
				}
			}
		}

		return comObjectList;
	}

	/**
	 * XMLelement [ComObjectInstanceRef] in 0.xml
	 *
	 * @param node
	 * @return
	 */
	private KNXComObject convertCOMObject(final Node node, final KNXProject knxProject) {

		final Element element = (Element) node;

		// parse number
		final String refIdAttribute = element.getAttribute("RefId");
		final String[] refIdAttributeSplit = refIdAttribute.split("_");
		final String numberAsString = refIdAttributeSplit[0].split("-")[1];
		final int number = Integer.parseInt(numberAsString, HEX_RADIX);

		final KNXComObject knxComObject = new KNXComObject();
		knxComObject.setId(refIdAttribute);
		knxComObject.setKnxProject(knxProject);
		knxComObject.setNumber(number);
		knxComObject.setText(element.getAttribute("Text"));
		if (element.hasAttribute("Links")) {
			knxComObject.setGroupAddressLink(element.getAttribute("Links"));
		}

		return knxComObject;
	}

	private String retrieveAddress(final Element deviceInstanceElement, final KNXProject knxProject) {

		final int addressLevels = 3;

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
