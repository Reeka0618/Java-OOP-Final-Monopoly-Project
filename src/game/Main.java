package game;

import gui.GameWindow;
import gui.SetupDialog;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            SetupDialog setup = new SetupDialog(null);
            setup.setVisible(true);

            if (!setup.isConfirmed()) {
                System.exit(0);
            }

            String[] names = setup.getPlayerNames();

            // Buffer log messages that arrive before the window is ready
            final List<String> logBuffer = new ArrayList<>();
            final GameWindow[] windowRef = {null};
            final boolean[] windowReady = {false};

            GameEngine engine = new GameEngine(
                names,
                msg -> {
                    if (windowReady[0] && windowRef[0] != null) {
                        windowRef[0].appendLog(msg);
                    } else {
                        logBuffer.add(msg);
                    }
                },
                () -> {
                    if (windowReady[0] && windowRef[0] != null) {
                        SwingUtilities.invokeLater(() -> windowRef[0].refreshAll());
                    }
                }
            );

            windowRef[0] = new GameWindow(engine);
            windowReady[0] = true;

            // Flush buffered messages
            for (String msg : logBuffer) {
                windowRef[0].appendLog(msg);
            }
        });
    }
}
