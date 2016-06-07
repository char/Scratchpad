package io.github.supercheese200.injection;

import java.io.*;
import java.nio.channels.FileChannel;

public class InjectionUtilities {
    private static final String NATIVE_DIR = "natives/";
    private static final String WIN_DIR = "windows/";
    private static final String NIX_DIR = "linux/";
    private static final String MAC_DIR = "mac/";
    private static final String SOLARIS_DIR = "solaris/";

    public static byte[] getBytesFromStream(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[65536];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();

    }


    public static byte[] getBytesFromClass(Class<?> clazz) throws IOException {
        return getBytesFromStream(clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class"));
    }


    public static byte[] getBytesFromResource(ClassLoader clazzLoader, String resource) throws IOException {
        return getBytesFromStream(clazzLoader.getResourceAsStream(resource));
    }

    public static void extractResourceToDirectory(ClassLoader loader, String resourceName, String targetName, String targetDir)
            throws IOException {
        InputStream source = loader.getResourceAsStream(resourceName);
        File tmpdir = new File(targetDir);
        File target = new File(tmpdir, targetName);
        target.createNewFile();

        FileOutputStream stream = new FileOutputStream(target);
        byte[] buf = new byte[65536];
        int read;
        while ((read = source.read(buf)) != -1) {
            stream.write(buf, 0, read);
        }
        stream.close();
        source.close();
    }



    public static void loadAgentLibrary() {
        switch (OS.detectCurrentOS()) {
            case WINDOWS:
                unpack(WIN_DIR + "attach.dll");
                break;
            case LINUX:
                unpack(NIX_DIR + "libattach.so");
                break;
            case MAC:
                unpack(MAC_DIR + "libattach.dylib");
                break;
            case SOLARIS:
                unpack(SOLARIS_DIR + "libattach.so");
                break;
            default:
                throw new UnsupportedOperationException("unsupported platform");
        }
    }

    private static void unpack(String path) {
        try {
            //System.out.println(NATIVE_DIR + ((OS.is64Bit() || OS.detectCurrentOS() == OS.MAC) ? "64/" : "32/") + path);
            File nativeFile = new File(NATIVE_DIR + ((OS.is64Bit() || OS.detectCurrentOS() == OS.MAC) ? "64/" : "32/"), path);

            File newFile = new File(path.split("/")[1]);
            System.out.println(nativeFile.getAbsolutePath() + " -> " + newFile.getAbsolutePath());

            copyFile(nativeFile, newFile);
        } catch (IOException x) {
            throw new RuntimeException("could not unpack binaries", x);
        }
    }

    private static void delete(String path) {
        try {
            File libraryFile = new File(path.split("/")[1]);
            libraryFile.delete();
            libraryFile.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    public enum OS {
        LINUX, WINDOWS, MAC, SOLARIS;

        public static OS detectCurrentOS() {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.indexOf("win") >= 0) {
                return WINDOWS;
            }
            if ((os.indexOf("nix") >= 0) || (os.indexOf("nux") >= 0) || (os.indexOf("aix") > 0)) {
                return LINUX;
            }
            if (os.indexOf("mac") >= 0) {
                return MAC;
            }
            if (os.indexOf("sunos") >= 0)
                return SOLARIS;
            return null;
        }

        public static boolean is64Bit() {
            String osArch = System.getProperty("os.arch");
            return "amd64".equals(osArch) || "x86_64".equals(osArch);
        }
    }
}
