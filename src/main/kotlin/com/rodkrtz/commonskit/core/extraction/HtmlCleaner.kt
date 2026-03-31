package com.rodkrtz.commonskit.core.extraction

import org.jsoup.Jsoup
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import java.io.InputStream

public class HtmlCleaner private constructor(
    private val document: Document
) {

    public constructor(
        html: InputStream
    ) : this(html.bufferedReader().use { it.readText() })

    public constructor(html: String) : this(Jsoup.parse(html))

    public fun withoutTags(vararg selectors: String): HtmlCleaner {
        selectors.forEach { selector ->
            document.select(selector).remove()
        }
        return this
    }

    public fun clearAllAttrs(): HtmlCleaner {
        document.allElements.forEach { it.clearAttributes() }
        return this
    }

    public fun clearAllEmptyElements(): HtmlCleaner {
        document.allElements
            .filter { it.children().isEmpty() && it.text().isBlank() }
            .forEach { it.remove() }
        return this
    }

    public fun clearComments(): HtmlCleaner {
        document.traverse { node, _ ->
            if (node is Comment) {
                node.remove()
            }
        }
        return this
    }

    public fun keepOnlyAttributes(vararg selectors: String): HtmlCleaner {
        document.allElements.forEach { element ->
            val attrs = element.attributes()
                .asList()
                .map { it.key }
                .filter { it !in selectors }

            attrs.forEach { element.removeAttr(it) }
        }
        return this
    }

    public fun withoutAttrs(vararg selectors: String): HtmlCleaner {
        selectors.forEach { selector ->
            document.select(selector).removeAttr(selector)
        }
        return this
    }

    public fun removeDoctype(): HtmlCleaner {
        document.documentType()?.remove()
        return this
    }


    public fun toExtractor(): HtmlExtractor {
        return HtmlExtractor(document)
    }

    public fun toCompactedHtml(): String {
        document.outputSettings().prettyPrint(false)
        return document.outerHtml()
            .replace(Regex("[\\n\\r\\t]+"), " ")
            .replace(Regex(" {2,}"), " ")
            .replace(Regex(">\\s+<"), "><")
            .trim()
    }

    public fun toHtml(): String {
        return document.outerHtml()
    }

}