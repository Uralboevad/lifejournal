package com.example.lifejournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.example.lifejournal.data.JournalViewModel
import com.example.lifejournal.ui.screens.*
import com.example.lifejournal.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: JournalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LifeJournalTheme {
                var showSplash by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(2000)
                    showSplash = false
                }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(tween(600)) togetherWith fadeOut(tween(400))
                    },
                    label = "splash_transition"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen()
                    } else {
                        JournalNavHost(viewModel)
                    }
                }
            }
        }
    }
}


@Composable
fun SplashScreen() {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "splash_scale"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Coral),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "📔",
                fontSize = 80.sp,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(androidx.compose.ui.unit.Dp(16f)))
            Text(
                "Life Journal",
                style = MaterialTheme.typography.headlineLarge.copy(color = CardWhite),
                modifier = Modifier.scale(scale)
            )
            Text(
                "Your memories, beautifully kept",
                style = MaterialTheme.typography.bodyMedium.copy(color = CardWhite.copy(alpha = 0.8f))
            )
        }
    }
}

private val MaterialTheme get() = androidx.compose.material3.MaterialTheme


@Composable
fun JournalNavHost(viewModel: JournalViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition  = { slideInHorizontally { it } + fadeIn() },
        exitTransition   = { slideOutHorizontally { -it } + fadeOut() },
        popEnterTransition  = { slideInHorizontally { -it } + fadeIn() },
        popExitTransition   = { slideOutHorizontally { it } + fadeOut() }
    ) {
        composable("home") {
            HomeScreen(
                viewModel     = viewModel,
                onCreateEntry = { navController.navigate("create") },
                onViewEntry   = { id -> navController.navigate("view/$id") }
            )
        }
        composable("create") {
            CreateEntryScreen(
                viewModel      = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("view/{entryId}") { back ->
            val id = back.arguments?.getString("entryId")?.toIntOrNull() ?: return@composable
            ViewEntryScreen(
                entryId        = id,
                viewModel      = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}