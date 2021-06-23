import * as $protobuf from "protobufjs";
/** Namespace server. */
export namespace server {

    /** Properties of an UploadAvatar. */
    interface IUploadAvatar {

        /** UploadAvatar id */
        id?: (string|null);

        /** UploadAvatar data */
        data?: (Uint8Array|null);
    }

    /** Represents an UploadAvatar. */
    class UploadAvatar implements IUploadAvatar {

        /**
         * Constructs a new UploadAvatar.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IUploadAvatar);

        /** UploadAvatar id. */
        public id: string;

        /** UploadAvatar data. */
        public data: Uint8Array;

        /**
         * Creates a new UploadAvatar instance using the specified properties.
         * @param [properties] Properties to set
         * @returns UploadAvatar instance
         */
        public static create(properties?: server.IUploadAvatar): server.UploadAvatar;

        /**
         * Encodes the specified UploadAvatar message. Does not implicitly {@link server.UploadAvatar.verify|verify} messages.
         * @param message UploadAvatar message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IUploadAvatar, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified UploadAvatar message, length delimited. Does not implicitly {@link server.UploadAvatar.verify|verify} messages.
         * @param message UploadAvatar message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IUploadAvatar, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an UploadAvatar message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns UploadAvatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.UploadAvatar;

        /**
         * Decodes an UploadAvatar message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns UploadAvatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.UploadAvatar;

        /**
         * Verifies an UploadAvatar message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an UploadAvatar message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns UploadAvatar
         */
        public static fromObject(object: { [k: string]: any }): server.UploadAvatar;

        /**
         * Creates a plain object from an UploadAvatar message. Also converts values to other types if specified.
         * @param message UploadAvatar
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.UploadAvatar, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this UploadAvatar to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an Avatar. */
    interface IAvatar {

        /** Avatar id */
        id?: (string|null);

        /** Avatar uid */
        uid?: (number|Long|null);
    }

    /** Represents an Avatar. */
    class Avatar implements IAvatar {

        /**
         * Constructs a new Avatar.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAvatar);

        /** Avatar id. */
        public id: string;

        /** Avatar uid. */
        public uid: (number|Long);

        /**
         * Creates a new Avatar instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Avatar instance
         */
        public static create(properties?: server.IAvatar): server.Avatar;

        /**
         * Encodes the specified Avatar message. Does not implicitly {@link server.Avatar.verify|verify} messages.
         * @param message Avatar message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAvatar, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Avatar message, length delimited. Does not implicitly {@link server.Avatar.verify|verify} messages.
         * @param message Avatar message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAvatar, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an Avatar message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Avatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Avatar;

        /**
         * Decodes an Avatar message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Avatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Avatar;

        /**
         * Verifies an Avatar message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an Avatar message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Avatar
         */
        public static fromObject(object: { [k: string]: any }): server.Avatar;

        /**
         * Creates a plain object from an Avatar message. Also converts values to other types if specified.
         * @param message Avatar
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Avatar, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Avatar to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an Avatars. */
    interface IAvatars {

        /** Avatars avatars */
        avatars?: (server.IAvatar[]|null);
    }

    /** Represents an Avatars. */
    class Avatars implements IAvatars {

        /**
         * Constructs a new Avatars.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAvatars);

        /** Avatars avatars. */
        public avatars: server.IAvatar[];

        /**
         * Creates a new Avatars instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Avatars instance
         */
        public static create(properties?: server.IAvatars): server.Avatars;

        /**
         * Encodes the specified Avatars message. Does not implicitly {@link server.Avatars.verify|verify} messages.
         * @param message Avatars message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAvatars, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Avatars message, length delimited. Does not implicitly {@link server.Avatars.verify|verify} messages.
         * @param message Avatars message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAvatars, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an Avatars message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Avatars
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Avatars;

        /**
         * Decodes an Avatars message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Avatars
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Avatars;

        /**
         * Verifies an Avatars message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an Avatars message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Avatars
         */
        public static fromObject(object: { [k: string]: any }): server.Avatars;

        /**
         * Creates a plain object from an Avatars message. Also converts values to other types if specified.
         * @param message Avatars
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Avatars, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Avatars to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an UploadGroupAvatar. */
    interface IUploadGroupAvatar {

        /** UploadGroupAvatar gid */
        gid?: (string|null);

        /** UploadGroupAvatar data */
        data?: (Uint8Array|null);
    }

    /** Represents an UploadGroupAvatar. */
    class UploadGroupAvatar implements IUploadGroupAvatar {

        /**
         * Constructs a new UploadGroupAvatar.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IUploadGroupAvatar);

        /** UploadGroupAvatar gid. */
        public gid: string;

        /** UploadGroupAvatar data. */
        public data: Uint8Array;

        /**
         * Creates a new UploadGroupAvatar instance using the specified properties.
         * @param [properties] Properties to set
         * @returns UploadGroupAvatar instance
         */
        public static create(properties?: server.IUploadGroupAvatar): server.UploadGroupAvatar;

        /**
         * Encodes the specified UploadGroupAvatar message. Does not implicitly {@link server.UploadGroupAvatar.verify|verify} messages.
         * @param message UploadGroupAvatar message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IUploadGroupAvatar, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified UploadGroupAvatar message, length delimited. Does not implicitly {@link server.UploadGroupAvatar.verify|verify} messages.
         * @param message UploadGroupAvatar message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IUploadGroupAvatar, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an UploadGroupAvatar message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns UploadGroupAvatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.UploadGroupAvatar;

        /**
         * Decodes an UploadGroupAvatar message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns UploadGroupAvatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.UploadGroupAvatar;

        /**
         * Verifies an UploadGroupAvatar message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an UploadGroupAvatar message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns UploadGroupAvatar
         */
        public static fromObject(object: { [k: string]: any }): server.UploadGroupAvatar;

        /**
         * Creates a plain object from an UploadGroupAvatar message. Also converts values to other types if specified.
         * @param message UploadGroupAvatar
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.UploadGroupAvatar, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this UploadGroupAvatar to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a CertMessage. */
    interface ICertMessage {

        /** CertMessage timestamp */
        timestamp?: (number|Long|null);

        /** CertMessage serverKey */
        serverKey?: (Uint8Array|null);
    }

    /** Represents a CertMessage. */
    class CertMessage implements ICertMessage {

        /**
         * Constructs a new CertMessage.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ICertMessage);

        /** CertMessage timestamp. */
        public timestamp: (number|Long);

        /** CertMessage serverKey. */
        public serverKey: Uint8Array;

        /**
         * Creates a new CertMessage instance using the specified properties.
         * @param [properties] Properties to set
         * @returns CertMessage instance
         */
        public static create(properties?: server.ICertMessage): server.CertMessage;

        /**
         * Encodes the specified CertMessage message. Does not implicitly {@link server.CertMessage.verify|verify} messages.
         * @param message CertMessage message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ICertMessage, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified CertMessage message, length delimited. Does not implicitly {@link server.CertMessage.verify|verify} messages.
         * @param message CertMessage message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ICertMessage, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a CertMessage message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns CertMessage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.CertMessage;

        /**
         * Decodes a CertMessage message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns CertMessage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.CertMessage;

        /**
         * Verifies a CertMessage message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a CertMessage message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns CertMessage
         */
        public static fromObject(object: { [k: string]: any }): server.CertMessage;

        /**
         * Creates a plain object from a CertMessage message. Also converts values to other types if specified.
         * @param message CertMessage
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.CertMessage, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this CertMessage to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a ClientMode. */
    interface IClientMode {

        /** ClientMode mode */
        mode?: (server.ClientMode.Mode|null);
    }

    /** Represents a ClientMode. */
    class ClientMode implements IClientMode {

        /**
         * Constructs a new ClientMode.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IClientMode);

        /** ClientMode mode. */
        public mode: server.ClientMode.Mode;

        /**
         * Creates a new ClientMode instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ClientMode instance
         */
        public static create(properties?: server.IClientMode): server.ClientMode;

        /**
         * Encodes the specified ClientMode message. Does not implicitly {@link server.ClientMode.verify|verify} messages.
         * @param message ClientMode message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IClientMode, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ClientMode message, length delimited. Does not implicitly {@link server.ClientMode.verify|verify} messages.
         * @param message ClientMode message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IClientMode, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ClientMode message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ClientMode
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ClientMode;

        /**
         * Decodes a ClientMode message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ClientMode
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ClientMode;

        /**
         * Verifies a ClientMode message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ClientMode message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ClientMode
         */
        public static fromObject(object: { [k: string]: any }): server.ClientMode;

        /**
         * Creates a plain object from a ClientMode message. Also converts values to other types if specified.
         * @param message ClientMode
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ClientMode, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ClientMode to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ClientMode {

        /** Mode enum. */
        enum Mode {
            ACTIVE = 0,
            PASSIVE = 1
        }
    }

    /** Properties of a ClientVersion. */
    interface IClientVersion {

        /** ClientVersion version */
        version?: (string|null);

        /** ClientVersion expiresInSeconds */
        expiresInSeconds?: (number|Long|null);
    }

    /** Represents a ClientVersion. */
    class ClientVersion implements IClientVersion {

        /**
         * Constructs a new ClientVersion.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IClientVersion);

        /** ClientVersion version. */
        public version: string;

        /** ClientVersion expiresInSeconds. */
        public expiresInSeconds: (number|Long);

        /**
         * Creates a new ClientVersion instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ClientVersion instance
         */
        public static create(properties?: server.IClientVersion): server.ClientVersion;

        /**
         * Encodes the specified ClientVersion message. Does not implicitly {@link server.ClientVersion.verify|verify} messages.
         * @param message ClientVersion message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IClientVersion, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ClientVersion message, length delimited. Does not implicitly {@link server.ClientVersion.verify|verify} messages.
         * @param message ClientVersion message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IClientVersion, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ClientVersion message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ClientVersion
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ClientVersion;

        /**
         * Decodes a ClientVersion message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ClientVersion
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ClientVersion;

        /**
         * Verifies a ClientVersion message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ClientVersion message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ClientVersion
         */
        public static fromObject(object: { [k: string]: any }): server.ClientVersion;

        /**
         * Creates a plain object from a ClientVersion message. Also converts values to other types if specified.
         * @param message ClientVersion
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ClientVersion, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ClientVersion to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a ClientLog. */
    interface IClientLog {

        /** ClientLog counts */
        counts?: (server.ICount[]|null);

        /** ClientLog events */
        events?: (server.IEventData[]|null);
    }

    /** Represents a ClientLog. */
    class ClientLog implements IClientLog {

        /**
         * Constructs a new ClientLog.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IClientLog);

        /** ClientLog counts. */
        public counts: server.ICount[];

        /** ClientLog events. */
        public events: server.IEventData[];

        /**
         * Creates a new ClientLog instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ClientLog instance
         */
        public static create(properties?: server.IClientLog): server.ClientLog;

        /**
         * Encodes the specified ClientLog message. Does not implicitly {@link server.ClientLog.verify|verify} messages.
         * @param message ClientLog message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IClientLog, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ClientLog message, length delimited. Does not implicitly {@link server.ClientLog.verify|verify} messages.
         * @param message ClientLog message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IClientLog, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ClientLog message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ClientLog
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ClientLog;

        /**
         * Decodes a ClientLog message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ClientLog
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ClientLog;

        /**
         * Verifies a ClientLog message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ClientLog message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ClientLog
         */
        public static fromObject(object: { [k: string]: any }): server.ClientLog;

        /**
         * Creates a plain object from a ClientLog message. Also converts values to other types if specified.
         * @param message ClientLog
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ClientLog, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ClientLog to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a Count. */
    interface ICount {

        /** Count namespace */
        namespace?: (string|null);

        /** Count metric */
        metric?: (string|null);

        /** Count count */
        count?: (number|Long|null);

        /** Count dims */
        dims?: (server.IDim[]|null);
    }

    /** Represents a Count. */
    class Count implements ICount {

        /**
         * Constructs a new Count.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ICount);

        /** Count namespace. */
        public namespace: string;

        /** Count metric. */
        public metric: string;

        /** Count count. */
        public count: (number|Long);

        /** Count dims. */
        public dims: server.IDim[];

        /**
         * Creates a new Count instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Count instance
         */
        public static create(properties?: server.ICount): server.Count;

        /**
         * Encodes the specified Count message. Does not implicitly {@link server.Count.verify|verify} messages.
         * @param message Count message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ICount, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Count message, length delimited. Does not implicitly {@link server.Count.verify|verify} messages.
         * @param message Count message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ICount, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Count message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Count
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Count;

        /**
         * Decodes a Count message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Count
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Count;

        /**
         * Verifies a Count message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Count message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Count
         */
        public static fromObject(object: { [k: string]: any }): server.Count;

        /**
         * Creates a plain object from a Count message. Also converts values to other types if specified.
         * @param message Count
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Count, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Count to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a Dim. */
    interface IDim {

        /** Dim name */
        name?: (string|null);

        /** Dim value */
        value?: (string|null);
    }

    /** Represents a Dim. */
    class Dim implements IDim {

        /**
         * Constructs a new Dim.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IDim);

        /** Dim name. */
        public name: string;

        /** Dim value. */
        public value: string;

        /**
         * Creates a new Dim instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Dim instance
         */
        public static create(properties?: server.IDim): server.Dim;

        /**
         * Encodes the specified Dim message. Does not implicitly {@link server.Dim.verify|verify} messages.
         * @param message Dim message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IDim, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Dim message, length delimited. Does not implicitly {@link server.Dim.verify|verify} messages.
         * @param message Dim message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IDim, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Dim message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Dim
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Dim;

        /**
         * Decodes a Dim message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Dim
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Dim;

        /**
         * Verifies a Dim message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Dim message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Dim
         */
        public static fromObject(object: { [k: string]: any }): server.Dim;

        /**
         * Creates a plain object from a Dim message. Also converts values to other types if specified.
         * @param message Dim
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Dim, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Dim to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a Contact. */
    interface IContact {

        /** Contact action */
        action?: (server.Contact.Action|null);

        /** Contact raw */
        raw?: (string|null);

        /** Contact normalized */
        normalized?: (string|null);

        /** Contact uid */
        uid?: (number|Long|null);

        /** Contact avatarId */
        avatarId?: (string|null);

        /** Contact role */
        role?: (server.Contact.Role|null);

        /** Contact name */
        name?: (string|null);

        /** Contact numPotentialFriends */
        numPotentialFriends?: (number|Long|null);
    }

    /** Represents a Contact. */
    class Contact implements IContact {

        /**
         * Constructs a new Contact.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IContact);

        /** Contact action. */
        public action: server.Contact.Action;

        /** Contact raw. */
        public raw: string;

        /** Contact normalized. */
        public normalized: string;

        /** Contact uid. */
        public uid: (number|Long);

        /** Contact avatarId. */
        public avatarId: string;

        /** Contact role. */
        public role: server.Contact.Role;

        /** Contact name. */
        public name: string;

        /** Contact numPotentialFriends. */
        public numPotentialFriends: (number|Long);

        /**
         * Creates a new Contact instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Contact instance
         */
        public static create(properties?: server.IContact): server.Contact;

        /**
         * Encodes the specified Contact message. Does not implicitly {@link server.Contact.verify|verify} messages.
         * @param message Contact message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IContact, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Contact message, length delimited. Does not implicitly {@link server.Contact.verify|verify} messages.
         * @param message Contact message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IContact, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Contact message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Contact
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Contact;

        /**
         * Decodes a Contact message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Contact
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Contact;

        /**
         * Verifies a Contact message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Contact message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Contact
         */
        public static fromObject(object: { [k: string]: any }): server.Contact;

        /**
         * Creates a plain object from a Contact message. Also converts values to other types if specified.
         * @param message Contact
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Contact, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Contact to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace Contact {

        /** Action enum. */
        enum Action {
            ADD = 0,
            DELETE = 1
        }

        /** Role enum. */
        enum Role {
            FRIENDS = 0,
            NONE = 1
        }
    }

    /** Properties of a ContactList. */
    interface IContactList {

        /** ContactList type */
        type?: (server.ContactList.Type|null);

        /** ContactList syncId */
        syncId?: (string|null);

        /** ContactList batchIndex */
        batchIndex?: (number|null);

        /** ContactList isLast */
        isLast?: (boolean|null);

        /** ContactList contacts */
        contacts?: (server.IContact[]|null);
    }

    /** Represents a ContactList. */
    class ContactList implements IContactList {

        /**
         * Constructs a new ContactList.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IContactList);

        /** ContactList type. */
        public type: server.ContactList.Type;

        /** ContactList syncId. */
        public syncId: string;

        /** ContactList batchIndex. */
        public batchIndex: number;

        /** ContactList isLast. */
        public isLast: boolean;

        /** ContactList contacts. */
        public contacts: server.IContact[];

        /**
         * Creates a new ContactList instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ContactList instance
         */
        public static create(properties?: server.IContactList): server.ContactList;

