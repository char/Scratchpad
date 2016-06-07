package io.github.supercheese200.scratchpad;


import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Set;

public class ScratchpadConsoleButtonActionListener implements ActionListener {
    private Set<ClassLoader> classLoaders;
    private JFrame frame;
    private ButtonGroup group;

    public ScratchpadConsoleButtonActionListener(Set<ClassLoader> classLoaders, JFrame frame, ButtonGroup group) {
        this.classLoaders = classLoaders;
        this.frame = frame;
        this.group = group;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ClassLoader loader = null;
        int index = 0;
        for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                loader = classLoaders.toArray(new ClassLoader[classLoaders.size()])[index];
            }

            index++;
        }

        if (loader != null) {
            new groovy.ui.Console(loader).run();
            frame.setVisible(false);
        }
    }
}
