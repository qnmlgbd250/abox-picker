package com.box.picker.logic

import org.junit.Test

class RegexTest {
    @Test
    fun testRegex() {
        val regex = Regex("""\b\d{1,2}-\d{1,2}-\d{3,4}\b""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val text = "【菜鸟驿站】您的包裹已到站，凭4-2-0329到深圳新屋村中区14栋有友店取件。"
        val match = regex.find(text)
        println("Match whole: " + match?.value)
        try {
            println("Group 1: " + match?.groups?.get(1)?.value)
        } catch (e: Exception) {
            println("Exception: " + e.message)
        }
    }
}
