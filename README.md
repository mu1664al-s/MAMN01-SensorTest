# MAMN01-SensorTest

## Compass

Koden till kompassen är hämtad från [https://www.wlsdevelop.com/index.php/en/blog?option=com_content&view=article&id=38](https://www.wlsdevelop.com/index.php/en/blog?option=com_content&view=article&id=38).

**Följande förändringar har gjorts:**
- Färgerna inverteras vid nord
- Vibration vid nord
  - Vibration koden är hämtad från [https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate](https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate)
- Översätter Nord, syd, öst och väst till systemspråket
  - Kombinerade [https://www.wikihow.com/Execute-HTTP-POST-Requests-in-Android](https://www.wikihow.com/Execute-HTTP-POST-Requests-in-Android) och [https://medium.com/@JasonCromer/android-asynctask-http-request-tutorial-6b429d833e28](https://medium.com/@JasonCromer/android-asynctask-http-request-tutorial-6b429d833e28) för att skicka förfrågan till YandexTranslate API
  - Dokumentation till APIn [https://tech.yandex.com/translate/doc/dg/reference/translate-docpage/](https://tech.yandex.com/translate/doc/dg/reference/translate-docpage/)
- Nord, syd, öst och väst läses upp med text-to-speach i systemspråket
  - TTS hämtad från [https://www.tutorialspoint.com/android/android_text_to_speech.htm](https://www.tutorialspoint.com/android/android_text_to_speech.htm)
- Lade till två switchar för att sätta på och stänga av ljud och/eller vibration
- Low-pass filter från [https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings](https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings) för att dämpa bort brus

## Accelerometer

Bygger på kompass koden från tidigare.

**Följande förändringar har gjorts:**
- Endast accelerometern (med low-pass filtern) och vibratorn används
- Gjordes om till en vattenpass
  - OBS! Lägg mobilen på baksidan
  - Vyns storlek hämtas för animationen med hjälp av [https://stackoverflow.com/questions/39660918/android-constraintlayout-getwidth-return-0](https://stackoverflow.com/questions/39660918/android-constraintlayout-getwidth-return-0)
  - Vibrerar vid 0 graders lutning
  - Cirkeln blir grön vid 0 graders lutning
- En switch för att sätta på och stänga av vibrationen
