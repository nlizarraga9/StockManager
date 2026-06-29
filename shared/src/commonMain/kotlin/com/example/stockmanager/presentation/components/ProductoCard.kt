package com.example.stockmanager.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stockmanager.domain.model.Producto
import com.example.stockmanager.utils.toPrice
import stockmanager.shared.generated.resources.Res

@Suppress("ktlint:standard:function-naming")
@Composable
fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit,
) {
    val stockColor =
        when {
            producto.sinStock -> MaterialTheme.colorScheme.error
            producto.stockBajo -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.primary
        }

    Card(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                producto.descripcion?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = producto.precio.toPrice(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = stockColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "Stock: ${producto.stock}",
                        color = stockColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(2.dp))
                when {
                    producto.sinStock -> {
                        Text(
                            "Sin stock",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    producto.stockBajo -> {
                        Text(
                            "Stock bajo",
                            color = Color(0xFFFF9800),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}


//---  Previews --------------------------------

private val productoNormal =
    Producto(
        id = "1",
        nombre = "Coca Cola 2.25L",
        descripcion = "Gaseosa",
        precio = 1500.0,
        stock = 24,
        stockMinimo = 5,
    )
private val productoStockBajo =
    Producto(
        id = "2",
        nombre = "Arroz Gallo 1kg",
        descripcion = "Arroz largo fino",
        precio = 900.0,
        stock = 3,
        stockMinimo = 5,
    )
private val productoSinStock =
    Producto(
        id = "3",
        nombre = "Fideos Matarazzo 500g",
        descripcion = "Fideos tallarines",
        precio = 750.0,
        stock = 0,
        stockMinimo = 5,
    )

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
fun ProductoCardNormalPreview() {
    MaterialTheme { ProductoCard(producto = productoNormal, onClick = {}) }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
fun ProductoCardStockBajoPreview() {
    MaterialTheme { ProductoCard(producto = productoStockBajo, onClick = {}) }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
fun ProductoCardSinStockPreview() {
    MaterialTheme { ProductoCard(producto = productoSinStock, onClick = {}) }
}
