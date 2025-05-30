package ru.golovanov.security.guard.dtos

data class OssIndexResult(
    val coordinates: String,
    val vulnerabilities: List<Vulnerability>
)
