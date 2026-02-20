# @btpns/jump-auth-bridge

Library React Native (Android) untuk komunikasi auth antar **2 aplikasi** via IPC yang aman.

- **Host app**: `com.btpns.acquisition` (handle login, role, simpan token/user)
- **Client app**: `com.testappjump` (minta token/user ke host)

## Cara kerja singkat
Client melakukan **bind** ke service di host (Bound Service + AIDL). Host mengembalikan `AuthContext` (accessToken, expiry, user info). Aksesnya dibatasi dengan **permission protectionLevel=signature** (jadi hanya app yang ditandatangani cert yang sama).

## Install (dari Git)
Tambahkan dependency dari repo git kalian. Contoh:

```json
{
  "dependencies": {
    "@btpns/jump-auth-bridge": "git+ssh://git@YOUR_GIT_SERVER/your-group/jump-auth-bridge.git#main"
  }
}
```

## Pemakaian (di RN JS)
Di `com.testappjump`:

```ts
import { JumpAuth } from '@btpns/jump-auth-bridge';

await JumpAuth.configure({
  hostPackage: 'com.btpns.acquisition',
  // optional kalau class servicenya beda
  // hostServiceClass: 'com.btpns.acquisition.jump.JumpAuthService',
  timeoutMs: 3000,
});

const ctx = await JumpAuth.getAuthContext({ forceRefresh: false });
console.log(ctx.accessToken, ctx.userId, ctx.roles);
```

## Integrasi Android di HOST (com.btpns.acquisition)
Kamu WAJIB menambahkan service + permission di host app. Library ini hanya menyediakan kontrak AIDL dan client binder.

### 1) Tambahkan permission signature + service
Di `android/app/src/main/AndroidManifest.xml` (host):

- Define permission signature:
  - `com.btpns.acquisition.permission.JUMP_AUTH`
- Declare exported bound service pakai permission itu.

Contoh (sesuaikan package kalian):

```xml
<manifest>
  <permission
    android:name="com.btpns.acquisition.permission.JUMP_AUTH"
    android:protectionLevel="signature" />

  <application>
    <service
      android:name="com.btpns.jump.JumpAuthService"
      android:exported="true"
      android:permission="com.btpns.acquisition.permission.JUMP_AUTH" />
  </application>
</manifest>
```

> Catatan: `android:name` service class bisa kalian taruh di package lain. Pastikan client memanggil `hostServiceClass` yang tepat.

### 2) Implementasi Service (AIDL Stub)
Host perlu implementasi `com.btpns.jump.ipc.IJumpAuthService.Stub` dan mengembalikan `AuthContext`.

Minimal logic yang disarankan di host:
- Cek caller package dari parameter `callerPackage`
- Ambil `Binder.getCallingUid()` lalu pastikan UID itu memang milik `callerPackage`
- (Optional) verifikasi signing certificate caller sama dengan host

## Integrasi Android di CLIENT (com.testappjump)
- Install library
- Panggil `JumpAuth.configure({ hostPackage })`
- Panggil `JumpAuth.getAuthContext()` kapanpun butuh token/user

## Catatan keamanan
- Jangan kirim **refreshToken** lewat IPC.
- Dengan permission `signature`, kedua app harus ditandatangani cert yang sama (release keystore yang sama).
- Untuk debug, pastikan juga debug keystore sama kalau mau test signature permission.

## Status
- Fokus saat ini: request/response `getAuthContext()`.
- Callback `subscribeAuthChanges()` sudah disiapkan di JS, tapi butuh implementasi push event dari host (akan ditambahkan berikutnya).

