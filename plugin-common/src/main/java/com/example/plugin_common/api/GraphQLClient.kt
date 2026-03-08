package com.example.plugin_common.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.network.okHttpClient
import okhttp3.OkHttpClient

class GraphQLClient(
    serverUrl: String,
    requestsPerMinute: Int? = null,
    configureClient: (OkHttpClient.Builder.() -> Unit)? = null
) {
    private val okHttpClient = OkHttpClient.Builder()
        .apply {
            if (requestsPerMinute != null && requestsPerMinute > 0) {
                addInterceptor(RateLimitInterceptor(requestsPerMinute))
            }
            configureClient?.invoke(this)
        }
        .build()

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(serverUrl)
        .okHttpClient(okHttpClient)
        .build()

    fun <D : Query.Data> query(query: Query<D>) = apolloClient.query(query)

    fun <D : Mutation.Data> mutation(mutation: Mutation<D>) = apolloClient.mutation(mutation)
}

