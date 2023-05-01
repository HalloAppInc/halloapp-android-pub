import * as $protobuf from "protobufjs";
/** Namespace server. */
export namespace server {

    /** Properties of an UploadAvatar. */
    interface IUploadAvatar {

        /** UploadAvatar id */
        id?: (string|null);

        /** UploadAvatar data */
        data?: (Uint8Array|null);

        /** UploadAvatar fullData */
        fullData?: (Uint8Array|null);
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

        /** UploadAvatar fullData. */
        public fullData: Uint8Array;

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

        /** UploadGroupAvatar fullData */
        fullData?: (Uint8Array|null);
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

        /** UploadGroupAvatar fullData. */
        public fullData: Uint8Array;

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

    /** Properties of a DeviceInfo. */
    interface IDeviceInfo {

        /** DeviceInfo device */
        device?: (string|null);

        /** DeviceInfo osVersion */
        osVersion?: (string|null);
    }

    /** Represents a DeviceInfo. */
    class DeviceInfo implements IDeviceInfo {

        /**
         * Constructs a new DeviceInfo.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IDeviceInfo);

        /** DeviceInfo device. */
        public device: string;

        /** DeviceInfo osVersion. */
        public osVersion: string;

        /**
         * Creates a new DeviceInfo instance using the specified properties.
         * @param [properties] Properties to set
         * @returns DeviceInfo instance
         */
        public static create(properties?: server.IDeviceInfo): server.DeviceInfo;

        /**
         * Encodes the specified DeviceInfo message. Does not implicitly {@link server.DeviceInfo.verify|verify} messages.
         * @param message DeviceInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IDeviceInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified DeviceInfo message, length delimited. Does not implicitly {@link server.DeviceInfo.verify|verify} messages.
         * @param message DeviceInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IDeviceInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a DeviceInfo message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns DeviceInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.DeviceInfo;

        /**
         * Decodes a DeviceInfo message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns DeviceInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.DeviceInfo;

        /**
         * Verifies a DeviceInfo message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a DeviceInfo message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns DeviceInfo
         */
        public static fromObject(object: { [k: string]: any }): server.DeviceInfo;

        /**
         * Creates a plain object from a DeviceInfo message. Also converts values to other types if specified.
         * @param message DeviceInfo
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.DeviceInfo, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this DeviceInfo to JSON.
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

        /** Contact name */
        name?: (string|null);

        /** Contact numPotentialFriends */
        numPotentialFriends?: (number|Long|null);

        /** Contact numPotentialCloseFriends */
        numPotentialCloseFriends?: (number|Long|null);

        /** Contact invitationRank */
        invitationRank?: (number|Long|null);
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

        /** Contact name. */
        public name: string;

        /** Contact numPotentialFriends. */
        public numPotentialFriends: (number|Long);

        /** Contact numPotentialCloseFriends. */
        public numPotentialCloseFriends: (number|Long);

        /** Contact invitationRank. */
        public invitationRank: (number|Long);

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

        /** ContactList hasPermissions */
        hasPermissions?: (boolean|null);
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

        /** ContactList hasPermissions. */
        public hasPermissions: boolean;

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

    /** Properties of a ContactSyncError. */
    interface IContactSyncError {

        /** ContactSyncError retryAfterSecs */
        retryAfterSecs?: (number|null);
    }

    /** Represents a ContactSyncError. */
    class ContactSyncError implements IContactSyncError {

        /**
         * Constructs a new ContactSyncError.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IContactSyncError);

        /** ContactSyncError retryAfterSecs. */
        public retryAfterSecs: number;

        /**
         * Creates a new ContactSyncError instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ContactSyncError instance
         */
        public static create(properties?: server.IContactSyncError): server.ContactSyncError;

        /**
         * Encodes the specified ContactSyncError message. Does not implicitly {@link server.ContactSyncError.verify|verify} messages.
         * @param message ContactSyncError message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IContactSyncError, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ContactSyncError message, length delimited. Does not implicitly {@link server.ContactSyncError.verify|verify} messages.
         * @param message ContactSyncError message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IContactSyncError, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ContactSyncError message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ContactSyncError
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ContactSyncError;

        /**
         * Decodes a ContactSyncError message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ContactSyncError
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ContactSyncError;

        /**
         * Verifies a ContactSyncError message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ContactSyncError message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ContactSyncError
         */
        public static fromObject(object: { [k: string]: any }): server.ContactSyncError;

        /**
         * Creates a plain object from a ContactSyncError message. Also converts values to other types if specified.
         * @param message ContactSyncError
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ContactSyncError, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ContactSyncError to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a MomentInfo. */
    interface IMomentInfo {

        /** MomentInfo notificationTimestamp */
        notificationTimestamp?: (number|Long|null);

        /** MomentInfo timeTaken */
        timeTaken?: (number|Long|null);

        /** MomentInfo numTakes */
        numTakes?: (number|Long|null);

        /** MomentInfo numSelfieTakes */
        numSelfieTakes?: (number|Long|null);

        /** MomentInfo notificationId */
        notificationId?: (number|Long|null);

        /** MomentInfo contentType */
        contentType?: (server.MomentInfo.ContentType|null);

        /** MomentInfo date */
        date?: (string|null);
    }

    /** Represents a MomentInfo. */
    class MomentInfo implements IMomentInfo {

        /**
         * Constructs a new MomentInfo.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMomentInfo);

        /** MomentInfo notificationTimestamp. */
        public notificationTimestamp: (number|Long);

        /** MomentInfo timeTaken. */
        public timeTaken: (number|Long);

        /** MomentInfo numTakes. */
        public numTakes: (number|Long);

        /** MomentInfo numSelfieTakes. */
        public numSelfieTakes: (number|Long);

        /** MomentInfo notificationId. */
        public notificationId: (number|Long);

        /** MomentInfo contentType. */
        public contentType: server.MomentInfo.ContentType;

        /** MomentInfo date. */
        public date: string;

        /**
         * Creates a new MomentInfo instance using the specified properties.
         * @param [properties] Properties to set
         * @returns MomentInfo instance
         */
        public static create(properties?: server.IMomentInfo): server.MomentInfo;

        /**
         * Encodes the specified MomentInfo message. Does not implicitly {@link server.MomentInfo.verify|verify} messages.
         * @param message MomentInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMomentInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified MomentInfo message, length delimited. Does not implicitly {@link server.MomentInfo.verify|verify} messages.
         * @param message MomentInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMomentInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a MomentInfo message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns MomentInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.MomentInfo;

        /**
         * Decodes a MomentInfo message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns MomentInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.MomentInfo;

        /**
         * Verifies a MomentInfo message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a MomentInfo message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns MomentInfo
         */
        public static fromObject(object: { [k: string]: any }): server.MomentInfo;

        /**
         * Creates a plain object from a MomentInfo message. Also converts values to other types if specified.
         * @param message MomentInfo
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.MomentInfo, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this MomentInfo to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace MomentInfo {

        /** ContentType enum. */
        enum ContentType {
            IMAGE = 0,
            VIDEO = 1,
            TEXT = 2,
            ALBUM_IMAGE = 3
        }
    }

    /** Properties of a MediaCounters. */
    interface IMediaCounters {

        /** MediaCounters numImages */
        numImages?: (number|null);

        /** MediaCounters numVideos */
        numVideos?: (number|null);

        /** MediaCounters numAudio */
        numAudio?: (number|null);
    }

    /** Represents a MediaCounters. */
    class MediaCounters implements IMediaCounters {

        /**
         * Constructs a new MediaCounters.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMediaCounters);

        /** MediaCounters numImages. */
        public numImages: number;

        /** MediaCounters numVideos. */
        public numVideos: number;

        /** MediaCounters numAudio. */
        public numAudio: number;

        /**
         * Creates a new MediaCounters instance using the specified properties.
         * @param [properties] Properties to set
         * @returns MediaCounters instance
         */
        public static create(properties?: server.IMediaCounters): server.MediaCounters;

        /**
         * Encodes the specified MediaCounters message. Does not implicitly {@link server.MediaCounters.verify|verify} messages.
         * @param message MediaCounters message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMediaCounters, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified MediaCounters message, length delimited. Does not implicitly {@link server.MediaCounters.verify|verify} messages.
         * @param message MediaCounters message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMediaCounters, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a MediaCounters message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns MediaCounters
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.MediaCounters;

        /**
         * Decodes a MediaCounters message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns MediaCounters
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.MediaCounters;

        /**
         * Verifies a MediaCounters message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a MediaCounters message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns MediaCounters
         */
        public static fromObject(object: { [k: string]: any }): server.MediaCounters;

        /**
         * Creates a plain object from a MediaCounters message. Also converts values to other types if specified.
         * @param message MediaCounters
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.MediaCounters, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this MediaCounters to JSON.
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

        /** Post mediaCounters */
        mediaCounters?: (server.IMediaCounters|null);

        /** Post tag */
        tag?: (server.Post.Tag|null);

        /** Post psaTag */
        psaTag?: (string|null);

        /** Post momentUnlockUid */
        momentUnlockUid?: (number|Long|null);

        /** Post showPostShareScreen */
        showPostShareScreen?: (boolean|null);

        /** Post momentInfo */
        momentInfo?: (server.IMomentInfo|null);

        /** Post isExpired */
        isExpired?: (boolean|null);
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

        /** Post mediaCounters. */
        public mediaCounters?: (server.IMediaCounters|null);

        /** Post tag. */
        public tag: server.Post.Tag;

        /** Post psaTag. */
        public psaTag: string;

        /** Post momentUnlockUid. */
        public momentUnlockUid: (number|Long);

        /** Post showPostShareScreen. */
        public showPostShareScreen: boolean;

        /** Post momentInfo. */
        public momentInfo?: (server.IMomentInfo|null);

        /** Post isExpired. */
        public isExpired: boolean;

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

    namespace Post {

        /** Tag enum. */
        enum Tag {
            EMPTY = 0,
            MOMENT = 1,
            PUBLIC_MOMENT = 2,
            PUBLIC_POST = 3
        }
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

        /** Comment mediaCounters */
        mediaCounters?: (server.IMediaCounters|null);

        /** Comment commentType */
        commentType?: (server.Comment.CommentType|null);
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

        /** Comment mediaCounters. */
        public mediaCounters?: (server.IMediaCounters|null);

        /** Comment commentType. */
        public commentType: server.Comment.CommentType;

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

    namespace Comment {

        /** CommentType enum. */
        enum CommentType {
            COMMENT = 0,
            COMMENT_REACTION = 1,
            POST_REACTION = 2
        }
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

        /** FeedItem senderStateBundles */
        senderStateBundles?: (server.ISenderStateBundle[]|null);

        /** FeedItem senderState */
        senderState?: (server.ISenderStateWithKeyInfo|null);

        /** FeedItem senderClientVersion */
        senderClientVersion?: (string|null);
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

        /** FeedItem senderStateBundles. */
        public senderStateBundles: server.ISenderStateBundle[];

        /** FeedItem senderState. */
        public senderState?: (server.ISenderStateWithKeyInfo|null);

        /** FeedItem senderClientVersion. */
        public senderClientVersion: string;

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
            SHARE = 2,
            PUBLIC_UPDATE_PUBLISH = 3,
            EXPIRE = 4,
            PUBLIC_UPDATE_RETRACT = 5
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

    /** PublicFeedContentType enum. */
    enum PublicFeedContentType {
        MOMENTS = 0,
        POSTS = 1
    }

    /** Properties of a PublicFeedRequest. */
    interface IPublicFeedRequest {

        /** PublicFeedRequest cursor */
        cursor?: (string|null);

        /** PublicFeedRequest publicFeedContentType */
        publicFeedContentType?: (server.PublicFeedContentType|null);

        /** PublicFeedRequest gpsLocation */
        gpsLocation?: (server.IGpsLocation|null);

        /** PublicFeedRequest showDevContent */
        showDevContent?: (boolean|null);
    }

    /** Represents a PublicFeedRequest. */
    class PublicFeedRequest implements IPublicFeedRequest {

        /**
         * Constructs a new PublicFeedRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPublicFeedRequest);

        /** PublicFeedRequest cursor. */
        public cursor: string;

        /** PublicFeedRequest publicFeedContentType. */
        public publicFeedContentType: server.PublicFeedContentType;

        /** PublicFeedRequest gpsLocation. */
        public gpsLocation?: (server.IGpsLocation|null);

        /** PublicFeedRequest showDevContent. */
        public showDevContent: boolean;

        /**
         * Creates a new PublicFeedRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PublicFeedRequest instance
         */
        public static create(properties?: server.IPublicFeedRequest): server.PublicFeedRequest;

        /**
         * Encodes the specified PublicFeedRequest message. Does not implicitly {@link server.PublicFeedRequest.verify|verify} messages.
         * @param message PublicFeedRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPublicFeedRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PublicFeedRequest message, length delimited. Does not implicitly {@link server.PublicFeedRequest.verify|verify} messages.
         * @param message PublicFeedRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPublicFeedRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PublicFeedRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PublicFeedRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PublicFeedRequest;

        /**
         * Decodes a PublicFeedRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PublicFeedRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PublicFeedRequest;

        /**
         * Verifies a PublicFeedRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PublicFeedRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PublicFeedRequest
         */
        public static fromObject(object: { [k: string]: any }): server.PublicFeedRequest;

        /**
         * Creates a plain object from a PublicFeedRequest message. Also converts values to other types if specified.
         * @param message PublicFeedRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PublicFeedRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PublicFeedRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a PublicFeedResponse. */
    interface IPublicFeedResponse {

        /** PublicFeedResponse result */
        result?: (server.PublicFeedResponse.Result|null);

        /** PublicFeedResponse reason */
        reason?: (server.PublicFeedResponse.Reason|null);

        /** PublicFeedResponse cursor */
        cursor?: (string|null);

        /** PublicFeedResponse publicFeedContentType */
        publicFeedContentType?: (server.PublicFeedContentType|null);

        /** PublicFeedResponse cursorRestarted */
        cursorRestarted?: (boolean|null);

        /** PublicFeedResponse items */
        items?: (server.IPublicFeedItem[]|null);

        /** PublicFeedResponse geoTags */
        geoTags?: (string[]|null);
    }

    /** Represents a PublicFeedResponse. */
    class PublicFeedResponse implements IPublicFeedResponse {

        /**
         * Constructs a new PublicFeedResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPublicFeedResponse);

        /** PublicFeedResponse result. */
        public result: server.PublicFeedResponse.Result;

        /** PublicFeedResponse reason. */
        public reason: server.PublicFeedResponse.Reason;

        /** PublicFeedResponse cursor. */
        public cursor: string;

        /** PublicFeedResponse publicFeedContentType. */
        public publicFeedContentType: server.PublicFeedContentType;

        /** PublicFeedResponse cursorRestarted. */
        public cursorRestarted: boolean;

        /** PublicFeedResponse items. */
        public items: server.IPublicFeedItem[];

        /** PublicFeedResponse geoTags. */
        public geoTags: string[];

        /**
         * Creates a new PublicFeedResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PublicFeedResponse instance
         */
        public static create(properties?: server.IPublicFeedResponse): server.PublicFeedResponse;

        /**
         * Encodes the specified PublicFeedResponse message. Does not implicitly {@link server.PublicFeedResponse.verify|verify} messages.
         * @param message PublicFeedResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPublicFeedResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PublicFeedResponse message, length delimited. Does not implicitly {@link server.PublicFeedResponse.verify|verify} messages.
         * @param message PublicFeedResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPublicFeedResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PublicFeedResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PublicFeedResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PublicFeedResponse;

        /**
         * Decodes a PublicFeedResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PublicFeedResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PublicFeedResponse;

        /**
         * Verifies a PublicFeedResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PublicFeedResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PublicFeedResponse
         */
        public static fromObject(object: { [k: string]: any }): server.PublicFeedResponse;

        /**
         * Creates a plain object from a PublicFeedResponse message. Also converts values to other types if specified.
         * @param message PublicFeedResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PublicFeedResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PublicFeedResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace PublicFeedResponse {

        /** Result enum. */
        enum Result {
            UNKNOWN = 0,
            SUCCESS = 1,
            FAILURE = 2
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            OK = 1
        }
    }

    /** Properties of a PublicFeedItem. */
    interface IPublicFeedItem {

        /** PublicFeedItem userProfile */
        userProfile?: (server.IBasicUserProfile|null);

        /** PublicFeedItem post */
        post?: (server.IPost|null);

        /** PublicFeedItem comments */
        comments?: (server.IComment[]|null);

        /** PublicFeedItem reason */
        reason?: (server.PublicFeedItem.Reason|null);

        /** PublicFeedItem score */
        score?: (server.IServerScore|null);
    }

    /** Represents a PublicFeedItem. */
    class PublicFeedItem implements IPublicFeedItem {

        /**
         * Constructs a new PublicFeedItem.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPublicFeedItem);

        /** PublicFeedItem userProfile. */
        public userProfile?: (server.IBasicUserProfile|null);

        /** PublicFeedItem post. */
        public post?: (server.IPost|null);

        /** PublicFeedItem comments. */
        public comments: server.IComment[];

        /** PublicFeedItem reason. */
        public reason: server.PublicFeedItem.Reason;

        /** PublicFeedItem score. */
        public score?: (server.IServerScore|null);

        /**
         * Creates a new PublicFeedItem instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PublicFeedItem instance
         */
        public static create(properties?: server.IPublicFeedItem): server.PublicFeedItem;

        /**
         * Encodes the specified PublicFeedItem message. Does not implicitly {@link server.PublicFeedItem.verify|verify} messages.
         * @param message PublicFeedItem message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPublicFeedItem, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PublicFeedItem message, length delimited. Does not implicitly {@link server.PublicFeedItem.verify|verify} messages.
         * @param message PublicFeedItem message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPublicFeedItem, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PublicFeedItem message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PublicFeedItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PublicFeedItem;

        /**
         * Decodes a PublicFeedItem message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PublicFeedItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PublicFeedItem;

        /**
         * Verifies a PublicFeedItem message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PublicFeedItem message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PublicFeedItem
         */
        public static fromObject(object: { [k: string]: any }): server.PublicFeedItem;

        /**
         * Creates a plain object from a PublicFeedItem message. Also converts values to other types if specified.
         * @param message PublicFeedItem
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PublicFeedItem, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PublicFeedItem to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace PublicFeedItem {

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            CAMPUS = 1,
            FOF = 2
        }
    }

    /** Properties of a ServerScore. */
    interface IServerScore {

        /** ServerScore score */
        score?: (number|Long|null);

        /** ServerScore explanation */
        explanation?: (string|null);

        /** ServerScore dscore */
        dscore?: (number|null);
    }

    /** Represents a ServerScore. */
    class ServerScore implements IServerScore {

        /**
         * Constructs a new ServerScore.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IServerScore);

        /** ServerScore score. */
        public score: (number|Long);

        /** ServerScore explanation. */
        public explanation: string;

        /** ServerScore dscore. */
        public dscore: number;

        /**
         * Creates a new ServerScore instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ServerScore instance
         */
        public static create(properties?: server.IServerScore): server.ServerScore;

        /**
         * Encodes the specified ServerScore message. Does not implicitly {@link server.ServerScore.verify|verify} messages.
         * @param message ServerScore message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IServerScore, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ServerScore message, length delimited. Does not implicitly {@link server.ServerScore.verify|verify} messages.
         * @param message ServerScore message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IServerScore, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ServerScore message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ServerScore
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ServerScore;

        /**
         * Decodes a ServerScore message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ServerScore
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ServerScore;

        /**
         * Verifies a ServerScore message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ServerScore message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ServerScore
         */
        public static fromObject(object: { [k: string]: any }): server.ServerScore;

        /**
         * Creates a plain object from a ServerScore message. Also converts values to other types if specified.
         * @param message ServerScore
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ServerScore, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ServerScore to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a PublicFeedUpdate. */
    interface IPublicFeedUpdate {

        /** PublicFeedUpdate cursor */
        cursor?: (string|null);

        /** PublicFeedUpdate publicFeedContentType */
        publicFeedContentType?: (server.PublicFeedContentType|null);

        /** PublicFeedUpdate items */
        items?: (server.IPublicFeedItem[]|null);
    }

    /** Represents a PublicFeedUpdate. */
    class PublicFeedUpdate implements IPublicFeedUpdate {

        /**
         * Constructs a new PublicFeedUpdate.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPublicFeedUpdate);

        /** PublicFeedUpdate cursor. */
        public cursor: string;

        /** PublicFeedUpdate publicFeedContentType. */
        public publicFeedContentType: server.PublicFeedContentType;

        /** PublicFeedUpdate items. */
        public items: server.IPublicFeedItem[];

        /**
         * Creates a new PublicFeedUpdate instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PublicFeedUpdate instance
         */
        public static create(properties?: server.IPublicFeedUpdate): server.PublicFeedUpdate;

        /**
         * Encodes the specified PublicFeedUpdate message. Does not implicitly {@link server.PublicFeedUpdate.verify|verify} messages.
         * @param message PublicFeedUpdate message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPublicFeedUpdate, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PublicFeedUpdate message, length delimited. Does not implicitly {@link server.PublicFeedUpdate.verify|verify} messages.
         * @param message PublicFeedUpdate message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPublicFeedUpdate, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PublicFeedUpdate message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PublicFeedUpdate
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PublicFeedUpdate;

        /**
         * Decodes a PublicFeedUpdate message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PublicFeedUpdate
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PublicFeedUpdate;

        /**
         * Verifies a PublicFeedUpdate message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PublicFeedUpdate message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PublicFeedUpdate
         */
        public static fromObject(object: { [k: string]: any }): server.PublicFeedUpdate;

        /**
         * Creates a plain object from a PublicFeedUpdate message. Also converts values to other types if specified.
         * @param message PublicFeedUpdate
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PublicFeedUpdate, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PublicFeedUpdate to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a GpsLocation. */
    interface IGpsLocation {

        /** GpsLocation latitude */
        latitude?: (number|null);

        /** GpsLocation longitude */
        longitude?: (number|null);
    }

    /** Represents a GpsLocation. */
    class GpsLocation implements IGpsLocation {

        /**
         * Constructs a new GpsLocation.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGpsLocation);

        /** GpsLocation latitude. */
        public latitude: number;

        /** GpsLocation longitude. */
        public longitude: number;

        /**
         * Creates a new GpsLocation instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GpsLocation instance
         */
        public static create(properties?: server.IGpsLocation): server.GpsLocation;

        /**
         * Encodes the specified GpsLocation message. Does not implicitly {@link server.GpsLocation.verify|verify} messages.
         * @param message GpsLocation message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGpsLocation, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GpsLocation message, length delimited. Does not implicitly {@link server.GpsLocation.verify|verify} messages.
         * @param message GpsLocation message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGpsLocation, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GpsLocation message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GpsLocation
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GpsLocation;

        /**
         * Decodes a GpsLocation message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GpsLocation
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GpsLocation;

        /**
         * Verifies a GpsLocation message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GpsLocation message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GpsLocation
         */
        public static fromObject(object: { [k: string]: any }): server.GpsLocation;

        /**
         * Creates a plain object from a GpsLocation message. Also converts values to other types if specified.
         * @param message GpsLocation
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GpsLocation, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GpsLocation to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a PostSubscriptionRequest. */
    interface IPostSubscriptionRequest {

        /** PostSubscriptionRequest action */
        action?: (server.PostSubscriptionRequest.Action|null);

        /** PostSubscriptionRequest postId */
        postId?: (string|null);
    }

    /** Represents a PostSubscriptionRequest. */
    class PostSubscriptionRequest implements IPostSubscriptionRequest {

        /**
         * Constructs a new PostSubscriptionRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPostSubscriptionRequest);

        /** PostSubscriptionRequest action. */
        public action: server.PostSubscriptionRequest.Action;

        /** PostSubscriptionRequest postId. */
        public postId: string;

        /**
         * Creates a new PostSubscriptionRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PostSubscriptionRequest instance
         */
        public static create(properties?: server.IPostSubscriptionRequest): server.PostSubscriptionRequest;

        /**
         * Encodes the specified PostSubscriptionRequest message. Does not implicitly {@link server.PostSubscriptionRequest.verify|verify} messages.
         * @param message PostSubscriptionRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPostSubscriptionRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PostSubscriptionRequest message, length delimited. Does not implicitly {@link server.PostSubscriptionRequest.verify|verify} messages.
         * @param message PostSubscriptionRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPostSubscriptionRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PostSubscriptionRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PostSubscriptionRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PostSubscriptionRequest;

        /**
         * Decodes a PostSubscriptionRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PostSubscriptionRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PostSubscriptionRequest;

        /**
         * Verifies a PostSubscriptionRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PostSubscriptionRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PostSubscriptionRequest
         */
        public static fromObject(object: { [k: string]: any }): server.PostSubscriptionRequest;

        /**
         * Creates a plain object from a PostSubscriptionRequest message. Also converts values to other types if specified.
         * @param message PostSubscriptionRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PostSubscriptionRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PostSubscriptionRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace PostSubscriptionRequest {

        /** Action enum. */
        enum Action {
            UNKNOWN_ACTION = 0,
            SUBSCRIBE = 1,
            UNSUBSCRIBE = 2
        }
    }

    /** Properties of a PostSubscriptionResponse. */
    interface IPostSubscriptionResponse {

        /** PostSubscriptionResponse result */
        result?: (server.PostSubscriptionResponse.Result|null);

        /** PostSubscriptionResponse reason */
        reason?: (server.PostSubscriptionResponse.Reason|null);

        /** PostSubscriptionResponse items */
        items?: (server.IFeedItem[]|null);
    }

    /** Represents a PostSubscriptionResponse. */
    class PostSubscriptionResponse implements IPostSubscriptionResponse {

        /**
         * Constructs a new PostSubscriptionResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPostSubscriptionResponse);

        /** PostSubscriptionResponse result. */
        public result: server.PostSubscriptionResponse.Result;

        /** PostSubscriptionResponse reason. */
        public reason: server.PostSubscriptionResponse.Reason;

        /** PostSubscriptionResponse items. */
        public items: server.IFeedItem[];

        /**
         * Creates a new PostSubscriptionResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PostSubscriptionResponse instance
         */
        public static create(properties?: server.IPostSubscriptionResponse): server.PostSubscriptionResponse;

        /**
         * Encodes the specified PostSubscriptionResponse message. Does not implicitly {@link server.PostSubscriptionResponse.verify|verify} messages.
         * @param message PostSubscriptionResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPostSubscriptionResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PostSubscriptionResponse message, length delimited. Does not implicitly {@link server.PostSubscriptionResponse.verify|verify} messages.
         * @param message PostSubscriptionResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPostSubscriptionResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PostSubscriptionResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PostSubscriptionResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PostSubscriptionResponse;

        /**
         * Decodes a PostSubscriptionResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PostSubscriptionResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PostSubscriptionResponse;

        /**
         * Verifies a PostSubscriptionResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PostSubscriptionResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PostSubscriptionResponse
         */
        public static fromObject(object: { [k: string]: any }): server.PostSubscriptionResponse;

        /**
         * Creates a plain object from a PostSubscriptionResponse message. Also converts values to other types if specified.
         * @param message PostSubscriptionResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PostSubscriptionResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PostSubscriptionResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace PostSubscriptionResponse {

        /** Result enum. */
        enum Result {
            UNKNOWN_RESULT = 0,
            SUCCESS = 1,
            FAILURE = 2
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            INVALID_POST_ID = 1
        }
    }

    /** Properties of a SenderStateWithKeyInfo. */
    interface ISenderStateWithKeyInfo {

        /** SenderStateWithKeyInfo publicKey */
        publicKey?: (Uint8Array|null);

        /** SenderStateWithKeyInfo oneTimePreKeyId */
        oneTimePreKeyId?: (number|Long|null);

        /** SenderStateWithKeyInfo encSenderState */
        encSenderState?: (Uint8Array|null);
    }

    /** Represents a SenderStateWithKeyInfo. */
    class SenderStateWithKeyInfo implements ISenderStateWithKeyInfo {

        /**
         * Constructs a new SenderStateWithKeyInfo.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISenderStateWithKeyInfo);

        /** SenderStateWithKeyInfo publicKey. */
        public publicKey: Uint8Array;

        /** SenderStateWithKeyInfo oneTimePreKeyId. */
        public oneTimePreKeyId: (number|Long);

        /** SenderStateWithKeyInfo encSenderState. */
        public encSenderState: Uint8Array;

        /**
         * Creates a new SenderStateWithKeyInfo instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SenderStateWithKeyInfo instance
         */
        public static create(properties?: server.ISenderStateWithKeyInfo): server.SenderStateWithKeyInfo;

        /**
         * Encodes the specified SenderStateWithKeyInfo message. Does not implicitly {@link server.SenderStateWithKeyInfo.verify|verify} messages.
         * @param message SenderStateWithKeyInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISenderStateWithKeyInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SenderStateWithKeyInfo message, length delimited. Does not implicitly {@link server.SenderStateWithKeyInfo.verify|verify} messages.
         * @param message SenderStateWithKeyInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISenderStateWithKeyInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SenderStateWithKeyInfo message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SenderStateWithKeyInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SenderStateWithKeyInfo;

        /**
         * Decodes a SenderStateWithKeyInfo message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SenderStateWithKeyInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SenderStateWithKeyInfo;

        /**
         * Verifies a SenderStateWithKeyInfo message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SenderStateWithKeyInfo message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SenderStateWithKeyInfo
         */
        public static fromObject(object: { [k: string]: any }): server.SenderStateWithKeyInfo;

        /**
         * Creates a plain object from a SenderStateWithKeyInfo message. Also converts values to other types if specified.
         * @param message SenderStateWithKeyInfo
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SenderStateWithKeyInfo, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SenderStateWithKeyInfo to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a SenderStateBundle. */
    interface ISenderStateBundle {

        /** SenderStateBundle senderState */
        senderState?: (server.ISenderStateWithKeyInfo|null);

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

        /** SenderStateBundle senderState. */
        public senderState?: (server.ISenderStateWithKeyInfo|null);

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

        /** GroupFeedItem senderState */
        senderState?: (server.ISenderStateWithKeyInfo|null);

        /** GroupFeedItem audienceHash */
        audienceHash?: (Uint8Array|null);

        /** GroupFeedItem isResentHistory */
        isResentHistory?: (boolean|null);

        /** GroupFeedItem expiryTimestamp */
        expiryTimestamp?: (number|Long|null);

        /** GroupFeedItem senderLogInfo */
        senderLogInfo?: (string|null);

        /** GroupFeedItem senderClientVersion */
        senderClientVersion?: (string|null);
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

        /** GroupFeedItem senderState. */
        public senderState?: (server.ISenderStateWithKeyInfo|null);

        /** GroupFeedItem audienceHash. */
        public audienceHash: Uint8Array;

        /** GroupFeedItem isResentHistory. */
        public isResentHistory: boolean;

        /** GroupFeedItem expiryTimestamp. */
        public expiryTimestamp: (number|Long);

        /** GroupFeedItem senderLogInfo. */
        public senderLogInfo: string;

        /** GroupFeedItem senderClientVersion. */
        public senderClientVersion: string;

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
            RETRACT = 1,
            SHARE = 2
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

    /** Properties of a GroupFeedHistory. */
    interface IGroupFeedHistory {

        /** GroupFeedHistory gid */
        gid?: (string|null);

        /** GroupFeedHistory id */
        id?: (string|null);

        /** GroupFeedHistory payload */
        payload?: (Uint8Array|null);

        /** GroupFeedHistory encPayload */
        encPayload?: (Uint8Array|null);

        /** GroupFeedHistory publicKey */
        publicKey?: (Uint8Array|null);

        /** GroupFeedHistory oneTimePreKeyId */
        oneTimePreKeyId?: (number|null);

        /** GroupFeedHistory senderLogInfo */
        senderLogInfo?: (string|null);

        /** GroupFeedHistory senderClientVersion */
        senderClientVersion?: (string|null);
    }

    /** Represents a GroupFeedHistory. */
    class GroupFeedHistory implements IGroupFeedHistory {

        /**
         * Constructs a new GroupFeedHistory.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupFeedHistory);

        /** GroupFeedHistory gid. */
        public gid: string;

        /** GroupFeedHistory id. */
        public id: string;

        /** GroupFeedHistory payload. */
        public payload: Uint8Array;

        /** GroupFeedHistory encPayload. */
        public encPayload: Uint8Array;

        /** GroupFeedHistory publicKey. */
        public publicKey: Uint8Array;

        /** GroupFeedHistory oneTimePreKeyId. */
        public oneTimePreKeyId: number;

        /** GroupFeedHistory senderLogInfo. */
        public senderLogInfo: string;

        /** GroupFeedHistory senderClientVersion. */
        public senderClientVersion: string;

        /**
         * Creates a new GroupFeedHistory instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupFeedHistory instance
         */
        public static create(properties?: server.IGroupFeedHistory): server.GroupFeedHistory;

