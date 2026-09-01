package com.winlator.xenvironment;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import app.gamenative.BuildConfig;
import app.gamenative.R;
import app.gamenative.enums.Marker;
import app.gamenative.utils.ContainerUtils;
import app.gamenative.utils.GameFoldPcResourceRoot;
import app.gamenative.utils.MarkerUtils;
import app.gamenative.utils.downloader.ContainerFilesDownloaderKt;
import app.gamenative.utils.downloader.ProgressCallback;

// import com.winlator.MainActivity;
// import com.winlator.R;
// import com.winlator.SettingsFragment;
import com.winlator.PrefManager;
import com.winlator.container.Container;
import com.winlator.container.ContainerManager;
// import com.winlator.core.DownloadProgressDialog;
import com.winlator.contents.ContentProfile;
import com.winlator.contents.ContentsManager;
import com.winlator.core.Callback;
import com.winlator.core.FileUtils;
// import com.winlator.core.PreloaderDialog;
import com.winlator.core.TarCompressorUtils;
import com.winlator.core.WineInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ImageFsInstaller {
    public static final byte LATEST_VERSION = 28;

    private static void resetContainerImgVersions(Context context) {
        ContainerManager manager = new ContainerManager(context);
        for (Container container : manager.getContainers()) {
            String imgVersion = container.getExtra("imgVersion");
            String wineVersion = container.getWineVersion();
            if (!imgVersion.isEmpty() && WineInfo.isMainWineVersion(wineVersion) && Short.parseShort(imgVersion) <= 5) {
                container.putExtra("wineprefixNeedsUpdate", "t");
            }

            container.putExtra("imgVersion", null);
            container.putExtra("dxwrapper", null);
            container.putExtra("appVersion", null);
            container.saveData();
        }
    }

    public static void installWineFromAssets(final Context context, AssetManager assetManager) {
        String[] versions = context.getResources().getStringArray(R.array.bionic_wine_entries);
        File rootDir = ImageFs.find(context).getRootDir();
        for (String version : versions) {
            File outFile = new File(rootDir, "/opt/" + version);
            outFile.mkdirs();
            TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, assetManager, version + ".txz", outFile);
        }
    }

    public static void installWineFromDownloads(final Context context) {
        String[] versions = context.getResources().getStringArray(R.array.bionic_wine_entries);
        File rootDir = ImageFs.find(context).getRootDir();
        ImageFs imageFs = ImageFs.find(context);
        for (String version : versions) {
            File downloaded = GameFoldPcResourceRoot.resolve(version + ".txz");
            if (downloaded == null) {
                downloaded = new File(imageFs.getFilesDir(), version + ".txz");
            }
            if (!downloaded.isFile() || downloaded.length() == 0L) {
                Log.i("ImageFsInstaller", "No standalone Wine archive for " + version + "; imagefs already contains the base runtime");
                continue;
            }
            File outFile = new File(rootDir, "/opt/" + version);
            outFile.mkdirs();
            boolean extracted = TarCompressorUtils.extract(
                TarCompressorUtils.Type.XZ,
                downloaded,
                outFile
            );
            if (!extracted) {
                Log.e("ImageFsInstaller", "Failed to extract standalone Wine archive: " + downloaded.getAbsolutePath());
            }
        }
    }

    // Modern flavor ships an additional bionic preload shipped as a flat asset
    // (src/modern/assets/) until it's folded into redirect.tzst. Copy it next to
    // the tarball-extracted variant so BionicProgramLauncherComponent can find it
    private static void ensureBionicLib(Context context, File imagefs) {
        if (BuildConfig.MODERN_ANDROID) {
            File wxDest = new File(imagefs, "usr/lib/libredirect-bionic-wx.so");
            if (!wxDest.exists()) {
                FileUtils.copy(context, "libredirect-bionic-wx.so", wxDest);
                chmod(wxDest);
            }
        }
    }

    private static Future<Boolean> installFromAssetsFuture(
            final Context context,
            AssetManager assetManager,
            String containerVariant,
            String wineVersion,
            Callback<Integer> onProgress
    ) {
        return installFromAssetsFuture(context, assetManager, containerVariant, wineVersion, onProgress, true);
    }

    private static Future<Boolean> installFromAssetsFuture(
            final Context context,
            AssetManager assetManager,
            String containerVariant,
            String wineVersion,
            Callback<Integer> onProgress,
            boolean installDeferredGuestDependencies
    ) {
        // AppUtils.keepScreenOn(context);
        ImageFs imageFs = ImageFs.find(context);
        final File rootDir = imageFs.getRootDir();

        PrefManager.init(context);
        PrefManager.putString("current_box64_version", "");

        // final DownloadProgressDialog dialog = new DownloadProgressDialog(context);
        // dialog.show(R.string.installing_system_files);
        return Executors.newSingleThreadExecutor().submit(() -> {
            clearRootDir(context, rootDir);
            ensureSharedHomeRoot(context, rootDir);
            ensureProtonVersionSymlink(context, rootDir, wineVersion);
            ensureBionicLib(context, rootDir);

            final byte compressionRatio = 22;
            String imagefsFile = containerVariant.equals(Container.GLIBC) ? "imagefs_gamenative.txz" : "imagefs_bionic.txz";
            File downloaded = new File(imageFs.getFilesDir(), imagefsFile);


            boolean success = false;

            File externalImageFs = GameFoldPcResourceRoot.resolve(imagefsFile);
            if (externalImageFs != null) {
                Log.i("ImageFsInstaller", "Using external GamePadPC imagefs: " + externalImageFs.getAbsolutePath());
                final long contentLength = (long) (FileUtils.getSize(externalImageFs) * (100.0f / compressionRatio));
                AtomicLong totalSizeRef = new AtomicLong();
                success = TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, externalImageFs, rootDir, (file, size) -> {
                    if (size > 0 && onProgress != null) {
                        long totalSize = totalSizeRef.addAndGet(size);
                        onProgress.call((int) (((float) totalSize / Math.max(1L, contentLength)) * 100));
                    }
                    return file;
                });
            } else if (Arrays.asList(context.getAssets().list("")).contains(imagefsFile) == true){
                final long contentLength = (long) (FileUtils.getSize(assetManager, imagefsFile) * (100.0f / compressionRatio));
                AtomicLong totalSizeRef = new AtomicLong();
                Log.d("Extraction", "extracting " + imagefsFile);

                success = TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, assetManager, imagefsFile, rootDir, (file, size) -> {
                    if (size > 0) {
                        long totalSize = totalSizeRef.addAndGet(size);
                        if (onProgress != null) {
                            final int progress = (int) (((float) totalSize / contentLength) * 100);
                            onProgress.call(progress);
                        }
                    }
                    return file;
                });
            }

            else if (downloaded.exists()){
                final long contentLength = (long) (FileUtils.getSize(downloaded) * (100.0f / compressionRatio));
                AtomicLong totalSizeRef = new AtomicLong();
                Log.d("Extraction", "extracting " + imagefsFile);
                success = TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, downloaded, rootDir, (file, size) -> {
                    if (size > 0) {
                        long totalSize = totalSizeRef.addAndGet(size);
                        if (onProgress != null) {
                            final int progress = (int) (((float) totalSize / contentLength) * 100);
                            onProgress.call(progress);
                        }
                    }
                    return file;
                });
            }

            if (success) {
                Log.d("ImageFsInstaller", "Successfully installed system files");
                ContainerManager containerManager = new ContainerManager(context);

                // imagefs_bionic.txz is the complete base filesystem. The old
                // download path expected separate proton-9 archives in the
                // private files directory; after a data clear those files do
                // not exist and must never trigger a retry/download. Only
                // unpack standalone archives when one is actually present.
                if (externalImageFs == null) {
                    installWineFromDownloads(context);
                } else {
                    Log.i("ImageFsInstaller", "Skipping private Wine archive lookup; external imagefs contains the base runtime");
                }
                // The first-run GameFold shell gate installs only the shared
                // ImageFS + Proton core. redirect/extras are deliberately
                // deferred until an actual game container is created; the
                // legacy launch path keeps installing them here.
                boolean dependenciesReady = installBundledProton(context, assetManager);
                if (installDeferredGuestDependencies) {
                    dependenciesReady = installGuestLibs(context) && dependenciesReady;
                }
                if (!dependenciesReady) {
                    Log.e("ImageFsInstaller", "Shared runtime dependencies are incomplete");
                    return false;
                }
                imageFs.createVariantFile(containerVariant);
                imageFs.createImgVersionFile(LATEST_VERSION);
                resetContainerImgVersions(context);

            }
            else {
                Log.e("ImageFsInstaller", "Failed to install system files");
                if (downloaded.exists()) {
                    Log.w("ImageFsInstaller", "Deleting corrupt archive so next attempt re-downloads: " + downloaded.getPath());
                    downloaded.delete();
                }
            }
            return success;
            // dialog.closeOnUiThread();
        });
    }

    private static boolean installGuestLibs(Context ctx) {
        final String ASSET_TAR = "redirect.tzst";          // ➊  add this to assets/
        File imagefs = new File(ctx.getFilesDir(), "imagefs");
        // ➋  Unpack straight into imagefs, preserving relative paths.
        File externalRedirect = GameFoldPcResourceRoot.resolve(ASSET_TAR);
        boolean redirectExtracted;
        if (externalRedirect != null) {
            redirectExtracted = TarCompressorUtils.extract(
                    TarCompressorUtils.Type.ZSTD, externalRedirect, imagefs);
        } else {
            try (InputStream in  = ctx.getAssets().open(ASSET_TAR)) {
                redirectExtracted = TarCompressorUtils.extract(
                        TarCompressorUtils.Type.ZSTD, in, imagefs);
            } catch (IOException e) {
                Log.e("ImageFsInstaller", "redirect deploy failed", e);
                return false;
            }
        }
        if (!redirectExtracted) {
            Log.e("ImageFsInstaller", "redirect deploy failed: archive extraction returned false");
            return false;
        }

        // ➌  Make sure the new libs are world-readable / executable
        chmod(new File(imagefs, "usr/lib/libredirect.so"));
        chmod(new File(imagefs, "usr/lib/libredirect-bionic.so"));

        ensureBionicLib(ctx, imagefs);

        // Extract extras.tzst. The upstream modern flavor downloads this even though
        // the APK already contains the asset; the lazy build keeps the first launch offline.
        File externalExtras = GameFoldPcResourceRoot.resolve("extras.tzst");
        if (externalExtras != null) {
            if (!TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, externalExtras, imagefs)) {
                Log.e("ImageFsInstaller", "external extras deploy failed");
                return false;
            }
        } else if (BuildConfig.GAMEFOLD_PC_LAZY) {
            final String EXTRAS_TAR = "extras.tzst";
            try (InputStream in = ctx.getAssets().open(EXTRAS_TAR)) {
                if (!TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, in, imagefs)) {
                    Log.e("ImageFsInstaller", "bundled extras deploy failed: extraction returned false");
                    return false;
                }
            } catch (IOException e) {
                Log.e("ImageFsInstaller", "bundled extras deploy failed", e);
                return false;
            }
        } else if (app.gamenative.BuildConfig.MODERN_ANDROID) {
            try {
                // Modern variant: download and extract
                java.io.File extrasFile = ContainerFilesDownloaderKt.ensureContainerFileAvailableBlocking(
                    ctx,
                    "extras",
                    new ProgressCallback() {
                        @Override
                        public void onProgress(float progress) {
                            Log.d("ImageFsInstaller", "Downloading extras.tzst: " + (int)(progress * 100) + "%");
                        }
                    }
                );

                if (extrasFile != null && extrasFile.exists()) {
                    if (!TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, extrasFile, imagefs)) {
                        Log.e("ImageFsInstaller", "extras extraction returned false");
                        return false;
                    }
                } else {
                    Log.e("ImageFsInstaller", "Failed to download extras.tzst");
                    return false;
                }
            } catch (Exception e) {
                Log.e("ImageFsInstaller", "extras download/extract failed", e);
                return false;
            }
        } else {
            // Legacy variant: use bundled assets
            final String EXTRAS_TAR = "extras.tzst";
            try (InputStream in = ctx.getAssets().open(EXTRAS_TAR)) {
                if (!TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, in, imagefs)) {
                    Log.e("ImageFsInstaller", "extras deploy failed: extraction returned false");
                    return false;
                }
            } catch (IOException e) {
                Log.e("ImageFsInstaller", "extras deploy failed", e);
                return false;
            }
        }

        // ➌  Make sure the new libs are world-readable / executable
        chmod(new File(imagefs, "generate_interfaces_file.exe"));
        chmod(new File(imagefs, "Steamless/Steamless.CLI.exe"));
        chmod(new File(imagefs, "opt/mono-gecko-offline/wine-mono-11.0.0-x86.msi"));
        return true;
    }

    private static boolean installBundledProton(Context context, AssetManager assetManager) {
        if (!BuildConfig.GAMEFOLD_PC_LAZY) return true;

        final String assetName = "proton-10.0-arm64ec.wcp";
        try {
            if (isBundledProtonReady(context)) {
                Log.d("ImageFsInstaller", "Bundled Proton already ready; skipping extraction");
                return true;
            }
            File externalProton = GameFoldPcResourceRoot.resolve(assetName);
            boolean bundledProton = Arrays.asList(assetManager.list("")).contains(assetName);
            if (externalProton == null && !bundledProton) return false;

            File tempDir = new File(context.getFilesDir(), "tmp/gamefold-bundled-proton");
            FileUtils.delete(tempDir);
            tempDir.mkdirs();

            boolean extracted = externalProton != null
                ? TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, externalProton, tempDir)
                : TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, assetManager, assetName, tempDir);
            if (!extracted) {
                Log.e("ImageFsInstaller", "Failed to extract bundled Proton package");
                return false;
            }

            ContentsManager contentsManager = new ContentsManager(context);
            ContentProfile profile = contentsManager.readProfile(new File(tempDir, ContentsManager.PROFILE_NAME));
            if (profile == null || profile.type != ContentProfile.ContentType.CONTENT_TYPE_PROTON) {
                Log.e("ImageFsInstaller", "Bundled Proton package has no valid profile");
                return false;
            }

            File installDir = ContentsManager.getInstallDir(context, profile);
            if (!installDir.exists()) {
                installDir.getParentFile().mkdirs();
                if (!tempDir.renameTo(installDir)) {
                    FileUtils.copy(tempDir, installDir);
                    FileUtils.delete(tempDir);
                }
            }

            File binDir = new File(installDir, profile.wineBinPath);
            chmodRecursive(binDir);
            Log.i("ImageFsInstaller", "Bundled Proton ready: " + installDir.getAbsolutePath());
            return binDir.isDirectory();
        } catch (Exception e) {
            Log.e("ImageFsInstaller", "Bundled Proton installation failed", e);
            return false;
        }
    }

    private static boolean isBundledProtonReady(Context context) {
        String logicalName = "proton-10.0-arm64ec-2";
        String normalizedName = logicalName.endsWith("-2")
                ? logicalName.substring(0, logicalName.length() - 2)
                : logicalName;
        File installDir = new File(ImageFs.getSharedProtonDir(context), normalizedName);
        File profileFile = new File(installDir, ContentsManager.PROFILE_NAME);
        if (!profileFile.isFile()) return false;
        ContentProfile profile = new ContentsManager(context).readProfile(profileFile);
        if (profile == null) return false;
        File bin = new File(installDir, profile.wineBinPath == null ? "bin" : profile.wineBinPath);
        File lib = new File(installDir, profile.wineLibPath == null ? "lib" : profile.wineLibPath);
        File prefix = profile.winePrefixPack == null ? null : new File(installDir, profile.winePrefixPack);
        return bin.isDirectory() && lib.isDirectory() && (prefix == null || prefix.isFile());
    }

    private static void chmodRecursive(File file) {
        if (!file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) chmodRecursive(child);
            }
        } else {
            FileUtils.chmod(file, 0755);
        }
    }

    private static void chmod(File f) { if (f.exists()) FileUtils.chmod(f, 0755);}

    public static Future<Boolean> installIfNeededFuture(final Context context, AssetManager assetManager, Container container, Callback<Integer> onProgress) {
        ImageFs imageFs = ImageFs.find(context);
        String wineVersion = container.getWineVersion();
        if (!ImageFSLegacyMigrator.migrateLegacyDirsIfNeeded(context, imageFs.getRootDir(), wineVersion)) {
            Log.w("ImageFsInstaller", "Failed to migrate legacy directories before installation.");
            return Executors.newSingleThreadExecutor().submit(() -> false);
        }
        if (!imageFs.isValid() || imageFs.getVersion() < LATEST_VERSION || !imageFs.getVariant().equals(container.getContainerVariant())) {
            Log.d("ImageFsInstaller", "Installing image from assets");
            return installFromAssetsFuture(
                    context,
                    assetManager,
                    container.getContainerVariant(),
                    wineVersion,
                    onProgress
            );
        } else {
            Log.d("ImageFsInstaller", "Image FS already valid and at latest version");
            return Executors.newSingleThreadExecutor().submit(() -> {
                ensureBionicLib(context, imageFs.getRootDir());
                return true;
            });
        }
    }

    /**
     * First-launch entry point for the GamePad PC shell. It keeps the old
     * launch ABI intact while allowing the Activity gate to provision the
     * shared runtime before the first game/container is opened.
     */
    public static Future<Boolean> installSharedRuntimeIfNeededFuture(
            final Context context,
            AssetManager assetManager,
            Container bootstrap,
            Callback<Integer> onProgress) {
        ImageFs imageFs = ImageFs.find(context);
        String wineVersion = bootstrap.getWineVersion();
        if (!ImageFSLegacyMigrator.migrateLegacyDirsIfNeeded(context, imageFs.getRootDir(), wineVersion)) {
            return Executors.newSingleThreadExecutor().submit(() -> false);
        }
        if (!imageFs.isValid() || imageFs.getVersion() < LATEST_VERSION ||
                !imageFs.getVariant().equals(bootstrap.getContainerVariant())) {
            return installFromAssetsFuture(
                    context,
                    assetManager,
                    bootstrap.getContainerVariant(),
                    wineVersion,
                    onProgress,
                    false
            );
        }
        return Executors.newSingleThreadExecutor().submit(() -> {
            ensureBionicLib(context, imageFs.getRootDir());
            return true;
        });
    }

    /** Remove only incomplete runtime markers; never delete public payload or user home. */
    public static void invalidateIncompleteSharedRuntime(final Context context) {
        ImageFs imageFs = ImageFs.find(context);
        File marker = imageFs.getImgVersionFile();
        File variant = imageFs.getVariantFile();
        if (marker.exists()) marker.delete();
        if (variant.exists()) variant.delete();
    }

    /** Install archives deferred by the first-run GameFold shell gate. */
    public static Future<Boolean> ensureLaunchRuntimeDependencies(
            final Context context,
            final AssetManager assetManager) {
        return Executors.newSingleThreadExecutor().submit(() -> {
            ImageFs imageFs = ImageFs.find(context);
            File root = imageFs.getRootDir();
            boolean guestReady = new File(root, "usr/lib/libredirect.so").isFile()
                    && new File(root, "generate_interfaces_file.exe").isFile();
            if (!guestReady && !installGuestLibs(context)) return false;
            return installBundledProton(context, assetManager);
        });
    }

    private static boolean isImportedWineProton(Context context, String fileName) {
        String lowerName = fileName.toLowerCase();

        // Not a Wine/Proton directory
        if (!lowerName.startsWith("wine-") && !lowerName.startsWith("proton-")) {
            return false;
        }

        // Get bundled versions from resource arrays
        String[] bionicWineEntries = context.getResources().getStringArray(R.array.bionic_wine_entries);
        String[] glibcWineEntries = context.getResources().getStringArray(R.array.glibc_wine_entries);

        // Check if it's a bundled version
        for (String version : bionicWineEntries) {
            if (lowerName.equals(version.toLowerCase())) return false;
        }
        for (String version : glibcWineEntries) {
            if (lowerName.equals(version.toLowerCase())) return false;
        }

        // It's Wine/Proton but not bundled, so it's imported
        return true;
    }

    private static void clearOptDir(Context context, File optDir) {
        File[] files = optDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            String fileName = file.getName();

            if (fileName.equals("installed-wine")) continue;

            // Keep imported Wine/Proton installations
            if (isImportedWineProton(context, fileName)) {
                Log.d("ImageFsInstaller", "Preserving imported installation: " + fileName);
                continue;
            }

            // Delete everything else
            FileUtils.delete(file);
        }
    }

    private static void clearRootDir(Context context, File rootDir) {
        if (rootDir.isDirectory()) {
            File[] files = rootDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        String name = file.getName();
                        if (name.equals("home")) {
                            continue;
                        }
                        // Preserve imported Wine/Proton installations in opt/
                        if (name.equals("opt")) {
                            Log.d("ImageFsInstaller", "Clearing opt directory while preserving imported Wine/Proton installations");
                            clearOptDir(context, file);
                            continue;
                        }
                    }
                    FileUtils.delete(file);
                }
            }
        }
        else rootDir.mkdirs();
    }

    public static void generateCompactContainerPattern(final Context context, AssetManager assetManager) {
        // AppUtils.keepScreenOn(context);
        // PreloaderDialog preloaderDialog = new PreloaderDialog(context);
        // preloaderDialog.show(R.string.loading);
        Executors.newSingleThreadExecutor().execute(() -> {
            File[] srcFiles, dstFiles;
            File rootDir = ImageFs.find(context).getRootDir();
            File wineSystem32Dir = new File(rootDir, "/opt/wine/lib/wine/x86_64-windows");
            File wineSysWoW64Dir = new File(rootDir, "/opt/wine/lib/wine/i386-windows");

            File containerPatternDir = new File(context.getCacheDir(), "container_pattern_gamenative");
            FileUtils.delete(containerPatternDir);
            TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, assetManager, "container_pattern_gamenative.tzst", containerPatternDir);

            File containerSystem32Dir = new File(containerPatternDir, ".wine/drive_c/windows/system32");
            File containerSysWoW64Dir = new File(containerPatternDir, ".wine/drive_c/windows/syswow64");

            dstFiles = containerSystem32Dir.listFiles();
            srcFiles = wineSystem32Dir.listFiles();

            ArrayList<String> system32Files = new ArrayList<>();
            ArrayList<String> syswow64Files = new ArrayList<>();

            for (File dstFile : dstFiles) {
                for (File srcFile : srcFiles) {
                    if (dstFile.getName().equals(srcFile.getName())) {
                        if (FileUtils.contentEquals(srcFile, dstFile)) system32Files.add(srcFile.getName());
                        break;
                    }
                }
            }

            dstFiles = containerSysWoW64Dir.listFiles();
            srcFiles = wineSysWoW64Dir.listFiles();

            for (File dstFile : dstFiles) {
                for (File srcFile : srcFiles) {
                    if (dstFile.getName().equals(srcFile.getName())) {
                        if (FileUtils.contentEquals(srcFile, dstFile)) syswow64Files.add(srcFile.getName());
                        break;
                    }
                }
            }

            try {
                JSONObject data = new JSONObject();

                JSONArray system32JSONArray = new JSONArray();
                for (String name : system32Files) {
                    FileUtils.delete(new File(containerSystem32Dir, name));
                    system32JSONArray.put(name);
                }
                data.put("system32", system32JSONArray);

                JSONArray syswow64JSONArray = new JSONArray();
                for (String name : syswow64Files) {
                    FileUtils.delete(new File(containerSysWoW64Dir, name));
                    syswow64JSONArray.put(name);
                }
                data.put("syswow64", syswow64JSONArray);

                FileUtils.writeString(new File(context.getCacheDir(), "common_dlls.json"), data.toString());

                File outputFile = new File(context.getCacheDir(), "container_pattern_gamenative.tzst");
                FileUtils.delete(outputFile);
                TarCompressorUtils.compress(TarCompressorUtils.Type.ZSTD, new File(containerPatternDir, ".wine"), outputFile, 22);

                FileUtils.delete(containerPatternDir);
                // preloaderDialog.closeOnUiThread();
            }
            catch (JSONException e) {
                Log.e("ImageFsInstaller", "Failed to read JSON data: " + e);
            }
        });
    }

    /**
     * Ensures that:
     * - A shared home backing directory exists at imagefs_shared/home (containing xuser, etc.)
     * - The given imagefs rootDir exposes /home as a symlink to that shared root.
     *
     * This allows the same user home (e.g. .wine, .cache) to be shared across variants.
     */
    public static void ensureSharedHomeRoot(Context context, File rootDir) {
        File sharedHomeRoot = new File(ImageFs.getImageFsSharedDir(context), "home");
        if (!sharedHomeRoot.exists()) {
            sharedHomeRoot.mkdirs();
        }

        File homePathInImageFs = new File(rootDir, "home");
        if (FileUtils.isSymlink(homePathInImageFs)) {
            // Already symlinked: /imagefs/home is a symlink to imagefs_shared/home.
            return;
        }

        FileUtils.symlink(sharedHomeRoot.getPath(), homePathInImageFs.getPath());
    }

    /**
     * For Bionic: ensures rootDir/opt/<protonVersion> points to imagefs_shared/proton/<protonVersion>.
     * This keeps only the active Proton version linked in opt, matching pre-branch layout.
     */
    public static void ensureProtonVersionSymlink(Context context, File rootDir, String protonVersion) {
        if (protonVersion == null || protonVersion.isEmpty() || !protonVersion.startsWith("proton-")) return;
        File optDir = new File(rootDir, "opt");
        if (!optDir.exists() && !optDir.mkdirs()) {
            Log.e("ImageFsInstaller", "Failed to create opt directory: " + optDir.getAbsolutePath());
            return;
        }
        removeCurrentProtonSymlink(optDir, protonVersion);

        File targetVersionDir = resolveInstalledProtonDir(context, protonVersion);
        if (!targetVersionDir.isDirectory()) {
            Log.w("ImageFsInstaller", "Skipping Proton symlink; shared dir missing for " + protonVersion);
            return;
        }
        File optVersionLink = new File(optDir, protonVersion);
        try {
            File desiredTarget = targetVersionDir.getCanonicalFile();
            if (isLinkAlreadyCorrect(optVersionLink, desiredTarget)) return;
            if (!deleteExistingPathIfPresent(optVersionLink)) return;

            FileUtils.symlink(targetVersionDir.getAbsolutePath(), optVersionLink.getAbsolutePath());
            if (!Files.isSymbolicLink(optVersionLink.toPath())) {
                Log.e("ImageFsInstaller", "Failed to create Proton symlink at: " + optVersionLink.getAbsolutePath());
                return;
            }
            File linkedTarget = optVersionLink.getCanonicalFile();
            if (!linkedTarget.equals(desiredTarget)) {
                Log.e("ImageFsInstaller", "Proton symlink points to unexpected target: " + linkedTarget);
                return;
            }
            Log.d("ImageFsInstaller", "Created opt/" + protonVersion + " -> " + targetVersionDir.getAbsolutePath());
        } catch (Exception e) {
            Log.e("ImageFsInstaller", "ensureProtonVersionSymlink failed for " + protonVersion, e);
        }
    }

    private static boolean isLinkAlreadyCorrect(File optVersionLink, File desiredTarget) {
        if (!Files.isSymbolicLink(optVersionLink.toPath())) return false;
        try {
            return optVersionLink.getCanonicalFile().equals(desiredTarget);
        } catch (IOException ignored) {
            // Dangling symlink (or inaccessible target): treat as incorrect and replace.
            return false;
        }
    }

    private static boolean deleteExistingPathIfPresent(File path) {
        if (!Files.isSymbolicLink(path.toPath()) && !path.exists()) return true;
        if (FileUtils.delete(path)) return true;
        Log.e("ImageFsInstaller", "Failed to delete existing Proton path: " + path.getAbsolutePath());
        return false;
    }

    private static File resolveInstalledProtonDir(Context context, String protonVersion) {
        ContentsManager contentsManager = new ContentsManager(context);
        contentsManager.syncContents();
        ContentProfile profile = contentsManager.getProfileByEntryName(protonVersion);
        if (profile != null && (profile.type == ContentProfile.ContentType.CONTENT_TYPE_WINE
                || profile.type == ContentProfile.ContentType.CONTENT_TYPE_PROTON)) {
            return ContentsManager.getInstallDir(context, profile);
        }
        return new File(ImageFs.getSharedProtonDir(context), protonVersion);
    }

    /**
     * Removes the current Proton symlink(s) from opt/ so it can be replaced with the current proton
     * version symlink.
     */
    private static void removeCurrentProtonSymlink(File optDir, String activeProtonVersion) {
        File[] optEntries = optDir.listFiles();
        if (optEntries == null) {
            return;
        }

        for (File entry : optEntries) {
            if (!entry.getName().startsWith("proton-")) {
                continue;
            }
            if (entry.getName().equals(activeProtonVersion)) {
                continue;
            }
            if (FileUtils.isSymlink(entry)) {
                FileUtils.delete(entry);
            }
        }
    }
}
