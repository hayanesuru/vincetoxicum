import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import net.fabricmc.classtweaker.api.ClassTweaker
import net.fabricmc.classtweaker.impl.ClassTweakerImpl
import net.fabricmc.classtweaker.reader.ClassTweakerReaderImpl
import net.fabricmc.fernflower.api.IFabricJavadocProvider
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.java.decompiler.api.Decompiler
import org.jetbrains.java.decompiler.main.DecompilerContext
import org.jetbrains.java.decompiler.main.decompiler.SingleFileSaver
import org.jetbrains.java.decompiler.main.extern.IContextSource
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences
import org.jetbrains.java.decompiler.main.extern.IResultSaver
import org.jetbrains.java.decompiler.struct.StructClass
import org.jetbrains.java.decompiler.struct.StructField
import org.jetbrains.java.decompiler.struct.StructMethod
import org.jetbrains.java.decompiler.util.InterpreterUtil
import org.jetbrains.java.decompiler.util.ZipFileCache
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.AutoCloseable
import java.nio.charset.StandardCharsets
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import java.util.jar.Manifest as Manifest1

buildscript {
    repositories {
        maven {
            name = "Fabric Maven"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
    }

    dependencies {
        classpath(libs.asm)
        classpath(libs.gson)
        classpath(libs.mappingio)
        classpath(libs.stitch)
        classpath(libs.vineflower)
        classpath(libs.classtweaker)
        classpath(libs.unpick)
    }
}

val minecraftVersion = "26.2"
val serverUrl = "https://piston-data.mojang.com/v1/objects/823e2250d24b3ddac457a60c92a6a941943fcd6a/server.jar"
val serverChecksum = "823e2250d24b3ddac457a60c92a6a941943fcd6a"
val serverJar: Provider<RegularFile> = layout.buildDirectory.file("$minecraftVersion.jar")
val serverDir: Provider<Directory> = layout.buildDirectory.dir("$minecraftVersion/artifacts")
val serverSrc: Provider<RegularFile> = layout.buildDirectory.file("$minecraftVersion/src.jar")
val serverDestJava: Directory = project(":server").layout.projectDirectory.dir("src/main/java")
val serverDestResources: Directory = project(":server").layout.projectDirectory.dir("src/main/resources")
val ats: Directory = layout.projectDirectory.dir("at")

plugins {
    `maven-publish`
    `java-library`
    eclipse
    idea
    id("de.undercouch.download") version "5.6.0"
}

version = "${minecraftVersion}+build.${System.getenv().getOrDefault("BUILD_NUMBER", "local")}"
group = "hayanesuru.vincetoxicum"

val downloadServerJar = tasks.register<Download>("downloadServerJar") {
    src(serverUrl)
    dest(serverJar)
    onlyIfModified(true)
}

val verifyServerJar = tasks.register<Verify>("verifyServerJar") {
    dependsOn(downloadServerJar)
    src(serverJar)
    algorithm("SHA1")
    checksum(serverChecksum)
}

val extractServerJar = tasks.register<Copy>("extractServerJar") {
    dependsOn(verifyServerJar)
    from(zipTree(serverJar))
    into(serverDir)
}

val decompileServer: TaskProvider<Task> = tasks.register("decompileServer") {
    dependsOn(extractServerJar)
    doLast {
        val ct = ClassTweakerImpl()
        val reader = ClassTweakerReaderImpl(ct)
        ats.asFileTree.filter { it.isFile }.forEach {
            reader.read(it.bufferedReader(), "named")
        }
        val saver = FileSaverImpl(serverSrc.get().asFile)
        val b = Decompiler.Builder()
            .option(IFernflowerPreferences.INDENT_STRING, "    ")
            .option(IFernflowerPreferences.DECOMPILE_INNER, true)
            .option(IFernflowerPreferences.REMOVE_BRIDGE, true)
            .option(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, true)
            .option(IFernflowerPreferences.REMOVE_SYNTHETIC, true)
            .option(IFernflowerPreferences.ASCII_STRING_CHARACTERS, true)
            .option(IFernflowerPreferences.PREFERRED_LINE_LENGTH, "120")
            .option(IFabricJavadocProvider.PROPERTY_NAME, DocProviderImpl())
            .option(IFernflowerPreferences.WARN_INCONSISTENT_INNER_CLASSES, false)
            .option(IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, true)
            .option(IFernflowerPreferences.INLINE_SIMPLE_LAMBDAS, false)
            .option(IFernflowerPreferences.INCLUDE_JAVA_RUNTIME, true)
            .option(IFernflowerPreferences.VERIFY_VARIABLE_MERGES, true)
            .logger(LoggerImpl(logger))
            .output(saver)
        fileTree(serverDir.get().dir("META-INF/libraries"))
            .filter { it.isFile }
            .forEach {
                val n = it.name
                if (n.startsWith("authlib")
                    || n.startsWith("brigadier")
                    || n.startsWith("datafixerupper")
                ) {
                    b.inputs(SourceImpl(ZipFile(it), ct))
                } else {
                    b.libraries(it)
                }
            }
        fileTree(serverDir.get().dir("META-INF/versions"))
            .filter { it.isFile }
            .forEach {
                b.inputs(SourceImpl(ZipFile(it), ct))
            }
        if (serverSrc.get().asFile.isFile) {
            serverSrc.get().asFile.delete()
        }
        b.build().decompile()
    }
}

private class DocProviderImpl : IFabricJavadocProvider {
    override fun getClassDoc(structClass: StructClass?): String? {
        return null
    }

    override fun getFieldDoc(
        structClass: StructClass?,
        structField: StructField?
    ): String? {
        return null
    }

    override fun getMethodDoc(
        structClass: StructClass?,
        structMethod: StructMethod?
    ): String? {
        return null
    }
}

private class FileSaverImpl(private val target: File) : IResultSaver, AutoCloseable {
    private var output: ZipOutputStream? = null
    private var isJar = false
    private val entries: HashSet<String> = HashSet()
    private val openZips = ZipFileCache()

    init {
        check(!target.isDirectory()) { "Trying to save " + target.absolutePath + " as a file but there's a directory there already!" }
    }

    override fun saveFolder(path: String?) {
    }

    override fun copyFile(source: String, path: String?, entryName: String) {
        if (!checkEntry(entryName)) return

        try {
            output!!.putNextEntry(ZipEntry(entryName))
            InterpreterUtil.copyStream(FileInputStream(source), output)
        } catch (ex: IOException) {
            val message = "Cannot write entry $entryName to $target"
            DecompilerContext.getLogger().writeMessage(message, ex)
        }
    }

    override fun saveClassFile(
        path: String?,
        qualifiedName: String?,
        entryName: String,
        content: String?,
        mapping: IntArray?
    ) {
        if (!checkEntry(entryName)) return

        try {
            output!!.putNextEntry(ZipEntry(entryName))

            content?.let { output!!.write(it.toByteArray(StandardCharsets.UTF_8)) }
        } catch (ex: IOException) {
            val message = "Cannot write entry $entryName to $target"
            DecompilerContext.getLogger().writeMessage(message, ex)
        }
    }

    override fun createArchive(path: String, archiveName: String, manifest: Manifest1?) {
        if (output != null) return
        try {
            val stream = FileOutputStream(target)
            isJar = manifest != null
            output = if (isJar) JarOutputStream(stream, manifest) else ZipOutputStream(stream)
        } catch (e: IOException) {
            DecompilerContext.getLogger().writeMessage("Cannot create archive $target", e)
        }
    }

    override fun saveDirEntry(path: String?, archiveName: String?, entryName: String?) {
    }

    override fun copyEntry(source: String?, path: String?, archiveName: String?, entryName: String) {
        if (!checkEntry(entryName)) return

        try {
            val srcArchive: ZipFile = this.openZips.get(source)
            val entry: ZipEntry? = srcArchive.getEntry(entryName)
            if (entry != null) {
                if (isJar && MANIFEST == entryName) {
                    return
                }

                srcArchive.getInputStream(entry).use {
                    output!!.putNextEntry(ZipEntry(entryName))
                    InterpreterUtil.copyStream(it, output)
                }
            }
        } catch (ex: IOException) {
            val message = "Cannot copy entry $entryName from $source to $target"
            DecompilerContext.getLogger().writeMessage(message, ex)
        }
    }

    override fun saveClassEntry(
        path: String?,
        archiveName: String?,
        qualifiedName: String?,
        entryName: String,
        content: String?
    ) {
        this.saveClassEntry(path, archiveName, qualifiedName, entryName, content, null)
    }

    override fun saveClassEntry(
        path: String?,
        archiveName: String?,
        qualifiedName: String?,
        entryName: String,
        content: String?,
        mapping: IntArray?
    ) {
        if (!checkEntry(entryName)) return

        try {
            val entry = ZipEntry(entryName)
            if (mapping != null && DecompilerContext.getOption(IFernflowerPreferences.DUMP_CODE_LINES)) entry.setExtra(
                this.getCodeLineData(mapping)
            )
            output!!.putNextEntry(entry)
            content?.let { output!!.write(it.toByteArray(StandardCharsets.UTF_8)) }
        } catch (ex: IOException) {
            val message = "Cannot write entry $entryName to $target"
            DecompilerContext.getLogger().writeMessage(message, ex)
        }
    }

    override fun closeArchive(path: String?, archiveName: String?) {
        // try {
        //     if (this.output != null) {
        //         output!!.close()
        //         entries.clear()
        //         output = null
        //     }
        // } catch (_: IOException) {
        //     DecompilerContext.getLogger().writeMessage("Cannot close $target", IFernflowerLogger.Severity.WARN)
        // }
    }

    private fun checkEntry(entryName: String): Boolean {
        val added = entries.add(entryName)
        if (!added) {
            val message = "Zip entry $entryName already exists in $target"
            DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.WARN)
        }
        return added
    }

    @Throws(IOException::class)
    override fun close() {
        output?.close()
        this.openZips.close()
    }

    companion object {
        const val MANIFEST: String = "META-INF/MANIFEST.MF"
    }
}