        /**
         * Encodes the specified GroupFeedHistory message. Does not implicitly {@link server.GroupFeedHistory.verify|verify} messages.
         * @param message GroupFeedHistory message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupFeedHistory, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupFeedHistory message, length delimited. Does not implicitly {@link server.GroupFeedHistory.verify|verify} messages.
         * @param message GroupFeedHistory message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupFeedHistory, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupFeedHistory message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupFeedHistory
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupFeedHistory;

        /**
         * Decodes a GroupFeedHistory message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupFeedHistory
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupFeedHistory;

        /**
         * Verifies a GroupFeedHistory message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupFeedHistory message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupFeedHistory
         */
        public static fromObject(object: { [k: string]: any }): server.GroupFeedHistory;

        /**
         * Creates a plain object from a GroupFeedHistory message. Also converts values to other types if specified.
         * @param message GroupFeedHistory
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupFeedHistory, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupFeedHistory to JSON.
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

        /** GroupStanza description */
        description?: (string|null);

        /** GroupStanza historyResend */
        historyResend?: (server.IHistoryResend|null);

        /** GroupStanza expiryInfo */
        expiryInfo?: (server.IExpiryInfo|null);

        /** GroupStanza groupType */
        groupType?: (server.GroupStanza.GroupType|null);

        /** GroupStanza maxGroupSize */
        maxGroupSize?: (number|Long|null);
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

        /** GroupStanza description. */
        public description: string;

        /** GroupStanza historyResend. */
        public historyResend?: (server.IHistoryResend|null);

        /** GroupStanza expiryInfo. */
        public expiryInfo?: (server.IExpiryInfo|null);

        /** GroupStanza groupType. */
        public groupType: server.GroupStanza.GroupType;

        /** GroupStanza maxGroupSize. */
        public maxGroupSize: (number|Long);

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
            GET_MEMBER_IDENTITY_KEYS = 14,
            CHANGE_DESCRIPTION = 15,
            SHARE_HISTORY = 16,
            CHANGE_EXPIRY = 17
        }

        /** GroupType enum. */
        enum GroupType {
            FEED = 0,
            CHAT = 1
        }
    }

    /** Properties of an ExpiryInfo. */
    interface IExpiryInfo {

        /** ExpiryInfo expiryType */
        expiryType?: (server.ExpiryInfo.ExpiryType|null);

        /** ExpiryInfo expiresInSeconds */
        expiresInSeconds?: (number|Long|null);

        /** ExpiryInfo expiryTimestamp */
        expiryTimestamp?: (number|Long|null);
    }

    /** Represents an ExpiryInfo. */
    class ExpiryInfo implements IExpiryInfo {

        /**
         * Constructs a new ExpiryInfo.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IExpiryInfo);

        /** ExpiryInfo expiryType. */
        public expiryType: server.ExpiryInfo.ExpiryType;

        /** ExpiryInfo expiresInSeconds. */
        public expiresInSeconds: (number|Long);

        /** ExpiryInfo expiryTimestamp. */
        public expiryTimestamp: (number|Long);

        /**
         * Creates a new ExpiryInfo instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ExpiryInfo instance
         */
        public static create(properties?: server.IExpiryInfo): server.ExpiryInfo;

        /**
         * Encodes the specified ExpiryInfo message. Does not implicitly {@link server.ExpiryInfo.verify|verify} messages.
         * @param message ExpiryInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IExpiryInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ExpiryInfo message, length delimited. Does not implicitly {@link server.ExpiryInfo.verify|verify} messages.
         * @param message ExpiryInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IExpiryInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an ExpiryInfo message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ExpiryInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ExpiryInfo;

        /**
         * Decodes an ExpiryInfo message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ExpiryInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ExpiryInfo;

        /**
         * Verifies an ExpiryInfo message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an ExpiryInfo message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ExpiryInfo
         */
        public static fromObject(object: { [k: string]: any }): server.ExpiryInfo;

        /**
         * Creates a plain object from an ExpiryInfo message. Also converts values to other types if specified.
         * @param message ExpiryInfo
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ExpiryInfo, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ExpiryInfo to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ExpiryInfo {

        /** ExpiryType enum. */
        enum ExpiryType {
            EXPIRES_IN_SECONDS = 0,
            NEVER = 1,
            CUSTOM_DATE = 2
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

    /** Properties of a GroupChatStanza. */
    interface IGroupChatStanza {

        /** GroupChatStanza gid */
        gid?: (string|null);

        /** GroupChatStanza name */
        name?: (string|null);

        /** GroupChatStanza avatarId */
        avatarId?: (string|null);

        /** GroupChatStanza senderPhone */
        senderPhone?: (string|null);

        /** GroupChatStanza senderName */
        senderName?: (string|null);

        /** GroupChatStanza timestamp */
        timestamp?: (number|Long|null);

        /** GroupChatStanza payload */
        payload?: (Uint8Array|null);

        /** GroupChatStanza encPayload */
        encPayload?: (Uint8Array|null);

        /** GroupChatStanza senderStateBundles */
        senderStateBundles?: (server.ISenderStateBundle[]|null);

        /** GroupChatStanza senderState */
        senderState?: (server.ISenderStateWithKeyInfo|null);

        /** GroupChatStanza audienceHash */
        audienceHash?: (Uint8Array|null);

        /** GroupChatStanza mediaCounters */
        mediaCounters?: (server.IMediaCounters|null);

        /** GroupChatStanza chatType */
        chatType?: (server.GroupChatStanza.ChatType|null);

        /** GroupChatStanza senderLogInfo */
        senderLogInfo?: (string|null);

        /** GroupChatStanza senderClientVersion */
        senderClientVersion?: (string|null);
    }

    /** Represents a GroupChatStanza. */
    class GroupChatStanza implements IGroupChatStanza {

        /**
         * Constructs a new GroupChatStanza.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupChatStanza);

        /** GroupChatStanza gid. */
        public gid: string;

        /** GroupChatStanza name. */
        public name: string;

        /** GroupChatStanza avatarId. */
        public avatarId: string;

        /** GroupChatStanza senderPhone. */
        public senderPhone: string;

        /** GroupChatStanza senderName. */
        public senderName: string;

        /** GroupChatStanza timestamp. */
        public timestamp: (number|Long);

        /** GroupChatStanza payload. */
        public payload: Uint8Array;

        /** GroupChatStanza encPayload. */
        public encPayload: Uint8Array;

        /** GroupChatStanza senderStateBundles. */
        public senderStateBundles: server.ISenderStateBundle[];

        /** GroupChatStanza senderState. */
        public senderState?: (server.ISenderStateWithKeyInfo|null);

        /** GroupChatStanza audienceHash. */
        public audienceHash: Uint8Array;

        /** GroupChatStanza mediaCounters. */
        public mediaCounters?: (server.IMediaCounters|null);

        /** GroupChatStanza chatType. */
        public chatType: server.GroupChatStanza.ChatType;

        /** GroupChatStanza senderLogInfo. */
        public senderLogInfo: string;

        /** GroupChatStanza senderClientVersion. */
        public senderClientVersion: string;

        /**
         * Creates a new GroupChatStanza instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupChatStanza instance
         */
        public static create(properties?: server.IGroupChatStanza): server.GroupChatStanza;

        /**
         * Encodes the specified GroupChatStanza message. Does not implicitly {@link server.GroupChatStanza.verify|verify} messages.
         * @param message GroupChatStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupChatStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupChatStanza message, length delimited. Does not implicitly {@link server.GroupChatStanza.verify|verify} messages.
         * @param message GroupChatStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupChatStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupChatStanza message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupChatStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupChatStanza;

        /**
         * Decodes a GroupChatStanza message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupChatStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupChatStanza;

        /**
         * Verifies a GroupChatStanza message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupChatStanza message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupChatStanza
         */
        public static fromObject(object: { [k: string]: any }): server.GroupChatStanza;

        /**
         * Creates a plain object from a GroupChatStanza message. Also converts values to other types if specified.
         * @param message GroupChatStanza
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupChatStanza, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupChatStanza to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GroupChatStanza {

        /** ChatType enum. */
        enum ChatType {
            CHAT = 0,
            CHAT_REACTION = 1
        }
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

        /** AuthRequest deviceInfo */
        deviceInfo?: (server.IDeviceInfo|null);
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

        /** AuthRequest deviceInfo. */
        public deviceInfo?: (server.IDeviceInfo|null);

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

        /** ChatStanza mediaCounters */
        mediaCounters?: (server.IMediaCounters|null);

        /** ChatStanza chatType */
        chatType?: (server.ChatStanza.ChatType|null);

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

        /** ChatStanza mediaCounters. */
        public mediaCounters?: (server.IMediaCounters|null);

        /** ChatStanza chatType. */
        public chatType: server.ChatStanza.ChatType;

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

    namespace ChatStanza {

        /** ChatType enum. */
        enum ChatType {
            CHAT = 0,
            CHAT_REACTION = 1
        }
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

        /** EndOfQueue trimmed */
        trimmed?: (boolean|null);
    }

    /** Represents an EndOfQueue. */
    class EndOfQueue implements IEndOfQueue {

        /**
         * Constructs a new EndOfQueue.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IEndOfQueue);

        /** EndOfQueue trimmed. */
        public trimmed: boolean;

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

    /** Properties of a HistoryResend. */
    interface IHistoryResend {

        /** HistoryResend gid */
        gid?: (string|null);

        /** HistoryResend id */
        id?: (string|null);

        /** HistoryResend payload */
        payload?: (Uint8Array|null);

        /** HistoryResend encPayload */
        encPayload?: (Uint8Array|null);

        /** HistoryResend senderStateBundles */
        senderStateBundles?: (server.ISenderStateBundle[]|null);

        /** HistoryResend senderState */
        senderState?: (server.ISenderStateWithKeyInfo|null);

        /** HistoryResend audienceHash */
        audienceHash?: (Uint8Array|null);

        /** HistoryResend senderLogInfo */
        senderLogInfo?: (string|null);

        /** HistoryResend senderClientVersion */
        senderClientVersion?: (string|null);
    }

    /** Represents a HistoryResend. */
    class HistoryResend implements IHistoryResend {

        /**
         * Constructs a new HistoryResend.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IHistoryResend);

        /** HistoryResend gid. */
        public gid: string;

        /** HistoryResend id. */
        public id: string;

        /** HistoryResend payload. */
        public payload: Uint8Array;

        /** HistoryResend encPayload. */
        public encPayload: Uint8Array;

        /** HistoryResend senderStateBundles. */
        public senderStateBundles: server.ISenderStateBundle[];

        /** HistoryResend senderState. */
        public senderState?: (server.ISenderStateWithKeyInfo|null);

        /** HistoryResend audienceHash. */
        public audienceHash: Uint8Array;

        /** HistoryResend senderLogInfo. */
        public senderLogInfo: string;

        /** HistoryResend senderClientVersion. */
        public senderClientVersion: string;

        /**
         * Creates a new HistoryResend instance using the specified properties.
         * @param [properties] Properties to set
         * @returns HistoryResend instance
         */
        public static create(properties?: server.IHistoryResend): server.HistoryResend;

        /**
         * Encodes the specified HistoryResend message. Does not implicitly {@link server.HistoryResend.verify|verify} messages.
         * @param message HistoryResend message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IHistoryResend, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified HistoryResend message, length delimited. Does not implicitly {@link server.HistoryResend.verify|verify} messages.
         * @param message HistoryResend message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IHistoryResend, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a HistoryResend message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns HistoryResend
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.HistoryResend;

        /**
         * Decodes a HistoryResend message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns HistoryResend
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.HistoryResend;

        /**
         * Verifies a HistoryResend message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a HistoryResend message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns HistoryResend
         */
        public static fromObject(object: { [k: string]: any }): server.HistoryResend;

        /**
         * Creates a plain object from a HistoryResend message. Also converts values to other types if specified.
         * @param message HistoryResend
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.HistoryResend, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this HistoryResend to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** CallType enum. */
    enum CallType {
        UNKNOWN_TYPE = 0,
        AUDIO = 1,
        VIDEO = 2
    }

    /** Properties of a StunServer. */
    interface IStunServer {

        /** StunServer host */
        host?: (string|null);

        /** StunServer port */
        port?: (number|null);
    }

    /** Represents a StunServer. */
    class StunServer implements IStunServer {

        /**
         * Constructs a new StunServer.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IStunServer);

        /** StunServer host. */
        public host: string;

        /** StunServer port. */
        public port: number;

        /**
         * Creates a new StunServer instance using the specified properties.
         * @param [properties] Properties to set
         * @returns StunServer instance
         */
        public static create(properties?: server.IStunServer): server.StunServer;

        /**
         * Encodes the specified StunServer message. Does not implicitly {@link server.StunServer.verify|verify} messages.
         * @param message StunServer message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IStunServer, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified StunServer message, length delimited. Does not implicitly {@link server.StunServer.verify|verify} messages.
         * @param message StunServer message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IStunServer, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a StunServer message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns StunServer
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.StunServer;

        /**
         * Decodes a StunServer message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns StunServer
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.StunServer;

        /**
         * Verifies a StunServer message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a StunServer message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns StunServer
         */
        public static fromObject(object: { [k: string]: any }): server.StunServer;

        /**
         * Creates a plain object from a StunServer message. Also converts values to other types if specified.
         * @param message StunServer
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.StunServer, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this StunServer to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a TurnServer. */
    interface ITurnServer {

        /** TurnServer host */
        host?: (string|null);

        /** TurnServer port */
        port?: (number|null);

        /** TurnServer username */
        username?: (string|null);

        /** TurnServer password */
        password?: (string|null);
    }

    /** Represents a TurnServer. */
    class TurnServer implements ITurnServer {

        /**
         * Constructs a new TurnServer.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ITurnServer);

        /** TurnServer host. */
        public host: string;

        /** TurnServer port. */
        public port: number;

        /** TurnServer username. */
        public username: string;

        /** TurnServer password. */
        public password: string;

        /**
         * Creates a new TurnServer instance using the specified properties.
         * @param [properties] Properties to set
         * @returns TurnServer instance
         */
        public static create(properties?: server.ITurnServer): server.TurnServer;

        /**
         * Encodes the specified TurnServer message. Does not implicitly {@link server.TurnServer.verify|verify} messages.
         * @param message TurnServer message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ITurnServer, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified TurnServer message, length delimited. Does not implicitly {@link server.TurnServer.verify|verify} messages.
         * @param message TurnServer message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ITurnServer, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a TurnServer message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns TurnServer
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.TurnServer;

        /**
         * Decodes a TurnServer message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns TurnServer
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.TurnServer;

        /**
         * Verifies a TurnServer message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a TurnServer message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns TurnServer
         */
        public static fromObject(object: { [k: string]: any }): server.TurnServer;

        /**
         * Creates a plain object from a TurnServer message. Also converts values to other types if specified.
         * @param message TurnServer
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.TurnServer, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this TurnServer to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a GetCallServers. */
    interface IGetCallServers {

        /** GetCallServers callId */
        callId?: (string|null);

        /** GetCallServers peerUid */
        peerUid?: (number|Long|null);

        /** GetCallServers callType */
        callType?: (server.CallType|null);
    }

    /** Represents a GetCallServers. */
    class GetCallServers implements IGetCallServers {

        /**
         * Constructs a new GetCallServers.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGetCallServers);

        /** GetCallServers callId. */
        public callId: string;

        /** GetCallServers peerUid. */
        public peerUid: (number|Long);

        /** GetCallServers callType. */
        public callType: server.CallType;

        /**
         * Creates a new GetCallServers instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GetCallServers instance
         */
        public static create(properties?: server.IGetCallServers): server.GetCallServers;

        /**
         * Encodes the specified GetCallServers message. Does not implicitly {@link server.GetCallServers.verify|verify} messages.
         * @param message GetCallServers message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGetCallServers, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GetCallServers message, length delimited. Does not implicitly {@link server.GetCallServers.verify|verify} messages.
         * @param message GetCallServers message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGetCallServers, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GetCallServers message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GetCallServers
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GetCallServers;

        /**
         * Decodes a GetCallServers message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GetCallServers
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GetCallServers;

        /**
         * Verifies a GetCallServers message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GetCallServers message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GetCallServers
         */
        public static fromObject(object: { [k: string]: any }): server.GetCallServers;

        /**
         * Creates a plain object from a GetCallServers message. Also converts values to other types if specified.
         * @param message GetCallServers
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GetCallServers, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GetCallServers to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a GetCallServersResult. */
    interface IGetCallServersResult {

        /** GetCallServersResult result */
        result?: (server.GetCallServersResult.Result|null);

        /** GetCallServersResult stunServers */
        stunServers?: (server.IStunServer[]|null);

        /** GetCallServersResult turnServers */
        turnServers?: (server.ITurnServer[]|null);

        /** GetCallServersResult callConfig */
        callConfig?: (server.ICallConfig|null);

        /** GetCallServersResult callId */
        callId?: (string|null);
    }

    /** Represents a GetCallServersResult. */
    class GetCallServersResult implements IGetCallServersResult {

        /**
         * Constructs a new GetCallServersResult.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGetCallServersResult);

        /** GetCallServersResult result. */
        public result: server.GetCallServersResult.Result;

        /** GetCallServersResult stunServers. */
        public stunServers: server.IStunServer[];

        /** GetCallServersResult turnServers. */
        public turnServers: server.ITurnServer[];

        /** GetCallServersResult callConfig. */
        public callConfig?: (server.ICallConfig|null);

        /** GetCallServersResult callId. */
        public callId: string;

        /**
         * Creates a new GetCallServersResult instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GetCallServersResult instance
         */
        public static create(properties?: server.IGetCallServersResult): server.GetCallServersResult;

        /**
         * Encodes the specified GetCallServersResult message. Does not implicitly {@link server.GetCallServersResult.verify|verify} messages.
         * @param message GetCallServersResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGetCallServersResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GetCallServersResult message, length delimited. Does not implicitly {@link server.GetCallServersResult.verify|verify} messages.
         * @param message GetCallServersResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGetCallServersResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GetCallServersResult message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GetCallServersResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GetCallServersResult;

        /**
         * Decodes a GetCallServersResult message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GetCallServersResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GetCallServersResult;

        /**
         * Verifies a GetCallServersResult message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GetCallServersResult message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GetCallServersResult
         */
        public static fromObject(object: { [k: string]: any }): server.GetCallServersResult;

        /**
         * Creates a plain object from a GetCallServersResult message. Also converts values to other types if specified.
         * @param message GetCallServersResult
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GetCallServersResult, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GetCallServersResult to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GetCallServersResult {

        /** Result enum. */
        enum Result {
            UNKNOWN = 0,
            OK = 1,
            FAIL = 2
        }
    }

    /** Properties of a CallCapabilities. */
    interface ICallCapabilities {

        /** CallCapabilities preAnswer */
        preAnswer?: (boolean|null);

        /** CallCapabilities sdpRestart */
        sdpRestart?: (boolean|null);
    }

    /** Represents a CallCapabilities. */
    class CallCapabilities implements ICallCapabilities {

        /**
         * Constructs a new CallCapabilities.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ICallCapabilities);

        /** CallCapabilities preAnswer. */
        public preAnswer: boolean;

        /** CallCapabilities sdpRestart. */
        public sdpRestart: boolean;

        /**
         * Creates a new CallCapabilities instance using the specified properties.
         * @param [properties] Properties to set
         * @returns CallCapabilities instance
         */
        public static create(properties?: server.ICallCapabilities): server.CallCapabilities;

        /**
         * Encodes the specified CallCapabilities message. Does not implicitly {@link server.CallCapabilities.verify|verify} messages.
         * @param message CallCapabilities message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ICallCapabilities, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified CallCapabilities message, length delimited. Does not implicitly {@link server.CallCapabilities.verify|verify} messages.
         * @param message CallCapabilities message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ICallCapabilities, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a CallCapabilities message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns CallCapabilities
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.CallCapabilities;

        /**
         * Decodes a CallCapabilities message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns CallCapabilities
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.CallCapabilities;

        /**
         * Verifies a CallCapabilities message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a CallCapabilities message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns CallCapabilities
         */
        public static fromObject(object: { [k: string]: any }): server.CallCapabilities;

        /**
         * Creates a plain object from a CallCapabilities message. Also converts values to other types if specified.
         * @param message CallCapabilities
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.CallCapabilities, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this CallCapabilities to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a StartCall. */
    interface IStartCall {

        /** StartCall callId */
        callId?: (string|null);

        /** StartCall peerUid */
        peerUid?: (number|Long|null);

        /** StartCall callType */
        callType?: (server.CallType|null);

        /** StartCall webrtcOffer */
        webrtcOffer?: (server.IWebRtcSessionDescription|null);

        /** StartCall rerequestCount */
        rerequestCount?: (number|null);

        /** StartCall callCapabilities */
        callCapabilities?: (server.ICallCapabilities|null);
    }

    /** Represents a StartCall. */
    class StartCall implements IStartCall {

        /**
         * Constructs a new StartCall.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IStartCall);

        /** StartCall callId. */
        public callId: string;

        /** StartCall peerUid. */
        public peerUid: (number|Long);

        /** StartCall callType. */
        public callType: server.CallType;

        /** StartCall webrtcOffer. */
        public webrtcOffer?: (server.IWebRtcSessionDescription|null);

        /** StartCall rerequestCount. */
        public rerequestCount: number;

        /** StartCall callCapabilities. */
        public callCapabilities?: (server.ICallCapabilities|null);

        /**
         * Creates a new StartCall instance using the specified properties.
         * @param [properties] Properties to set
         * @returns StartCall instance
         */
        public static create(properties?: server.IStartCall): server.StartCall;

        /**
         * Encodes the specified StartCall message. Does not implicitly {@link server.StartCall.verify|verify} messages.
         * @param message StartCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IStartCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified StartCall message, length delimited. Does not implicitly {@link server.StartCall.verify|verify} messages.
         * @param message StartCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IStartCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a StartCall message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns StartCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.StartCall;

        /**
         * Decodes a StartCall message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns StartCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.StartCall;

        /**
         * Verifies a StartCall message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a StartCall message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns StartCall
         */
        public static fromObject(object: { [k: string]: any }): server.StartCall;

        /**
         * Creates a plain object from a StartCall message. Also converts values to other types if specified.
         * @param message StartCall
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.StartCall, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this StartCall to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a StartCallResult. */
    interface IStartCallResult {

        /** StartCallResult result */
        result?: (server.StartCallResult.Result|null);

        /** StartCallResult stunServers */
        stunServers?: (server.IStunServer[]|null);

        /** StartCallResult turnServers */
        turnServers?: (server.ITurnServer[]|null);

        /** StartCallResult timestampMs */
        timestampMs?: (number|Long|null);
    }

    /** Represents a StartCallResult. */
    class StartCallResult implements IStartCallResult {

        /**
         * Constructs a new StartCallResult.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IStartCallResult);

        /** StartCallResult result. */
        public result: server.StartCallResult.Result;

        /** StartCallResult stunServers. */
        public stunServers: server.IStunServer[];

        /** StartCallResult turnServers. */
        public turnServers: server.ITurnServer[];

        /** StartCallResult timestampMs. */
        public timestampMs: (number|Long);

        /**
         * Creates a new StartCallResult instance using the specified properties.
         * @param [properties] Properties to set
         * @returns StartCallResult instance
         */
        public static create(properties?: server.IStartCallResult): server.StartCallResult;

        /**
         * Encodes the specified StartCallResult message. Does not implicitly {@link server.StartCallResult.verify|verify} messages.
         * @param message StartCallResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IStartCallResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified StartCallResult message, length delimited. Does not implicitly {@link server.StartCallResult.verify|verify} messages.
         * @param message StartCallResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IStartCallResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a StartCallResult message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns StartCallResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.StartCallResult;

        /**
         * Decodes a StartCallResult message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns StartCallResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.StartCallResult;

        /**
         * Verifies a StartCallResult message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a StartCallResult message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns StartCallResult
         */
        public static fromObject(object: { [k: string]: any }): server.StartCallResult;

        /**
         * Creates a plain object from a StartCallResult message. Also converts values to other types if specified.
         * @param message StartCallResult
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.StartCallResult, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this StartCallResult to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace StartCallResult {

        /** Result enum. */
        enum Result {
            UNKNOWN = 0,
            OK = 1,
            FAIL = 2
        }
    }

    /** Properties of a WebRtcSessionDescription. */
    interface IWebRtcSessionDescription {

        /** WebRtcSessionDescription encPayload */
        encPayload?: (Uint8Array|null);

        /** WebRtcSessionDescription publicKey */
        publicKey?: (Uint8Array|null);

        /** WebRtcSessionDescription oneTimePreKeyId */
        oneTimePreKeyId?: (number|null);
    }

    /** Represents a WebRtcSessionDescription. */
    class WebRtcSessionDescription implements IWebRtcSessionDescription {

        /**
         * Constructs a new WebRtcSessionDescription.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IWebRtcSessionDescription);

        /** WebRtcSessionDescription encPayload. */
        public encPayload: Uint8Array;

        /** WebRtcSessionDescription publicKey. */
        public publicKey: Uint8Array;

        /** WebRtcSessionDescription oneTimePreKeyId. */
        public oneTimePreKeyId: number;

        /**
         * Creates a new WebRtcSessionDescription instance using the specified properties.
         * @param [properties] Properties to set
         * @returns WebRtcSessionDescription instance
         */
        public static create(properties?: server.IWebRtcSessionDescription): server.WebRtcSessionDescription;

        /**
         * Encodes the specified WebRtcSessionDescription message. Does not implicitly {@link server.WebRtcSessionDescription.verify|verify} messages.
         * @param message WebRtcSessionDescription message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IWebRtcSessionDescription, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified WebRtcSessionDescription message, length delimited. Does not implicitly {@link server.WebRtcSessionDescription.verify|verify} messages.
         * @param message WebRtcSessionDescription message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IWebRtcSessionDescription, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a WebRtcSessionDescription message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns WebRtcSessionDescription
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.WebRtcSessionDescription;

        /**
         * Decodes a WebRtcSessionDescription message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns WebRtcSessionDescription
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.WebRtcSessionDescription;

        /**
         * Verifies a WebRtcSessionDescription message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a WebRtcSessionDescription message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns WebRtcSessionDescription
         */
        public static fromObject(object: { [k: string]: any }): server.WebRtcSessionDescription;

        /**
         * Creates a plain object from a WebRtcSessionDescription message. Also converts values to other types if specified.
         * @param message WebRtcSessionDescription
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.WebRtcSessionDescription, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this WebRtcSessionDescription to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an IncomingCallPush. */
    interface IIncomingCallPush {

        /** IncomingCallPush callId */
        callId?: (string|null);

        /** IncomingCallPush callType */
        callType?: (server.CallType|null);

        /** IncomingCallPush stunServers */
        stunServers?: (server.IStunServer[]|null);

        /** IncomingCallPush turnServers */
        turnServers?: (server.ITurnServer[]|null);

        /** IncomingCallPush timestampMs */
        timestampMs?: (number|Long|null);

        /** IncomingCallPush callConfig */
        callConfig?: (server.ICallConfig|null);

        /** IncomingCallPush callCapabilities */
        callCapabilities?: (server.ICallCapabilities|null);
    }

    /** Represents an IncomingCallPush. */
    class IncomingCallPush implements IIncomingCallPush {

        /**
         * Constructs a new IncomingCallPush.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IIncomingCallPush);

        /** IncomingCallPush callId. */
        public callId: string;

        /** IncomingCallPush callType. */
        public callType: server.CallType;

        /** IncomingCallPush stunServers. */
        public stunServers: server.IStunServer[];

        /** IncomingCallPush turnServers. */
        public turnServers: server.ITurnServer[];

        /** IncomingCallPush timestampMs. */
        public timestampMs: (number|Long);

        /** IncomingCallPush callConfig. */
        public callConfig?: (server.ICallConfig|null);

        /** IncomingCallPush callCapabilities. */
        public callCapabilities?: (server.ICallCapabilities|null);

        /**
         * Creates a new IncomingCallPush instance using the specified properties.
         * @param [properties] Properties to set
         * @returns IncomingCallPush instance
         */
        public static create(properties?: server.IIncomingCallPush): server.IncomingCallPush;

        /**
         * Encodes the specified IncomingCallPush message. Does not implicitly {@link server.IncomingCallPush.verify|verify} messages.
         * @param message IncomingCallPush message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IIncomingCallPush, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified IncomingCallPush message, length delimited. Does not implicitly {@link server.IncomingCallPush.verify|verify} messages.
         * @param message IncomingCallPush message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IIncomingCallPush, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an IncomingCallPush message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns IncomingCallPush
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.IncomingCallPush;

        /**
         * Decodes an IncomingCallPush message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns IncomingCallPush
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.IncomingCallPush;

        /**
         * Verifies an IncomingCallPush message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an IncomingCallPush message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns IncomingCallPush
         */
        public static fromObject(object: { [k: string]: any }): server.IncomingCallPush;

        /**
         * Creates a plain object from an IncomingCallPush message. Also converts values to other types if specified.
         * @param message IncomingCallPush
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.IncomingCallPush, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this IncomingCallPush to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an IncomingCall. */
    interface IIncomingCall {

        /** IncomingCall callId */
        callId?: (string|null);

        /** IncomingCall callType */
        callType?: (server.CallType|null);

        /** IncomingCall webrtcOffer */
        webrtcOffer?: (server.IWebRtcSessionDescription|null);

        /** IncomingCall stunServers */
        stunServers?: (server.IStunServer[]|null);

        /** IncomingCall turnServers */
        turnServers?: (server.ITurnServer[]|null);

        /** IncomingCall timestampMs */
        timestampMs?: (number|Long|null);

        /** IncomingCall serverSentTsMs */
        serverSentTsMs?: (number|Long|null);

        /** IncomingCall callConfig */
        callConfig?: (server.ICallConfig|null);

        /** IncomingCall callCapabilities */
        callCapabilities?: (server.ICallCapabilities|null);
    }

    /** Represents an IncomingCall. */
    class IncomingCall implements IIncomingCall {

        /**
         * Constructs a new IncomingCall.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IIncomingCall);

        /** IncomingCall callId. */
        public callId: string;

        /** IncomingCall callType. */
        public callType: server.CallType;

        /** IncomingCall webrtcOffer. */
        public webrtcOffer?: (server.IWebRtcSessionDescription|null);

        /** IncomingCall stunServers. */
        public stunServers: server.IStunServer[];

        /** IncomingCall turnServers. */
        public turnServers: server.ITurnServer[];

        /** IncomingCall timestampMs. */
        public timestampMs: (number|Long);

        /** IncomingCall serverSentTsMs. */
        public serverSentTsMs: (number|Long);

        /** IncomingCall callConfig. */
        public callConfig?: (server.ICallConfig|null);

        /** IncomingCall callCapabilities. */
        public callCapabilities?: (server.ICallCapabilities|null);

        /**
         * Creates a new IncomingCall instance using the specified properties.
         * @param [properties] Properties to set
         * @returns IncomingCall instance
         */
        public static create(properties?: server.IIncomingCall): server.IncomingCall;

        /**
         * Encodes the specified IncomingCall message. Does not implicitly {@link server.IncomingCall.verify|verify} messages.
         * @param message IncomingCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IIncomingCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified IncomingCall message, length delimited. Does not implicitly {@link server.IncomingCall.verify|verify} messages.
         * @param message IncomingCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IIncomingCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an IncomingCall message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns IncomingCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.IncomingCall;

        /**
         * Decodes an IncomingCall message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns IncomingCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.IncomingCall;

        /**
         * Verifies an IncomingCall message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an IncomingCall message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns IncomingCall
         */
        public static fromObject(object: { [k: string]: any }): server.IncomingCall;

        /**
         * Creates a plain object from an IncomingCall message. Also converts values to other types if specified.
         * @param message IncomingCall
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.IncomingCall, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this IncomingCall to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an IceCandidate. */
    interface IIceCandidate {

        /** IceCandidate callId */
        callId?: (string|null);

        /** IceCandidate sdpMediaId */
        sdpMediaId?: (string|null);

        /** IceCandidate sdpMediaLineIndex */
        sdpMediaLineIndex?: (number|null);

        /** IceCandidate sdp */
        sdp?: (string|null);
    }

    /** Represents an IceCandidate. */
    class IceCandidate implements IIceCandidate {

        /**
         * Constructs a new IceCandidate.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IIceCandidate);

        /** IceCandidate callId. */
        public callId: string;

        /** IceCandidate sdpMediaId. */
        public sdpMediaId: string;

        /** IceCandidate sdpMediaLineIndex. */
        public sdpMediaLineIndex: number;

        /** IceCandidate sdp. */
        public sdp: string;

        /**
         * Creates a new IceCandidate instance using the specified properties.
         * @param [properties] Properties to set
         * @returns IceCandidate instance
         */
        public static create(properties?: server.IIceCandidate): server.IceCandidate;

        /**
         * Encodes the specified IceCandidate message. Does not implicitly {@link server.IceCandidate.verify|verify} messages.
         * @param message IceCandidate message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IIceCandidate, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified IceCandidate message, length delimited. Does not implicitly {@link server.IceCandidate.verify|verify} messages.
         * @param message IceCandidate message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IIceCandidate, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an IceCandidate message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns IceCandidate
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.IceCandidate;

        /**
         * Decodes an IceCandidate message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns IceCandidate
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.IceCandidate;

        /**
         * Verifies an IceCandidate message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an IceCandidate message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns IceCandidate
         */
        public static fromObject(object: { [k: string]: any }): server.IceCandidate;

