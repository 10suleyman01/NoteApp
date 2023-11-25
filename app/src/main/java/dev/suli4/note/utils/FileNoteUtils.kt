package dev.suli4.note.utils

import android.content.Context
import android.net.Uri
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

fun Context.saveImageToInternalStorage(uri: Uri): String? {
    // Создайте имя файла (или сохраните существующее имя)
    val fileName = "t_${System.currentTimeMillis()}.jpg"

    // Откройте поток вывода для нового файла во внутреннем хранилище приложения
    val fileOutputStream: FileOutputStream
    try {
        fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return null
    }

    // Откройте входной поток для URI
    val inputStream = contentResolver.openInputStream(uri)

    // Скопируйте данные из входного потока в выходной поток
    try {
        val buffer = ByteArray(4 * 1024) // 4 KB буфер
        var bytesRead: Int
        while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
            fileOutputStream.write(buffer, 0, bytesRead)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    } finally {
        // Закройте потоки после завершения операции
        inputStream?.close()
        fileOutputStream.close()
    }

    // Возвращаем полный путь к сохраненному файлу
    return getFileStreamPath(fileName)?.absolutePath
}

/**
 *
 * val savedImagePath = saveImageToInternalStorage(context, yourImageUri)
 * if (savedImagePath != null) {
 *     // Изображение успешно сохранено, можно использовать savedImagePath по вашему усмотрению
 * } else {
 *     // Произошла ошибка при сохранении изображения
 * }
 *
 * **/