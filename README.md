# IT Projekt "Time Recording" - Backend

**Projektgruppe:** Kernel Panic
**Projektname:** Time Recording System
**Mitglieder:** Ece Kaya, Furkan Aydin [cite: 2]

## 1. Einleitung


Das Time Recording System ist eine webbasierte Anwendung zur Erfassung von Arbeitszeiten, Projektverwaltung und zur Beantragung sowie Genehmigung von Abwesenheiten. Es unterstützt sowohl die Live-Zeiterfassung per Timer als auch die manuelle Nacherfassung. Für Administratoren sind erweiterte Funktionen zur Benutzer- und Systemverwaltung, einschliesslich eines Backup-Mechanismus, verfügbar.

**Zielgruppen:**
* **Mitarbeiter:** Erfassen Arbeitszeiten und beantragen Abwesenheiten. [cite: 18]
* **Manager:** Sehen Zeiten und Abwesenheiten ihrer Teammitglieder ein, verwalten Projekte und genehmigen Abwesenheitsanträge. 
* **Administratoren:** Haben vollen Zugriff auf alle Systemfunktionen, einschliesslich Benutzerverwaltung, Systemkonfiguration und Datensicherung. 

## 2. Installationsanweisung

Um das Time Recording System lokal auszuführen und zu testen, sind folgende Komponenten und Schritte erforderlich.

### 2.1 Systemvoraussetzungen

