package com.example.plugin_common.api

class ApiException(val code: Int, message: String) : Exception(message)
