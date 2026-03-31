package com.rodkrtz.commonskit.core.extraction

import org.jsoup.Jsoup
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.InputStream

public class HtmlExtractor private constructor(
    private val document: Document,
    private val normalizer: ValueNormalizer = ValueNormalizer.default()
) {

    public constructor(
        html: InputStream
    ) : this(html.bufferedReader().use { it.readText() })

    public constructor(
        html: Document
    ) : this(html, ValueNormalizer.default())

    public constructor(
        html: String,
        normalizer: ValueNormalizer = ValueNormalizer.default()
    ) : this(Jsoup.parse(html), normalizer)

    public fun <T> extract(definition: ExtractionDefinition<T>): ExtractionExecution<T> {
        val scalarResults = linkedMapOf<FieldKey<*>, FieldExtractionResult<*>>()
        val listResults = linkedMapOf<ListFieldKey<*>, ListFieldExtractionResult<*>>()
        val context = ExtractionContext()

        definition.scalarFields.forEach { field ->
            val result = extractField(field)
            scalarResults[field.key] = result

            if (result is FieldExtractionResult.Success<*>) {
                @Suppress("UNCHECKED_CAST")
                context.put(field.key as FieldKey<Any?>, result.value)
            } else if (result is FieldExtractionResult.MissingOptional<*>) {
                if (result.defaultValueConfigured) {
                    @Suppress("UNCHECKED_CAST")
                    context.put(field.key as FieldKey<Any?>, result.defaultValue)
                }
            }
        }

        definition.listFields.forEach { field ->
            val result = extractListField(field)
            listResults[field.key] = result

            if (result is ListFieldExtractionResult.Success<*>) {
                @Suppress("UNCHECKED_CAST")
                context.putList(field.key as ListFieldKey<Any?>, result.values)
            } else if (result is ListFieldExtractionResult.MissingOptional<*>) {
                @Suppress("UNCHECKED_CAST")
                context.putList(field.key as ListFieldKey<Any?>, result.defaultValue)
            }
        }

        val report = ExtractionReport(
            scalarResults = scalarResults,
            listResults = listResults
        )

        val value = if (report.isSuccess) {
            runCatching { definition.factory(context) }.getOrNull()
        } else {
            null
        }

        return ExtractionExecution(
            value = value,
            context = context,
            report = report
        )
    }

    public fun <T> extractField(field: ScalarFieldDefinition<T>): FieldExtractionResult<T> {
        return runCatching {
            val match = findFirstMatchingElement(field.selectors)
                ?: return buildMissingScalarResult(
                    field = field,
                    reason = "No element found for selectors: ${field.selectors.joinToString()}"
                )

            val rawValue = field.strategy.extract(match.element)
            val normalizedValue = normalize(rawValue, field.postProcessor)

            if (normalizedValue == null) {
                return buildEmptyScalarResult(
                    field = field,
                    matchedSelector = match.selector,
                    reason = "Value was null/blank after normalization or post-processing"
                )
            }

            val transformedValue = field.transformer.transform(normalizedValue)
                ?: return FieldExtractionResult.TransformationFailed(
                    key = field.key,
                    selectors = field.selectors,
                    matchedSelector = match.selector,
                    rawValue = normalizedValue,
                    reason = "Transformer returned null",
                    required = field.required
                )

            FieldExtractionResult.Success(
                key = field.key,
                selectors = field.selectors,
                matchedSelector = match.selector,
                rawValue = normalizedValue,
                value = transformedValue,
                required = field.required
            )
        }.getOrElse { ex ->
            FieldExtractionResult.Error(
                key = field.key,
                selectors = field.selectors,
                matchedSelector = null,
                reason = ex.message ?: "Unknown extraction error",
                cause = ex,
                required = field.required
            )
        }
    }

    public fun <T> extractListField(field: ListFieldDefinition<T>): ListFieldExtractionResult<T> {
        return runCatching {
            val match = findFirstMatchingElements(field.selectors)
                ?: return buildMissingListResult(
                    field = field,
                    reason = "No elements found for selectors: ${field.selectors.joinToString()}"
                )

            val rawValues = field.strategy.extract(match.elements)

            val values = rawValues.mapNotNull { raw ->
                normalize(raw, field.postProcessor)?.let(field.transformer::transform)
            }

            if (values.isEmpty()) {
                return if (field.required) {
                    ListFieldExtractionResult.Empty(
                        key = field.key,
                        selectors = field.selectors,
                        matchedSelector = match.selector,
                        reason = "List extraction produced no values",
                        required = true
                    )
                } else {
                    ListFieldExtractionResult.MissingOptional(
                        key = field.key,
                        selectors = field.selectors,
                        matchedSelector = match.selector,
                        reason = "Optional list resolved to empty",
                        required = false,
                        defaultValue = field.defaultValue ?: emptyList()
                    )
                }
            }

            ListFieldExtractionResult.Success(
                key = field.key,
                selectors = field.selectors,
                matchedSelector = match.selector,
                values = values,
                required = field.required
            )
        }.getOrElse { ex ->
            ListFieldExtractionResult.Error(
                key = field.key,
                selectors = field.selectors,
                matchedSelector = null,
                reason = ex.message ?: "Unknown list extraction error",
                cause = ex,
                required = field.required
            )
        }
    }

    private fun normalize(
        rawValue: String?,
        postProcessor: (String) -> String?
    ): String? = normalizer.normalize(rawValue)?.let(postProcessor)

    private fun <T> buildMissingScalarResult(
        field: ScalarFieldDefinition<T>,
        reason: String
    ): FieldExtractionResult<T> {
        return if (field.required) {
            FieldExtractionResult.NotFound(
                key = field.key,
                selectors = field.selectors,
                matchedSelector = null,
                reason = reason,
                required = true
            )
        } else {
            FieldExtractionResult.MissingOptional(
                key = field.key,
                selectors = field.selectors,
                matchedSelector = null,
                reason = reason,
                required = false,
                defaultValue = field.defaultValue,
                defaultValueConfigured = field.hasDefaultValue
            )
        }
    }

    private fun <T> buildEmptyScalarResult(
        field: ScalarFieldDefinition<T>,
        matchedSelector: String,
        reason: String
    ): FieldExtractionResult<T> {
        return if (field.required) {
            FieldExtractionResult.Empty(
                key = field.key,
                selectors = field.selectors,
                matchedSelector = matchedSelector,
                reason = reason,
                required = true
            )
        } else {
            FieldExtractionResult.MissingOptional(
                key = field.key,
                selectors = field.selectors,
                matchedSelector = matchedSelector,
                reason = reason,
                required = false,
                defaultValue = field.defaultValue,
                defaultValueConfigured = field.hasDefaultValue
            )
        }
    }

    private fun <T> buildMissingListResult(
        field: ListFieldDefinition<T>,
        reason: String
    ): ListFieldExtractionResult<T> {
        return if (field.required) {
            ListFieldExtractionResult.NotFound(
                key = field.key,
                selectors = field.selectors,
                matchedSelector = null,
                reason = reason,
                required = true
            )
        } else {
            ListFieldExtractionResult.MissingOptional(
                key = field.key,
                selectors = field.selectors,
                matchedSelector = null,
                reason = reason,
                required = false,
                defaultValue = field.defaultValue ?: emptyList()
            )
        }
    }

    private fun findFirstMatchingElement(selectors: List<String>): SelectorMatch? {
        for (selector in selectors) {
            val element = document.selectFirst(selector)
            if (element != null) {
                return SelectorMatch(selector, element)
            }
        }
        return null
    }

    private fun findFirstMatchingElements(selectors: List<String>): SelectorListMatch? {
        for (selector in selectors) {
            val elements = document.select(selector)
            if (elements.isNotEmpty()) {
                return SelectorListMatch(selector, elements.toList())
            }
        }
        return null
    }

    private data class SelectorMatch(
        val selector: String,
        val element: Element
    )

    private data class SelectorListMatch(
        val selector: String,
        val elements: List<Element>
    )

    public fun extractFirstOuterHtml(selector: String): String? =
        document.selectFirst(selector)?.outerHtml()

    public fun extractFirstInnerHtml(selector: String): String? =
        document.selectFirst(selector)?.html()

    public fun extractAllOuterHtml(selector: String): List<String> =
        document.select(selector).map(Element::outerHtml)

    public fun extractAllInnerHtml(selector: String): List<String> =
        document.select(selector).map(Element::html)

    public fun extractFirstText(selector: String): String? =
        document.selectFirst(selector)?.text()

    public fun extractAllText(selector: String): List<String> =
        document.select(selector).map(Element::text)

    public fun slice(selector: String): HtmlExtractor? =
        extractFirstOuterHtml(selector)?.let { HtmlExtractor(it, normalizer) }

    public fun sliceAll(selector: String): List<HtmlExtractor> =
        extractAllOuterHtml(selector).map { HtmlExtractor(it, normalizer) }

}