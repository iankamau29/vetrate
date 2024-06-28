package net.ezra.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import net.ezra.R
import net.ezra.navigation.ROUTE_ADD_PRODUCT
import net.ezra.navigation.ROUTE_HOME
import net.ezra.navigation.ROUTE_LOGIN
import net.ezra.navigation.ROUTE_SHOPPING_CART
import net.ezra.navigation.ROUTE_USER_DASHBOARD
import net.ezra.navigation.ROUTE_VIEW_PROD
import net.ezra.navigation.ROUTE_VIEW_SPECIALOFFER
import net.ezra.ui.products.Product
import net.ezra.ui.products.ProductListItem
import net.ezra.ui.products.fetchProducts

data class Screen(val title: String, val icon: Int)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ResourceAsColor", "UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    var isDrawerOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var locationEnabled by remember { mutableStateOf(true) }
    var productList by remember { mutableStateOf(emptyList<Product>()) }
    var specialOfferList by remember { mutableStateOf(emptyList<Product>()) }
    var isLoading by remember { mutableStateOf(true) }
    var displayedProductCount by remember { mutableStateOf(1) }
    var progress by remember { mutableStateOf(0) }
    var userEmail by remember { mutableStateOf("No Email") }

    val callLauncher: ManagedActivityResultLauncher<Intent, ActivityResult> =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { _ -> }

    LaunchedEffect(Unit) {
        fetchSpecialOffer { specialOffer ->
            specialOfferList = specialOffer
        }
        fetchProducts { fetchedProducts ->
            productList = fetchedProducts
            isLoading = false
        }
    }

    Scaffold(
        topBar = { HomeTopBar(searchQuery, onSearchQueryChange = { searchQuery = it }, locationEnabled, onLocationToggle = { locationEnabled = !locationEnabled }) },
        content = { HomeContent(navController, isDrawerOpen, onDrawerClose = { isDrawerOpen = false }, isLoading, productList, progress) },
        bottomBar = { BottomBar(navController = navController) },
//        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center
    )

    AnimatedDrawer(isOpen = isDrawerOpen, onClose = { isDrawerOpen = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(searchQuery: String, onSearchQueryChange: (String) -> Unit, locationEnabled: Boolean, onLocationToggle: () -> Unit) {
    var userName by remember { mutableStateOf("Guest") }
    var profilePictureUrl by remember { mutableStateOf("https://via.placeholder.com/150") } // Default profile picture URL

    // Fetch user data from Firebase Authentication
    LaunchedEffect(Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            userName = user.displayName ?: "No Name"
            profilePictureUrl = user.photoUrl?.toString() ?: profilePictureUrl
        }
    }

    Column(
        modifier = Modifier
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberImagePainter(profilePictureUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Good Morning",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = userName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            Row {
                IconButton(onClick = { /* TODO: Handle Notification Click */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* TODO: Handle Favorite Click */ }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.White
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .background(Color.DarkGray, shape = CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Looking For Shoes",
                        modifier = Modifier,
                        color = Color.Gray,
                        fontSize = 16.sp,

                        )
                } else {
                    Text(
                        text = searchQuery,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { /* TODO: Handle Settings Click */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings), // Replace with your own settings icon
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }
    }
}
@Composable
fun HomeContent(
    navController: NavHostController,
    isDrawerOpen: Boolean,
    onDrawerClose: () -> Unit,
    isLoading: Boolean,
    productList: List<Product>,
    progress: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { if (isDrawerOpen) onDrawerClose() }
    ) {
        Spacer(modifier = Modifier.height(80.dp))
            Column(
                
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
            ) {

                Spacer(modifier = Modifier.height(160.dp))
                Row{
                    Text("Special Offers")
                    Spacer(modifier = Modifier.width(130.dp))
                    Text("View All", modifier = Modifier.clickable { navController.navigate(
                        ROUTE_VIEW_SPECIALOFFER) })
                }
                Row {
                    SpecialOffers()
                }

                Row {
                    Text("Jordan 1's")
                    Spacer(modifier = Modifier.width(130.dp))
                    Text("View All",
                        modifier = Modifier.clickable { navController.navigate(ROUTE_VIEW_PROD) },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = progress / 100f)
                        Text(text = "Loading... $progress%", fontSize = 20.sp, color = Color.Black)
                    }
                } else {
                    if (productList.isEmpty()) {
                        Text(text = "No products found")
                    } else {
                        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                            items(productList.take(2)) { product ->
                                ProductListItem(product) {
                                    navController.navigate("productDetail/${product.id}")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Load More Button
                    }
                }
            }
        }

}

@Composable
fun AnimatedDrawer(isOpen: Boolean, onClose: () -> Unit) {
    val drawerWidth = remember { Animatable(if (isOpen) 250f else 0f) }

    LaunchedEffect(isOpen) {
        drawerWidth.animateTo(if (isOpen) 250f else 0f, animationSpec = tween(durationMillis = 300))
    }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(drawerWidth.value.dp),
        color = Color.Black,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Drawer")
            }
            Spacer(modifier = Modifier.height(16.dp))
            DrawerItem(text = "All Products") { }
            DrawerItem(text = "Drawer Item 2") { /* TODO: Handle click */ }
            DrawerItem(text = "Drawer Item 3") { /* TODO: Handle click */ }
            DrawerItem(text = "Drawer Item 4") { /* TODO: Handle click */ }
        }
    }
}

