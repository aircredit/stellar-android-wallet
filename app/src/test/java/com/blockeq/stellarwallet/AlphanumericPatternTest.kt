package com.blockeq.stellarwallet

import org.junit.Test

import org.junit.Assert.*
import java.util.regex.Pattern


class AlphanumericPatternTest {
    // Hindi is not working, p.e. एक हिंदी वाक्य
    var positiveCases = arrayOf("אײַ־אײַ־אײַ","Доброе утро","日本語文", "Una frase en español", "한국어 문장", "Một câu tiếng việt", "جملة عربية")


    @Test
    fun test_characters(){
        val patt = Pattern.compile("\\p{IsDigit}|\\p{IsLetter}|\\p{Zs}")
        assert(patt.matcher("ए").matches())
        assert(patt.matcher("क").matches())
        assert(patt.matcher("ह").matches())

    }

    @Test
    fun negative_match(){
        val patt = Pattern.compile("[\\p{L}\\p{Nd}\\p{Zs}]+")
        assertFalse(patt.matcher("❤️-ꪂ_{{}{!").matches())
    }

    @Test
    fun positive_cases(){
        val patt = Pattern.compile("[\\p{L}\\p{Nd}\\p{Zs}]+")
        positiveCases.forEach {
            assertTrue(patt.matcher(it).matches())
        }
    }
}
