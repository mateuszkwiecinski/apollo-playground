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
  fun `the same object, nested`() = runBlocking {
    mockWebServer.enqueue(nestedResponse)
    val (prefetch, prefetchJob) = watch(
      query = GetAuthorQuery(id = "author-id"),
      fetchPolicy = FetchPolicy.NetworkOnly,
      failFast = true,
    )
    val receivedFirst = prefetch.receiveAsFlow().first()
    checkNotNull(receivedFirst.data)

    val (secondCall, secondJob) = watch(
      query = GetBookQuery(id = "book-id"),
      fetchPolicy = FetchPolicy.CacheOnly,
      failFast = true,
    )
    val receivedSecond = secondCall.receiveAsFlow().first()
    checkNotNull(receivedSecond.data).viewer

    prefetchJob.cancel()
    secondJob.cancel()
  }
}