        /**
         * Creates a plain object from an IceCandidate message. Also converts values to other types if specified.
         * @param message IceCandidate
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.IceCandidate, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this IceCandidate to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a CallRinging. */
    interface ICallRinging {

        /** CallRinging callId */
        callId?: (string|null);

        /** CallRinging timestampMs */
        timestampMs?: (number|Long|null);

        /** CallRinging webrtcAnswer */
        webrtcAnswer?: (server.IWebRtcSessionDescription|null);
    }

    /** Represents a CallRinging. */
    class CallRinging implements ICallRinging {

        /**
         * Constructs a new CallRinging.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ICallRinging);

        /** CallRinging callId. */
        public callId: string;

        /** CallRinging timestampMs. */
        public timestampMs: (number|Long);

        /** CallRinging webrtcAnswer. */
        public webrtcAnswer?: (server.IWebRtcSessionDescription|null);

        /**
         * Creates a new CallRinging instance using the specified properties.
         * @param [properties] Properties to set
         * @returns CallRinging instance
         */
        public static create(properties?: server.ICallRinging): server.CallRinging;

        /**
         * Encodes the specified CallRinging message. Does not implicitly {@link server.CallRinging.verify|verify} messages.
         * @param message CallRinging message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ICallRinging, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified CallRinging message, length delimited. Does not implicitly {@link server.CallRinging.verify|verify} messages.
         * @param message CallRinging message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ICallRinging, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a CallRinging message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns CallRinging
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.CallRinging;

        /**
         * Decodes a CallRinging message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns CallRinging
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.CallRinging;

        /**
         * Verifies a CallRinging message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a CallRinging message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns CallRinging
         */
        public static fromObject(object: { [k: string]: any }): server.CallRinging;

        /**
         * Creates a plain object from a CallRinging message. Also converts values to other types if specified.
         * @param message CallRinging
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.CallRinging, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this CallRinging to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a PreAnswerCall. */
    interface IPreAnswerCall {

        /** PreAnswerCall callId */
        callId?: (string|null);

        /** PreAnswerCall webrtcAnswer */
        webrtcAnswer?: (server.IWebRtcSessionDescription|null);

        /** PreAnswerCall timestampMs */
        timestampMs?: (number|Long|null);
    }

    /** Represents a PreAnswerCall. */
    class PreAnswerCall implements IPreAnswerCall {

        /**
         * Constructs a new PreAnswerCall.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPreAnswerCall);

        /** PreAnswerCall callId. */
        public callId: string;

        /** PreAnswerCall webrtcAnswer. */
        public webrtcAnswer?: (server.IWebRtcSessionDescription|null);

        /** PreAnswerCall timestampMs. */
        public timestampMs: (number|Long);

        /**
         * Creates a new PreAnswerCall instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PreAnswerCall instance
         */
        public static create(properties?: server.IPreAnswerCall): server.PreAnswerCall;

        /**
         * Encodes the specified PreAnswerCall message. Does not implicitly {@link server.PreAnswerCall.verify|verify} messages.
         * @param message PreAnswerCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPreAnswerCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PreAnswerCall message, length delimited. Does not implicitly {@link server.PreAnswerCall.verify|verify} messages.
         * @param message PreAnswerCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPreAnswerCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PreAnswerCall message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PreAnswerCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PreAnswerCall;

        /**
         * Decodes a PreAnswerCall message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PreAnswerCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PreAnswerCall;

        /**
         * Verifies a PreAnswerCall message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PreAnswerCall message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PreAnswerCall
         */
        public static fromObject(object: { [k: string]: any }): server.PreAnswerCall;

        /**
         * Creates a plain object from a PreAnswerCall message. Also converts values to other types if specified.
         * @param message PreAnswerCall
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PreAnswerCall, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PreAnswerCall to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an AnswerCall. */
    interface IAnswerCall {

        /** AnswerCall callId */
        callId?: (string|null);

        /** AnswerCall webrtcAnswer */
        webrtcAnswer?: (server.IWebRtcSessionDescription|null);

        /** AnswerCall timestampMs */
        timestampMs?: (number|Long|null);

        /** AnswerCall webrtcOffer */
        webrtcOffer?: (server.IWebRtcSessionDescription|null);
    }

    /** Represents an AnswerCall. */
    class AnswerCall implements IAnswerCall {

        /**
         * Constructs a new AnswerCall.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAnswerCall);

        /** AnswerCall callId. */
        public callId: string;

        /** AnswerCall webrtcAnswer. */
        public webrtcAnswer?: (server.IWebRtcSessionDescription|null);

        /** AnswerCall timestampMs. */
        public timestampMs: (number|Long);

        /** AnswerCall webrtcOffer. */
        public webrtcOffer?: (server.IWebRtcSessionDescription|null);

        /**
         * Creates a new AnswerCall instance using the specified properties.
         * @param [properties] Properties to set
         * @returns AnswerCall instance
         */
        public static create(properties?: server.IAnswerCall): server.AnswerCall;

        /**
         * Encodes the specified AnswerCall message. Does not implicitly {@link server.AnswerCall.verify|verify} messages.
         * @param message AnswerCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAnswerCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified AnswerCall message, length delimited. Does not implicitly {@link server.AnswerCall.verify|verify} messages.
         * @param message AnswerCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAnswerCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an AnswerCall message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns AnswerCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.AnswerCall;

        /**
         * Decodes an AnswerCall message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns AnswerCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.AnswerCall;

        /**
         * Verifies an AnswerCall message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an AnswerCall message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns AnswerCall
         */
        public static fromObject(object: { [k: string]: any }): server.AnswerCall;

        /**
         * Creates a plain object from an AnswerCall message. Also converts values to other types if specified.
         * @param message AnswerCall
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.AnswerCall, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this AnswerCall to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a CallSdp. */
    interface ICallSdp {

        /** CallSdp callId */
        callId?: (string|null);

        /** CallSdp sdpType */
        sdpType?: (server.CallSdp.SdpType|null);

        /** CallSdp info */
        info?: (server.IWebRtcSessionDescription|null);

        /** CallSdp timestampMs */
        timestampMs?: (number|Long|null);
    }

    /** Represents a CallSdp. */
    class CallSdp implements ICallSdp {

        /**
         * Constructs a new CallSdp.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ICallSdp);

        /** CallSdp callId. */
        public callId: string;

        /** CallSdp sdpType. */
        public sdpType: server.CallSdp.SdpType;

        /** CallSdp info. */
        public info?: (server.IWebRtcSessionDescription|null);

        /** CallSdp timestampMs. */
        public timestampMs: (number|Long);

        /**
         * Creates a new CallSdp instance using the specified properties.
         * @param [properties] Properties to set
         * @returns CallSdp instance
         */
        public static create(properties?: server.ICallSdp): server.CallSdp;

        /**
         * Encodes the specified CallSdp message. Does not implicitly {@link server.CallSdp.verify|verify} messages.
         * @param message CallSdp message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ICallSdp, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified CallSdp message, length delimited. Does not implicitly {@link server.CallSdp.verify|verify} messages.
         * @param message CallSdp message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ICallSdp, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a CallSdp message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns CallSdp
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.CallSdp;

        /**
         * Decodes a CallSdp message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns CallSdp
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.CallSdp;

        /**
         * Verifies a CallSdp message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a CallSdp message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns CallSdp
         */
        public static fromObject(object: { [k: string]: any }): server.CallSdp;

        /**
         * Creates a plain object from a CallSdp message. Also converts values to other types if specified.
         * @param message CallSdp
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.CallSdp, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this CallSdp to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace CallSdp {

        /** SdpType enum. */
        enum SdpType {
            UNKNOWN = 0,
            OFFER = 1,
            ANSWER = 2
        }
    }

    /** Properties of an EndCall. */
    interface IEndCall {

        /** EndCall callId */
        callId?: (string|null);

        /** EndCall reason */
        reason?: (server.EndCall.Reason|null);

        /** EndCall timestampMs */
        timestampMs?: (number|Long|null);
    }

    /** Represents an EndCall. */
    class EndCall implements IEndCall {

        /**
         * Constructs a new EndCall.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IEndCall);

        /** EndCall callId. */
        public callId: string;

        /** EndCall reason. */
        public reason: server.EndCall.Reason;

        /** EndCall timestampMs. */
        public timestampMs: (number|Long);

        /**
         * Creates a new EndCall instance using the specified properties.
         * @param [properties] Properties to set
         * @returns EndCall instance
         */
        public static create(properties?: server.IEndCall): server.EndCall;

        /**
         * Encodes the specified EndCall message. Does not implicitly {@link server.EndCall.verify|verify} messages.
         * @param message EndCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IEndCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified EndCall message, length delimited. Does not implicitly {@link server.EndCall.verify|verify} messages.
         * @param message EndCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IEndCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an EndCall message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns EndCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.EndCall;

        /**
         * Decodes an EndCall message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns EndCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.EndCall;

        /**
         * Verifies an EndCall message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an EndCall message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns EndCall
         */
        public static fromObject(object: { [k: string]: any }): server.EndCall;

        /**
         * Creates a plain object from an EndCall message. Also converts values to other types if specified.
         * @param message EndCall
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.EndCall, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this EndCall to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace EndCall {

        /** Reason enum. */
        enum Reason {
            UNKNOWN = 0,
            REJECT = 1,
            BUSY = 2,
            TIMEOUT = 3,
            CALL_END = 4,
            CANCEL = 5,
            DECRYPTION_FAILED = 6,
            ENCRYPTION_FAILED = 7,
            SYSTEM_ERROR = 8,
            VIDEO_UNSUPPORTED = 9,
            CONNECTION_ERROR = 10
        }
    }

    /** Properties of an IceRestartOffer. */
    interface IIceRestartOffer {

        /** IceRestartOffer callId */
        callId?: (string|null);

        /** IceRestartOffer idx */
        idx?: (number|null);

        /** IceRestartOffer webrtcOffer */
        webrtcOffer?: (server.IWebRtcSessionDescription|null);
    }

    /** Represents an IceRestartOffer. */
    class IceRestartOffer implements IIceRestartOffer {

        /**
         * Constructs a new IceRestartOffer.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IIceRestartOffer);

        /** IceRestartOffer callId. */
        public callId: string;

        /** IceRestartOffer idx. */
        public idx: number;

        /** IceRestartOffer webrtcOffer. */
        public webrtcOffer?: (server.IWebRtcSessionDescription|null);

        /**
         * Creates a new IceRestartOffer instance using the specified properties.
         * @param [properties] Properties to set
         * @returns IceRestartOffer instance
         */
        public static create(properties?: server.IIceRestartOffer): server.IceRestartOffer;

        /**
         * Encodes the specified IceRestartOffer message. Does not implicitly {@link server.IceRestartOffer.verify|verify} messages.
         * @param message IceRestartOffer message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IIceRestartOffer, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified IceRestartOffer message, length delimited. Does not implicitly {@link server.IceRestartOffer.verify|verify} messages.
         * @param message IceRestartOffer message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IIceRestartOffer, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an IceRestartOffer message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns IceRestartOffer
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.IceRestartOffer;

        /**
         * Decodes an IceRestartOffer message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns IceRestartOffer
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.IceRestartOffer;

        /**
         * Verifies an IceRestartOffer message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an IceRestartOffer message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns IceRestartOffer
         */
        public static fromObject(object: { [k: string]: any }): server.IceRestartOffer;

        /**
         * Creates a plain object from an IceRestartOffer message. Also converts values to other types if specified.
         * @param message IceRestartOffer
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.IceRestartOffer, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this IceRestartOffer to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an IceRestartAnswer. */
    interface IIceRestartAnswer {

        /** IceRestartAnswer callId */
        callId?: (string|null);

        /** IceRestartAnswer idx */
        idx?: (number|null);

        /** IceRestartAnswer webrtcAnswer */
        webrtcAnswer?: (server.IWebRtcSessionDescription|null);
    }

    /** Represents an IceRestartAnswer. */
    class IceRestartAnswer implements IIceRestartAnswer {

        /**
         * Constructs a new IceRestartAnswer.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IIceRestartAnswer);

        /** IceRestartAnswer callId. */
        public callId: string;

        /** IceRestartAnswer idx. */
        public idx: number;

        /** IceRestartAnswer webrtcAnswer. */
        public webrtcAnswer?: (server.IWebRtcSessionDescription|null);

        /**
         * Creates a new IceRestartAnswer instance using the specified properties.
         * @param [properties] Properties to set
         * @returns IceRestartAnswer instance
         */
        public static create(properties?: server.IIceRestartAnswer): server.IceRestartAnswer;

        /**
         * Encodes the specified IceRestartAnswer message. Does not implicitly {@link server.IceRestartAnswer.verify|verify} messages.
         * @param message IceRestartAnswer message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IIceRestartAnswer, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified IceRestartAnswer message, length delimited. Does not implicitly {@link server.IceRestartAnswer.verify|verify} messages.
         * @param message IceRestartAnswer message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IIceRestartAnswer, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an IceRestartAnswer message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns IceRestartAnswer
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.IceRestartAnswer;

        /**
         * Decodes an IceRestartAnswer message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns IceRestartAnswer
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.IceRestartAnswer;

        /**
         * Verifies an IceRestartAnswer message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an IceRestartAnswer message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns IceRestartAnswer
         */
        public static fromObject(object: { [k: string]: any }): server.IceRestartAnswer;

        /**
         * Creates a plain object from an IceRestartAnswer message. Also converts values to other types if specified.
         * @param message IceRestartAnswer
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.IceRestartAnswer, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this IceRestartAnswer to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a HoldCall. */
    interface IHoldCall {

        /** HoldCall callId */
        callId?: (string|null);

        /** HoldCall hold */
        hold?: (boolean|null);

        /** HoldCall timestampMs */
        timestampMs?: (number|Long|null);
    }

    /** Represents a HoldCall. */
    class HoldCall implements IHoldCall {

        /**
         * Constructs a new HoldCall.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IHoldCall);

        /** HoldCall callId. */
        public callId: string;

        /** HoldCall hold. */
        public hold: boolean;

        /** HoldCall timestampMs. */
        public timestampMs: (number|Long);

        /**
         * Creates a new HoldCall instance using the specified properties.
         * @param [properties] Properties to set
         * @returns HoldCall instance
         */
        public static create(properties?: server.IHoldCall): server.HoldCall;

        /**
         * Encodes the specified HoldCall message. Does not implicitly {@link server.HoldCall.verify|verify} messages.
         * @param message HoldCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IHoldCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified HoldCall message, length delimited. Does not implicitly {@link server.HoldCall.verify|verify} messages.
         * @param message HoldCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IHoldCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a HoldCall message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns HoldCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.HoldCall;

        /**
         * Decodes a HoldCall message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns HoldCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.HoldCall;

        /**
         * Verifies a HoldCall message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a HoldCall message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns HoldCall
         */
        public static fromObject(object: { [k: string]: any }): server.HoldCall;

        /**
         * Creates a plain object from a HoldCall message. Also converts values to other types if specified.
         * @param message HoldCall
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.HoldCall, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this HoldCall to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a MuteCall. */
    interface IMuteCall {

        /** MuteCall callId */
        callId?: (string|null);

        /** MuteCall mediaType */
        mediaType?: (server.MuteCall.MediaType|null);

        /** MuteCall muted */
        muted?: (boolean|null);

        /** MuteCall timestampMs */
        timestampMs?: (number|Long|null);
    }

    /** Represents a MuteCall. */
    class MuteCall implements IMuteCall {

        /**
         * Constructs a new MuteCall.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMuteCall);

        /** MuteCall callId. */
        public callId: string;

        /** MuteCall mediaType. */
        public mediaType: server.MuteCall.MediaType;

        /** MuteCall muted. */
        public muted: boolean;

        /** MuteCall timestampMs. */
        public timestampMs: (number|Long);

        /**
         * Creates a new MuteCall instance using the specified properties.
         * @param [properties] Properties to set
         * @returns MuteCall instance
         */
        public static create(properties?: server.IMuteCall): server.MuteCall;

        /**
         * Encodes the specified MuteCall message. Does not implicitly {@link server.MuteCall.verify|verify} messages.
         * @param message MuteCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMuteCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified MuteCall message, length delimited. Does not implicitly {@link server.MuteCall.verify|verify} messages.
         * @param message MuteCall message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMuteCall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a MuteCall message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns MuteCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.MuteCall;

        /**
         * Decodes a MuteCall message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns MuteCall
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.MuteCall;

        /**
         * Verifies a MuteCall message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a MuteCall message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns MuteCall
         */
        public static fromObject(object: { [k: string]: any }): server.MuteCall;

        /**
         * Creates a plain object from a MuteCall message. Also converts values to other types if specified.
         * @param message MuteCall
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.MuteCall, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this MuteCall to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace MuteCall {

        /** MediaType enum. */
        enum MediaType {
            AUDIO = 0,
            VIDEO = 1
        }
    }

    /** Properties of a CallConfig. */
    interface ICallConfig {

        /** CallConfig audioBitrateMax */
        audioBitrateMax?: (number|null);

        /** CallConfig videoBitrateMax */
        videoBitrateMax?: (number|null);

        /** CallConfig audioCodec */
        audioCodec?: (number|null);

        /** CallConfig videoCodec */
        videoCodec?: (number|null);

        /** CallConfig videoWidth */
        videoWidth?: (number|null);

        /** CallConfig videoHeight */
        videoHeight?: (number|null);

        /** CallConfig videoFps */
        videoFps?: (number|null);

        /** CallConfig audioJitterBufferMaxPackets */
        audioJitterBufferMaxPackets?: (number|null);

        /** CallConfig audioJitterBufferFastAccelerate */
        audioJitterBufferFastAccelerate?: (boolean|null);

        /** CallConfig iceTransportPolicy */
        iceTransportPolicy?: (server.CallConfig.IceTransportPolicy|null);

        /** CallConfig iceRestartDelayMs */
        iceRestartDelayMs?: (number|null);

        /** CallConfig pruneTurnPorts */
        pruneTurnPorts?: (boolean|null);

        /** CallConfig iceCandidatePoolSize */
        iceCandidatePoolSize?: (number|null);

        /** CallConfig iceBackupPingIntervalMs */
        iceBackupPingIntervalMs?: (number|null);

        /** CallConfig iceConnectionTimeoutMs */
        iceConnectionTimeoutMs?: (number|null);
    }

    /** Represents a CallConfig. */
    class CallConfig implements ICallConfig {

        /**
         * Constructs a new CallConfig.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ICallConfig);

        /** CallConfig audioBitrateMax. */
        public audioBitrateMax: number;

        /** CallConfig videoBitrateMax. */
        public videoBitrateMax: number;

        /** CallConfig audioCodec. */
        public audioCodec: number;

        /** CallConfig videoCodec. */
        public videoCodec: number;

        /** CallConfig videoWidth. */
        public videoWidth: number;

        /** CallConfig videoHeight. */
        public videoHeight: number;

        /** CallConfig videoFps. */
        public videoFps: number;

        /** CallConfig audioJitterBufferMaxPackets. */
        public audioJitterBufferMaxPackets: number;

        /** CallConfig audioJitterBufferFastAccelerate. */
        public audioJitterBufferFastAccelerate: boolean;

        /** CallConfig iceTransportPolicy. */
        public iceTransportPolicy: server.CallConfig.IceTransportPolicy;

        /** CallConfig iceRestartDelayMs. */
        public iceRestartDelayMs: number;

        /** CallConfig pruneTurnPorts. */
        public pruneTurnPorts: boolean;

        /** CallConfig iceCandidatePoolSize. */
        public iceCandidatePoolSize: number;

        /** CallConfig iceBackupPingIntervalMs. */
        public iceBackupPingIntervalMs: number;

        /** CallConfig iceConnectionTimeoutMs. */
        public iceConnectionTimeoutMs: number;

        /**
         * Creates a new CallConfig instance using the specified properties.
         * @param [properties] Properties to set
         * @returns CallConfig instance
         */
        public static create(properties?: server.ICallConfig): server.CallConfig;

        /**
         * Encodes the specified CallConfig message. Does not implicitly {@link server.CallConfig.verify|verify} messages.
         * @param message CallConfig message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ICallConfig, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified CallConfig message, length delimited. Does not implicitly {@link server.CallConfig.verify|verify} messages.
         * @param message CallConfig message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ICallConfig, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a CallConfig message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns CallConfig
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.CallConfig;

        /**
         * Decodes a CallConfig message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns CallConfig
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.CallConfig;

        /**
         * Verifies a CallConfig message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a CallConfig message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns CallConfig
         */
        public static fromObject(object: { [k: string]: any }): server.CallConfig;

        /**
         * Creates a plain object from a CallConfig message. Also converts values to other types if specified.
         * @param message CallConfig
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.CallConfig, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this CallConfig to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace CallConfig {

        /** IceTransportPolicy enum. */
        enum IceTransportPolicy {
            ALL = 0,
            RELAY = 1
        }
    }

    /** Properties of an OgTagInfo. */
    interface IOgTagInfo {

        /** OgTagInfo title */
        title?: (string|null);

        /** OgTagInfo description */
        description?: (string|null);

        /** OgTagInfo thumbnailUrl */
        thumbnailUrl?: (string|null);

        /** OgTagInfo thumbnailWidth */
        thumbnailWidth?: (number|null);

        /** OgTagInfo thumbnailHeight */
        thumbnailHeight?: (number|null);
    }

    /** Represents an OgTagInfo. */
    class OgTagInfo implements IOgTagInfo {

        /**
         * Constructs a new OgTagInfo.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IOgTagInfo);

        /** OgTagInfo title. */
        public title: string;

        /** OgTagInfo description. */
        public description: string;

        /** OgTagInfo thumbnailUrl. */
        public thumbnailUrl: string;

        /** OgTagInfo thumbnailWidth. */
        public thumbnailWidth: number;

        /** OgTagInfo thumbnailHeight. */
        public thumbnailHeight: number;

        /**
         * Creates a new OgTagInfo instance using the specified properties.
         * @param [properties] Properties to set
         * @returns OgTagInfo instance
         */
        public static create(properties?: server.IOgTagInfo): server.OgTagInfo;

        /**
         * Encodes the specified OgTagInfo message. Does not implicitly {@link server.OgTagInfo.verify|verify} messages.
         * @param message OgTagInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IOgTagInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified OgTagInfo message, length delimited. Does not implicitly {@link server.OgTagInfo.verify|verify} messages.
         * @param message OgTagInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IOgTagInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an OgTagInfo message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns OgTagInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.OgTagInfo;

        /**
         * Decodes an OgTagInfo message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns OgTagInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.OgTagInfo;

        /**
         * Verifies an OgTagInfo message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an OgTagInfo message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns OgTagInfo
         */
        public static fromObject(object: { [k: string]: any }): server.OgTagInfo;

        /**
         * Creates a plain object from an OgTagInfo message. Also converts values to other types if specified.
         * @param message OgTagInfo
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.OgTagInfo, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this OgTagInfo to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an ExternalSharePost. */
    interface IExternalSharePost {

        /** ExternalSharePost action */
        action?: (server.ExternalSharePost.Action|null);

        /** ExternalSharePost blobId */
        blobId?: (string|null);

        /** ExternalSharePost blob */
        blob?: (Uint8Array|null);

        /** ExternalSharePost expiresInSeconds */
        expiresInSeconds?: (number|Long|null);

        /** ExternalSharePost ogTagInfo */
        ogTagInfo?: (server.IOgTagInfo|null);
    }

    /** Represents an ExternalSharePost. */
    class ExternalSharePost implements IExternalSharePost {

        /**
         * Constructs a new ExternalSharePost.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IExternalSharePost);

        /** ExternalSharePost action. */
        public action: server.ExternalSharePost.Action;

        /** ExternalSharePost blobId. */
        public blobId: string;

        /** ExternalSharePost blob. */
        public blob: Uint8Array;

        /** ExternalSharePost expiresInSeconds. */
        public expiresInSeconds: (number|Long);

        /** ExternalSharePost ogTagInfo. */
        public ogTagInfo?: (server.IOgTagInfo|null);

        /**
         * Creates a new ExternalSharePost instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ExternalSharePost instance
         */
        public static create(properties?: server.IExternalSharePost): server.ExternalSharePost;

        /**
         * Encodes the specified ExternalSharePost message. Does not implicitly {@link server.ExternalSharePost.verify|verify} messages.
         * @param message ExternalSharePost message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IExternalSharePost, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ExternalSharePost message, length delimited. Does not implicitly {@link server.ExternalSharePost.verify|verify} messages.
         * @param message ExternalSharePost message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IExternalSharePost, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an ExternalSharePost message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ExternalSharePost
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ExternalSharePost;

        /**
         * Decodes an ExternalSharePost message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ExternalSharePost
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ExternalSharePost;

        /**
         * Verifies an ExternalSharePost message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an ExternalSharePost message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ExternalSharePost
         */
        public static fromObject(object: { [k: string]: any }): server.ExternalSharePost;

        /**
         * Creates a plain object from an ExternalSharePost message. Also converts values to other types if specified.
         * @param message ExternalSharePost
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ExternalSharePost, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ExternalSharePost to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ExternalSharePost {

        /** Action enum. */
        enum Action {
            STORE = 0,
            DELETE = 1,
            GET = 2
        }
    }

    /** Properties of an ExternalSharePostContainer. */
    interface IExternalSharePostContainer {

        /** ExternalSharePostContainer uid */
        uid?: (number|Long|null);

        /** ExternalSharePostContainer blob */
        blob?: (Uint8Array|null);

        /** ExternalSharePostContainer ogTagInfo */
        ogTagInfo?: (server.IOgTagInfo|null);

        /** ExternalSharePostContainer name */
        name?: (string|null);

        /** ExternalSharePostContainer avatarId */
        avatarId?: (string|null);
    }

    /** Represents an ExternalSharePostContainer. */
    class ExternalSharePostContainer implements IExternalSharePostContainer {

        /**
         * Constructs a new ExternalSharePostContainer.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IExternalSharePostContainer);

        /** ExternalSharePostContainer uid. */
        public uid: (number|Long);

        /** ExternalSharePostContainer blob. */
        public blob: Uint8Array;

        /** ExternalSharePostContainer ogTagInfo. */
        public ogTagInfo?: (server.IOgTagInfo|null);

        /** ExternalSharePostContainer name. */
        public name: string;

        /** ExternalSharePostContainer avatarId. */
        public avatarId: string;

        /**
         * Creates a new ExternalSharePostContainer instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ExternalSharePostContainer instance
         */
        public static create(properties?: server.IExternalSharePostContainer): server.ExternalSharePostContainer;

        /**
         * Encodes the specified ExternalSharePostContainer message. Does not implicitly {@link server.ExternalSharePostContainer.verify|verify} messages.
         * @param message ExternalSharePostContainer message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IExternalSharePostContainer, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ExternalSharePostContainer message, length delimited. Does not implicitly {@link server.ExternalSharePostContainer.verify|verify} messages.
         * @param message ExternalSharePostContainer message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IExternalSharePostContainer, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an ExternalSharePostContainer message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ExternalSharePostContainer
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ExternalSharePostContainer;

        /**
         * Decodes an ExternalSharePostContainer message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ExternalSharePostContainer
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ExternalSharePostContainer;

        /**
         * Verifies an ExternalSharePostContainer message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an ExternalSharePostContainer message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ExternalSharePostContainer
         */
        public static fromObject(object: { [k: string]: any }): server.ExternalSharePostContainer;

        /**
         * Creates a plain object from an ExternalSharePostContainer message. Also converts values to other types if specified.
         * @param message ExternalSharePostContainer
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ExternalSharePostContainer, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ExternalSharePostContainer to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a WebClientInfo. */
    interface IWebClientInfo {

        /** WebClientInfo action */
        action?: (server.WebClientInfo.Action|null);

        /** WebClientInfo staticKey */
        staticKey?: (Uint8Array|null);

        /** WebClientInfo result */
        result?: (server.WebClientInfo.Result|null);
    }

    /** Represents a WebClientInfo. */
    class WebClientInfo implements IWebClientInfo {

        /**
         * Constructs a new WebClientInfo.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IWebClientInfo);

        /** WebClientInfo action. */
        public action: server.WebClientInfo.Action;

        /** WebClientInfo staticKey. */
        public staticKey: Uint8Array;

        /** WebClientInfo result. */
        public result: server.WebClientInfo.Result;

        /**
         * Creates a new WebClientInfo instance using the specified properties.
         * @param [properties] Properties to set
         * @returns WebClientInfo instance
         */
        public static create(properties?: server.IWebClientInfo): server.WebClientInfo;

        /**
         * Encodes the specified WebClientInfo message. Does not implicitly {@link server.WebClientInfo.verify|verify} messages.
         * @param message WebClientInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IWebClientInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified WebClientInfo message, length delimited. Does not implicitly {@link server.WebClientInfo.verify|verify} messages.
         * @param message WebClientInfo message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IWebClientInfo, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a WebClientInfo message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns WebClientInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.WebClientInfo;

        /**
         * Decodes a WebClientInfo message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns WebClientInfo
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.WebClientInfo;

        /**
         * Verifies a WebClientInfo message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a WebClientInfo message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns WebClientInfo
         */
        public static fromObject(object: { [k: string]: any }): server.WebClientInfo;

        /**
         * Creates a plain object from a WebClientInfo message. Also converts values to other types if specified.
         * @param message WebClientInfo
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.WebClientInfo, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this WebClientInfo to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace WebClientInfo {

        /** Action enum. */
        enum Action {
            UNKNOWN_ACTION = 0,
            ADD_KEY = 1,
            IS_KEY_AUTHENTICATED = 2,
            AUTHENTICATE_KEY = 3,
            REMOVE_KEY = 4
        }

        /** Result enum. */
        enum Result {
            UNKNOWN = 0,
            OK = 1,
            AUTHENTICATED = 2,
            NOT_AUTHENTICATED = 3
        }
    }

    /** Properties of a ReportUserContent. */
    interface IReportUserContent {

        /** ReportUserContent type */
        type?: (server.ReportUserContent.Type|null);

        /** ReportUserContent uid */
        uid?: (number|Long|null);

        /** ReportUserContent contentId */
        contentId?: (string|null);

        /** ReportUserContent reason */
        reason?: (server.ReportUserContent.Reason|null);
    }

    /** Represents a ReportUserContent. */
    class ReportUserContent implements IReportUserContent {

        /**
         * Constructs a new ReportUserContent.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IReportUserContent);

        /** ReportUserContent type. */
        public type: server.ReportUserContent.Type;

        /** ReportUserContent uid. */
        public uid: (number|Long);

        /** ReportUserContent contentId. */
        public contentId: string;

        /** ReportUserContent reason. */
        public reason: server.ReportUserContent.Reason;

        /**
         * Creates a new ReportUserContent instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ReportUserContent instance
         */
        public static create(properties?: server.IReportUserContent): server.ReportUserContent;

        /**
         * Encodes the specified ReportUserContent message. Does not implicitly {@link server.ReportUserContent.verify|verify} messages.
         * @param message ReportUserContent message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IReportUserContent, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ReportUserContent message, length delimited. Does not implicitly {@link server.ReportUserContent.verify|verify} messages.
         * @param message ReportUserContent message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IReportUserContent, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ReportUserContent message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ReportUserContent
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ReportUserContent;

        /**
         * Decodes a ReportUserContent message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ReportUserContent
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ReportUserContent;

        /**
         * Verifies a ReportUserContent message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ReportUserContent message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ReportUserContent
         */
        public static fromObject(object: { [k: string]: any }): server.ReportUserContent;

        /**
         * Creates a plain object from a ReportUserContent message. Also converts values to other types if specified.
         * @param message ReportUserContent
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ReportUserContent, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ReportUserContent to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ReportUserContent {

        /** Type enum. */
        enum Type {
            UNKNOWN_TYPE = 0,
            USER = 1,
            POST = 2
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            DONT_LIKE = 1,
            SPAM = 2,
            VIOLATES_RULES = 3,
            OTHER = 4
        }
    }

    /** Properties of a WebStanza. */
    interface IWebStanza {

        /** WebStanza staticKey */
        staticKey?: (Uint8Array|null);

        /** WebStanza content */
        content?: (Uint8Array|null);

        /** WebStanza noiseMessage */
        noiseMessage?: (server.INoiseMessage|null);
    }

    /** Represents a WebStanza. */
    class WebStanza implements IWebStanza {

        /**
         * Constructs a new WebStanza.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IWebStanza);

        /** WebStanza staticKey. */
        public staticKey: Uint8Array;

        /** WebStanza content. */
        public content: Uint8Array;

        /** WebStanza noiseMessage. */
        public noiseMessage?: (server.INoiseMessage|null);

        /** WebStanza payload. */
        public payload?: ("content"|"noiseMessage");

        /**
         * Creates a new WebStanza instance using the specified properties.
         * @param [properties] Properties to set
         * @returns WebStanza instance
         */
        public static create(properties?: server.IWebStanza): server.WebStanza;

