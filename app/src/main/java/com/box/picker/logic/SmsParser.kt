package com.box.picker.logic

import com.box.picker.data.ExpressPackage
import com.box.picker.data.RegexRule

object SmsParser {
    // 不再提供内置默认规则，强制要求用户自定义
    val defaultRules = emptyList<RegexRule>()

    fun parse(text: String, rules: List<RegexRule>): ExpressPackage? {
        // 如果用户没定义规则，则不解析
        if (rules.isEmpty()) return null
        
        for (rule in rules.sortedByDescending { it.priority }) {
            try {
                // 1. 识别关键词匹配(逗号分隔的多个关键词必须全部包含)
                val keywords = rule.identificationKeywords.split(",", "，")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                
                if (keywords.isNotEmpty() && !keywords.all { text.contains(it, ignoreCase = true) }) {
                    continue
                }

                // 2. 匹配规则提取
                val mRules = rule.matchingRules.split("\n", "\r").map { it.trim() }.filter { it.isNotBlank() }
                var extractedParts = mutableListOf<String>()
                
                for (mRule in mRules) {
                    val regex = Regex(mRule, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                    val match = regex.find(text)
                    if (match != null) {
                        // 优先提取 code 命名组，没有则提取第一个捕获组，再没有则提取整个匹配项
                        val extracted = try { match.groups["code"]?.value } catch (e: Exception) { null }
                            ?: match.groups[1]?.value 
                            ?: match.value
                        extractedParts.add(extracted.trim())
                    }
                }

                if (extractedParts.isNotEmpty()) {
                    return ExpressPackage(
                        stationName = rule.stationName,
                        providerName = rule.providerName.ifBlank { rule.stationName },
                        pickupCode = extractedParts.joinToString(" "),
                        address = "", // 不再显示占位提示
                        originalText = text
                    )
                }
            } catch (e: Exception) {
                // 忽略解析错误
            }
        }
        return null
    }
}
