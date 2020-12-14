package com.example

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Test : TestBase() {

  @Test
  fun `cache only in parallel`() = runBlocking {
    mockWebServer.enqueue(response)
    val (prefetch, prefetchJob) = watch(
      query = BooksQuery(),
      fetchPolicy = FetchPolicy.NetworkOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
    )
    val (cache_only, firstJob) = watch(
      query = BooksQuery(),
      fetchPolicy = FetchPolicy.CacheOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
    )

    val receivedFirst = prefetch.receiveAsFlow().first()
    val receivedSecond = cache_only.receiveAsFlow().first()
    check(receivedFirst.data == receivedSecond.data)

    prefetchJob.cancel()
    firstJob.cancel()
  }

  @Test
  fun `disjoint queries`() = runBlocking {
    // start cache only
    val (cache_only, cacheOnlyJob) = watch(
      query = BooksQuery(),
      fetchPolicy = FetchPolicy.CacheOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
    )
    val values = mutableListOf<ApolloResponse<BooksQuery.Data>>()
    launch {
      for (response in cache_only) {
        values.add(response)
      }
    }
    check(mockWebServer.mockWebServer.requestCount == 0)

    // make unrelated query
    mockWebServer.enqueue(unrelatedQuery)
    val (unrelated, unrelatedJob) = watch(
      query = UnrelatedQuery(),
      fetchPolicy = FetchPolicy.NetworkOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
    )
    val unrelatedResponse = unrelated.receiveAsFlow().first()
    checkNotNull(unrelatedResponse.data)

    delay(100) // test is asynchronous, wait until_cache_only_emits
    check(values.isEmpty())
    check(mockWebServer.mockWebServer.requestCount == 1) // this fails

    // trigger refresh
    mockWebServer.enqueue(response)
    val (networkOnly, networkOnlyJob) = watch(
      query = BooksQuery(),
      fetchPolicy = FetchPolicy.NetworkOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
    )
    val networkOnlyResponse = networkOnly.receiveAsFlow().first()
    checkNotNull(networkOnlyResponse.data)
    delay(100) // test is asynchronous, wait until_cache_only_emits
    check(values.single().data == networkOnlyResponse.data)
    check(mockWebServer.mockWebServer.requestCount == 2)

    networkOnlyJob.cancel()
    cacheOnlyJob.cancel()
    unrelatedJob.cancel()
  }

  @Test
  fun `nested response`() = runBlocking {
    mockWebServer.enqueue(nestedResponse)
    val (prefetch, prefetchJob) = watch(NestedQuery(), fetchPolicy = FetchPolicy.NetworkOnly, refetchPolicy = FetchPolicy.CacheFirst)
    val receivedFirst = prefetch.receiveAsFlow().first()
    checkNotNull(receivedFirst.data)
    val (cache_only, firstJob) = watch(
      query = NestedQuery(),
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
