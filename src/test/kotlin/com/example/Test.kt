package com.example

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class Test : TestBase() {

  @Test
  fun issue3672() = runBlocking {
    mockWebServer.enqueue(nestedResponse)
    val (prefetch, prefetchJob) = watch(
      query = NestedFragmentQuery(),
      fetchPolicy = FetchPolicy.NetworkOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
      failFast = true,
    )
    val receivedFirst = prefetch.receiveAsFlow().first()
    checkNotNull(receivedFirst.data)
    println(cacheString())
    val (cache_only, firstJob) = watch(
      query = NestedFragmentQuery(),
      fetchPolicy = FetchPolicy.CacheOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
      failFast = true,
    )
    val receivedSecond = cache_only.receiveAsFlow().first()
    check(receivedFirst.data == receivedSecond.data)

    prefetchJob.cancel()
    firstJob.cancel()
  }
}
