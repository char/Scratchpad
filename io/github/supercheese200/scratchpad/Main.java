package io.github.supercheese200.scratchpad;

import com.sun.tools.attach.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

public class Main {
    public static void main(String... args) {
        try {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
                e.printStackTrace();
            }

            List<VirtualMachineDescriptor> vmList = VirtualMachine.list();
            JFrame frame = new JFrame("JVM Selector - " + vmList.size() + " VMs");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setResizable(false);

            JPanel panel = new JPanel(new GridLayout(0, 1));

            ButtonGroup group = new ButtonGroup();

            for (VirtualMachineDescriptor vm : vmList) {
                JRadioButton button = new JRadioButton(vm.displayName() == null || vm.displayName().isEmpty() ?
                        vm.id() : vm.id() + " - " + vm.displayName().split(" ")[0]);
                group.add(button);
                panel.add(button);
            }
            JButton button = new JButton("Inject");
            button.setHorizontalAlignment(SwingConstants.CENTER);

            button.addActionListener(e -> {
                try {
                    int index = 0;
                    for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements(); ) {
                        AbstractButton button1 = buttons.nextElement();

                        if (button1.isSelected()) {
                            VirtualMachine vm = VirtualMachine.attach(vmList.get(index));
                            vm.loadAgent(new File(".", "scratchpad.jar").getAbsolutePath());
                            vm.detach();
                            System.exit(0);
                            break;
                        }
                        index++;
                    }
                } catch (IOException | AttachNotSupportedException | AgentInitializationException | AgentLoadException e1) {
                    e1.printStackTrace();
                }
            });

            panel.add(button);

            frame.add(panel);
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {}
    }
}
