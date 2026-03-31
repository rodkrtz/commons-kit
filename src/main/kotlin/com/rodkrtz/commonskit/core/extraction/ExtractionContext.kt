package com.rodkrtz.commonskit.core.extraction

public class ExtractionContext {

    private val scalarValues: MutableMap<FieldKey<*>, Any?> = mutableMapOf()
    private val listValues: MutableMap<ListFieldKey<*>, List<*>> = mutableMapOf()

    internal fun <T> put(key: FieldKey<T>, value: T) {
        scalarValues[key] = value
    }

    internal fun <T> putList(key: ListFieldKey<T>, value: List<T>) {
        listValues[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> get(key: FieldKey<T>): T? =
        scalarValues[key] as? T

    @Suppress("UNCHECKED_CAST")
    public operator fun <T> get(key: ListFieldKey<T>): List<T> =
        listValues[key] as? List<T> ?: emptyList()

    public fun contains(key: FieldKey<*>): Boolean =
        scalarValues.containsKey(key)

    public fun contains(key: ListFieldKey<*>): Boolean =
        listValues.containsKey(key)
}