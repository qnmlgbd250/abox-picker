package com.box.picker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.box.picker.data.AppDatabase
import com.box.picker.data.RegexRule
import com.box.picker.logic.SmsParser
import com.box.picker.ui.components.DialogTextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(db: AppDatabase, onBack: () -> Unit) {
    val dao = db.expressDao()
    val scope = rememberCoroutineScope()
    var rules by remember { mutableStateOf<List<RegexRule>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    fun refreshRules() {
        scope.launch {
            rules = dao.getAllRules()
        }
    }

    LaunchedEffect(Unit) {
        refreshRules()
    }

    val iosBg = if (isSystemInDarkTheme()) Color.Black else Color(0xFFF2F2F7)
    val cardBg = if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color.White

    Column(modifier = Modifier.fillMaxSize().background(iosBg)) {
        // iOS Style Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 16.dp, top = 32.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                letterSpacing = (-1).sp
            )
            Surface(
                onClick = { showAddDialog = true },
                color = Color(0xFF007AFF).copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Add, null, tint = Color(0xFF007AFF), modifier = Modifier.size(22.dp))
                }
            }
        }

        if (showAddDialog) {
            AddRuleDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { station, keywords, provider, matching ->
                    scope.launch {
                        dao.insertRule(RegexRule(
                            stationName = station,
                            identificationKeywords = keywords,
                            providerName = provider,
                            matchingRules = matching,
                            priority = (rules.maxOfOrNull { it.priority } ?: 0) + 1
                        ))
                        refreshRules()
                    }
                    showAddDialog = false
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(rules) { rule ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardBg,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(rule.stationName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF007AFF).copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(rule.providerName, style = MaterialTheme.typography.labelSmall, color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("关键词: ${rule.identificationKeywords}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        dao.deleteRule(rule)
                                        refreshRules()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Rounded.Delete, "删除", tint = Color(0xFFFF3B30).copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            rule.matchingRules, 
                            style = MaterialTheme.typography.bodySmall, 
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Color.Gray.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddRuleDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var stationName by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var providerName by remember { mutableStateOf("") }
    var matchingRules by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { if (stationName.isNotBlank() && matchingRules.isNotBlank()) onConfirm(stationName, keywords, providerName, matchingRules) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
            ) {
                Text("保存规则")
            }
        },
        title = { Text("新建解析规则", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                DialogTextField(value = stationName, onValueChange = { stationName = it; if(providerName.isEmpty()) providerName = it }, label = "驿站名称 (如: 菜鸟驿站)")
                DialogTextField(value = keywords, onValueChange = { keywords = it }, label = "识别关键词(逗号分隔)")
                DialogTextField(value = providerName, onValueChange = { providerName = it }, label = "服务商标示(展示用)")
                DialogTextField(value = matchingRules, onValueChange = { matchingRules = it }, label = "匹配正则 (支持多行)", minLines = 3)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color.White
    )
}
