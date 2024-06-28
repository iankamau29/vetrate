package net.ezra.ui.products

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import net.ezra.navigation.ROUTE_HOME



@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersListScreen(navController: NavController) {
    var isLoading by remember { mutableStateOf(true) }
    var offersList by remember { mutableStateOf(emptyList<Product>()) }
    var displayedProductCount by remember { mutableStateOf(1) }
    var progress by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        fetchSpecialOffers { fetchedSpecialOffers ->
            offersList = fetchedSpecialOffers
            isLoading = false
            displayedProductCount = 10
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Special Offers", fontSize = 30.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_HOME)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Gray)
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(it)  // Apply padding values provided by Scaffold
            ) {
                if (isLoading) {
                    // Progress indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(progress = progress / 100f)
                        Text(text = "Loading... $progress%", fontSize = 20.sp)
                    }
                } else {
                    if (offersList.isEmpty()) {
                        // No products found
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No special offers found")
                        }
                    } else {
                        // Products list
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(offersList.take(displayedProductCount)) { product ->
                                OffersListItem(product) {
                                    navController.navigate("productDetail/${product.id}")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Load More Button
                        if (displayedProductCount < offersList.size) {
                            Button(
                                colors = ButtonDefaults.buttonColors(Color.Gray),
                                onClick = { displayedProductCount += 8 },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(text = "Load More")
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun OffersListItem(product: Product, onItemClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClick(product.id) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Product Image
            Image(
                painter = rememberImagePainter(product.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Product Details
            Column {
                Text(text = product.name)
                Text(text = "Price: ${product.price}")
            }
        }
    }
}

suspend fun fetchSpecialOffers(onSuccess: (List<Product>) -> Unit) {
    val firestore = Firebase.firestore
    try {
        val snapshot = firestore.collection("special offers").get().await()
        val productList = snapshot.documents.mapNotNull { doc ->
            val product = doc.toObject<Product>()
            product?.id = doc.id
            product
        }
        onSuccess(productList)
    } catch (e: Exception) {
        onSuccess(emptyList())
    }
}
