package com.example

import com.apollographql.apollo3.api.CompiledField
import com.apollographql.apollo3.api.Executable
import com.apollographql.apollo3.cache.normalized.api.*

internal object IdBasedCacheKeyResolver : CacheKeyResolver(), CacheKeyGenerator {

  override fun cacheKeyForObject(obj: Map<String, Any?>, context: CacheKeyGeneratorContext) =
    obj["id"]?.toString()?.let(::CacheKey) ?: TypePolicyCacheKeyGenerator.cacheKeyForObject(obj, context)

  override fun cacheKeyForField(field: CompiledField, variables: Executable.Variables): CacheKey? =
    (field.resolveArgument("id", variables) as? String)?.let(::CacheKey)
}
