package com.example

import com.apollographql.apollo3.api.CompiledField
import com.apollographql.apollo3.api.Executable
import com.apollographql.apollo3.cache.normalized.api.*

internal object IdBasedCacheKeyResolver : CacheKeyResolver(), CacheKeyGenerator {

  override fun cacheKeyForObject(obj: Map<String, Any?>, context: CacheKeyGeneratorContext): CacheKey? =
    obj["id"]?.toString()?.let(::CacheKey)

  override fun cacheKeyForField(field: CompiledField, variables: Executable.Variables): CacheKey? =
    (field.resolveArgument("id", variables) as? String)?.let(::CacheKey)

  override fun listOfCacheKeysForField(field: CompiledField, variables: Executable.Variables): List<CacheKey?>? {
    val ids = field.resolveArgument("ids", variables)

    return if (ids is List<*>) {
      ids.map { it?.toString()?.let(::CacheKey) }
    } else {
      null
    }
  }
}
