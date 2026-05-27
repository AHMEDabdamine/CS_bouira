package com.example.ui.screens.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Module
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CardDark
import com.example.ui.theme.CosmicDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(
    viewModel: ModulesViewModel,
    yearName: String,
    onModuleClick: (String, Int, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(yearName) {
        viewModel.loadYearModules(yearName)
    }

    val modules by viewModel.modules.collectAsState()
    val yearTitle by viewModel.yearTitle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedSemesterTab by remember { mutableIntStateOf(0) }

    val filteredModules = remember(modules, selectedSemesterTab) {
        val targetSemester = if (selectedSemesterTab == 0) 1 else 2
        modules.filter { it.semester == targetSemester }
    }

    val hasS1 = modules.any { it.semester == 1 }
    val hasS2 = modules.any { it.semester == 2 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = yearTitle.ifEmpty { "Academic Year" },
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("modules_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to home",
                            tint = AccentGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDark)
            )
        },
        containerColor = CosmicDark,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CosmicDark)
        ) {
            if (hasS1 && hasS2) {
                TabRow(
                    selectedTabIndex = selectedSemesterTab,
                    containerColor = CosmicDark,
                    contentColor = AccentGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSemesterTab]),
                            color = AccentGreen
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("semester_tab_row")
                ) {
                    Tab(
                        selected = selectedSemesterTab == 0,
                        onClick = { selectedSemesterTab = 0 },
                        text = {
                            Text(
                                text = "Semester 1",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (selectedSemesterTab == 0) AccentGreen else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_semester_1")
                    )
                    Tab(
                        selected = selectedSemesterTab == 1,
                        onClick = { selectedSemesterTab = 1 },
                        text = {
                            Text(
                                text = "Semester 2",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (selectedSemesterTab == 1) AccentGreen else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("tab_semester_2")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            } else if (filteredModules.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Book,
                            contentDescription = "No courses",
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No subjects reported in this semester yet.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    modifier = Modifier.weight(1f).testTag("modules_list")
                ) {
                    items(filteredModules) { module ->
                        ModuleCard(
                            module = module,
                            onClick = { onModuleClick(yearName, module.semester, module.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleCard(
    module: Module,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("module_card_${module.id}"),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF222224)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Subject Icon",
                    tint = AccentGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.name,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Semester ${module.semester}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