        /**
         * Encodes the specified WebStanza message. Does not implicitly {@link server.WebStanza.verify|verify} messages.
         * @param message WebStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IWebStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified WebStanza message, length delimited. Does not implicitly {@link server.WebStanza.verify|verify} messages.
         * @param message WebStanza message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IWebStanza, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a WebStanza message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns WebStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.WebStanza;

        /**
         * Decodes a WebStanza message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns WebStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.WebStanza;

        /**
         * Verifies a WebStanza message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a WebStanza message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns WebStanza
         */
        public static fromObject(object: { [k: string]: any }): server.WebStanza;

        /**
         * Creates a plain object from a WebStanza message. Also converts values to other types if specified.
         * @param message WebStanza
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.WebStanza, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this WebStanza to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a ContentMissing. */
    interface IContentMissing {

        /** ContentMissing contentId */
        contentId?: (string|null);

        /** ContentMissing contentType */
        contentType?: (server.ContentMissing.ContentType|null);

        /** ContentMissing senderClientVersion */
        senderClientVersion?: (string|null);
    }

    /** Represents a ContentMissing. */
    class ContentMissing implements IContentMissing {

        /**
         * Constructs a new ContentMissing.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IContentMissing);

        /** ContentMissing contentId. */
        public contentId: string;

        /** ContentMissing contentType. */
        public contentType: server.ContentMissing.ContentType;

        /** ContentMissing senderClientVersion. */
        public senderClientVersion: string;

        /**
         * Creates a new ContentMissing instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ContentMissing instance
         */
        public static create(properties?: server.IContentMissing): server.ContentMissing;

        /**
         * Encodes the specified ContentMissing message. Does not implicitly {@link server.ContentMissing.verify|verify} messages.
         * @param message ContentMissing message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IContentMissing, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ContentMissing message, length delimited. Does not implicitly {@link server.ContentMissing.verify|verify} messages.
         * @param message ContentMissing message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IContentMissing, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ContentMissing message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ContentMissing
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ContentMissing;

        /**
         * Decodes a ContentMissing message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ContentMissing
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ContentMissing;

        /**
         * Verifies a ContentMissing message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ContentMissing message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ContentMissing
         */
        public static fromObject(object: { [k: string]: any }): server.ContentMissing;

        /**
         * Creates a plain object from a ContentMissing message. Also converts values to other types if specified.
         * @param message ContentMissing
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ContentMissing, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ContentMissing to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ContentMissing {

        /** ContentType enum. */
        enum ContentType {
            UNKNOWN = 0,
            CHAT = 1,
            CALL = 2,
            GROUP_FEED_POST = 3,
            GROUP_FEED_COMMENT = 4,
            HOME_FEED_POST = 5,
            HOME_FEED_COMMENT = 6,
            HISTORY_RESEND = 7,
            GROUP_HISTORY = 8,
            CHAT_REACTION = 9,
            GROUP_COMMENT_REACTION = 10,
            GROUP_POST_REACTION = 11,
            HOME_COMMENT_REACTION = 12,
            HOME_POST_REACTION = 13,
            GROUP_CHAT = 14,
            GROUP_CHAT_REACTION = 15
        }
    }

    /** Properties of a MomentNotification. */
    interface IMomentNotification {

        /** MomentNotification timestamp */
        timestamp?: (number|Long|null);

        /** MomentNotification notificationId */
        notificationId?: (number|Long|null);

        /** MomentNotification type */
        type?: (server.MomentNotification.Type|null);

        /** MomentNotification prompt */
        prompt?: (string|null);

        /** MomentNotification hideBanner */
        hideBanner?: (boolean|null);

        /** MomentNotification promptImage */
        promptImage?: (Uint8Array|null);

        /** MomentNotification date */
        date?: (string|null);
    }

    /** Represents a MomentNotification. */
    class MomentNotification implements IMomentNotification {

        /**
         * Constructs a new MomentNotification.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMomentNotification);

        /** MomentNotification timestamp. */
        public timestamp: (number|Long);

        /** MomentNotification notificationId. */
        public notificationId: (number|Long);

        /** MomentNotification type. */
        public type: server.MomentNotification.Type;

        /** MomentNotification prompt. */
        public prompt: string;

        /** MomentNotification hideBanner. */
        public hideBanner: boolean;

        /** MomentNotification promptImage. */
        public promptImage: Uint8Array;

        /** MomentNotification date. */
        public date: string;

        /**
         * Creates a new MomentNotification instance using the specified properties.
         * @param [properties] Properties to set
         * @returns MomentNotification instance
         */
        public static create(properties?: server.IMomentNotification): server.MomentNotification;

        /**
         * Encodes the specified MomentNotification message. Does not implicitly {@link server.MomentNotification.verify|verify} messages.
         * @param message MomentNotification message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMomentNotification, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified MomentNotification message, length delimited. Does not implicitly {@link server.MomentNotification.verify|verify} messages.
         * @param message MomentNotification message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMomentNotification, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a MomentNotification message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns MomentNotification
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.MomentNotification;

        /**
         * Decodes a MomentNotification message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns MomentNotification
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.MomentNotification;

        /**
         * Verifies a MomentNotification message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a MomentNotification message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns MomentNotification
         */
        public static fromObject(object: { [k: string]: any }): server.MomentNotification;

        /**
         * Creates a plain object from a MomentNotification message. Also converts values to other types if specified.
         * @param message MomentNotification
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.MomentNotification, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this MomentNotification to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace MomentNotification {

        /** Type enum. */
        enum Type {
            LIVE_CAMERA = 0,
            TEXT_POST = 1,
            PROMPT_POST = 2,
            ALBUM_POST = 3
        }
    }

    /** Properties of an ArchiveRequest. */
    interface IArchiveRequest {

        /** ArchiveRequest uid */
        uid?: (number|Long|null);
    }

    /** Represents an ArchiveRequest. */
    class ArchiveRequest implements IArchiveRequest {

        /**
         * Constructs a new ArchiveRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IArchiveRequest);

        /** ArchiveRequest uid. */
        public uid: (number|Long);

        /**
         * Creates a new ArchiveRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ArchiveRequest instance
         */
        public static create(properties?: server.IArchiveRequest): server.ArchiveRequest;

        /**
         * Encodes the specified ArchiveRequest message. Does not implicitly {@link server.ArchiveRequest.verify|verify} messages.
         * @param message ArchiveRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IArchiveRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ArchiveRequest message, length delimited. Does not implicitly {@link server.ArchiveRequest.verify|verify} messages.
         * @param message ArchiveRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IArchiveRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an ArchiveRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ArchiveRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ArchiveRequest;

        /**
         * Decodes an ArchiveRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ArchiveRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ArchiveRequest;

        /**
         * Verifies an ArchiveRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an ArchiveRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ArchiveRequest
         */
        public static fromObject(object: { [k: string]: any }): server.ArchiveRequest;

        /**
         * Creates a plain object from an ArchiveRequest message. Also converts values to other types if specified.
         * @param message ArchiveRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ArchiveRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ArchiveRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an ArchiveResult. */
    interface IArchiveResult {

        /** ArchiveResult result */
        result?: (server.ArchiveResult.Result|null);

        /** ArchiveResult reason */
        reason?: (server.ArchiveResult.Reason|null);

        /** ArchiveResult uid */
        uid?: (number|Long|null);

        /** ArchiveResult posts */
        posts?: (server.IPost[]|null);

        /** ArchiveResult startDate */
        startDate?: (string|null);
    }

    /** Represents an ArchiveResult. */
    class ArchiveResult implements IArchiveResult {

        /**
         * Constructs a new ArchiveResult.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IArchiveResult);

        /** ArchiveResult result. */
        public result: server.ArchiveResult.Result;

        /** ArchiveResult reason. */
        public reason: server.ArchiveResult.Reason;

        /** ArchiveResult uid. */
        public uid: (number|Long);

        /** ArchiveResult posts. */
        public posts: server.IPost[];

        /** ArchiveResult startDate. */
        public startDate: string;

        /**
         * Creates a new ArchiveResult instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ArchiveResult instance
         */
        public static create(properties?: server.IArchiveResult): server.ArchiveResult;

        /**
         * Encodes the specified ArchiveResult message. Does not implicitly {@link server.ArchiveResult.verify|verify} messages.
         * @param message ArchiveResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IArchiveResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ArchiveResult message, length delimited. Does not implicitly {@link server.ArchiveResult.verify|verify} messages.
         * @param message ArchiveResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IArchiveResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an ArchiveResult message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ArchiveResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ArchiveResult;

        /**
         * Decodes an ArchiveResult message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ArchiveResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ArchiveResult;

        /**
         * Verifies an ArchiveResult message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an ArchiveResult message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ArchiveResult
         */
        public static fromObject(object: { [k: string]: any }): server.ArchiveResult;

        /**
         * Creates a plain object from an ArchiveResult message. Also converts values to other types if specified.
         * @param message ArchiveResult
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ArchiveResult, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ArchiveResult to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ArchiveResult {

        /** Result enum. */
        enum Result {
            UNKNOWN_RESULT = 0,
            OK = 1,
            FAIL = 2
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            INVALID_USER = 1
        }
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

        /** Iq historyResend */
        historyResend?: (server.IHistoryResend|null);

        /** Iq exportData */
        exportData?: (server.IExportData|null);

        /** Iq contactSyncError */
        contactSyncError?: (server.IContactSyncError|null);

        /** Iq clientOtpRequest */
        clientOtpRequest?: (server.IClientOtpRequest|null);

        /** Iq clientOtpResponse */
        clientOtpResponse?: (server.IClientOtpResponse|null);

        /** Iq whisperKeysCollection */
        whisperKeysCollection?: (server.IWhisperKeysCollection|null);

        /** Iq getCallServers */
        getCallServers?: (server.IGetCallServers|null);

        /** Iq getCallServersResult */
        getCallServersResult?: (server.IGetCallServersResult|null);

        /** Iq startCall */
        startCall?: (server.IStartCall|null);

        /** Iq startCallResult */
        startCallResult?: (server.IStartCallResult|null);

        /** Iq truncWhisperKeysCollection */
        truncWhisperKeysCollection?: (server.ITruncWhisperKeysCollection|null);

        /** Iq externalSharePost */
        externalSharePost?: (server.IExternalSharePost|null);

        /** Iq externalSharePostContainer */
        externalSharePostContainer?: (server.IExternalSharePostContainer|null);

        /** Iq webClientInfo */
        webClientInfo?: (server.IWebClientInfo|null);

        /** Iq reportUserContent */
        reportUserContent?: (server.IReportUserContent|null);

        /** Iq publicFeedRequest */
        publicFeedRequest?: (server.IPublicFeedRequest|null);

        /** Iq publicFeedResponse */
        publicFeedResponse?: (server.IPublicFeedResponse|null);

        /** Iq relationshipRequest */
        relationshipRequest?: (server.IRelationshipRequest|null);

        /** Iq relationshipResponse */
        relationshipResponse?: (server.IRelationshipResponse|null);

        /** Iq relationshipList */
        relationshipList?: (server.IRelationshipList|null);

        /** Iq usernameRequest */
        usernameRequest?: (server.IUsernameRequest|null);

        /** Iq usernameResponse */
        usernameResponse?: (server.IUsernameResponse|null);

        /** Iq searchRequest */
        searchRequest?: (server.ISearchRequest|null);

        /** Iq searchResponse */
        searchResponse?: (server.ISearchResponse|null);

        /** Iq followSuggestionsRequest */
        followSuggestionsRequest?: (server.IFollowSuggestionsRequest|null);

        /** Iq followSuggestionsResponse */
        followSuggestionsResponse?: (server.IFollowSuggestionsResponse|null);

        /** Iq setLinkRequest */
        setLinkRequest?: (server.ISetLinkRequest|null);

        /** Iq setLinkResult */
        setLinkResult?: (server.ISetLinkResult|null);

        /** Iq setBioRequest */
        setBioRequest?: (server.ISetBioRequest|null);

        /** Iq setBioResult */
        setBioResult?: (server.ISetBioResult|null);

        /** Iq userProfileRequest */
        userProfileRequest?: (server.IUserProfileRequest|null);

        /** Iq userProfileResult */
        userProfileResult?: (server.IUserProfileResult|null);

        /** Iq postMetricsRequest */
        postMetricsRequest?: (server.IPostMetricsRequest|null);

        /** Iq postMetricsResult */
        postMetricsResult?: (server.IPostMetricsResult|null);

        /** Iq aiImageRequest */
        aiImageRequest?: (server.IAiImageRequest|null);

        /** Iq aiImageResult */
        aiImageResult?: (server.IAiImageResult|null);

        /** Iq archiveRequest */
        archiveRequest?: (server.IArchiveRequest|null);

        /** Iq archiveResult */
        archiveResult?: (server.IArchiveResult|null);

        /** Iq postSubscriptionRequest */
        postSubscriptionRequest?: (server.IPostSubscriptionRequest|null);

        /** Iq postSubscriptionResponse */
        postSubscriptionResponse?: (server.IPostSubscriptionResponse|null);

        /** Iq geoTagRequest */
        geoTagRequest?: (server.IGeoTagRequest|null);

        /** Iq geoTagResponse */
        geoTagResponse?: (server.IGeoTagResponse|null);
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

        /** Iq historyResend. */
        public historyResend?: (server.IHistoryResend|null);

        /** Iq exportData. */
        public exportData?: (server.IExportData|null);

        /** Iq contactSyncError. */
        public contactSyncError?: (server.IContactSyncError|null);

        /** Iq clientOtpRequest. */
        public clientOtpRequest?: (server.IClientOtpRequest|null);

        /** Iq clientOtpResponse. */
        public clientOtpResponse?: (server.IClientOtpResponse|null);

        /** Iq whisperKeysCollection. */
        public whisperKeysCollection?: (server.IWhisperKeysCollection|null);

        /** Iq getCallServers. */
        public getCallServers?: (server.IGetCallServers|null);

        /** Iq getCallServersResult. */
        public getCallServersResult?: (server.IGetCallServersResult|null);

        /** Iq startCall. */
        public startCall?: (server.IStartCall|null);

        /** Iq startCallResult. */
        public startCallResult?: (server.IStartCallResult|null);

        /** Iq truncWhisperKeysCollection. */
        public truncWhisperKeysCollection?: (server.ITruncWhisperKeysCollection|null);

        /** Iq externalSharePost. */
        public externalSharePost?: (server.IExternalSharePost|null);

        /** Iq externalSharePostContainer. */
        public externalSharePostContainer?: (server.IExternalSharePostContainer|null);

        /** Iq webClientInfo. */
        public webClientInfo?: (server.IWebClientInfo|null);

        /** Iq reportUserContent. */
        public reportUserContent?: (server.IReportUserContent|null);

        /** Iq publicFeedRequest. */
        public publicFeedRequest?: (server.IPublicFeedRequest|null);

        /** Iq publicFeedResponse. */
        public publicFeedResponse?: (server.IPublicFeedResponse|null);

        /** Iq relationshipRequest. */
        public relationshipRequest?: (server.IRelationshipRequest|null);

        /** Iq relationshipResponse. */
        public relationshipResponse?: (server.IRelationshipResponse|null);

        /** Iq relationshipList. */
        public relationshipList?: (server.IRelationshipList|null);

        /** Iq usernameRequest. */
        public usernameRequest?: (server.IUsernameRequest|null);

        /** Iq usernameResponse. */
        public usernameResponse?: (server.IUsernameResponse|null);

        /** Iq searchRequest. */
        public searchRequest?: (server.ISearchRequest|null);

        /** Iq searchResponse. */
        public searchResponse?: (server.ISearchResponse|null);

        /** Iq followSuggestionsRequest. */
        public followSuggestionsRequest?: (server.IFollowSuggestionsRequest|null);

        /** Iq followSuggestionsResponse. */
        public followSuggestionsResponse?: (server.IFollowSuggestionsResponse|null);

        /** Iq setLinkRequest. */
        public setLinkRequest?: (server.ISetLinkRequest|null);

        /** Iq setLinkResult. */
        public setLinkResult?: (server.ISetLinkResult|null);

        /** Iq setBioRequest. */
        public setBioRequest?: (server.ISetBioRequest|null);

        /** Iq setBioResult. */
        public setBioResult?: (server.ISetBioResult|null);

        /** Iq userProfileRequest. */
        public userProfileRequest?: (server.IUserProfileRequest|null);

        /** Iq userProfileResult. */
        public userProfileResult?: (server.IUserProfileResult|null);

        /** Iq postMetricsRequest. */
        public postMetricsRequest?: (server.IPostMetricsRequest|null);

        /** Iq postMetricsResult. */
        public postMetricsResult?: (server.IPostMetricsResult|null);

        /** Iq aiImageRequest. */
        public aiImageRequest?: (server.IAiImageRequest|null);

        /** Iq aiImageResult. */
        public aiImageResult?: (server.IAiImageResult|null);

        /** Iq archiveRequest. */
        public archiveRequest?: (server.IArchiveRequest|null);

        /** Iq archiveResult. */
        public archiveResult?: (server.IArchiveResult|null);

        /** Iq postSubscriptionRequest. */
        public postSubscriptionRequest?: (server.IPostSubscriptionRequest|null);

        /** Iq postSubscriptionResponse. */
        public postSubscriptionResponse?: (server.IPostSubscriptionResponse|null);

        /** Iq geoTagRequest. */
        public geoTagRequest?: (server.IGeoTagRequest|null);

        /** Iq geoTagResponse. */
        public geoTagResponse?: (server.IGeoTagResponse|null);

        /** Iq payload. */
        public payload?: ("uploadMedia"|"contactList"|"uploadAvatar"|"avatar"|"avatars"|"clientMode"|"clientVersion"|"pushRegister"|"whisperKeys"|"ping"|"feedItem"|"privacyList"|"privacyLists"|"groupStanza"|"groupsStanza"|"clientLog"|"name"|"errorStanza"|"props"|"invitesRequest"|"invitesResponse"|"notificationPrefs"|"groupFeedItem"|"groupAvatar"|"deleteAccount"|"groupInviteLink"|"historyResend"|"exportData"|"contactSyncError"|"clientOtpRequest"|"clientOtpResponse"|"whisperKeysCollection"|"getCallServers"|"getCallServersResult"|"startCall"|"startCallResult"|"truncWhisperKeysCollection"|"externalSharePost"|"externalSharePostContainer"|"webClientInfo"|"reportUserContent"|"publicFeedRequest"|"publicFeedResponse"|"relationshipRequest"|"relationshipResponse"|"relationshipList"|"usernameRequest"|"usernameResponse"|"searchRequest"|"searchResponse"|"followSuggestionsRequest"|"followSuggestionsResponse"|"setLinkRequest"|"setLinkResult"|"setBioRequest"|"setBioResult"|"userProfileRequest"|"userProfileResult"|"postMetricsRequest"|"postMetricsResult"|"aiImageRequest"|"aiImageResult"|"archiveRequest"|"archiveResult"|"postSubscriptionRequest"|"postSubscriptionResponse"|"geoTagRequest"|"geoTagResponse");

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

        /** Msg historyResend */
        historyResend?: (server.IHistoryResend|null);

        /** Msg playedReceipt */
        playedReceipt?: (server.IPlayedReceipt|null);

        /** Msg requestLogs */
        requestLogs?: (server.IRequestLogs|null);

        /** Msg wakeup */
        wakeup?: (server.IWakeUp|null);

        /** Msg homeFeedRerequest */
        homeFeedRerequest?: (server.IHomeFeedRerequest|null);

        /** Msg incomingCall */
        incomingCall?: (server.IIncomingCall|null);

        /** Msg callRinging */
        callRinging?: (server.ICallRinging|null);

        /** Msg answerCall */
        answerCall?: (server.IAnswerCall|null);

        /** Msg endCall */
        endCall?: (server.IEndCall|null);

        /** Msg iceCandidate */
        iceCandidate?: (server.IIceCandidate|null);

        /** Msg marketingAlert */
        marketingAlert?: (server.IMarketingAlert|null);

        /** Msg iceRestartOffer */
        iceRestartOffer?: (server.IIceRestartOffer|null);

        /** Msg iceRestartAnswer */
        iceRestartAnswer?: (server.IIceRestartAnswer|null);

        /** Msg groupFeedHistory */
        groupFeedHistory?: (server.IGroupFeedHistory|null);

        /** Msg preAnswerCall */
        preAnswerCall?: (server.IPreAnswerCall|null);

        /** Msg holdCall */
        holdCall?: (server.IHoldCall|null);

        /** Msg muteCall */
        muteCall?: (server.IMuteCall|null);

        /** Msg incomingCallPush */
        incomingCallPush?: (server.IIncomingCallPush|null);

        /** Msg callSdp */
        callSdp?: (server.ICallSdp|null);

        /** Msg webStanza */
        webStanza?: (server.IWebStanza|null);

        /** Msg contentMissing */
        contentMissing?: (server.IContentMissing|null);

        /** Msg screenshotReceipt */
        screenshotReceipt?: (server.IScreenshotReceipt|null);

        /** Msg savedReceipt */
        savedReceipt?: (server.ISavedReceipt|null);

        /** Msg groupChatStanza */
        groupChatStanza?: (server.IGroupChatStanza|null);

        /** Msg momentNotification */
        momentNotification?: (server.IMomentNotification|null);

        /** Msg profileUpdate */
        profileUpdate?: (server.IProfileUpdate|null);

        /** Msg publicFeedUpdate */
        publicFeedUpdate?: (server.IPublicFeedUpdate|null);

        /** Msg aiImage */
        aiImage?: (server.IAiImage|null);

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

        /** Msg historyResend. */
        public historyResend?: (server.IHistoryResend|null);

        /** Msg playedReceipt. */
        public playedReceipt?: (server.IPlayedReceipt|null);

        /** Msg requestLogs. */
        public requestLogs?: (server.IRequestLogs|null);

        /** Msg wakeup. */
        public wakeup?: (server.IWakeUp|null);

        /** Msg homeFeedRerequest. */
        public homeFeedRerequest?: (server.IHomeFeedRerequest|null);

        /** Msg incomingCall. */
        public incomingCall?: (server.IIncomingCall|null);

        /** Msg callRinging. */
        public callRinging?: (server.ICallRinging|null);

        /** Msg answerCall. */
        public answerCall?: (server.IAnswerCall|null);

        /** Msg endCall. */
        public endCall?: (server.IEndCall|null);

        /** Msg iceCandidate. */
        public iceCandidate?: (server.IIceCandidate|null);

        /** Msg marketingAlert. */
        public marketingAlert?: (server.IMarketingAlert|null);

        /** Msg iceRestartOffer. */
        public iceRestartOffer?: (server.IIceRestartOffer|null);

        /** Msg iceRestartAnswer. */
        public iceRestartAnswer?: (server.IIceRestartAnswer|null);

        /** Msg groupFeedHistory. */
        public groupFeedHistory?: (server.IGroupFeedHistory|null);

        /** Msg preAnswerCall. */
        public preAnswerCall?: (server.IPreAnswerCall|null);

        /** Msg holdCall. */
        public holdCall?: (server.IHoldCall|null);

        /** Msg muteCall. */
        public muteCall?: (server.IMuteCall|null);

        /** Msg incomingCallPush. */
        public incomingCallPush?: (server.IIncomingCallPush|null);

        /** Msg callSdp. */
        public callSdp?: (server.ICallSdp|null);

        /** Msg webStanza. */
        public webStanza?: (server.IWebStanza|null);

        /** Msg contentMissing. */
        public contentMissing?: (server.IContentMissing|null);

        /** Msg screenshotReceipt. */
        public screenshotReceipt?: (server.IScreenshotReceipt|null);

        /** Msg savedReceipt. */
        public savedReceipt?: (server.ISavedReceipt|null);

        /** Msg groupChatStanza. */
        public groupChatStanza?: (server.IGroupChatStanza|null);

        /** Msg momentNotification. */
        public momentNotification?: (server.IMomentNotification|null);

        /** Msg profileUpdate. */
        public profileUpdate?: (server.IProfileUpdate|null);

        /** Msg publicFeedUpdate. */
        public publicFeedUpdate?: (server.IPublicFeedUpdate|null);

        /** Msg aiImage. */
        public aiImage?: (server.IAiImage|null);

        /** Msg retryCount. */
        public retryCount: number;

        /** Msg rerequestCount. */
        public rerequestCount: number;

        /** Msg payload. */
        public payload?: ("contactList"|"avatar"|"whisperKeys"|"seenReceipt"|"deliveryReceipt"|"chatStanza"|"feedItem"|"feedItems"|"contactHash"|"groupStanza"|"groupChat"|"name"|"errorStanza"|"groupchatRetract"|"chatRetract"|"groupFeedItem"|"rerequest"|"silentChatStanza"|"groupFeedItems"|"endOfQueue"|"inviteeNotice"|"groupFeedRerequest"|"historyResend"|"playedReceipt"|"requestLogs"|"wakeup"|"homeFeedRerequest"|"incomingCall"|"callRinging"|"answerCall"|"endCall"|"iceCandidate"|"marketingAlert"|"iceRestartOffer"|"iceRestartAnswer"|"groupFeedHistory"|"preAnswerCall"|"holdCall"|"muteCall"|"incomingCallPush"|"callSdp"|"webStanza"|"contentMissing"|"screenshotReceipt"|"savedReceipt"|"groupChatStanza"|"momentNotification"|"profileUpdate"|"publicFeedUpdate"|"aiImage");

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
            CHAT = 4,
            CALL = 5
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

    /** Properties of a PhoneElement. */
    interface IPhoneElement {

        /** PhoneElement action */
        action?: (server.PhoneElement.Action|null);

        /** PhoneElement phone */
        phone?: (string|null);
    }

    /** Represents a PhoneElement. */
    class PhoneElement implements IPhoneElement {

        /**
         * Constructs a new PhoneElement.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPhoneElement);

        /** PhoneElement action. */
        public action: server.PhoneElement.Action;

        /** PhoneElement phone. */
        public phone: string;

        /**
         * Creates a new PhoneElement instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PhoneElement instance
         */
        public static create(properties?: server.IPhoneElement): server.PhoneElement;

        /**
         * Encodes the specified PhoneElement message. Does not implicitly {@link server.PhoneElement.verify|verify} messages.
         * @param message PhoneElement message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPhoneElement, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PhoneElement message, length delimited. Does not implicitly {@link server.PhoneElement.verify|verify} messages.
         * @param message PhoneElement message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPhoneElement, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PhoneElement message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PhoneElement
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PhoneElement;

        /**
         * Decodes a PhoneElement message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PhoneElement
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PhoneElement;

        /**
         * Verifies a PhoneElement message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PhoneElement message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PhoneElement
         */
        public static fromObject(object: { [k: string]: any }): server.PhoneElement;

        /**
         * Creates a plain object from a PhoneElement message. Also converts values to other types if specified.
         * @param message PhoneElement
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PhoneElement, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PhoneElement to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace PhoneElement {

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

        /** PrivacyList phoneElements */
        phoneElements?: (server.IPhoneElement[]|null);

        /** PrivacyList usingPhones */
        usingPhones?: (boolean|null);
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

        /** PrivacyList phoneElements. */
        public phoneElements: server.IPhoneElement[];

        /** PrivacyList usingPhones. */
        public usingPhones: boolean;

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

        /** PushToken tokenType */
        tokenType?: (server.PushToken.TokenType|null);

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

        /** PushToken tokenType. */
        public tokenType: server.PushToken.TokenType;

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

        /** TokenType enum. */
        enum TokenType {
            ANDROID = 0,
            IOS = 1,
            IOS_DEV = 2,
            IOS_APPCLIP = 3,
            IOS_VOIP = 4,
            ANDROID_HUAWEI = 5
        }
    }

    /** Properties of a PushRegister. */
    interface IPushRegister {

        /** PushRegister pushToken */
        pushToken?: (server.IPushToken|null);

        /** PushRegister langId */
        langId?: (string|null);

        /** PushRegister zoneOffset */
        zoneOffset?: (number|Long|null);
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

        /** PushRegister zoneOffset. */
        public zoneOffset: (number|Long);

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
            COMMENT = 1,
            MENTIONS = 2,
            ON_FIRE = 3,
            NEW_USERS = 4,
            FOLLOWERS = 5
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

        /** Rerequest contentType */
        contentType?: (server.Rerequest.ContentType|null);
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

        /** Rerequest contentType. */
        public contentType: server.Rerequest.ContentType;

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

    namespace Rerequest {

        /** ContentType enum. */
        enum ContentType {
            CHAT = 0,
            CALL = 1,
            GROUP_HISTORY = 2,
            CHAT_REACTION = 3
        }
    }

    /** Properties of a GroupFeedRerequest. */
    interface IGroupFeedRerequest {

        /** GroupFeedRerequest gid */
        gid?: (string|null);

        /** GroupFeedRerequest id */
        id?: (string|null);

        /** GroupFeedRerequest rerequestType */
        rerequestType?: (server.GroupFeedRerequest.RerequestType|null);

        /** GroupFeedRerequest contentType */
        contentType?: (server.GroupFeedRerequest.ContentType|null);
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

        /** GroupFeedRerequest rerequestType. */
        public rerequestType: server.GroupFeedRerequest.RerequestType;

        /** GroupFeedRerequest contentType. */
        public contentType: server.GroupFeedRerequest.ContentType;

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

    namespace GroupFeedRerequest {

        /** RerequestType enum. */
        enum RerequestType {
            PAYLOAD = 0,
            SENDER_STATE = 1
        }

        /** ContentType enum. */
        enum ContentType {
            UNKNOWN = 0,
            POST = 1,
            COMMENT = 2,
            HISTORY_RESEND = 3,
            POST_REACTION = 4,
            COMMENT_REACTION = 5,
            MESSAGE = 6,
            MESSAGE_REACTION = 7
        }
    }

    /** Properties of a HomeFeedRerequest. */
    interface IHomeFeedRerequest {

        /** HomeFeedRerequest id */
        id?: (string|null);

        /** HomeFeedRerequest rerequestType */
        rerequestType?: (server.HomeFeedRerequest.RerequestType|null);

        /** HomeFeedRerequest contentType */
        contentType?: (server.HomeFeedRerequest.ContentType|null);
    }

    /** Represents a HomeFeedRerequest. */
    class HomeFeedRerequest implements IHomeFeedRerequest {

        /**
         * Constructs a new HomeFeedRerequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IHomeFeedRerequest);

        /** HomeFeedRerequest id. */
        public id: string;

        /** HomeFeedRerequest rerequestType. */
        public rerequestType: server.HomeFeedRerequest.RerequestType;

        /** HomeFeedRerequest contentType. */
        public contentType: server.HomeFeedRerequest.ContentType;

        /**
         * Creates a new HomeFeedRerequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns HomeFeedRerequest instance
         */
        public static create(properties?: server.IHomeFeedRerequest): server.HomeFeedRerequest;

        /**
         * Encodes the specified HomeFeedRerequest message. Does not implicitly {@link server.HomeFeedRerequest.verify|verify} messages.
         * @param message HomeFeedRerequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IHomeFeedRerequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified HomeFeedRerequest message, length delimited. Does not implicitly {@link server.HomeFeedRerequest.verify|verify} messages.
         * @param message HomeFeedRerequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IHomeFeedRerequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a HomeFeedRerequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns HomeFeedRerequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.HomeFeedRerequest;

        /**
         * Decodes a HomeFeedRerequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns HomeFeedRerequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.HomeFeedRerequest;

        /**
         * Verifies a HomeFeedRerequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a HomeFeedRerequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns HomeFeedRerequest
         */
        public static fromObject(object: { [k: string]: any }): server.HomeFeedRerequest;

        /**
         * Creates a plain object from a HomeFeedRerequest message. Also converts values to other types if specified.
         * @param message HomeFeedRerequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.HomeFeedRerequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this HomeFeedRerequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace HomeFeedRerequest {

        /** RerequestType enum. */
        enum RerequestType {
            UNKNOWN_TYPE = 0,
            PAYLOAD = 1,
            SENDER_STATE = 2
        }

        /** ContentType enum. */
        enum ContentType {
            UNKNOWN = 0,
            POST = 1,
            COMMENT = 2,
            POST_REACTION = 3,
            COMMENT_REACTION = 4
        }
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

    /** Properties of a PlayedReceipt. */
    interface IPlayedReceipt {

        /** PlayedReceipt id */
        id?: (string|null);

        /** PlayedReceipt threadId */
        threadId?: (string|null);

        /** PlayedReceipt timestamp */
        timestamp?: (number|Long|null);
    }

    /** Represents a PlayedReceipt. */
    class PlayedReceipt implements IPlayedReceipt {

        /**
         * Constructs a new PlayedReceipt.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPlayedReceipt);

        /** PlayedReceipt id. */
        public id: string;

        /** PlayedReceipt threadId. */
        public threadId: string;

        /** PlayedReceipt timestamp. */
        public timestamp: (number|Long);

        /**
         * Creates a new PlayedReceipt instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PlayedReceipt instance
         */
        public static create(properties?: server.IPlayedReceipt): server.PlayedReceipt;

        /**
         * Encodes the specified PlayedReceipt message. Does not implicitly {@link server.PlayedReceipt.verify|verify} messages.
         * @param message PlayedReceipt message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPlayedReceipt, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PlayedReceipt message, length delimited. Does not implicitly {@link server.PlayedReceipt.verify|verify} messages.
         * @param message PlayedReceipt message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPlayedReceipt, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PlayedReceipt message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PlayedReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PlayedReceipt;

        /**
         * Decodes a PlayedReceipt message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PlayedReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PlayedReceipt;

        /**
         * Verifies a PlayedReceipt message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PlayedReceipt message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PlayedReceipt
         */
        public static fromObject(object: { [k: string]: any }): server.PlayedReceipt;

