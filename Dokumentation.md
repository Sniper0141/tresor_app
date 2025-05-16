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
Zum kontrollieren, ob die Encryption funktioniert hat, habe ich mit DBeaver nachgeschaut.

## Secret Encryption

### Requirements
> - Secrets in der Datenbank müssen verschlüsselt gespeichert werden.
> - Beim Lesen der Secrets müssen diese entschlüsselt werden.
> - Der Schlüssel soll für jeden User unterschiedlich sein.

### Informieren Asymmetrische Verschlüsselung


Asymmetrische Verschlüsselung ist asymmetrisch, weil das Verschlüsseln und das Entschlüsseln verschiedene "Keys" braucht. Diese Verschlüsselungsart eignet sich perfekt für das Projekt, weil in der Aufgabenstellung auch von "privaten Schlüsseln" die Rede ist. 

#### So funktioniert es:

- Es werden zwei random Keys generiert: Ein Public- und ein Private-Key. 

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/9/92/Orange_blue_public_private_keygeneration_de.svg/1280px-Orange_blue_public_private_keygeneration_de.svg.png" alt="random keys" width="200"/>

- Der Public-Key kann Klartext verschlüsseln und der Private-Key kann den verschlüsselten Text entschlüsseln. 

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Orange_blue_public_key_cryptography_de.svg/2880px-Orange_blue_public_key_cryptography_de.svg.png" alt="encryption" width="400"/>

- Der Private-Key wird geheimgehalten, aber der Public-Key ist öffentlich.
- Private-Keys können Signaturen (persönliches Identifikationsmittel) generieren, Public-Keys können bestätigen, dass diese Signatur auf ihren Private-Key zugeordnet werden kann.

#### Beispiel:

- Wenn ich von meinem Kollegen eine geheime Nachricht erhalten will, kann ich ihm meinen Public-Key geben, damit mir er seine Klartext-Nachrichten sicher zukommen lassen kann. 
- Ebenfalls er mir seinen Public-Key geben und in seiner verschlüsselten Nachricht seine Signatur mitgeben, so dass ich (mit seinem Public-Key) identifizieren kann, dass diese Nachricht von ihm stammt. 


