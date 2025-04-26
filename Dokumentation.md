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

Ein *Salt* ist ein zufällig generierter Wert, der vor dem Hashing dem Passwort angehängt wird, damit, wenn ein Passwort geknackt wird, andere gleiche Passwörter nicht auch geknackt werden können. 
**Beispiel:** aus `password123` wird `password123q9ct`, und daraus wird der Hash `3c5b2047a0b25bf7aa3b812b70f832184248d8f7c4bb84dd9a6023117cfe6e9f` gemacht.  

Ein *Pepper* funktioniert gleich, man hängt ihn vor das Passwort. Der Unterschied beim Pepper ist, dass er nicht zusammen mit dem Passwort abgespeichert wird, sondern an einem besser gesicherten Ort. Bei einem allfälligen DB-Leak können keine bereits bekannten Hashes auf Passwörter zurückgeführt werden. Meistens gibt es einen einzigen Pepper für jede Applikation.
