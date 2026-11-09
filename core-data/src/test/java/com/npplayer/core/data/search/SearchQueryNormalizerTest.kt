package com.npplayer.core.data.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchQueryNormalizerTest {

    @Test
    fun normalize_removesDiacriticsAndConvertsToLowercase() {
        assertEquals("pokemon", SearchQueryNormalizer.normalize("Pokémon"))
        assertEquals("resume", SearchQueryNormalizer.normalize("Résumé"))
        assertEquals("creme brulee", SearchQueryNormalizer.normalize("Crème Brûlée"))
        assertEquals("voila", SearchQueryNormalizer.normalize("Voilà"))
    }

    @Test
    fun extractTokens_splitsByPunctuationAndWhitespace() {
        val tokens = SearchQueryNormalizer.extractTokens("Dead-End.Job_S01E08 [1080p]")
        assertEquals(listOf("dead", "end", "job", "s01e08", "1080p"), tokens)
    }

    @Test
    fun matchesAllTokens_returnsTrueOnlyWhenAllTokensPresent() {
        val target = "One Piece Episode 1172 - [Anime Locus] 1080p.mkv"
        val tokens1 = listOf("piece", "1172")
        val tokens2 = listOf("piece", "1173") // 1173 not in target

        assertTrue(SearchQueryNormalizer.matchesAllTokens(target, tokens1))
        assertFalse(SearchQueryNormalizer.matchesAllTokens(target, tokens2))
    }

    @Test
    fun matchesAllTokens_handlesAccentedSearchQueries() {
        val target = "Amélie (2001) 1080p BluRay"
        // User typed without accent: amelie
        val tokens = SearchQueryNormalizer.extractTokens("amelie 2001")
        assertTrue(SearchQueryNormalizer.matchesAllTokens(target, tokens))
    }

    @Test
    fun findMatchRanges_locatesCorrectSubstringIndices() {
        val text = "Dead-End.Job.S01E08"
        val tokens = listOf("job", "dead")
        val ranges = SearchQueryNormalizer.findMatchRanges(text, tokens)

        assertEquals(2, ranges.size)
        // "Dead" is at 0..3
        assertEquals(0..3, ranges[0])
        assertEquals("Dead", text.substring(ranges[0]))

        // "Job" is at 9..11
        assertEquals(9..11, ranges[1])
        assertEquals("Job", text.substring(ranges[1]))
    }

    @Test
    fun findMatchRanges_mergesAdjacentOrOverlappingRanges() {
        val text = "Matrix Reloaded"
        val tokens = listOf("matrix", "mat")
        val ranges = SearchQueryNormalizer.findMatchRanges(text, tokens)

        assertEquals(1, ranges.size)
        assertEquals(0..5, ranges[0])
        assertEquals("Matrix", text.substring(ranges[0]))
    }
}
