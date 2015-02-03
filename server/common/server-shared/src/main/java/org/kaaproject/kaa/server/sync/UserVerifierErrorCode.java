package org.kaaproject.kaa.server.sync;

public enum UserVerifierErrorCode {
    NO_VERIFIER_CONFIGURED,
    TOKEN_INVALID,
    TOKEN_EXPIRED,
    INTERNAL_ERROR,
    CONNECTION_ERROR,
    REMOTE_ERROR,
    OTHER
}