package com.rodkrtz.commonskit.core.extraction

import org.jsoup.nodes.Element

public sealed interface ExtractionStrategy {

    public fun extract(element: Element): String?

    public data object SelfText : ExtractionStrategy {
        override fun extract(element: Element): String = element.text()
    }

    public data object OwnText : ExtractionStrategy {
        override fun extract(element: Element): String = element.ownText()
    }

    public data object Html : ExtractionStrategy {
        override fun extract(element: Element): String = element.html()
    }

    public data object NextSiblingText : ExtractionStrategy {
        override fun extract(element: Element): String? =
            element.nextElementSibling()?.text()
    }

    public data object PreviousSiblingText : ExtractionStrategy {
        override fun extract(element: Element): String? =
            element.previousElementSibling()?.text()
    }

    public data class ChildText(
        val childSelector: String
    ) : ExtractionStrategy {
        override fun extract(element: Element): String? =
            element.selectFirst(childSelector)?.text()
    }

    public data class ParentChildText(
        val childSelector: String
    ) : ExtractionStrategy {
        override fun extract(element: Element): String? =
            element.parent()?.selectFirst(childSelector)?.text()
    }

    public data class Attribute(
        val attributeName: String
    ) : ExtractionStrategy {
        override fun extract(element: Element): String? =
            element.attr(attributeName).takeIf(String::isNotBlank)
    }

    public data class ParentAttribute(
        val attributeName: String
    ) : ExtractionStrategy {
        override fun extract(element: Element): String? =
            element.parent()?.attr(attributeName)?.takeIf(String::isNotBlank)
    }

    public data class NextSiblingAttribute(
        val attributeName: String
    ) : ExtractionStrategy {
        override fun extract(element: Element): String? =
            element.nextElementSibling()?.attr(attributeName)?.takeIf(String::isNotBlank)
    }

    public data class RegexFromText(
        val regex: Regex,
        val groupIndex: Int = 1
    ) : ExtractionStrategy {
        override fun extract(element: Element): String? =
            regex.find(element.text())?.groupValues?.getOrNull(groupIndex)
    }

    public data class Custom(
        val extractor: (Element) -> String?
    ) : ExtractionStrategy {
        override fun extract(element: Element): String? = extractor(element)
    }
}

public sealed interface ListExtractionStrategy {

    public fun extract(elements: List<Element>): List<String?>

    public data object SelfText : ListExtractionStrategy {
        override fun extract(elements: List<Element>): List<String?> =
            elements.map(Element::text)
    }

    public data object OwnText : ListExtractionStrategy {
        override fun extract(elements: List<Element>): List<String?> =
            elements.map(Element::ownText)
    }

    public data class Attribute(
        val attributeName: String
    ) : ListExtractionStrategy {
        override fun extract(elements: List<Element>): List<String?> =
            elements.map { it.attr(attributeName).takeIf(String::isNotBlank) }
    }

    public data class ChildText(
        val childSelector: String
    ) : ListExtractionStrategy {
        override fun extract(elements: List<Element>): List<String?> =
            elements.map { it.selectFirst(childSelector)?.text() }
    }

    public data class Custom(
        val extractor: (Element) -> String?
    ) : ListExtractionStrategy {
        override fun extract(elements: List<Element>): List<String?> =
            elements.map(extractor)
    }
}