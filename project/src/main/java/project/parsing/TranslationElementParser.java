package project.parsing;

import java.util.Map;

import org.w3c.dom.Document;

public interface TranslationElementParser {

    Map<String, Map<String, String>> parse(Document document);

}