        /**
         * Encodes the specified ContactList message. Does not implicitly {@link server.ContactList.verify|verify} messages.
         * @param message ContactList message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IContactList, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ContactList message, length delimited. Does not implicitly {@link server.ContactList.verify|verify} messages.
         * @param message ContactList message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IContactList, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ContactList message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ContactList
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ContactList;

        /**
         * Decodes a ContactList message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ContactList
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ContactList;

        /**
         * Verifies a ContactList message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ContactList message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ContactList
         */
        public static fromObject(object: { [k: string]: any }): server.ContactList;

        /**
         * Creates a plain object from a ContactList message. Also converts values to other types if specified.
         * @param message ContactList
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ContactList, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ContactList to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ContactList {

        /** Type enum. */
        enum Type {
            FULL = 0,
            DELTA = 1,
            NORMAL = 2,
            FRIEND_NOTICE = 3,
            INVITER_NOTICE = 4,
            DELETE_NOTICE = 5,
            CONTACT_NOTICE = 6
        }
    }

    /** Properties of a ContactHash. */
    interface IContactHash {

        /** ContactHash hash */
        hash?: (Uint8Array|null);
    }

    /** Represents a ContactHash. */
    class ContactHash implements IContactHash {

        /**
         * Constructs a new ContactHash.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IContactHash);

        /** ContactHash hash. */
        public hash: Uint8Array;

        /**
         * Creates a new ContactHash instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ContactHash instance
         */
        public static create(properties?: server.IContactHash): server.ContactHash;

        /**
         * Encodes the specified ContactHash message. Does not implicitly {@link server.ContactHash.verify|verify} messages.
         * @param message ContactHash message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IContactHash, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ContactHash message, length delimited. Does not implicitly {@link server.ContactHash.verify|verify} messages.
         * @param message ContactHash message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IContactHash, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ContactHash message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ContactHash
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ContactHash;

        /**
         * Decodes a ContactHash message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ContactHash
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ContactHash;

        /**
         * Verifies a ContactHash message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ContactHash message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ContactHash
         */
        public static fromObject(object: { [k: string]: any }): server.ContactHash;

        /**
         * Creates a plain object from a ContactHash message. Also converts values to other types if specified.
         * @param message ContactHash
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ContactHash, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ContactHash to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an Audience. */
    interface IAudience {

        /** Audience type */
        type?: (server.Audience.Type|null);

        /** Audience uids */
        uids?: ((number|Long)[]|null);
    }

    /** Represents an Audience. */
    class Audience implements IAudience {

        /**
         * Constructs a new Audience.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAudience);

        /** Audience type. */
        public type: server.Audience.Type;

        /** Audience uids. */
        public uids: (number|Long)[];

        /**
         * Creates a new Audience instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Audience instance
         */
        public static create(properties?: server.IAudience): server.Audience;

        /**
         * Encodes the specified Audience message. Does not implicitly {@link server.Audience.verify|verify} messages.
         * @param message Audience message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAudience, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Audience message, length delimited. Does not implicitly {@link server.Audience.verify|verify} messages.
         * @param message Audience message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAudience, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an Audience message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Audience
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Audience;

        /**
         * Decodes an Audience message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Audience
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Audience;

        /**
         * Verifies an Audience message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an Audience message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Audience
         */
        public static fromObject(object: { [k: string]: any }): server.Audience;

        /**
         * Creates a plain object from an Audience message. Also converts values to other types if specified.
         * @param message Audience
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Audience, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Audience to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace Audience {

        /** Type enum. */
        enum Type {
            ALL = 0,
            EXCEPT = 1,
            ONLY = 2
        }
    }

    /** Properties of a Post. */
    interface IPost {

        /** Post id */
        id?: (string|null);

        /** Post publisherUid */
        publisherUid?: (number|Long|null);

        /** Post payload */
        payload?: (Uint8Array|null);

        /** Post audience */
        audience?: (server.IAudience|null);

        /** Post timestamp */
        timestamp?: (number|Long|null);

        /** Post publisherName */
        publisherName?: (string|null);

        /** Post encPayload */
        encPayload?: (Uint8Array|null);
    }

    /** Represents a Post. */
    class Post implements IPost {

        /**
         * Constructs a new Post.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPost);

        /** Post id. */
        public id: string;

        /** Post publisherUid. */
        public publisherUid: (number|Long);

        /** Post payload. */
        public payload: Uint8Array;

        /** Post audience. */
        public audience?: (server.IAudience|null);

        /** Post timestamp. */
        public timestamp: (number|Long);

        /** Post publisherName. */
        public publisherName: string;

        /** Post encPayload. */
        public encPayload: Uint8Array;

        /**
         * Creates a new Post instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Post instance
         */
        public static create(properties?: server.IPost): server.Post;

        /**
         * Encodes the specified Post message. Does not implicitly {@link server.Post.verify|verify} messages.
         * @param message Post message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPost, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Post message, length delimited. Does not implicitly {@link server.Post.verify|verify} messages.
         * @param message Post message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPost, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Post message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Post
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Post;

        /**
         * Decodes a Post message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Post
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Post;

        /**
         * Verifies a Post message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Post message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Post
         */
        public static fromObject(object: { [k: string]: any }): server.Post;

        /**
         * Creates a plain object from a Post message. Also converts values to other types if specified.
         * @param message Post
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Post, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Post to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a Comment. */
    interface IComment {

        /** Comment id */
        id?: (string|null);

        /** Comment postId */
        postId?: (string|null);

        /** Comment parentCommentId */
        parentCommentId?: (string|null);

        /** Comment publisherUid */
        publisherUid?: (number|Long|null);

        /** Comment publisherName */
        publisherName?: (string|null);

        /** Comment payload */
        payload?: (Uint8Array|null);

        /** Comment timestamp */
        timestamp?: (number|Long|null);

        /** Comment encPayload */
        encPayload?: (Uint8Array|null);
    }

    /** Represents a Comment. */
    class Comment implements IComment {

        /**
         * Constructs a new Comment.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IComment);

        /** Comment id. */
        public id: string;

        /** Comment postId. */
        public postId: string;

        /** Comment parentCommentId. */
        public parentCommentId: string;

        /** Comment publisherUid. */
        public publisherUid: (number|Long);

        /** Comment publisherName. */
        public publisherName: string;

        /** Comment payload. */
        public payload: Uint8Array;

        /** Comment timestamp. */
        public timestamp: (number|Long);

        /** Comment encPayload. */
        public encPayload: Uint8Array;

        /**
         * Creates a new Comment instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Comment instance
         */
        public static create(properties?: server.IComment): server.Comment;

        /**
         * Encodes the specified Comment message. Does not implicitly {@link server.Comment.verify|verify} messages.
         * @param message Comment message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IComment, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Comment message, length delimited. Does not implicitly {@link server.Comment.verify|verify} messages.
         * @param message Comment message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IComment, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Comment message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Comment
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Comment;

        /**
         * Decodes a Comment message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Comment
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Comment;

        /**
         * Verifies a Comment message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Comment message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Comment
         */
        public static fromObject(object: { [k: string]: any }): server.Comment;

        /**
         * Creates a plain object from a Comment message. Also converts values to other types if specified.
         * @param message Comment
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Comment, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Comment to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a ShareStanza. */
    interface IShareStanza {

        /** ShareStanza uid */
        uid?: (number|Long|null);

        /** ShareStanza postIds */
        postIds?: (string[]|null);

        /** ShareStanza result */
        result?: (string|null);

        /** ShareStanza reason */
        reason?: (string|null);
    }

    /** Represents a ShareStanza. */
    class ShareStanza implements IShareStanza {

        /**
         * Constructs a new ShareStanza.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IShareStanza);

        /** ShareStanza uid. */
        public uid: (number|Long);

        /** ShareStanza postIds. */
        public postIds: string[];

        /** ShareStanza result. */
        public result: string;

        /** ShareStanza reason. */
        public reason: string;

        /**
         * Creates a new ShareStanza instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ShareStanza instance
         */
        public static create(properties?: server.IShareStanza): server.ShareStanza;

        /**
         * Encodes the specified ShareStanza message. Does not implicitly {@link server.ShareStanza.verify|verify} messages.
         * @param message ShareStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IShareStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ShareStanza message, length delimited. Does not implicitly {@link server.ShareStanza.verify|verify} messages.
         * @param message ShareStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IShareStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ShareStanza message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ShareStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ShareStanza;

        /**
         * Decodes a ShareStanza message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ShareStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ShareStanza;

        /**
         * Verifies a ShareStanza message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ShareStanza message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ShareStanza
         */
        public static fromObject(object: { [k: string]: any }): server.ShareStanza;

        /**
         * Creates a plain object from a ShareStanza message. Also converts values to other types if specified.
         * @param message ShareStanza
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ShareStanza, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ShareStanza to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a FeedItem. */
    interface IFeedItem {

        /** FeedItem action */
        action?: (server.FeedItem.Action|null);

        /** FeedItem post */
        post?: (server.IPost|null);

        /** FeedItem comment */
        comment?: (server.IComment|null);

        /** FeedItem shareStanzas */
        shareStanzas?: (server.IShareStanza[]|null);
    }

    /** Represents a FeedItem. */
    class FeedItem implements IFeedItem {

        /**
         * Constructs a new FeedItem.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IFeedItem);

        /** FeedItem action. */
        public action: server.FeedItem.Action;

        /** FeedItem post. */
        public post?: (server.IPost|null);

        /** FeedItem comment. */
        public comment?: (server.IComment|null);

        /** FeedItem shareStanzas. */
        public shareStanzas: server.IShareStanza[];

        /** FeedItem item. */
        public item?: ("post"|"comment");

        /**
         * Creates a new FeedItem instance using the specified properties.
         * @param [properties] Properties to set
         * @returns FeedItem instance
         */
        public static create(properties?: server.IFeedItem): server.FeedItem;

        /**
         * Encodes the specified FeedItem message. Does not implicitly {@link server.FeedItem.verify|verify} messages.
         * @param message FeedItem message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IFeedItem, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified FeedItem message, length delimited. Does not implicitly {@link server.FeedItem.verify|verify} messages.
         * @param message FeedItem message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IFeedItem, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a FeedItem message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns FeedItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.FeedItem;

        /**
         * Decodes a FeedItem message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns FeedItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.FeedItem;

        /**
         * Verifies a FeedItem message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a FeedItem message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns FeedItem
         */
        public static fromObject(object: { [k: string]: any }): server.FeedItem;

        /**
         * Creates a plain object from a FeedItem message. Also converts values to other types if specified.
         * @param message FeedItem
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.FeedItem, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this FeedItem to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace FeedItem {

        /** Action enum. */
        enum Action {
            PUBLISH = 0,
            RETRACT = 1,
            SHARE = 2
        }
    }

    /** Properties of a FeedItems. */
    interface IFeedItems {

        /** FeedItems uid */
        uid?: (number|Long|null);

        /** FeedItems items */
        items?: (server.IFeedItem[]|null);
    }

    /** Represents a FeedItems. */
    class FeedItems implements IFeedItems {

        /**
         * Constructs a new FeedItems.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IFeedItems);

        /** FeedItems uid. */
        public uid: (number|Long);

        /** FeedItems items. */
        public items: server.IFeedItem[];

        /**
         * Creates a new FeedItems instance using the specified properties.
         * @param [properties] Properties to set
         * @returns FeedItems instance
         */
        public static create(properties?: server.IFeedItems): server.FeedItems;

        /**
         * Encodes the specified FeedItems message. Does not implicitly {@link server.FeedItems.verify|verify} messages.
         * @param message FeedItems message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IFeedItems, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified FeedItems message, length delimited. Does not implicitly {@link server.FeedItems.verify|verify} messages.
         * @param message FeedItems message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IFeedItems, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a FeedItems message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns FeedItems
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.FeedItems;

        /**
         * Decodes a FeedItems message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns FeedItems
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.FeedItems;

        /**
         * Verifies a FeedItems message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a FeedItems message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns FeedItems
         */
        public static fromObject(object: { [k: string]: any }): server.FeedItems;

        /**
         * Creates a plain object from a FeedItems message. Also converts values to other types if specified.
         * @param message FeedItems
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.FeedItems, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this FeedItems to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a SenderStateBundle. */
    interface ISenderStateBundle {

        /** SenderStateBundle encSenderState */
        encSenderState?: (Uint8Array|null);

        /** SenderStateBundle uid */
        uid?: (number|Long|null);
    }

    /** Represents a SenderStateBundle. */
    class SenderStateBundle implements ISenderStateBundle {

        /**
         * Constructs a new SenderStateBundle.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISenderStateBundle);

        /** SenderStateBundle encSenderState. */
        public encSenderState: Uint8Array;

        /** SenderStateBundle uid. */
        public uid: (number|Long);

        /**
         * Creates a new SenderStateBundle instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SenderStateBundle instance
         */
        public static create(properties?: server.ISenderStateBundle): server.SenderStateBundle;

        /**
         * Encodes the specified SenderStateBundle message. Does not implicitly {@link server.SenderStateBundle.verify|verify} messages.
         * @param message SenderStateBundle message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISenderStateBundle, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SenderStateBundle message, length delimited. Does not implicitly {@link server.SenderStateBundle.verify|verify} messages.
         * @param message SenderStateBundle message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISenderStateBundle, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SenderStateBundle message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SenderStateBundle
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SenderStateBundle;

        /**
         * Decodes a SenderStateBundle message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SenderStateBundle
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SenderStateBundle;

        /**
         * Verifies a SenderStateBundle message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SenderStateBundle message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SenderStateBundle
         */
        public static fromObject(object: { [k: string]: any }): server.SenderStateBundle;

        /**
         * Creates a plain object from a SenderStateBundle message. Also converts values to other types if specified.
         * @param message SenderStateBundle
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SenderStateBundle, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SenderStateBundle to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a GroupFeedItem. */
    interface IGroupFeedItem {

        /** GroupFeedItem action */
        action?: (server.GroupFeedItem.Action|null);

        /** GroupFeedItem gid */
        gid?: (string|null);

        /** GroupFeedItem name */
        name?: (string|null);

        /** GroupFeedItem avatarId */
        avatarId?: (string|null);

        /** GroupFeedItem post */
        post?: (server.IPost|null);

        /** GroupFeedItem comment */
        comment?: (server.IComment|null);

        /** GroupFeedItem senderStateBundles */
        senderStateBundles?: (server.ISenderStateBundle[]|null);

        /** GroupFeedItem encSenderState */
        encSenderState?: (Uint8Array|null);

        /** GroupFeedItem audienceHash */
        audienceHash?: (Uint8Array|null);
    }

    /** Represents a GroupFeedItem. */
    class GroupFeedItem implements IGroupFeedItem {

        /**
         * Constructs a new GroupFeedItem.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupFeedItem);

        /** GroupFeedItem action. */
        public action: server.GroupFeedItem.Action;

        /** GroupFeedItem gid. */
        public gid: string;

        /** GroupFeedItem name. */
        public name: string;

        /** GroupFeedItem avatarId. */
        public avatarId: string;

        /** GroupFeedItem post. */
        public post?: (server.IPost|null);

        /** GroupFeedItem comment. */
        public comment?: (server.IComment|null);

        /** GroupFeedItem senderStateBundles. */
        public senderStateBundles: server.ISenderStateBundle[];

        /** GroupFeedItem encSenderState. */
        public encSenderState: Uint8Array;

        /** GroupFeedItem audienceHash. */
        public audienceHash: Uint8Array;

        /** GroupFeedItem item. */
        public item?: ("post"|"comment");

        /**
         * Creates a new GroupFeedItem instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupFeedItem instance
         */
        public static create(properties?: server.IGroupFeedItem): server.GroupFeedItem;

        /**
         * Encodes the specified GroupFeedItem message. Does not implicitly {@link server.GroupFeedItem.verify|verify} messages.
         * @param message GroupFeedItem message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupFeedItem, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupFeedItem message, length delimited. Does not implicitly {@link server.GroupFeedItem.verify|verify} messages.
         * @param message GroupFeedItem message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupFeedItem, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupFeedItem message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupFeedItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupFeedItem;

        /**
         * Decodes a GroupFeedItem message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupFeedItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupFeedItem;

        /**
         * Verifies a GroupFeedItem message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupFeedItem message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupFeedItem
         */
        public static fromObject(object: { [k: string]: any }): server.GroupFeedItem;

        /**
         * Creates a plain object from a GroupFeedItem message. Also converts values to other types if specified.
         * @param message GroupFeedItem
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupFeedItem, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupFeedItem to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GroupFeedItem {

        /** Action enum. */
        enum Action {
            PUBLISH = 0,
            RETRACT = 1
        }
    }

    /** Properties of a GroupFeedItems. */
    interface IGroupFeedItems {

        /** GroupFeedItems gid */
        gid?: (string|null);

        /** GroupFeedItems name */
        name?: (string|null);

        /** GroupFeedItems avatarId */
        avatarId?: (string|null);

        /** GroupFeedItems items */
        items?: (server.IGroupFeedItem[]|null);
    }

    /** Represents a GroupFeedItems. */
    class GroupFeedItems implements IGroupFeedItems {

