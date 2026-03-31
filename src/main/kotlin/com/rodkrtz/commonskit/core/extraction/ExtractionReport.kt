package com.rodkrtz.commonskit.core.extraction

public data class ExtractionExecution<T>(
    val value: T?,
    val context: ExtractionContext,
    val report: ExtractionReport
)

public data class ExtractionReport(
    val scalarResults: Map<FieldKey<*>, FieldExtractionResult<*>>,
    val listResults: Map<ListFieldKey<*>, ListFieldExtractionResult<*>>
) {
    public val requiredFailures: List<String>
        get() = buildList {
            scalarResults.values.forEach { if (it.isRequiredFailure()) add(it.key.name) }
            listResults.values.forEach { if (it.isRequiredFailure()) add(it.key.name) }
        }

    public val optionalMisses: List<String>
        get() = buildList {
            scalarResults.values.forEach {
                if (it is FieldExtractionResult.MissingOptional<*>) add(it.key.name)
            }
            listResults.values.forEach {
                if (it is ListFieldExtractionResult.MissingOptional<*>) add(it.key.name)
            }
        }

    public val isSuccess: Boolean
        get() = requiredFailures.isEmpty()

    public fun requireSuccess(): ExtractionReport {
        check(isSuccess) {
            "Required extraction failures: ${requiredFailures.joinToString()}"
        }
        return this
    }

    public fun toDebugString(): String {
        val lines = mutableListOf<String>()
        lines += "ExtractionReport(success=$isSuccess)"

        scalarResults.values.forEach { result ->
            lines += when (result) {
                is FieldExtractionResult.Success<*> ->
                    "  [FIELD][SUCCESS] ${result.key.name} selector=${result.matchedSelector} value=${result.value}"
                is FieldExtractionResult.NotFound<*> ->
                    "  [FIELD][NOT_FOUND] ${result.key.name} selectors=${result.selectors}"
                is FieldExtractionResult.MissingOptional<*> ->
                    "  [FIELD][OPTIONAL_MISSING] ${result.key.name} default=${result.defaultValueConfigured}"
                is FieldExtractionResult.Empty<*> ->
                    "  [FIELD][EMPTY] ${result.key.name} selector=${result.matchedSelector}"
                is FieldExtractionResult.TransformationFailed<*> ->
                    "  [FIELD][TRANSFORMATION_FAILED] ${result.key.name} raw=${result.rawValue}"
                is FieldExtractionResult.Error<*> ->
                    "  [FIELD][ERROR] ${result.key.name} reason=${result.reason}"
            }
        }

        listResults.values.forEach { result ->
            lines += when (result) {
                is ListFieldExtractionResult.Success<*> ->
                    "  [LIST][SUCCESS] ${result.key.name} selector=${result.matchedSelector} size=${result.values.size}"
                is ListFieldExtractionResult.NotFound<*> ->
                    "  [LIST][NOT_FOUND] ${result.key.name} selectors=${result.selectors}"
                is ListFieldExtractionResult.MissingOptional<*> ->
                    "  [LIST][OPTIONAL_MISSING] ${result.key.name} defaultSize=${result.defaultValue.size}"
                is ListFieldExtractionResult.Empty<*> ->
                    "  [LIST][EMPTY] ${result.key.name} selector=${result.matchedSelector}"
                is ListFieldExtractionResult.Error<*> ->
                    "  [LIST][ERROR] ${result.key.name} reason=${result.reason}"
            }
        }

        return lines.joinToString("\n")
    }
}

private fun FieldExtractionResult<*>.isRequiredFailure(): Boolean =
    when (this) {
        is FieldExtractionResult.Success -> false
        is FieldExtractionResult.MissingOptional -> false
        is FieldExtractionResult.NotFound -> required
        is FieldExtractionResult.Empty -> required
        is FieldExtractionResult.TransformationFailed -> required
        is FieldExtractionResult.Error -> required
    }

private fun ListFieldExtractionResult<*>.isRequiredFailure(): Boolean =
    when (this) {
        is ListFieldExtractionResult.Success -> false
        is ListFieldExtractionResult.MissingOptional -> false
        is ListFieldExtractionResult.NotFound -> required
        is ListFieldExtractionResult.Empty -> required
        is ListFieldExtractionResult.Error -> required
    }