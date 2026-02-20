"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.JumpAuth = void 0;
const react_native_1 = require("react-native");
const types_1 = require("./types");
const NativeJumpAuth_1 = require("./native/NativeJumpAuth");
const AUTH_CHANGED_EVENT = 'JumpAuthBridge:authChanged';
let configured = null;
exports.JumpAuth = {
    async configure(options) {
        if (react_native_1.Platform.OS !== 'android') {
            throw new types_1.JumpAuthError('E_UNAVAILABLE', 'JumpAuth is only available on Android.');
        }
        if (!(options === null || options === void 0 ? void 0 : options.hostPackage)) {
            throw new types_1.JumpAuthError('E_INVALID_RESPONSE', 'configure() requires hostPackage');
        }
        configured = options;
        await NativeJumpAuth_1.NativeJumpAuth.configure(options);
    },
    async getAuthContext(options) {
        if (react_native_1.Platform.OS !== 'android') {
            throw new types_1.JumpAuthError('E_UNAVAILABLE', 'JumpAuth is only available on Android.');
        }
        if (!configured) {
            throw new types_1.JumpAuthError('E_NOT_CONFIGURED', 'Call JumpAuth.configure() first.');
        }
        const ctx = await NativeJumpAuth_1.NativeJumpAuth.getAuthContext(options);
        if (!(ctx === null || ctx === void 0 ? void 0 : ctx.accessToken) || !(ctx === null || ctx === void 0 ? void 0 : ctx.userId) || !(ctx === null || ctx === void 0 ? void 0 : ctx.issuerPackage)) {
            throw new types_1.JumpAuthError('E_INVALID_RESPONSE', 'Invalid AuthContext from host.');
        }
        return ctx;
    },
    /**
     * Subscribe to auth changes coming from host (optional; requires host to push updates).
     */
    subscribeAuthChanges(listener) {
        if (react_native_1.Platform.OS !== 'android') {
            return () => { };
        }
        const sub = react_native_1.DeviceEventEmitter.addListener(AUTH_CHANGED_EVENT, (payload) => {
            var _a;
            listener((_a = payload === null || payload === void 0 ? void 0 : payload.context) !== null && _a !== void 0 ? _a : null, payload === null || payload === void 0 ? void 0 : payload.reason);
        });
        return () => sub.remove();
    },
};