        /**
         * Constructs a new GroupFeedItems.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupFeedItems);

        /** GroupFeedItems gid. */
        public gid: string;

        /** GroupFeedItems name. */
        public name: string;

        /** GroupFeedItems avatarId. */
        public avatarId: string;

        /** GroupFeedItems items. */
        public items: server.IGroupFeedItem[];

        /**
         * Creates a new GroupFeedItems instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupFeedItems instance
         */
        public static create(properties?: server.IGroupFeedItems): server.GroupFeedItems;

        /**
         * Encodes the specified GroupFeedItems message. Does not implicitly {@link server.GroupFeedItems.verify|verify} messages.
         * @param message GroupFeedItems message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupFeedItems, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupFeedItems message, length delimited. Does not implicitly {@link server.GroupFeedItems.verify|verify} messages.
         * @param message GroupFeedItems message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupFeedItems, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupFeedItems message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupFeedItems
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupFeedItems;

        /**
         * Decodes a GroupFeedItems message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupFeedItems
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupFeedItems;

        /**
         * Verifies a GroupFeedItems message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupFeedItems message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupFeedItems
         */
        public static fromObject(object: { [k: string]: any }): server.GroupFeedItems;

        /**
         * Creates a plain object from a GroupFeedItems message. Also converts values to other types if specified.
         * @param message GroupFeedItems
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupFeedItems, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupFeedItems to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a GroupMember. */
    interface IGroupMember {

        /** GroupMember action */
        action?: (server.GroupMember.Action|null);

        /** GroupMember uid */
        uid?: (number|Long|null);

        /** GroupMember type */
        type?: (server.GroupMember.Type|null);

        /** GroupMember name */
        name?: (string|null);

        /** GroupMember avatarId */
        avatarId?: (string|null);

        /** GroupMember result */
        result?: (string|null);

        /** GroupMember reason */
        reason?: (string|null);

        /** GroupMember identityKey */
        identityKey?: (Uint8Array|null);
    }

    /** Represents a GroupMember. */
    class GroupMember implements IGroupMember {

        /**
         * Constructs a new GroupMember.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupMember);

        /** GroupMember action. */
        public action: server.GroupMember.Action;

        /** GroupMember uid. */
        public uid: (number|Long);

        /** GroupMember type. */
        public type: server.GroupMember.Type;

        /** GroupMember name. */
        public name: string;

        /** GroupMember avatarId. */
        public avatarId: string;

        /** GroupMember result. */
        public result: string;

        /** GroupMember reason. */
        public reason: string;

        /** GroupMember identityKey. */
        public identityKey: Uint8Array;

        /**
         * Creates a new GroupMember instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupMember instance
         */
        public static create(properties?: server.IGroupMember): server.GroupMember;

        /**
         * Encodes the specified GroupMember message. Does not implicitly {@link server.GroupMember.verify|verify} messages.
         * @param message GroupMember message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupMember, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupMember message, length delimited. Does not implicitly {@link server.GroupMember.verify|verify} messages.
         * @param message GroupMember message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupMember, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupMember message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupMember
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupMember;

        /**
         * Decodes a GroupMember message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupMember
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupMember;

        /**
         * Verifies a GroupMember message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupMember message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupMember
         */
        public static fromObject(object: { [k: string]: any }): server.GroupMember;

        /**
         * Creates a plain object from a GroupMember message. Also converts values to other types if specified.
         * @param message GroupMember
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupMember, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupMember to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GroupMember {

        /** Action enum. */
        enum Action {
            ADD = 0,
            REMOVE = 1,
            PROMOTE = 2,
            DEMOTE = 3,
            LEAVE = 4,
            JOIN = 5
        }

        /** Type enum. */
        enum Type {
            MEMBER = 0,
            ADMIN = 1
        }
    }

    /** Properties of a GroupStanza. */
    interface IGroupStanza {

        /** GroupStanza action */
        action?: (server.GroupStanza.Action|null);

        /** GroupStanza gid */
        gid?: (string|null);

        /** GroupStanza name */
        name?: (string|null);

        /** GroupStanza avatarId */
        avatarId?: (string|null);

        /** GroupStanza senderUid */
        senderUid?: (number|Long|null);

        /** GroupStanza senderName */
        senderName?: (string|null);

        /** GroupStanza members */
        members?: (server.IGroupMember[]|null);

        /** GroupStanza background */
        background?: (string|null);

        /** GroupStanza audienceHash */
        audienceHash?: (Uint8Array|null);
    }

    /** Represents a GroupStanza. */
    class GroupStanza implements IGroupStanza {

        /**
         * Constructs a new GroupStanza.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupStanza);

        /** GroupStanza action. */
        public action: server.GroupStanza.Action;

        /** GroupStanza gid. */
        public gid: string;

        /** GroupStanza name. */
        public name: string;

        /** GroupStanza avatarId. */
        public avatarId: string;

        /** GroupStanza senderUid. */
        public senderUid: (number|Long);

        /** GroupStanza senderName. */
        public senderName: string;

        /** GroupStanza members. */
        public members: server.IGroupMember[];

        /** GroupStanza background. */
        public background: string;

        /** GroupStanza audienceHash. */
        public audienceHash: Uint8Array;

        /**
         * Creates a new GroupStanza instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupStanza instance
         */
        public static create(properties?: server.IGroupStanza): server.GroupStanza;

        /**
         * Encodes the specified GroupStanza message. Does not implicitly {@link server.GroupStanza.verify|verify} messages.
         * @param message GroupStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupStanza message, length delimited. Does not implicitly {@link server.GroupStanza.verify|verify} messages.
         * @param message GroupStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupStanza message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupStanza;

        /**
         * Decodes a GroupStanza message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupStanza;

        /**
         * Verifies a GroupStanza message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupStanza message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupStanza
         */
        public static fromObject(object: { [k: string]: any }): server.GroupStanza;

        /**
         * Creates a plain object from a GroupStanza message. Also converts values to other types if specified.
         * @param message GroupStanza
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupStanza, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupStanza to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GroupStanza {

        /** Action enum. */
        enum Action {
            SET = 0,
            GET = 1,
            CREATE = 2,
            DELETE = 3,
            LEAVE = 4,
            CHANGE_AVATAR = 5,
            CHANGE_NAME = 6,
            MODIFY_ADMINS = 7,
            MODIFY_MEMBERS = 8,
            AUTO_PROMOTE_ADMINS = 9,
            SET_NAME = 10,
            JOIN = 11,
            PREVIEW = 12,
            SET_BACKGROUND = 13,
            GET_MEMBER_IDENTITY_KEYS = 14
        }
    }

    /** Properties of a GroupChat. */
    interface IGroupChat {

        /** GroupChat gid */
        gid?: (string|null);

        /** GroupChat name */
        name?: (string|null);

        /** GroupChat avatarId */
        avatarId?: (string|null);

        /** GroupChat senderUid */
        senderUid?: (number|Long|null);

        /** GroupChat senderName */
        senderName?: (string|null);

        /** GroupChat timestamp */
        timestamp?: (number|Long|null);

        /** GroupChat payload */
        payload?: (Uint8Array|null);
    }

    /** Represents a GroupChat. */
    class GroupChat implements IGroupChat {

        /**
         * Constructs a new GroupChat.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupChat);

        /** GroupChat gid. */
        public gid: string;

        /** GroupChat name. */
        public name: string;

        /** GroupChat avatarId. */
        public avatarId: string;

        /** GroupChat senderUid. */
        public senderUid: (number|Long);

        /** GroupChat senderName. */
        public senderName: string;

        /** GroupChat timestamp. */
        public timestamp: (number|Long);

        /** GroupChat payload. */
        public payload: Uint8Array;

        /**
         * Creates a new GroupChat instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupChat instance
         */
        public static create(properties?: server.IGroupChat): server.GroupChat;

        /**
         * Encodes the specified GroupChat message. Does not implicitly {@link server.GroupChat.verify|verify} messages.
         * @param message GroupChat message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupChat, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupChat message, length delimited. Does not implicitly {@link server.GroupChat.verify|verify} messages.
         * @param message GroupChat message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupChat, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupChat message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupChat
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupChat;

        /**
         * Decodes a GroupChat message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupChat
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupChat;

        /**
         * Verifies a GroupChat message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupChat message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupChat
         */
        public static fromObject(object: { [k: string]: any }): server.GroupChat;

        /**
         * Creates a plain object from a GroupChat message. Also converts values to other types if specified.
         * @param message GroupChat
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupChat, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupChat to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a GroupsStanza. */
    interface IGroupsStanza {

        /** GroupsStanza action */
        action?: (server.GroupsStanza.Action|null);

        /** GroupsStanza groupStanzas */
        groupStanzas?: (server.IGroupStanza[]|null);
    }

    /** Represents a GroupsStanza. */
    class GroupsStanza implements IGroupsStanza {

        /**
         * Constructs a new GroupsStanza.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupsStanza);

        /** GroupsStanza action. */
        public action: server.GroupsStanza.Action;

        /** GroupsStanza groupStanzas. */
        public groupStanzas: server.IGroupStanza[];

        /**
         * Creates a new GroupsStanza instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupsStanza instance
         */
        public static create(properties?: server.IGroupsStanza): server.GroupsStanza;

        /**
         * Encodes the specified GroupsStanza message. Does not implicitly {@link server.GroupsStanza.verify|verify} messages.
         * @param message GroupsStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupsStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupsStanza message, length delimited. Does not implicitly {@link server.GroupsStanza.verify|verify} messages.
         * @param message GroupsStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupsStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupsStanza message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupsStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupsStanza;

        /**
         * Decodes a GroupsStanza message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupsStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupsStanza;

        /**
         * Verifies a GroupsStanza message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupsStanza message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupsStanza
         */
        public static fromObject(object: { [k: string]: any }): server.GroupsStanza;

        /**
         * Creates a plain object from a GroupsStanza message. Also converts values to other types if specified.
         * @param message GroupsStanza
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupsStanza, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupsStanza to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GroupsStanza {

        /** Action enum. */
        enum Action {
            GET = 0
        }
    }

    /** Properties of a GroupInviteLink. */
    interface IGroupInviteLink {

        /** GroupInviteLink action */
        action?: (server.GroupInviteLink.Action|null);

        /** GroupInviteLink gid */
        gid?: (string|null);

        /** GroupInviteLink link */
        link?: (string|null);

        /** GroupInviteLink result */
        result?: (string|null);

        /** GroupInviteLink reason */
        reason?: (string|null);

        /** GroupInviteLink group */
        group?: (server.IGroupStanza|null);
    }

    /** Represents a GroupInviteLink. */
    class GroupInviteLink implements IGroupInviteLink {

        /**
         * Constructs a new GroupInviteLink.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupInviteLink);

        /** GroupInviteLink action. */
        public action: server.GroupInviteLink.Action;

        /** GroupInviteLink gid. */
        public gid: string;

        /** GroupInviteLink link. */
        public link: string;

        /** GroupInviteLink result. */
        public result: string;

        /** GroupInviteLink reason. */
        public reason: string;

        /** GroupInviteLink group. */
        public group?: (server.IGroupStanza|null);

        /**
         * Creates a new GroupInviteLink instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupInviteLink instance
         */
        public static create(properties?: server.IGroupInviteLink): server.GroupInviteLink;

        /**
         * Encodes the specified GroupInviteLink message. Does not implicitly {@link server.GroupInviteLink.verify|verify} messages.
         * @param message GroupInviteLink message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupInviteLink, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupInviteLink message, length delimited. Does not implicitly {@link server.GroupInviteLink.verify|verify} messages.
         * @param message GroupInviteLink message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupInviteLink, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupInviteLink message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupInviteLink
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupInviteLink;

        /**
         * Decodes a GroupInviteLink message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupInviteLink
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupInviteLink;

        /**
         * Verifies a GroupInviteLink message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupInviteLink message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupInviteLink
         */
        public static fromObject(object: { [k: string]: any }): server.GroupInviteLink;

        /**
         * Creates a plain object from a GroupInviteLink message. Also converts values to other types if specified.
         * @param message GroupInviteLink
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupInviteLink, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupInviteLink to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GroupInviteLink {

        /** Action enum. */
        enum Action {
            UNKNOWN = 0,
            GET = 1,
            RESET = 2,
            JOIN = 3,
            PREVIEW = 4
        }
    }

    /** Properties of an AuthRequest. */
    interface IAuthRequest {

        /** AuthRequest uid */
        uid?: (number|Long|null);

        /** AuthRequest pwd */
        pwd?: (string|null);

        /** AuthRequest clientMode */
        clientMode?: (server.IClientMode|null);

        /** AuthRequest clientVersion */
        clientVersion?: (server.IClientVersion|null);

        /** AuthRequest resource */
        resource?: (string|null);
    }

    /** Represents an AuthRequest. */
    class AuthRequest implements IAuthRequest {

        /**
         * Constructs a new AuthRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAuthRequest);

        /** AuthRequest uid. */
        public uid: (number|Long);

        /** AuthRequest pwd. */
        public pwd: string;

        /** AuthRequest clientMode. */
        public clientMode?: (server.IClientMode|null);

        /** AuthRequest clientVersion. */
        public clientVersion?: (server.IClientVersion|null);

        /** AuthRequest resource. */
        public resource: string;

        /**
         * Creates a new AuthRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns AuthRequest instance
         */
        public static create(properties?: server.IAuthRequest): server.AuthRequest;

        /**
         * Encodes the specified AuthRequest message. Does not implicitly {@link server.AuthRequest.verify|verify} messages.
         * @param message AuthRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAuthRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified AuthRequest message, length delimited. Does not implicitly {@link server.AuthRequest.verify|verify} messages.
         * @param message AuthRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAuthRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an AuthRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns AuthRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.AuthRequest;

        /**
         * Decodes an AuthRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns AuthRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.AuthRequest;

        /**
         * Verifies an AuthRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an AuthRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns AuthRequest
         */
        public static fromObject(object: { [k: string]: any }): server.AuthRequest;

        /**
         * Creates a plain object from an AuthRequest message. Also converts values to other types if specified.
         * @param message AuthRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.AuthRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this AuthRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an AuthResult. */
    interface IAuthResult {

        /** AuthResult resultString */
        resultString?: (string|null);

        /** AuthResult reasonString */
        reasonString?: (string|null);

        /** AuthResult propsHash */
        propsHash?: (Uint8Array|null);

        /** AuthResult versionTtl */
        versionTtl?: (number|Long|null);

        /** AuthResult result */
        result?: (server.AuthResult.Result|null);

        /** AuthResult reason */
        reason?: (server.AuthResult.Reason|null);
    }

    /** Represents an AuthResult. */
    class AuthResult implements IAuthResult {

        /**
         * Constructs a new AuthResult.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAuthResult);

        /** AuthResult resultString. */
        public resultString: string;

        /** AuthResult reasonString. */
        public reasonString: string;

        /** AuthResult propsHash. */
        public propsHash: Uint8Array;

        /** AuthResult versionTtl. */
        public versionTtl: (number|Long);

        /** AuthResult result. */
        public result: server.AuthResult.Result;

        /** AuthResult reason. */
        public reason: server.AuthResult.Reason;

        /**
         * Creates a new AuthResult instance using the specified properties.
         * @param [properties] Properties to set
         * @returns AuthResult instance
         */
        public static create(properties?: server.IAuthResult): server.AuthResult;

        /**
         * Encodes the specified AuthResult message. Does not implicitly {@link server.AuthResult.verify|verify} messages.
         * @param message AuthResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAuthResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified AuthResult message, length delimited. Does not implicitly {@link server.AuthResult.verify|verify} messages.
         * @param message AuthResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAuthResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an AuthResult message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns AuthResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.AuthResult;

        /**
         * Decodes an AuthResult message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns AuthResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.AuthResult;

        /**
         * Verifies an AuthResult message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an AuthResult message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns AuthResult
         */
        public static fromObject(object: { [k: string]: any }): server.AuthResult;

        /**
         * Creates a plain object from an AuthResult message. Also converts values to other types if specified.
         * @param message AuthResult
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.AuthResult, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this AuthResult to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace AuthResult {

        /** Result enum. */
        enum Result {
            UNKNOWN = 0,
            SUCCESS = 1,
            FAILURE = 2
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            OK = 1,
            SPUB_MISMATCH = 2,
            INVALID_CLIENT_VERSION = 3,
            INVALID_RESOURCE = 4,
            ACCOUNT_DELETED = 5,
            INVALID_UID_OR_PASSWORD = 6
        }
    }

    /** Properties of an Invite. */
    interface IInvite {

        /** Invite phone */
        phone?: (string|null);

        /** Invite result */
        result?: (string|null);

        /** Invite reason */
        reason?: (string|null);
    }

    /** Represents an Invite. */
    class Invite implements IInvite {

        /**
         * Constructs a new Invite.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IInvite);

        /** Invite phone. */
        public phone: string;

        /** Invite result. */
        public result: string;

        /** Invite reason. */
        public reason: string;

        /**
         * Creates a new Invite instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Invite instance
         */
        public static create(properties?: server.IInvite): server.Invite;

