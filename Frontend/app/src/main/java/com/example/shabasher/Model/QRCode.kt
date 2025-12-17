import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.ImageBitmap

@Composable
fun QRCode(
    text: String,
    sizeDp: Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    val qrBitmap = remember(text) { createQrBitmap(text) }

    qrBitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "QR code",
            modifier = modifier.size(sizeDp),
            contentScale = ContentScale.Fit
        )
    }
}

fun createQrBitmap(text: String, pixels: Int = 512): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, pixels, pixels)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                // android.graphics.Color.BLACK/WHITE возвращают Int ARGB
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
