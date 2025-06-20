# Regist, Password, Captcha

## Informieren (Password)
Oft werden bei Passwort-Validation auf folgende Sachen geachtet: 
- Länge des Passworts (mind. 8)
- Grossbuchstaben
- Kleinbuchstaben
- Zahlen

Diese Sachen werden oft mit Regex überprüft.

## Umsetzen (Password)
Die nicht abgedeckten Anforderungs werden dem User in Rot angezeigt, nachdem er das Formular abgeschickt hat. 

## Informieren (Captcha)
"reCAPTCHA v2" ist von Google entwickelt. Es gibt dem User einfache Aufgaben, um zu bestätigen dass er ein Mensch ist.  
Sprich: Es ist das gewöhnliche Captcha, das man auf fast jeder Website gesehen hat, bis v3 herausgekommen ist.

## Umsetzen (Captcha)
Auf der Seite von [reCAPTCHA](https://cloud.google.com/security/products/recaptcha?hl=de) kann man einfach ein Captcha erstellen. Dabei bekommt man folgende Keys: 

"Site Key": `6LdCAlcrAAAAACYMlwM3o7yjKG2xQ0_3KDKYvixQ`
"Secret Key": `6LdCAlcrAAAAAL01IO4CaEAK46KG1_FnbT2XcIW6`

Im Frontend kann man ein [JS-File laden](https://developers.google.com/recaptcha/docs/display?hl=de#auto_render) und ein HTML-Element mit dem Site-Key ins HTML einfügen.

Das Backend erhält dann vom Frontend (im POST-Request zum Registrieren) das Captcha-Token und validiert dieses mit dem Google API.
Das Token war schon ein Property in der Request-Klasse, ich habe es noch required gemacht.

[Google reCAPTCHA](https://www.google.com/recaptcha/admin/site/727122498/setup)
