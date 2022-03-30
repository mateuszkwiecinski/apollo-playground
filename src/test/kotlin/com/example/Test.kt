package com.example

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class Test : TestBase() {

  @Test
  fun `watch response`() = runBlocking {
    val (cache_only, cacheOnlyJob) = watch(
      query = ShelfQuery(id = "test"),
      fetchPolicy = FetchPolicy.CacheOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
      failFast = false,
    )
    val list = cache_only.receiveAsFlow().first()
    check(list.data == null)

    cacheOnlyJob.cancel()
  }

  @Test
  fun `toFlow response`() = runBlocking {
    val (cache_only, cacheOnlyJob) = watch(
      query = ShelfQuery(id = "test"),
      fetchPolicy = FetchPolicy.CacheOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
      failFast = true,
    )
    val list = cache_only.receiveAsFlow().first()
    check(list.data == null)

    cacheOnlyJob.cancel()
  }
}
