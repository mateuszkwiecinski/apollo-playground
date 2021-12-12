package com.example

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.example.fragment.SectionFragment
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
      query = Issue3672Query(),
      fetchPolicy = FetchPolicy.NetworkOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
      failFast = true,
    )
    val receivedFirst = prefetch.receiveAsFlow().first()
    checkNotNull(receivedFirst.data)
    val (cache_only, firstJob) = watch(
      query = Issue3672Query(),
      fetchPolicy = FetchPolicy.CacheOnly,
      refetchPolicy = FetchPolicy.CacheFirst,
      failFast = true,
    )
    val receivedSecond = cache_only.receiveAsFlow().first()
    check(receivedFirst.data == receivedSecond.data)

    prefetchJob.cancel()
    firstJob.cancel()
  }


  @Test
  internal fun issue2818() = runBlocking {
    apollo.apolloStore.writeOperation(
      Issue2818Query(),
      Issue2818Query.Data(
        Issue2818Query.Home(
          __typename = "Home",
          sectionA = Issue2818Query.SectionA(
            name = "section-name",
          ),
          sectionFragment = SectionFragment(
            sectionA = SectionFragment.SectionA(
              id = "section-id",
              imageUrl = "https://...",
            ),
          ),
        ),
      ),
    )

    val (prefetch, prefetchJob) = watch(
      query = Issue2818Query(),
      fetchPolicy = FetchPolicy.CacheOnly,
      refetchPolicy = FetchPolicy.CacheOnly,
      failFast = true,
    )
    val cached = prefetch.receiveAsFlow().first()
    check(cached.data?.home?.sectionA?.name == "section-name")
    check(cached.data?.home?.sectionFragment?.sectionA?.id == "section=id")
    check(cached.data?.home?.sectionFragment?.sectionA?.imageUrl == "https://...")
    prefetchJob.cancel()
  }
}
