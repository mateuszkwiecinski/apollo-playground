package com.example

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class Test : TestBase() {

  @Test
  fun `nested response`() = runBlocking {
    mockWebServer.enqueue(response)
    val (prefetch, prefetchJob) = watch(ShelfQuery(id = "test"), fetchPolicy = FetchPolicy.NetworkOnly, refetchPolicy = FetchPolicy.CacheFirst)
    val prefetched = prefetch.receiveAsFlow().first()
    val shelf = checkNotNull(prefetched.data?.viewer?.shelf)
    val (cache_only, cacheOnlyJob) = watch(
      query = BooksByIdsQuery(ids = listOf(shelf.books.first().bookFragment.id)),
      fetchPolicy = FetchPolicy.CacheOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
      failFast = true,
    )
    val list = cache_only.receiveAsFlow().first()
    check(prefetched.data?.viewer?.shelf?.books?.first()?.bookFragment == list.data?.viewer?.nodes?.first()?.bookFragment)

    prefetchJob.cancel()
    cacheOnlyJob.cancel()
  }
}
