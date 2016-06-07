package io.github.supercheese200.scratchpad;

import javax.swing.*;
import java.awt.GridLayout;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScratchpadAgent implements ClassFileTransformer {
    public static void agentmain(String agentParams, Instrumentation instrumentation) {
        Set<ClassLoader> classLoaders = new HashSet<>();
        for (Class<?> loadedClass : instrumentation.getAllLoadedClasses()) {
            classLoaders.add(loadedClass.getClassLoader());
        }

        List<ClassLoader> toRemove = new ArrayList<>();
        classLoaders.stream().forEach(classLoader -> {
            try {
                if (classLoader.getClass().getSimpleName().equals("DelegatingClassLoader")) {
                    toRemove.add(classLoader);
                }
            } catch (Exception gobbled) {}
        });
        toRemove.stream().forEach(classLoaders::remove);

        int width = Math.max(1, Math.min((3 * classLoaders.size()) / 20, 6));
        JFrame frame = new JFrame("ClassLoaders: " + classLoaders.size() + " loaders");
        frame.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(0, width));
        ButtonGroup group = new ButtonGroup();

        for (ClassLoader classLoader : classLoaders) {
            JRadioButton button = null;
            try {
                button = new JRadioButton(classLoader.getClass() == null ? "ClassLoader" : classLoader.getClass().getSimpleName());
            } catch (Exception e) {
                button = new JRadioButton("ClassLoader");
            }
            group.add(button);
            panel.add(button);
        }

        for (int i = 0; i<(width - (classLoaders.size() % width)) + width; i++) {
            panel.add(new JLabel());
        }

        JButton button = new JButton("Groovy Console");
        button.setHorizontalAlignment(SwingConstants.CENTER);

        button.addActionListener(new ScratchpadConsoleButtonActionListener(classLoaders, frame, group));

        frame.add(panel);
        panel.add(button);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return classfileBuffer;
    }
}
