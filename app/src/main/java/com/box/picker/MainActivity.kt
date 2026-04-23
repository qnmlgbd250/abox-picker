package com.box.picker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.box.picker.R
import com.box.picker.data.AppDatabase
import com.box.picker.data.ExpressPackage
import com.box.picker.logic.SmsParser
import com.box.picker.ui.components.GlassCard
import com.box.picker.ui.screens.SettingsScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(this)
        checkAndRequestPermissions()

        lifecycleScope.launch {
            val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
            db.expressDao().deleteOldArchivedPackages(threeDaysAgo)
        }

        setContent {
            var activeTab by remember { mutableStateOf("pending") }
            val iosBlue = Color(0xFF007AFF)
            val iosBg = if (isSystemInDarkTheme()) Color.Black else Color(0xFFF2F2F7)

            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme(primary = iosBlue) 
                             else lightColorScheme(primary = iosBlue, background = iosBg)
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Scaffold(
                        bottomBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp, start = 48.dp, end = 48.dp)
                                    .height(60.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(30.dp),
                                    color = if (isSystemInDarkTheme()) Color(0xFF1C1C1E).copy(alpha = 0.75f) 
                                           else Color.White.copy(alpha = 0.75f),
                                    border = BorderStroke(0.5.dp, if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f)),
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        BottomTabItem(selected = activeTab == "pending", onClick = { activeTab = "pending" }, iconRes = R.drawable.ic_pending, activeColor = iosBlue)
                                        BottomTabItem(selected = activeTab == "archived", onClick = { activeTab = "archived" }, iconRes = R.drawable.ic_archived, activeColor = iosBlue)
                                        BottomTabItem(selected = activeTab == "settings", onClick = { activeTab = "settings" }, iconRes = R.drawable.ic_settings, activeColor = iosBlue)
                                    }
                                }
                            }
                        }
                    ) { padding ->
                        AnimatedContent(
                            targetState = activeTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_change"
                        ) { targetTab ->
                            Box(modifier = Modifier.padding(padding)) {
                                when (targetTab) {
                                    "pending" -> MainListScreen(db, "pending")
                                    "archived" -> MainListScreen(db, "archived")
                                    "settings" -> SettingsScreen(db, onBack = { activeTab = "pending" })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(arrayOf(permission))
            }
        }
    }
}

