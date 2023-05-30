package expo.modules.pdfhelpers

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.core.Promise
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class PdfThumbnail(uri: String, width: Int, height: Int): Record {
  @Field var uri: String = uri

  @Field var width: Int = width

  @Field var height: Int = height
}

class ExpoPdfHelpersModule : Module() {
  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  override fun definition() = ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoPdfHelpers')` in JavaScript.
    Name("ExpoPdfHelpers")

    AsyncFunction("getPageCount") { filePath: String, promise: Promise ->
      var parcelFileDescriptor: ParcelFileDescriptor? = null
      var pdfRenderer: PdfRenderer? = null
      try {
        parcelFileDescriptor = getParcelFileDescriptor(filePath)
        if (parcelFileDescriptor !is ParcelFileDescriptor) {
          promise.reject("FILE_NOT_FOUND", "File $filePath not found")
        } else {
          pdfRenderer = PdfRenderer(parcelFileDescriptor)
          promise.resolve(pdfRenderer.pageCount)
        }
      } catch (ex: IOException) {
        promise.reject("INTERNAL_ERROR", ex)
      } finally {
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
      }
    }

    AsyncFunction("generateThumbnail") { filePath: String, page: Int, quality: Int, promise: Promise ->
      var parcelFileDescriptorThumbnail: ParcelFileDescriptor? = null
      var pdfRendererThumbnail: PdfRenderer? = null
      try {
        parcelFileDescriptorThumbnail = getParcelFileDescriptor(filePath)
        if (parcelFileDescriptorThumbnail !is ParcelFileDescriptor) {
          promise.reject("FILE_NOT_FOUND", "File $filePath not found")
        } else {
          pdfRendererThumbnail = PdfRenderer(parcelFileDescriptorThumbnail)
          if (page < 0 || page >= pdfRendererThumbnail.pageCount) {
            promise.reject("INVALID_PAGE", "Page number $page is invalid, file has ${pdfRendererThumbnail.pageCount} pages")
          }
  
          val result = renderPage(pdfRendererThumbnail, page, filePath, quality)
          promise.resolve(result)
        }
      } catch (ex: IOException) {
        promise.reject("INTERNAL_ERROR", ex)
      } finally {
        pdfRendererThumbnail?.close()
        parcelFileDescriptorThumbnail?.close()
      }
    }
  }

  private fun getParcelFileDescriptor(filePath: String): ParcelFileDescriptor? {
    val uri = Uri.parse(filePath)
    if (ContentResolver.SCHEME_CONTENT == uri.scheme || ContentResolver.SCHEME_FILE == uri.scheme) {
      return appContext.reactContext?.contentResolver?.openFileDescriptor(uri, "r")
    } else if (filePath.startsWith("/")) {
      val file = File(filePath)
      return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }
    return null
  }

  private fun renderPage(pdfRenderer: PdfRenderer, page: Int, filePath: String, quality: Int): PdfThumbnail {
    val currentPage = pdfRenderer.openPage(page)
    val width = currentPage.width
    val height = currentPage.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    currentPage.close()

    // Some bitmaps have transparent background which results in a black thumbnail. Add a white background.
    val bitmapWhiteBG = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    bitmapWhiteBG.eraseColor(Color.WHITE)
    val canvas = Canvas(bitmapWhiteBG)
    canvas.drawBitmap(bitmap, 0f, 0f, null)
    bitmap.recycle()

    val outputFile = File.createTempFile(getOutputFilePrefix(filePath, page), ".jpg", appContext.reactContext?.cacheDir)
    if (outputFile.exists()) {
      outputFile.delete()
    }
    val out = FileOutputStream(outputFile)
    bitmapWhiteBG.compress(Bitmap.CompressFormat.JPEG, quality, out)
    bitmapWhiteBG.recycle()
    out.flush()
    out.close()

    var map = PdfThumbnail(
      uri = Uri.fromFile(outputFile).toString(),
      width = width,
      height = height
    )

    return map
  }

  private fun getOutputFilePrefix(filePath: String, page: Int): String {
    val tokens = filePath.split("/")
    val originalFilename = tokens[tokens.lastIndex]
    val prefix = originalFilename.replace(".", "-")
    val generator = Random()
    val random = generator.nextInt(Integer.MAX_VALUE)
    return "$prefix-thumbnail-$page-$random"
  }
}