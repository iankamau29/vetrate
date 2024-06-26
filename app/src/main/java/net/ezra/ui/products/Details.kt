package net.ezra.ui.products

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(navController: NavController, productId: String) {
    var product by remember { mutableStateOf<Product?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        product = fetchProduct(productId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = product?.name ?: "Details",
                        fontSize = 24.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "backIcon",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Gray,
                    titleContentColor = Color.White,
                )
            )
        },
        content = {
            if (isLoading) {
                // Show a loading indicator if needed
            }

            product?.let { product ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = rememberImagePainter(product.imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(8.dp)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.h5,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Price: $${product.price}",
                        style = MaterialTheme.typography.subtitle1,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.body1,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Buy Button
                    Button(
                        onClick = {
                            addToCartAndNavigate(navController, product)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Buy", fontSize = 18.sp)
                    }
                }
            }
        }
    )
}

private fun addToCartAndNavigate(navController: NavController, product: Product) {
    CartState.addToCart(CartItem(product))
    navController.navigateUp()
}




suspend fun fetchProduct(productId: String): Product? {
    val db = FirebaseFirestore.getInstance()
    val productsCollection = db.collection("products")

    return try {
        val documentSnapshot = productsCollection.document(productId).get().await()
        if (documentSnapshot.exists()) {
            val productData = documentSnapshot.data ?: return null
            Product(
                id = productId,
                name = productData["name"] as String,
                description = productData["description"] as String,
                price = (productData["price"] as? Double) ?: 0.0,
                imageUrl = productData["imageUrl"] as? String ?: ""
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}


data class CartItem(val product: Product)


