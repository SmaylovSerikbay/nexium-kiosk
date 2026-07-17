# Device Owner Setup

Инструкция описывает, как правильно назначить приложение Nexium Kiosk владельцем устройства (Device Owner) на Android-планшете.

## Главное ограничение

Приложение не может автоматически стать Device Owner при обычной установке APK. Android разрешает назначить Device Owner только через provisioning: ADB, QR/NFC, Android Enterprise/EMM или zero-touch.

Для ручной установки используется ADB-команда `dpm set-device-owner`. Ее нужно выполнять на чистом устройстве после factory reset, до добавления Google-аккаунтов и рабочих профилей.

## Данные приложения

Текущая команда для этого проекта:

```bash
adb shell dpm set-device-owner com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
```

Значения взяты из проекта:

- `applicationId`: `com.aistudio.nexiumhealth.qptwyx`
- `DeviceAdminReceiver`: `com.example.KioskDeviceAdminReceiver`

Важно: не используйте сокращенную форму `.KioskDeviceAdminReceiver` для этой сборки. В проекте `applicationId` отличается от Kotlin package классов. Поэтому Android развернет `.KioskDeviceAdminReceiver` в неверный компонент `com.aistudio.nexiumhealth.qptwyx.KioskDeviceAdminReceiver`. Правильный полный компонент:

```text
com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
```

## Подготовка планшета

1. Сделайте factory reset планшета.
2. После сброса не добавляйте Google-аккаунт.
3. Не восстанавливайте backup.
4. Не создавайте рабочий профиль.
5. Завершите первичную настройку минимально, насколько позволяет устройство.
6. Включите режим разработчика:
   - `Settings` -> `About tablet`
   - нажмите `Build number` 7 раз.
7. Включите USB debugging:
   - `Settings` -> `Developer options`
   - включите `USB debugging`.
8. Подключите планшет к компьютеру по USB.
9. На планшете подтвердите RSA-запрос для USB debugging.

## Установка APK и назначение Device Owner

Проверьте, что ADB видит планшет:

```bash
adb devices
```

Ожидаемый результат:

```text
DEVICE_ID    device
```

Если в списке `unauthorized`, подтвердите RSA-запрос на планшете и повторите команду.

Установите APK:

```bash
adb install app-release.apk
```

Для debug-сборки:

```bash
adb install app-debug.apk
```

Назначьте приложение Device Owner:

```bash
adb shell dpm set-device-owner com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
```

Проверьте владельца устройства:

```bash
adb shell dpm list-owners
```

Дополнительная проверка:

```bash
adb shell dumpsys device_policy | grep -i "device owner"
```

Ожидаемо устройство должно показать Device Owner с компонентом:

```text
com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
```

После успешного назначения Device Owner перезапустите приложение, чтобы оно заново
прочитало политики устройства и включило kiosk/silent-update режим:

```bash
adb shell am force-stop com.aistudio.nexiumhealth.qptwyx
adb shell monkey -p com.aistudio.nexiumhealth.qptwyx 1
```

Или перезагрузите планшет:

```bash
adb reboot
```

## Короткий чеклист

```bash
adb devices
adb install app-release.apk
adb shell dpm set-device-owner com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
adb shell dpm list-owners
adb shell am force-stop com.aistudio.nexiumhealth.qptwyx
adb shell monkey -p com.aistudio.nexiumhealth.qptwyx 1
```

## Google Play Protect при установке APK

Если появляется окно `Google Play Защита` / `Приложение заблокировано для защиты устройства`,
это означает, что APK устанавливается через обычный системный установщик или открыт вручную
из файла/браузера. Для Device Owner обновления должны идти через silent install
`PackageInstaller` без системного окна установки.

Важно:

- первичный APK ставьте командой `adb install`, а не открытием APK на планшете;
- после `dpm set-device-owner` перезапустите приложение или планшет;
- не запускайте скачанный APK вручную из файлового менеджера;
- если приложение показывает собственный диалог обновления и затем открывается Play Protect,
  значит код пошел по fallback-пути обычной установки, а не по Device Owner silent install.

Проверьте, что Device Owner действительно назначен:

```bash
adb shell dpm list-owners
adb shell dumpsys device_policy | grep -i "device owner"
```

Ожидаемый компонент:

```text
com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
```

Проверьте логи во время автообновления:

```bash
adb logcat | grep -E "KioskManager|InstallResultReceiver|PackageInstaller|isDeviceOwner"
```