private class LoggerImpl(private val logger: Logger) : IFernflowerLogger() {

    override fun writeMessage(
        message: String,
        severity: IFernflowerLogger.Severity
    ) {
        logger.log(
            when (severity) {
                Severity.TRACE -> LogLevel.DEBUG
                Severity.INFO -> LogLevel.INFO
                Severity.WARN -> LogLevel.WARN
                Severity.ERROR -> LogLevel.ERROR
            }, message)
    }

    override fun writeMessage(
        message: String,
        severity: IFernflowerLogger.Severity,
        t: Throwable?
    ) {
        logger.log(
            when (severity) {
                Severity.TRACE -> LogLevel.DEBUG
                Severity.INFO -> LogLevel.INFO
                Severity.WARN -> LogLevel.WARN
                Severity.ERROR -> LogLevel.ERROR
            }, message, t)
    }
}

private class SourceImpl(private val file: ZipFile, private val ct: ClassTweaker, private val relativePath: String = "") : IContextSource, AutoCloseable {

    override fun getName(): String {
        return "archive " + file.name
    }

    override fun getEntries(): IContextSource.Entries {
        val classes: MutableList<IContextSource.Entry> = ArrayList()
        val directories: MutableSet<String> = HashSet()
        val others: MutableList<IContextSource.Entry> = ArrayList()
        val entries = file.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val name = entry.name
            this.addDirectories(entry, directories)
            if (!entry.isDirectory) {
                if (name.endsWith(".class")) {
                    classes.add(IContextSource.Entry.parse(name.dropLast(".class".length)))
                } else {
                    others.add(IContextSource.Entry.parse(name))
                }
            }
        }

