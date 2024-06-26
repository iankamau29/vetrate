package net.ezra.ui.cart

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
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
import kotlinx.coroutines.launch
import net.ezra.navigation.ROUTE_HOME
import androidx.compose.ui.text.font.FontSynthesis.Companion.All
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.tasks.await
import net.ezra.ui.products.CartItem
import net.ezra.ui.products.CartState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ShoppingCartScreen(navController: NavController) {
    val cartItems by remember { derivedStateOf { CartState.cartItems } }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Shopping Cart",
                        fontSize = 24.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_HOME)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Optional: Add other actions if needed
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Gray,
                    titleContentColor = Color.White
                )
            )

        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Your cart is empty", style = MaterialTheme.typography.h6)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(cartItems) { item ->
                            CartItemRow(item, onRemoveItem = { removedItem ->
                                CartState.cartItems.remove(removedItem)
                            })
                            Divider()
                        }
                    }
                }

                // Checkout Button at the bottom
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                checkout(cartItems)
                                // Clear cart after successful checkout
                                CartState.cartItems.clear()
                                // Optionally show a confirmation message (not using snackbar here)
                            } catch (e: Exception) {
                                // Handle error (not using snackbar here)
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(48.dp)
                ) {
                    Text("Checkout", fontSize = 18.sp)
                }
            }

            // Loading dialog
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.White)
                            .width(IntrinsicSize.Max)
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Checking out...", style = MaterialTheme.typography.h6)
                    }
                }
            }
        }
    )
}

@Composable
fun CartItemRow(cartItem: CartItem, onRemoveItem: (CartItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White)
    ) {
        Image(
            painter = rememberImagePainter(cartItem.product.imageUrl),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = cartItem.product.name,
                style = MaterialTheme.typography.h6,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Price: $${cartItem.product.price}",
                style = MaterialTheme.typography.body1,
                color = Color.Black
            )
        }
        IconButton(
            onClick = { onRemoveItem(cartItem) },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
                tint = Color.Red
            )
        }
    }
}

private suspend fun checkout(cartItems: List<CartItem>) {
    val db = FirebaseFirestore.getInstance()
    val ordersCollection = db.collection("orders")

    val batch = db.batch()
    for (cartItem in cartItems) {
        val orderData = mapOf(
            "productId" to cartItem.product.id,
            "productName" to cartItem.product.name,
            "quantity" to 1, // For simplicity, assuming quantity is 1
            "timestamp" to System.currentTimeMillis()
        )
        batch.set(ordersCollection.document(), orderData)
    }

    // Commit the batch
    batch.commit().await()
}
