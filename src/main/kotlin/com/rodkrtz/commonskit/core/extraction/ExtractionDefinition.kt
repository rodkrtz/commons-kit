package com.rodkrtz.commonskit.core.extraction

public data class ScalarFieldDefinition<T>(
    val key: FieldKey<T>,
    val selectors: List<String>,
    val strategy: ExtractionStrategy,
    val transformer: ValueTransformer<T>,
    val required: Boolean,
    val defaultValue: T?,
    val hasDefaultValue: Boolean,
    val postProcessor: (String) -> String?
)

public data class ListFieldDefinition<T>(
    val key: ListFieldKey<T>,
    val selectors: List<String>,
    val strategy: ListExtractionStrategy,
    val transformer: ValueTransformer<T>,
    val required: Boolean,
    val defaultValue: List<T>?,
    val postProcessor: (String) -> String?
)

public class ExtractionDefinition<T> internal constructor(
    internal val scalarFields: List<ScalarFieldDefinition<*>>,
    internal val listFields: List<ListFieldDefinition<*>>,
    internal val factory: (ExtractionContext) -> T
)