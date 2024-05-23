package com.madalin.disertatie.core.domain.util

import java.util.UUID

/**
 * Generates and returns an UUID without dashes.
 */
fun generateId() = UUID.randomUUID().toString().replace("-", "")

/**
 * Generates and returns an UUID with dashes.
 */
fun generateIdWithDashes() = UUID.randomUUID().toString()