        /**
         * Encodes the specified Invite message. Does not implicitly {@link server.Invite.verify|verify} messages.
         * @param message Invite message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IInvite, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Invite message, length delimited. Does not implicitly {@link server.Invite.verify|verify} messages.
         * @param message Invite message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IInvite, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an Invite message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Invite
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Invite;

        /**
         * Decodes an Invite message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Invite
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Invite;

        /**
         * Verifies an Invite message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an Invite message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Invite
         */
        public static fromObject(object: { [k: string]: any }): server.Invite;

        /**
         * Creates a plain object from an Invite message. Also converts values to other types if specified.
         * @param message Invite
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Invite, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Invite to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an InvitesRequest. */
    interface IInvitesRequest {

        /** InvitesRequest invites */
        invites?: (server.IInvite[]|null);
    }

    /** Represents an InvitesRequest. */
    class InvitesRequest implements IInvitesRequest {

        /**
         * Constructs a new InvitesRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IInvitesRequest);

        /** InvitesRequest invites. */
        public invites: server.IInvite[];

        /**
         * Creates a new InvitesRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns InvitesRequest instance
         */
        public static create(properties?: server.IInvitesRequest): server.InvitesRequest;

        /**
         * Encodes the specified InvitesRequest message. Does not implicitly {@link server.InvitesRequest.verify|verify} messages.
         * @param message InvitesRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IInvitesRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified InvitesRequest message, length delimited. Does not implicitly {@link server.InvitesRequest.verify|verify} messages.
         * @param message InvitesRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IInvitesRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an InvitesRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns InvitesRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.InvitesRequest;

        /**
         * Decodes an InvitesRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns InvitesRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.InvitesRequest;

        /**
         * Verifies an InvitesRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an InvitesRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns InvitesRequest
         */
        public static fromObject(object: { [k: string]: any }): server.InvitesRequest;

        /**
         * Creates a plain object from an InvitesRequest message. Also converts values to other types if specified.
         * @param message InvitesRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.InvitesRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this InvitesRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an InvitesResponse. */
    interface IInvitesResponse {

        /** InvitesResponse invitesLeft */
        invitesLeft?: (number|null);

        /** InvitesResponse timeUntilRefresh */
        timeUntilRefresh?: (number|Long|null);

        /** InvitesResponse invites */
        invites?: (server.IInvite[]|null);
    }

    /** Represents an InvitesResponse. */
    class InvitesResponse implements IInvitesResponse {

        /**
         * Constructs a new InvitesResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IInvitesResponse);

        /** InvitesResponse invitesLeft. */
        public invitesLeft: number;

        /** InvitesResponse timeUntilRefresh. */
        public timeUntilRefresh: (number|Long);

        /** InvitesResponse invites. */
        public invites: server.IInvite[];

        /**
         * Creates a new InvitesResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns InvitesResponse instance
         */
        public static create(properties?: server.IInvitesResponse): server.InvitesResponse;

        /**
         * Encodes the specified InvitesResponse message. Does not implicitly {@link server.InvitesResponse.verify|verify} messages.
         * @param message InvitesResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IInvitesResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified InvitesResponse message, length delimited. Does not implicitly {@link server.InvitesResponse.verify|verify} messages.
         * @param message InvitesResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IInvitesResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an InvitesResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns InvitesResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.InvitesResponse;

        /**
         * Decodes an InvitesResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns InvitesResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.InvitesResponse;

        /**
         * Verifies an InvitesResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an InvitesResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns InvitesResponse
         */
        public static fromObject(object: { [k: string]: any }): server.InvitesResponse;

        /**
         * Creates a plain object from an InvitesResponse message. Also converts values to other types if specified.
         * @param message InvitesResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.InvitesResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this InvitesResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a MediaUrl. */
    interface IMediaUrl {

        /** MediaUrl get */
        get?: (string|null);

        /** MediaUrl put */
        put?: (string|null);

        /** MediaUrl patch */
        patch?: (string|null);
    }

    /** Represents a MediaUrl. */
    class MediaUrl implements IMediaUrl {

        /**
         * Constructs a new MediaUrl.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMediaUrl);

        /** MediaUrl get. */
        public get: string;

        /** MediaUrl put. */
        public put: string;

        /** MediaUrl patch. */
        public patch: string;

        /**
         * Creates a new MediaUrl instance using the specified properties.
         * @param [properties] Properties to set
         * @returns MediaUrl instance
         */
        public static create(properties?: server.IMediaUrl): server.MediaUrl;

        /**
         * Encodes the specified MediaUrl message. Does not implicitly {@link server.MediaUrl.verify|verify} messages.
         * @param message MediaUrl message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMediaUrl, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified MediaUrl message, length delimited. Does not implicitly {@link server.MediaUrl.verify|verify} messages.
         * @param message MediaUrl message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMediaUrl, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a MediaUrl message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns MediaUrl
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.MediaUrl;

        /**
         * Decodes a MediaUrl message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns MediaUrl
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.MediaUrl;

        /**
         * Verifies a MediaUrl message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a MediaUrl message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns MediaUrl
         */
        public static fromObject(object: { [k: string]: any }): server.MediaUrl;

        /**
         * Creates a plain object from a MediaUrl message. Also converts values to other types if specified.
         * @param message MediaUrl
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.MediaUrl, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this MediaUrl to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an UploadMedia. */
    interface IUploadMedia {

        /** UploadMedia size */
        size?: (number|Long|null);

        /** UploadMedia url */
        url?: (server.IMediaUrl|null);

        /** UploadMedia downloadUrl */
        downloadUrl?: (string|null);

        /** UploadMedia type */
        type?: (server.UploadMedia.Type|null);
    }

    /** Represents an UploadMedia. */
    class UploadMedia implements IUploadMedia {

        /**
         * Constructs a new UploadMedia.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IUploadMedia);

        /** UploadMedia size. */
        public size: (number|Long);

        /** UploadMedia url. */
        public url?: (server.IMediaUrl|null);

        /** UploadMedia downloadUrl. */
        public downloadUrl: string;

        /** UploadMedia type. */
        public type: server.UploadMedia.Type;

        /**
         * Creates a new UploadMedia instance using the specified properties.
         * @param [properties] Properties to set
         * @returns UploadMedia instance
         */
        public static create(properties?: server.IUploadMedia): server.UploadMedia;

        /**
         * Encodes the specified UploadMedia message. Does not implicitly {@link server.UploadMedia.verify|verify} messages.
         * @param message UploadMedia message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IUploadMedia, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified UploadMedia message, length delimited. Does not implicitly {@link server.UploadMedia.verify|verify} messages.
         * @param message UploadMedia message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IUploadMedia, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an UploadMedia message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns UploadMedia
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.UploadMedia;

        /**
         * Decodes an UploadMedia message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns UploadMedia
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.UploadMedia;

        /**
         * Verifies an UploadMedia message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an UploadMedia message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns UploadMedia
         */
        public static fromObject(object: { [k: string]: any }): server.UploadMedia;

        /**
         * Creates a plain object from an UploadMedia message. Also converts values to other types if specified.
         * @param message UploadMedia
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.UploadMedia, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this UploadMedia to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace UploadMedia {

        /** Type enum. */
        enum Type {
            DEFAULT = 0,
            RESUMABLE = 1,
            DIRECT = 2
        }
    }

    /** Properties of a ChatStanza. */
    interface IChatStanza {

        /** ChatStanza timestamp */
        timestamp?: (number|Long|null);

        /** ChatStanza payload */
        payload?: (Uint8Array|null);

        /** ChatStanza encPayload */
        encPayload?: (Uint8Array|null);

        /** ChatStanza publicKey */
        publicKey?: (Uint8Array|null);

        /** ChatStanza oneTimePreKeyId */
        oneTimePreKeyId?: (number|Long|null);

        /** ChatStanza senderName */
        senderName?: (string|null);

        /** ChatStanza senderPhone */
        senderPhone?: (string|null);

        /** ChatStanza senderLogInfo */
        senderLogInfo?: (string|null);

        /** ChatStanza senderClientVersion */
        senderClientVersion?: (string|null);
    }

    /** Represents a ChatStanza. */
    class ChatStanza implements IChatStanza {

        /**
         * Constructs a new ChatStanza.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IChatStanza);

        /** ChatStanza timestamp. */
        public timestamp: (number|Long);

        /** ChatStanza payload. */
        public payload: Uint8Array;

        /** ChatStanza encPayload. */
        public encPayload: Uint8Array;

        /** ChatStanza publicKey. */
        public publicKey: Uint8Array;

        /** ChatStanza oneTimePreKeyId. */
        public oneTimePreKeyId: (number|Long);

        /** ChatStanza senderName. */
        public senderName: string;

        /** ChatStanza senderPhone. */
        public senderPhone: string;

        /** ChatStanza senderLogInfo. */
        public senderLogInfo: string;

        /** ChatStanza senderClientVersion. */
        public senderClientVersion: string;

        /**
         * Creates a new ChatStanza instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ChatStanza instance
         */
        public static create(properties?: server.IChatStanza): server.ChatStanza;

        /**
         * Encodes the specified ChatStanza message. Does not implicitly {@link server.ChatStanza.verify|verify} messages.
         * @param message ChatStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IChatStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ChatStanza message, length delimited. Does not implicitly {@link server.ChatStanza.verify|verify} messages.
         * @param message ChatStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IChatStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ChatStanza message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ChatStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ChatStanza;

        /**
         * Decodes a ChatStanza message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ChatStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ChatStanza;

        /**
         * Verifies a ChatStanza message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ChatStanza message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ChatStanza
         */
        public static fromObject(object: { [k: string]: any }): server.ChatStanza;

        /**
         * Creates a plain object from a ChatStanza message. Also converts values to other types if specified.
         * @param message ChatStanza
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ChatStanza, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ChatStanza to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a SilentChatStanza. */
    interface ISilentChatStanza {

        /** SilentChatStanza chatStanza */
        chatStanza?: (server.IChatStanza|null);
    }

    /** Represents a SilentChatStanza. */
    class SilentChatStanza implements ISilentChatStanza {

        /**
         * Constructs a new SilentChatStanza.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISilentChatStanza);

        /** SilentChatStanza chatStanza. */
        public chatStanza?: (server.IChatStanza|null);

        /**
         * Creates a new SilentChatStanza instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SilentChatStanza instance
         */
        public static create(properties?: server.ISilentChatStanza): server.SilentChatStanza;

        /**
         * Encodes the specified SilentChatStanza message. Does not implicitly {@link server.SilentChatStanza.verify|verify} messages.
         * @param message SilentChatStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISilentChatStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SilentChatStanza message, length delimited. Does not implicitly {@link server.SilentChatStanza.verify|verify} messages.
         * @param message SilentChatStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISilentChatStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SilentChatStanza message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SilentChatStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SilentChatStanza;

        /**
         * Decodes a SilentChatStanza message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SilentChatStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SilentChatStanza;

        /**
         * Verifies a SilentChatStanza message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SilentChatStanza message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SilentChatStanza
         */
        public static fromObject(object: { [k: string]: any }): server.SilentChatStanza;

        /**
         * Creates a plain object from a SilentChatStanza message. Also converts values to other types if specified.
         * @param message SilentChatStanza
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SilentChatStanza, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SilentChatStanza to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a Ping. */
    interface IPing {
    }

    /** Represents a Ping. */
    class Ping implements IPing {

        /**
         * Constructs a new Ping.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPing);

        /**
         * Creates a new Ping instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Ping instance
         */
        public static create(properties?: server.IPing): server.Ping;

        /**
         * Encodes the specified Ping message. Does not implicitly {@link server.Ping.verify|verify} messages.
         * @param message Ping message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPing, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Ping message, length delimited. Does not implicitly {@link server.Ping.verify|verify} messages.
         * @param message Ping message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPing, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Ping message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Ping
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Ping;

        /**
         * Decodes a Ping message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Ping
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Ping;

        /**
         * Verifies a Ping message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Ping message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Ping
         */
        public static fromObject(object: { [k: string]: any }): server.Ping;

        /**
         * Creates a plain object from a Ping message. Also converts values to other types if specified.
         * @param message Ping
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Ping, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Ping to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an ErrorStanza. */
    interface IErrorStanza {

        /** ErrorStanza reason */
        reason?: (string|null);
    }

    /** Represents an ErrorStanza. */
    class ErrorStanza implements IErrorStanza {

        /**
         * Constructs a new ErrorStanza.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IErrorStanza);

        /** ErrorStanza reason. */
        public reason: string;

        /**
         * Creates a new ErrorStanza instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ErrorStanza instance
         */
        public static create(properties?: server.IErrorStanza): server.ErrorStanza;

        /**
         * Encodes the specified ErrorStanza message. Does not implicitly {@link server.ErrorStanza.verify|verify} messages.
         * @param message ErrorStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IErrorStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ErrorStanza message, length delimited. Does not implicitly {@link server.ErrorStanza.verify|verify} messages.
         * @param message ErrorStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IErrorStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an ErrorStanza message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ErrorStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ErrorStanza;

        /**
         * Decodes an ErrorStanza message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ErrorStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ErrorStanza;

        /**
         * Verifies an ErrorStanza message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an ErrorStanza message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ErrorStanza
         */
        public static fromObject(object: { [k: string]: any }): server.ErrorStanza;

        /**
         * Creates a plain object from an ErrorStanza message. Also converts values to other types if specified.
         * @param message ErrorStanza
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ErrorStanza, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ErrorStanza to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a Name. */
    interface IName {

        /** Name uid */
        uid?: (number|Long|null);

        /** Name name */
        name?: (string|null);
    }

    /** Represents a Name. */
    class Name implements IName {

        /**
         * Constructs a new Name.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IName);

        /** Name uid. */
        public uid: (number|Long);

        /** Name name. */
        public name: string;

        /**
         * Creates a new Name instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Name instance
         */
        public static create(properties?: server.IName): server.Name;

        /**
         * Encodes the specified Name message. Does not implicitly {@link server.Name.verify|verify} messages.
         * @param message Name message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IName, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Name message, length delimited. Does not implicitly {@link server.Name.verify|verify} messages.
         * @param message Name message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IName, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Name message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Name
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Name;

        /**
         * Decodes a Name message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Name
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Name;

        /**
         * Verifies a Name message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Name message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Name
         */
        public static fromObject(object: { [k: string]: any }): server.Name;

        /**
         * Creates a plain object from a Name message. Also converts values to other types if specified.
         * @param message Name
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Name, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Name to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an EndOfQueue. */
    interface IEndOfQueue {
    }

    /** Represents an EndOfQueue. */
    class EndOfQueue implements IEndOfQueue {

        /**
         * Constructs a new EndOfQueue.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IEndOfQueue);

        /**
         * Creates a new EndOfQueue instance using the specified properties.
         * @param [properties] Properties to set
         * @returns EndOfQueue instance
         */
        public static create(properties?: server.IEndOfQueue): server.EndOfQueue;

        /**
         * Encodes the specified EndOfQueue message. Does not implicitly {@link server.EndOfQueue.verify|verify} messages.
         * @param message EndOfQueue message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IEndOfQueue, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified EndOfQueue message, length delimited. Does not implicitly {@link server.EndOfQueue.verify|verify} messages.
         * @param message EndOfQueue message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IEndOfQueue, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an EndOfQueue message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns EndOfQueue
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.EndOfQueue;

        /**
         * Decodes an EndOfQueue message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns EndOfQueue
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.EndOfQueue;

        /**
         * Verifies an EndOfQueue message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an EndOfQueue message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns EndOfQueue
         */
        public static fromObject(object: { [k: string]: any }): server.EndOfQueue;

        /**
         * Creates a plain object from an EndOfQueue message. Also converts values to other types if specified.
         * @param message EndOfQueue
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.EndOfQueue, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this EndOfQueue to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an Iq. */
    interface IIq {

        /** Iq id */
        id?: (string|null);

        /** Iq type */
        type?: (server.Iq.Type|null);

        /** Iq uploadMedia */
        uploadMedia?: (server.IUploadMedia|null);

        /** Iq contactList */
        contactList?: (server.IContactList|null);

        /** Iq uploadAvatar */
        uploadAvatar?: (server.IUploadAvatar|null);

        /** Iq avatar */
        avatar?: (server.IAvatar|null);

        /** Iq avatars */
        avatars?: (server.IAvatars|null);

        /** Iq clientMode */
        clientMode?: (server.IClientMode|null);

        /** Iq clientVersion */
        clientVersion?: (server.IClientVersion|null);

        /** Iq pushRegister */
        pushRegister?: (server.IPushRegister|null);

        /** Iq whisperKeys */
        whisperKeys?: (server.IWhisperKeys|null);

        /** Iq ping */
        ping?: (server.IPing|null);

        /** Iq feedItem */
        feedItem?: (server.IFeedItem|null);

        /** Iq privacyList */
        privacyList?: (server.IPrivacyList|null);

        /** Iq privacyLists */
        privacyLists?: (server.IPrivacyLists|null);

