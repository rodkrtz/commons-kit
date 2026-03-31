package com.rodkrtz.commonskit.core.extraction

public sealed interface FieldExtractionResult<out T> {
    public val key: FieldKey<*>
    public val selectors: List<String>
    public val matchedSelector: String?
    public val required: Boolean

    public data class Success<T>(
        override val key: FieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val rawValue: String,
        val value: T,
        override val required: Boolean
    ) : FieldExtractionResult<T>

    public data class NotFound<T>(
        override val key: FieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val reason: String,
        override val required: Boolean
    ) : FieldExtractionResult<T>

    public data class MissingOptional<T>(
        override val key: FieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val reason: String,
        override val required: Boolean,
        val defaultValue: T?,
        val defaultValueConfigured: Boolean
    ) : FieldExtractionResult<T>

    public data class Empty<T>(
        override val key: FieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val reason: String,
        override val required: Boolean
    ) : FieldExtractionResult<T>

    public data class TransformationFailed<T>(
        override val key: FieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val rawValue: String?,
        val reason: String,
        override val required: Boolean
    ) : FieldExtractionResult<T>

    public data class Error<T>(
        override val key: FieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val reason: String,
        val cause: Throwable? = null,
        override val required: Boolean
    ) : FieldExtractionResult<T>
}

public sealed interface ListFieldExtractionResult<out T> {
    public val key: ListFieldKey<*>
    public val selectors: List<String>
    public val matchedSelector: String?
    public val required: Boolean

    public data class Success<T>(
        override val key: ListFieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val values: List<T>,
        override val required: Boolean
    ) : ListFieldExtractionResult<T>

    public data class NotFound<T>(
        override val key: ListFieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val reason: String,
        override val required: Boolean
    ) : ListFieldExtractionResult<T>

    public data class MissingOptional<T>(
        override val key: ListFieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val reason: String,
        override val required: Boolean,
        val defaultValue: List<T>
    ) : ListFieldExtractionResult<T>

    public data class Empty<T>(
        override val key: ListFieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val reason: String,
        override val required: Boolean
    ) : ListFieldExtractionResult<T>

    public data class Error<T>(
        override val key: ListFieldKey<T>,
        override val selectors: List<String>,
        override val matchedSelector: String?,
        val reason: String,
        val cause: Throwable? = null,
        override val required: Boolean
    ) : ListFieldExtractionResult<T>
}