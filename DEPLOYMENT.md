# Развёртывание киоска

## 1. Назначение устройства Device Owner (обязательно для полной блокировки и тихих обновлений)

Приложение поддерживает два режима:

- **Без Device Owner** — работает как обычное приложение: кнопка "назад" перехватывается
  (см. `BackHandler` в `MainActivity.kt`), но пользователь технически может свернуть
  приложение через Recents/Home, а обновления ставятся через системный диалог установки.
- **С Device Owner** — полная блокировка экрана (Lock Task Mode: недоступны Home, Recents,
  шторка уведомлений) и тихая установка APK-обновлений без единого диалога.

Device Owner нельзя включить из самого приложения — это осознанное ограничение
безопасности Android (иначе любое приложение могло бы "захватить" чужой телефон).
Настраивается один раз на чистом устройстве через adb.

### Шаги

1. **Полный сброс устройства до заводских настроек**
   `Настройки → Система → Сброс настроек → Стереть все данные (factory reset)`.
   Даже если на планшете уже стоял Google-аккаунт — Android не даст назначить
   Device Owner, пока на устройстве есть хоть один аккаунт. Проще всего сбросить,
   чем гарантированно вычищать все системные аккаунты вручную.

2. **При первом запуске после сброса — не входить ни в один аккаунт** (Google и т.д.)
   и не подключать автосинхронизацию. Setup wizard можно пропустить кнопкой
   "Skip"/"Пропустить"; если он требует Wi-Fi для продолжения — подключиться к сети,
   но не логиниться.

3. **Включить отладку по USB:**
   `Настройки → О планшете → 7 раз тапнуть "Номер сборки"` → появится раздел
   `Для разработчиков` → включить `Отладка по USB`.

4. **Установить APK и назначить Device Owner** (с компьютера, подключив планшет по USB):
   ```bash
   adb devices        # убедиться, что устройство видно и авторизовано
   adb install nexium-kiosk.apk
   adb shell dpm set-device-owner com.aistudio.nexiumhealth.qptwyx/.KioskDeviceAdminReceiver
   ```
   APK обязательно должен быть установлен **до** этой команды, и на устройстве всё ещё
   не должно быть ни одного аккаунта. Успешный вывод: `Success: Device owner set...`.

5. Запустить приложение — оно само:
   - включит Lock Task Mode (см. `KioskManager.configureLockTask` в `onCreate`,
     `startLockTask()` в `onResume` `MainActivity.kt`);
   - будет раз в час проверять `/api/app/latest-version` и тихо ставить обновления
     через `PackageInstaller` (см. `KioskManager.installSilently`), без диалогов.

### Снятие Device Owner (для сервисного обслуживания)

```bash
adb shell dpm remove-active-admin com.aistudio.nexiumhealth.qptwyx/.KioskDeviceAdminReceiver
```

Тоже выполняется только через adb — приложение намеренно не может снять с себя эти права.

---

## 2. Публикация обновлений приложения (auto-update)

Обновления раздаются собственным backend'ом (`/home/ubuntu/Documents/projectsgo/nexium`),
без Google Play.

1. Собрать релизный APK (`versionCode` в `app/build.gradle.kts` должен быть увеличен
   относительно предыдущего релиза).
2. Загрузить его через admin API (JWT-токен администратора):
   ```bash
   curl -X POST "https://nexium-health.com/api/admin/app-releases?version_code=<N>" \
     -H "Authorization: Bearer <JWT>" \
     -F "file=@nexium-kiosk-release.apk" \
     -F "version_name=1.4.2" \
     -F "release_notes=Что изменилось в этой версии"
   ```
   Релиз создаётся **неактивным** — киоски его ещё не видят.
3. Опубликовать релиз (сделать активным для всех киосков):
   ```bash
   curl -X POST "https://nexium-health.com/api/admin/app-releases/<id>/activate" \
     -H "Authorization: Bearer <JWT>"
   ```
   Активным может быть только один релиз одновременно — предыдущий автоматически
   деактивируется.
4. В течение часа все киоски подхватят новую версию:
   - **Device Owner устройства** — тихо, без вмешательства пользователя.
   - **Обычные устройства** — покажут диалог "Доступно обновление" на экране
     выбора языка/авторизации (не мешает середине медосмотра).

Откатить публикацию (если релиз оказался проблемным) — деактивировать его и
активировать предыдущий:
```bash
curl -X POST "https://nexium-health.com/api/admin/app-releases/<id>/deactivate" -H "Authorization: Bearer <JWT>"
curl -X POST "https://nexium-health.com/api/admin/app-releases/<previous_id>/activate" -H "Authorization: Bearer <JWT>"
```
