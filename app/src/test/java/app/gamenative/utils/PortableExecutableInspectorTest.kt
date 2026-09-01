package app.gamenative.utils

import java.io.File
import java.io.RandomAccessFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PortableExecutableInspectorTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun inspect_readsArchitectureImportsDelayImportsAndExecutableReferences() {
        val fixture = File(temporaryFolder.root, "launcher.exe")
        writeMinimalPe(
            fixture,
            imports = listOf("KERNEL32.dll", "dxgi.dll"),
            delayImports = listOf("d3d12.dll"),
            executableReferences = listOf("GTA5.exe"),
        )

        val info = PortableExecutableInspector.inspect(fixture)

        assertEquals(0x8664, info.machine)
        assertTrue(info.is64Bit)
        assertEquals(2, info.subsystem)
        assertEquals(listOf("KERNEL32.dll", "dxgi.dll"), info.imports)
        assertEquals(listOf("d3d12.dll"), info.delayImports)
        assertEquals(listOf("GTA5.exe"), info.referencedExecutables)
    }

    @Test(expected = PortableExecutableInspector.FormatException::class)
    fun inspect_rejectsMalformedFile() {
        val fixture = File(temporaryFolder.root, "broken.exe").apply { writeText("not a PE") }
        PortableExecutableInspector.inspect(fixture)
    }

    companion object {
        internal fun writeMinimalPe(
            file: File,
            machine: Int = 0x8664,
            imports: List<String> = emptyList(),
            delayImports: List<String> = emptyList(),
            executableReferences: List<String> = emptyList(),
        ) {
            val pe32Plus = machine == 0x8664
            val optionalSize = if (pe32Plus) 0xf0 else 0xe0
            val optionalMagic = if (pe32Plus) 0x20b else 0x10b
            val directoryOffset = if (pe32Plus) 112 else 96
            val optional = 0x98
            val rawOffset = 0x200
            val sectionRva = 0x1000
            val data = ByteArray(0x2400)

            fun putShort(offset: Int, value: Int) {
                data[offset] = value.toByte()
                data[offset + 1] = (value ushr 8).toByte()
            }

            fun putInt(offset: Int, value: Int) {
                repeat(4) { data[offset + it] = (value ushr (it * 8)).toByte() }
            }

            "MZ".toByteArray().copyInto(data, 0)
            putInt(0x3c, 0x80)
            byteArrayOf('P'.code.toByte(), 'E'.code.toByte(), 0, 0).copyInto(data, 0x80)
            putShort(0x84, machine)
            putShort(0x86, 1)
            putShort(0x94, optionalSize)
            putShort(optional, optionalMagic)
            putShort(optional + 68, 2)
            putInt(optional + if (pe32Plus) 108 else 92, 16)

            val section = optional + optionalSize
            ".rdata".toByteArray().copyInto(data, section)
            putInt(section + 8, 0x2000)
            putInt(section + 12, sectionRva)
            putInt(section + 16, 0x2000)
            putInt(section + 20, rawOffset)

            var cursor = 0x600
            fun storeName(value: String): Int {
                val bytes = value.toByteArray() + byteArrayOf(0)
                bytes.copyInto(data, cursor)
                val rva = sectionRva + cursor - rawOffset
                cursor += bytes.size
                return rva
            }

            if (imports.isNotEmpty()) {
                val table = 0x300
                putInt(optional + directoryOffset + 8, sectionRva + table - rawOffset)
                putInt(optional + directoryOffset + 12, 20 * (imports.size + 1))
                imports.forEachIndexed { index, value ->
                    putInt(table + index * 20 + 12, storeName(value))
                }
            }
            if (delayImports.isNotEmpty()) {
                val table = 0x480
                putInt(optional + directoryOffset + 13 * 8, sectionRva + table - rawOffset)
                putInt(optional + directoryOffset + 13 * 8 + 4, 32 * (delayImports.size + 1))
                delayImports.forEachIndexed { index, value ->
                    putInt(table + index * 32, 1)
                    putInt(table + index * 32 + 4, storeName(value))
                }
            }
            executableReferences.forEach { value ->
                val bytes = value.toByteArray() + byteArrayOf(0)
                bytes.copyInto(data, cursor)
                cursor += bytes.size
            }
            RandomAccessFile(file, "rw").use { it.write(data) }
        }
    }
}