Ожидаемые признаки правильного silent install:

```text
KioskManager: isDeviceOwner: true
InstallResultReceiver: Обновление до versionCode=... установлено успешно
```

Если `isDeviceOwner: false`, хотя в настройках написано `DEVICE OWNER: ACTIVE`,
перезапустите процесс приложения:

```bash
adb shell am force-stop com.aistudio.nexiumhealth.qptwyx
adb shell monkey -p com.aistudio.nexiumhealth.qptwyx 1
```

Если Play Protect появляется при первичной установке, значит установка идет не через
`adb install`. Временно можно нажать `Все равно установить`, но для production-планшетов
лучше использовать ADB provisioning или Android Enterprise / Managed Google Play.

## Типовые ошибки

### Accounts already exist

Ошибка может выглядеть так:

```text
Not allowed to set the device owner because there are already some accounts on the device
```

Причина: на планшете уже есть Google-аккаунт, рабочий профиль или устройство уже было полноценно настроено.

Решение: сделать factory reset и повторить настройку без добавления аккаунтов.

### Unknown admin

Ошибка может выглядеть так:

```text
Unknown admin: ComponentInfo{...}
```

Причина: неверно указан receiver или установлен не тот APK.

Решение:

1. Проверьте, что установлен APK именно этого приложения.
2. Проверьте package name:

```bash
adb shell pm list packages | grep nexium
```

3. Используйте команду:

```bash
adb shell dpm set-device-owner com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
```

### ComponentInfo does not exist

Причина: приложение не установлено, package name отличается, или receiver отсутствует в установленной версии APK.

Решение: переустановить актуальный APK и повторить команду.

```bash
adb install -r app-release.apk
adb shell dpm set-device-owner com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
```

### Device is already provisioned

Причина: устройство уже считается настроенным Android-системой.

Решение: factory reset и повторное назначение Device Owner до добавления аккаунтов.

### Already an admin, но Not active admin

Ошибка может выглядеть так:

```text
com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver was already an admin for user 0. No need to set it again.

Exception occurred while executing 'set-device-owner':
java.lang.IllegalArgumentException: Not active admin: ComponentInfo{com.aistudio.nexiumhealth.qptwyx/com.aistudio.nexiumhealth.qptwyx.KioskDeviceAdminReceiver}
```

Если при этом `adb shell dpm list-owners` ничего не показывает, Device Owner не назначен.

Причина: на планшете есть частично активированный device admin state, но компонент не стал Device Owner. Такое может случиться, если приложение уже включали как Device Admin через настройки, повторно ставили APK, запускали команду не на полностью чистом устройстве или прошивка некорректно обработала provisioning.

Сначала попробуйте сбросить active admin и повторить:

```bash
adb shell dpm remove-active-admin --user 0 com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
adb shell dpm set-device-owner --user 0 com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
adb shell dpm list-owners
```

Если `remove-active-admin` не помог или команда запрещена, сделайте полный factory reset и повторите установку без ручного включения Device Admin в настройках Android:

```bash
adb install app-release.apk
adb shell dpm set-device-owner --user 0 com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
adb shell dpm list-owners
```

Не открывайте `Settings` -> `Security` -> `Device admin apps` и не активируйте приложение вручную. Для Device Owner нужен именно `dpm set-device-owner`, а не обычный Device Admin.

## Что дает Device Owner в этом приложении

После назначения Device Owner приложение может использовать kiosk-возможности Android, включая Lock Task Mode и silent install обновлений через `PackageInstaller`, если это поддерживается текущей версией Android и политиками устройства.

Само наличие receiver в `AndroidManifest.xml` не включает kiosk mode автоматически. Сначала приложение должно быть назначено Device Owner.

## Как снять Device Owner

На dev-устройстве чаще всего проще сделать factory reset.

Если нужно попробовать снять admin через ADB:

```bash
adb shell dpm remove-active-admin com.aistudio.nexiumhealth.qptwyx/com.example.KioskDeviceAdminReceiver
```

На некоторых версиях Android удаление Device Owner может быть запрещено политиками. В этом случае используйте factory reset.

## Production-варианты provisioning

Для небольшого количества планшетов достаточно ADB-инструкции выше.

Для массовой установки лучше использовать один из вариантов:

- QR provisioning после factory reset.
- NFC provisioning, если поддерживается устройством.
- Android Enterprise / EMM.
- Zero-touch enrollment, если поставщик устройств это поддерживает.
