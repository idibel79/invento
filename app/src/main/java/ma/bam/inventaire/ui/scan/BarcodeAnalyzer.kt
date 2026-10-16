package ma.bam.inventaire.ui.scan

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.math.abs

/**
 * Analyseur CameraX qui délègue la détection de codes-barres à ML Kit (traitement 100% local).
 * [onBarcodeDetected] n'est pas rappelé plus d'une fois toutes les [minIntervalMs] pour éviter
 * les scans en rafale du même code pendant que la caméra reste pointée dessus.
 *
 * Seuls les codes-barres situés près du centre vertical de l'image sont retenus (voir
 * [isNearScanLine]), pour correspondre au trait de visée affiché à l'écran et éviter de lire
 * n'importe quel code-barres visible ailleurs dans le cadre.
 */
class BarcodeAnalyzer(
    private val minIntervalMs: Long = 1500L,
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Seuls les formats des produits (EAN-13 / EAN-8, UPC-A, Code 128) : les autres formats
    // (UPC-E, ITF, Codabar...) produisaient de faux codes en lisant une partie d'un EAN.
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_CODE_128
            )
            .build()
    )
    private var lastDetectedValue: String? = null
    private var lastDetectedAt: Long = 0L

    // Lecture candidate : un code n'est retenu qu'après avoir été lu identique sur 2 images
    // proches, ce qui écarte les lectures erronées isolées.
    private var candidateValue: String? = null
    private var candidateAt: Long = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)
        val imageWidth = imageProxy.width
        val imageHeight = imageProxy.height

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                handleResults(barcodes, rotationDegrees, imageWidth, imageHeight)
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun handleResults(
        barcodes: List<Barcode>,
        rotationDegrees: Int,
        imageWidth: Int,
        imageHeight: Int
    ) {
        val value = barcodes.firstOrNull { barcode ->
            !barcode.rawValue.isNullOrBlank() &&
                isNearScanLine(barcode.boundingBox, rotationDegrees, imageWidth, imageHeight)
        }?.rawValue ?: return

        val now = System.currentTimeMillis()
        if (value != candidateValue || now - candidateAt > CONFIRM_WINDOW_MS) {
            candidateValue = value
            candidateAt = now
            return
        }
        candidateAt = now
        if (value == lastDetectedValue && now - lastDetectedAt < minIntervalMs) {
            return
        }
        lastDetectedValue = value
        lastDetectedAt = now
        onBarcodeDetected(value)
    }

    /**
     * ML Kit renvoie le rectangle du code-barres dans le repère de l'image déjà remise à
     * l'endroit (après application de [rotationDegrees]) : ses dimensions sont donc celles du
     * buffer capteur avec largeur/hauteur permutées quand la rotation vaut 90° ou 270°. Comme la
     * preview recadre l'image en son centre (center-crop), le centre de cette image correspond
     * toujours au centre affiché à l'écran, quel que soit le ratio d'aspect. On vérifie donc que
     * le centre vertical du code-barres est proche du centre vertical de l'image remise à l'endroit.
     */
    private fun isNearScanLine(
        box: Rect?,
        rotationDegrees: Int,
        imageWidth: Int,
        imageHeight: Int
    ): Boolean {
        if (box == null) return false
        val uprightHeight = if (rotationDegrees == 90 || rotationDegrees == 270) imageWidth else imageHeight
        val fraction = box.exactCenterY() / uprightHeight
        return abs(fraction - 0.5f) <= SCAN_LINE_TOLERANCE_FRACTION
    }

    companion object {
        /** Demi-largeur (en fraction de la dimension totale) de la bande acceptée autour du trait de visée.
         * Couvre la fenêtre de visée sans déborder : un code-barres dans le cadre est lu même s'il n'est pas
         * pile sur le trait, un code-barres hors du cadre ne l'est pas. */
        private const val SCAN_LINE_TOLERANCE_FRACTION = 0.09f

        /** Délai max entre les 2 lectures identiques qui confirment un code. */
        private const val CONFIRM_WINDOW_MS = 700L
    }
}
