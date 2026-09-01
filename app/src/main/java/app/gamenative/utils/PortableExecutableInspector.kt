package app.gamenative.utils

import java.io.EOFException
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * Bounded, dependency-free PE metadata reader used by the offline launch analyzer.
 *
 * Import tables are read through [RandomAccessFile] instead of loading a whole game binary into
 * the Android heap. Executable-name references are discovered with a streaming ASCII scan so a
 * launcher can be linked to the runtime it explicitly starts.
 */
object PortableExecutableInspector {
    private const val MAX_SECTIONS = 96
    private const val MAX_IMPORT_DESCRIPTORS = 4096
    private const val MAX_STRING_BYTES = 512
    private const val REFERENCE_CHUNK_BYTES = 64 * 1024
    private const val REFERENCE_OVERLAP_BYTES = 512

    data class Info(
        val machine: Int,
        val subsystem: Int,
        val imports: List<String>,
        val delayImports: List<String>,
        val referencedExecutables: List<String>,
    ) {
        val is64Bit: Boolean
            get() = machine == 0x8664
    }

    class FormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

    private data class Section(
        val virtualAddress: Long,
        val virtualSize: Long,
        val rawOffset: Long,
        val rawSize: Long,
    )

    fun inspect(file: File): Info {
        try {
            RandomAccessFile(file, "r").use { input ->
                if (input.length() < 0x40 || input.readU16(0) != 0x5a4d) {
                    throw FormatException("missing MZ signature")
                }
                val peOffset = input.readU32(0x3c)
                if (peOffset + 24 > input.length() || input.readU32(peOffset) != 0x00004550L) {
                    throw FormatException("missing PE signature")
                }

                val fileHeader = peOffset + 4
                val machine = input.readU16(fileHeader)
                val sectionCount = input.readU16(fileHeader + 2)
                if (sectionCount !in 1..MAX_SECTIONS) throw FormatException("invalid PE section count")
                val optionalSize = input.readU16(fileHeader + 16)
                val optional = fileHeader + 20
                val magic = input.readU16(optional)
                val directoryOffset: Int
                val directoryCountOffset: Int
                val imageBase: Long
                when (magic) {
                    0x20b -> {
                        directoryOffset = 112
                        directoryCountOffset = 108
                        imageBase = input.readU64(optional + 24)
                    }

                    0x10b -> {
                        directoryOffset = 96
                        directoryCountOffset = 92
                        imageBase = input.readU32(optional + 28)
                    }

                    else -> throw FormatException("unsupported optional-header magic 0x${magic.toString(16)}")
                }
                if (optionalSize < directoryOffset) throw FormatException("optional header is too small")

                val subsystem = input.readU16(optional + 68)
                val directoryCount = input.readU32(optional + directoryCountOffset).toInt()
                val sectionTable = optional + optionalSize
                val sections = buildList {
                    repeat(sectionCount) { index ->
                        val header = sectionTable + index * 40L
                        add(
                            Section(
                                virtualSize = input.readU32(header + 8),
                                virtualAddress = input.readU32(header + 12),
                                rawSize = input.readU32(header + 16),
                                rawOffset = input.readU32(header + 20),
                            ),
                        )
                    }
                }

                fun directory(index: Int): Pair<Long, Long> {
                    if (directoryCount <= index || directoryOffset + (index + 1) * 8 > optionalSize) {
                        return 0L to 0L
                    }
                    val entry = optional + directoryOffset + index * 8L
                    return input.readU32(entry) to input.readU32(entry + 4)
                }

                val imports = readImports(input, directory(1), sections)
                val delayImports = readDelayImports(input, directory(13), sections, imageBase)
                return Info(
                    machine = machine,
                    subsystem = subsystem,
                    imports = imports,
                    delayImports = delayImports,
                    referencedExecutables = scanExecutableReferences(input),
                )
            }
        } catch (error: FormatException) {
            throw error
        } catch (error: Exception) {
            throw FormatException("unable to inspect ${file.name}", error)
        }
    }