@Composable
fun DrawerItem(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = text)
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun BottomBar(navController: NavHostController) {
    val selectedIndex = remember { mutableStateOf(0) }
    val context = LocalContext.current
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val isLoggedIn: Boolean = sharedPreferences.getBoolean("isLoggedIn", false)

    BottomAppBar(containerColor = Color.Transparent) {
        BottomNavigation(
            backgroundColor = Color.Gray,
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            BottomNavigationItem(
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = Color(0xffffa500)) },
                selected = (selectedIndex.value == 0),
                onClick = {
                    selectedIndex.value = 0
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                }
            )
            BottomNavigationItem(
                icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Profile", tint = Color(0xffffa500)) },
                selected = (selectedIndex.value == 3),
                onClick = {
                    selectedIndex.value = 3
                    navController.navigate(ROUTE_SHOPPING_CART) { popUpTo("home") { inclusive = true } }
                }
            )
            BottomNavigationItem(
                icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color(0xffffa500)) },
                label = { Text(text = "Profile", color = Color.White) },
                selected = (selectedIndex.value == 3),
                onClick = {
                    selectedIndex.value = 3
                    if (isLoggedIn) {
                        navController.navigate(ROUTE_USER_DASHBOARD) { popUpTo("home") { inclusive = true } }
                    } else {
                        navController.navigate(ROUTE_LOGIN) { popUpTo(ROUTE_HOME) { inclusive = true } }
                    }
                }
            )
            BottomNavigationItem(
                icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite", tint = Color(0xffffa500)) },
                selected = (selectedIndex.value == 2),
                onClick = {
                    selectedIndex.value = 2
                }
            )
            BottomNavigationItem(
                icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Profile", tint = Color(0xffffa500)) },
                selected = (selectedIndex.value == 3),
                onClick = {
                    selectedIndex.value = 3
                    navController.navigate(ROUTE_ADD_PRODUCT) { popUpTo("home") { inclusive = true } }
                }
            )
        }
    }
}

private suspend fun fetchSpecialOffer(onSuccess: (List<Product>) -> Unit) {
    val firestore = Firebase.firestore
    val snapshot = firestore.collection("special offers").get().await()
    val productList = snapshot.documents.mapNotNull { doc ->
        val product = doc.toObject<Product>()
        product?.id = doc.id
        product
    }
    onSuccess(productList)
}

data class SpecialOffer(val discount: String, val productName: String, val description: String, val price: String, val imageUrl: String)

@Composable
fun SpecialOffers() {
    var specialOffer by remember { mutableStateOf<SpecialOffer?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        fetchSingleSpecialOffer { offer ->
            specialOffer = offer
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(150.dp)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        specialOffer?.let { offer ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(150.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = offer.discount,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = offer.productName,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = offer.description,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = offer.price,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Image(
                        painter = rememberImagePainter(data = offer.imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}



fun fetchSingleSpecialOffer(onComplete: (SpecialOffer?) -> Unit) {
    val db = Firebase.firestore
    val collectionRef = db.collection("special offers")

    // Assuming "offer1" is the document ID you want to fetch
    val documentId = "4da8be58-a2d2-42e3-82e3-cfd29bcce340"

    collectionRef.document(documentId)
        .get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val data = snapshot.data
                val discount = data?.get("discount") as? String ?: "N/A"
                val productName = data?.get("name") as? String ?: "N/A"
                val description = data?.get("description") as? String ?: "N/A"
                val price = data?.get("price") as? Double ?: "N/A"
                val imageUrl = data?.get("imageUrl") as? String ?: ""

                onComplete(SpecialOffer(discount, productName, description,
                    price.toString(), imageUrl))
            } else {
                onComplete(null)
            }
        }
        .addOnFailureListener { exception ->
            // Handle error
            onComplete(null)
        }
}
