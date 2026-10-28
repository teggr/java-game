package io.github.teggr.javagame;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefBuildInfo;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.network.CefRequest;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DesktopApp {
    private DesktopApp() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("Skybound requires a graphical desktop.");
        }
        GameServer server = null;
        CefApp app = null;
        try {
            server = new GameServer();
            GameServer gameServer = server;
            Runtime.getRuntime().addShutdownHook(new Thread(gameServer::close, "game-server-shutdown"));
            Path data = Path.of(System.getProperty("user.home"), ".java-game");
            Files.createDirectories(data);
            CefAppBuilder builder = new CefAppBuilder();
            String nativeVersion = CefBuildInfo.fromClasspath().getReleaseTag()
                    .replaceAll("[^A-Za-z0-9._-]", "_");
            builder.setInstallDir(data.resolve(nativeVersion).toFile());
            builder.getCefSettings().windowless_rendering_enabled = false;
            builder.getCefSettings().cache_path = data.resolve("chromium-cache").toString();
            builder.setAppHandler(new MavenCefAppHandlerAdapter() {
                @Override
                public void stateHasChanged(CefApp.CefAppState state) {
                    if (state == CefApp.CefAppState.TERMINATED) {
                        gameServer.close();
                        System.exit(0);
                    }
                }
            });
            app = builder.build();
            CefApp cefApp = app;
            SwingUtilities.invokeAndWait(() -> openWindow(cefApp, gameServer));
        } catch (Exception | LinkageError failure) {
            if (server != null) {
                server.close();
            }
            failure.printStackTrace(System.err);
            JOptionPane.showMessageDialog(null,
                    "Could not start Skybound.\n" + failure.getMessage()
                            + "\nCheck your connection on first launch and the Java/JCEF requirements in README.",
                    "Skybound startup failed", JOptionPane.ERROR_MESSAGE);
            if (app != null) {
                app.dispose();
            }
            System.exit(1);
        }
    }

    private static void openWindow(CefApp app, GameServer server) {
        CefClient client = app.createClient();
        client.addRequestHandler(new CefRequestHandlerAdapter() {
            @Override
            public boolean onBeforeBrowse(CefBrowser browser, CefFrame frame, CefRequest request,
                                          boolean userGesture, boolean isRedirect) {
                return !request.getURL().startsWith(server.uri().toString());
            }
        });
        client.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame,
                                         String targetUrl, String targetFrameName) {
                return true;
            }
        });
        CefBrowser browser = client.createBrowser(server.uri().toString(), false, false);
        JFrame window = new JFrame("Skybound — Java Game");
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.add(browser.getUIComponent(), BorderLayout.CENTER);
        window.setSize(1100, 850);
        window.setLocationRelativeTo(null);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                app.dispose();
                server.close();
                window.dispose();
            }
        });
        window.setVisible(true);
        browser.setFocus(true);
    }
}
