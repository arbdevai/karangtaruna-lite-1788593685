package id.or.karangtaruna.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.or.karangtaruna.core.model.*
import id.or.karangtaruna.data.*
import id.or.karangtaruna.ui.screens.*
import id.or.karangtaruna.ui.theme.AppColors
import id.or.karangtaruna.ui.theme.KarangTarunaTheme

@Composable fun KarangTarunaApp() {
    KarangTarunaTheme {
        val authRepo = remember { AuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()) }
        val authVm: AuthViewModel = viewModel(factory = factory { AuthViewModel(authRepo) })
        val sessionValue = authVm.session.collectAsStateWithLifecycle().value
        when (sessionValue) {
            SessionState.Loading -> LoadingScreen()
            SessionState.SignedOut -> AuthScreen(authVm)
            is SessionState.SignedIn -> MainShell(sessionValue.profile, authVm)
        }
    }
}

@Composable private fun MainShell(profile: UserProfile, authVm: AuthViewModel) {
    val repo = remember { OrganizationRepository(FirebaseFirestore.getInstance()) { profile.uid } }
    val moduleVm: ModuleViewModel = viewModel(factory = factory { ModuleViewModel(repo) })
    val homeVm: HomeViewModel = viewModel(factory = factory { HomeViewModel(repo) })
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    Scaffold(containerColor = AppColors.background, bottomBar = { AppBottomNavigation(route) { nav.navigate(it) { launchSingleTop = true; restoreState = true } } }) { padding ->
        NavHost(nav, "home", Modifier.padding(padding)) {
            composable("home") { val state by homeVm.state.collectAsStateWithLifecycle(); HomeScreen(FinanceSummary(state.balance, state.incomeMonth, state.expenseMonth), "RT", state.transactions, { dest -> nav.navigate(dest) }, { homeVm.refresh() }) }
            composable("dues") { DuesScreen(moduleVm) { nav.navigate("dues/new") } }
            composable("dues/new") { DuesFormScreen(moduleVm) { nav.popBackStack() } }
            composable("tx_list") { TransactionsScreen(moduleVm) { type -> nav.navigate(if (type == TransactionType.INCOME) "tx/in" else "tx/out") } }
            composable("tx/in") { TransactionFormScreen(TransactionType.INCOME, moduleVm) { homeVm.refresh(); nav.popBackStack() } }
            composable("tx/out") { TransactionFormScreen(TransactionType.EXPENSE, moduleVm) { homeVm.refresh(); nav.popBackStack() } }
            composable("members") { MembersScreen(moduleVm) { nav.navigate("members/new") } }
            composable("members/new") { MemberFormScreen(moduleVm) { nav.popBackStack() } }
            composable("profile") { ProfileScreen(profile, authVm, { nav.popBackStack() }) { nav.navigate("roles") } }
            composable("roles") { RoleManagementScreen(moduleVm) { nav.popBackStack() } }
        }
    }
}

@Composable private fun AppBottomNavigation(route: String, navigate: (String) -> Unit) {
    NavigationBar(containerColor = AppColors.background, modifier = Modifier.navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp).height(68.dp), tonalElevation = 0.dp) {
        listOf("home" to ("Beranda" to Icons.Default.Home), "dues" to ("Iuran" to Icons.Default.Payments), "tx_list" to ("Transaksi" to Icons.Default.ReceiptLong), "members" to ("Warga" to Icons.Default.People), "profile" to ("Profil" to Icons.Default.Person)).forEach { (destination, labelIcon) ->
            NavigationBarItem(selected = route == destination, onClick = { navigate(destination) }, icon = { Icon(labelIcon.second, labelIcon.first) }, label = { Text(labelIcon.first) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = AppColors.accent, selectedTextColor = AppColors.accent, indicatorColor = AppColors.elevated, unselectedIconColor = AppColors.textTertiary, unselectedTextColor = AppColors.textTertiary))
        }
    }
}

@Composable private fun LoadingScreen() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppColors.accent) } }
internal inline fun <reified T : androidx.lifecycle.ViewModel> factory(crossinline create: () -> T) = object : androidx.lifecycle.ViewModelProvider.Factory { @Suppress("UNCHECKED_CAST") override fun <VM : androidx.lifecycle.ViewModel> create(modelClass: Class<VM>): VM = create() as VM }