        /**
         * Creates a plain object from a PlayedReceipt message. Also converts values to other types if specified.
         * @param message PlayedReceipt
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PlayedReceipt, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PlayedReceipt to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a ScreenshotReceipt. */
    interface IScreenshotReceipt {

        /** ScreenshotReceipt id */
        id?: (string|null);

        /** ScreenshotReceipt threadId */
        threadId?: (string|null);

        /** ScreenshotReceipt timestamp */
        timestamp?: (number|Long|null);
    }

    /** Represents a ScreenshotReceipt. */
    class ScreenshotReceipt implements IScreenshotReceipt {

        /**
         * Constructs a new ScreenshotReceipt.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IScreenshotReceipt);

        /** ScreenshotReceipt id. */
        public id: string;

        /** ScreenshotReceipt threadId. */
        public threadId: string;

        /** ScreenshotReceipt timestamp. */
        public timestamp: (number|Long);

        /**
         * Creates a new ScreenshotReceipt instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ScreenshotReceipt instance
         */
        public static create(properties?: server.IScreenshotReceipt): server.ScreenshotReceipt;

        /**
         * Encodes the specified ScreenshotReceipt message. Does not implicitly {@link server.ScreenshotReceipt.verify|verify} messages.
         * @param message ScreenshotReceipt message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IScreenshotReceipt, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ScreenshotReceipt message, length delimited. Does not implicitly {@link server.ScreenshotReceipt.verify|verify} messages.
         * @param message ScreenshotReceipt message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IScreenshotReceipt, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ScreenshotReceipt message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ScreenshotReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ScreenshotReceipt;

        /**
         * Decodes a ScreenshotReceipt message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ScreenshotReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ScreenshotReceipt;

        /**
         * Verifies a ScreenshotReceipt message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ScreenshotReceipt message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ScreenshotReceipt
         */
        public static fromObject(object: { [k: string]: any }): server.ScreenshotReceipt;

        /**
         * Creates a plain object from a ScreenshotReceipt message. Also converts values to other types if specified.
         * @param message ScreenshotReceipt
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ScreenshotReceipt, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ScreenshotReceipt to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a SavedReceipt. */
    interface ISavedReceipt {

        /** SavedReceipt id */
        id?: (string|null);

        /** SavedReceipt threadId */
        threadId?: (string|null);

        /** SavedReceipt timestamp */
        timestamp?: (number|Long|null);
    }

    /** Represents a SavedReceipt. */
    class SavedReceipt implements ISavedReceipt {

        /**
         * Constructs a new SavedReceipt.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISavedReceipt);

        /** SavedReceipt id. */
        public id: string;

        /** SavedReceipt threadId. */
        public threadId: string;

        /** SavedReceipt timestamp. */
        public timestamp: (number|Long);

        /**
         * Creates a new SavedReceipt instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SavedReceipt instance
         */
        public static create(properties?: server.ISavedReceipt): server.SavedReceipt;

        /**
         * Encodes the specified SavedReceipt message. Does not implicitly {@link server.SavedReceipt.verify|verify} messages.
         * @param message SavedReceipt message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISavedReceipt, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SavedReceipt message, length delimited. Does not implicitly {@link server.SavedReceipt.verify|verify} messages.
         * @param message SavedReceipt message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISavedReceipt, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SavedReceipt message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SavedReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SavedReceipt;

        /**
         * Decodes a SavedReceipt message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SavedReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SavedReceipt;

        /**
         * Verifies a SavedReceipt message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SavedReceipt message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SavedReceipt
         */
        public static fromObject(object: { [k: string]: any }): server.SavedReceipt;

        /**
         * Creates a plain object from a SavedReceipt message. Also converts values to other types if specified.
         * @param message SavedReceipt
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SavedReceipt, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SavedReceipt to JSON.
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

    /** Properties of a TruncWhisperKeys. */
    interface ITruncWhisperKeys {

        /** TruncWhisperKeys uid */
        uid?: (number|Long|null);

        /** TruncWhisperKeys truncPublicIdentityKey */
        truncPublicIdentityKey?: (Uint8Array|null);
    }

    /** Represents a TruncWhisperKeys. */
    class TruncWhisperKeys implements ITruncWhisperKeys {

        /**
         * Constructs a new TruncWhisperKeys.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ITruncWhisperKeys);

        /** TruncWhisperKeys uid. */
        public uid: (number|Long);

        /** TruncWhisperKeys truncPublicIdentityKey. */
        public truncPublicIdentityKey: Uint8Array;

        /**
         * Creates a new TruncWhisperKeys instance using the specified properties.
         * @param [properties] Properties to set
         * @returns TruncWhisperKeys instance
         */
        public static create(properties?: server.ITruncWhisperKeys): server.TruncWhisperKeys;

        /**
         * Encodes the specified TruncWhisperKeys message. Does not implicitly {@link server.TruncWhisperKeys.verify|verify} messages.
         * @param message TruncWhisperKeys message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ITruncWhisperKeys, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified TruncWhisperKeys message, length delimited. Does not implicitly {@link server.TruncWhisperKeys.verify|verify} messages.
         * @param message TruncWhisperKeys message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ITruncWhisperKeys, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a TruncWhisperKeys message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns TruncWhisperKeys
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.TruncWhisperKeys;

        /**
         * Decodes a TruncWhisperKeys message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns TruncWhisperKeys
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.TruncWhisperKeys;

        /**
         * Verifies a TruncWhisperKeys message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a TruncWhisperKeys message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns TruncWhisperKeys
         */
        public static fromObject(object: { [k: string]: any }): server.TruncWhisperKeys;

        /**
         * Creates a plain object from a TruncWhisperKeys message. Also converts values to other types if specified.
         * @param message TruncWhisperKeys
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.TruncWhisperKeys, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this TruncWhisperKeys to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a WhisperKeysCollection. */
    interface IWhisperKeysCollection {

        /** WhisperKeysCollection collection */
        collection?: (server.IWhisperKeys[]|null);
    }

    /** Represents a WhisperKeysCollection. */
    class WhisperKeysCollection implements IWhisperKeysCollection {

        /**
         * Constructs a new WhisperKeysCollection.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IWhisperKeysCollection);

        /** WhisperKeysCollection collection. */
        public collection: server.IWhisperKeys[];

        /**
         * Creates a new WhisperKeysCollection instance using the specified properties.
         * @param [properties] Properties to set
         * @returns WhisperKeysCollection instance
         */
        public static create(properties?: server.IWhisperKeysCollection): server.WhisperKeysCollection;

        /**
         * Encodes the specified WhisperKeysCollection message. Does not implicitly {@link server.WhisperKeysCollection.verify|verify} messages.
         * @param message WhisperKeysCollection message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IWhisperKeysCollection, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified WhisperKeysCollection message, length delimited. Does not implicitly {@link server.WhisperKeysCollection.verify|verify} messages.
         * @param message WhisperKeysCollection message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IWhisperKeysCollection, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a WhisperKeysCollection message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns WhisperKeysCollection
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.WhisperKeysCollection;

        /**
         * Decodes a WhisperKeysCollection message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns WhisperKeysCollection
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.WhisperKeysCollection;

        /**
         * Verifies a WhisperKeysCollection message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a WhisperKeysCollection message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns WhisperKeysCollection
         */
        public static fromObject(object: { [k: string]: any }): server.WhisperKeysCollection;

        /**
         * Creates a plain object from a WhisperKeysCollection message. Also converts values to other types if specified.
         * @param message WhisperKeysCollection
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.WhisperKeysCollection, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this WhisperKeysCollection to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a TruncWhisperKeysCollection. */
    interface ITruncWhisperKeysCollection {

        /** TruncWhisperKeysCollection collection */
        collection?: (server.ITruncWhisperKeys[]|null);
    }

    /** Represents a TruncWhisperKeysCollection. */
    class TruncWhisperKeysCollection implements ITruncWhisperKeysCollection {

        /**
         * Constructs a new TruncWhisperKeysCollection.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ITruncWhisperKeysCollection);

        /** TruncWhisperKeysCollection collection. */
        public collection: server.ITruncWhisperKeys[];

        /**
         * Creates a new TruncWhisperKeysCollection instance using the specified properties.
         * @param [properties] Properties to set
         * @returns TruncWhisperKeysCollection instance
         */
        public static create(properties?: server.ITruncWhisperKeysCollection): server.TruncWhisperKeysCollection;

        /**
         * Encodes the specified TruncWhisperKeysCollection message. Does not implicitly {@link server.TruncWhisperKeysCollection.verify|verify} messages.
         * @param message TruncWhisperKeysCollection message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ITruncWhisperKeysCollection, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified TruncWhisperKeysCollection message, length delimited. Does not implicitly {@link server.TruncWhisperKeysCollection.verify|verify} messages.
         * @param message TruncWhisperKeysCollection message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ITruncWhisperKeysCollection, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a TruncWhisperKeysCollection message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns TruncWhisperKeysCollection
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.TruncWhisperKeysCollection;

        /**
         * Decodes a TruncWhisperKeysCollection message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns TruncWhisperKeysCollection
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.TruncWhisperKeysCollection;

        /**
         * Verifies a TruncWhisperKeysCollection message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a TruncWhisperKeysCollection message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns TruncWhisperKeysCollection
         */
        public static fromObject(object: { [k: string]: any }): server.TruncWhisperKeysCollection;

        /**
         * Creates a plain object from a TruncWhisperKeysCollection message. Also converts values to other types if specified.
         * @param message TruncWhisperKeysCollection
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.TruncWhisperKeysCollection, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this TruncWhisperKeysCollection to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
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
            XX_FALLBACK_B = 6,
            KK_A = 7,
            KK_B = 8
        }
    }

    /** Properties of a DeleteAccount. */
    interface IDeleteAccount {

        /** DeleteAccount phone */
        phone?: (string|null);

        /** DeleteAccount reason */
        reason?: (server.DeleteAccount.Reason|null);

        /** DeleteAccount feedback */
        feedback?: (string|null);

        /** DeleteAccount username */
        username?: (string|null);
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

        /** DeleteAccount reason. */
        public reason: server.DeleteAccount.Reason;

        /** DeleteAccount feedback. */
        public feedback: string;

        /** DeleteAccount username. */
        public username: string;

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

    namespace DeleteAccount {

        /** Reason enum. */
        enum Reason {
            UNKNOWN_DELETE_REASON = 0
        }
    }

    /** Properties of an ExportData. */
    interface IExportData {

        /** ExportData dataReadyTs */
        dataReadyTs?: (number|Long|null);

        /** ExportData status */
        status?: (server.ExportData.Status|null);

        /** ExportData dataUrl */
        dataUrl?: (string|null);

        /** ExportData availableUntilTs */
        availableUntilTs?: (number|Long|null);
    }

    /** Represents an ExportData. */
    class ExportData implements IExportData {

        /**
         * Constructs a new ExportData.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IExportData);

        /** ExportData dataReadyTs. */
        public dataReadyTs: (number|Long);

        /** ExportData status. */
        public status: server.ExportData.Status;

        /** ExportData dataUrl. */
        public dataUrl: string;

        /** ExportData availableUntilTs. */
        public availableUntilTs: (number|Long);

        /**
         * Creates a new ExportData instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ExportData instance
         */
        public static create(properties?: server.IExportData): server.ExportData;

        /**
         * Encodes the specified ExportData message. Does not implicitly {@link server.ExportData.verify|verify} messages.
         * @param message ExportData message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IExportData, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ExportData message, length delimited. Does not implicitly {@link server.ExportData.verify|verify} messages.
         * @param message ExportData message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IExportData, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an ExportData message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ExportData
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ExportData;

        /**
         * Decodes an ExportData message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ExportData
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ExportData;

        /**
         * Verifies an ExportData message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an ExportData message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ExportData
         */
        public static fromObject(object: { [k: string]: any }): server.ExportData;

        /**
         * Creates a plain object from an ExportData message. Also converts values to other types if specified.
         * @param message ExportData
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ExportData, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ExportData to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ExportData {

        /** Status enum. */
        enum Status {
            UNKNOWN = 0,
            PENDING = 1,
            READY = 2,
            NOT_STARTED = 3
        }
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

    /** Properties of an IdentityKey. */
    interface IIdentityKey {

        /** IdentityKey publicKey */
        publicKey?: (Uint8Array|null);
    }

    /** Represents an IdentityKey. */
    class IdentityKey implements IIdentityKey {

        /**
         * Constructs a new IdentityKey.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IIdentityKey);

        /** IdentityKey publicKey. */
        public publicKey: Uint8Array;

        /**
         * Creates a new IdentityKey instance using the specified properties.
         * @param [properties] Properties to set
         * @returns IdentityKey instance
         */
        public static create(properties?: server.IIdentityKey): server.IdentityKey;

        /**
         * Encodes the specified IdentityKey message. Does not implicitly {@link server.IdentityKey.verify|verify} messages.
         * @param message IdentityKey message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IIdentityKey, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified IdentityKey message, length delimited. Does not implicitly {@link server.IdentityKey.verify|verify} messages.
         * @param message IdentityKey message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IIdentityKey, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an IdentityKey message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns IdentityKey
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.IdentityKey;

        /**
         * Decodes an IdentityKey message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns IdentityKey
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.IdentityKey;

        /**
         * Verifies an IdentityKey message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an IdentityKey message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns IdentityKey
         */
        public static fromObject(object: { [k: string]: any }): server.IdentityKey;

        /**
         * Creates a plain object from an IdentityKey message. Also converts values to other types if specified.
         * @param message IdentityKey
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.IdentityKey, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this IdentityKey to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a RequestLogs. */
    interface IRequestLogs {

        /** RequestLogs timestamp */
        timestamp?: (number|Long|null);
    }

    /** Represents a RequestLogs. */
    class RequestLogs implements IRequestLogs {

        /**
         * Constructs a new RequestLogs.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IRequestLogs);

        /** RequestLogs timestamp. */
        public timestamp: (number|Long);

        /**
         * Creates a new RequestLogs instance using the specified properties.
         * @param [properties] Properties to set
         * @returns RequestLogs instance
         */
        public static create(properties?: server.IRequestLogs): server.RequestLogs;

        /**
         * Encodes the specified RequestLogs message. Does not implicitly {@link server.RequestLogs.verify|verify} messages.
         * @param message RequestLogs message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IRequestLogs, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified RequestLogs message, length delimited. Does not implicitly {@link server.RequestLogs.verify|verify} messages.
         * @param message RequestLogs message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IRequestLogs, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a RequestLogs message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns RequestLogs
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.RequestLogs;

        /**
         * Decodes a RequestLogs message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns RequestLogs
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.RequestLogs;

        /**
         * Verifies a RequestLogs message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a RequestLogs message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns RequestLogs
         */
        public static fromObject(object: { [k: string]: any }): server.RequestLogs;

        /**
         * Creates a plain object from a RequestLogs message. Also converts values to other types if specified.
         * @param message RequestLogs
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.RequestLogs, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this RequestLogs to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a RegisterRequest. */
    interface IRegisterRequest {

        /** RegisterRequest otpRequest */
        otpRequest?: (server.IOtpRequest|null);

        /** RegisterRequest verifyRequest */
        verifyRequest?: (server.IVerifyOtpRequest|null);

        /** RegisterRequest hashcashRequest */
        hashcashRequest?: (server.IHashcashRequest|null);
    }

    /** Represents a RegisterRequest. */
    class RegisterRequest implements IRegisterRequest {

        /**
         * Constructs a new RegisterRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IRegisterRequest);

        /** RegisterRequest otpRequest. */
        public otpRequest?: (server.IOtpRequest|null);

        /** RegisterRequest verifyRequest. */
        public verifyRequest?: (server.IVerifyOtpRequest|null);

        /** RegisterRequest hashcashRequest. */
        public hashcashRequest?: (server.IHashcashRequest|null);

        /** RegisterRequest request. */
        public request?: ("otpRequest"|"verifyRequest"|"hashcashRequest");

        /**
         * Creates a new RegisterRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns RegisterRequest instance
         */
        public static create(properties?: server.IRegisterRequest): server.RegisterRequest;

        /**
         * Encodes the specified RegisterRequest message. Does not implicitly {@link server.RegisterRequest.verify|verify} messages.
         * @param message RegisterRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IRegisterRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified RegisterRequest message, length delimited. Does not implicitly {@link server.RegisterRequest.verify|verify} messages.
         * @param message RegisterRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IRegisterRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a RegisterRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns RegisterRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.RegisterRequest;

        /**
         * Decodes a RegisterRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns RegisterRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.RegisterRequest;

        /**
         * Verifies a RegisterRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a RegisterRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns RegisterRequest
         */
        public static fromObject(object: { [k: string]: any }): server.RegisterRequest;

        /**
         * Creates a plain object from a RegisterRequest message. Also converts values to other types if specified.
         * @param message RegisterRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.RegisterRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this RegisterRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a RegisterResponse. */
    interface IRegisterResponse {

        /** RegisterResponse otpResponse */
        otpResponse?: (server.IOtpResponse|null);

        /** RegisterResponse verifyResponse */
        verifyResponse?: (server.IVerifyOtpResponse|null);

        /** RegisterResponse hashcashResponse */
        hashcashResponse?: (server.IHashcashResponse|null);
    }

    /** Represents a RegisterResponse. */
    class RegisterResponse implements IRegisterResponse {

        /**
         * Constructs a new RegisterResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IRegisterResponse);

        /** RegisterResponse otpResponse. */
        public otpResponse?: (server.IOtpResponse|null);

        /** RegisterResponse verifyResponse. */
        public verifyResponse?: (server.IVerifyOtpResponse|null);

        /** RegisterResponse hashcashResponse. */
        public hashcashResponse?: (server.IHashcashResponse|null);

        /** RegisterResponse response. */
        public response?: ("otpResponse"|"verifyResponse"|"hashcashResponse");

        /**
         * Creates a new RegisterResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns RegisterResponse instance
         */
        public static create(properties?: server.IRegisterResponse): server.RegisterResponse;

        /**
         * Encodes the specified RegisterResponse message. Does not implicitly {@link server.RegisterResponse.verify|verify} messages.
         * @param message RegisterResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IRegisterResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified RegisterResponse message, length delimited. Does not implicitly {@link server.RegisterResponse.verify|verify} messages.
         * @param message RegisterResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IRegisterResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a RegisterResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns RegisterResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.RegisterResponse;

        /**
         * Decodes a RegisterResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns RegisterResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.RegisterResponse;

        /**
         * Verifies a RegisterResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a RegisterResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns RegisterResponse
         */
        public static fromObject(object: { [k: string]: any }): server.RegisterResponse;

        /**
         * Creates a plain object from a RegisterResponse message. Also converts values to other types if specified.
         * @param message RegisterResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.RegisterResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this RegisterResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a HashcashRequest. */
    interface IHashcashRequest {

        /** HashcashRequest countryCode */
        countryCode?: (string|null);
    }

    /** Represents a HashcashRequest. */
    class HashcashRequest implements IHashcashRequest {

        /**
         * Constructs a new HashcashRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IHashcashRequest);

        /** HashcashRequest countryCode. */
        public countryCode: string;

        /**
         * Creates a new HashcashRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns HashcashRequest instance
         */
        public static create(properties?: server.IHashcashRequest): server.HashcashRequest;

        /**
         * Encodes the specified HashcashRequest message. Does not implicitly {@link server.HashcashRequest.verify|verify} messages.
         * @param message HashcashRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IHashcashRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified HashcashRequest message, length delimited. Does not implicitly {@link server.HashcashRequest.verify|verify} messages.
         * @param message HashcashRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IHashcashRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a HashcashRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns HashcashRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.HashcashRequest;

        /**
         * Decodes a HashcashRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns HashcashRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.HashcashRequest;

        /**
         * Verifies a HashcashRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a HashcashRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns HashcashRequest
         */
        public static fromObject(object: { [k: string]: any }): server.HashcashRequest;

        /**
         * Creates a plain object from a HashcashRequest message. Also converts values to other types if specified.
         * @param message HashcashRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.HashcashRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this HashcashRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a HashcashResponse. */
    interface IHashcashResponse {

        /** HashcashResponse hashcashChallenge */
        hashcashChallenge?: (string|null);

        /** HashcashResponse isPhoneNotNeeded */
        isPhoneNotNeeded?: (boolean|null);
    }

    /** Represents a HashcashResponse. */
    class HashcashResponse implements IHashcashResponse {

        /**
         * Constructs a new HashcashResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IHashcashResponse);

        /** HashcashResponse hashcashChallenge. */
        public hashcashChallenge: string;

        /** HashcashResponse isPhoneNotNeeded. */
        public isPhoneNotNeeded: boolean;

        /**
         * Creates a new HashcashResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns HashcashResponse instance
         */
        public static create(properties?: server.IHashcashResponse): server.HashcashResponse;

        /**
         * Encodes the specified HashcashResponse message. Does not implicitly {@link server.HashcashResponse.verify|verify} messages.
         * @param message HashcashResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IHashcashResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified HashcashResponse message, length delimited. Does not implicitly {@link server.HashcashResponse.verify|verify} messages.
         * @param message HashcashResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IHashcashResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a HashcashResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns HashcashResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.HashcashResponse;

        /**
         * Decodes a HashcashResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns HashcashResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.HashcashResponse;

        /**
         * Verifies a HashcashResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a HashcashResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns HashcashResponse
         */
        public static fromObject(object: { [k: string]: any }): server.HashcashResponse;

        /**
         * Creates a plain object from a HashcashResponse message. Also converts values to other types if specified.
         * @param message HashcashResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.HashcashResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this HashcashResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an OtpRequest. */
    interface IOtpRequest {

        /** OtpRequest phone */
        phone?: (string|null);

        /** OtpRequest method */
        method?: (server.OtpRequest.Method|null);

        /** OtpRequest langId */
        langId?: (string|null);

        /** OtpRequest groupInviteToken */
        groupInviteToken?: (string|null);

        /** OtpRequest userAgent */
        userAgent?: (string|null);

        /** OtpRequest hashcashSolution */
        hashcashSolution?: (string|null);

        /** OtpRequest hashcashSolutionTimeTakenMs */
        hashcashSolutionTimeTakenMs?: (number|Long|null);

        /** OtpRequest campaignId */
        campaignId?: (string|null);
    }

    /** Represents an OtpRequest. */
    class OtpRequest implements IOtpRequest {

        /**
         * Constructs a new OtpRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IOtpRequest);

        /** OtpRequest phone. */
        public phone: string;

        /** OtpRequest method. */
        public method: server.OtpRequest.Method;

        /** OtpRequest langId. */
        public langId: string;

        /** OtpRequest groupInviteToken. */
        public groupInviteToken: string;

        /** OtpRequest userAgent. */
        public userAgent: string;

        /** OtpRequest hashcashSolution. */
        public hashcashSolution: string;

        /** OtpRequest hashcashSolutionTimeTakenMs. */
        public hashcashSolutionTimeTakenMs: (number|Long);

        /** OtpRequest campaignId. */
        public campaignId: string;

        /**
         * Creates a new OtpRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns OtpRequest instance
         */
        public static create(properties?: server.IOtpRequest): server.OtpRequest;

        /**
         * Encodes the specified OtpRequest message. Does not implicitly {@link server.OtpRequest.verify|verify} messages.
         * @param message OtpRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IOtpRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified OtpRequest message, length delimited. Does not implicitly {@link server.OtpRequest.verify|verify} messages.
         * @param message OtpRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IOtpRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an OtpRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns OtpRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.OtpRequest;

        /**
         * Decodes an OtpRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns OtpRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.OtpRequest;

        /**
         * Verifies an OtpRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an OtpRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns OtpRequest
         */
        public static fromObject(object: { [k: string]: any }): server.OtpRequest;

        /**
         * Creates a plain object from an OtpRequest message. Also converts values to other types if specified.
         * @param message OtpRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.OtpRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this OtpRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace OtpRequest {

        /** Method enum. */
        enum Method {
            SMS = 0,
            VOICE_CALL = 1
        }
    }

    /** Properties of an OtpResponse. */
    interface IOtpResponse {

        /** OtpResponse phone */
        phone?: (string|null);

        /** OtpResponse result */
        result?: (server.OtpResponse.Result|null);

        /** OtpResponse reason */
        reason?: (server.OtpResponse.Reason|null);

        /** OtpResponse retryAfterSecs */
        retryAfterSecs?: (number|Long|null);

        /** OtpResponse shouldVerifyNumber */
        shouldVerifyNumber?: (boolean|null);
    }

    /** Represents an OtpResponse. */
    class OtpResponse implements IOtpResponse {

        /**
         * Constructs a new OtpResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IOtpResponse);

        /** OtpResponse phone. */
        public phone: string;

        /** OtpResponse result. */
        public result: server.OtpResponse.Result;

        /** OtpResponse reason. */
        public reason: server.OtpResponse.Reason;

        /** OtpResponse retryAfterSecs. */
        public retryAfterSecs: (number|Long);

        /** OtpResponse shouldVerifyNumber. */
        public shouldVerifyNumber: boolean;

        /**
         * Creates a new OtpResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns OtpResponse instance
         */
        public static create(properties?: server.IOtpResponse): server.OtpResponse;

        /**
         * Encodes the specified OtpResponse message. Does not implicitly {@link server.OtpResponse.verify|verify} messages.
         * @param message OtpResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IOtpResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified OtpResponse message, length delimited. Does not implicitly {@link server.OtpResponse.verify|verify} messages.
         * @param message OtpResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IOtpResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an OtpResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns OtpResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.OtpResponse;

        /**
         * Decodes an OtpResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns OtpResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.OtpResponse;

        /**
         * Verifies an OtpResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an OtpResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns OtpResponse
         */
        public static fromObject(object: { [k: string]: any }): server.OtpResponse;

        /**
         * Creates a plain object from an OtpResponse message. Also converts values to other types if specified.
         * @param message OtpResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.OtpResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this OtpResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace OtpResponse {

        /** Result enum. */
        enum Result {
            UNKNOWN_RESULT = 0,
            SUCCESS = 1,
            FAILURE = 2
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            INVALID_PHONE_NUMBER = 1,
            INVALID_CLIENT_VERSION = 2,
            BAD_METHOD = 3,
            OTP_FAIL = 4,
            NOT_INVITED = 5,
            INVALID_GROUP_INVITE_TOKEN = 6,
            RETRIED_TOO_SOON = 7,
            BAD_REQUEST = 8,
            INTERNAL_SERVER_ERROR = 9,
            INVALID_HASHCASH_NONCE = 10,
            WRONG_HASHCASH_SOLUTION = 11,
            INVALID_COUNTRY_CODE = 12,
            INVALID_LENGTH = 13,
            LINE_TYPE_VOIP = 14,
            LINE_TYPE_FIXED = 15,
            LINE_TYPE_OTHER = 16
        }
    }

    /** Properties of a VerifyOtpRequest. */
    interface IVerifyOtpRequest {

        /** VerifyOtpRequest phone */
        phone?: (string|null);

        /** VerifyOtpRequest code */
        code?: (string|null);

        /** VerifyOtpRequest name */
        name?: (string|null);

        /** VerifyOtpRequest staticKey */
        staticKey?: (Uint8Array|null);

        /** VerifyOtpRequest signedPhrase */
        signedPhrase?: (Uint8Array|null);

        /** VerifyOtpRequest identityKey */
        identityKey?: (Uint8Array|null);

        /** VerifyOtpRequest signedKey */
        signedKey?: (Uint8Array|null);

        /** VerifyOtpRequest oneTimeKeys */
        oneTimeKeys?: (Uint8Array[]|null);

        /** VerifyOtpRequest groupInviteToken */
        groupInviteToken?: (string|null);

        /** VerifyOtpRequest pushRegister */
        pushRegister?: (server.IPushRegister|null);

        /** VerifyOtpRequest userAgent */
        userAgent?: (string|null);

        /** VerifyOtpRequest campaignId */
        campaignId?: (string|null);

        /** VerifyOtpRequest hashcashSolution */
        hashcashSolution?: (string|null);

        /** VerifyOtpRequest hashcashSolutionTimeTakenMs */
        hashcashSolutionTimeTakenMs?: (number|Long|null);
    }

    /** Represents a VerifyOtpRequest. */
    class VerifyOtpRequest implements IVerifyOtpRequest {

        /**
         * Constructs a new VerifyOtpRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IVerifyOtpRequest);

        /** VerifyOtpRequest phone. */
        public phone: string;

        /** VerifyOtpRequest code. */
        public code: string;

        /** VerifyOtpRequest name. */
        public name: string;

        /** VerifyOtpRequest staticKey. */
        public staticKey: Uint8Array;

        /** VerifyOtpRequest signedPhrase. */
        public signedPhrase: Uint8Array;

        /** VerifyOtpRequest identityKey. */
        public identityKey: Uint8Array;

        /** VerifyOtpRequest signedKey. */
        public signedKey: Uint8Array;

        /** VerifyOtpRequest oneTimeKeys. */
        public oneTimeKeys: Uint8Array[];

        /** VerifyOtpRequest groupInviteToken. */
        public groupInviteToken: string;

        /** VerifyOtpRequest pushRegister. */
        public pushRegister?: (server.IPushRegister|null);

        /** VerifyOtpRequest userAgent. */
        public userAgent: string;

        /** VerifyOtpRequest campaignId. */
        public campaignId: string;

        /** VerifyOtpRequest hashcashSolution. */
        public hashcashSolution: string;

        /** VerifyOtpRequest hashcashSolutionTimeTakenMs. */
        public hashcashSolutionTimeTakenMs: (number|Long);

        /**
         * Creates a new VerifyOtpRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns VerifyOtpRequest instance
         */
        public static create(properties?: server.IVerifyOtpRequest): server.VerifyOtpRequest;

        /**
         * Encodes the specified VerifyOtpRequest message. Does not implicitly {@link server.VerifyOtpRequest.verify|verify} messages.
         * @param message VerifyOtpRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IVerifyOtpRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified VerifyOtpRequest message, length delimited. Does not implicitly {@link server.VerifyOtpRequest.verify|verify} messages.
         * @param message VerifyOtpRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IVerifyOtpRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a VerifyOtpRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns VerifyOtpRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.VerifyOtpRequest;

        /**
         * Decodes a VerifyOtpRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns VerifyOtpRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.VerifyOtpRequest;

        /**
         * Verifies a VerifyOtpRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a VerifyOtpRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns VerifyOtpRequest
         */
        public static fromObject(object: { [k: string]: any }): server.VerifyOtpRequest;

        /**
         * Creates a plain object from a VerifyOtpRequest message. Also converts values to other types if specified.
         * @param message VerifyOtpRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.VerifyOtpRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this VerifyOtpRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a VerifyOtpResponse. */
    interface IVerifyOtpResponse {

        /** VerifyOtpResponse phone */
        phone?: (string|null);

        /** VerifyOtpResponse uid */
        uid?: (number|Long|null);

        /** VerifyOtpResponse name */
        name?: (string|null);

        /** VerifyOtpResponse result */
        result?: (server.VerifyOtpResponse.Result|null);

        /** VerifyOtpResponse reason */
        reason?: (server.VerifyOtpResponse.Reason|null);

        /** VerifyOtpResponse groupInviteResult */
        groupInviteResult?: (string|null);

        /** VerifyOtpResponse username */
        username?: (string|null);
    }

    /** Represents a VerifyOtpResponse. */
    class VerifyOtpResponse implements IVerifyOtpResponse {

        /**
         * Constructs a new VerifyOtpResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IVerifyOtpResponse);

        /** VerifyOtpResponse phone. */
        public phone: string;

        /** VerifyOtpResponse uid. */
        public uid: (number|Long);

        /** VerifyOtpResponse name. */
        public name: string;

        /** VerifyOtpResponse result. */
        public result: server.VerifyOtpResponse.Result;

        /** VerifyOtpResponse reason. */
        public reason: server.VerifyOtpResponse.Reason;

        /** VerifyOtpResponse groupInviteResult. */
        public groupInviteResult: string;

        /** VerifyOtpResponse username. */
        public username: string;

        /**
         * Creates a new VerifyOtpResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns VerifyOtpResponse instance
         */
        public static create(properties?: server.IVerifyOtpResponse): server.VerifyOtpResponse;

        /**
         * Encodes the specified VerifyOtpResponse message. Does not implicitly {@link server.VerifyOtpResponse.verify|verify} messages.
         * @param message VerifyOtpResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IVerifyOtpResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified VerifyOtpResponse message, length delimited. Does not implicitly {@link server.VerifyOtpResponse.verify|verify} messages.
         * @param message VerifyOtpResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IVerifyOtpResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a VerifyOtpResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns VerifyOtpResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.VerifyOtpResponse;

        /**
         * Decodes a VerifyOtpResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns VerifyOtpResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.VerifyOtpResponse;

        /**
         * Verifies a VerifyOtpResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a VerifyOtpResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns VerifyOtpResponse
         */
        public static fromObject(object: { [k: string]: any }): server.VerifyOtpResponse;

