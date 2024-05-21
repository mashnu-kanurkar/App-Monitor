import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.redwater.appmonitor.logger.Logger


@Composable
fun GIFImageLoader(modifier: Modifier = Modifier, context: Context, @DrawableRes resourceId: Int) {
    val TAG = "GIFImageLoader"
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White),
        factory = {
            val imageView = ImageView(context)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            imageView.layoutParams = layoutParams
            imageView
        },
        update = {
            Logger.d(TAG, "Image inflated")
            Glide.with(context)
                .load(resourceId)
                .into(it)
        }
    )
}