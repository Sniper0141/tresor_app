# Secret Encryption

## Requirements
> - Secrets in der Datenbank müssen verschlüsselt gespeichert werden.
> - Beim Lesen der Secrets müssen diese entschlüsselt werden.
> - Der Schlüssel soll für jeden User unterschiedlich sein.

## Informieren (Asymmetrische Verschlüsselung)

Asymmetrische Verschlüsselung ist asymmetrisch, weil das Verschlüsseln und das Entschlüsseln verschiedene "Keys" braucht. Diese Verschlüsselungsart eignet sich perfekt für das Projekt, weil in der Aufgabenstellung auch von "privaten Schlüsseln" die Rede ist. 

### So funktioniert es:

- Es werden zwei random Keys generiert: Ein Public- und ein Private-Key. 

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/9/92/Orange_blue_public_private_keygeneration_de.svg/1280px-Orange_blue_public_private_keygeneration_de.svg.png" alt="random keys" width="200"/>

- Der Public-Key kann Klartext verschlüsseln und der Private-Key kann den verschlüsselten Text entschlüsseln. 

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Orange_blue_public_key_cryptography_de.svg/2880px-Orange_blue_public_key_cryptography_de.svg.png" alt="encryption" width="400"/>

- Der Private-Key wird geheimgehalten, aber der Public-Key ist öffentlich.
- Private-Keys können Signaturen (persönliches Identifikationsmittel) generieren, Public-Keys können bestätigen, dass diese Signatur auf ihren Private-Key zugeordnet werden kann.

### Beispiel:

- Wenn ich von meinem Kollegen eine geheime Nachricht erhalten will, kann ich ihm meinen Public-Key geben, damit mir er seine Klartext-Nachrichten sicher zukommen lassen kann. 
- Ebenfalls er mir seinen Public-Key geben und in seiner verschlüsselten Nachricht seine Signatur mitgeben, so dass ich (mit seinem Public-Key) identifizieren kann, dass diese Nachricht von ihm stammt. 

## Informieren (Eindeutige Schlüssel)

Eindeutige Schlüssel sind Abfolgen von Zeichen, die innerhalb eines Systems (in der Theorie) genau einmal vorkommen können.

Ein Beispiel dafür sind UUIDs.

UUIDs sind zusammengesetzt aus möglichst eindeutigen und möglichst random Infos, wie z.B. der momentane Timestamp des Systems zum Zeitpunkt der Erzeugung. 

<img src="https://bootcamptoprod.com/wp-content/uploads/2024/01/Java-UUID-Structure.png" alt="UUIDs" width="400"/>

Sie werden oft in Datenbanken verwendet, um Einträge eindeutig zu identifizieren.

## Umsetzen (Variante 1)

Ich habe mit Java-Libraries eine Encryption (und Decryption) mit Private- und Public-Keys umgesetzt.
Den Master-Key (Private-Key) lade ich von der Festplatte, ausserhalb des Repositories, weil geheime Infos innerhalb eines Repos immer heikel sind. 

Ich verwende für jedes Secret einen Salt. Ich encrypte ihn nur und speichere ihn nirgends ab. Ich encrypte nicht wie das Passwort, sondern ich setze noch ein `$` zwischen Salt und Secret, um den Salt vom Secret unterscheiden zu können.
