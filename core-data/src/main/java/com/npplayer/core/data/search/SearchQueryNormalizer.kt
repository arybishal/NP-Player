package com.npplayer.core.data.search

import java.text.Normalizer
import java.util.Locale

/**
 * High-performance accent-insensitive, case-insensitive query normalizer and highlighter
 * for NP Player search.
 */
object SearchQueryNormalizer {

    private val DIACRITICS_REGEX = "\\p{M}+".toRegex()
    private val DELIMITER_REGEX = "[._\\-\\+\\s\\[\\]\\(\\)\\{\\}]+".toRegex()

    /**
     * Removes diacritics/accents and converts to lowercase.
     */
    fun normalize(text: String): String {
        if (text.isEmpty()) return ""
        val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
        return DIACRITICS_REGEX.replace(decomposed, "").lowercase(Locale.ROOT)
    }

    /**
     * Extracts non-empty search tokens from user query.
     */
    fun extractTokens(query: String): List<String> {
        val normalized = normalize(query.trim())
        if (normalized.isBlank()) return emptyList()
        return normalized.split(DELIMITER_REGEX).filter { it.isNotBlank() }
    }

    /**
     * Verifies that all tokens are present in target text.
     */
    fun matchesAllTokens(target: String, tokens: List<String>): Boolean {
        if (tokens.isEmpty()) return false
        val normalized = normalize(target)
        return tokens.all { token -> normalized.contains(token) }
    }

    /**
     * Finds character index ranges in the original text corresponding to matched tokens.
     * Safely maps positions back to original text indices even with accents/diacritics.
     */
    fun findMatchRanges(originalText: String, tokens: List<String>): List<IntRange> {
        if (tokens.isEmpty() || originalText.isEmpty()) return emptyList()

        // Build mapping from normalized character index back to original character index
        val origIndices = ArrayList<Int>(originalText.length)
        val normalizedSb = StringBuilder(originalText.length)

        for (i in originalText.indices) {
            val char = originalText[i]
            val decomposed = Normalizer.normalize(char.toString(), Normalizer.Form.NFD)
            val stripped = DIACRITICS_REGEX.replace(decomposed, "").lowercase(Locale.ROOT)
            for (ch in stripped) {
                normalizedSb.append(ch)
                origIndices.add(i)
            }
        }

        val normalized = normalizedSb.toString()
        if (normalized.isEmpty()) return emptyList()

        val rawRanges = mutableListOf<IntRange>()

        for (token in tokens) {
            if (token.isEmpty()) continue
            var startIndex = 0
            while (startIndex < normalized.length) {
                val found = normalized.indexOf(token, startIndex)
                if (found == -1) break
                val endPos = found + token.length - 1
                if (found < origIndices.size && endPos < origIndices.size) {
                    val origStart = origIndices[found]
                    val origEnd = origIndices[endPos]
                    if (origStart <= origEnd && origEnd < originalText.length) {
                        rawRanges.add(origStart..origEnd)
                    }
                }
                startIndex = found + 1
            }
        }

        if (rawRanges.isEmpty()) return emptyList()

        // Sort and merge overlapping / adjacent ranges
        rawRanges.sortBy { it.first }
        val merged = mutableListOf<IntRange>()
        var current = rawRanges.first()

        for (i in 1 until rawRanges.size) {
            val next = rawRanges[i]
            if (next.first <= current.last + 1) {
                current = current.first..maxOf(current.last, next.last)
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)
        return merged
    }
}
