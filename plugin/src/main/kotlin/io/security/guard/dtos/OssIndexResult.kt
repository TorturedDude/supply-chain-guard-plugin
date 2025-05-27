package io.security.guard.dtos

data class OssIndexResult(
    val coordinates: String,
    val vulnerabilities: List<Vulnerability>
)