        /** Iq groupStanza */
        groupStanza?: (server.IGroupStanza|null);

        /** Iq groupsStanza */
        groupsStanza?: (server.IGroupsStanza|null);

        /** Iq clientLog */
        clientLog?: (server.IClientLog|null);

        /** Iq name */
        name?: (server.IName|null);

        /** Iq errorStanza */
        errorStanza?: (server.IErrorStanza|null);

        /** Iq props */
        props?: (server.IProps|null);

        /** Iq invitesRequest */
        invitesRequest?: (server.IInvitesRequest|null);

        /** Iq invitesResponse */
        invitesResponse?: (server.IInvitesResponse|null);

        /** Iq notificationPrefs */
        notificationPrefs?: (server.INotificationPrefs|null);

        /** Iq groupFeedItem */
        groupFeedItem?: (server.IGroupFeedItem|null);

        /** Iq groupAvatar */
        groupAvatar?: (server.IUploadGroupAvatar|null);

        /** Iq deleteAccount */
        deleteAccount?: (server.IDeleteAccount|null);

        /** Iq groupInviteLink */
        groupInviteLink?: (server.IGroupInviteLink|null);
    }

    /** Represents an Iq. */
    class Iq implements IIq {

        /**
         * Constructs a new Iq.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IIq);

        /** Iq id. */
        public id: string;

        /** Iq type. */
        public type: server.Iq.Type;

        /** Iq uploadMedia. */
        public uploadMedia?: (server.IUploadMedia|null);

        /** Iq contactList. */
        public contactList?: (server.IContactList|null);

        /** Iq uploadAvatar. */
        public uploadAvatar?: (server.IUploadAvatar|null);

        /** Iq avatar. */
        public avatar?: (server.IAvatar|null);

        /** Iq avatars. */
        public avatars?: (server.IAvatars|null);

        /** Iq clientMode. */
        public clientMode?: (server.IClientMode|null);

        /** Iq clientVersion. */
        public clientVersion?: (server.IClientVersion|null);

        /** Iq pushRegister. */
        public pushRegister?: (server.IPushRegister|null);

        /** Iq whisperKeys. */
        public whisperKeys?: (server.IWhisperKeys|null);

        /** Iq ping. */
        public ping?: (server.IPing|null);

        /** Iq feedItem. */
        public feedItem?: (server.IFeedItem|null);

        /** Iq privacyList. */
        public privacyList?: (server.IPrivacyList|null);

        /** Iq privacyLists. */
        public privacyLists?: (server.IPrivacyLists|null);

        /** Iq groupStanza. */
        public groupStanza?: (server.IGroupStanza|null);

        /** Iq groupsStanza. */
        public groupsStanza?: (server.IGroupsStanza|null);

        /** Iq clientLog. */
        public clientLog?: (server.IClientLog|null);

        /** Iq name. */
        public name?: (server.IName|null);

        /** Iq errorStanza. */
        public errorStanza?: (server.IErrorStanza|null);

        /** Iq props. */
        public props?: (server.IProps|null);

        /** Iq invitesRequest. */
        public invitesRequest?: (server.IInvitesRequest|null);

        /** Iq invitesResponse. */
        public invitesResponse?: (server.IInvitesResponse|null);

        /** Iq notificationPrefs. */
        public notificationPrefs?: (server.INotificationPrefs|null);

        /** Iq groupFeedItem. */
        public groupFeedItem?: (server.IGroupFeedItem|null);

        /** Iq groupAvatar. */
        public groupAvatar?: (server.IUploadGroupAvatar|null);

        /** Iq deleteAccount. */
        public deleteAccount?: (server.IDeleteAccount|null);

        /** Iq groupInviteLink. */
        public groupInviteLink?: (server.IGroupInviteLink|null);

        /** Iq payload. */
        public payload?: ("uploadMedia"|"contactList"|"uploadAvatar"|"avatar"|"avatars"|"clientMode"|"clientVersion"|"pushRegister"|"whisperKeys"|"ping"|"feedItem"|"privacyList"|"privacyLists"|"groupStanza"|"groupsStanza"|"clientLog"|"name"|"errorStanza"|"props"|"invitesRequest"|"invitesResponse"|"notificationPrefs"|"groupFeedItem"|"groupAvatar"|"deleteAccount"|"groupInviteLink");

        /**
         * Creates a new Iq instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Iq instance
         */
        public static create(properties?: server.IIq): server.Iq;

        /**
         * Encodes the specified Iq message. Does not implicitly {@link server.Iq.verify|verify} messages.
         * @param message Iq message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IIq, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Iq message, length delimited. Does not implicitly {@link server.Iq.verify|verify} messages.
         * @param message Iq message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IIq, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an Iq message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Iq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Iq;

        /**
         * Decodes an Iq message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Iq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Iq;

        /**
         * Verifies an Iq message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an Iq message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Iq
         */
        public static fromObject(object: { [k: string]: any }): server.Iq;

        /**
         * Creates a plain object from an Iq message. Also converts values to other types if specified.
         * @param message Iq
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Iq, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Iq to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace Iq {

        /** Type enum. */
        enum Type {
            GET = 0,
            SET = 1,
            RESULT = 2,
            ERROR = 3
        }
    }

    /** Properties of a Msg. */
    interface IMsg {

        /** Msg id */
        id?: (string|null);

        /** Msg type */
        type?: (server.Msg.Type|null);

        /** Msg toUid */
        toUid?: (number|Long|null);

        /** Msg fromUid */
        fromUid?: (number|Long|null);

        /** Msg contactList */
        contactList?: (server.IContactList|null);

        /** Msg avatar */
        avatar?: (server.IAvatar|null);

        /** Msg whisperKeys */
        whisperKeys?: (server.IWhisperKeys|null);

        /** Msg seenReceipt */
        seenReceipt?: (server.ISeenReceipt|null);

        /** Msg deliveryReceipt */
        deliveryReceipt?: (server.IDeliveryReceipt|null);

        /** Msg chatStanza */
        chatStanza?: (server.IChatStanza|null);

        /** Msg feedItem */
        feedItem?: (server.IFeedItem|null);

        /** Msg feedItems */
        feedItems?: (server.IFeedItems|null);

        /** Msg contactHash */
        contactHash?: (server.IContactHash|null);

        /** Msg groupStanza */
        groupStanza?: (server.IGroupStanza|null);

        /** Msg groupChat */
        groupChat?: (server.IGroupChat|null);

        /** Msg name */
        name?: (server.IName|null);

        /** Msg errorStanza */
        errorStanza?: (server.IErrorStanza|null);

        /** Msg groupchatRetract */
        groupchatRetract?: (server.IGroupChatRetract|null);

        /** Msg chatRetract */
        chatRetract?: (server.IChatRetract|null);

        /** Msg groupFeedItem */
        groupFeedItem?: (server.IGroupFeedItem|null);

        /** Msg rerequest */
        rerequest?: (server.IRerequest|null);

        /** Msg silentChatStanza */
        silentChatStanza?: (server.ISilentChatStanza|null);

        /** Msg groupFeedItems */
        groupFeedItems?: (server.IGroupFeedItems|null);

        /** Msg endOfQueue */
        endOfQueue?: (server.IEndOfQueue|null);

        /** Msg inviteeNotice */
        inviteeNotice?: (server.IInviteeNotice|null);

        /** Msg groupFeedRerequest */
        groupFeedRerequest?: (server.IGroupFeedRerequest|null);

        /** Msg retryCount */
        retryCount?: (number|null);

        /** Msg rerequestCount */
        rerequestCount?: (number|null);
    }

    /** Represents a Msg. */
    class Msg implements IMsg {

        /**
         * Constructs a new Msg.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMsg);

        /** Msg id. */
        public id: string;

        /** Msg type. */
        public type: server.Msg.Type;

        /** Msg toUid. */
        public toUid: (number|Long);

        /** Msg fromUid. */
        public fromUid: (number|Long);

        /** Msg contactList. */
        public contactList?: (server.IContactList|null);

        /** Msg avatar. */
        public avatar?: (server.IAvatar|null);

        /** Msg whisperKeys. */
        public whisperKeys?: (server.IWhisperKeys|null);

        /** Msg seenReceipt. */
        public seenReceipt?: (server.ISeenReceipt|null);

        /** Msg deliveryReceipt. */
        public deliveryReceipt?: (server.IDeliveryReceipt|null);

        /** Msg chatStanza. */
        public chatStanza?: (server.IChatStanza|null);

        /** Msg feedItem. */
        public feedItem?: (server.IFeedItem|null);

        /** Msg feedItems. */
        public feedItems?: (server.IFeedItems|null);

        /** Msg contactHash. */
        public contactHash?: (server.IContactHash|null);

        /** Msg groupStanza. */
        public groupStanza?: (server.IGroupStanza|null);

        /** Msg groupChat. */
        public groupChat?: (server.IGroupChat|null);

        /** Msg name. */
        public name?: (server.IName|null);

        /** Msg errorStanza. */
        public errorStanza?: (server.IErrorStanza|null);

        /** Msg groupchatRetract. */
        public groupchatRetract?: (server.IGroupChatRetract|null);

        /** Msg chatRetract. */
        public chatRetract?: (server.IChatRetract|null);

        /** Msg groupFeedItem. */
        public groupFeedItem?: (server.IGroupFeedItem|null);

        /** Msg rerequest. */
        public rerequest?: (server.IRerequest|null);

        /** Msg silentChatStanza. */
        public silentChatStanza?: (server.ISilentChatStanza|null);

        /** Msg groupFeedItems. */
        public groupFeedItems?: (server.IGroupFeedItems|null);

        /** Msg endOfQueue. */
        public endOfQueue?: (server.IEndOfQueue|null);

        /** Msg inviteeNotice. */
        public inviteeNotice?: (server.IInviteeNotice|null);

        /** Msg groupFeedRerequest. */
        public groupFeedRerequest?: (server.IGroupFeedRerequest|null);

        /** Msg retryCount. */
        public retryCount: number;

        /** Msg rerequestCount. */
        public rerequestCount: number;

        /** Msg payload. */
        public payload?: ("contactList"|"avatar"|"whisperKeys"|"seenReceipt"|"deliveryReceipt"|"chatStanza"|"feedItem"|"feedItems"|"contactHash"|"groupStanza"|"groupChat"|"name"|"errorStanza"|"groupchatRetract"|"chatRetract"|"groupFeedItem"|"rerequest"|"silentChatStanza"|"groupFeedItems"|"endOfQueue"|"inviteeNotice"|"groupFeedRerequest");

        /**
         * Creates a new Msg instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Msg instance
         */
        public static create(properties?: server.IMsg): server.Msg;

        /**
         * Encodes the specified Msg message. Does not implicitly {@link server.Msg.verify|verify} messages.
         * @param message Msg message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMsg, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Msg message, length delimited. Does not implicitly {@link server.Msg.verify|verify} messages.
         * @param message Msg message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMsg, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Msg message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Msg
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Msg;

        /**
         * Decodes a Msg message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Msg
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Msg;

        /**
         * Verifies a Msg message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Msg message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Msg
         */
        public static fromObject(object: { [k: string]: any }): server.Msg;

        /**
         * Creates a plain object from a Msg message. Also converts values to other types if specified.
         * @param message Msg
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Msg, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Msg to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace Msg {

        /** Type enum. */
        enum Type {
            NORMAL = 0,
            ERROR = 1,
            GROUPCHAT = 2,
            HEADLINE = 3,
            CHAT = 4
        }
    }

    /** Properties of a Presence. */
    interface IPresence {

        /** Presence id */
        id?: (string|null);

        /** Presence type */
        type?: (server.Presence.Type|null);

        /** Presence uid */
        uid?: (number|Long|null);

        /** Presence lastSeen */
        lastSeen?: (number|Long|null);

        /** Presence toUid */
        toUid?: (number|Long|null);

        /** Presence fromUid */
        fromUid?: (number|Long|null);
    }

    /** Represents a Presence. */
    class Presence implements IPresence {

        /**
         * Constructs a new Presence.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPresence);

        /** Presence id. */
        public id: string;

        /** Presence type. */
        public type: server.Presence.Type;

        /** Presence uid. */
        public uid: (number|Long);

        /** Presence lastSeen. */
        public lastSeen: (number|Long);

        /** Presence toUid. */
        public toUid: (number|Long);

        /** Presence fromUid. */
        public fromUid: (number|Long);

        /**
         * Creates a new Presence instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Presence instance
         */
        public static create(properties?: server.IPresence): server.Presence;

        /**
         * Encodes the specified Presence message. Does not implicitly {@link server.Presence.verify|verify} messages.
         * @param message Presence message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPresence, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Presence message, length delimited. Does not implicitly {@link server.Presence.verify|verify} messages.
         * @param message Presence message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPresence, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Presence message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Presence
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Presence;

        /**
         * Decodes a Presence message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Presence
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Presence;

        /**
         * Verifies a Presence message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Presence message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Presence
         */
        public static fromObject(object: { [k: string]: any }): server.Presence;

        /**
         * Creates a plain object from a Presence message. Also converts values to other types if specified.
         * @param message Presence
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Presence, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Presence to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace Presence {

        /** Type enum. */
        enum Type {
            AVAILABLE = 0,
            AWAY = 1,
            SUBSCRIBE = 2,
            UNSUBSCRIBE = 3
        }
    }

    /** Properties of a ChatState. */
    interface IChatState {

        /** ChatState type */
        type?: (server.ChatState.Type|null);

        /** ChatState threadId */
        threadId?: (string|null);

        /** ChatState threadType */
        threadType?: (server.ChatState.ThreadType|null);

        /** ChatState fromUid */
        fromUid?: (number|Long|null);
    }

    /** Represents a ChatState. */
    class ChatState implements IChatState {

        /**
         * Constructs a new ChatState.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IChatState);

        /** ChatState type. */
        public type: server.ChatState.Type;

        /** ChatState threadId. */
        public threadId: string;

        /** ChatState threadType. */
        public threadType: server.ChatState.ThreadType;

        /** ChatState fromUid. */
        public fromUid: (number|Long);

        /**
         * Creates a new ChatState instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ChatState instance
         */
        public static create(properties?: server.IChatState): server.ChatState;

        /**
         * Encodes the specified ChatState message. Does not implicitly {@link server.ChatState.verify|verify} messages.
         * @param message ChatState message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IChatState, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ChatState message, length delimited. Does not implicitly {@link server.ChatState.verify|verify} messages.
         * @param message ChatState message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IChatState, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ChatState message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ChatState
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ChatState;

        /**
         * Decodes a ChatState message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ChatState
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ChatState;

        /**
         * Verifies a ChatState message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ChatState message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ChatState
         */
        public static fromObject(object: { [k: string]: any }): server.ChatState;

        /**
         * Creates a plain object from a ChatState message. Also converts values to other types if specified.
         * @param message ChatState
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ChatState, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ChatState to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ChatState {

        /** Type enum. */
        enum Type {
            AVAILABLE = 0,
            TYPING = 1
        }

        /** ThreadType enum. */
        enum ThreadType {
            CHAT = 0,
            GROUP_CHAT = 1
        }
    }

    /** Properties of an Ack. */
    interface IAck {

        /** Ack id */
        id?: (string|null);

        /** Ack timestamp */
        timestamp?: (number|Long|null);
    }

    /** Represents an Ack. */
    class Ack implements IAck {

        /**
         * Constructs a new Ack.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAck);

        /** Ack id. */
        public id: string;

        /** Ack timestamp. */
        public timestamp: (number|Long);

        /**
         * Creates a new Ack instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Ack instance
         */
        public static create(properties?: server.IAck): server.Ack;

        /**
         * Encodes the specified Ack message. Does not implicitly {@link server.Ack.verify|verify} messages.
         * @param message Ack message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAck, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Ack message, length delimited. Does not implicitly {@link server.Ack.verify|verify} messages.
         * @param message Ack message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAck, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an Ack message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Ack
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Ack;

        /**
         * Decodes an Ack message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Ack
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Ack;

        /**
         * Verifies an Ack message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an Ack message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Ack
         */
        public static fromObject(object: { [k: string]: any }): server.Ack;

        /**
         * Creates a plain object from an Ack message. Also converts values to other types if specified.
         * @param message Ack
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Ack, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Ack to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a HaError. */
    interface IHaError {

        /** HaError reason */
        reason?: (string|null);
    }

    /** Represents a HaError. */
    class HaError implements IHaError {

        /**
         * Constructs a new HaError.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IHaError);

        /** HaError reason. */
        public reason: string;

        /**
         * Creates a new HaError instance using the specified properties.
         * @param [properties] Properties to set
         * @returns HaError instance
         */
        public static create(properties?: server.IHaError): server.HaError;

        /**
         * Encodes the specified HaError message. Does not implicitly {@link server.HaError.verify|verify} messages.
         * @param message HaError message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IHaError, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified HaError message, length delimited. Does not implicitly {@link server.HaError.verify|verify} messages.
         * @param message HaError message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IHaError, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a HaError message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns HaError
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.HaError;

        /**
         * Decodes a HaError message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns HaError
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.HaError;

        /**
         * Verifies a HaError message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a HaError message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns HaError
         */
        public static fromObject(object: { [k: string]: any }): server.HaError;

