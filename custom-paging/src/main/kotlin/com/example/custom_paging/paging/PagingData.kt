package com.example.custom_paging.paging
class PagingData<Value : Any>(
    val items: List<PagingItem<Value>>,
    val startIndex: Int
)