        /**
         * Creates a plain object from a VerifyOtpResponse message. Also converts values to other types if specified.
         * @param message VerifyOtpResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.VerifyOtpResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this VerifyOtpResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace VerifyOtpResponse {

        /** Result enum. */
        enum Result {
            UNKNOWN_RESULT = 0,
            SUCCESS = 1,
            FAILURE = 2
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            INVALID_PHONE_NUMBER = 1,
            INVALID_CLIENT_VERSION = 2,
            WRONG_SMS_CODE = 3,
            MISSING_PHONE = 4,
            MISSING_CODE = 5,
            MISSING_NAME = 6,
            INVALID_NAME = 7,
            MISSING_IDENTITY_KEY = 8,
            MISSING_SIGNED_KEY = 9,
            MISSING_ONE_TIME_KEYS = 10,
            BAD_BASE64_KEY = 11,
            INVALID_ONE_TIME_KEYS = 12,
            TOO_FEW_ONE_TIME_KEYS = 13,
            TOO_MANY_ONE_TIME_KEYS = 14,
            TOO_BIG_IDENTITY_KEY = 15,
            TOO_BIG_SIGNED_KEY = 16,
            TOO_BIG_ONE_TIME_KEYS = 17,
            INVALID_S_ED_PUB = 18,
            INVALID_SIGNED_PHRASE = 19,
            UNABLE_TO_OPEN_SIGNED_PHRASE = 20,
            BAD_REQUEST = 21,
            INTERNAL_SERVER_ERROR = 22,
            INVALID_COUNTRY_CODE = 23,
            INVALID_LENGTH = 24,
            LINE_TYPE_VOIP = 25,
            LINE_TYPE_FIXED = 26,
            LINE_TYPE_OTHER = 27,
            WRONG_HASHCASH_SOLUTION = 28
        }
    }

    /** Properties of a ClientOtpRequest. */
    interface IClientOtpRequest {

        /** ClientOtpRequest method */
        method?: (server.ClientOtpRequest.Method|null);

        /** ClientOtpRequest phone */
        phone?: (string|null);

        /** ClientOtpRequest content */
        content?: (string|null);
    }

    /** Represents a ClientOtpRequest. */
    class ClientOtpRequest implements IClientOtpRequest {

        /**
         * Constructs a new ClientOtpRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IClientOtpRequest);

        /** ClientOtpRequest method. */
        public method: server.ClientOtpRequest.Method;

        /** ClientOtpRequest phone. */
        public phone: string;

        /** ClientOtpRequest content. */
        public content: string;

        /**
         * Creates a new ClientOtpRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ClientOtpRequest instance
         */
        public static create(properties?: server.IClientOtpRequest): server.ClientOtpRequest;

        /**
         * Encodes the specified ClientOtpRequest message. Does not implicitly {@link server.ClientOtpRequest.verify|verify} messages.
         * @param message ClientOtpRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IClientOtpRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ClientOtpRequest message, length delimited. Does not implicitly {@link server.ClientOtpRequest.verify|verify} messages.
         * @param message ClientOtpRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IClientOtpRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ClientOtpRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ClientOtpRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ClientOtpRequest;

        /**
         * Decodes a ClientOtpRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ClientOtpRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ClientOtpRequest;

        /**
         * Verifies a ClientOtpRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ClientOtpRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ClientOtpRequest
         */
        public static fromObject(object: { [k: string]: any }): server.ClientOtpRequest;

        /**
         * Creates a plain object from a ClientOtpRequest message. Also converts values to other types if specified.
         * @param message ClientOtpRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ClientOtpRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ClientOtpRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ClientOtpRequest {

        /** Method enum. */
        enum Method {
            SMS = 0,
            VOICE_CALL = 1
        }
    }

    /** Properties of a ClientOtpResponse. */
    interface IClientOtpResponse {

        /** ClientOtpResponse result */
        result?: (server.ClientOtpResponse.Result|null);

        /** ClientOtpResponse reason */
        reason?: (server.ClientOtpResponse.Reason|null);
    }

    /** Represents a ClientOtpResponse. */
    class ClientOtpResponse implements IClientOtpResponse {

        /**
         * Constructs a new ClientOtpResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IClientOtpResponse);

        /** ClientOtpResponse result. */
        public result: server.ClientOtpResponse.Result;

        /** ClientOtpResponse reason. */
        public reason: server.ClientOtpResponse.Reason;

        /**
         * Creates a new ClientOtpResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ClientOtpResponse instance
         */
        public static create(properties?: server.IClientOtpResponse): server.ClientOtpResponse;

        /**
         * Encodes the specified ClientOtpResponse message. Does not implicitly {@link server.ClientOtpResponse.verify|verify} messages.
         * @param message ClientOtpResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IClientOtpResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ClientOtpResponse message, length delimited. Does not implicitly {@link server.ClientOtpResponse.verify|verify} messages.
         * @param message ClientOtpResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IClientOtpResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ClientOtpResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ClientOtpResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ClientOtpResponse;

        /**
         * Decodes a ClientOtpResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ClientOtpResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ClientOtpResponse;

        /**
         * Verifies a ClientOtpResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ClientOtpResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ClientOtpResponse
         */
        public static fromObject(object: { [k: string]: any }): server.ClientOtpResponse;

        /**
         * Creates a plain object from a ClientOtpResponse message. Also converts values to other types if specified.
         * @param message ClientOtpResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ClientOtpResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ClientOtpResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ClientOtpResponse {

        /** Result enum. */
        enum Result {
            UNKNOWN_RESULT = 0,
            SUCCESS = 1,
            FAILURE = 2
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            SETUP_ISSUE = 1,
            PERMISSION_ISSUE = 2,
            FORMATTING_ISSUE = 3,
            NETWORKING_ISSUE = 4
        }
    }

    /** Properties of a WakeUp. */
    interface IWakeUp {

        /** WakeUp alertType */
        alertType?: (server.WakeUp.AlertType|null);
    }

    /** Represents a WakeUp. */
    class WakeUp implements IWakeUp {

        /**
         * Constructs a new WakeUp.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IWakeUp);

        /** WakeUp alertType. */
        public alertType: server.WakeUp.AlertType;

        /**
         * Creates a new WakeUp instance using the specified properties.
         * @param [properties] Properties to set
         * @returns WakeUp instance
         */
        public static create(properties?: server.IWakeUp): server.WakeUp;

        /**
         * Encodes the specified WakeUp message. Does not implicitly {@link server.WakeUp.verify|verify} messages.
         * @param message WakeUp message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IWakeUp, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified WakeUp message, length delimited. Does not implicitly {@link server.WakeUp.verify|verify} messages.
         * @param message WakeUp message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IWakeUp, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a WakeUp message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns WakeUp
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.WakeUp;

        /**
         * Decodes a WakeUp message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns WakeUp
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.WakeUp;

        /**
         * Verifies a WakeUp message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a WakeUp message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns WakeUp
         */
        public static fromObject(object: { [k: string]: any }): server.WakeUp;

        /**
         * Creates a plain object from a WakeUp message. Also converts values to other types if specified.
         * @param message WakeUp
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.WakeUp, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this WakeUp to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace WakeUp {

        /** AlertType enum. */
        enum AlertType {
            ALERT = 0,
            SILENT = 1
        }
    }

    /** Properties of a MarketingAlert. */
    interface IMarketingAlert {

        /** MarketingAlert type */
        type?: (server.MarketingAlert.Type|null);
    }

    /** Represents a MarketingAlert. */
    class MarketingAlert implements IMarketingAlert {

        /**
         * Constructs a new MarketingAlert.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMarketingAlert);

        /** MarketingAlert type. */
        public type: server.MarketingAlert.Type;

        /**
         * Creates a new MarketingAlert instance using the specified properties.
         * @param [properties] Properties to set
         * @returns MarketingAlert instance
         */
        public static create(properties?: server.IMarketingAlert): server.MarketingAlert;

        /**
         * Encodes the specified MarketingAlert message. Does not implicitly {@link server.MarketingAlert.verify|verify} messages.
         * @param message MarketingAlert message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMarketingAlert, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified MarketingAlert message, length delimited. Does not implicitly {@link server.MarketingAlert.verify|verify} messages.
         * @param message MarketingAlert message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMarketingAlert, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a MarketingAlert message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns MarketingAlert
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.MarketingAlert;

        /**
         * Decodes a MarketingAlert message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns MarketingAlert
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.MarketingAlert;

        /**
         * Verifies a MarketingAlert message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a MarketingAlert message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns MarketingAlert
         */
        public static fromObject(object: { [k: string]: any }): server.MarketingAlert;

        /**
         * Creates a plain object from a MarketingAlert message. Also converts values to other types if specified.
         * @param message MarketingAlert
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.MarketingAlert, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this MarketingAlert to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace MarketingAlert {

        /** Type enum. */
        enum Type {
            UNKNOWN = 0,
            INVITE_FRIENDS = 1,
            SHARE_POST = 2
        }
    }

    /** FollowStatus enum. */
    enum FollowStatus {
        NONE = 0,
        PENDING = 1,
        FOLLOWING = 2
    }

    /** Properties of a BasicUserProfile. */
    interface IBasicUserProfile {

        /** BasicUserProfile uid */
        uid?: (number|Long|null);

        /** BasicUserProfile username */
        username?: (string|null);

        /** BasicUserProfile name */
        name?: (string|null);

        /** BasicUserProfile avatarId */
        avatarId?: (string|null);

        /** BasicUserProfile followerStatus */
        followerStatus?: (server.FollowStatus|null);

        /** BasicUserProfile followingStatus */
        followingStatus?: (server.FollowStatus|null);

        /** BasicUserProfile numMutualFollowing */
        numMutualFollowing?: (number|null);

        /** BasicUserProfile blocked */
        blocked?: (boolean|null);

        /** BasicUserProfile geoTags */
        geoTags?: (string[]|null);
    }

    /** Represents a BasicUserProfile. */
    class BasicUserProfile implements IBasicUserProfile {

        /**
         * Constructs a new BasicUserProfile.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IBasicUserProfile);

        /** BasicUserProfile uid. */
        public uid: (number|Long);

        /** BasicUserProfile username. */
        public username: string;

        /** BasicUserProfile name. */
        public name: string;

        /** BasicUserProfile avatarId. */
        public avatarId: string;

        /** BasicUserProfile followerStatus. */
        public followerStatus: server.FollowStatus;

        /** BasicUserProfile followingStatus. */
        public followingStatus: server.FollowStatus;

        /** BasicUserProfile numMutualFollowing. */
        public numMutualFollowing: number;

        /** BasicUserProfile blocked. */
        public blocked: boolean;

        /** BasicUserProfile geoTags. */
        public geoTags: string[];

        /**
         * Creates a new BasicUserProfile instance using the specified properties.
         * @param [properties] Properties to set
         * @returns BasicUserProfile instance
         */
        public static create(properties?: server.IBasicUserProfile): server.BasicUserProfile;

        /**
         * Encodes the specified BasicUserProfile message. Does not implicitly {@link server.BasicUserProfile.verify|verify} messages.
         * @param message BasicUserProfile message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IBasicUserProfile, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified BasicUserProfile message, length delimited. Does not implicitly {@link server.BasicUserProfile.verify|verify} messages.
         * @param message BasicUserProfile message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IBasicUserProfile, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a BasicUserProfile message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns BasicUserProfile
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.BasicUserProfile;

        /**
         * Decodes a BasicUserProfile message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns BasicUserProfile
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.BasicUserProfile;

        /**
         * Verifies a BasicUserProfile message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a BasicUserProfile message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns BasicUserProfile
         */
        public static fromObject(object: { [k: string]: any }): server.BasicUserProfile;

        /**
         * Creates a plain object from a BasicUserProfile message. Also converts values to other types if specified.
         * @param message BasicUserProfile
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.BasicUserProfile, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this BasicUserProfile to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a UserProfile. */
    interface IUserProfile {

        /** UserProfile uid */
        uid?: (number|Long|null);

        /** UserProfile username */
        username?: (string|null);

        /** UserProfile name */
        name?: (string|null);

        /** UserProfile avatarId */
        avatarId?: (string|null);

        /** UserProfile followerStatus */
        followerStatus?: (server.FollowStatus|null);

        /** UserProfile followingStatus */
        followingStatus?: (server.FollowStatus|null);

        /** UserProfile numMutualFollowing */
        numMutualFollowing?: (number|null);

        /** UserProfile bio */
        bio?: (string|null);

        /** UserProfile links */
        links?: (server.ILink[]|null);

        /** UserProfile relevantFollowers */
        relevantFollowers?: (server.IBasicUserProfile[]|null);

        /** UserProfile blocked */
        blocked?: (boolean|null);

        /** UserProfile totalPostImpressions */
        totalPostImpressions?: (number|null);

        /** UserProfile totalPostReactions */
        totalPostReactions?: (number|null);

        /** UserProfile totalNumPosts */
        totalNumPosts?: (number|null);

        /** UserProfile geoTags */
        geoTags?: (string[]|null);
    }

    /** Represents a UserProfile. */
    class UserProfile implements IUserProfile {

        /**
         * Constructs a new UserProfile.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IUserProfile);

        /** UserProfile uid. */
        public uid: (number|Long);

        /** UserProfile username. */
        public username: string;

        /** UserProfile name. */
        public name: string;

        /** UserProfile avatarId. */
        public avatarId: string;

        /** UserProfile followerStatus. */
        public followerStatus: server.FollowStatus;

        /** UserProfile followingStatus. */
        public followingStatus: server.FollowStatus;

        /** UserProfile numMutualFollowing. */
        public numMutualFollowing: number;

        /** UserProfile bio. */
        public bio: string;

        /** UserProfile links. */
        public links: server.ILink[];

        /** UserProfile relevantFollowers. */
        public relevantFollowers: server.IBasicUserProfile[];

        /** UserProfile blocked. */
        public blocked: boolean;

        /** UserProfile totalPostImpressions. */
        public totalPostImpressions: number;

        /** UserProfile totalPostReactions. */
        public totalPostReactions: number;

        /** UserProfile totalNumPosts. */
        public totalNumPosts: number;

        /** UserProfile geoTags. */
        public geoTags: string[];

        /**
         * Creates a new UserProfile instance using the specified properties.
         * @param [properties] Properties to set
         * @returns UserProfile instance
         */
        public static create(properties?: server.IUserProfile): server.UserProfile;

        /**
         * Encodes the specified UserProfile message. Does not implicitly {@link server.UserProfile.verify|verify} messages.
         * @param message UserProfile message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IUserProfile, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified UserProfile message, length delimited. Does not implicitly {@link server.UserProfile.verify|verify} messages.
         * @param message UserProfile message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IUserProfile, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a UserProfile message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns UserProfile
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.UserProfile;

        /**
         * Decodes a UserProfile message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns UserProfile
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.UserProfile;

        /**
         * Verifies a UserProfile message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a UserProfile message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns UserProfile
         */
        public static fromObject(object: { [k: string]: any }): server.UserProfile;

        /**
         * Creates a plain object from a UserProfile message. Also converts values to other types if specified.
         * @param message UserProfile
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.UserProfile, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this UserProfile to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a PostMetrics. */
    interface IPostMetrics {

        /** PostMetrics numImpressions */
        numImpressions?: (number|null);
    }

    /** Represents a PostMetrics. */
    class PostMetrics implements IPostMetrics {

        /**
         * Constructs a new PostMetrics.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPostMetrics);

        /** PostMetrics numImpressions. */
        public numImpressions: number;

        /**
         * Creates a new PostMetrics instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PostMetrics instance
         */
        public static create(properties?: server.IPostMetrics): server.PostMetrics;

        /**
         * Encodes the specified PostMetrics message. Does not implicitly {@link server.PostMetrics.verify|verify} messages.
         * @param message PostMetrics message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPostMetrics, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PostMetrics message, length delimited. Does not implicitly {@link server.PostMetrics.verify|verify} messages.
         * @param message PostMetrics message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPostMetrics, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PostMetrics message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PostMetrics
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PostMetrics;

        /**
         * Decodes a PostMetrics message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PostMetrics
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PostMetrics;

        /**
         * Verifies a PostMetrics message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PostMetrics message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PostMetrics
         */
        public static fromObject(object: { [k: string]: any }): server.PostMetrics;

        /**
         * Creates a plain object from a PostMetrics message. Also converts values to other types if specified.
         * @param message PostMetrics
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PostMetrics, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PostMetrics to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a ProfileUpdate. */
    interface IProfileUpdate {

        /** ProfileUpdate type */
        type?: (server.ProfileUpdate.Type|null);

        /** ProfileUpdate profile */
        profile?: (server.IBasicUserProfile|null);
    }

    /** Represents a ProfileUpdate. */
    class ProfileUpdate implements IProfileUpdate {

        /**
         * Constructs a new ProfileUpdate.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IProfileUpdate);

        /** ProfileUpdate type. */
        public type: server.ProfileUpdate.Type;

        /** ProfileUpdate profile. */
        public profile?: (server.IBasicUserProfile|null);

        /**
         * Creates a new ProfileUpdate instance using the specified properties.
         * @param [properties] Properties to set
         * @returns ProfileUpdate instance
         */
        public static create(properties?: server.IProfileUpdate): server.ProfileUpdate;

        /**
         * Encodes the specified ProfileUpdate message. Does not implicitly {@link server.ProfileUpdate.verify|verify} messages.
         * @param message ProfileUpdate message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IProfileUpdate, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified ProfileUpdate message, length delimited. Does not implicitly {@link server.ProfileUpdate.verify|verify} messages.
         * @param message ProfileUpdate message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IProfileUpdate, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a ProfileUpdate message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns ProfileUpdate
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.ProfileUpdate;

        /**
         * Decodes a ProfileUpdate message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns ProfileUpdate
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.ProfileUpdate;

        /**
         * Verifies a ProfileUpdate message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a ProfileUpdate message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns ProfileUpdate
         */
        public static fromObject(object: { [k: string]: any }): server.ProfileUpdate;

        /**
         * Creates a plain object from a ProfileUpdate message. Also converts values to other types if specified.
         * @param message ProfileUpdate
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.ProfileUpdate, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this ProfileUpdate to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace ProfileUpdate {

        /** Type enum. */
        enum Type {
            NORMAL = 0,
            DELETE = 1,
            FOLLOWER_NOTICE = 2,
            CONTACT_NOTICE = 3
        }
    }

    /** Properties of a UserProfileRequest. */
    interface IUserProfileRequest {

        /** UserProfileRequest uid */
        uid?: (number|Long|null);

        /** UserProfileRequest username */
        username?: (string|null);
    }

    /** Represents a UserProfileRequest. */
    class UserProfileRequest implements IUserProfileRequest {

        /**
         * Constructs a new UserProfileRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IUserProfileRequest);

        /** UserProfileRequest uid. */
        public uid: (number|Long);

        /** UserProfileRequest username. */
        public username: string;

        /**
         * Creates a new UserProfileRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns UserProfileRequest instance
         */
        public static create(properties?: server.IUserProfileRequest): server.UserProfileRequest;

        /**
         * Encodes the specified UserProfileRequest message. Does not implicitly {@link server.UserProfileRequest.verify|verify} messages.
         * @param message UserProfileRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IUserProfileRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified UserProfileRequest message, length delimited. Does not implicitly {@link server.UserProfileRequest.verify|verify} messages.
         * @param message UserProfileRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IUserProfileRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a UserProfileRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns UserProfileRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.UserProfileRequest;

        /**
         * Decodes a UserProfileRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns UserProfileRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.UserProfileRequest;

        /**
         * Verifies a UserProfileRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a UserProfileRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns UserProfileRequest
         */
        public static fromObject(object: { [k: string]: any }): server.UserProfileRequest;

        /**
         * Creates a plain object from a UserProfileRequest message. Also converts values to other types if specified.
         * @param message UserProfileRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.UserProfileRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this UserProfileRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a UserProfileResult. */
    interface IUserProfileResult {

        /** UserProfileResult result */
        result?: (server.UserProfileResult.Result|null);

        /** UserProfileResult reason */
        reason?: (server.UserProfileResult.Reason|null);

        /** UserProfileResult profile */
        profile?: (server.IUserProfile|null);

        /** UserProfileResult recentPosts */
        recentPosts?: (server.IPost[]|null);
    }

    /** Represents a UserProfileResult. */
    class UserProfileResult implements IUserProfileResult {

        /**
         * Constructs a new UserProfileResult.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IUserProfileResult);

        /** UserProfileResult result. */
        public result: server.UserProfileResult.Result;

        /** UserProfileResult reason. */
        public reason: server.UserProfileResult.Reason;

        /** UserProfileResult profile. */
        public profile?: (server.IUserProfile|null);

        /** UserProfileResult recentPosts. */
        public recentPosts: server.IPost[];

        /**
         * Creates a new UserProfileResult instance using the specified properties.
         * @param [properties] Properties to set
         * @returns UserProfileResult instance
         */
        public static create(properties?: server.IUserProfileResult): server.UserProfileResult;

        /**
         * Encodes the specified UserProfileResult message. Does not implicitly {@link server.UserProfileResult.verify|verify} messages.
         * @param message UserProfileResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IUserProfileResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified UserProfileResult message, length delimited. Does not implicitly {@link server.UserProfileResult.verify|verify} messages.
         * @param message UserProfileResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IUserProfileResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a UserProfileResult message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns UserProfileResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.UserProfileResult;

        /**
         * Decodes a UserProfileResult message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns UserProfileResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.UserProfileResult;

        /**
         * Verifies a UserProfileResult message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a UserProfileResult message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns UserProfileResult
         */
        public static fromObject(object: { [k: string]: any }): server.UserProfileResult;

        /**
         * Creates a plain object from a UserProfileResult message. Also converts values to other types if specified.
         * @param message UserProfileResult
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.UserProfileResult, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this UserProfileResult to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace UserProfileResult {

        /** Result enum. */
        enum Result {
            OK = 0,
            FAIL = 1
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            NO_USER = 1
        }
    }

    /** Properties of a PostMetricsRequest. */
    interface IPostMetricsRequest {

        /** PostMetricsRequest postId */
        postId?: (string|null);
    }

    /** Represents a PostMetricsRequest. */
    class PostMetricsRequest implements IPostMetricsRequest {

        /**
         * Constructs a new PostMetricsRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPostMetricsRequest);

        /** PostMetricsRequest postId. */
        public postId: string;

        /**
         * Creates a new PostMetricsRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PostMetricsRequest instance
         */
        public static create(properties?: server.IPostMetricsRequest): server.PostMetricsRequest;

        /**
         * Encodes the specified PostMetricsRequest message. Does not implicitly {@link server.PostMetricsRequest.verify|verify} messages.
         * @param message PostMetricsRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPostMetricsRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PostMetricsRequest message, length delimited. Does not implicitly {@link server.PostMetricsRequest.verify|verify} messages.
         * @param message PostMetricsRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPostMetricsRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PostMetricsRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PostMetricsRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PostMetricsRequest;

        /**
         * Decodes a PostMetricsRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PostMetricsRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PostMetricsRequest;

        /**
         * Verifies a PostMetricsRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PostMetricsRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PostMetricsRequest
         */
        public static fromObject(object: { [k: string]: any }): server.PostMetricsRequest;

        /**
         * Creates a plain object from a PostMetricsRequest message. Also converts values to other types if specified.
         * @param message PostMetricsRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PostMetricsRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PostMetricsRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a PostMetricsResult. */
    interface IPostMetricsResult {

        /** PostMetricsResult result */
        result?: (server.PostMetricsResult.Result|null);

        /** PostMetricsResult reason */
        reason?: (server.PostMetricsResult.Reason|null);

        /** PostMetricsResult postMetrics */
        postMetrics?: (server.IPostMetrics|null);
    }

    /** Represents a PostMetricsResult. */
    class PostMetricsResult implements IPostMetricsResult {

        /**
         * Constructs a new PostMetricsResult.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IPostMetricsResult);

        /** PostMetricsResult result. */
        public result: server.PostMetricsResult.Result;

        /** PostMetricsResult reason. */
        public reason: server.PostMetricsResult.Reason;

        /** PostMetricsResult postMetrics. */
        public postMetrics?: (server.IPostMetrics|null);

        /**
         * Creates a new PostMetricsResult instance using the specified properties.
         * @param [properties] Properties to set
         * @returns PostMetricsResult instance
         */
        public static create(properties?: server.IPostMetricsResult): server.PostMetricsResult;

        /**
         * Encodes the specified PostMetricsResult message. Does not implicitly {@link server.PostMetricsResult.verify|verify} messages.
         * @param message PostMetricsResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IPostMetricsResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified PostMetricsResult message, length delimited. Does not implicitly {@link server.PostMetricsResult.verify|verify} messages.
         * @param message PostMetricsResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IPostMetricsResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a PostMetricsResult message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns PostMetricsResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.PostMetricsResult;

        /**
         * Decodes a PostMetricsResult message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns PostMetricsResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.PostMetricsResult;

        /**
         * Verifies a PostMetricsResult message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a PostMetricsResult message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns PostMetricsResult
         */
        public static fromObject(object: { [k: string]: any }): server.PostMetricsResult;

        /**
         * Creates a plain object from a PostMetricsResult message. Also converts values to other types if specified.
         * @param message PostMetricsResult
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.PostMetricsResult, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this PostMetricsResult to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace PostMetricsResult {

        /** Result enum. */
        enum Result {
            OK = 0,
            FAIL = 1
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            NO_POST = 1
        }
    }

    /** Properties of a RelationshipRequest. */
    interface IRelationshipRequest {

        /** RelationshipRequest action */
        action?: (server.RelationshipRequest.Action|null);

        /** RelationshipRequest uid */
        uid?: (number|Long|null);
    }

    /** Represents a RelationshipRequest. */
    class RelationshipRequest implements IRelationshipRequest {

        /**
         * Constructs a new RelationshipRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IRelationshipRequest);

        /** RelationshipRequest action. */
        public action: server.RelationshipRequest.Action;

        /** RelationshipRequest uid. */
        public uid: (number|Long);

        /**
         * Creates a new RelationshipRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns RelationshipRequest instance
         */
        public static create(properties?: server.IRelationshipRequest): server.RelationshipRequest;

        /**
         * Encodes the specified RelationshipRequest message. Does not implicitly {@link server.RelationshipRequest.verify|verify} messages.
         * @param message RelationshipRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IRelationshipRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified RelationshipRequest message, length delimited. Does not implicitly {@link server.RelationshipRequest.verify|verify} messages.
         * @param message RelationshipRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IRelationshipRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a RelationshipRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns RelationshipRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.RelationshipRequest;

        /**
         * Decodes a RelationshipRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns RelationshipRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.RelationshipRequest;

        /**
         * Verifies a RelationshipRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a RelationshipRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns RelationshipRequest
         */
        public static fromObject(object: { [k: string]: any }): server.RelationshipRequest;

        /**
         * Creates a plain object from a RelationshipRequest message. Also converts values to other types if specified.
         * @param message RelationshipRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.RelationshipRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this RelationshipRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace RelationshipRequest {

        /** Action enum. */
        enum Action {
            FOLLOW = 0,
            UNFOLLOW = 1,
            ACCEPT_FOLLOW = 2,
            IGNORE_FOLLOW = 3,
            REMOVE_FOLLOWER = 4,
            BLOCK = 5,
            UNBLOCK = 6
        }
    }

    /** Properties of a RelationshipResponse. */
    interface IRelationshipResponse {

        /** RelationshipResponse result */
        result?: (server.RelationshipResponse.Result|null);

        /** RelationshipResponse profile */
        profile?: (server.IBasicUserProfile|null);
    }

    /** Represents a RelationshipResponse. */
    class RelationshipResponse implements IRelationshipResponse {

        /**
         * Constructs a new RelationshipResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IRelationshipResponse);

        /** RelationshipResponse result. */
        public result: server.RelationshipResponse.Result;

        /** RelationshipResponse profile. */
        public profile?: (server.IBasicUserProfile|null);

        /**
         * Creates a new RelationshipResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns RelationshipResponse instance
         */
        public static create(properties?: server.IRelationshipResponse): server.RelationshipResponse;

        /**
         * Encodes the specified RelationshipResponse message. Does not implicitly {@link server.RelationshipResponse.verify|verify} messages.
         * @param message RelationshipResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IRelationshipResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified RelationshipResponse message, length delimited. Does not implicitly {@link server.RelationshipResponse.verify|verify} messages.
         * @param message RelationshipResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IRelationshipResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a RelationshipResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns RelationshipResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.RelationshipResponse;

        /**
         * Decodes a RelationshipResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns RelationshipResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.RelationshipResponse;

        /**
         * Verifies a RelationshipResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a RelationshipResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns RelationshipResponse
         */
        public static fromObject(object: { [k: string]: any }): server.RelationshipResponse;

        /**
         * Creates a plain object from a RelationshipResponse message. Also converts values to other types if specified.
         * @param message RelationshipResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.RelationshipResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this RelationshipResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace RelationshipResponse {

        /** Result enum. */
        enum Result {
            OK = 0,
            FAIL = 1
        }
    }

    /** Properties of a RelationshipList. */
    interface IRelationshipList {

        /** RelationshipList type */
        type?: (server.RelationshipList.Type|null);

        /** RelationshipList cursor */
        cursor?: (string|null);

        /** RelationshipList users */
        users?: (server.IBasicUserProfile[]|null);
    }

    /** Represents a RelationshipList. */
    class RelationshipList implements IRelationshipList {

        /**
         * Constructs a new RelationshipList.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IRelationshipList);

        /** RelationshipList type. */
        public type: server.RelationshipList.Type;

        /** RelationshipList cursor. */
        public cursor: string;

        /** RelationshipList users. */
        public users: server.IBasicUserProfile[];

        /**
         * Creates a new RelationshipList instance using the specified properties.
         * @param [properties] Properties to set
         * @returns RelationshipList instance
         */
        public static create(properties?: server.IRelationshipList): server.RelationshipList;

        /**
         * Encodes the specified RelationshipList message. Does not implicitly {@link server.RelationshipList.verify|verify} messages.
         * @param message RelationshipList message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IRelationshipList, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified RelationshipList message, length delimited. Does not implicitly {@link server.RelationshipList.verify|verify} messages.
         * @param message RelationshipList message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IRelationshipList, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a RelationshipList message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns RelationshipList
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.RelationshipList;

        /**
         * Decodes a RelationshipList message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns RelationshipList
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.RelationshipList;

        /**
         * Verifies a RelationshipList message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a RelationshipList message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns RelationshipList
         */
        public static fromObject(object: { [k: string]: any }): server.RelationshipList;

        /**
         * Creates a plain object from a RelationshipList message. Also converts values to other types if specified.
         * @param message RelationshipList
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.RelationshipList, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this RelationshipList to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace RelationshipList {

        /** Type enum. */
        enum Type {
            FOLLOWER = 0,
            FOLLOWING = 1,
            INCOMING = 2,
            OUTGOING = 3,
            BLOCKED = 4
        }
    }

    /** Properties of a UsernameRequest. */
    interface IUsernameRequest {

        /** UsernameRequest action */
        action?: (server.UsernameRequest.Action|null);

        /** UsernameRequest username */
        username?: (string|null);
    }

    /** Represents a UsernameRequest. */
    class UsernameRequest implements IUsernameRequest {

        /**
         * Constructs a new UsernameRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IUsernameRequest);

        /** UsernameRequest action. */
        public action: server.UsernameRequest.Action;

        /** UsernameRequest username. */
        public username: string;

        /**
         * Creates a new UsernameRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns UsernameRequest instance
         */
        public static create(properties?: server.IUsernameRequest): server.UsernameRequest;

        /**
         * Encodes the specified UsernameRequest message. Does not implicitly {@link server.UsernameRequest.verify|verify} messages.
         * @param message UsernameRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IUsernameRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified UsernameRequest message, length delimited. Does not implicitly {@link server.UsernameRequest.verify|verify} messages.
         * @param message UsernameRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IUsernameRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a UsernameRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns UsernameRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.UsernameRequest;

        /**
         * Decodes a UsernameRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns UsernameRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.UsernameRequest;

        /**
         * Verifies a UsernameRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a UsernameRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns UsernameRequest
         */
        public static fromObject(object: { [k: string]: any }): server.UsernameRequest;

        /**
         * Creates a plain object from a UsernameRequest message. Also converts values to other types if specified.
         * @param message UsernameRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.UsernameRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this UsernameRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace UsernameRequest {

        /** Action enum. */
        enum Action {
            IS_AVAILABLE = 0,
            SET = 1
        }
    }

    /** Properties of a UsernameResponse. */
    interface IUsernameResponse {

        /** UsernameResponse result */
        result?: (server.UsernameResponse.Result|null);

        /** UsernameResponse reason */
        reason?: (server.UsernameResponse.Reason|null);
    }

    /** Represents a UsernameResponse. */
    class UsernameResponse implements IUsernameResponse {

        /**
         * Constructs a new UsernameResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IUsernameResponse);

        /** UsernameResponse result. */
        public result: server.UsernameResponse.Result;

