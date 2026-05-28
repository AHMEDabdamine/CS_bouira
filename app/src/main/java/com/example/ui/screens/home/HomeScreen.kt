package com.example.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.HardcodedData
import com.example.data.model.Year
import com.example.i18n.LanguagePickerDialog
import com.example.i18n.LocaleHelper
import com.example.ui.components.CsBouiraBottomNav
import com.example.ui.components.HeroCard
import com.example.ui.components.IconBadge
import com.example.ui.components.NavItem
import com.example.ui.components.SectionLabel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onYearClick: (String, Int) -> Unit,
    onSearchClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val years by viewModel.years.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    val activeNavItem = remember { NavItem.Home }
    var showLanguagePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentLanguage = remember { LocaleHelper.getPersistedLanguage(context) }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { LocaleHelper.setLanguage(context, it) },
            onDismiss = { showLanguagePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CS Bouira",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings), tint = TextSecondary)
                    }
                    IconButton(onClick = { showLanguagePicker = true }) {
                        Icon(Icons.Default.Language, contentDescription = stringResource(R.string.language), tint = TextSecondary)
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh), tint = TextSecondary)
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search), tint = TextSecondary)
                    }
                    IconButton(onClick = onBookmarksClick) {
                        Icon(Icons.Default.Bookmark, contentDescription = stringResource(R.string.bookmarks), tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            CsBouiraBottomNav(
                selectedItem = activeNavItem,
                onItemSelected = { item ->
                    when (item) {
                        NavItem.Favoris -> onBookmarksClick()
                        NavItem.Search -> onSearchClick()
                        else -> { }
                    }
                }
            )
        },
        containerColor = Background,
        modifier = modifier
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 8.dp, bottom = 16.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item(span = { GridItemSpan(2) }) {
                HeroCard(
                    icon = Icons.Default.School,
                    title = stringResource(R.string.app_name),
                    subtitle = stringResource(R.string.university_documents),
                    buttonLabel = stringResource(R.string.explore),
                    onButtonClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://csbouira.xyz/"))
                        context.startActivity(intent)
                    },
                    extraContent = {
                        val dateText = viewModel.currentDate
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dateText,
                            color = OnPrimary.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(4.dp))
                SectionLabel(text = stringResource(R.string.years_section))
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(years) { year ->
                YearCard(
                    year = year,
                    isSelected = selectedYear == year.name,
                    onClick = { viewModel.onYearTap(year.name) }
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        AnimatedVisibility(
            visible = selectedYear != null,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)),
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150))
        ) {
            SemesterSelector(
                yearName = selectedYear ?: "",
                onSemesterClick = { sem ->
                    selectedYear?.let { onYearClick(it, sem) }
                    viewModel.onSemesterSelected()
                },
                onDismiss = { viewModel.onYearTap("") }
            )
        }
    }
}

@Composable
private fun YearCard(
    year: Year,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) SurfaceElevated else Surface,
        animationSpec = tween(200),
        label = "yearCardBg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            IconBadge(
                icon = Icons.Default.School,
                accentColor = Primary,
                containerColor = PrimaryDim,
                iconSize = 20
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = year.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        Text(
            text = year.id.take(2).uppercase(),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = TextLabel.copy(alpha = 0.15f),
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun SemesterSelector(
    yearName: String,
    onSemesterClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceElevated,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = yearName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.choose_semester),
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SemesterPill(HardcodedData.getSemesterLabel(yearName, 1), onClick = { onSemesterClick(1) })
                    SemesterPill(HardcodedData.getSemesterLabel(yearName, 2), onClick = { onSemesterClick(2) })
                }
            }
        }
    }
}

@Composable
private fun RowScope.SemesterPill(
    label: String,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(listOf(Primary, Color(0xFF0A3FCC)))

    Box(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(gradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = OnPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}
