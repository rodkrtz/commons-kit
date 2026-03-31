package com.rodkrtz.commonskit.core.extraction

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path

class HtmlExtractorTest {

    @Test
    fun `should extract required scalar fields successfully`() {
        val html = """
            <html>
              <body>
                <h1 class="title">Notebook Pro</h1>
                <div class="price">R$ 5.499,90</div>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<ProductSnapshot> {
            val title = requiredField(
                name = "title",
                selector = ".title",
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.string()
            )

            val price = requiredField(
                name = "price",
                selector = ".price",
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.bigDecimal()
            )

            build { ctx ->
                ProductSnapshot(
                    title = ctx[title] ?: error("title is required"),
                    price = ctx[price] ?: error("price is required")
                )
            }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertTrue(result.report.isSuccess)
        assertTrue(result.report.requiredFailures.isEmpty())
        assertEquals("Notebook Pro", result.value?.title)
        assertEquals(BigDecimal("5499.90"), result.value?.price)
    }

    @Test
    fun `should support selector fallback`() {
        val html = """
            <html>
              <body>
                <h1>Fallback Title</h1>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<String> {
            val title = requiredField(
                name = "title",
                selectors = listOf(".product-title", ".title", "h1"),
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.string()
            )

            build { ctx ->
                ctx[title] ?: error("title is required")
            }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertTrue(result.report.isSuccess)
        assertEquals("Fallback Title", result.value)
    }

    @Test
    fun `should return failure when required field is missing`() {
        val html = """
            <html>
              <body>
                <div class="content">Only content</div>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<String> {
            val title = requiredField(
                name = "title",
                selector = ".title",
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.string()
            )

            build { ctx ->
                ctx[title] ?: error("title is required")
            }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertFalse(result.report.isSuccess)
        assertNull(result.value)
        assertEquals(listOf("title"), result.report.requiredFailures)
    }

    @Test
    fun `should resolve optional field with default value when missing`() {
        val html = """
            <html>
              <body>
                <div class="title">Produto</div>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<ProductWithCategory> {
            val title = requiredField(
                name = "title",
                selector = ".title",
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.string()
            )

            val category = optionalField(
                name = "category",
                selector = ".category",
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.string(),
                defaultValue = "uncategorized"
            )

            build { ctx ->
                ProductWithCategory(
                    title = ctx[title] ?: error("title is required"),
                    category = ctx[category]
                )
            }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertTrue(result.report.isSuccess)
        assertEquals("Produto", result.value?.title)
        assertEquals("uncategorized", result.value?.category)
        assertTrue("category" in result.report.optionalMisses)
    }

    @Test
    fun `should extract optional list successfully`() {
        val html = """
            <html>
              <body>
                <ul class="tags">
                  <li>Kotlin</li>
                  <li>Jsoup</li>
                  <li>Parsing</li>
                </ul>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<List<String>> {
            val tags = optionalList(
                name = "tags",
                selector = ".tags li",
                strategy = ListExtractionStrategy.SelfText,
                transformer = ValueTransformer.string()
            )

            build { ctx ->
                ctx[tags]
            }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertTrue(result.report.isSuccess)
        assertEquals(listOf("Kotlin", "Jsoup", "Parsing"), result.value)
    }

    @Test
    fun `should use default empty list for optional list when missing`() {
        val html = """
            <html>
              <body>
                <div>No tags here</div>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<List<String>> {
            val tags = optionalList(
                name = "tags",
                selector = ".tags li",
                transformer = ValueTransformer.string(),
                defaultValue = emptyList()
            )

            build { ctx ->
                ctx[tags]
            }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertTrue(result.report.isSuccess)
        assertEquals(emptyList<String>(), result.value)
        assertTrue("tags" in result.report.optionalMisses)
    }

    @Test
    fun `should extract attribute value`() {
        val html = """
            <html>
              <body>
                <a class="buy-button" href="/checkout/123">Comprar</a>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<String?> {
            val href = optionalField(
                name = "href",
                selector = ".buy-button",
                strategy = ExtractionStrategy.Attribute("href"),
                transformer = ValueTransformer.string(),
                defaultValue = null
            )

            build { ctx -> ctx[href] }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertTrue(result.report.isSuccess)
        assertEquals("/checkout/123", result.value)
    }

    @Test
    fun `should extract next sibling text`() {
        val html = """
            <html>
              <body>
                <div class="meta">
                  <span class="label">SKU:</span>
                  <span class="value">ABC-123</span>
                </div>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<String> {
            val sku = requiredField(
                name = "sku",
                selector = ".label",
                strategy = ExtractionStrategy.NextSiblingText,
                transformer = ValueTransformer.string()
            )

            build { ctx -> ctx[sku] ?: error("sku is required") }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertTrue(result.report.isSuccess)
        assertEquals("ABC-123", result.value)
    }

    @Test
    fun `should extract using regex from text`() {
        val html = """
            <html>
              <body>
                <div class="info">Pedido #12345</div>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<Long> {
            val orderId = requiredField(
                name = "orderId",
                selector = ".info",
                strategy = ExtractionStrategy.RegexFromText(
                    regex = Regex("""Pedido\s*#\s*(\d+)""")
                ),
                transformer = ValueTransformer.long()
            )

            build { ctx -> ctx[orderId] ?: error("orderId is required") }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertTrue(result.report.isSuccess)
        assertEquals(12345L, result.value)
    }

    @Test
    fun `should fail when transformation returns null for required field`() {
        val html = """
            <html>
              <body>
                <div class="price">not-a-number</div>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<BigDecimal> {
            val price = requiredField(
                name = "price",
                selector = ".price",
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.bigDecimal()
            )

            build { ctx -> ctx[price] ?: error("price is required") }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertFalse(result.report.isSuccess)
        assertNull(result.value)
        assertEquals(listOf("price"), result.report.requiredFailures)
    }

    @Test
    fun `should normalize extracted text`() {
        val html = """
            <html>
              <body>
                <div class="title">
                    Produto    
                    Premium
                </div>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<String> {
            val title = requiredField(
                name = "title",
                selector = ".title",
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.string()
            )

            build { ctx -> ctx[title] ?: error("title is required") }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertTrue(result.report.isSuccess)
        assertEquals("Produto Premium", result.value)
    }

    @Test
    fun `should apply post processor`() {
        val html = """
            <html>
              <body>
                <div class="document">CPF: 123.456.789-00</div>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<String> {
            val document = requiredField(
                name = "document",
                selector = ".document",
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.string(),
                postProcessor = { value -> value.replace(Regex("[^0-9]"), "") }
            )

            build { ctx -> ctx[document] ?: error("document is required") }
        }

        val result = HtmlExtractor(html).extract(definition)

        assertTrue(result.report.isSuccess)
        assertEquals("12345678900", result.value)
    }

    @Test
    fun `should generate useful debug report`() {
        val html = """
            <html>
              <body>
                <div class="title">Produto X</div>
              </body>
            </html>
        """.trimIndent()

        val definition = extractionDefinition<String> {
            val title = requiredField(
                name = "title",
                selector = ".title",
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.string()
            )

            val price = requiredField(
                name = "price",
                selector = ".price",
                strategy = ExtractionStrategy.SelfText,
                transformer = ValueTransformer.bigDecimal()
            )

            build { ctx ->
                val titleValue = ctx[title] ?: error("title is required")
                requireNotNull(ctx[price]) { "price is required" }
                titleValue
            }
        }

        val result = HtmlExtractor(html).extract(definition)
        val debug = result.report.toDebugString()

        assertNotNull(debug)
        assertTrue(debug.contains("title"))
        assertTrue(debug.contains("price"))
        assertTrue(debug.contains("success=false"))
    }

    private data class ProductSnapshot(
        val title: String,
        val price: BigDecimal
    )

    private data class ProductWithCategory(
        val title: String,
        val category: String?
    )

    @Test
    fun teste() {
        val input = Thread.currentThread().contextClassLoader.getResourceAsStream("teste1.html")

        val html = HtmlCleaner(input)
            .withoutTags("head", "input", "script", "style", "link")
            .keepOnlyAttributes("id")
            .clearAllEmptyElements()
            .removeDoctype()
            .clearComments()
            .toCompactedHtml()

        //674571
        //566973
        //337561
        //331989
        //330106
        //291159
        //276646
        //127906
        //127619
        println("----->>>>>" + html.length)
        println(html)

        val path = Path.of("cleaned.html")

//        Files.createDirectories(path.parent)
        Files.writeString(path, html)
    }
}