@Composable
fun BottomTabItem(selected: Boolean, onClick: () -> Unit, iconRes: Int, activeColor: Color) {
    val contentColor by animateColorAsState(if (selected) activeColor else Color.Gray.copy(alpha = 0.4f), label = "color")
    val scale by animateFloatAsState(if (selected) 1.15f else 1f, label = "scale")
    Box(
        modifier = Modifier.size(56.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes), 
            contentDescription = null, 
            tint = contentColor, 
            modifier = Modifier.size(26.dp).scale(scale)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainListScreen(db: AppDatabase, type: String) {
    val dao = db.expressDao()
    val scope = rememberCoroutineScope()
    var showImportDialog by remember { mutableStateOf(false) }
    val packages by (if (type == "pending") dao.getPendingPackages() else dao.getArchivedPackages()).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp, top = 32.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = if (type == "pending") "待取件" else "已签收", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
                Spacer(Modifier.width(10.dp))
                Text(text = packages.size.toString(), style = MaterialTheme.typography.titleLarge, color = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 2.dp))
            }
            if (type == "pending") {
                Surface(onClick = { showImportDialog = true }, color = Color(0xFF007AFF).copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp), modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, null, tint = Color(0xFF007AFF), modifier = Modifier.size(22.dp)) }
                }
            } else {
                IconButton(onClick = { scope.launch { dao.clearArchivedPackages() } }, modifier = Modifier.size(32.dp)) {
                    val clearColor = if (packages.isNotEmpty()) Color(0xFFFF3B30) else Color.Gray.copy(alpha = 0.2f)
                    Icon(Icons.Rounded.Delete, null, tint = clearColor, modifier = Modifier.size(22.dp))
                }
            }
        }

        if (showImportDialog) {
            ImportPackageDialog(onDismiss = { showImportDialog = false }) { text ->
                scope.launch {
                    val rules = dao.getAllRules()
                    val pkg = SmsParser.parse(text, rules) ?: ExpressPackage(
                        stationName = "手动导入", providerName = "其他", pickupCode = text.take(20), address = "手动输入", originalText = text
                    )
                    dao.insertPackage(pkg)
                }
                showImportDialog = false
            }
        }

        if (packages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无内容", color = Color.Gray) }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(packages, key = { it.id }) { pkg ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            when {
                                type == "pending" && value == SwipeToDismissBoxValue.StartToEnd -> { scope.launch { dao.archivePackage(pkg.id) }; true }
                                type == "archived" && value == SwipeToDismissBoxValue.EndToStart -> { scope.launch { dao.restorePackage(pkg.id) }; true }
                                else -> false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = type == "pending",
                        enableDismissFromEndToStart = type == "archived",
                        backgroundContent = {
                            val direction = dismissState.dismissDirection
                            val isRightSwipe = direction == SwipeToDismissBoxValue.StartToEnd
                            val isLeftSwipe = direction == SwipeToDismissBoxValue.EndToStart
                            val color = when {
                                type == "pending" && isRightSwipe -> Color(0xFF4CD964)
                                type == "archived" && isLeftSwipe -> Color(0xFFFF9500)
                                else -> Color.Transparent
                            }
                            val alignment = if (isRightSwipe) Alignment.CenterStart else Alignment.CenterEnd
                            val icon = if (isRightSwipe) Icons.Rounded.Check else Icons.Rounded.Refresh
                            val showIcon = (type == "pending" && isRightSwipe) || (type == "archived" && isLeftSwipe)
                            val scale by animateFloatAsState(if (direction != SwipeToDismissBoxValue.Settled) 1.2f else 0.8f, label = "icon_scale")
                            val iconAlpha by animateFloatAsState(if (direction != SwipeToDismissBoxValue.Settled) 1f else 0f, label = "icon_alpha")
                            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(color).padding(horizontal = 24.dp), contentAlignment = alignment) {
                                if (showIcon) { Icon(imageVector = icon, contentDescription = null, tint = Color.White.copy(alpha = iconAlpha), modifier = Modifier.scale(scale)) }
                            }
                        }
                    ) { PackageItem(pkg, type == "archived") }
                }
            }
        }
    }
}

@Composable
fun PackageItem(pkg: ExpressPackage, isArchived: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "rotate")
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val tagColor = remember(pkg.providerName) {
        when {
            pkg.providerName.contains("菜鸟") -> Color(0xFF007AFF)
            pkg.providerName.contains("顺丰") || pkg.providerName.contains("京东") || pkg.providerName.contains("极兔") -> Color(0xFFFF3B30)
            pkg.providerName.contains("丰巢") || pkg.providerName.contains("韵达") -> Color(0xFFFFCC00)
            pkg.providerName.contains("邮政") || pkg.providerName.contains("EMS") -> Color(0xFF4CD964)
            pkg.providerName.contains("中通") -> Color(0xFF5856D6)
            pkg.providerName.contains("圆通") -> Color(0xFF8E44AD)
            pkg.providerName.contains("申通") -> Color(0xFFFF9500)
            else -> Color(0xFF8E8E93)
        }
    }
    val onClick = if (!isArchived) { { expanded = !expanded } } else null

    GlassCard(onClick = onClick) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(tagColor.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(pkg.providerName, color = tagColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Text(pkg.stationName, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f))
                if (!isArchived) {
                    Icon(painter = painterResource(id = R.drawable.ic_chevron_down), contentDescription = null, tint = Color.Gray.copy(0.4f), modifier = Modifier.size(16.dp).rotate(rotation))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = pkg.pickupCode, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                if (isArchived) { Text(dateFormat.format(Date(pkg.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
            }
            AnimatedVisibility(visible = !isArchived && expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    Spacer(Modifier.height(10.dp))
                    Text("原始短信内容", style = MaterialTheme.typography.labelSmall, color = tagColor, fontWeight = FontWeight.SemiBold)
                    Text(pkg.originalText, style = MaterialTheme.typography.bodySmall, color = Color.Gray.copy(0.8f), modifier = Modifier.padding(top = 4.dp), lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
fun ImportPackageDialog(onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { if(text.isNotBlank()) onImport(text) }, shape = RoundedCornerShape(12.dp)) { Text("添加") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        title = { Text("录入包裹", fontWeight = FontWeight.Bold) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth().height(120.dp), placeholder = { Text("在此粘贴短信原文...") }, shape = RoundedCornerShape(12.dp)) },
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color.White
    )
}
