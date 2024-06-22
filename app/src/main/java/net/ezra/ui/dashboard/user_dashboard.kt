package net.ezra.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import net.ezra.navigation.ROUTE_ADD_STUDENTS
import net.ezra.navigation.ROUTE_DASHBOARD
import net.ezra.navigation.ROUTE_HOME
import net.ezra.navigation.ROUTE_LOGIN

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val isLoggedIn: Boolean = sharedPreferences.getBoolean("isLoggedIn", false)
    val email: String? = sharedPreferences.getString("userEmail", "No Email")
    var userName by remember { mutableStateOf("Fetching...") }

    if (!isLoggedIn) {
        LaunchedEffect(Unit) {
            navController.navigate(ROUTE_LOGIN) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    } else {
        LaunchedEffect(Unit) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                fetchUserData(user) { name ->
                    userName = name
                }
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Dashboard",
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(ROUTE_ADD_STUDENTS) }) {
                            Icon(Icons.Default.Person, contentDescription = "Add Students", tint = Color.White)
                        }
                        IconButton(onClick = {
                            navController.navigate(ROUTE_HOME) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                        IconButton(onClick = {
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLoggedIn", false)
                            editor.apply()
                            navController.navigate(ROUTE_LOGIN) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xff0FB06A)
                    )
                )
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Welcome, $userName!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 32.sp,
                                color = Color(0xff0FB06A),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Email: $email",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = {
                                navController.navigate(ROUTE_HOME) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(Color(0xffffa500))
                        ) {
                            Text("Go to Home", color = Color.White)
                        }

                        Button(
                            onClick = {
                                val editor = sharedPreferences.edit()
                                editor.putBoolean("isLoggedIn", false)
                                editor.apply()
                                navController.navigate(ROUTE_LOGIN) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(Color.Red)
                        ) {
                            Text("Logout", color = Color.White)
                        }
                    }
                }
            }
        )
    }
}

private fun fetchUserData(user: FirebaseUser, onResult: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(user.uid)

    userRef.get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val userName = document.getString("name") ?: "No Name"
                onResult(userName)
            } else {
                onResult("No Name")
            }
        }
        .addOnFailureListener {
            onResult("Error fetching name")
        }
}
