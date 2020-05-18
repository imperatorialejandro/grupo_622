package com.tpsoa.api

sealed class ApiRoute {

    val timeOut: Int
        get() {
            return 3000
        }

    val baseUrl: String
        get() {
            return "put here your URL to consume"
        }
    val url: String
        get() {
            return "$baseUrl/${when (this) {
                else -> ""
            }}"
        }
    val httpMethod: Int
        get() {
            return when (this) {
                else -> Request.Method.GET
            }
        }

    val params: HashMap<String, String>
        get() {
            return when (this) {
                else -> hashMapOf()
            }
        }

    val headers: HashMap<String, String>
        get() {
            return when (this) {
                else -> hashMapOf()
            }
        }

}