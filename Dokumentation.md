# Dokumentation Verbesserung der Sicherheit

Hier ist dokumentiert, wie die Sicherheit der Tresor-App schrittweise verbessert wurde.

## Password Hash

### Requirements

> - Beim Registrieren wird das Passwort als Hash in der DB gespeichert.
> - Der Hash verwendet Salt und Pepper.
> - Beim Anmelden (login) wird das Passwort erneut ge-hashed und mit den Hash in der Datenbank verglichen werden.
> - Bestehende Klartext-Passwörter in der DB müssen ersetzt werden.

### Informieren 

#### Was ist ein Hash? 

*Hash* ist eine Verschlüsselungsmethode.  
Jeder String hat einen eindeutigen Hash-Wert, aber Hash-Werte lassen sich nur sehr schwierig auf spezifische Strings zuordnen, weil man sie praktisch nicht rückgängig machen kann. Das macht Hashing sehr sicher.   

**Anwendung:**
In sicheren Applikationen werden Passwörter nicht als "plain-text" gespeichert, sondern sie werden zuerst gehasht.  
Wenn ein User sich anmeldet, wird sein Input im Passwort-Feld gehasht und mit dem entsprechenden Hash in der Datenbank verglichen.  
So sind User weniger gefährdet durch Daten-Lecks.  
**Beispiel:** Aus `password123` wird `ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f`.

#### Was ist Salt/Pepper?

Ein *Salt* ist ein zufällig generierter Wert, der vor dem Hashing dem Passwort angehängt wird, damit, wenn ein Passwort geknackt wird, andere Accounts mit gleichen Passwörtern nicht auch gestohlen werden können. 
**Beispiel:** aus `password123` wird `password123q9ct`, und daraus wird der Hash `3c5b2047a0b25bf7aa3b812b70f832184248d8f7c4bb84dd9a6023117cfe6e9f` gemacht.  

Ein *Pepper* funktioniert gleich, man hängt ihn vor das Passwort. Der Unterschied beim Pepper ist, dass er nicht zusammen mit dem Passwort abgespeichert wird, sondern an einem besser gesicherten Ort. Bei einem allfälligen DB-Leak können keine bereits bekannten Hashes auf Passwörter zurückgeführt werden. Meistens gibt es einen einzigen Pepper für jede Applikation.

### Umsetzen

#### Hash beim Registrieren

Ich verwende die Java-Library "Guava", um das Passwort zu hashen.
Den Pepper speichere ich hardcoded in meinem Repository und für den Salt generiere ich einen zufälligen String (der nicht mit `$` enden darf).

Der Code unten hasht das Passwort zusammen mit Pepper und Salt, dann wird das Salt dem Hashcode angehängt und so in der DB gespeichert.

```java
var pepper = "+jb)tN*R?Y@l";
var salt = generateSalt();

var seasonedPassword = pepper + salt + password;
String hashedPassword = Hashing.sha256()
        .hashString(seasonedPassword, StandardCharsets.UTF_8)
        .toString();

return salt + "$" + hashedPassword;
```

#### Hash beim Login

Das Passwort wird zum Backend geschickt. Der Hash des Passwortes wird mit dem abgespeicherten Hash verglichen. Wenn die Hashes übereinstimmen, ist das Login erfolgreich. Eine Message und die UserId werden als Response geschickt. Wenn das Login nicht erfolgreich ist, wird der Fehler als "Message" mit einer UserId `0` als Response geschickt. 

> *Zusätzliche Features:*  
> - Falls es keinen User mit der angegebenen Email gibt, kommt die gleiche Antwort wie bei einem falschen Passwort. So kann verhindert werden, dass Hacker herausfinden, welche Emails einen Account haben.  
> - Falls ein Login fehlschlägt, bleibt man auf der Login-Page und die Error-Message wird dem User angezeigt.

#### Existierende Passwörter

Ich habe eine Klasse `OneTimeEncryptionScript` erstellt, die alle Einträge rausliest und sie mithilfe des `PasswordEncryptionService` gehasht abspeichert.  
Beim Applikationsstart wird die Klasse aufgerufen. Ich führe die Applikation einmal aus lösche den Aufruf der Klasse wieder.
