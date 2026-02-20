"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.JumpAuthError = void 0;
class JumpAuthError extends Error {
    constructor(code, message, cause) {
        super(message);
        this.name = 'JumpAuthError';
        this.code = code;
        this.cause = cause;
    }
}
exports.JumpAuthError = JumpAuthError;
