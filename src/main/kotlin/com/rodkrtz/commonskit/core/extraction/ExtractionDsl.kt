package com.rodkrtz.commonskit.core.extraction

public class ExtractionDefinitionBuilder<T> {

    private val scalarFields: MutableList<ScalarFieldDefinition<*>> = mutableListOf()
    private val listFields: MutableList<ListFieldDefinition<*>> = mutableListOf()

    public fun <V> requiredField(
        name: String,
        selectors: List<String>,
        strategy: ExtractionStrategy,
        transformer: ValueTransformer<V>,
        postProcessor: (String) -> String? = { it }
    ): FieldKey<V> {
        val key = FieldKey<V>(name)
        scalarFields += ScalarFieldDefinition(
            key = key,
            selectors = selectors,
            strategy = strategy,
            transformer = transformer,
            required = true,
            defaultValue = null,
            hasDefaultValue = false,
            postProcessor = postProcessor
        )
        return key
    }

    public fun <V> requiredField(
        name: String,
        selector: String,
        strategy: ExtractionStrategy,
        transformer: ValueTransformer<V>,
        postProcessor: (String) -> String? = { it }
    ): FieldKey<V> =
        requiredField(name, listOf(selector), strategy, transformer, postProcessor)

    public fun <V> optionalField(
        name: String,
        selectors: List<String>,
        strategy: ExtractionStrategy,
        transformer: ValueTransformer<V>,
        defaultValue: V? = null,
        postProcessor: (String) -> String? = { it }
    ): FieldKey<V> {
        val key = FieldKey<V>(name)
        scalarFields += ScalarFieldDefinition(
            key = key,
            selectors = selectors,
            strategy = strategy,
            transformer = transformer,
            required = false,
            defaultValue = defaultValue,
            hasDefaultValue = true,
            postProcessor = postProcessor
        )
        return key
    }

    public fun <V> optionalField(
        name: String,
        selector: String,
        strategy: ExtractionStrategy,
        transformer: ValueTransformer<V>,
        defaultValue: V? = null,
        postProcessor: (String) -> String? = { it }
    ): FieldKey<V> =
        optionalField(name, listOf(selector), strategy, transformer, defaultValue, postProcessor)

    public fun <V> requiredList(
        name: String,
        selectors: List<String>,
        strategy: ListExtractionStrategy = ListExtractionStrategy.SelfText,
        transformer: ValueTransformer<V>,
        postProcessor: (String) -> String? = { it }
    ): ListFieldKey<V> {
        val key = ListFieldKey<V>(name)
        listFields += ListFieldDefinition(
            key = key,
            selectors = selectors,
            strategy = strategy,
            transformer = transformer,
            required = true,
            defaultValue = null,
            postProcessor = postProcessor
        )
        return key
    }

    public fun <V> requiredList(
        name: String,
        selector: String,
        strategy: ListExtractionStrategy = ListExtractionStrategy.SelfText,
        transformer: ValueTransformer<V>,
        postProcessor: (String) -> String? = { it }
    ): ListFieldKey<V> =
        requiredList(name, listOf(selector), strategy, transformer, postProcessor)

    public fun <V> optionalList(
        name: String,
        selectors: List<String>,
        strategy: ListExtractionStrategy = ListExtractionStrategy.SelfText,
        transformer: ValueTransformer<V>,
        defaultValue: List<V> = emptyList(),
        postProcessor: (String) -> String? = { it }
    ): ListFieldKey<V> {
        val key = ListFieldKey<V>(name)
        listFields += ListFieldDefinition(
            key = key,
            selectors = selectors,
            strategy = strategy,
            transformer = transformer,
            required = false,
            defaultValue = defaultValue,
            postProcessor = postProcessor
        )
        return key
    }

    public fun <V> optionalList(
        name: String,
        selector: String,
        strategy: ListExtractionStrategy = ListExtractionStrategy.SelfText,
        transformer: ValueTransformer<V>,
        defaultValue: List<V> = emptyList(),
        postProcessor: (String) -> String? = { it }
    ): ListFieldKey<V> =
        optionalList(name, listOf(selector), strategy, transformer, defaultValue, postProcessor)

    public fun build(
        factory: (ExtractionContext) -> T
    ): ExtractionDefinition<T> =
        ExtractionDefinition(
            scalarFields = scalarFields.toList(),
            listFields = listFields.toList(),
            factory = factory
        )
}

public fun <T> extractionDefinition(
    block: ExtractionDefinitionBuilder<T>.() -> ExtractionDefinition<T>
): ExtractionDefinition<T> =
    ExtractionDefinitionBuilder<T>().block()