        /** UsernameResponse reason. */
        public reason: server.UsernameResponse.Reason;

        /**
         * Creates a new UsernameResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns UsernameResponse instance
         */
        public static create(properties?: server.IUsernameResponse): server.UsernameResponse;

        /**
         * Encodes the specified UsernameResponse message. Does not implicitly {@link server.UsernameResponse.verify|verify} messages.
         * @param message UsernameResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IUsernameResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified UsernameResponse message, length delimited. Does not implicitly {@link server.UsernameResponse.verify|verify} messages.
         * @param message UsernameResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IUsernameResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a UsernameResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns UsernameResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.UsernameResponse;

        /**
         * Decodes a UsernameResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns UsernameResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.UsernameResponse;

        /**
         * Verifies a UsernameResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a UsernameResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns UsernameResponse
         */
        public static fromObject(object: { [k: string]: any }): server.UsernameResponse;

        /**
         * Creates a plain object from a UsernameResponse message. Also converts values to other types if specified.
         * @param message UsernameResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.UsernameResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this UsernameResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace UsernameResponse {

        /** Result enum. */
        enum Result {
            OK = 0,
            FAIL = 1
        }

        /** Reason enum. */
        enum Reason {
            TOOSHORT = 0,
            TOOLONG = 1,
            BADEXPR = 2,
            NOTUNIQ = 3
        }
    }

    /** Properties of a GeoTagRequest. */
    interface IGeoTagRequest {

        /** GeoTagRequest action */
        action?: (server.GeoTagRequest.Action|null);

        /** GeoTagRequest gpsLocation */
        gpsLocation?: (server.IGpsLocation|null);

        /** GeoTagRequest geoTag */
        geoTag?: (string|null);
    }

    /** Represents a GeoTagRequest. */
    class GeoTagRequest implements IGeoTagRequest {

        /**
         * Constructs a new GeoTagRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGeoTagRequest);

        /** GeoTagRequest action. */
        public action: server.GeoTagRequest.Action;

        /** GeoTagRequest gpsLocation. */
        public gpsLocation?: (server.IGpsLocation|null);

        /** GeoTagRequest geoTag. */
        public geoTag: string;

        /**
         * Creates a new GeoTagRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GeoTagRequest instance
         */
        public static create(properties?: server.IGeoTagRequest): server.GeoTagRequest;

        /**
         * Encodes the specified GeoTagRequest message. Does not implicitly {@link server.GeoTagRequest.verify|verify} messages.
         * @param message GeoTagRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGeoTagRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GeoTagRequest message, length delimited. Does not implicitly {@link server.GeoTagRequest.verify|verify} messages.
         * @param message GeoTagRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGeoTagRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GeoTagRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GeoTagRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GeoTagRequest;

        /**
         * Decodes a GeoTagRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GeoTagRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GeoTagRequest;

        /**
         * Verifies a GeoTagRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GeoTagRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GeoTagRequest
         */
        public static fromObject(object: { [k: string]: any }): server.GeoTagRequest;

        /**
         * Creates a plain object from a GeoTagRequest message. Also converts values to other types if specified.
         * @param message GeoTagRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GeoTagRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GeoTagRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GeoTagRequest {

        /** Action enum. */
        enum Action {
            GET = 0,
            BLOCK = 1,
            FORCE_ADD = 2
        }
    }

    /** Properties of a GeoTagResponse. */
    interface IGeoTagResponse {

        /** GeoTagResponse result */
        result?: (server.GeoTagResponse.Result|null);

        /** GeoTagResponse reason */
        reason?: (server.GeoTagResponse.Reason|null);

        /** GeoTagResponse geoTags */
        geoTags?: (string[]|null);
    }

    /** Represents a GeoTagResponse. */
    class GeoTagResponse implements IGeoTagResponse {

        /**
         * Constructs a new GeoTagResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGeoTagResponse);

        /** GeoTagResponse result. */
        public result: server.GeoTagResponse.Result;

        /** GeoTagResponse reason. */
        public reason: server.GeoTagResponse.Reason;

        /** GeoTagResponse geoTags. */
        public geoTags: string[];

        /**
         * Creates a new GeoTagResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GeoTagResponse instance
         */
        public static create(properties?: server.IGeoTagResponse): server.GeoTagResponse;

        /**
         * Encodes the specified GeoTagResponse message. Does not implicitly {@link server.GeoTagResponse.verify|verify} messages.
         * @param message GeoTagResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGeoTagResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GeoTagResponse message, length delimited. Does not implicitly {@link server.GeoTagResponse.verify|verify} messages.
         * @param message GeoTagResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGeoTagResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GeoTagResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GeoTagResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GeoTagResponse;

        /**
         * Decodes a GeoTagResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GeoTagResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GeoTagResponse;

        /**
         * Verifies a GeoTagResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GeoTagResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GeoTagResponse
         */
        public static fromObject(object: { [k: string]: any }): server.GeoTagResponse;

        /**
         * Creates a plain object from a GeoTagResponse message. Also converts values to other types if specified.
         * @param message GeoTagResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GeoTagResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GeoTagResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GeoTagResponse {

        /** Result enum. */
        enum Result {
            OK = 0,
            FAIL = 1
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN = 0,
            INVALID_REQUEST = 1
        }
    }

    /** Properties of a SearchRequest. */
    interface ISearchRequest {

        /** SearchRequest usernameString */
        usernameString?: (string|null);
    }

    /** Represents a SearchRequest. */
    class SearchRequest implements ISearchRequest {

        /**
         * Constructs a new SearchRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISearchRequest);

        /** SearchRequest usernameString. */
        public usernameString: string;

        /**
         * Creates a new SearchRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SearchRequest instance
         */
        public static create(properties?: server.ISearchRequest): server.SearchRequest;

        /**
         * Encodes the specified SearchRequest message. Does not implicitly {@link server.SearchRequest.verify|verify} messages.
         * @param message SearchRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISearchRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SearchRequest message, length delimited. Does not implicitly {@link server.SearchRequest.verify|verify} messages.
         * @param message SearchRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISearchRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SearchRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SearchRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SearchRequest;

        /**
         * Decodes a SearchRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SearchRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SearchRequest;

        /**
         * Verifies a SearchRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SearchRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SearchRequest
         */
        public static fromObject(object: { [k: string]: any }): server.SearchRequest;

        /**
         * Creates a plain object from a SearchRequest message. Also converts values to other types if specified.
         * @param message SearchRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SearchRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SearchRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a SearchResponse. */
    interface ISearchResponse {

        /** SearchResponse result */
        result?: (server.SearchResponse.Result|null);

        /** SearchResponse searchResult */
        searchResult?: (server.IBasicUserProfile[]|null);
    }

    /** Represents a SearchResponse. */
    class SearchResponse implements ISearchResponse {

        /**
         * Constructs a new SearchResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISearchResponse);

        /** SearchResponse result. */
        public result: server.SearchResponse.Result;

        /** SearchResponse searchResult. */
        public searchResult: server.IBasicUserProfile[];

        /**
         * Creates a new SearchResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SearchResponse instance
         */
        public static create(properties?: server.ISearchResponse): server.SearchResponse;

        /**
         * Encodes the specified SearchResponse message. Does not implicitly {@link server.SearchResponse.verify|verify} messages.
         * @param message SearchResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISearchResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SearchResponse message, length delimited. Does not implicitly {@link server.SearchResponse.verify|verify} messages.
         * @param message SearchResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISearchResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SearchResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SearchResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SearchResponse;

        /**
         * Decodes a SearchResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SearchResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SearchResponse;

        /**
         * Verifies a SearchResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SearchResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SearchResponse
         */
        public static fromObject(object: { [k: string]: any }): server.SearchResponse;

        /**
         * Creates a plain object from a SearchResponse message. Also converts values to other types if specified.
         * @param message SearchResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SearchResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SearchResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace SearchResponse {

        /** Result enum. */
        enum Result {
            OK = 0,
            FAIL = 1
        }
    }

    /** Properties of a FollowSuggestionsRequest. */
    interface IFollowSuggestionsRequest {

        /** FollowSuggestionsRequest action */
        action?: (server.FollowSuggestionsRequest.Action|null);

        /** FollowSuggestionsRequest rejectedUids */
        rejectedUids?: ((number|Long)[]|null);
    }

    /** Represents a FollowSuggestionsRequest. */
    class FollowSuggestionsRequest implements IFollowSuggestionsRequest {

        /**
         * Constructs a new FollowSuggestionsRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IFollowSuggestionsRequest);

        /** FollowSuggestionsRequest action. */
        public action: server.FollowSuggestionsRequest.Action;

        /** FollowSuggestionsRequest rejectedUids. */
        public rejectedUids: (number|Long)[];

        /**
         * Creates a new FollowSuggestionsRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns FollowSuggestionsRequest instance
         */
        public static create(properties?: server.IFollowSuggestionsRequest): server.FollowSuggestionsRequest;

        /**
         * Encodes the specified FollowSuggestionsRequest message. Does not implicitly {@link server.FollowSuggestionsRequest.verify|verify} messages.
         * @param message FollowSuggestionsRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IFollowSuggestionsRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified FollowSuggestionsRequest message, length delimited. Does not implicitly {@link server.FollowSuggestionsRequest.verify|verify} messages.
         * @param message FollowSuggestionsRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IFollowSuggestionsRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a FollowSuggestionsRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns FollowSuggestionsRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.FollowSuggestionsRequest;

        /**
         * Decodes a FollowSuggestionsRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns FollowSuggestionsRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.FollowSuggestionsRequest;

        /**
         * Verifies a FollowSuggestionsRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a FollowSuggestionsRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns FollowSuggestionsRequest
         */
        public static fromObject(object: { [k: string]: any }): server.FollowSuggestionsRequest;

        /**
         * Creates a plain object from a FollowSuggestionsRequest message. Also converts values to other types if specified.
         * @param message FollowSuggestionsRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.FollowSuggestionsRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this FollowSuggestionsRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace FollowSuggestionsRequest {

        /** Action enum. */
        enum Action {
            GET = 0,
            REJECT = 1
        }
    }

    /** Properties of a SuggestedProfile. */
    interface ISuggestedProfile {

        /** SuggestedProfile userProfile */
        userProfile?: (server.IBasicUserProfile|null);

        /** SuggestedProfile reason */
        reason?: (server.SuggestedProfile.Reason|null);

        /** SuggestedProfile rank */
        rank?: (number|null);
    }

    /** Represents a SuggestedProfile. */
    class SuggestedProfile implements ISuggestedProfile {

        /**
         * Constructs a new SuggestedProfile.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISuggestedProfile);

        /** SuggestedProfile userProfile. */
        public userProfile?: (server.IBasicUserProfile|null);

        /** SuggestedProfile reason. */
        public reason: server.SuggestedProfile.Reason;

        /** SuggestedProfile rank. */
        public rank: number;

        /**
         * Creates a new SuggestedProfile instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SuggestedProfile instance
         */
        public static create(properties?: server.ISuggestedProfile): server.SuggestedProfile;

        /**
         * Encodes the specified SuggestedProfile message. Does not implicitly {@link server.SuggestedProfile.verify|verify} messages.
         * @param message SuggestedProfile message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISuggestedProfile, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SuggestedProfile message, length delimited. Does not implicitly {@link server.SuggestedProfile.verify|verify} messages.
         * @param message SuggestedProfile message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISuggestedProfile, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SuggestedProfile message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SuggestedProfile
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SuggestedProfile;

        /**
         * Decodes a SuggestedProfile message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SuggestedProfile
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SuggestedProfile;

        /**
         * Verifies a SuggestedProfile message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SuggestedProfile message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SuggestedProfile
         */
        public static fromObject(object: { [k: string]: any }): server.SuggestedProfile;

        /**
         * Creates a plain object from a SuggestedProfile message. Also converts values to other types if specified.
         * @param message SuggestedProfile
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SuggestedProfile, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SuggestedProfile to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace SuggestedProfile {

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            DIRECT_CONTACT = 1,
            FOF = 2,
            CAMPUS = 3
        }
    }

    /** Properties of a FollowSuggestionsResponse. */
    interface IFollowSuggestionsResponse {

        /** FollowSuggestionsResponse result */
        result?: (server.FollowSuggestionsResponse.Result|null);

        /** FollowSuggestionsResponse suggestedProfiles */
        suggestedProfiles?: (server.ISuggestedProfile[]|null);
    }

    /** Represents a FollowSuggestionsResponse. */
    class FollowSuggestionsResponse implements IFollowSuggestionsResponse {

        /**
         * Constructs a new FollowSuggestionsResponse.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IFollowSuggestionsResponse);

        /** FollowSuggestionsResponse result. */
        public result: server.FollowSuggestionsResponse.Result;

        /** FollowSuggestionsResponse suggestedProfiles. */
        public suggestedProfiles: server.ISuggestedProfile[];

        /**
         * Creates a new FollowSuggestionsResponse instance using the specified properties.
         * @param [properties] Properties to set
         * @returns FollowSuggestionsResponse instance
         */
        public static create(properties?: server.IFollowSuggestionsResponse): server.FollowSuggestionsResponse;

        /**
         * Encodes the specified FollowSuggestionsResponse message. Does not implicitly {@link server.FollowSuggestionsResponse.verify|verify} messages.
         * @param message FollowSuggestionsResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IFollowSuggestionsResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified FollowSuggestionsResponse message, length delimited. Does not implicitly {@link server.FollowSuggestionsResponse.verify|verify} messages.
         * @param message FollowSuggestionsResponse message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IFollowSuggestionsResponse, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a FollowSuggestionsResponse message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns FollowSuggestionsResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.FollowSuggestionsResponse;

        /**
         * Decodes a FollowSuggestionsResponse message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns FollowSuggestionsResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.FollowSuggestionsResponse;

        /**
         * Verifies a FollowSuggestionsResponse message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a FollowSuggestionsResponse message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns FollowSuggestionsResponse
         */
        public static fromObject(object: { [k: string]: any }): server.FollowSuggestionsResponse;

        /**
         * Creates a plain object from a FollowSuggestionsResponse message. Also converts values to other types if specified.
         * @param message FollowSuggestionsResponse
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.FollowSuggestionsResponse, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this FollowSuggestionsResponse to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace FollowSuggestionsResponse {

        /** Result enum. */
        enum Result {
            OK = 0,
            FAIL = 1
        }
    }

    /** Properties of a Link. */
    interface ILink {

        /** Link type */
        type?: (server.Link.Type|null);

        /** Link text */
        text?: (string|null);
    }

    /** Represents a Link. */
    class Link implements ILink {

        /**
         * Constructs a new Link.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ILink);

        /** Link type. */
        public type: server.Link.Type;

        /** Link text. */
        public text: string;

        /**
         * Creates a new Link instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Link instance
         */
        public static create(properties?: server.ILink): server.Link;

        /**
         * Encodes the specified Link message. Does not implicitly {@link server.Link.verify|verify} messages.
         * @param message Link message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ILink, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Link message, length delimited. Does not implicitly {@link server.Link.verify|verify} messages.
         * @param message Link message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ILink, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Link message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Link
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Link;

        /**
         * Decodes a Link message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Link
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Link;

        /**
         * Verifies a Link message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Link message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Link
         */
        public static fromObject(object: { [k: string]: any }): server.Link;

        /**
         * Creates a plain object from a Link message. Also converts values to other types if specified.
         * @param message Link
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Link, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Link to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace Link {

        /** Type enum. */
        enum Type {
            USER_DEFINED = 0,
            TIKTOK = 1,
            SNAPCHAT = 2,
            INSTAGRAM = 3
        }
    }

    /** Properties of a SetLinkRequest. */
    interface ISetLinkRequest {

        /** SetLinkRequest link */
        link?: (server.ILink|null);
    }

    /** Represents a SetLinkRequest. */
    class SetLinkRequest implements ISetLinkRequest {

        /**
         * Constructs a new SetLinkRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISetLinkRequest);

        /** SetLinkRequest link. */
        public link?: (server.ILink|null);

        /**
         * Creates a new SetLinkRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SetLinkRequest instance
         */
        public static create(properties?: server.ISetLinkRequest): server.SetLinkRequest;

        /**
         * Encodes the specified SetLinkRequest message. Does not implicitly {@link server.SetLinkRequest.verify|verify} messages.
         * @param message SetLinkRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISetLinkRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SetLinkRequest message, length delimited. Does not implicitly {@link server.SetLinkRequest.verify|verify} messages.
         * @param message SetLinkRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISetLinkRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SetLinkRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SetLinkRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SetLinkRequest;

        /**
         * Decodes a SetLinkRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SetLinkRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SetLinkRequest;

        /**
         * Verifies a SetLinkRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SetLinkRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SetLinkRequest
         */
        public static fromObject(object: { [k: string]: any }): server.SetLinkRequest;

        /**
         * Creates a plain object from a SetLinkRequest message. Also converts values to other types if specified.
         * @param message SetLinkRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SetLinkRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SetLinkRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a SetLinkResult. */
    interface ISetLinkResult {

        /** SetLinkResult result */
        result?: (server.SetLinkResult.Result|null);

        /** SetLinkResult reason */
        reason?: (server.SetLinkResult.Reason|null);
    }

    /** Represents a SetLinkResult. */
    class SetLinkResult implements ISetLinkResult {

        /**
         * Constructs a new SetLinkResult.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISetLinkResult);

        /** SetLinkResult result. */
        public result: server.SetLinkResult.Result;

        /** SetLinkResult reason. */
        public reason: server.SetLinkResult.Reason;

        /**
         * Creates a new SetLinkResult instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SetLinkResult instance
         */
        public static create(properties?: server.ISetLinkResult): server.SetLinkResult;

        /**
         * Encodes the specified SetLinkResult message. Does not implicitly {@link server.SetLinkResult.verify|verify} messages.
         * @param message SetLinkResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISetLinkResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SetLinkResult message, length delimited. Does not implicitly {@link server.SetLinkResult.verify|verify} messages.
         * @param message SetLinkResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISetLinkResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SetLinkResult message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SetLinkResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SetLinkResult;

        /**
         * Decodes a SetLinkResult message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SetLinkResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SetLinkResult;

        /**
         * Verifies a SetLinkResult message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SetLinkResult message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SetLinkResult
         */
        public static fromObject(object: { [k: string]: any }): server.SetLinkResult;

        /**
         * Creates a plain object from a SetLinkResult message. Also converts values to other types if specified.
         * @param message SetLinkResult
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SetLinkResult, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SetLinkResult to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace SetLinkResult {

        /** Result enum. */
        enum Result {
            OK = 0,
            FAIL = 1
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            BAD_TYPE = 1
        }
    }

    /** Properties of a SetBioRequest. */
    interface ISetBioRequest {

        /** SetBioRequest text */
        text?: (string|null);
    }

    /** Represents a SetBioRequest. */
    class SetBioRequest implements ISetBioRequest {

        /**
         * Constructs a new SetBioRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISetBioRequest);

        /** SetBioRequest text. */
        public text: string;

        /**
         * Creates a new SetBioRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SetBioRequest instance
         */
        public static create(properties?: server.ISetBioRequest): server.SetBioRequest;

        /**
         * Encodes the specified SetBioRequest message. Does not implicitly {@link server.SetBioRequest.verify|verify} messages.
         * @param message SetBioRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISetBioRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SetBioRequest message, length delimited. Does not implicitly {@link server.SetBioRequest.verify|verify} messages.
         * @param message SetBioRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISetBioRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SetBioRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SetBioRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SetBioRequest;

        /**
         * Decodes a SetBioRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SetBioRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SetBioRequest;

        /**
         * Verifies a SetBioRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SetBioRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SetBioRequest
         */
        public static fromObject(object: { [k: string]: any }): server.SetBioRequest;

        /**
         * Creates a plain object from a SetBioRequest message. Also converts values to other types if specified.
         * @param message SetBioRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SetBioRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SetBioRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a SetBioResult. */
    interface ISetBioResult {

        /** SetBioResult result */
        result?: (server.SetBioResult.Result|null);

        /** SetBioResult reason */
        reason?: (server.SetBioResult.Reason|null);
    }

    /** Represents a SetBioResult. */
    class SetBioResult implements ISetBioResult {

        /**
         * Constructs a new SetBioResult.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ISetBioResult);

        /** SetBioResult result. */
        public result: server.SetBioResult.Result;

        /** SetBioResult reason. */
        public reason: server.SetBioResult.Reason;

        /**
         * Creates a new SetBioResult instance using the specified properties.
         * @param [properties] Properties to set
         * @returns SetBioResult instance
         */
        public static create(properties?: server.ISetBioResult): server.SetBioResult;

        /**
         * Encodes the specified SetBioResult message. Does not implicitly {@link server.SetBioResult.verify|verify} messages.
         * @param message SetBioResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ISetBioResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified SetBioResult message, length delimited. Does not implicitly {@link server.SetBioResult.verify|verify} messages.
         * @param message SetBioResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ISetBioResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a SetBioResult message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns SetBioResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.SetBioResult;

        /**
         * Decodes a SetBioResult message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns SetBioResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.SetBioResult;

        /**
         * Verifies a SetBioResult message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a SetBioResult message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns SetBioResult
         */
        public static fromObject(object: { [k: string]: any }): server.SetBioResult;

        /**
         * Creates a plain object from a SetBioResult message. Also converts values to other types if specified.
         * @param message SetBioResult
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.SetBioResult, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this SetBioResult to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace SetBioResult {

        /** Result enum. */
        enum Result {
            OK = 0,
            FAIL = 1
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN_REASON = 0,
            TOO_LONG = 1
        }
    }

    /** Properties of an AiImageRequest. */
    interface IAiImageRequest {

        /** AiImageRequest text */
        text?: (string|null);

        /** AiImageRequest numImages */
        numImages?: (number|Long|null);

        /** AiImageRequest promptMode */
        promptMode?: (server.AiImageRequest.PromptMode|null);

        /** AiImageRequest negativePrompt */
        negativePrompt?: (string|null);
    }

    /** Represents an AiImageRequest. */
    class AiImageRequest implements IAiImageRequest {

        /**
         * Constructs a new AiImageRequest.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAiImageRequest);

        /** AiImageRequest text. */
        public text: string;

        /** AiImageRequest numImages. */
        public numImages: (number|Long);

        /** AiImageRequest promptMode. */
        public promptMode: server.AiImageRequest.PromptMode;

        /** AiImageRequest negativePrompt. */
        public negativePrompt: string;

        /**
         * Creates a new AiImageRequest instance using the specified properties.
         * @param [properties] Properties to set
         * @returns AiImageRequest instance
         */
        public static create(properties?: server.IAiImageRequest): server.AiImageRequest;

        /**
         * Encodes the specified AiImageRequest message. Does not implicitly {@link server.AiImageRequest.verify|verify} messages.
         * @param message AiImageRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAiImageRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified AiImageRequest message, length delimited. Does not implicitly {@link server.AiImageRequest.verify|verify} messages.
         * @param message AiImageRequest message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAiImageRequest, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an AiImageRequest message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns AiImageRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.AiImageRequest;

        /**
         * Decodes an AiImageRequest message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns AiImageRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.AiImageRequest;

        /**
         * Verifies an AiImageRequest message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an AiImageRequest message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns AiImageRequest
         */
        public static fromObject(object: { [k: string]: any }): server.AiImageRequest;

        /**
         * Creates a plain object from an AiImageRequest message. Also converts values to other types if specified.
         * @param message AiImageRequest
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.AiImageRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this AiImageRequest to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace AiImageRequest {

        /** PromptMode enum. */
        enum PromptMode {
            UNKNOWN = 0,
            USER = 1,
            SERVER = 2
        }
    }

    /** Properties of an AiImageResult. */
    interface IAiImageResult {

        /** AiImageResult result */
        result?: (server.AiImageResult.Result|null);

        /** AiImageResult reason */
        reason?: (server.AiImageResult.Reason|null);

        /** AiImageResult id */
        id?: (string|null);

        /** AiImageResult backoff */
        backoff?: (number|Long|null);
    }

    /** Represents an AiImageResult. */
    class AiImageResult implements IAiImageResult {

        /**
         * Constructs a new AiImageResult.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAiImageResult);

        /** AiImageResult result. */
        public result: server.AiImageResult.Result;

        /** AiImageResult reason. */
        public reason: server.AiImageResult.Reason;

        /** AiImageResult id. */
        public id: string;

        /** AiImageResult backoff. */
        public backoff: (number|Long);

        /**
         * Creates a new AiImageResult instance using the specified properties.
         * @param [properties] Properties to set
         * @returns AiImageResult instance
         */
        public static create(properties?: server.IAiImageResult): server.AiImageResult;

        /**
         * Encodes the specified AiImageResult message. Does not implicitly {@link server.AiImageResult.verify|verify} messages.
         * @param message AiImageResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAiImageResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified AiImageResult message, length delimited. Does not implicitly {@link server.AiImageResult.verify|verify} messages.
         * @param message AiImageResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAiImageResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an AiImageResult message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns AiImageResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.AiImageResult;

        /**
         * Decodes an AiImageResult message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns AiImageResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.AiImageResult;

        /**
         * Verifies an AiImageResult message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an AiImageResult message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns AiImageResult
         */
        public static fromObject(object: { [k: string]: any }): server.AiImageResult;

        /**
         * Creates a plain object from an AiImageResult message. Also converts values to other types if specified.
         * @param message AiImageResult
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.AiImageResult, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this AiImageResult to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace AiImageResult {

        /** Result enum. */
        enum Result {
            PENDING = 0,
            FAIL = 1
        }

        /** Reason enum. */
        enum Reason {
            UNKNOWN = 0,
            TOO_SOON = 1,
            TOO_MANY_ATTEMPTS = 2
        }
    }

    /** Properties of an AiImage. */
    interface IAiImage {

        /** AiImage id */
        id?: (string|null);

        /** AiImage image */
        image?: (Uint8Array|null);
    }

    /** Represents an AiImage. */
    class AiImage implements IAiImage {

        /**
         * Constructs a new AiImage.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAiImage);

        /** AiImage id. */
        public id: string;

        /** AiImage image. */
        public image: Uint8Array;

        /**
         * Creates a new AiImage instance using the specified properties.
         * @param [properties] Properties to set
         * @returns AiImage instance
         */
        public static create(properties?: server.IAiImage): server.AiImage;

        /**
         * Encodes the specified AiImage message. Does not implicitly {@link server.AiImage.verify|verify} messages.
         * @param message AiImage message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAiImage, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified AiImage message, length delimited. Does not implicitly {@link server.AiImage.verify|verify} messages.
         * @param message AiImage message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAiImage, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an AiImage message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns AiImage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.AiImage;

        /**
         * Decodes an AiImage message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns AiImage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.AiImage;

        /**
         * Verifies an AiImage message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an AiImage message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns AiImage
         */
        public static fromObject(object: { [k: string]: any }): server.AiImage;

        /**
         * Creates a plain object from an AiImage message. Also converts values to other types if specified.
         * @param message AiImage
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.AiImage, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this AiImage to JSON.
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

        /** EventData cc */
        cc?: (string|null);

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

        /** EventData mediaObjectDownload */
        mediaObjectDownload?: (server.IMediaObjectDownload|null);

        /** EventData groupDecryptionReport */
        groupDecryptionReport?: (server.IGroupDecryptionReport|null);

        /** EventData call */
        call?: (server.ICall|null);

        /** EventData fabAction */
        fabAction?: (server.IFabAction|null);

        /** EventData groupHistoryReport */
        groupHistoryReport?: (server.IGroupHistoryReport|null);

        /** EventData homeDecryptionReport */
        homeDecryptionReport?: (server.IHomeDecryptionReport|null);

        /** EventData inviteRequestResult */
        inviteRequestResult?: (server.IInviteRequestResult|null);
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

        /** EventData cc. */
        public cc: string;

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

        /** EventData mediaObjectDownload. */
        public mediaObjectDownload?: (server.IMediaObjectDownload|null);

        /** EventData groupDecryptionReport. */
        public groupDecryptionReport?: (server.IGroupDecryptionReport|null);

        /** EventData call. */
        public call?: (server.ICall|null);

        /** EventData fabAction. */
        public fabAction?: (server.IFabAction|null);

        /** EventData groupHistoryReport. */
        public groupHistoryReport?: (server.IGroupHistoryReport|null);

        /** EventData homeDecryptionReport. */
        public homeDecryptionReport?: (server.IHomeDecryptionReport|null);

        /** EventData inviteRequestResult. */
        public inviteRequestResult?: (server.IInviteRequestResult|null);

        /** EventData edata. */
        public edata?: ("mediaUpload"|"mediaDownload"|"mediaComposeLoad"|"pushReceived"|"decryptionReport"|"permissions"|"mediaObjectDownload"|"groupDecryptionReport"|"call"|"fabAction"|"groupHistoryReport"|"homeDecryptionReport"|"inviteRequestResult");

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

    /** Properties of a MediaObjectDownload. */
    interface IMediaObjectDownload {

        /** MediaObjectDownload id */
        id?: (string|null);

        /** MediaObjectDownload index */
        index?: (number|Long|null);

        /** MediaObjectDownload type */
        type?: (server.MediaObjectDownload.Type|null);

        /** MediaObjectDownload mediaType */
        mediaType?: (server.MediaObjectDownload.MediaType|null);

        /** MediaObjectDownload durationMs */
        durationMs?: (number|Long|null);

        /** MediaObjectDownload size */
        size?: (number|Long|null);

        /** MediaObjectDownload progressBytes */
        progressBytes?: (number|Long|null);

        /** MediaObjectDownload cdn */
        cdn?: (server.MediaObjectDownload.Cdn|null);

        /** MediaObjectDownload cdnPop */
        cdnPop?: (string|null);

        /** MediaObjectDownload cdnId */
        cdnId?: (string|null);

        /** MediaObjectDownload cdnCache */
        cdnCache?: (server.MediaObjectDownload.CdnCache|null);

        /** MediaObjectDownload status */
        status?: (server.MediaObjectDownload.Status|null);

        /** MediaObjectDownload retryCount */
        retryCount?: (number|Long|null);
    }

    /** Represents a MediaObjectDownload. */
    class MediaObjectDownload implements IMediaObjectDownload {

        /**
         * Constructs a new MediaObjectDownload.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IMediaObjectDownload);

        /** MediaObjectDownload id. */
        public id: string;

        /** MediaObjectDownload index. */
        public index: (number|Long);

        /** MediaObjectDownload type. */
        public type: server.MediaObjectDownload.Type;

        /** MediaObjectDownload mediaType. */
        public mediaType: server.MediaObjectDownload.MediaType;

        /** MediaObjectDownload durationMs. */
        public durationMs: (number|Long);

        /** MediaObjectDownload size. */
        public size: (number|Long);

        /** MediaObjectDownload progressBytes. */
        public progressBytes: (number|Long);

        /** MediaObjectDownload cdn. */
        public cdn: server.MediaObjectDownload.Cdn;

        /** MediaObjectDownload cdnPop. */
        public cdnPop: string;

        /** MediaObjectDownload cdnId. */
        public cdnId: string;

        /** MediaObjectDownload cdnCache. */
        public cdnCache: server.MediaObjectDownload.CdnCache;

        /** MediaObjectDownload status. */
        public status: server.MediaObjectDownload.Status;

        /** MediaObjectDownload retryCount. */
        public retryCount: (number|Long);

        /**
         * Creates a new MediaObjectDownload instance using the specified properties.
         * @param [properties] Properties to set
         * @returns MediaObjectDownload instance
         */
        public static create(properties?: server.IMediaObjectDownload): server.MediaObjectDownload;

        /**
         * Encodes the specified MediaObjectDownload message. Does not implicitly {@link server.MediaObjectDownload.verify|verify} messages.
         * @param message MediaObjectDownload message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IMediaObjectDownload, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified MediaObjectDownload message, length delimited. Does not implicitly {@link server.MediaObjectDownload.verify|verify} messages.
         * @param message MediaObjectDownload message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IMediaObjectDownload, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a MediaObjectDownload message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns MediaObjectDownload
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.MediaObjectDownload;

        /**
         * Decodes a MediaObjectDownload message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns MediaObjectDownload
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.MediaObjectDownload;

        /**
         * Verifies a MediaObjectDownload message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a MediaObjectDownload message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns MediaObjectDownload
         */
        public static fromObject(object: { [k: string]: any }): server.MediaObjectDownload;

        /**
         * Creates a plain object from a MediaObjectDownload message. Also converts values to other types if specified.
         * @param message MediaObjectDownload
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.MediaObjectDownload, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this MediaObjectDownload to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace MediaObjectDownload {

        /** Type enum. */
        enum Type {
            POST = 0,
            MESSAGE = 1,
            COMMENT = 2
        }

        /** MediaType enum. */
        enum MediaType {
            PHOTO = 0,
            VIDEO = 1,
            AUDIO = 2
        }

        /** Cdn enum. */
        enum Cdn {
            UNKNOWN_CDN = 0,
            CLOUDFRONT = 1
        }

