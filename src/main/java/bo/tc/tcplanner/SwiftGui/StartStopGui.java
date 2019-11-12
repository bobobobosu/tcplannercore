package bo.tc.tcplanner.SwiftGui;

import bo.tc.tcplanner.app.SolverThread;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartStopGui extends JFrame {
    public StartStopGui(SolverThread solverThread) {
        // Set GUI
        this.setTitle("TCPlanner");
        JButton b = new JButton("STOP");
        b.setBounds(100, 0, 100, 50);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solverThread.terminateSolver();
            }
        });
        JButton b2 = new JButton("START");
        b2.setBounds(0, 0, 100, 50);
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solverThread.restartSolvers();
            }
        });
        this.add(b);
        this.add(b2);
        this.setSize(220, 100);
        this.setAlwaysOnTop(true);
        this.setLayout(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