    private fun readImports(
        input: RandomAccessFile,
        directory: Pair<Long, Long>,
        sections: List<Section>,
    ): List<String> {
        val (rva, size) = directory
        if (rva == 0L || size == 0L) return emptyList()
        val table = rvaToOffset(rva, sections, input.length())
        val descriptorCount = minOf(MAX_IMPORT_DESCRIPTORS, maxOf(1, (size / 20 + 1).toInt()))
        val result = linkedSetOf<String>()
        repeat(descriptorCount) { index ->
            val descriptor = table + index * 20L
            val values = LongArray(5) { field -> input.readU32(descriptor + field * 4L) }
            if (values.all { it == 0L }) return result.toList()
            val nameRva = values[3]
            if (nameRva != 0L) result += input.readCString(rvaToOffset(nameRva, sections, input.length()))
        }
        return result.toList()
    }

    private fun readDelayImports(
        input: RandomAccessFile,
        directory: Pair<Long, Long>,
        sections: List<Section>,
        imageBase: Long,
    ): List<String> {
        val (rva, size) = directory
        if (rva == 0L || size == 0L) return emptyList()
        val table = rvaToOffset(rva, sections, input.length())
        val descriptorCount = minOf(MAX_IMPORT_DESCRIPTORS, maxOf(1, (size / 32 + 1).toInt()))
        val result = linkedSetOf<String>()
        repeat(descriptorCount) { index ->
            val descriptor = table + index * 32L
            val values = LongArray(8) { field -> input.readU32(descriptor + field * 4L) }
            if (values.all { it == 0L }) return result.toList()
            val attributes = values[0]
            val nameValue = values[1]
            if (nameValue != 0L) {
                val nameRva = if (attributes and 1L != 0L) nameValue else nameValue - imageBase
                result += input.readCString(rvaToOffset(nameRva, sections, input.length()))
            }
        }
        return result.toList()
    }

    private fun scanExecutableReferences(input: RandomAccessFile): List<String> {
        val pattern = Regex("[A-Za-z0-9_./\\\\ +()\\[\\]-]{1,180}\\.exe", RegexOption.IGNORE_CASE)
        val result = linkedSetOf<String>()
        val buffer = ByteArray(REFERENCE_CHUNK_BYTES)
        var carry = ByteArray(0)
        input.seek(0)
        while (true) {
            val count = input.read(buffer)
            if (count <= 0) break
            val combined = carry + buffer.copyOf(count)
            val text = String(combined, StandardCharsets.ISO_8859_1)
            pattern.findAll(text).forEach { match ->
                val value = match.value.trim().replace('\\', '/').substringAfterLast('/')
                if (value.isNotBlank()) result += value
            }
            carry = combined.copyOfRange(maxOf(0, combined.size - REFERENCE_OVERLAP_BYTES), combined.size)
        }
        return result.toList()
    }

    private fun rvaToOffset(rva: Long, sections: List<Section>, fileSize: Long): Long {
        for (section in sections) {
            val span = maxOf(section.virtualSize, section.rawSize)
            if (rva >= section.virtualAddress && rva < section.virtualAddress + span) {
                val offset = section.rawOffset + rva - section.virtualAddress
                if (offset in 0 until fileSize) return offset
            }
        }
        if (rva in 0 until fileSize) return rva
        throw FormatException("RVA 0x${rva.toString(16)} is outside file sections")
    }

    private fun RandomAccessFile.readU16(offset: Long): Int {
        requireRange(offset, 2)
        seek(offset)
        val first = read()
        val second = read()
        return first or (second shl 8)
    }

    private fun RandomAccessFile.readU32(offset: Long): Long {
        requireRange(offset, 4)
        seek(offset)
        var value = 0L
        repeat(4) { value = value or (read().toLong() shl (it * 8)) }
        return value
    }

    private fun RandomAccessFile.readU64(offset: Long): Long {
        requireRange(offset, 8)
        seek(offset)
        var value = 0L
        repeat(8) { value = value or (read().toLong() shl (it * 8)) }
        return value
    }

    private fun RandomAccessFile.readCString(offset: Long): String {
        requireRange(offset, 1)
        seek(offset)
        val bytes = ArrayList<Byte>()
        repeat(MAX_STRING_BYTES) {
            val value = read()
            if (value < 0) throw EOFException()
            if (value == 0) return String(bytes.toByteArray(), StandardCharsets.US_ASCII)
            bytes += value.toByte()
        }
        throw FormatException("unterminated PE string")
    }

    private fun RandomAccessFile.requireRange(offset: Long, size: Int) {
        if (offset < 0 || offset + size > length()) throw EOFException("truncated PE at 0x${offset.toString(16)}")
    }
}