        /** CdnCache enum. */
        enum CdnCache {
            UNKNOWN_CACHE = 0,
            HIT = 1,
            MISS = 2,
            REFRESH_HIT = 3,
            REFRESH_MISS = 4
        }

        /** Status enum. */
        enum Status {
            OK = 0,
            FAIL = 1
        }
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

        /** DecryptionReport contentType */
        contentType?: (server.DecryptionReport.ContentType|null);
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

        /** DecryptionReport contentType. */
        public contentType: server.DecryptionReport.ContentType;

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

        /** ContentType enum. */
        enum ContentType {
            CHAT = 0,
            GROUP_HISTORY = 1,
            CHAT_REACTION = 2
        }
    }

    /** Properties of a GroupDecryptionReport. */
    interface IGroupDecryptionReport {

        /** GroupDecryptionReport result */
        result?: (server.GroupDecryptionReport.Status|null);

        /** GroupDecryptionReport reason */
        reason?: (string|null);

        /** GroupDecryptionReport contentId */
        contentId?: (string|null);

        /** GroupDecryptionReport gid */
        gid?: (string|null);

        /** GroupDecryptionReport itemType */
        itemType?: (server.GroupDecryptionReport.ItemType|null);

        /** GroupDecryptionReport originalVersion */
        originalVersion?: (string|null);

        /** GroupDecryptionReport rerequestCount */
        rerequestCount?: (number|null);

        /** GroupDecryptionReport timeTakenS */
        timeTakenS?: (number|null);

        /** GroupDecryptionReport senderPlatform */
        senderPlatform?: (server.Platform|null);

        /** GroupDecryptionReport senderVersion */
        senderVersion?: (string|null);

        /** GroupDecryptionReport schedule */
        schedule?: (server.GroupDecryptionReport.Schedule|null);
    }

    /** Represents a GroupDecryptionReport. */
    class GroupDecryptionReport implements IGroupDecryptionReport {

        /**
         * Constructs a new GroupDecryptionReport.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupDecryptionReport);

        /** GroupDecryptionReport result. */
        public result: server.GroupDecryptionReport.Status;

        /** GroupDecryptionReport reason. */
        public reason: string;

        /** GroupDecryptionReport contentId. */
        public contentId: string;

        /** GroupDecryptionReport gid. */
        public gid: string;

        /** GroupDecryptionReport itemType. */
        public itemType: server.GroupDecryptionReport.ItemType;

        /** GroupDecryptionReport originalVersion. */
        public originalVersion: string;

        /** GroupDecryptionReport rerequestCount. */
        public rerequestCount: number;

        /** GroupDecryptionReport timeTakenS. */
        public timeTakenS: number;

        /** GroupDecryptionReport senderPlatform. */
        public senderPlatform: server.Platform;

        /** GroupDecryptionReport senderVersion. */
        public senderVersion: string;

        /** GroupDecryptionReport schedule. */
        public schedule: server.GroupDecryptionReport.Schedule;

        /**
         * Creates a new GroupDecryptionReport instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupDecryptionReport instance
         */
        public static create(properties?: server.IGroupDecryptionReport): server.GroupDecryptionReport;

        /**
         * Encodes the specified GroupDecryptionReport message. Does not implicitly {@link server.GroupDecryptionReport.verify|verify} messages.
         * @param message GroupDecryptionReport message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupDecryptionReport, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupDecryptionReport message, length delimited. Does not implicitly {@link server.GroupDecryptionReport.verify|verify} messages.
         * @param message GroupDecryptionReport message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupDecryptionReport, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupDecryptionReport message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupDecryptionReport
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupDecryptionReport;

        /**
         * Decodes a GroupDecryptionReport message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupDecryptionReport
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupDecryptionReport;

        /**
         * Verifies a GroupDecryptionReport message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupDecryptionReport message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupDecryptionReport
         */
        public static fromObject(object: { [k: string]: any }): server.GroupDecryptionReport;

        /**
         * Creates a plain object from a GroupDecryptionReport message. Also converts values to other types if specified.
         * @param message GroupDecryptionReport
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupDecryptionReport, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupDecryptionReport to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GroupDecryptionReport {

        /** Status enum. */
        enum Status {
            UNKNOWN_STATUS = 0,
            OK = 1,
            FAIL = 2
        }

        /** ItemType enum. */
        enum ItemType {
            UNKNOWN_TYPE = 0,
            POST = 1,
            COMMENT = 2,
            HISTORY_RESEND = 3,
            POST_REACTION = 4,
            COMMENT_REACTION = 5,
            CHAT = 6,
            CHAT_REACTION = 7
        }

        /** Schedule enum. */
        enum Schedule {
            DAILY = 0,
            RESULT_BASED = 1
        }
    }

    /** Properties of a HomeDecryptionReport. */
    interface IHomeDecryptionReport {

        /** HomeDecryptionReport result */
        result?: (server.HomeDecryptionReport.Status|null);

        /** HomeDecryptionReport reason */
        reason?: (string|null);

        /** HomeDecryptionReport contentId */
        contentId?: (string|null);

        /** HomeDecryptionReport audienceType */
        audienceType?: (server.HomeDecryptionReport.AudienceType|null);

        /** HomeDecryptionReport itemType */
        itemType?: (server.HomeDecryptionReport.ItemType|null);

        /** HomeDecryptionReport originalVersion */
        originalVersion?: (string|null);

        /** HomeDecryptionReport rerequestCount */
        rerequestCount?: (number|null);

        /** HomeDecryptionReport timeTakenS */
        timeTakenS?: (number|null);

        /** HomeDecryptionReport senderPlatform */
        senderPlatform?: (server.Platform|null);

        /** HomeDecryptionReport senderVersion */
        senderVersion?: (string|null);

        /** HomeDecryptionReport schedule */
        schedule?: (server.HomeDecryptionReport.Schedule|null);
    }

    /** Represents a HomeDecryptionReport. */
    class HomeDecryptionReport implements IHomeDecryptionReport {

        /**
         * Constructs a new HomeDecryptionReport.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IHomeDecryptionReport);

        /** HomeDecryptionReport result. */
        public result: server.HomeDecryptionReport.Status;

        /** HomeDecryptionReport reason. */
        public reason: string;

        /** HomeDecryptionReport contentId. */
        public contentId: string;

        /** HomeDecryptionReport audienceType. */
        public audienceType: server.HomeDecryptionReport.AudienceType;

        /** HomeDecryptionReport itemType. */
        public itemType: server.HomeDecryptionReport.ItemType;

        /** HomeDecryptionReport originalVersion. */
        public originalVersion: string;

        /** HomeDecryptionReport rerequestCount. */
        public rerequestCount: number;

        /** HomeDecryptionReport timeTakenS. */
        public timeTakenS: number;

        /** HomeDecryptionReport senderPlatform. */
        public senderPlatform: server.Platform;

        /** HomeDecryptionReport senderVersion. */
        public senderVersion: string;

        /** HomeDecryptionReport schedule. */
        public schedule: server.HomeDecryptionReport.Schedule;

        /**
         * Creates a new HomeDecryptionReport instance using the specified properties.
         * @param [properties] Properties to set
         * @returns HomeDecryptionReport instance
         */
        public static create(properties?: server.IHomeDecryptionReport): server.HomeDecryptionReport;

        /**
         * Encodes the specified HomeDecryptionReport message. Does not implicitly {@link server.HomeDecryptionReport.verify|verify} messages.
         * @param message HomeDecryptionReport message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IHomeDecryptionReport, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified HomeDecryptionReport message, length delimited. Does not implicitly {@link server.HomeDecryptionReport.verify|verify} messages.
         * @param message HomeDecryptionReport message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IHomeDecryptionReport, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a HomeDecryptionReport message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns HomeDecryptionReport
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.HomeDecryptionReport;

        /**
         * Decodes a HomeDecryptionReport message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns HomeDecryptionReport
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.HomeDecryptionReport;

        /**
         * Verifies a HomeDecryptionReport message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a HomeDecryptionReport message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns HomeDecryptionReport
         */
        public static fromObject(object: { [k: string]: any }): server.HomeDecryptionReport;

        /**
         * Creates a plain object from a HomeDecryptionReport message. Also converts values to other types if specified.
         * @param message HomeDecryptionReport
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.HomeDecryptionReport, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this HomeDecryptionReport to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace HomeDecryptionReport {

        /** Status enum. */
        enum Status {
            UNKNOWN_STATUS = 0,
            OK = 1,
            FAIL = 2
        }

        /** AudienceType enum. */
        enum AudienceType {
            UNKNOWN_AUDIENCE_TYPE = 0,
            ALL = 1,
            ONLY = 2
        }

        /** ItemType enum. */
        enum ItemType {
            UNKNOWN_TYPE = 0,
            POST = 1,
            COMMENT = 2,
            POST_REACTION = 4,
            COMMENT_REACTION = 5
        }

        /** Schedule enum. */
        enum Schedule {
            DAILY = 0,
            RESULT_BASED = 1
        }
    }

    /** Properties of a GroupHistoryReport. */
    interface IGroupHistoryReport {

        /** GroupHistoryReport gid */
        gid?: (string|null);

        /** GroupHistoryReport numExpected */
        numExpected?: (number|null);

        /** GroupHistoryReport numDecrypted */
        numDecrypted?: (number|null);

        /** GroupHistoryReport originalVersion */
        originalVersion?: (string|null);

        /** GroupHistoryReport rerequestCount */
        rerequestCount?: (number|null);

        /** GroupHistoryReport timeTakenS */
        timeTakenS?: (number|null);

        /** GroupHistoryReport schedule */
        schedule?: (server.GroupHistoryReport.Schedule|null);
    }

    /** Represents a GroupHistoryReport. */
    class GroupHistoryReport implements IGroupHistoryReport {

        /**
         * Constructs a new GroupHistoryReport.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IGroupHistoryReport);

        /** GroupHistoryReport gid. */
        public gid: string;

        /** GroupHistoryReport numExpected. */
        public numExpected: number;

        /** GroupHistoryReport numDecrypted. */
        public numDecrypted: number;

        /** GroupHistoryReport originalVersion. */
        public originalVersion: string;

        /** GroupHistoryReport rerequestCount. */
        public rerequestCount: number;

        /** GroupHistoryReport timeTakenS. */
        public timeTakenS: number;

        /** GroupHistoryReport schedule. */
        public schedule: server.GroupHistoryReport.Schedule;

        /**
         * Creates a new GroupHistoryReport instance using the specified properties.
         * @param [properties] Properties to set
         * @returns GroupHistoryReport instance
         */
        public static create(properties?: server.IGroupHistoryReport): server.GroupHistoryReport;

        /**
         * Encodes the specified GroupHistoryReport message. Does not implicitly {@link server.GroupHistoryReport.verify|verify} messages.
         * @param message GroupHistoryReport message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IGroupHistoryReport, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified GroupHistoryReport message, length delimited. Does not implicitly {@link server.GroupHistoryReport.verify|verify} messages.
         * @param message GroupHistoryReport message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IGroupHistoryReport, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a GroupHistoryReport message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns GroupHistoryReport
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.GroupHistoryReport;

        /**
         * Decodes a GroupHistoryReport message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns GroupHistoryReport
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.GroupHistoryReport;

        /**
         * Verifies a GroupHistoryReport message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a GroupHistoryReport message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns GroupHistoryReport
         */
        public static fromObject(object: { [k: string]: any }): server.GroupHistoryReport;

        /**
         * Creates a plain object from a GroupHistoryReport message. Also converts values to other types if specified.
         * @param message GroupHistoryReport
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.GroupHistoryReport, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this GroupHistoryReport to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace GroupHistoryReport {

        /** Schedule enum. */
        enum Schedule {
            DAILY = 0,
            RESULT_BASED = 1
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
            NOTIFICATIONS = 1,
            LOCATION = 2
        }

        /** Status enum. */
        enum Status {
            ALLOWED = 0,
            DENIED = 1
        }
    }

    /** Properties of a Call. */
    interface ICall {

        /** Call callId */
        callId?: (string|null);

        /** Call peerUid */
        peerUid?: (number|Long|null);

        /** Call type */
        type?: (server.Call.CallType|null);

        /** Call direction */
        direction?: (server.Call.CallDirection|null);

        /** Call answered */
        answered?: (boolean|null);

        /** Call connected */
        connected?: (boolean|null);

        /** Call durationMs */
        durationMs?: (number|Long|null);

        /** Call endCallReason */
        endCallReason?: (string|null);

        /** Call localEndCall */
        localEndCall?: (boolean|null);

        /** Call networkType */
        networkType?: (server.Call.NetworkType|null);

        /** Call isKrispActive */
        isKrispActive?: (boolean|null);

        /** Call iceTimeTakenMs */
        iceTimeTakenMs?: (number|Long|null);

        /** Call webrtcStats */
        webrtcStats?: (string|null);

        /** Call webrtcSummary */
        webrtcSummary?: (server.IWebrtcSummary|null);
    }

    /** Represents a Call. */
    class Call implements ICall {

        /**
         * Constructs a new Call.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ICall);

        /** Call callId. */
        public callId: string;

        /** Call peerUid. */
        public peerUid: (number|Long);

        /** Call type. */
        public type: server.Call.CallType;

        /** Call direction. */
        public direction: server.Call.CallDirection;

        /** Call answered. */
        public answered: boolean;

        /** Call connected. */
        public connected: boolean;

        /** Call durationMs. */
        public durationMs: (number|Long);

        /** Call endCallReason. */
        public endCallReason: string;

        /** Call localEndCall. */
        public localEndCall: boolean;

        /** Call networkType. */
        public networkType: server.Call.NetworkType;

        /** Call isKrispActive. */
        public isKrispActive: boolean;

        /** Call iceTimeTakenMs. */
        public iceTimeTakenMs: (number|Long);

        /** Call webrtcStats. */
        public webrtcStats: string;

        /** Call webrtcSummary. */
        public webrtcSummary?: (server.IWebrtcSummary|null);

        /**
         * Creates a new Call instance using the specified properties.
         * @param [properties] Properties to set
         * @returns Call instance
         */
        public static create(properties?: server.ICall): server.Call;

        /**
         * Encodes the specified Call message. Does not implicitly {@link server.Call.verify|verify} messages.
         * @param message Call message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ICall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified Call message, length delimited. Does not implicitly {@link server.Call.verify|verify} messages.
         * @param message Call message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ICall, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a Call message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns Call
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.Call;

        /**
         * Decodes a Call message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns Call
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.Call;

        /**
         * Verifies a Call message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a Call message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns Call
         */
        public static fromObject(object: { [k: string]: any }): server.Call;

        /**
         * Creates a plain object from a Call message. Also converts values to other types if specified.
         * @param message Call
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.Call, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this Call to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace Call {

        /** CallType enum. */
        enum CallType {
            UNKNOWN_TYPE = 0,
            AUDIO = 1,
            VIDEO = 2
        }

        /** CallDirection enum. */
        enum CallDirection {
            UNKNOWN_DIRECTION = 0,
            OUTGOING = 1,
            INCOMING = 2
        }

        /** NetworkType enum. */
        enum NetworkType {
            UNKNOWN_NETWORK = 0,
            WIFI = 1,
            CELLULAR = 2
        }
    }

    /** Properties of a WebrtcSummary. */
    interface IWebrtcSummary {

        /** WebrtcSummary audioStream */
        audioStream?: (server.IStreamStats|null);

        /** WebrtcSummary videoStream */
        videoStream?: (server.IStreamStats|null);

        /** WebrtcSummary audio */
        audio?: (server.IAudioStats|null);

        /** WebrtcSummary video */
        video?: (server.IVideoStats|null);

        /** WebrtcSummary candidatePairs */
        candidatePairs?: (server.ICandidatePairStats[]|null);
    }

    /** Represents a WebrtcSummary. */
    class WebrtcSummary implements IWebrtcSummary {

        /**
         * Constructs a new WebrtcSummary.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IWebrtcSummary);

        /** WebrtcSummary audioStream. */
        public audioStream?: (server.IStreamStats|null);

        /** WebrtcSummary videoStream. */
        public videoStream?: (server.IStreamStats|null);

        /** WebrtcSummary audio. */
        public audio?: (server.IAudioStats|null);

        /** WebrtcSummary video. */
        public video?: (server.IVideoStats|null);

        /** WebrtcSummary candidatePairs. */
        public candidatePairs: server.ICandidatePairStats[];

        /**
         * Creates a new WebrtcSummary instance using the specified properties.
         * @param [properties] Properties to set
         * @returns WebrtcSummary instance
         */
        public static create(properties?: server.IWebrtcSummary): server.WebrtcSummary;

        /**
         * Encodes the specified WebrtcSummary message. Does not implicitly {@link server.WebrtcSummary.verify|verify} messages.
         * @param message WebrtcSummary message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IWebrtcSummary, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified WebrtcSummary message, length delimited. Does not implicitly {@link server.WebrtcSummary.verify|verify} messages.
         * @param message WebrtcSummary message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IWebrtcSummary, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a WebrtcSummary message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns WebrtcSummary
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.WebrtcSummary;

        /**
         * Decodes a WebrtcSummary message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns WebrtcSummary
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.WebrtcSummary;

        /**
         * Verifies a WebrtcSummary message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a WebrtcSummary message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns WebrtcSummary
         */
        public static fromObject(object: { [k: string]: any }): server.WebrtcSummary;

        /**
         * Creates a plain object from a WebrtcSummary message. Also converts values to other types if specified.
         * @param message WebrtcSummary
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.WebrtcSummary, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this WebrtcSummary to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a StreamStats. */
    interface IStreamStats {

        /** StreamStats packetsSent */
        packetsSent?: (number|Long|null);

        /** StreamStats packetsLost */
        packetsLost?: (number|Long|null);

        /** StreamStats packetsReceived */
        packetsReceived?: (number|Long|null);

        /** StreamStats bytesReceived */
        bytesReceived?: (number|Long|null);

        /** StreamStats jitter */
        jitter?: (number|null);

        /** StreamStats jitterBufferDelay */
        jitterBufferDelay?: (number|null);

        /** StreamStats jitterBufferEmittedCount */
        jitterBufferEmittedCount?: (number|Long|null);

        /** StreamStats jitterBufferMinimumDelay */
        jitterBufferMinimumDelay?: (number|null);
    }

    /** Represents a StreamStats. */
    class StreamStats implements IStreamStats {

        /**
         * Constructs a new StreamStats.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IStreamStats);

        /** StreamStats packetsSent. */
        public packetsSent: (number|Long);

        /** StreamStats packetsLost. */
        public packetsLost: (number|Long);

        /** StreamStats packetsReceived. */
        public packetsReceived: (number|Long);

        /** StreamStats bytesReceived. */
        public bytesReceived: (number|Long);

        /** StreamStats jitter. */
        public jitter: number;

        /** StreamStats jitterBufferDelay. */
        public jitterBufferDelay: number;

        /** StreamStats jitterBufferEmittedCount. */
        public jitterBufferEmittedCount: (number|Long);

        /** StreamStats jitterBufferMinimumDelay. */
        public jitterBufferMinimumDelay: number;

        /**
         * Creates a new StreamStats instance using the specified properties.
         * @param [properties] Properties to set
         * @returns StreamStats instance
         */
        public static create(properties?: server.IStreamStats): server.StreamStats;

        /**
         * Encodes the specified StreamStats message. Does not implicitly {@link server.StreamStats.verify|verify} messages.
         * @param message StreamStats message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IStreamStats, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified StreamStats message, length delimited. Does not implicitly {@link server.StreamStats.verify|verify} messages.
         * @param message StreamStats message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IStreamStats, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a StreamStats message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns StreamStats
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.StreamStats;

        /**
         * Decodes a StreamStats message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns StreamStats
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.StreamStats;

        /**
         * Verifies a StreamStats message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a StreamStats message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns StreamStats
         */
        public static fromObject(object: { [k: string]: any }): server.StreamStats;

        /**
         * Creates a plain object from a StreamStats message. Also converts values to other types if specified.
         * @param message StreamStats
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.StreamStats, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this StreamStats to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of an AudioStats. */
    interface IAudioStats {

        /** AudioStats insertedSamplesForDeceleration */
        insertedSamplesForDeceleration?: (number|Long|null);

        /** AudioStats removedSamplesForAcceleration */
        removedSamplesForAcceleration?: (number|Long|null);

        /** AudioStats packetsDiscarded */
        packetsDiscarded?: (number|Long|null);
    }

    /** Represents an AudioStats. */
    class AudioStats implements IAudioStats {

        /**
         * Constructs a new AudioStats.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IAudioStats);

        /** AudioStats insertedSamplesForDeceleration. */
        public insertedSamplesForDeceleration: (number|Long);

        /** AudioStats removedSamplesForAcceleration. */
        public removedSamplesForAcceleration: (number|Long);

        /** AudioStats packetsDiscarded. */
        public packetsDiscarded: (number|Long);

        /**
         * Creates a new AudioStats instance using the specified properties.
         * @param [properties] Properties to set
         * @returns AudioStats instance
         */
        public static create(properties?: server.IAudioStats): server.AudioStats;

        /**
         * Encodes the specified AudioStats message. Does not implicitly {@link server.AudioStats.verify|verify} messages.
         * @param message AudioStats message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IAudioStats, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified AudioStats message, length delimited. Does not implicitly {@link server.AudioStats.verify|verify} messages.
         * @param message AudioStats message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IAudioStats, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an AudioStats message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns AudioStats
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.AudioStats;

        /**
         * Decodes an AudioStats message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns AudioStats
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.AudioStats;

        /**
         * Verifies an AudioStats message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an AudioStats message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns AudioStats
         */
        public static fromObject(object: { [k: string]: any }): server.AudioStats;

        /**
         * Creates a plain object from an AudioStats message. Also converts values to other types if specified.
         * @param message AudioStats
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.AudioStats, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this AudioStats to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a VideoStats. */
    interface IVideoStats {

        /** VideoStats framesReceived */
        framesReceived?: (number|Long|null);

        /** VideoStats framesDropped */
        framesDropped?: (number|Long|null);

        /** VideoStats qualityLimitationDurationBandwidth */
        qualityLimitationDurationBandwidth?: (number|null);

        /** VideoStats qualityLimitationDurationCpu */
        qualityLimitationDurationCpu?: (number|null);

        /** VideoStats qualityLimitationDurationNone */
        qualityLimitationDurationNone?: (number|null);

        /** VideoStats qualityLimitationDurationOther */
        qualityLimitationDurationOther?: (number|null);

        /** VideoStats averageQp */
        averageQp?: (number|null);

        /** VideoStats totalProcessingDelay */
        totalProcessingDelay?: (number|null);
    }

    /** Represents a VideoStats. */
    class VideoStats implements IVideoStats {

        /**
         * Constructs a new VideoStats.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IVideoStats);

        /** VideoStats framesReceived. */
        public framesReceived: (number|Long);

        /** VideoStats framesDropped. */
        public framesDropped: (number|Long);

        /** VideoStats qualityLimitationDurationBandwidth. */
        public qualityLimitationDurationBandwidth: number;

        /** VideoStats qualityLimitationDurationCpu. */
        public qualityLimitationDurationCpu: number;

        /** VideoStats qualityLimitationDurationNone. */
        public qualityLimitationDurationNone: number;

        /** VideoStats qualityLimitationDurationOther. */
        public qualityLimitationDurationOther: number;

        /** VideoStats averageQp. */
        public averageQp: number;

        /** VideoStats totalProcessingDelay. */
        public totalProcessingDelay: number;

        /**
         * Creates a new VideoStats instance using the specified properties.
         * @param [properties] Properties to set
         * @returns VideoStats instance
         */
        public static create(properties?: server.IVideoStats): server.VideoStats;

        /**
         * Encodes the specified VideoStats message. Does not implicitly {@link server.VideoStats.verify|verify} messages.
         * @param message VideoStats message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IVideoStats, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified VideoStats message, length delimited. Does not implicitly {@link server.VideoStats.verify|verify} messages.
         * @param message VideoStats message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IVideoStats, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a VideoStats message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns VideoStats
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.VideoStats;

        /**
         * Decodes a VideoStats message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns VideoStats
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.VideoStats;

        /**
         * Verifies a VideoStats message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a VideoStats message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns VideoStats
         */
        public static fromObject(object: { [k: string]: any }): server.VideoStats;

        /**
         * Creates a plain object from a VideoStats message. Also converts values to other types if specified.
         * @param message VideoStats
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.VideoStats, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this VideoStats to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    /** Properties of a CandidatePairStats. */
    interface ICandidatePairStats {

        /** CandidatePairStats local */
        local?: (server.CandidatePairStats.CandidateType|null);

        /** CandidatePairStats localIP */
        localIP?: (string|null);

        /** CandidatePairStats remote */
        remote?: (server.CandidatePairStats.CandidateType|null);

        /** CandidatePairStats remoteIP */
        remoteIP?: (string|null);

        /** CandidatePairStats packetsSent */
        packetsSent?: (number|Long|null);

        /** CandidatePairStats packetsReceived */
        packetsReceived?: (number|Long|null);

        /** CandidatePairStats bytesSent */
        bytesSent?: (number|Long|null);

        /** CandidatePairStats bytesReceived */
        bytesReceived?: (number|Long|null);

        /** CandidatePairStats averageRoundTripTime */
        averageRoundTripTime?: (number|null);

        /** CandidatePairStats currentRoundTripTime */
        currentRoundTripTime?: (number|null);

        /** CandidatePairStats availableOutgoingBitrate */
        availableOutgoingBitrate?: (number|null);

        /** CandidatePairStats availableIncomingBitrate */
        availableIncomingBitrate?: (number|null);

        /** CandidatePairStats state */
        state?: (server.CandidatePairStats.CandidatePairState|null);
    }

    /** Represents a CandidatePairStats. */
    class CandidatePairStats implements ICandidatePairStats {

        /**
         * Constructs a new CandidatePairStats.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.ICandidatePairStats);

        /** CandidatePairStats local. */
        public local: server.CandidatePairStats.CandidateType;

        /** CandidatePairStats localIP. */
        public localIP: string;

        /** CandidatePairStats remote. */
        public remote: server.CandidatePairStats.CandidateType;

        /** CandidatePairStats remoteIP. */
        public remoteIP: string;

        /** CandidatePairStats packetsSent. */
        public packetsSent: (number|Long);

        /** CandidatePairStats packetsReceived. */
        public packetsReceived: (number|Long);

        /** CandidatePairStats bytesSent. */
        public bytesSent: (number|Long);

        /** CandidatePairStats bytesReceived. */
        public bytesReceived: (number|Long);

        /** CandidatePairStats averageRoundTripTime. */
        public averageRoundTripTime: number;

        /** CandidatePairStats currentRoundTripTime. */
        public currentRoundTripTime: number;

        /** CandidatePairStats availableOutgoingBitrate. */
        public availableOutgoingBitrate: number;

        /** CandidatePairStats availableIncomingBitrate. */
        public availableIncomingBitrate: number;

        /** CandidatePairStats state. */
        public state: server.CandidatePairStats.CandidatePairState;

        /**
         * Creates a new CandidatePairStats instance using the specified properties.
         * @param [properties] Properties to set
         * @returns CandidatePairStats instance
         */
        public static create(properties?: server.ICandidatePairStats): server.CandidatePairStats;

        /**
         * Encodes the specified CandidatePairStats message. Does not implicitly {@link server.CandidatePairStats.verify|verify} messages.
         * @param message CandidatePairStats message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.ICandidatePairStats, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified CandidatePairStats message, length delimited. Does not implicitly {@link server.CandidatePairStats.verify|verify} messages.
         * @param message CandidatePairStats message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.ICandidatePairStats, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a CandidatePairStats message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns CandidatePairStats
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.CandidatePairStats;

        /**
         * Decodes a CandidatePairStats message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns CandidatePairStats
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.CandidatePairStats;

        /**
         * Verifies a CandidatePairStats message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a CandidatePairStats message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns CandidatePairStats
         */
        public static fromObject(object: { [k: string]: any }): server.CandidatePairStats;

        /**
         * Creates a plain object from a CandidatePairStats message. Also converts values to other types if specified.
         * @param message CandidatePairStats
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.CandidatePairStats, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this CandidatePairStats to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace CandidatePairStats {

        /** CandidateType enum. */
        enum CandidateType {
            PRFLX = 0,
            SRFLX = 1,
            RELAY = 2,
            HOST = 3
        }

        /** CandidatePairState enum. */
        enum CandidatePairState {
            FROZEN = 0,
            WAITING = 1,
            IN_PROGRESS = 2,
            FAILED = 3,
            SUCCEEDED = 4
        }
    }

    /** Properties of a FabAction. */
    interface IFabAction {

        /** FabAction type */
        type?: (server.FabAction.FabActionType|null);
    }

    /** Represents a FabAction. */
    class FabAction implements IFabAction {

        /**
         * Constructs a new FabAction.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IFabAction);

        /** FabAction type. */
        public type: server.FabAction.FabActionType;

        /**
         * Creates a new FabAction instance using the specified properties.
         * @param [properties] Properties to set
         * @returns FabAction instance
         */
        public static create(properties?: server.IFabAction): server.FabAction;

        /**
         * Encodes the specified FabAction message. Does not implicitly {@link server.FabAction.verify|verify} messages.
         * @param message FabAction message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IFabAction, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified FabAction message, length delimited. Does not implicitly {@link server.FabAction.verify|verify} messages.
         * @param message FabAction message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IFabAction, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes a FabAction message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns FabAction
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.FabAction;

        /**
         * Decodes a FabAction message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns FabAction
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.FabAction;

        /**
         * Verifies a FabAction message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates a FabAction message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns FabAction
         */
        public static fromObject(object: { [k: string]: any }): server.FabAction;

        /**
         * Creates a plain object from a FabAction message. Also converts values to other types if specified.
         * @param message FabAction
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.FabAction, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this FabAction to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace FabAction {

        /** FabActionType enum. */
        enum FabActionType {
            UNKNOWN_TYPE = 0,
            GALLERY = 1,
            AUDIO = 2,
            TEXT = 3,
            CAMERA = 4
        }
    }

    /** Properties of an InviteRequestResult. */
    interface IInviteRequestResult {

        /** InviteRequestResult type */
        type?: (server.InviteRequestResult.Type|null);

        /** InviteRequestResult invitedPhone */
        invitedPhone?: (string|null);

        /** InviteRequestResult langId */
        langId?: (string|null);

        /** InviteRequestResult inviteStringId */
        inviteStringId?: (string|null);
    }

    /** Represents an InviteRequestResult. */
    class InviteRequestResult implements IInviteRequestResult {

        /**
         * Constructs a new InviteRequestResult.
         * @param [properties] Properties to set
         */
        constructor(properties?: server.IInviteRequestResult);

        /** InviteRequestResult type. */
        public type: server.InviteRequestResult.Type;

        /** InviteRequestResult invitedPhone. */
        public invitedPhone: string;

        /** InviteRequestResult langId. */
        public langId: string;

        /** InviteRequestResult inviteStringId. */
        public inviteStringId: string;

        /**
         * Creates a new InviteRequestResult instance using the specified properties.
         * @param [properties] Properties to set
         * @returns InviteRequestResult instance
         */
        public static create(properties?: server.IInviteRequestResult): server.InviteRequestResult;

        /**
         * Encodes the specified InviteRequestResult message. Does not implicitly {@link server.InviteRequestResult.verify|verify} messages.
         * @param message InviteRequestResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encode(message: server.IInviteRequestResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Encodes the specified InviteRequestResult message, length delimited. Does not implicitly {@link server.InviteRequestResult.verify|verify} messages.
         * @param message InviteRequestResult message or plain object to encode
         * @param [writer] Writer to encode to
         * @returns Writer
         */
        public static encodeDelimited(message: server.IInviteRequestResult, writer?: $protobuf.Writer): $protobuf.Writer;

        /**
         * Decodes an InviteRequestResult message from the specified reader or buffer.
         * @param reader Reader or buffer to decode from
         * @param [length] Message length if known beforehand
         * @returns InviteRequestResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): server.InviteRequestResult;

        /**
         * Decodes an InviteRequestResult message from the specified reader or buffer, length delimited.
         * @param reader Reader or buffer to decode from
         * @returns InviteRequestResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): server.InviteRequestResult;

        /**
         * Verifies an InviteRequestResult message.
         * @param message Plain object to verify
         * @returns `null` if valid, otherwise the reason why it is not
         */
        public static verify(message: { [k: string]: any }): (string|null);

        /**
         * Creates an InviteRequestResult message from a plain object. Also converts values to their respective internal types.
         * @param object Plain object
         * @returns InviteRequestResult
         */
        public static fromObject(object: { [k: string]: any }): server.InviteRequestResult;

        /**
         * Creates a plain object from an InviteRequestResult message. Also converts values to other types if specified.
         * @param message InviteRequestResult
         * @param [options] Conversion options
         * @returns Plain object
         */
        public static toObject(message: server.InviteRequestResult, options?: $protobuf.IConversionOptions): { [k: string]: any };

        /**
         * Converts this InviteRequestResult to JSON.
         * @returns JSON object
         */
        public toJSON(): { [k: string]: any };
    }

    namespace InviteRequestResult {

        /** Type enum. */
        enum Type {
            UNKNOWN = 0,
            CANCELLED = 1,
            SENT = 2,
            FAILED = 3
        }
    }
}