* **Docker Desktop:** Eine installierte und laufende Version wird benötigt, um die PostgreSQL-Datenbank in einem Container zu betreiben. Herunterladbar von [docker.com](https://www.docker.com/).
* **Java Development Kit (JDK):** Eine aktuelle Version (Version 17 oder höher) muss installiert sein, um die Anwendung auszuführen.
* **Webbrowser:** Ein aktueller Webbrowser (z.B. Chrome, Firefox, Edge, Safari) für den Zugriff auf die Benutzeroberfläche der Anwendung.

### 2.2 Zugriff auf den Quellcode

Der gesamte Quellcode ist im GitLab-Repository verfügbar. Sie können das Repository klonen, um den Quellcode einzusehen oder die Anwendung direkt aus einer Entwicklungsumgebung zu starten.

**GitLab Repository Link:** `https://gitlab.fhnw.ch/kernel-panic/backend.git`

### 2.3 Lokale Inbetriebnahme

Die Inbetriebnahme des Time Recording Systems für eine lokale Ausführung umfasst folgende Schritte: 

#### 2.3.1 PostgreSQL-Datenbank mittels Docker starten

1.  **Docker Desktop herunterladen und installieren:** Falls noch nicht geschehen, laden Sie Docker Desktop von [docker.com](https://www.docker.com/) herunter und installieren Sie es.
2.  **Docker starten:** Stellen Sie sicher, dass Docker Desktop auf Ihrem System gestartet ist und läuft.
3.  **PostgreSQL-Container zum ersten Mal ausführen:** Öffnen Sie ein Terminal (CMD oder PowerShell unter Windows, Terminal unter macOS/Linux) und führen Sie folgenden Befehl aus:

    ```bash
    docker run --name timerecording-postgres \
    -e POSTGRES_DB=timerecording \
    -e POSTGRES_USER=timerecording_user \
    -e POSTGRES_PASSWORD=secure_password123 \
    -p 5432:5432 \
    -v timerecording_data:/var/lib/postgresql/data \
    -d postgres:15
    ```

    *Hinweis: Dies erstellt den Container und startet die Datenbank. Die Daten werden in einem Docker-Volume namens `timerecording_data` gespeichert.*

4.  **PostgreSQL-Container nachfolgend starten:** Wenn der Container bereits einmal erstellt wurde, können Sie ihn für zukünftige Nutzungen einfach starten: 

    ```bash
    docker start timerecording-postgres
    ```

5.  **PostgreSQL-Container stoppen:** Um den Datenbank-Container zu beenden:

    ```bash
    docker stop timerecording-postgres
    ```

#### 2.3.2 Time Recording Backend-Anwendung starten

Sie haben zwei Optionen, um die Backend-Anwendung zu starten:

**Option A: Ausführung der Anwendung über Docker (empfohlen für einfache Inbetriebnahme)**

Diese Option nutzt ein vorgefertigtes Docker-Image der Anwendung. Ein Docker Account ist nötig.

1.  **Stellen Sie sicher, dass Java (JRE/JDK Version 17 oder höher) installiert ist.** 
2.  **Führen Sie folgende Befehle Schritt für Schritt im Terminal/CMD aus:**
    * **Schritt 1: Docker Login**
        ```bash
        docker login
        ```
    * **Schritt 2: Docker Image herunterladen**
        * **Windows:**
            ```bash
            docker pull pdunkel/kernalpanic-timerecording:latest 
            ```
        * **Linux/Mac:**
            ```bash
            docker pull --platform linux/amd64 pdunkel/kernalpanic-timerecording:latest 
            ```
    * **Schritt 3: Docker Container starten (Backend-Anwendung)**
        * **Windows:**
            ```bash
            docker run -d -p 8080:8080 -e \
            SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/timerecording" \
            -e SPRING_DATASOURCE_USERNAME="timerecording_user" \
            -e SPRING_DATASOURCE_PASSWORD="secure_password123" \
            --name kernelpanic pdunkel/kernalpanic-timerecording:latest 
            ```
        * **Linux/Mac:**
            ```bash
            docker run -d \
            --platform linux/amd64 \
            -p 8080:8080 \
            -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/timerecording" \
            -e SPRING_DATASOURCE_USERNAME="timerecording_user" \
            -e SPRING_DATASOURCE_PASSWORD="secure_password123" \
            --name kernelpanic pdunkel/kernalpanic-timerecording:latest 
            ```
    * **Schritt 4: Docker Compose Up (falls vorhanden und genutzt)**
        ```bash
        docker compose up [cite: 27]
        ```
        *(Dieser Schritt ist nur relevant, falls eine `docker-compose.yml` Datei im Projekt existiert und verwendet wird, um mehrere Dienste gleichzeitig zu starten. Das vorherige `docker run` startet nur den Backend-Container.)*
    * **Schritt 5: Im Webbrowser auf localhost:8080 navigieren.** 

**Option B: Ausführung aus einer Entwicklungsumgebung (IDE) (für Code-Inspektion)**

Diese Option ermöglicht es Ihnen, den Code direkt in einer IDE zu prüfen und die Anwendung von dort zu starten.

1.  **Entwicklungsumgebung (IDE) installieren:** Eine IDE wie IntelliJ IDEA, Eclipse oder VS-Code mit Java- und Maven-Unterstützung ist erforderlich.
2.  **Projekt öffnen:** Öffnen Sie das Projekt in Ihrer bevorzugten IDE. [cite: 29]
3.  **Hauptklasse finden:** Suchen Sie die Hauptklasse `TimeRecordingBackendApplication.java` (typischerweise im Verzeichnis `src/main/java/ch/fhnw/timerecordingbackend/`). 
4.  **Anwendung starten:** Klicken Sie mit der rechten Maustaste auf die Datei und wählen Sie "Run 'TimeRecordingBackendApplication.main()'". Ihre IDE kompiliert dann den Code und startet die Anwendung. [cite: 30, 31]
5.  **Im Webbrowser auf localhost:8080 navigieren.**

#### 2.3.3 Auf die Anwendung im Browser zugreifen

Nachdem sowohl der PostgreSQL-Container als auch die Time Recording Backend-Anwendung erfolgreich gestartet wurden, öffnen Sie einen Webbrowser und geben Sie die folgende Adresse in die Adresszeile ein: `http://localhost:8080`

Sie sollten nun die Login-Seite des Time Recording Systems sehen. 

### 2.4 Demo-Anmeldedaten

Für Testzwecke können folgende Demo-Anmeldedaten verwendet werden, die durch Klick auf die Rolle auf der Login-Seite automatisch in die Felder übernommen werden können: 

* **Admin:** `admin@timerecording.ch` / `admin123` 
* **Manager:** `manager@timerecording.ch` / `manager123` 
* **Mitarbeiter:** `anna.schmidt@timerecording.ch` / `employee123` 

## 3. Projektdetails & Kernfunktionen

Das Time Recording System bietet folgende Hauptfunktionen:

* **Zeiterfassung:** Live-Zeiterfassung per Timer und manuelle Nacherfassung von Arbeitszeiten. 
* **Abwesenheiten:** Beantragung und Genehmigung von Abwesenheitsanträgen (Urlaub, Krankheit, etc.). 
* **Projektverwaltung:** Zuordnung von Arbeitszeiten zu Projekten, Erstellung und Bearbeitung von Projekten. 
* **Benutzerverwaltung:** Registrierung, Anmeldung, Passwortänderung, Benutzer- und globale Abwesenheitsverwaltung (für Admins). 
* **Datensicherung (Backup):** Manuelle und automatisierte Backups im JSON-Format, täglich um 02:00 Uhr nachts. 

---

**Wichtiger Hinweis:** Diese ZIP-Datei enthält den vollständigen Quellcode (`src/`), eine ausführbare JAR-Datei (`your_application.jar`, falls zutreffend), den Datenbank-Dump und diese `README.md`. Der Dozent kann den Code in einer IDE prüfen (`Option B`) oder die Anwendung über Docker starten (`Option A`).