        /**
         * Creates a plain object from a HaError message. Also converts values to other types if specified.
         * @param message HaError
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.HaError, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this HaError to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a Packet. */
    interface IPacket {

        /** Packet msg */
        msg?: (server.IMsg|null);

        /** Packet iq */
        iq?: (server.IIq|null);

        /** Packet ack */
        ack?: (server.IAck|null);

        /** Packet presence */
        presence?: (server.IPresence|null);

        /** Packet haError */
        haError?: (server.IHaError|null);

        /** Packet chatState */
        chatState?: (server.IChatState|null);
    }

    /** Represents a Packet. */
    class Packet implements IPacket {

        /**
         * Constructs a new Packet.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPacket);

        /** Packet msg. */
        public msg?: (server.IMsg|null);

        /** Packet iq. */
        public iq?: (server.IIq|null);

        /** Packet ack. */
        public ack?: (server.IAck|null);

        /** Packet presence. */
        public presence?: (server.IPresence|null);

        /** Packet haError. */
        public haError?: (server.IHaError|null);

        /** Packet chatState. */
        public chatState?: (server.IChatState|null);

        /** Packet stanza. */
        public stanza?: ("msg"|"iq"|"ack"|"presence"|"haError"|"chatState");

        /**
         * Creates a new Packet instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Packet instance
         */
        public static create(properties?: server.IPacket): server.Packet;

        /**
         * Encodes the specified Packet message. Does not implicitly {@link server.Packet.verify|verify} messages.
         * @param message Packet message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPacket, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Packet message, length delimited. Does not implicitly {@link server.Packet.verify|verify} messages.
         * @param message Packet message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPacket, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Packet message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Packet
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Packet;

        /**
         * Decodes a Packet message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Packet
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Packet;

        /**
         * Verifies a Packet message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Packet message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Packet
         */
        public static fromObject(object: { [k: string]: any }): server.Packet;

        /**
         * Creates a plain object from a Packet message. Also converts values to other types if specified.
         * @param message Packet
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Packet, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Packet to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an UidElement. */
    interface IUidElement {

        /** UidElement action */
        action?: (server.UidElement.Action|null);

        /** UidElement uid */
        uid?: (number|Long|null);
    }

    /** Represents an UidElement. */
    class UidElement implements IUidElement {

        /**
         * Constructs a new UidElement.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IUidElement);

        /** UidElement action. */
        public action: server.UidElement.Action;

        /** UidElement uid. */
        public uid: (number|Long);

        /**
         * Creates a new UidElement instance using the specified properties.
         * @param [properties] Properties to set
         * @returns UidElement instance
         */
        public static create(properties?: server.IUidElement): server.UidElement;

        /**
         * Encodes the specified UidElement message. Does not implicitly {@link server.UidElement.verify|verify} messages.
         * @param message UidElement message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IUidElement, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified UidElement message, length delimited. Does not implicitly {@link server.UidElement.verify|verify} messages.
         * @param message UidElement message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IUidElement, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an UidElement message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns UidElement
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.UidElement;

        /**
         * Decodes an UidElement message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns UidElement
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.UidElement;

        /**
         * Verifies an UidElement message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an UidElement message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns UidElement
         */
        public static fromObject(object: { [k: string]: any }): server.UidElement;

        /**
         * Creates a plain object from an UidElement message. Also converts values to other types if specified.
         * @param message UidElement
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.UidElement, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this UidElement to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace UidElement {

        /** Action enum. */
        enum Action {
            ADD = 0,
            DELETE = 1
        }
    }

    /** Properties of a PrivacyList. */
    interface IPrivacyList {

        /** PrivacyList type */
        type?: (server.PrivacyList.Type|null);

        /** PrivacyList uidElements */
        uidElements?: (server.IUidElement[]|null);

        /** PrivacyList hash */
        hash?: (Uint8Array|null);
    }

    /** Represents a PrivacyList. */
    class PrivacyList implements IPrivacyList {

        /**
         * Constructs a new PrivacyList.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPrivacyList);

        /** PrivacyList type. */
        public type: server.PrivacyList.Type;

        /** PrivacyList uidElements. */
        public uidElements: server.IUidElement[];

        /** PrivacyList hash. */
        public hash: Uint8Array;

        /**
         * Creates a new PrivacyList instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PrivacyList instance
         */
        public static create(properties?: server.IPrivacyList): server.PrivacyList;

        /**
         * Encodes the specified PrivacyList message. Does not implicitly {@link server.PrivacyList.verify|verify} messages.
         * @param message PrivacyList message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPrivacyList, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PrivacyList message, length delimited. Does not implicitly {@link server.PrivacyList.verify|verify} messages.
         * @param message PrivacyList message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPrivacyList, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PrivacyList message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PrivacyList
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PrivacyList;

        /**
         * Decodes a PrivacyList message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PrivacyList
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PrivacyList;

        /**
         * Verifies a PrivacyList message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PrivacyList message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PrivacyList
         */
        public static fromObject(object: { [k: string]: any }): server.PrivacyList;

        /**
         * Creates a plain object from a PrivacyList message. Also converts values to other types if specified.
         * @param message PrivacyList
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PrivacyList, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PrivacyList to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace PrivacyList {

        /** Type enum. */
        enum Type {
            ALL = 0,
            BLOCK = 1,
            EXCEPT = 2,
            MUTE = 3,
            ONLY = 4
        }
    }

    /** Properties of a PrivacyLists. */
    interface IPrivacyLists {

        /** PrivacyLists activeType */
        activeType?: (server.PrivacyLists.Type|null);

        /** PrivacyLists lists */
        lists?: (server.IPrivacyList[]|null);
    }

    /** Represents a PrivacyLists. */
    class PrivacyLists implements IPrivacyLists {

        /**
         * Constructs a new PrivacyLists.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPrivacyLists);

        /** PrivacyLists activeType. */
        public activeType: server.PrivacyLists.Type;

        /** PrivacyLists lists. */
        public lists: server.IPrivacyList[];

        /**
         * Creates a new PrivacyLists instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PrivacyLists instance
         */
        public static create(properties?: server.IPrivacyLists): server.PrivacyLists;

        /**
         * Encodes the specified PrivacyLists message. Does not implicitly {@link server.PrivacyLists.verify|verify} messages.
         * @param message PrivacyLists message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPrivacyLists, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PrivacyLists message, length delimited. Does not implicitly {@link server.PrivacyLists.verify|verify} messages.
         * @param message PrivacyLists message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPrivacyLists, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PrivacyLists message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PrivacyLists
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PrivacyLists;

        /**
         * Decodes a PrivacyLists message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PrivacyLists
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PrivacyLists;

        /**
         * Verifies a PrivacyLists message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PrivacyLists message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PrivacyLists
         */
        public static fromObject(object: { [k: string]: any }): server.PrivacyLists;

        /**
         * Creates a plain object from a PrivacyLists message. Also converts values to other types if specified.
         * @param message PrivacyLists
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PrivacyLists, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PrivacyLists to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace PrivacyLists {

        /** Type enum. */
        enum Type {
            ALL = 0,
            BLOCK = 1,
            EXCEPT = 2,
            ONLY = 3
        }
    }

    /** Properties of a PushToken. */
    interface IPushToken {

        /** PushToken os */
        os?: (server.PushToken.Os|null);

        /** PushToken token */
        token?: (string|null);
    }

    /** Represents a PushToken. */
    class PushToken implements IPushToken {

        /**
         * Constructs a new PushToken.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPushToken);

        /** PushToken os. */
        public os: server.PushToken.Os;

        /** PushToken token. */
        public token: string;

        /**
         * Creates a new PushToken instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PushToken instance
         */
        public static create(properties?: server.IPushToken): server.PushToken;

        /**
         * Encodes the specified PushToken message. Does not implicitly {@link server.PushToken.verify|verify} messages.
         * @param message PushToken message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPushToken, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PushToken message, length delimited. Does not implicitly {@link server.PushToken.verify|verify} messages.
         * @param message PushToken message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPushToken, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PushToken message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PushToken
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PushToken;

        /**
         * Decodes a PushToken message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PushToken
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PushToken;

        /**
         * Verifies a PushToken message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PushToken message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PushToken
         */
        public static fromObject(object: { [k: string]: any }): server.PushToken;

        /**
         * Creates a plain object from a PushToken message. Also converts values to other types if specified.
         * @param message PushToken
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PushToken, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PushToken to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace PushToken {

        /** Os enum. */
        enum Os {
            ANDROID = 0,
            IOS = 1,
            IOS_DEV = 2
        }
    }

    /** Properties of a PushRegister. */
    interface IPushRegister {

        /** PushRegister pushToken */
        pushToken?: (server.IPushToken|null);

        /** PushRegister langId */
        langId?: (string|null);
    }

    /** Represents a PushRegister. */
    class PushRegister implements IPushRegister {

        /**
         * Constructs a new PushRegister.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPushRegister);

        /** PushRegister pushToken. */
        public pushToken?: (server.IPushToken|null);

        /** PushRegister langId. */
        public langId: string;

        /**
         * Creates a new PushRegister instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PushRegister instance
         */
        public static create(properties?: server.IPushRegister): server.PushRegister;

        /**
         * Encodes the specified PushRegister message. Does not implicitly {@link server.PushRegister.verify|verify} messages.
         * @param message PushRegister message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPushRegister, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PushRegister message, length delimited. Does not implicitly {@link server.PushRegister.verify|verify} messages.
         * @param message PushRegister message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPushRegister, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PushRegister message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PushRegister
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PushRegister;

        /**
         * Decodes a PushRegister message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PushRegister
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PushRegister;

        /**
         * Verifies a PushRegister message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PushRegister message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PushRegister
         */
        public static fromObject(object: { [k: string]: any }): server.PushRegister;

        /**
         * Creates a plain object from a PushRegister message. Also converts values to other types if specified.
         * @param message PushRegister
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PushRegister, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PushRegister to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a PushPref. */
    interface IPushPref {

        /** PushPref name */
        name?: (server.PushPref.Name|null);

        /** PushPref value */
        value?: (boolean|null);
    }

    /** Represents a PushPref. */
    class PushPref implements IPushPref {

        /**
         * Constructs a new PushPref.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPushPref);

        /** PushPref name. */
        public name: server.PushPref.Name;

        /** PushPref value. */
        public value: boolean;

        /**
         * Creates a new PushPref instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PushPref instance
         */
        public static create(properties?: server.IPushPref): server.PushPref;

        /**
         * Encodes the specified PushPref message. Does not implicitly {@link server.PushPref.verify|verify} messages.
         * @param message PushPref message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPushPref, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PushPref message, length delimited. Does not implicitly {@link server.PushPref.verify|verify} messages.
         * @param message PushPref message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPushPref, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PushPref message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PushPref
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PushPref;

        /**
         * Decodes a PushPref message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PushPref
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PushPref;

        /**
         * Verifies a PushPref message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PushPref message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PushPref
         */
        public static fromObject(object: { [k: string]: any }): server.PushPref;

        /**
         * Creates a plain object from a PushPref message. Also converts values to other types if specified.
         * @param message PushPref
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PushPref, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PushPref to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace PushPref {

        /** Name enum. */
        enum Name {
            POST = 0,
            COMMENT = 1
        }
    }

    /** Properties of a NotificationPrefs. */
    interface INotificationPrefs {

        /** NotificationPrefs pushPrefs */
        pushPrefs?: (server.IPushPref[]|null);
    }

    /** Represents a NotificationPrefs. */
    class NotificationPrefs implements INotificationPrefs {

        /**
         * Constructs a new NotificationPrefs.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.INotificationPrefs);

        /** NotificationPrefs pushPrefs. */
        public pushPrefs: server.IPushPref[];

        /**
         * Creates a new NotificationPrefs instance using the specified properties.
         * @param [properties] Properties to set
         * @returns NotificationPrefs instance
         */
        public static create(properties?: server.INotificationPrefs): server.NotificationPrefs;

        /**
         * Encodes the specified NotificationPrefs message. Does not implicitly {@link server.NotificationPrefs.verify|verify} messages.
         * @param message NotificationPrefs message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.INotificationPrefs, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified NotificationPrefs message, length delimited. Does not implicitly {@link server.NotificationPrefs.verify|verify} messages.
         * @param message NotificationPrefs message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.INotificationPrefs, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a NotificationPrefs message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns NotificationPrefs
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.NotificationPrefs;

        /**
         * Decodes a NotificationPrefs message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns NotificationPrefs
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.NotificationPrefs;

        /**
         * Verifies a NotificationPrefs message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a NotificationPrefs message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns NotificationPrefs
         */
        public static fromObject(object: { [k: string]: any }): server.NotificationPrefs;

        /**
         * Creates a plain object from a NotificationPrefs message. Also converts values to other types if specified.
         * @param message NotificationPrefs
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.NotificationPrefs, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this NotificationPrefs to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a Rerequest. */
    interface IRerequest {

        /** Rerequest id */
        id?: (string|null);

        /** Rerequest identityKey */
        identityKey?: (Uint8Array|null);

        /** Rerequest signedPreKeyId */
        signedPreKeyId?: (number|Long|null);

        /** Rerequest oneTimePreKeyId */
        oneTimePreKeyId?: (number|Long|null);

        /** Rerequest sessionSetupEphemeralKey */
        sessionSetupEphemeralKey?: (Uint8Array|null);

        /** Rerequest messageEphemeralKey */
        messageEphemeralKey?: (Uint8Array|null);
    }

    /** Represents a Rerequest. */
    class Rerequest implements IRerequest {

        /**
         * Constructs a new Rerequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IRerequest);

        /** Rerequest id. */
        public id: string;

        /** Rerequest identityKey. */
        public identityKey: Uint8Array;

        /** Rerequest signedPreKeyId. */
        public signedPreKeyId: (number|Long);

        /** Rerequest oneTimePreKeyId. */
        public oneTimePreKeyId: (number|Long);

        /** Rerequest sessionSetupEphemeralKey. */
        public sessionSetupEphemeralKey: Uint8Array;

        /** Rerequest messageEphemeralKey. */
        public messageEphemeralKey: Uint8Array;

        /**
         * Creates a new Rerequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Rerequest instance
         */
        public static create(properties?: server.IRerequest): server.Rerequest;

        /**
         * Encodes the specified Rerequest message. Does not implicitly {@link server.Rerequest.verify|verify} messages.
         * @param message Rerequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IRerequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Rerequest message, length delimited. Does not implicitly {@link server.Rerequest.verify|verify} messages.
         * @param message Rerequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IRerequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Rerequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Rerequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Rerequest;

        /**
         * Decodes a Rerequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Rerequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Rerequest;

        /**
         * Verifies a Rerequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Rerequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Rerequest
         */
        public static fromObject(object: { [k: string]: any }): server.Rerequest;

        /**
         * Creates a plain object from a Rerequest message. Also converts values to other types if specified.
         * @param message Rerequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Rerequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Rerequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a GroupFeedRerequest. */
    interface IGroupFeedRerequest {

        /** GroupFeedRerequest gid */
        gid?: (string|null);

        /** GroupFeedRerequest id */
        id?: (string|null);
    }

    /** Represents a GroupFeedRerequest. */
    class GroupFeedRerequest implements IGroupFeedRerequest {

        /**
         * Constructs a new GroupFeedRerequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupFeedRerequest);

        /** GroupFeedRerequest gid. */
        public gid: string;

        /** GroupFeedRerequest id. */
        public id: string;

        /**
         * Creates a new GroupFeedRerequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupFeedRerequest instance
         */
        public static create(properties?: server.IGroupFeedRerequest): server.GroupFeedRerequest;

        /**
         * Encodes the specified GroupFeedRerequest message. Does not implicitly {@link server.GroupFeedRerequest.verify|verify} messages.
         * @param message GroupFeedRerequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupFeedRerequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupFeedRerequest message, length delimited. Does not implicitly {@link server.GroupFeedRerequest.verify|verify} messages.
         * @param message GroupFeedRerequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupFeedRerequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupFeedRerequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupFeedRerequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupFeedRerequest;

        /**
         * Decodes a GroupFeedRerequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupFeedRerequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupFeedRerequest;

        /**
         * Verifies a GroupFeedRerequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupFeedRerequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupFeedRerequest
         */
        public static fromObject(object: { [k: string]: any }): server.GroupFeedRerequest;

        /**
         * Creates a plain object from a GroupFeedRerequest message. Also converts values to other types if specified.
         * @param message GroupFeedRerequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupFeedRerequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupFeedRerequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a SeenReceipt. */
    interface ISeenReceipt {

        /** SeenReceipt id */
        id?: (string|null);

        /** SeenReceipt threadId */
        threadId?: (string|null);

        /** SeenReceipt timestamp */
        timestamp?: (number|Long|null);
    }

    /** Represents a SeenReceipt. */
    class SeenReceipt implements ISeenReceipt {

