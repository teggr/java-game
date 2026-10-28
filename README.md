# java-game

Skybound, a Java 25 platform game compiled to JavaScript by [TeaVM](https://teavm.org),
rendered inside [JCEF's embedded Chromium](https://github.com/chromiumembedded/java-cef),
and distributed with native Windows/macOS launchers from [jr](https://github.com/jarrunner/jr).

## Build and run

Install **JDK 25** and **Maven 3.9+**. Ensure `JAVA_HOME` points to JDK 25.

```sh
mvn clean verify
java --enable-native-access=ALL-UNNAMED -jar /home/runner/work/java-game/java-game/desktop/target/java-game.jar
```

The absolute paths here use this checkout's location; replace that prefix with your
own checkout or extracted distribution directory.

On macOS, the direct Java command additionally needs these options before `-jar`
(the packaged Mac launcher supplies them automatically):

```text
--add-opens=java.desktop/sun.awt=ALL-UNNAMED
--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED
--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED
```

A graphical desktop is required. Move with arrows or A/D, jump with Space/Up/W,
and restart with R or **Play again**. Touch controls are also retained.

## Native distributions

Build **one profile at a time**:

| Target | Build command | Distribution |
| --- | --- | --- |
| Windows x64 (build on Windows) | `mvn -Pwindows clean verify` | `/home/runner/work/java-game/java-game/desktop/target/java-game-windows.zip` |
| macOS Intel/Apple Silicon | `mvn -Pmacos clean verify` | `/home/runner/work/java-game/java-game/desktop/target/java-game-macos.zip` |

Extract the **whole ZIP** and double-click `java-game.exe` or `Skybound.app`.
Keep the adjacent `java-game.jar` beside the launcher/app; the paths are relative
to the launcher, not to the working directory. Do not distribute jr's intermediate
app-only ZIP: the outer distribution ZIP also contains the game JAR.

jr finds Java 25 or offers to install a suitable runtime. On the first application
launch, [jcefmaven](https://github.com/jcefmaven/jcefmaven) downloads and extracts
the platform-specific JCEF native bundle. **An internet connection is required
for this first-run setup**; neither the JVM nor Chromium natives are bundled in
the ZIP. Subsequent launches reuse the per-user runtime/native caches. The game,
HTML, CSS, and compiled JavaScript are already inside the JAR, not fetched from
the showcase website.

JCEF downloads and browser data are stored under the user's `.java-game`
directory. A versioned native directory prevents upgrades from reusing an older
Chromium bundle. The launcher uses a normal child JVM (`jvmMode=exe`) for
desktop compatibility; jr's AOT training is disabled for this interactive app.

The Mac launcher requires macOS 13+. The jr app is ad-hoc signed when built on
macOS, not Developer-ID signed/notarized; Windows builds are unsigned.
Gatekeeper/SmartScreen may require approval. Public releases should be signed
and tested on each target OS. macOS packaging can run on Linux, but signing and
native UI validation require a Mac.

The GitHub Actions build tests/packages on Windows and macOS and uploads the two
distribution ZIPs as artifacts. It does not publish releases or exercise a real
Chromium window.

## Project layout

- `/home/runner/work/java-game/java-game/game`: the reused Java game and browser
  resources. TeaVM runs during `process-classes`; generated JavaScript is packaged
  with the HTML/CSS under the JAR's `web` directory.
- `/home/runner/work/java-game/java-game/desktop`: Swing/JCEF host, shaded
  executable JAR, jr profiles, and distribution assemblies.
- `/home/runner/work/java-game/java-game/pom.xml`: Java 25 reactor and shared versions.

The desktop serves only an explicit allowlist of bundled game resources on an
ephemeral IPv4 loopback port. It never serves checkout files. Navigation outside
that origin and popup windows are blocked. Closing the window disposes JCEF and
stops the resource server. Native Chromium initialization happens off the Swing
event thread; the window is created on that thread.

`mvn verify` runs the original physics/reset tests and HTTP tests for assets,
content types, HEAD, method restrictions, traversal rejection, foreign Host
headers, independent instances, and server shutdown. No native browser download
or display is needed for these tests.

## Upstream game provenance

The five game Java classes, three browser resources, and original game tests in
this repository are copied unchanged from
[`teggr/teavm-showcase`](https://github.com/teggr/teavm-showcase/tree/13908cd8d7fbd0d572d23ca9d7c1c865445e974f/teavm-showcase-platform-game)
at commit `13908cd8d7fbd0d572d23ca9d7c1c865445e974f`.
The original package names, visuals, controls, and game logic are preserved.
The showcase's standalone NanoHTTPD development server is omitted: the desktop
host instead serves classpath resources using the JDK HTTP server.

The inspected showcase revision has no explicit license. It is reused here at
the repository owner's request; no new license is asserted for the original
game. Obtain appropriate permission/license terms before third-party
redistribution. TeaVM, JCEF/Chromium, jcefmaven, jr, and transitive dependencies
retain their own licenses and notices, including notices in downloaded native
bundles. Native distribution ZIPs include `THIRD-PARTY-NOTICES.txt` and the
Apache license text; dependency notices and the JCEF license are retained in
the application JAR.
