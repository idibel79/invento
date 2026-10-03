package ma.bam.inventaire.ui.scan

import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    torchEnabled: Boolean = false,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) { PreviewView(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        BarcodeAnalyzer(onBarcodeDetected = onBarcodeDetected)
                    )
                }

            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )
        }
        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProviderFuture.get().unbindAll()
        }
    }

    LaunchedEffect(camera, torchEnabled) {
        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            camera?.cameraControl?.enableTorch(torchEnabled)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )
        ScanWindowOverlay(modifier = Modifier.fillMaxSize())
    }
}

/**
 * Fenêtre de visée façon scanner natif : zone assombrie tout autour, coins verts délimitant la
 * zone de scan, et trait rouge clignotant au centre. Seuls les codes-barres proches de ce trait
 * sont lus (voir [BarcodeAnalyzer.isNearScanLine]).
 */
@Composable
private fun ScanWindowOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLineBlink")
    val lineAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineAlpha"
    )

    Canvas(modifier = modifier) {
        val windowWidth = size.width * 0.82f
        val windowHeight = windowWidth / 1.7f
        val left = (size.width - windowWidth) / 2f
        val top = (size.height - windowHeight) / 2f
        val cornerRadius = 16.dp.toPx()

        drawDimmedScrim(
            windowRect = Rect(left, top, left + windowWidth, top + windowHeight),
            cornerRadius = cornerRadius
        )
        drawCorners(
            left = left,
            top = top,
            right = left + windowWidth,
            bottom = top + windowHeight
        )

        val lineY = top + windowHeight / 2f
        drawLine(
            color = Color.Red.copy(alpha = lineAlpha),
            start = Offset(left + 12.dp.toPx(), lineY),
            end = Offset(left + windowWidth - 12.dp.toPx(), lineY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawDimmedScrim(windowRect: Rect, cornerRadius: Float) {
    drawIntoCanvas { canvas ->
        val scrimPaint = Paint().apply { color = Color.Black.copy(alpha = 0.55f) }
        val layerBounds = Rect(Offset.Zero, size)
        canvas.saveLayer(layerBounds, scrimPaint)
        canvas.drawRect(layerBounds, scrimPaint)
        val clearPaint = Paint().apply { blendMode = BlendMode.Clear }
        canvas.drawRoundRect(
            left = windowRect.left,
            top = windowRect.top,
            right = windowRect.right,
            bottom = windowRect.bottom,
            radiusX = cornerRadius,
            radiusY = cornerRadius,
            paint = clearPaint
        )
        canvas.restore()
    }
}

private fun DrawScope.drawCorners(left: Float, top: Float, right: Float, bottom: Float) {
    val color = Color(0xFF34C759)
    val length = 22.dp.toPx()
    val stroke = 4.dp.toPx()

    fun corner(x: Float, y: Float, dx: Float, dy: Float) {
        drawLine(color, Offset(x, y), Offset(x + dx, y), stroke, StrokeCap.Round)
        drawLine(color, Offset(x, y), Offset(x, y + dy), stroke, StrokeCap.Round)
    }

    corner(left, top, length, length)
    corner(right, top, -length, length)
    corner(left, bottom, length, -length)
    corner(right, bottom, -length, -length)
}
