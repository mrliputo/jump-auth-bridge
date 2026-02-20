import { DeviceEventEmitter, Platform } from 'react-native';
import {
  AuthContext,
  ConfigureOptions,
  GetAuthContextOptions,
  JumpAuthError,
} from './types';
import { NativeJumpAuth } from './native/NativeJumpAuth';

const AUTH_CHANGED_EVENT = 'JumpAuthBridge:authChanged';

let configured: ConfigureOptions | null = null;

export const JumpAuth = {
  async configure(options: ConfigureOptions): Promise<void> {
    if (Platform.OS !== 'android') {
      throw new JumpAuthError('E_UNAVAILABLE', 'JumpAuth is only available on Android.');
    }
    if (!options?.hostPackage) {
      throw new JumpAuthError('E_INVALID_RESPONSE', 'configure() requires hostPackage');
    }
    configured = options;
    await NativeJumpAuth.configure(options);
  },

  async getAuthContext(options?: GetAuthContextOptions): Promise<AuthContext> {
    if (Platform.OS !== 'android') {
      throw new JumpAuthError('E_UNAVAILABLE', 'JumpAuth is only available on Android.');
    }
    if (!configured) {
      throw new JumpAuthError('E_NOT_CONFIGURED', 'Call JumpAuth.configure() first.');
    }
    const ctx = await NativeJumpAuth.getAuthContext(options);
    if (!ctx?.accessToken || !ctx?.userId || !ctx?.issuerPackage) {
      throw new JumpAuthError('E_INVALID_RESPONSE', 'Invalid AuthContext from host.');
    }
    return ctx;
  },

  /**
   * Subscribe to auth changes coming from host (optional; requires host to push updates).
   */
  subscribeAuthChanges(listener: (context: AuthContext | null, reason?: string) => void) {
    if (Platform.OS !== 'android') {
      return () => {};
    }
    const sub = DeviceEventEmitter.addListener(
      AUTH_CHANGED_EVENT,
      (payload: { context?: AuthContext; reason?: string }) => {
        listener(payload?.context ?? null, payload?.reason);
      }
    );
    return () => sub.remove();
  },
};

