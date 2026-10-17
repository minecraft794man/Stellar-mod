# Weltraum-Mod (NeoForge 1.21.1) - Version 0.1

## Was drin ist
- **Rakete**: Rechtsklick = zum Ziel fliegen, Shift + Rechtsklick = Ziel wechseln (Erdorbit, Mond, Mars, Erde)
- **Himmelskoerper** als eigene Dimensionen: niedrige Schwerkraft (Orbit 10 %, Mond 17 %, Mars 38 %), kein Fallschaden
- **Sauerstoff**: ohne Raumanzug (Helm + Brustpanzer) hast du im All 20 Sekunden Luft, danach Schaden
- **Orbitallaser-Block**: im Weltraum aufstellen, Rechtsklick -> 10 Sekunden laden -> schiesst auf die
  gleiche X/Z-Position in der Oberwelt und schneidet dort eine Saeule (Radius 12) vom Himmel bis zum Void weg.
  Alles Lebende darin stirbt (Kreativ-Spieler nicht).
- Alles im Kreativ-Tab "Weltraum", alles auch craftbar.

## Jar bauen - Weg A: GitHub Actions (nichts installieren)
1. Auf github.com einloggen -> oben rechts "+" -> "New repository" -> Name z. B. `weltraum-mod` -> "Create repository".
2. Im neuen Repo auf "uploading an existing file" klicken.
3. Den **Inhalt** dieses entpackten Ordners (auch den versteckten Ordner `.github`) per Drag & Drop hineinziehen
   -> unten "Commit changes".
4. Oben auf den Reiter **Actions** gehen. Der Lauf "Mod bauen" startet automatisch (ca. 3-6 Minuten).
5. Wenn er gruen ist: Lauf anklicken -> unten bei "Artifacts" `weltraum-mod` herunterladen -> ZIP entpacken -> `weltraum-0.1.0.jar`.
   Wird er rot: Lauf anklicken, die Fehlermeldung kopieren und an Claude schicken.

## Jar bauen - Weg B: lokal
1. JDK 21 installieren (z. B. Temurin) und Gradle 8.10.2.
2. Im Projektordner: `gradle build`
3. Die Jar liegt danach in `build/libs/`. (Gradle laedt dabei Minecraft herunter: mehrere GB Speicher.)

## Installieren
`weltraum-0.1.0.jar` in den `mods`-Ordner von Minecraft **1.21.1 mit NeoForge** legen.
