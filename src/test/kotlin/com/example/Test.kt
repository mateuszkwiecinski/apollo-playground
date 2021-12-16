package com.example

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.CacheKey
import com.apollographql.apollo3.cache.normalized.apolloStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class Test : TestBase() {

  @Test
  fun `apollo store remove(cascade=true)`() = runBlocking {
    mockWebServer.enqueue(nestedResponse)
    val (prefetch, prefetchJob) = watch(
      query = NestedFragmentQuery(),
      fetchPolicy = FetchPolicy.NetworkOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
      failFast = true,
    )
    val receivedFirst = prefetch.receiveAsFlow().first()
    checkNotNull(receivedFirst.data)

    val keyToRemove = "book-1"
    apollo.apolloStore.remove(CacheKey(keyToRemove), cascade = true)

    apollo.apolloStore.dump().forEach { (cacheType, cacheContent) ->
      check(!cacheContent.containsKey(keyToRemove)) { "${cacheType.simpleName} still contains key with id=$keyToRemove"}
    }

    prefetchJob.cancel()
  }
}
