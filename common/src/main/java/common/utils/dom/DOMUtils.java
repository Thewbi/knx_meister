package common.utils.dom;

import java.util.Optional;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DOMUtils {

    private DOMUtils() {
        // no instances of this class
    }

    public static Optional<Element> firstChildElementByTagName(final Element parentElement, final String tagName) {
        return childElementByTagName(parentElement, tagName, 0);
    }

    public static Optional<Element> childElementByTagName(final Element parentElement, final String tagName,
            final int index) {
        final NodeList connectorsElements = parentElement.getElementsByTagName(tagName);
        if (connectorsElements.getLength() == 0) {
            return Optional.empty();
        }
        return Optional.of((Element) connectorsElements.item(index));
    }

}