        /**
         * Constructs a new SeenReceipt.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISeenReceipt);

        /** SeenReceipt id. */
        public id: string;

        /** SeenReceipt threadId. */
        public threadId: string;

        /** SeenReceipt timestamp. */
        public timestamp: (number|Long);

        /**
         * Creates a new SeenReceipt instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SeenReceipt instance
         */
        public static create(properties?: server.ISeenReceipt): server.SeenReceipt;

        /**
         * Encodes the specified SeenReceipt message. Does not implicitly {@link server.SeenReceipt.verify|verify} messages.
         * @param message SeenReceipt message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISeenReceipt, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SeenReceipt message, length delimited. Does not implicitly {@link server.SeenReceipt.verify|verify} messages.
         * @param message SeenReceipt message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISeenReceipt, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SeenReceipt message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SeenReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SeenReceipt;

        /**
         * Decodes a SeenReceipt message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SeenReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SeenReceipt;

        /**
         * Verifies a SeenReceipt message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SeenReceipt message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SeenReceipt
         */
        public static fromObject(object: { [k: string]: any }): server.SeenReceipt;

        /**
         * Creates a plain object from a SeenReceipt message. Also converts values to other types if specified.
         * @param message SeenReceipt
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SeenReceipt, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SeenReceipt to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a DeliveryReceipt. */
    interface IDeliveryReceipt {

        /** DeliveryReceipt id */
        id?: (string|null);

        /** DeliveryReceipt threadId */
        threadId?: (string|null);

        /** DeliveryReceipt timestamp */
        timestamp?: (number|Long|null);
    }

    /** Represents a DeliveryReceipt. */
    class DeliveryReceipt implements IDeliveryReceipt {

        /**
         * Constructs a new DeliveryReceipt.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IDeliveryReceipt);

        /** DeliveryReceipt id. */
        public id: string;

        /** DeliveryReceipt threadId. */
        public threadId: string;

        /** DeliveryReceipt timestamp. */
        public timestamp: (number|Long);

        /**
         * Creates a new DeliveryReceipt instance using the specified properties.
         * @param [properties] Properties to set
         * @returns DeliveryReceipt instance
         */
        public static create(properties?: server.IDeliveryReceipt): server.DeliveryReceipt;

        /**
         * Encodes the specified DeliveryReceipt message. Does not implicitly {@link server.DeliveryReceipt.verify|verify} messages.
         * @param message DeliveryReceipt message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IDeliveryReceipt, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified DeliveryReceipt message, length delimited. Does not implicitly {@link server.DeliveryReceipt.verify|verify} messages.
         * @param message DeliveryReceipt message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IDeliveryReceipt, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a DeliveryReceipt message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns DeliveryReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.DeliveryReceipt;

        /**
         * Decodes a DeliveryReceipt message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns DeliveryReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.DeliveryReceipt;

        /**
         * Verifies a DeliveryReceipt message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a DeliveryReceipt message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns DeliveryReceipt
         */
        public static fromObject(object: { [k: string]: any }): server.DeliveryReceipt;

        /**
         * Creates a plain object from a DeliveryReceipt message. Also converts values to other types if specified.
         * @param message DeliveryReceipt
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.DeliveryReceipt, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this DeliveryReceipt to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a GroupChatRetract. */
    interface IGroupChatRetract {

        /** GroupChatRetract id */
        id?: (string|null);

        /** GroupChatRetract gid */
        gid?: (string|null);
    }

    /** Represents a GroupChatRetract. */
    class GroupChatRetract implements IGroupChatRetract {

        /**
         * Constructs a new GroupChatRetract.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupChatRetract);

        /** GroupChatRetract id. */
        public id: string;

        /** GroupChatRetract gid. */
        public gid: string;

        /**
         * Creates a new GroupChatRetract instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupChatRetract instance
         */
        public static create(properties?: server.IGroupChatRetract): server.GroupChatRetract;

        /**
         * Encodes the specified GroupChatRetract message. Does not implicitly {@link server.GroupChatRetract.verify|verify} messages.
         * @param message GroupChatRetract message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupChatRetract, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupChatRetract message, length delimited. Does not implicitly {@link server.GroupChatRetract.verify|verify} messages.
         * @param message GroupChatRetract message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupChatRetract, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupChatRetract message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupChatRetract
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupChatRetract;

        /**
         * Decodes a GroupChatRetract message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupChatRetract
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupChatRetract;

        /**
         * Verifies a GroupChatRetract message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupChatRetract message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupChatRetract
         */
        public static fromObject(object: { [k: string]: any }): server.GroupChatRetract;

        /**
         * Creates a plain object from a GroupChatRetract message. Also converts values to other types if specified.
         * @param message GroupChatRetract
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupChatRetract, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupChatRetract to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a ChatRetract. */
    interface IChatRetract {

        /** ChatRetract id */
        id?: (string|null);
    }

    /** Represents a ChatRetract. */
    class ChatRetract implements IChatRetract {

        /**
         * Constructs a new ChatRetract.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IChatRetract);

        /** ChatRetract id. */
        public id: string;

        /**
         * Creates a new ChatRetract instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ChatRetract instance
         */
        public static create(properties?: server.IChatRetract): server.ChatRetract;

        /**
         * Encodes the specified ChatRetract message. Does not implicitly {@link server.ChatRetract.verify|verify} messages.
         * @param message ChatRetract message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IChatRetract, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ChatRetract message, length delimited. Does not implicitly {@link server.ChatRetract.verify|verify} messages.
         * @param message ChatRetract message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IChatRetract, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ChatRetract message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ChatRetract
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ChatRetract;

        /**
         * Decodes a ChatRetract message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ChatRetract
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ChatRetract;

        /**
         * Verifies a ChatRetract message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ChatRetract message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ChatRetract
         */
        public static fromObject(object: { [k: string]: any }): server.ChatRetract;

        /**
         * Creates a plain object from a ChatRetract message. Also converts values to other types if specified.
         * @param message ChatRetract
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ChatRetract, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ChatRetract to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a Prop. */
    interface IProp {

        /** Prop name */
        name?: (string|null);

        /** Prop value */
        value?: (string|null);
    }

    /** Represents a Prop. */
    class Prop implements IProp {

        /**
         * Constructs a new Prop.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IProp);

        /** Prop name. */
        public name: string;

        /** Prop value. */
        public value: string;

        /**
         * Creates a new Prop instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Prop instance
         */
        public static create(properties?: server.IProp): server.Prop;

        /**
         * Encodes the specified Prop message. Does not implicitly {@link server.Prop.verify|verify} messages.
         * @param message Prop message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IProp, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Prop message, length delimited. Does not implicitly {@link server.Prop.verify|verify} messages.
         * @param message Prop message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IProp, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Prop message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Prop
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Prop;

        /**
         * Decodes a Prop message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Prop
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Prop;

        /**
         * Verifies a Prop message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Prop message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Prop
         */
        public static fromObject(object: { [k: string]: any }): server.Prop;

        /**
         * Creates a plain object from a Prop message. Also converts values to other types if specified.
         * @param message Prop
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Prop, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Prop to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a Props. */
    interface IProps {

        /** Props hash */
        hash?: (Uint8Array|null);

        /** Props props */
        props?: (server.IProp[]|null);
    }

    /** Represents a Props. */
    class Props implements IProps {

        /**
         * Constructs a new Props.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IProps);

        /** Props hash. */
        public hash: Uint8Array;

        /** Props props. */
        public props: server.IProp[];

        /**
         * Creates a new Props instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Props instance
         */
        public static create(properties?: server.IProps): server.Props;

        /**
         * Encodes the specified Props message. Does not implicitly {@link server.Props.verify|verify} messages.
         * @param message Props message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IProps, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Props message, length delimited. Does not implicitly {@link server.Props.verify|verify} messages.
         * @param message Props message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IProps, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Props message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Props
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Props;

        /**
         * Decodes a Props message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Props
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Props;

        /**
         * Verifies a Props message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Props message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Props
         */
        public static fromObject(object: { [k: string]: any }): server.Props;

        /**
         * Creates a plain object from a Props message. Also converts values to other types if specified.
         * @param message Props
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Props, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Props to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a WhisperKeys. */
    interface IWhisperKeys {

        /** WhisperKeys uid */
        uid?: (number|Long|null);

        /** WhisperKeys action */
        action?: (server.WhisperKeys.Action|null);

        /** WhisperKeys identityKey */
        identityKey?: (Uint8Array|null);

        /** WhisperKeys signedKey */
        signedKey?: (Uint8Array|null);

        /** WhisperKeys otpKeyCount */
        otpKeyCount?: (number|null);

        /** WhisperKeys oneTimeKeys */
        oneTimeKeys?: (Uint8Array[]|null);
    }

    /** Represents a WhisperKeys. */
    class WhisperKeys implements IWhisperKeys {

        /**
         * Constructs a new WhisperKeys.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IWhisperKeys);

        /** WhisperKeys uid. */
        public uid: (number|Long);

        /** WhisperKeys action. */
        public action: server.WhisperKeys.Action;

        /** WhisperKeys identityKey. */
        public identityKey: Uint8Array;

        /** WhisperKeys signedKey. */
        public signedKey: Uint8Array;

        /** WhisperKeys otpKeyCount. */
        public otpKeyCount: number;

        /** WhisperKeys oneTimeKeys. */
        public oneTimeKeys: Uint8Array[];

        /**
         * Creates a new WhisperKeys instance using the specified properties.
         * @param [properties] Properties to set
         * @returns WhisperKeys instance
         */
        public static create(properties?: server.IWhisperKeys): server.WhisperKeys;

        /**
         * Encodes the specified WhisperKeys message. Does not implicitly {@link server.WhisperKeys.verify|verify} messages.
         * @param message WhisperKeys message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IWhisperKeys, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified WhisperKeys message, length delimited. Does not implicitly {@link server.WhisperKeys.verify|verify} messages.
         * @param message WhisperKeys message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IWhisperKeys, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a WhisperKeys message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns WhisperKeys
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.WhisperKeys;

        /**
         * Decodes a WhisperKeys message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns WhisperKeys
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.WhisperKeys;

        /**
         * Verifies a WhisperKeys message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a WhisperKeys message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns WhisperKeys
         */
        public static fromObject(object: { [k: string]: any }): server.WhisperKeys;

        /**
         * Creates a plain object from a WhisperKeys message. Also converts values to other types if specified.
         * @param message WhisperKeys
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.WhisperKeys, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this WhisperKeys to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace WhisperKeys {

        /** Action enum. */
        enum Action {
            NORMAL = 0,
            ADD = 1,
            COUNT = 2,
            GET = 3,
            SET = 4,
            UPDATE = 5
        }
    }

    /** Properties of a NoiseMessage. */
    interface INoiseMessage {

        /** NoiseMessage messageType */
        messageType?: (server.NoiseMessage.MessageType|null);

        /** NoiseMessage content */
        content?: (Uint8Array|null);
    }

    /** Represents a NoiseMessage. */
    class NoiseMessage implements INoiseMessage {

        /**
         * Constructs a new NoiseMessage.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.INoiseMessage);

        /** NoiseMessage messageType. */
        public messageType: server.NoiseMessage.MessageType;

        /** NoiseMessage content. */
        public content: Uint8Array;

        /**
         * Creates a new NoiseMessage instance using the specified properties.
         * @param [properties] Properties to set
         * @returns NoiseMessage instance
         */
        public static create(properties?: server.INoiseMessage): server.NoiseMessage;

        /**
         * Encodes the specified NoiseMessage message. Does not implicitly {@link server.NoiseMessage.verify|verify} messages.
         * @param message NoiseMessage message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.INoiseMessage, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified NoiseMessage message, length delimited. Does not implicitly {@link server.NoiseMessage.verify|verify} messages.
         * @param message NoiseMessage message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.INoiseMessage, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a NoiseMessage message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns NoiseMessage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.NoiseMessage;

        /**
         * Decodes a NoiseMessage message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns NoiseMessage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.NoiseMessage;

        /**
         * Verifies a NoiseMessage message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a NoiseMessage message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns NoiseMessage
         */
        public static fromObject(object: { [k: string]: any }): server.NoiseMessage;

        /**
         * Creates a plain object from a NoiseMessage message. Also converts values to other types if specified.
         * @param message NoiseMessage
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.NoiseMessage, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this NoiseMessage to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace NoiseMessage {

        /** MessageType enum. */
        enum MessageType {
            XX_A = 0,
            XX_B = 1,
            XX_C = 2,
            IK_A = 3,
            IK_B = 4,
            XX_FALLBACK_A = 5,
            XX_FALLBACK_B = 6
        }
    }

    /** Properties of a DeleteAccount. */
    interface IDeleteAccount {

        /** DeleteAccount phone */
        phone?: (string|null);
    }

    /** Represents a DeleteAccount. */
    class DeleteAccount implements IDeleteAccount {

        /**
         * Constructs a new DeleteAccount.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IDeleteAccount);

        /** DeleteAccount phone. */
        public phone: string;

        /**
         * Creates a new DeleteAccount instance using the specified properties.
         * @param [properties] Properties to set
         * @returns DeleteAccount instance
         */
        public static create(properties?: server.IDeleteAccount): server.DeleteAccount;

        /**
         * Encodes the specified DeleteAccount message. Does not implicitly {@link server.DeleteAccount.verify|verify} messages.
         * @param message DeleteAccount message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IDeleteAccount, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified DeleteAccount message, length delimited. Does not implicitly {@link server.DeleteAccount.verify|verify} messages.
         * @param message DeleteAccount message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IDeleteAccount, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a DeleteAccount message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns DeleteAccount
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.DeleteAccount;

        /**
         * Decodes a DeleteAccount message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns DeleteAccount
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.DeleteAccount;

        /**
         * Verifies a DeleteAccount message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a DeleteAccount message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns DeleteAccount
         */
        public static fromObject(object: { [k: string]: any }): server.DeleteAccount;

        /**
         * Creates a plain object from a DeleteAccount message. Also converts values to other types if specified.
         * @param message DeleteAccount
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.DeleteAccount, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this DeleteAccount to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a PushContent. */
    interface IPushContent {

        /** PushContent certificate */
        certificate?: (Uint8Array|null);

        /** PushContent content */
        content?: (Uint8Array|null);
    }

    /** Represents a PushContent. */
    class PushContent implements IPushContent {

        /**
         * Constructs a new PushContent.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPushContent);

        /** PushContent certificate. */
        public certificate: Uint8Array;

        /** PushContent content. */
        public content: Uint8Array;

        /**
         * Creates a new PushContent instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PushContent instance
         */
        public static create(properties?: server.IPushContent): server.PushContent;

        /**
         * Encodes the specified PushContent message. Does not implicitly {@link server.PushContent.verify|verify} messages.
         * @param message PushContent message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPushContent, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PushContent message, length delimited. Does not implicitly {@link server.PushContent.verify|verify} messages.
         * @param message PushContent message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPushContent, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PushContent message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PushContent
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PushContent;

        /**
         * Decodes a PushContent message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PushContent
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PushContent;

        /**
         * Verifies a PushContent message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PushContent message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PushContent
         */
        public static fromObject(object: { [k: string]: any }): server.PushContent;

        /**
         * Creates a plain object from a PushContent message. Also converts values to other types if specified.
         * @param message PushContent
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PushContent, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PushContent to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an InviteeNotice. */
    interface IInviteeNotice {

        /** InviteeNotice inviters */
        inviters?: (server.IInviter[]|null);
    }

    /** Represents an InviteeNotice. */
    class InviteeNotice implements IInviteeNotice {

        /**
         * Constructs a new InviteeNotice.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IInviteeNotice);

        /** InviteeNotice inviters. */
        public inviters: server.IInviter[];

        /**
         * Creates a new InviteeNotice instance using the specified properties.
         * @param [properties] Properties to set
         * @returns InviteeNotice instance
         */
        public static create(properties?: server.IInviteeNotice): server.InviteeNotice;

        /**
         * Encodes the specified InviteeNotice message. Does not implicitly {@link server.InviteeNotice.verify|verify} messages.
         * @param message InviteeNotice message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IInviteeNotice, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified InviteeNotice message, length delimited. Does not implicitly {@link server.InviteeNotice.verify|verify} messages.
         * @param message InviteeNotice message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IInviteeNotice, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an InviteeNotice message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns InviteeNotice
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.InviteeNotice;

        /**
         * Decodes an InviteeNotice message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns InviteeNotice
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.InviteeNotice;

        /**
         * Verifies an InviteeNotice message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an InviteeNotice message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns InviteeNotice
         */
        public static fromObject(object: { [k: string]: any }): server.InviteeNotice;

        /**
         * Creates a plain object from an InviteeNotice message. Also converts values to other types if specified.
         * @param message InviteeNotice
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.InviteeNotice, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this InviteeNotice to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an Inviter. */
    interface IInviter {

        /** Inviter uid */
        uid?: (number|Long|null);

        /** Inviter name */
        name?: (string|null);

        /** Inviter phone */
        phone?: (string|null);

        /** Inviter timestamp */
        timestamp?: (number|Long|null);
    }

