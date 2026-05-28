package com.example.ui.screens.modules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.HardcodedData
import com.example.data.model.Module
import com.example.ui.components.IconBadge
import com.example.ui.components.SectionLabel
import com.example.ui.components.ShimmerBox
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(
    viewModel: ModulesViewModel,
    yearName: String,
    semester: Int,
    onModuleClick: (String, Int, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(yearName, semester) {
        viewModel.loadYearModules(yearName, semester)
    }

    val modules by viewModel.modules.collectAsState()
    val yearTitle by viewModel.yearTitle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val filteredModules = remember(modules, semester) {
        modules.filter { it.semester == semester }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                        Text(
                            text = yearTitle.ifEmpty { stringResource(R.string.loading_dots) },
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background,
        modifier = modifier
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(5) {
                        ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp))
                    }
                }
            }
        } else if (filteredModules.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.no_modules),
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                item {
                    SectionLabel(text = HardcodedData.getSemesterLabel(yearName, semester))
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(filteredModules) { index, module ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(250 + index * 50)) +
                                slideInVertically(tween(250 + index * 50)) { it / 4 }
                    ) {
                        ModuleRowCard(
                            module = module,
                            yearName = yearName,
                            onClick = { onModuleClick(yearName, semester, module.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleRowCard(
    module: Module,
    yearName: String,
    onClick: () -> Unit
) {
    val (icon, accentColor, containerColor) = moduleIconInfo(module.name)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        IconBadge(
            icon = icon,
            accentColor = accentColor,
            containerColor = containerColor
        )
        Spacer(modifier = Modifier.width(14.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = module.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${module.code} · ${HardcodedData.getSemesterLabel(yearName, module.semester)}",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private data class ModuleIconInfo(
    val icon: ImageVector,
    val accent: Color,
    val bg: Color
)

private fun moduleIconInfo(name: String): ModuleIconInfo {
    val lower = name.lowercase()
    return when {
        "algo" in lower || "structure" in lower -> ModuleIconInfo(Icons.Default.AccountTree, CourseAccent, CourseBg)
        "math" in lower || "algèbre" in lower || "analyse" in lower -> ModuleIconInfo(Icons.Default.Functions, CourseAccent, CourseBg)
        "program" in lower || "java" in lower || "poo" in lower -> ModuleIconInfo(Icons.Default.Code, TdAccent, TdBg)
        "base" in lower || "donnée" in lower || "bdd" in lower -> ModuleIconInfo(Icons.Default.Storage, TdAccent, TdBg)
        "réseau" in lower || "reseau" in lower -> ModuleIconInfo(Icons.Default.Lan, CourseAccent, CourseBg)
        "système" in lower || "exploitation" in lower || "os" in lower -> ModuleIconInfo(Icons.Default.Memory, TestAccent, TestBg)
        "architectur" in lower -> ModuleIconInfo(Icons.Default.DeveloperBoard, ExamAccent, ExamBg)
        "recherche" in lower || "opérationnel" in lower -> ModuleIconInfo(Icons.Default.Calculate, CourseAccent, CourseBg)
        "anglais" in lower || "terminologie" in lower -> ModuleIconInfo(Icons.Default.Translate, ColorOther, SurfaceElevated)
        "probabilité" in lower || "statistique" in lower -> ModuleIconInfo(Icons.Default.BarChart, ResumeAccent, ResumeBg)
        "méthode" in lower || "numérique" in lower -> ModuleIconInfo(Icons.Default.Functions, ExamAccent, ExamBg)
        "conception" in lower || "uml" in lower -> ModuleIconInfo(Icons.Default.AccountTree, ResumeAccent, ResumeBg)
        "sécurité" in lower || "securite" in lower || "cyber" in lower -> ModuleIconInfo(Icons.Default.Security, TestAccent, TestBg)
        "ia" in lower || "intelligence" in lower || "machine" in lower || "apprentissage" in lower -> ModuleIconInfo(Icons.Default.Psychology, CourseAccent, CourseBg)
        "compilation" in lower || "compilateur" in lower -> ModuleIconInfo(Icons.Default.Terminal, TdAccent, TdBg)
        "génie" in lower || "logiciel" in lower -> ModuleIconInfo(Icons.Default.Construction, ExamAccent, ExamBg)
        "gouvernance" in lower || "management" in lower || "projet" in lower -> ModuleIconInfo(Icons.Default.Business, CourseAccent, CourseBg)
        else -> ModuleIconInfo(Icons.Default.School, ColorOther, SurfaceElevated)
    }
}