        return IContextSource.Entries(classes, directories.toList(), others, listOf())
    }

    private fun addDirectories(entry: ZipEntry, directories: MutableSet<String>) {
        val name = entry.name

        var segmentIndex = name.indexOf(Char(47))
        while (segmentIndex != -1) {
            directories.add(name.take(segmentIndex))
            segmentIndex = name.indexOf(Char(47), segmentIndex + 1)
        }

        if (entry.isDirectory) {
            directories.add(name)
        }
    }

    @Throws(IOException::class)
    override fun getInputStream(resource: String): InputStream {
        val entry = file.getEntry(resource) ?: throw IOException("Resource not found: $resource")
        return file.getInputStream(entry)
    }

    override fun getInputStream(resource: IContextSource.Entry): InputStream {
        return this.getInputStream(resource.path())
    }

    @Throws(IOException::class)
    override fun getClassBytes(className: String?): ByteArray {
        this.getInputStream("$className.class").use {
            val base = it.readAllBytes()
            val classReader = ClassReader(base)
            val cw = ClassWriter(classReader, 0)
            val cv = ct.createClassVisitor(Opcodes.ASM9, cw, null)
            classReader.accept(cv, 0)
            return cw.toByteArray()
        }
    }

    override fun createOutputSink(saver: IResultSaver): IContextSource.IOutputSink {
        val archiveName = file.name
        return object : IContextSource.IOutputSink {
            override fun begin() {
                val potentialManifest = file.getEntry(SingleFileSaver.MANIFEST)
                var manifest: Manifest1? = null

                if (potentialManifest != null) {
                    try {
                        file.getInputStream(potentialManifest).use {
                            manifest = Manifest1(it)
                        }
                    } catch (ex: IOException) {
                        DecompilerContext.getLogger()
                            .writeMessage("Failed to read manifest from $file", IFernflowerLogger.Severity.ERROR, ex)
                    }
                }
                saver.saveFolder(this@SourceImpl.relativePath)
                saver.createArchive(relativePath, archiveName, manifest)
            }

            override fun acceptOther(path: String) {
                saver.copyEntry(
                    this@SourceImpl.file.name,
                    this@SourceImpl.relativePath, archiveName, path
                )
            }

            override fun acceptDirectory(directory: String) {
                saver.saveDirEntry(this@SourceImpl.relativePath, archiveName, directory)
            }

            override fun acceptClass(qualifiedName: String, fileName: String, content: String, mapping: IntArray?) {
                saver.saveClassEntry(
                    this@SourceImpl.relativePath,
                    this@SourceImpl.file.name, qualifiedName, fileName, content, mapping
                )
            }

            @Throws(IOException::class)
            override fun close() {
                saver.closeArchive(this@SourceImpl.relativePath, archiveName)
            }
        }
    }

    @Throws(IOException::class)
    override fun close() {
        file.close()
    }
}

tasks.register<Sync>("syncSrc") {
    outputs.upToDateWhen { false }

    from(zipTree(serverSrc).matching {
        include { it.path.startsWith("net") || it.path.startsWith("com") }
    })
    into(serverDestJava)
}

tasks.register<Sync>("syncData") {
    outputs.upToDateWhen { false }

    from(zipTree(serverSrc).matching {
        include { it.path.startsWith("data") || it.path.startsWith("assets") || it.path.startsWith("version") }
    })
    into(serverDestResources)
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = Charsets.UTF_8.name()
    options.compilerArgs.add("--add-modules=jdk.incubator.vector")
}

tasks.withType<Javadoc> {
    options.encoding = Charsets.UTF_8.name()
}

tasks.withType<ProcessResources> {
    filteringCharset = Charsets.UTF_8.name()
}

tasks.withType<Test> {
    testLogging {
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events(TestLogEvent.STANDARD_OUT)
    }
}
