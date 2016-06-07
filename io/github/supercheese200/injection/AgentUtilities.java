package io.github.supercheese200.injection;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * A utility class for loading Java agents.
 * (credits to https://github.com/Xyene/ASM-Late-Bind-Agent)
 * @author Tudor
 */
public class AgentUtilities {

    /**
     * Loads an agent into a JVM.
     *
     * @param agent     The main agent class.
     * @param resources Array of classes to be included with agent.
     * @param pid       The ID of the target JVM.
     * @throws IOException
     * @throws AttachNotSupportedException
     * @throws AgentLoadException
     * @throws AgentInitializationException
     */
    public static void attachAgentToJVM(String pid, Class agent, Class... resources)
            throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {

        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(generateAgentJar(agent, resources).getAbsolutePath());
        vm.detach();
    }

    /**
     * Generates a temporary agent file to be loaded.
     *
     * @param agent     The main agent class.
     * @param resources Array of classes to be included with agent.
     * @return Returns a temporary jar file with the specified classes included.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static File generateAgentJar(Class agent, Class... resources) throws IOException {
        File groovyJarFile = File.createTempFile("groovy-all-2.4.4-indy", ".jar");
        copyFile(new File("lib/groovy-all-2.4.4-indy.jar"), groovyJarFile);

        File jarFile = File.createTempFile("agent", ".jar");
        jarFile.deleteOnExit();

        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        // Create manifest stating that agent is allowed to transform classes
        mainAttributes.put(Name.MANIFEST_VERSION, "1.0");
        mainAttributes.put(new Name("Agent-Class"), agent.getName());
        mainAttributes.put(new Name("Can-Retransform-Classes"), "true");
        mainAttributes.put(new Name("Can-Redefine-Classes"), "true");
        mainAttributes.put(new Name("Class-Path"), groovyJarFile.getName());

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile), manifest);

        jos.putNextEntry(new JarEntry(agent.getName().replace('.', '/') + ".class"));

        jos.write(InjectionUtilities.getBytesFromStream(agent.getClassLoader().getResourceAsStream(unqualify(agent))));
        jos.closeEntry();

        for (Class clazz : resources) {
            String name = unqualify(clazz);
            jos.putNextEntry(new JarEntry(name));
            jos.write(InjectionUtilities.getBytesFromStream(clazz.getClassLoader().getResourceAsStream(name)));
            jos.closeEntry();
        }

        jos.close();
        return jarFile;
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
    private static String unqualify(Class clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }
}