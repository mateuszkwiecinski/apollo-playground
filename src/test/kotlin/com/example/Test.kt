package com.example

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class Test : TestBase() {

  @Test
  fun newIssue() = runBlocking {
    mockWebServer.enqueue(nestedResponse)
    val (prefetch, prefetchJob) = watch(
      query = GetLibrariesQuery(),
      fetchPolicy = FetchPolicy.NetworkOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
      failFast = true,
    )
    val receivedFirst = prefetch.receiveAsFlow().first()
    checkNotNull(receivedFirst.data)
    val (cache_only, firstJob) = watch(
      query = GetBookDetailsQuery(id = "book-1"),
      fetchPolicy = FetchPolicy.CacheOnly,
      refetchPolicy = FetchPolicy.CacheOnly,
      failFast = true,
    )
    val receivedSecond = cache_only.receiveAsFlow().first()
    check(receivedFirst.data!!.viewer.libraries.first().book.book == receivedSecond.data!!.viewer.book.book)

    prefetchJob.cancel()
    firstJob.cancel()
  }
}