    /** Represents an Inviter. */
    class Inviter implements IInviter {

        /**
         * Constructs a new Inviter.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IInviter);

        /** Inviter uid. */
        public uid: (number|Long);

        /** Inviter name. */
        public name: string;

        /** Inviter phone. */
        public phone: string;

        /** Inviter timestamp. */
        public timestamp: (number|Long);

        /**
         * Creates a new Inviter instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Inviter instance
         */
        public static create(properties?: server.IInviter): server.Inviter;

        /**
         * Encodes the specified Inviter message. Does not implicitly {@link server.Inviter.verify|verify} messages.
         * @param message Inviter message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IInviter, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Inviter message, length delimited. Does not implicitly {@link server.Inviter.verify|verify} messages.
         * @param message Inviter message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IInviter, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an Inviter message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Inviter
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Inviter;

        /**
         * Decodes an Inviter message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Inviter
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Inviter;

        /**
         * Verifies an Inviter message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an Inviter message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Inviter
         */
        public static fromObject(object: { [k: string]: any }): server.Inviter;

        /**
         * Creates a plain object from an Inviter message. Also converts values to other types if specified.
         * @param message Inviter
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Inviter, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Inviter to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an EventData. */
    interface IEventData {

        /** EventData uid */
        uid?: (number|Long|null);

        /** EventData platform */
        platform?: (server.Platform|null);

        /** EventData version */
        version?: (string|null);

        /** EventData timestampMs */
        timestampMs?: (number|Long|null);

        /** EventData mediaUpload */
        mediaUpload?: (server.IMediaUpload|null);

        /** EventData mediaDownload */
        mediaDownload?: (server.IMediaDownload|null);

        /** EventData mediaComposeLoad */
        mediaComposeLoad?: (server.IMediaComposeLoad|null);

        /** EventData pushReceived */
        pushReceived?: (server.IPushReceived|null);

        /** EventData decryptionReport */
        decryptionReport?: (server.IDecryptionReport|null);

        /** EventData permissions */
        permissions?: (server.IPermissions|null);
    }

    /** Represents an EventData. */
    class EventData implements IEventData {

        /**
         * Constructs a new EventData.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IEventData);

        /** EventData uid. */
        public uid: (number|Long);

        /** EventData platform. */
        public platform: server.Platform;

        /** EventData version. */
        public version: string;

        /** EventData timestampMs. */
        public timestampMs: (number|Long);

        /** EventData mediaUpload. */
        public mediaUpload?: (server.IMediaUpload|null);

        /** EventData mediaDownload. */
        public mediaDownload?: (server.IMediaDownload|null);

        /** EventData mediaComposeLoad. */
        public mediaComposeLoad?: (server.IMediaComposeLoad|null);

        /** EventData pushReceived. */
        public pushReceived?: (server.IPushReceived|null);

        /** EventData decryptionReport. */
        public decryptionReport?: (server.IDecryptionReport|null);

        /** EventData permissions. */
        public permissions?: (server.IPermissions|null);

        /** EventData edata. */
        public edata?: ("mediaUpload"|"mediaDownload"|"mediaComposeLoad"|"pushReceived"|"decryptionReport"|"permissions");

        /**
         * Creates a new EventData instance using the specified properties.
         * @param [properties] Properties to set
         * @returns EventData instance
         */
        public static create(properties?: server.IEventData): server.EventData;

        /**
         * Encodes the specified EventData message. Does not implicitly {@link server.EventData.verify|verify} messages.
         * @param message EventData message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IEventData, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified EventData message, length delimited. Does not implicitly {@link server.EventData.verify|verify} messages.
         * @param message EventData message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IEventData, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an EventData message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns EventData
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.EventData;

        /**
         * Decodes an EventData message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns EventData
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.EventData;

        /**
         * Verifies an EventData message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an EventData message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns EventData
         */
        public static fromObject(object: { [k: string]: any }): server.EventData;

        /**
         * Creates a plain object from an EventData message. Also converts values to other types if specified.
         * @param message EventData
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.EventData, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this EventData to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Platform enum. */
    enum Platform {
        UNKNOWN = 0,
        IOS = 1,
        ANDROID = 2
    }

    /** Properties of a MediaUpload. */
    interface IMediaUpload {

        /** MediaUpload id */
        id?: (string|null);

        /** MediaUpload type */
        type?: (server.MediaUpload.Type|null);

        /** MediaUpload durationMs */
        durationMs?: (number|null);

        /** MediaUpload numPhotos */
        numPhotos?: (number|null);

        /** MediaUpload numVideos */
        numVideos?: (number|null);

        /** MediaUpload totalSize */
        totalSize?: (number|null);

        /** MediaUpload status */
        status?: (server.MediaUpload.Status|null);

        /** MediaUpload retryCount */
        retryCount?: (number|null);
    }

    /** Represents a MediaUpload. */
    class MediaUpload implements IMediaUpload {

        /**
         * Constructs a new MediaUpload.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMediaUpload);

        /** MediaUpload id. */
        public id: string;

        /** MediaUpload type. */
        public type: server.MediaUpload.Type;

        /** MediaUpload durationMs. */
        public durationMs: number;

        /** MediaUpload numPhotos. */
        public numPhotos: number;

        /** MediaUpload numVideos. */
        public numVideos: number;

        /** MediaUpload totalSize. */
        public totalSize: number;

        /** MediaUpload status. */
        public status: server.MediaUpload.Status;

        /** MediaUpload retryCount. */
        public retryCount: number;

        /**
         * Creates a new MediaUpload instance using the specified properties.
         * @param [properties] Properties to set
         * @returns MediaUpload instance
         */
        public static create(properties?: server.IMediaUpload): server.MediaUpload;

        /**
         * Encodes the specified MediaUpload message. Does not implicitly {@link server.MediaUpload.verify|verify} messages.
         * @param message MediaUpload message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMediaUpload, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified MediaUpload message, length delimited. Does not implicitly {@link server.MediaUpload.verify|verify} messages.
         * @param message MediaUpload message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMediaUpload, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a MediaUpload message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns MediaUpload
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.MediaUpload;

        /**
         * Decodes a MediaUpload message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns MediaUpload
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.MediaUpload;

        /**
         * Verifies a MediaUpload message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a MediaUpload message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns MediaUpload
         */
        public static fromObject(object: { [k: string]: any }): server.MediaUpload;

        /**
         * Creates a plain object from a MediaUpload message. Also converts values to other types if specified.
         * @param message MediaUpload
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.MediaUpload, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this MediaUpload to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace MediaUpload {

        /** Type enum. */
        enum Type {
            POST = 0,
            MESSAGE = 1,
            COMMENT = 2
        }

        /** Status enum. */
        enum Status {
            OK = 0,
            FAIL = 1
        }
    }

    /** Properties of a MediaDownload. */
    interface IMediaDownload {

        /** MediaDownload id */
        id?: (string|null);

        /** MediaDownload type */
        type?: (server.MediaDownload.Type|null);

        /** MediaDownload durationMs */
        durationMs?: (number|null);

        /** MediaDownload numPhotos */
        numPhotos?: (number|null);

        /** MediaDownload numVideos */
        numVideos?: (number|null);

        /** MediaDownload totalSize */
        totalSize?: (number|null);

        /** MediaDownload status */
        status?: (server.MediaDownload.Status|null);

        /** MediaDownload retryCount */
        retryCount?: (number|null);
    }

    /** Represents a MediaDownload. */
    class MediaDownload implements IMediaDownload {

        /**
         * Constructs a new MediaDownload.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMediaDownload);

        /** MediaDownload id. */
        public id: string;

        /** MediaDownload type. */
        public type: server.MediaDownload.Type;

        /** MediaDownload durationMs. */
        public durationMs: number;

        /** MediaDownload numPhotos. */
        public numPhotos: number;

        /** MediaDownload numVideos. */
        public numVideos: number;

        /** MediaDownload totalSize. */
        public totalSize: number;

        /** MediaDownload status. */
        public status: server.MediaDownload.Status;

        /** MediaDownload retryCount. */
        public retryCount: number;

        /**
         * Creates a new MediaDownload instance using the specified properties.
         * @param [properties] Properties to set
         * @returns MediaDownload instance
         */
        public static create(properties?: server.IMediaDownload): server.MediaDownload;

        /**
         * Encodes the specified MediaDownload message. Does not implicitly {@link server.MediaDownload.verify|verify} messages.
         * @param message MediaDownload message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMediaDownload, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified MediaDownload message, length delimited. Does not implicitly {@link server.MediaDownload.verify|verify} messages.
         * @param message MediaDownload message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMediaDownload, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a MediaDownload message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns MediaDownload
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.MediaDownload;

        /**
         * Decodes a MediaDownload message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns MediaDownload
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.MediaDownload;

        /**
         * Verifies a MediaDownload message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a MediaDownload message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns MediaDownload
         */
        public static fromObject(object: { [k: string]: any }): server.MediaDownload;

        /**
         * Creates a plain object from a MediaDownload message. Also converts values to other types if specified.
         * @param message MediaDownload
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.MediaDownload, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this MediaDownload to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace MediaDownload {

        /** Type enum. */
        enum Type {
            POST = 0,
            MESSAGE = 1,
            COMMENT = 2
        }

        /** Status enum. */
        enum Status {
            OK = 0,
            FAIL = 1
        }
    }

    /** Properties of a MediaComposeLoad. */
    interface IMediaComposeLoad {

        /** MediaComposeLoad durationMs */
        durationMs?: (number|null);

        /** MediaComposeLoad numPhotos */
        numPhotos?: (number|null);

        /** MediaComposeLoad numVideos */
        numVideos?: (number|null);

        /** MediaComposeLoad totalSize */
        totalSize?: (number|null);
    }

    /** Represents a MediaComposeLoad. */
    class MediaComposeLoad implements IMediaComposeLoad {

        /**
         * Constructs a new MediaComposeLoad.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMediaComposeLoad);

        /** MediaComposeLoad durationMs. */
        public durationMs: number;

        /** MediaComposeLoad numPhotos. */
        public numPhotos: number;

        /** MediaComposeLoad numVideos. */
        public numVideos: number;

        /** MediaComposeLoad totalSize. */
        public totalSize: number;

        /**
         * Creates a new MediaComposeLoad instance using the specified properties.
         * @param [properties] Properties to set
         * @returns MediaComposeLoad instance
         */
        public static create(properties?: server.IMediaComposeLoad): server.MediaComposeLoad;

        /**
         * Encodes the specified MediaComposeLoad message. Does not implicitly {@link server.MediaComposeLoad.verify|verify} messages.
         * @param message MediaComposeLoad message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMediaComposeLoad, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified MediaComposeLoad message, length delimited. Does not implicitly {@link server.MediaComposeLoad.verify|verify} messages.
         * @param message MediaComposeLoad message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMediaComposeLoad, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a MediaComposeLoad message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns MediaComposeLoad
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.MediaComposeLoad;

        /**
         * Decodes a MediaComposeLoad message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns MediaComposeLoad
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.MediaComposeLoad;

        /**
         * Verifies a MediaComposeLoad message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a MediaComposeLoad message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns MediaComposeLoad
         */
        public static fromObject(object: { [k: string]: any }): server.MediaComposeLoad;

        /**
         * Creates a plain object from a MediaComposeLoad message. Also converts values to other types if specified.
         * @param message MediaComposeLoad
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.MediaComposeLoad, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this MediaComposeLoad to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a PushReceived. */
    interface IPushReceived {

        /** PushReceived id */
        id?: (string|null);

        /** PushReceived clientTimestamp */
        clientTimestamp?: (number|Long|null);
    }

    /** Represents a PushReceived. */
    class PushReceived implements IPushReceived {

        /**
         * Constructs a new PushReceived.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPushReceived);

        /** PushReceived id. */
        public id: string;

        /** PushReceived clientTimestamp. */
        public clientTimestamp: (number|Long);

        /**
         * Creates a new PushReceived instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PushReceived instance
         */
        public static create(properties?: server.IPushReceived): server.PushReceived;

        /**
         * Encodes the specified PushReceived message. Does not implicitly {@link server.PushReceived.verify|verify} messages.
         * @param message PushReceived message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPushReceived, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PushReceived message, length delimited. Does not implicitly {@link server.PushReceived.verify|verify} messages.
         * @param message PushReceived message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPushReceived, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PushReceived message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PushReceived
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PushReceived;

        /**
         * Decodes a PushReceived message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PushReceived
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PushReceived;

        /**
         * Verifies a PushReceived message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PushReceived message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PushReceived
         */
        public static fromObject(object: { [k: string]: any }): server.PushReceived;

        /**
         * Creates a plain object from a PushReceived message. Also converts values to other types if specified.
         * @param message PushReceived
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PushReceived, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PushReceived to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a DecryptionReport. */
    interface IDecryptionReport {

        /** DecryptionReport result */
        result?: (server.DecryptionReport.Status|null);

        /** DecryptionReport reason */
        reason?: (string|null);

        /** DecryptionReport msgId */
        msgId?: (string|null);

        /** DecryptionReport originalVersion */
        originalVersion?: (string|null);

        /** DecryptionReport senderPlatform */
        senderPlatform?: (server.Platform|null);

        /** DecryptionReport senderVersion */
        senderVersion?: (string|null);

        /** DecryptionReport rerequestCount */
        rerequestCount?: (number|null);

        /** DecryptionReport timeTakenS */
        timeTakenS?: (number|null);

        /** DecryptionReport isSilent */
        isSilent?: (boolean|null);
    }

    /** Represents a DecryptionReport. */
    class DecryptionReport implements IDecryptionReport {

        /**
         * Constructs a new DecryptionReport.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IDecryptionReport);

        /** DecryptionReport result. */
        public result: server.DecryptionReport.Status;

        /** DecryptionReport reason. */
        public reason: string;

        /** DecryptionReport msgId. */
        public msgId: string;

        /** DecryptionReport originalVersion. */
        public originalVersion: string;

        /** DecryptionReport senderPlatform. */
        public senderPlatform: server.Platform;

        /** DecryptionReport senderVersion. */
        public senderVersion: string;

        /** DecryptionReport rerequestCount. */
        public rerequestCount: number;

        /** DecryptionReport timeTakenS. */
        public timeTakenS: number;

        /** DecryptionReport isSilent. */
        public isSilent: boolean;

        /**
         * Creates a new DecryptionReport instance using the specified properties.
         * @param [properties] Properties to set
         * @returns DecryptionReport instance
         */
        public static create(properties?: server.IDecryptionReport): server.DecryptionReport;

        /**
         * Encodes the specified DecryptionReport message. Does not implicitly {@link server.DecryptionReport.verify|verify} messages.
         * @param message DecryptionReport message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IDecryptionReport, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified DecryptionReport message, length delimited. Does not implicitly {@link server.DecryptionReport.verify|verify} messages.
         * @param message DecryptionReport message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IDecryptionReport, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a DecryptionReport message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns DecryptionReport
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.DecryptionReport;

        /**
         * Decodes a DecryptionReport message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns DecryptionReport
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.DecryptionReport;

        /**
         * Verifies a DecryptionReport message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a DecryptionReport message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns DecryptionReport
         */
        public static fromObject(object: { [k: string]: any }): server.DecryptionReport;

        /**
         * Creates a plain object from a DecryptionReport message. Also converts values to other types if specified.
         * @param message DecryptionReport
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.DecryptionReport, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this DecryptionReport to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace DecryptionReport {

        /** Status enum. */
        enum Status {
            OK = 0,
            FAIL = 1
        }
    }

    /** Properties of a Permissions. */
    interface IPermissions {

        /** Permissions type */
        type?: (server.Permissions.Type|null);

        /** Permissions status */
        status?: (server.Permissions.Status|null);
    }

    /** Represents a Permissions. */
    class Permissions implements IPermissions {

        /**
         * Constructs a new Permissions.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPermissions);

        /** Permissions type. */
        public type: server.Permissions.Type;

        /** Permissions status. */
        public status: server.Permissions.Status;

        /**
         * Creates a new Permissions instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Permissions instance
         */
        public static create(properties?: server.IPermissions): server.Permissions;

        /**
         * Encodes the specified Permissions message. Does not implicitly {@link server.Permissions.verify|verify} messages.
         * @param message Permissions message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPermissions, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Permissions message, length delimited. Does not implicitly {@link server.Permissions.verify|verify} messages.
         * @param message Permissions message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPermissions, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Permissions message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Permissions
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Permissions;

        /**
         * Decodes a Permissions message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Permissions
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Permissions;

        /**
         * Verifies a Permissions message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Permissions message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Permissions
         */
        public static fromObject(object: { [k: string]: any }): server.Permissions;

        /**
         * Creates a plain object from a Permissions message. Also converts values to other types if specified.
         * @param message Permissions
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Permissions, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Permissions to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace Permissions {

        /** Type enum. */
        enum Type {
            CONTACTS = 0,
            NOTIFICATIONS = 1
        }

        /** Status enum. */
        enum Status {
            ALLOWED = 0,
            DENIED = 1
        }
    }
}
