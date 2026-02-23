package com.example.custom_paging.paging

data class PagingItem<Value : Any>(
    val value: Value,
    val index: Int
)