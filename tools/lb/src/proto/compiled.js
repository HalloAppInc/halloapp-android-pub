/* eslint-disable */
// @ts-nocheck
/*eslint-disable block-scoped-var, id-length, no-control-regex, no-magic-numbers, no-prototype-builtins, no-redeclare, no-shadow, no-var, sort-vars*/
"use strict";

var $protobuf = require("protobufjs/minimal");

// Common aliases
var $Reader = $protobuf.Reader, $Writer = $protobuf.Writer, $util = $protobuf.util;

// Exported root namespace
var $root = $protobuf.roots["default"] || ($protobuf.roots["default"] = {});

$root.server = (function() {

    /**
     * Namespace server.
     * @exports server
     * @namespace
     */
    var server = {};

    server.UploadAvatar = (function() {

        /**
         * Properties of an UploadAvatar.
         * @memberof server
         * @interface IUploadAvatar
         * @property {string|null} [id] UploadAvatar id
         * @property {Uint8Array|null} [data] UploadAvatar data
         */

        /**
         * Constructs a new UploadAvatar.
         * @memberof server
         * @classdesc Represents an UploadAvatar.
         * @implements IUploadAvatar
         * @constructor
         * @param {server.IUploadAvatar=} [properties] Properties to set
         */
        function UploadAvatar(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * UploadAvatar id.
         * @member {string} id
         * @memberof server.UploadAvatar
         * @instance
         */
        UploadAvatar.prototype.id = "";

        /**
         * UploadAvatar data.
         * @member {Uint8Array} data
         * @memberof server.UploadAvatar
         * @instance
         */
        UploadAvatar.prototype.data = $util.newBuffer([]);

        /**
         * Creates a new UploadAvatar instance using the specified properties.
         * @function create
         * @memberof server.UploadAvatar
         * @static
         * @param {server.IUploadAvatar=} [properties] Properties to set
         * @returns {server.UploadAvatar} UploadAvatar instance
         */
        UploadAvatar.create = function create(properties) {
            return new UploadAvatar(properties);
        };

        /**
         * Encodes the specified UploadAvatar message. Does not implicitly {@link server.UploadAvatar.verify|verify} messages.
         * @function encode
         * @memberof server.UploadAvatar
         * @static
         * @param {server.IUploadAvatar} message UploadAvatar message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UploadAvatar.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.data != null && Object.hasOwnProperty.call(message, "data"))
                writer.uint32(/* id 2, wireType 2 =*/18).bytes(message.data);
            return writer;
        };

        /**
         * Encodes the specified UploadAvatar message, length delimited. Does not implicitly {@link server.UploadAvatar.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.UploadAvatar
         * @static
         * @param {server.IUploadAvatar} message UploadAvatar message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UploadAvatar.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an UploadAvatar message from the specified reader or buffer.
         * @function decode
         * @memberof server.UploadAvatar
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.UploadAvatar} UploadAvatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        UploadAvatar.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.UploadAvatar();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.data = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an UploadAvatar message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.UploadAvatar
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.UploadAvatar} UploadAvatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        UploadAvatar.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an UploadAvatar message.
         * @function verify
         * @memberof server.UploadAvatar
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        UploadAvatar.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.data != null && message.hasOwnProperty("data"))
                if (!(message.data && typeof message.data.length === "number" || $util.isString(message.data)))
                    return "data: buffer expected";
            return null;
        };

        /**
         * Creates an UploadAvatar message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.UploadAvatar
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.UploadAvatar} UploadAvatar
         */
        UploadAvatar.fromObject = function fromObject(object) {
            if (object instanceof $root.server.UploadAvatar)
                return object;
            var message = new $root.server.UploadAvatar();
            if (object.id != null)
                message.id = String(object.id);
            if (object.data != null)
                if (typeof object.data === "string")
                    $util.base64.decode(object.data, message.data = $util.newBuffer($util.base64.length(object.data)), 0);
                else if (object.data.length)
                    message.data = object.data;
            return message;
        };

        /**
         * Creates a plain object from an UploadAvatar message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.UploadAvatar
         * @static
         * @param {server.UploadAvatar} message UploadAvatar
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        UploadAvatar.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                if (options.bytes === String)
                    object.data = "";
                else {
                    object.data = [];
                    if (options.bytes !== Array)
                        object.data = $util.newBuffer(object.data);
                }
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.data != null && message.hasOwnProperty("data"))
                object.data = options.bytes === String ? $util.base64.encode(message.data, 0, message.data.length) : options.bytes === Array ? Array.prototype.slice.call(message.data) : message.data;
            return object;
        };

        /**
         * Converts this UploadAvatar to JSON.
         * @function toJSON
         * @memberof server.UploadAvatar
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        UploadAvatar.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return UploadAvatar;
    })();

    server.Avatar = (function() {

        /**
         * Properties of an Avatar.
         * @memberof server
         * @interface IAvatar
         * @property {string|null} [id] Avatar id
         * @property {number|Long|null} [uid] Avatar uid
         */

        /**
         * Constructs a new Avatar.
         * @memberof server
         * @classdesc Represents an Avatar.
         * @implements IAvatar
         * @constructor
         * @param {server.IAvatar=} [properties] Properties to set
         */
        function Avatar(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Avatar id.
         * @member {string} id
         * @memberof server.Avatar
         * @instance
         */
        Avatar.prototype.id = "";

        /**
         * Avatar uid.
         * @member {number|Long} uid
         * @memberof server.Avatar
         * @instance
         */
        Avatar.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Creates a new Avatar instance using the specified properties.
         * @function create
         * @memberof server.Avatar
         * @static
         * @param {server.IAvatar=} [properties] Properties to set
         * @returns {server.Avatar} Avatar instance
         */
        Avatar.create = function create(properties) {
            return new Avatar(properties);
        };

        /**
         * Encodes the specified Avatar message. Does not implicitly {@link server.Avatar.verify|verify} messages.
         * @function encode
         * @memberof server.Avatar
         * @static
         * @param {server.IAvatar} message Avatar message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Avatar.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 2, wireType 0 =*/16).int64(message.uid);
            return writer;
        };

        /**
         * Encodes the specified Avatar message, length delimited. Does not implicitly {@link server.Avatar.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Avatar
         * @static
         * @param {server.IAvatar} message Avatar message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Avatar.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an Avatar message from the specified reader or buffer.
         * @function decode
         * @memberof server.Avatar
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Avatar} Avatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Avatar.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Avatar();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.uid = reader.int64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an Avatar message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Avatar
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Avatar} Avatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Avatar.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an Avatar message.
         * @function verify
         * @memberof server.Avatar
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Avatar.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            return null;
        };

        /**
         * Creates an Avatar message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Avatar
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Avatar} Avatar
         */
        Avatar.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Avatar)
                return object;
            var message = new $root.server.Avatar();
            if (object.id != null)
                message.id = String(object.id);
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = false;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber();
            return message;
        };

        /**
         * Creates a plain object from an Avatar message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Avatar
         * @static
         * @param {server.Avatar} message Avatar
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Avatar.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber() : message.uid;
            return object;
        };

        /**
         * Converts this Avatar to JSON.
         * @function toJSON
         * @memberof server.Avatar
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Avatar.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Avatar;
    })();

    server.Avatars = (function() {

        /**
         * Properties of an Avatars.
         * @memberof server
         * @interface IAvatars
         * @property {Array.<server.IAvatar>|null} [avatars] Avatars avatars
         */

        /**
         * Constructs a new Avatars.
         * @memberof server
         * @classdesc Represents an Avatars.
         * @implements IAvatars
         * @constructor
         * @param {server.IAvatars=} [properties] Properties to set
         */
        function Avatars(properties) {
            this.avatars = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Avatars avatars.
         * @member {Array.<server.IAvatar>} avatars
         * @memberof server.Avatars
         * @instance
         */
        Avatars.prototype.avatars = $util.emptyArray;

        /**
         * Creates a new Avatars instance using the specified properties.
         * @function create
         * @memberof server.Avatars
         * @static
         * @param {server.IAvatars=} [properties] Properties to set
         * @returns {server.Avatars} Avatars instance
         */
        Avatars.create = function create(properties) {
            return new Avatars(properties);
        };

        /**
         * Encodes the specified Avatars message. Does not implicitly {@link server.Avatars.verify|verify} messages.
         * @function encode
         * @memberof server.Avatars
         * @static
         * @param {server.IAvatars} message Avatars message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Avatars.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.avatars != null && message.avatars.length)
                for (var i = 0; i < message.avatars.length; ++i)
                    $root.server.Avatar.encode(message.avatars[i], writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified Avatars message, length delimited. Does not implicitly {@link server.Avatars.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Avatars
         * @static
         * @param {server.IAvatars} message Avatars message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Avatars.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an Avatars message from the specified reader or buffer.
         * @function decode
         * @memberof server.Avatars
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Avatars} Avatars
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Avatars.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Avatars();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    if (!(message.avatars && message.avatars.length))
                        message.avatars = [];
                    message.avatars.push($root.server.Avatar.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an Avatars message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Avatars
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Avatars} Avatars
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Avatars.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an Avatars message.
         * @function verify
         * @memberof server.Avatars
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Avatars.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.avatars != null && message.hasOwnProperty("avatars")) {
                if (!Array.isArray(message.avatars))
                    return "avatars: array expected";
                for (var i = 0; i < message.avatars.length; ++i) {
                    var error = $root.server.Avatar.verify(message.avatars[i]);
                    if (error)
                        return "avatars." + error;
                }
            }
            return null;
        };

        /**
         * Creates an Avatars message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Avatars
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Avatars} Avatars
         */
        Avatars.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Avatars)
                return object;
            var message = new $root.server.Avatars();
            if (object.avatars) {
                if (!Array.isArray(object.avatars))
                    throw TypeError(".server.Avatars.avatars: array expected");
                message.avatars = [];
                for (var i = 0; i < object.avatars.length; ++i) {
                    if (typeof object.avatars[i] !== "object")
                        throw TypeError(".server.Avatars.avatars: object expected");
                    message.avatars[i] = $root.server.Avatar.fromObject(object.avatars[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from an Avatars message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Avatars
         * @static
         * @param {server.Avatars} message Avatars
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Avatars.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.avatars = [];
            if (message.avatars && message.avatars.length) {
                object.avatars = [];
                for (var j = 0; j < message.avatars.length; ++j)
                    object.avatars[j] = $root.server.Avatar.toObject(message.avatars[j], options);
            }
            return object;
        };

        /**
         * Converts this Avatars to JSON.
         * @function toJSON
         * @memberof server.Avatars
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Avatars.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Avatars;
    })();

    server.UploadGroupAvatar = (function() {

        /**
         * Properties of an UploadGroupAvatar.
         * @memberof server
         * @interface IUploadGroupAvatar
         * @property {string|null} [gid] UploadGroupAvatar gid
         * @property {Uint8Array|null} [data] UploadGroupAvatar data
         */

        /**
         * Constructs a new UploadGroupAvatar.
         * @memberof server
         * @classdesc Represents an UploadGroupAvatar.
         * @implements IUploadGroupAvatar
         * @constructor
         * @param {server.IUploadGroupAvatar=} [properties] Properties to set
         */
        function UploadGroupAvatar(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * UploadGroupAvatar gid.
         * @member {string} gid
         * @memberof server.UploadGroupAvatar
         * @instance
         */
        UploadGroupAvatar.prototype.gid = "";

        /**
         * UploadGroupAvatar data.
         * @member {Uint8Array} data
         * @memberof server.UploadGroupAvatar
         * @instance
         */
        UploadGroupAvatar.prototype.data = $util.newBuffer([]);

        /**
         * Creates a new UploadGroupAvatar instance using the specified properties.
         * @function create
         * @memberof server.UploadGroupAvatar
         * @static
         * @param {server.IUploadGroupAvatar=} [properties] Properties to set
         * @returns {server.UploadGroupAvatar} UploadGroupAvatar instance
         */
        UploadGroupAvatar.create = function create(properties) {
            return new UploadGroupAvatar(properties);
        };

        /**
         * Encodes the specified UploadGroupAvatar message. Does not implicitly {@link server.UploadGroupAvatar.verify|verify} messages.
         * @function encode
         * @memberof server.UploadGroupAvatar
         * @static
         * @param {server.IUploadGroupAvatar} message UploadGroupAvatar message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UploadGroupAvatar.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.gid != null && Object.hasOwnProperty.call(message, "gid"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.gid);
            if (message.data != null && Object.hasOwnProperty.call(message, "data"))
                writer.uint32(/* id 2, wireType 2 =*/18).bytes(message.data);
            return writer;
        };

        /**
         * Encodes the specified UploadGroupAvatar message, length delimited. Does not implicitly {@link server.UploadGroupAvatar.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.UploadGroupAvatar
         * @static
         * @param {server.IUploadGroupAvatar} message UploadGroupAvatar message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UploadGroupAvatar.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an UploadGroupAvatar message from the specified reader or buffer.
         * @function decode
         * @memberof server.UploadGroupAvatar
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.UploadGroupAvatar} UploadGroupAvatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        UploadGroupAvatar.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.UploadGroupAvatar();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.gid = reader.string();
                    break;
                case 2:
                    message.data = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an UploadGroupAvatar message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.UploadGroupAvatar
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.UploadGroupAvatar} UploadGroupAvatar
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        UploadGroupAvatar.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an UploadGroupAvatar message.
         * @function verify
         * @memberof server.UploadGroupAvatar
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        UploadGroupAvatar.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.gid != null && message.hasOwnProperty("gid"))
                if (!$util.isString(message.gid))
                    return "gid: string expected";
            if (message.data != null && message.hasOwnProperty("data"))
                if (!(message.data && typeof message.data.length === "number" || $util.isString(message.data)))
                    return "data: buffer expected";
            return null;
        };

        /**
         * Creates an UploadGroupAvatar message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.UploadGroupAvatar
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.UploadGroupAvatar} UploadGroupAvatar
         */
        UploadGroupAvatar.fromObject = function fromObject(object) {
            if (object instanceof $root.server.UploadGroupAvatar)
                return object;
            var message = new $root.server.UploadGroupAvatar();
            if (object.gid != null)
                message.gid = String(object.gid);
            if (object.data != null)
                if (typeof object.data === "string")
                    $util.base64.decode(object.data, message.data = $util.newBuffer($util.base64.length(object.data)), 0);
                else if (object.data.length)
                    message.data = object.data;
            return message;
        };

        /**
         * Creates a plain object from an UploadGroupAvatar message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.UploadGroupAvatar
         * @static
         * @param {server.UploadGroupAvatar} message UploadGroupAvatar
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        UploadGroupAvatar.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.gid = "";
                if (options.bytes === String)
                    object.data = "";
                else {
                    object.data = [];
                    if (options.bytes !== Array)
                        object.data = $util.newBuffer(object.data);
                }
            }
            if (message.gid != null && message.hasOwnProperty("gid"))
                object.gid = message.gid;
            if (message.data != null && message.hasOwnProperty("data"))
                object.data = options.bytes === String ? $util.base64.encode(message.data, 0, message.data.length) : options.bytes === Array ? Array.prototype.slice.call(message.data) : message.data;
            return object;
        };

        /**
         * Converts this UploadGroupAvatar to JSON.
         * @function toJSON
         * @memberof server.UploadGroupAvatar
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        UploadGroupAvatar.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return UploadGroupAvatar;
    })();

    server.CertMessage = (function() {

        /**
         * Properties of a CertMessage.
         * @memberof server
         * @interface ICertMessage
         * @property {number|Long|null} [timestamp] CertMessage timestamp
         * @property {Uint8Array|null} [serverKey] CertMessage serverKey
         */

        /**
         * Constructs a new CertMessage.
         * @memberof server
         * @classdesc Represents a CertMessage.
         * @implements ICertMessage
         * @constructor
         * @param {server.ICertMessage=} [properties] Properties to set
         */
        function CertMessage(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * CertMessage timestamp.
         * @member {number|Long} timestamp
         * @memberof server.CertMessage
         * @instance
         */
        CertMessage.prototype.timestamp = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * CertMessage serverKey.
         * @member {Uint8Array} serverKey
         * @memberof server.CertMessage
         * @instance
         */
        CertMessage.prototype.serverKey = $util.newBuffer([]);

        /**
         * Creates a new CertMessage instance using the specified properties.
         * @function create
         * @memberof server.CertMessage
         * @static
         * @param {server.ICertMessage=} [properties] Properties to set
         * @returns {server.CertMessage} CertMessage instance
         */
        CertMessage.create = function create(properties) {
            return new CertMessage(properties);
        };

        /**
         * Encodes the specified CertMessage message. Does not implicitly {@link server.CertMessage.verify|verify} messages.
         * @function encode
         * @memberof server.CertMessage
         * @static
         * @param {server.ICertMessage} message CertMessage message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        CertMessage.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.timestamp != null && Object.hasOwnProperty.call(message, "timestamp"))
                writer.uint32(/* id 1, wireType 0 =*/8).int64(message.timestamp);
            if (message.serverKey != null && Object.hasOwnProperty.call(message, "serverKey"))
                writer.uint32(/* id 2, wireType 2 =*/18).bytes(message.serverKey);
            return writer;
        };

        /**
         * Encodes the specified CertMessage message, length delimited. Does not implicitly {@link server.CertMessage.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.CertMessage
         * @static
         * @param {server.ICertMessage} message CertMessage message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        CertMessage.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a CertMessage message from the specified reader or buffer.
         * @function decode
         * @memberof server.CertMessage
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.CertMessage} CertMessage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        CertMessage.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.CertMessage();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.timestamp = reader.int64();
                    break;
                case 2:
                    message.serverKey = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a CertMessage message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.CertMessage
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.CertMessage} CertMessage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        CertMessage.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a CertMessage message.
         * @function verify
         * @memberof server.CertMessage
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        CertMessage.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (!$util.isInteger(message.timestamp) && !(message.timestamp && $util.isInteger(message.timestamp.low) && $util.isInteger(message.timestamp.high)))
                    return "timestamp: integer|Long expected";
            if (message.serverKey != null && message.hasOwnProperty("serverKey"))
                if (!(message.serverKey && typeof message.serverKey.length === "number" || $util.isString(message.serverKey)))
                    return "serverKey: buffer expected";
            return null;
        };

        /**
         * Creates a CertMessage message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.CertMessage
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.CertMessage} CertMessage
         */
        CertMessage.fromObject = function fromObject(object) {
            if (object instanceof $root.server.CertMessage)
                return object;
            var message = new $root.server.CertMessage();
            if (object.timestamp != null)
                if ($util.Long)
                    (message.timestamp = $util.Long.fromValue(object.timestamp)).unsigned = false;
                else if (typeof object.timestamp === "string")
                    message.timestamp = parseInt(object.timestamp, 10);
                else if (typeof object.timestamp === "number")
                    message.timestamp = object.timestamp;
                else if (typeof object.timestamp === "object")
                    message.timestamp = new $util.LongBits(object.timestamp.low >>> 0, object.timestamp.high >>> 0).toNumber();
            if (object.serverKey != null)
                if (typeof object.serverKey === "string")
                    $util.base64.decode(object.serverKey, message.serverKey = $util.newBuffer($util.base64.length(object.serverKey)), 0);
                else if (object.serverKey.length)
                    message.serverKey = object.serverKey;
            return message;
        };

        /**
         * Creates a plain object from a CertMessage message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.CertMessage
         * @static
         * @param {server.CertMessage} message CertMessage
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        CertMessage.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.timestamp = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.timestamp = options.longs === String ? "0" : 0;
                if (options.bytes === String)
                    object.serverKey = "";
                else {
                    object.serverKey = [];
                    if (options.bytes !== Array)
                        object.serverKey = $util.newBuffer(object.serverKey);
                }
            }
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (typeof message.timestamp === "number")
                    object.timestamp = options.longs === String ? String(message.timestamp) : message.timestamp;
                else
                    object.timestamp = options.longs === String ? $util.Long.prototype.toString.call(message.timestamp) : options.longs === Number ? new $util.LongBits(message.timestamp.low >>> 0, message.timestamp.high >>> 0).toNumber() : message.timestamp;
            if (message.serverKey != null && message.hasOwnProperty("serverKey"))
                object.serverKey = options.bytes === String ? $util.base64.encode(message.serverKey, 0, message.serverKey.length) : options.bytes === Array ? Array.prototype.slice.call(message.serverKey) : message.serverKey;
            return object;
        };

        /**
         * Converts this CertMessage to JSON.
         * @function toJSON
         * @memberof server.CertMessage
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        CertMessage.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return CertMessage;
    })();

    server.ClientMode = (function() {

        /**
         * Properties of a ClientMode.
         * @memberof server
         * @interface IClientMode
         * @property {server.ClientMode.Mode|null} [mode] ClientMode mode
         */

        /**
         * Constructs a new ClientMode.
         * @memberof server
         * @classdesc Represents a ClientMode.
         * @implements IClientMode
         * @constructor
         * @param {server.IClientMode=} [properties] Properties to set
         */
        function ClientMode(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ClientMode mode.
         * @member {server.ClientMode.Mode} mode
         * @memberof server.ClientMode
         * @instance
         */
        ClientMode.prototype.mode = 0;

        /**
         * Creates a new ClientMode instance using the specified properties.
         * @function create
         * @memberof server.ClientMode
         * @static
         * @param {server.IClientMode=} [properties] Properties to set
         * @returns {server.ClientMode} ClientMode instance
         */
        ClientMode.create = function create(properties) {
            return new ClientMode(properties);
        };

        /**
         * Encodes the specified ClientMode message. Does not implicitly {@link server.ClientMode.verify|verify} messages.
         * @function encode
         * @memberof server.ClientMode
         * @static
         * @param {server.IClientMode} message ClientMode message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ClientMode.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.mode != null && Object.hasOwnProperty.call(message, "mode"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.mode);
            return writer;
        };

        /**
         * Encodes the specified ClientMode message, length delimited. Does not implicitly {@link server.ClientMode.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.ClientMode
         * @static
         * @param {server.IClientMode} message ClientMode message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ClientMode.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ClientMode message from the specified reader or buffer.
         * @function decode
         * @memberof server.ClientMode
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.ClientMode} ClientMode
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ClientMode.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.ClientMode();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.mode = reader.int32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ClientMode message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.ClientMode
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.ClientMode} ClientMode
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ClientMode.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ClientMode message.
         * @function verify
         * @memberof server.ClientMode
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ClientMode.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.mode != null && message.hasOwnProperty("mode"))
                switch (message.mode) {
                default:
                    return "mode: enum value expected";
                case 0:
                case 1:
                    break;
                }
            return null;
        };

        /**
         * Creates a ClientMode message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.ClientMode
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.ClientMode} ClientMode
         */
        ClientMode.fromObject = function fromObject(object) {
            if (object instanceof $root.server.ClientMode)
                return object;
            var message = new $root.server.ClientMode();
            switch (object.mode) {
            case "ACTIVE":
            case 0:
                message.mode = 0;
                break;
            case "PASSIVE":
            case 1:
                message.mode = 1;
                break;
            }
            return message;
        };

        /**
         * Creates a plain object from a ClientMode message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.ClientMode
         * @static
         * @param {server.ClientMode} message ClientMode
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ClientMode.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults)
                object.mode = options.enums === String ? "ACTIVE" : 0;
            if (message.mode != null && message.hasOwnProperty("mode"))
                object.mode = options.enums === String ? $root.server.ClientMode.Mode[message.mode] : message.mode;
            return object;
        };

        /**
         * Converts this ClientMode to JSON.
         * @function toJSON
         * @memberof server.ClientMode
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ClientMode.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Mode enum.
         * @name server.ClientMode.Mode
         * @enum {number}
         * @property {number} ACTIVE=0 ACTIVE value
         * @property {number} PASSIVE=1 PASSIVE value
         */
        ClientMode.Mode = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "ACTIVE"] = 0;
            values[valuesById[1] = "PASSIVE"] = 1;
            return values;
        })();

        return ClientMode;
    })();

    server.ClientVersion = (function() {

        /**
         * Properties of a ClientVersion.
         * @memberof server
         * @interface IClientVersion
         * @property {string|null} [version] ClientVersion version
         * @property {number|Long|null} [expiresInSeconds] ClientVersion expiresInSeconds
         */

        /**
         * Constructs a new ClientVersion.
         * @memberof server
         * @classdesc Represents a ClientVersion.
         * @implements IClientVersion
         * @constructor
         * @param {server.IClientVersion=} [properties] Properties to set
         */
        function ClientVersion(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ClientVersion version.
         * @member {string} version
         * @memberof server.ClientVersion
         * @instance
         */
        ClientVersion.prototype.version = "";

        /**
         * ClientVersion expiresInSeconds.
         * @member {number|Long} expiresInSeconds
         * @memberof server.ClientVersion
         * @instance
         */
        ClientVersion.prototype.expiresInSeconds = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Creates a new ClientVersion instance using the specified properties.
         * @function create
         * @memberof server.ClientVersion
         * @static
         * @param {server.IClientVersion=} [properties] Properties to set
         * @returns {server.ClientVersion} ClientVersion instance
         */
        ClientVersion.create = function create(properties) {
            return new ClientVersion(properties);
        };

        /**
         * Encodes the specified ClientVersion message. Does not implicitly {@link server.ClientVersion.verify|verify} messages.
         * @function encode
         * @memberof server.ClientVersion
         * @static
         * @param {server.IClientVersion} message ClientVersion message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ClientVersion.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.version != null && Object.hasOwnProperty.call(message, "version"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.version);
            if (message.expiresInSeconds != null && Object.hasOwnProperty.call(message, "expiresInSeconds"))
                writer.uint32(/* id 2, wireType 0 =*/16).int64(message.expiresInSeconds);
            return writer;
        };

        /**
         * Encodes the specified ClientVersion message, length delimited. Does not implicitly {@link server.ClientVersion.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.ClientVersion
         * @static
         * @param {server.IClientVersion} message ClientVersion message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ClientVersion.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ClientVersion message from the specified reader or buffer.
         * @function decode
         * @memberof server.ClientVersion
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.ClientVersion} ClientVersion
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ClientVersion.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.ClientVersion();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.version = reader.string();
                    break;
                case 2:
                    message.expiresInSeconds = reader.int64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ClientVersion message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.ClientVersion
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.ClientVersion} ClientVersion
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ClientVersion.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ClientVersion message.
         * @function verify
         * @memberof server.ClientVersion
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ClientVersion.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.version != null && message.hasOwnProperty("version"))
                if (!$util.isString(message.version))
                    return "version: string expected";
            if (message.expiresInSeconds != null && message.hasOwnProperty("expiresInSeconds"))
                if (!$util.isInteger(message.expiresInSeconds) && !(message.expiresInSeconds && $util.isInteger(message.expiresInSeconds.low) && $util.isInteger(message.expiresInSeconds.high)))
                    return "expiresInSeconds: integer|Long expected";
            return null;
        };

        /**
         * Creates a ClientVersion message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.ClientVersion
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.ClientVersion} ClientVersion
         */
        ClientVersion.fromObject = function fromObject(object) {
            if (object instanceof $root.server.ClientVersion)
                return object;
            var message = new $root.server.ClientVersion();
            if (object.version != null)
                message.version = String(object.version);
            if (object.expiresInSeconds != null)
                if ($util.Long)
                    (message.expiresInSeconds = $util.Long.fromValue(object.expiresInSeconds)).unsigned = false;
                else if (typeof object.expiresInSeconds === "string")
                    message.expiresInSeconds = parseInt(object.expiresInSeconds, 10);
                else if (typeof object.expiresInSeconds === "number")
                    message.expiresInSeconds = object.expiresInSeconds;
                else if (typeof object.expiresInSeconds === "object")
                    message.expiresInSeconds = new $util.LongBits(object.expiresInSeconds.low >>> 0, object.expiresInSeconds.high >>> 0).toNumber();
            return message;
        };

        /**
         * Creates a plain object from a ClientVersion message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.ClientVersion
         * @static
         * @param {server.ClientVersion} message ClientVersion
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ClientVersion.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.version = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.expiresInSeconds = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.expiresInSeconds = options.longs === String ? "0" : 0;
            }
            if (message.version != null && message.hasOwnProperty("version"))
                object.version = message.version;
            if (message.expiresInSeconds != null && message.hasOwnProperty("expiresInSeconds"))
                if (typeof message.expiresInSeconds === "number")
                    object.expiresInSeconds = options.longs === String ? String(message.expiresInSeconds) : message.expiresInSeconds;
                else
                    object.expiresInSeconds = options.longs === String ? $util.Long.prototype.toString.call(message.expiresInSeconds) : options.longs === Number ? new $util.LongBits(message.expiresInSeconds.low >>> 0, message.expiresInSeconds.high >>> 0).toNumber() : message.expiresInSeconds;
            return object;
        };

        /**
         * Converts this ClientVersion to JSON.
         * @function toJSON
         * @memberof server.ClientVersion
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ClientVersion.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ClientVersion;
    })();

    server.ClientLog = (function() {

        /**
         * Properties of a ClientLog.
         * @memberof server
         * @interface IClientLog
         * @property {Array.<server.ICount>|null} [counts] ClientLog counts
         * @property {Array.<server.IEventData>|null} [events] ClientLog events
         */

        /**
         * Constructs a new ClientLog.
         * @memberof server
         * @classdesc Represents a ClientLog.
         * @implements IClientLog
         * @constructor
         * @param {server.IClientLog=} [properties] Properties to set
         */
        function ClientLog(properties) {
            this.counts = [];
            this.events = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ClientLog counts.
         * @member {Array.<server.ICount>} counts
         * @memberof server.ClientLog
         * @instance
         */
        ClientLog.prototype.counts = $util.emptyArray;

        /**
         * ClientLog events.
         * @member {Array.<server.IEventData>} events
         * @memberof server.ClientLog
         * @instance
         */
        ClientLog.prototype.events = $util.emptyArray;

        /**
         * Creates a new ClientLog instance using the specified properties.
         * @function create
         * @memberof server.ClientLog
         * @static
         * @param {server.IClientLog=} [properties] Properties to set
         * @returns {server.ClientLog} ClientLog instance
         */
        ClientLog.create = function create(properties) {
            return new ClientLog(properties);
        };

        /**
         * Encodes the specified ClientLog message. Does not implicitly {@link server.ClientLog.verify|verify} messages.
         * @function encode
         * @memberof server.ClientLog
         * @static
         * @param {server.IClientLog} message ClientLog message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ClientLog.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.counts != null && message.counts.length)
                for (var i = 0; i < message.counts.length; ++i)
                    $root.server.Count.encode(message.counts[i], writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
            if (message.events != null && message.events.length)
                for (var i = 0; i < message.events.length; ++i)
                    $root.server.EventData.encode(message.events[i], writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified ClientLog message, length delimited. Does not implicitly {@link server.ClientLog.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.ClientLog
         * @static
         * @param {server.IClientLog} message ClientLog message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ClientLog.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ClientLog message from the specified reader or buffer.
         * @function decode
         * @memberof server.ClientLog
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.ClientLog} ClientLog
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ClientLog.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.ClientLog();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    if (!(message.counts && message.counts.length))
                        message.counts = [];
                    message.counts.push($root.server.Count.decode(reader, reader.uint32()));
                    break;
                case 2:
                    if (!(message.events && message.events.length))
                        message.events = [];
                    message.events.push($root.server.EventData.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ClientLog message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.ClientLog
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.ClientLog} ClientLog
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ClientLog.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ClientLog message.
         * @function verify
         * @memberof server.ClientLog
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ClientLog.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.counts != null && message.hasOwnProperty("counts")) {
                if (!Array.isArray(message.counts))
                    return "counts: array expected";
                for (var i = 0; i < message.counts.length; ++i) {
                    var error = $root.server.Count.verify(message.counts[i]);
                    if (error)
                        return "counts." + error;
                }
            }
            if (message.events != null && message.hasOwnProperty("events")) {
                if (!Array.isArray(message.events))
                    return "events: array expected";
                for (var i = 0; i < message.events.length; ++i) {
                    var error = $root.server.EventData.verify(message.events[i]);
                    if (error)
                        return "events." + error;
                }
            }
            return null;
        };

        /**
         * Creates a ClientLog message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.ClientLog
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.ClientLog} ClientLog
         */
        ClientLog.fromObject = function fromObject(object) {
            if (object instanceof $root.server.ClientLog)
                return object;
            var message = new $root.server.ClientLog();
            if (object.counts) {
                if (!Array.isArray(object.counts))
                    throw TypeError(".server.ClientLog.counts: array expected");
                message.counts = [];
                for (var i = 0; i < object.counts.length; ++i) {
                    if (typeof object.counts[i] !== "object")
                        throw TypeError(".server.ClientLog.counts: object expected");
                    message.counts[i] = $root.server.Count.fromObject(object.counts[i]);
                }
            }
            if (object.events) {
                if (!Array.isArray(object.events))
                    throw TypeError(".server.ClientLog.events: array expected");
                message.events = [];
                for (var i = 0; i < object.events.length; ++i) {
                    if (typeof object.events[i] !== "object")
                        throw TypeError(".server.ClientLog.events: object expected");
                    message.events[i] = $root.server.EventData.fromObject(object.events[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a ClientLog message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.ClientLog
         * @static
         * @param {server.ClientLog} message ClientLog
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ClientLog.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults) {
                object.counts = [];
                object.events = [];
            }
            if (message.counts && message.counts.length) {
                object.counts = [];
                for (var j = 0; j < message.counts.length; ++j)
                    object.counts[j] = $root.server.Count.toObject(message.counts[j], options);
            }
            if (message.events && message.events.length) {
                object.events = [];
                for (var j = 0; j < message.events.length; ++j)
                    object.events[j] = $root.server.EventData.toObject(message.events[j], options);
            }
            return object;
        };

        /**
         * Converts this ClientLog to JSON.
         * @function toJSON
         * @memberof server.ClientLog
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ClientLog.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ClientLog;
    })();

    server.Count = (function() {

        /**
         * Properties of a Count.
         * @memberof server
         * @interface ICount
         * @property {string|null} [namespace] Count namespace
         * @property {string|null} [metric] Count metric
         * @property {number|Long|null} [count] Count count
         * @property {Array.<server.IDim>|null} [dims] Count dims
         */

        /**
         * Constructs a new Count.
         * @memberof server
         * @classdesc Represents a Count.
         * @implements ICount
         * @constructor
         * @param {server.ICount=} [properties] Properties to set
         */
        function Count(properties) {
            this.dims = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Count namespace.
         * @member {string} namespace
         * @memberof server.Count
         * @instance
         */
        Count.prototype.namespace = "";

        /**
         * Count metric.
         * @member {string} metric
         * @memberof server.Count
         * @instance
         */
        Count.prototype.metric = "";

        /**
         * Count count.
         * @member {number|Long} count
         * @memberof server.Count
         * @instance
         */
        Count.prototype.count = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Count dims.
         * @member {Array.<server.IDim>} dims
         * @memberof server.Count
         * @instance
         */
        Count.prototype.dims = $util.emptyArray;

        /**
         * Creates a new Count instance using the specified properties.
         * @function create
         * @memberof server.Count
         * @static
         * @param {server.ICount=} [properties] Properties to set
         * @returns {server.Count} Count instance
         */
        Count.create = function create(properties) {
            return new Count(properties);
        };

        /**
         * Encodes the specified Count message. Does not implicitly {@link server.Count.verify|verify} messages.
         * @function encode
         * @memberof server.Count
         * @static
         * @param {server.ICount} message Count message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Count.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.namespace != null && Object.hasOwnProperty.call(message, "namespace"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.namespace);
            if (message.metric != null && Object.hasOwnProperty.call(message, "metric"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.metric);
            if (message.count != null && Object.hasOwnProperty.call(message, "count"))
                writer.uint32(/* id 3, wireType 0 =*/24).int64(message.count);
            if (message.dims != null && message.dims.length)
                for (var i = 0; i < message.dims.length; ++i)
                    $root.server.Dim.encode(message.dims[i], writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified Count message, length delimited. Does not implicitly {@link server.Count.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Count
         * @static
         * @param {server.ICount} message Count message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Count.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Count message from the specified reader or buffer.
         * @function decode
         * @memberof server.Count
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Count} Count
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Count.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Count();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.namespace = reader.string();
                    break;
                case 2:
                    message.metric = reader.string();
                    break;
                case 3:
                    message.count = reader.int64();
                    break;
                case 4:
                    if (!(message.dims && message.dims.length))
                        message.dims = [];
                    message.dims.push($root.server.Dim.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Count message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Count
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Count} Count
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Count.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Count message.
         * @function verify
         * @memberof server.Count
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Count.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.namespace != null && message.hasOwnProperty("namespace"))
                if (!$util.isString(message.namespace))
                    return "namespace: string expected";
            if (message.metric != null && message.hasOwnProperty("metric"))
                if (!$util.isString(message.metric))
                    return "metric: string expected";
            if (message.count != null && message.hasOwnProperty("count"))
                if (!$util.isInteger(message.count) && !(message.count && $util.isInteger(message.count.low) && $util.isInteger(message.count.high)))
                    return "count: integer|Long expected";
            if (message.dims != null && message.hasOwnProperty("dims")) {
                if (!Array.isArray(message.dims))
                    return "dims: array expected";
                for (var i = 0; i < message.dims.length; ++i) {
                    var error = $root.server.Dim.verify(message.dims[i]);
                    if (error)
                        return "dims." + error;
                }
            }
            return null;
        };

        /**
         * Creates a Count message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Count
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Count} Count
         */
        Count.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Count)
                return object;
            var message = new $root.server.Count();
            if (object.namespace != null)
                message.namespace = String(object.namespace);
            if (object.metric != null)
                message.metric = String(object.metric);
            if (object.count != null)
                if ($util.Long)
                    (message.count = $util.Long.fromValue(object.count)).unsigned = false;
                else if (typeof object.count === "string")
                    message.count = parseInt(object.count, 10);
                else if (typeof object.count === "number")
                    message.count = object.count;
                else if (typeof object.count === "object")
                    message.count = new $util.LongBits(object.count.low >>> 0, object.count.high >>> 0).toNumber();
            if (object.dims) {
                if (!Array.isArray(object.dims))
                    throw TypeError(".server.Count.dims: array expected");
                message.dims = [];
                for (var i = 0; i < object.dims.length; ++i) {
                    if (typeof object.dims[i] !== "object")
                        throw TypeError(".server.Count.dims: object expected");
                    message.dims[i] = $root.server.Dim.fromObject(object.dims[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a Count message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Count
         * @static
         * @param {server.Count} message Count
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Count.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.dims = [];
            if (options.defaults) {
                object.namespace = "";
                object.metric = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.count = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.count = options.longs === String ? "0" : 0;
            }
            if (message.namespace != null && message.hasOwnProperty("namespace"))
                object.namespace = message.namespace;
            if (message.metric != null && message.hasOwnProperty("metric"))
                object.metric = message.metric;
            if (message.count != null && message.hasOwnProperty("count"))
                if (typeof message.count === "number")
                    object.count = options.longs === String ? String(message.count) : message.count;
                else
                    object.count = options.longs === String ? $util.Long.prototype.toString.call(message.count) : options.longs === Number ? new $util.LongBits(message.count.low >>> 0, message.count.high >>> 0).toNumber() : message.count;
            if (message.dims && message.dims.length) {
                object.dims = [];
                for (var j = 0; j < message.dims.length; ++j)
                    object.dims[j] = $root.server.Dim.toObject(message.dims[j], options);
            }
            return object;
        };

        /**
         * Converts this Count to JSON.
         * @function toJSON
         * @memberof server.Count
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Count.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Count;
    })();

    server.Dim = (function() {

        /**
         * Properties of a Dim.
         * @memberof server
         * @interface IDim
         * @property {string|null} [name] Dim name
         * @property {string|null} [value] Dim value
         */

        /**
         * Constructs a new Dim.
         * @memberof server
         * @classdesc Represents a Dim.
         * @implements IDim
         * @constructor
         * @param {server.IDim=} [properties] Properties to set
         */
        function Dim(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Dim name.
         * @member {string} name
         * @memberof server.Dim
         * @instance
         */
        Dim.prototype.name = "";

        /**
         * Dim value.
         * @member {string} value
         * @memberof server.Dim
         * @instance
         */
        Dim.prototype.value = "";

        /**
         * Creates a new Dim instance using the specified properties.
         * @function create
         * @memberof server.Dim
         * @static
         * @param {server.IDim=} [properties] Properties to set
         * @returns {server.Dim} Dim instance
         */
        Dim.create = function create(properties) {
            return new Dim(properties);
        };

        /**
         * Encodes the specified Dim message. Does not implicitly {@link server.Dim.verify|verify} messages.
         * @function encode
         * @memberof server.Dim
         * @static
         * @param {server.IDim} message Dim message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Dim.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.name);
            if (message.value != null && Object.hasOwnProperty.call(message, "value"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.value);
            return writer;
        };

        /**
         * Encodes the specified Dim message, length delimited. Does not implicitly {@link server.Dim.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Dim
         * @static
         * @param {server.IDim} message Dim message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Dim.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Dim message from the specified reader or buffer.
         * @function decode
         * @memberof server.Dim
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Dim} Dim
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Dim.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Dim();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.name = reader.string();
                    break;
                case 2:
                    message.value = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Dim message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Dim
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Dim} Dim
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Dim.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Dim message.
         * @function verify
         * @memberof server.Dim
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Dim.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.name != null && message.hasOwnProperty("name"))
                if (!$util.isString(message.name))
                    return "name: string expected";
            if (message.value != null && message.hasOwnProperty("value"))
                if (!$util.isString(message.value))
                    return "value: string expected";
            return null;
        };

        /**
         * Creates a Dim message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Dim
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Dim} Dim
         */
        Dim.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Dim)
                return object;
            var message = new $root.server.Dim();
            if (object.name != null)
                message.name = String(object.name);
            if (object.value != null)
                message.value = String(object.value);
            return message;
        };

        /**
         * Creates a plain object from a Dim message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Dim
         * @static
         * @param {server.Dim} message Dim
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Dim.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.name = "";
                object.value = "";
            }
            if (message.name != null && message.hasOwnProperty("name"))
                object.name = message.name;
            if (message.value != null && message.hasOwnProperty("value"))
                object.value = message.value;
            return object;
        };

        /**
         * Converts this Dim to JSON.
         * @function toJSON
         * @memberof server.Dim
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Dim.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Dim;
    })();

    server.Contact = (function() {

        /**
         * Properties of a Contact.
         * @memberof server
         * @interface IContact
         * @property {server.Contact.Action|null} [action] Contact action
         * @property {string|null} [raw] Contact raw
         * @property {string|null} [normalized] Contact normalized
         * @property {number|Long|null} [uid] Contact uid
         * @property {string|null} [avatarId] Contact avatarId
         * @property {server.Contact.Role|null} [role] Contact role
         * @property {string|null} [name] Contact name
         * @property {number|Long|null} [numPotentialFriends] Contact numPotentialFriends
         */

        /**
         * Constructs a new Contact.
         * @memberof server
         * @classdesc Represents a Contact.
         * @implements IContact
         * @constructor
         * @param {server.IContact=} [properties] Properties to set
         */
        function Contact(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Contact action.
         * @member {server.Contact.Action} action
         * @memberof server.Contact
         * @instance
         */
        Contact.prototype.action = 0;

        /**
         * Contact raw.
         * @member {string} raw
         * @memberof server.Contact
         * @instance
         */
        Contact.prototype.raw = "";

        /**
         * Contact normalized.
         * @member {string} normalized
         * @memberof server.Contact
         * @instance
         */
        Contact.prototype.normalized = "";

        /**
         * Contact uid.
         * @member {number|Long} uid
         * @memberof server.Contact
         * @instance
         */
        Contact.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Contact avatarId.
         * @member {string} avatarId
         * @memberof server.Contact
         * @instance
         */
        Contact.prototype.avatarId = "";

        /**
         * Contact role.
         * @member {server.Contact.Role} role
         * @memberof server.Contact
         * @instance
         */
        Contact.prototype.role = 0;

        /**
         * Contact name.
         * @member {string} name
         * @memberof server.Contact
         * @instance
         */
        Contact.prototype.name = "";

        /**
         * Contact numPotentialFriends.
         * @member {number|Long} numPotentialFriends
         * @memberof server.Contact
         * @instance
         */
        Contact.prototype.numPotentialFriends = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Creates a new Contact instance using the specified properties.
         * @function create
         * @memberof server.Contact
         * @static
         * @param {server.IContact=} [properties] Properties to set
         * @returns {server.Contact} Contact instance
         */
        Contact.create = function create(properties) {
            return new Contact(properties);
        };

        /**
         * Encodes the specified Contact message. Does not implicitly {@link server.Contact.verify|verify} messages.
         * @function encode
         * @memberof server.Contact
         * @static
         * @param {server.IContact} message Contact message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Contact.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.action != null && Object.hasOwnProperty.call(message, "action"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.action);
            if (message.raw != null && Object.hasOwnProperty.call(message, "raw"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.raw);
            if (message.normalized != null && Object.hasOwnProperty.call(message, "normalized"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.normalized);
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 4, wireType 0 =*/32).int64(message.uid);
            if (message.avatarId != null && Object.hasOwnProperty.call(message, "avatarId"))
                writer.uint32(/* id 5, wireType 2 =*/42).string(message.avatarId);
            if (message.role != null && Object.hasOwnProperty.call(message, "role"))
                writer.uint32(/* id 6, wireType 0 =*/48).int32(message.role);
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                writer.uint32(/* id 7, wireType 2 =*/58).string(message.name);
            if (message.numPotentialFriends != null && Object.hasOwnProperty.call(message, "numPotentialFriends"))
                writer.uint32(/* id 8, wireType 0 =*/64).int64(message.numPotentialFriends);
            return writer;
        };

        /**
         * Encodes the specified Contact message, length delimited. Does not implicitly {@link server.Contact.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Contact
         * @static
         * @param {server.IContact} message Contact message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Contact.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Contact message from the specified reader or buffer.
         * @function decode
         * @memberof server.Contact
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Contact} Contact
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Contact.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Contact();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.action = reader.int32();
                    break;
                case 2:
                    message.raw = reader.string();
                    break;
                case 3:
                    message.normalized = reader.string();
                    break;
                case 4:
                    message.uid = reader.int64();
                    break;
                case 5:
                    message.avatarId = reader.string();
                    break;
                case 6:
                    message.role = reader.int32();
                    break;
                case 7:
                    message.name = reader.string();
                    break;
                case 8:
                    message.numPotentialFriends = reader.int64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Contact message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Contact
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Contact} Contact
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Contact.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Contact message.
         * @function verify
         * @memberof server.Contact
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Contact.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.action != null && message.hasOwnProperty("action"))
                switch (message.action) {
                default:
                    return "action: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.raw != null && message.hasOwnProperty("raw"))
                if (!$util.isString(message.raw))
                    return "raw: string expected";
            if (message.normalized != null && message.hasOwnProperty("normalized"))
                if (!$util.isString(message.normalized))
                    return "normalized: string expected";
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                if (!$util.isString(message.avatarId))
                    return "avatarId: string expected";
            if (message.role != null && message.hasOwnProperty("role"))
                switch (message.role) {
                default:
                    return "role: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.name != null && message.hasOwnProperty("name"))
                if (!$util.isString(message.name))
                    return "name: string expected";
            if (message.numPotentialFriends != null && message.hasOwnProperty("numPotentialFriends"))
                if (!$util.isInteger(message.numPotentialFriends) && !(message.numPotentialFriends && $util.isInteger(message.numPotentialFriends.low) && $util.isInteger(message.numPotentialFriends.high)))
                    return "numPotentialFriends: integer|Long expected";
            return null;
        };

        /**
         * Creates a Contact message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Contact
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Contact} Contact
         */
        Contact.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Contact)
                return object;
            var message = new $root.server.Contact();
            switch (object.action) {
            case "ADD":
            case 0:
                message.action = 0;
                break;
            case "DELETE":
            case 1:
                message.action = 1;
                break;
            }
            if (object.raw != null)
                message.raw = String(object.raw);
            if (object.normalized != null)
                message.normalized = String(object.normalized);
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = false;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber();
            if (object.avatarId != null)
                message.avatarId = String(object.avatarId);
            switch (object.role) {
            case "FRIENDS":
            case 0:
                message.role = 0;
                break;
            case "NONE":
            case 1:
                message.role = 1;
                break;
            }
            if (object.name != null)
                message.name = String(object.name);
            if (object.numPotentialFriends != null)
                if ($util.Long)
                    (message.numPotentialFriends = $util.Long.fromValue(object.numPotentialFriends)).unsigned = false;
                else if (typeof object.numPotentialFriends === "string")
                    message.numPotentialFriends = parseInt(object.numPotentialFriends, 10);
                else if (typeof object.numPotentialFriends === "number")
                    message.numPotentialFriends = object.numPotentialFriends;
                else if (typeof object.numPotentialFriends === "object")
                    message.numPotentialFriends = new $util.LongBits(object.numPotentialFriends.low >>> 0, object.numPotentialFriends.high >>> 0).toNumber();
            return message;
        };

        /**
         * Creates a plain object from a Contact message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Contact
         * @static
         * @param {server.Contact} message Contact
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Contact.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.action = options.enums === String ? "ADD" : 0;
                object.raw = "";
                object.normalized = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
                object.avatarId = "";
                object.role = options.enums === String ? "FRIENDS" : 0;
                object.name = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.numPotentialFriends = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.numPotentialFriends = options.longs === String ? "0" : 0;
            }
            if (message.action != null && message.hasOwnProperty("action"))
                object.action = options.enums === String ? $root.server.Contact.Action[message.action] : message.action;
            if (message.raw != null && message.hasOwnProperty("raw"))
                object.raw = message.raw;
            if (message.normalized != null && message.hasOwnProperty("normalized"))
                object.normalized = message.normalized;
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber() : message.uid;
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                object.avatarId = message.avatarId;
            if (message.role != null && message.hasOwnProperty("role"))
                object.role = options.enums === String ? $root.server.Contact.Role[message.role] : message.role;
            if (message.name != null && message.hasOwnProperty("name"))
                object.name = message.name;
            if (message.numPotentialFriends != null && message.hasOwnProperty("numPotentialFriends"))
                if (typeof message.numPotentialFriends === "number")
                    object.numPotentialFriends = options.longs === String ? String(message.numPotentialFriends) : message.numPotentialFriends;
                else
                    object.numPotentialFriends = options.longs === String ? $util.Long.prototype.toString.call(message.numPotentialFriends) : options.longs === Number ? new $util.LongBits(message.numPotentialFriends.low >>> 0, message.numPotentialFriends.high >>> 0).toNumber() : message.numPotentialFriends;
            return object;
        };

        /**
         * Converts this Contact to JSON.
         * @function toJSON
         * @memberof server.Contact
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Contact.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Action enum.
         * @name server.Contact.Action
         * @enum {number}
         * @property {number} ADD=0 ADD value
         * @property {number} DELETE=1 DELETE value
         */
        Contact.Action = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "ADD"] = 0;
            values[valuesById[1] = "DELETE"] = 1;
            return values;
        })();

        /**
         * Role enum.
         * @name server.Contact.Role
         * @enum {number}
         * @property {number} FRIENDS=0 FRIENDS value
         * @property {number} NONE=1 NONE value
         */
        Contact.Role = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "FRIENDS"] = 0;
            values[valuesById[1] = "NONE"] = 1;
            return values;
        })();

        return Contact;
    })();

    server.ContactList = (function() {

        /**
         * Properties of a ContactList.
         * @memberof server
         * @interface IContactList
         * @property {server.ContactList.Type|null} [type] ContactList type
         * @property {string|null} [syncId] ContactList syncId
         * @property {number|null} [batchIndex] ContactList batchIndex
         * @property {boolean|null} [isLast] ContactList isLast
         * @property {Array.<server.IContact>|null} [contacts] ContactList contacts
         */

        /**
         * Constructs a new ContactList.
         * @memberof server
         * @classdesc Represents a ContactList.
         * @implements IContactList
         * @constructor
         * @param {server.IContactList=} [properties] Properties to set
         */
        function ContactList(properties) {
            this.contacts = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ContactList type.
         * @member {server.ContactList.Type} type
         * @memberof server.ContactList
         * @instance
         */
        ContactList.prototype.type = 0;

        /**
         * ContactList syncId.
         * @member {string} syncId
         * @memberof server.ContactList
         * @instance
         */
        ContactList.prototype.syncId = "";

        /**
         * ContactList batchIndex.
         * @member {number} batchIndex
         * @memberof server.ContactList
         * @instance
         */
        ContactList.prototype.batchIndex = 0;

        /**
         * ContactList isLast.
         * @member {boolean} isLast
         * @memberof server.ContactList
         * @instance
         */
        ContactList.prototype.isLast = false;

        /**
         * ContactList contacts.
         * @member {Array.<server.IContact>} contacts
         * @memberof server.ContactList
         * @instance
         */
        ContactList.prototype.contacts = $util.emptyArray;

        /**
         * Creates a new ContactList instance using the specified properties.
         * @function create
         * @memberof server.ContactList
         * @static
         * @param {server.IContactList=} [properties] Properties to set
         * @returns {server.ContactList} ContactList instance
         */
        ContactList.create = function create(properties) {
            return new ContactList(properties);
        };

        /**
         * Encodes the specified ContactList message. Does not implicitly {@link server.ContactList.verify|verify} messages.
         * @function encode
         * @memberof server.ContactList
         * @static
         * @param {server.IContactList} message ContactList message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ContactList.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.type);
            if (message.syncId != null && Object.hasOwnProperty.call(message, "syncId"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.syncId);
            if (message.batchIndex != null && Object.hasOwnProperty.call(message, "batchIndex"))
                writer.uint32(/* id 3, wireType 0 =*/24).int32(message.batchIndex);
            if (message.isLast != null && Object.hasOwnProperty.call(message, "isLast"))
                writer.uint32(/* id 4, wireType 0 =*/32).bool(message.isLast);
            if (message.contacts != null && message.contacts.length)
                for (var i = 0; i < message.contacts.length; ++i)
                    $root.server.Contact.encode(message.contacts[i], writer.uint32(/* id 5, wireType 2 =*/42).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified ContactList message, length delimited. Does not implicitly {@link server.ContactList.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.ContactList
         * @static
         * @param {server.IContactList} message ContactList message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ContactList.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ContactList message from the specified reader or buffer.
         * @function decode
         * @memberof server.ContactList
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.ContactList} ContactList
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ContactList.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.ContactList();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.type = reader.int32();
                    break;
                case 2:
                    message.syncId = reader.string();
                    break;
                case 3:
                    message.batchIndex = reader.int32();
                    break;
                case 4:
                    message.isLast = reader.bool();
                    break;
                case 5:
                    if (!(message.contacts && message.contacts.length))
                        message.contacts = [];
                    message.contacts.push($root.server.Contact.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ContactList message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.ContactList
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.ContactList} ContactList
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ContactList.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ContactList message.
         * @function verify
         * @memberof server.ContactList
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ContactList.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.type != null && message.hasOwnProperty("type"))
                switch (message.type) {
                default:
                    return "type: enum value expected";
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    break;
                }
            if (message.syncId != null && message.hasOwnProperty("syncId"))
                if (!$util.isString(message.syncId))
                    return "syncId: string expected";
            if (message.batchIndex != null && message.hasOwnProperty("batchIndex"))
                if (!$util.isInteger(message.batchIndex))
                    return "batchIndex: integer expected";
            if (message.isLast != null && message.hasOwnProperty("isLast"))
                if (typeof message.isLast !== "boolean")
                    return "isLast: boolean expected";
            if (message.contacts != null && message.hasOwnProperty("contacts")) {
                if (!Array.isArray(message.contacts))
                    return "contacts: array expected";
                for (var i = 0; i < message.contacts.length; ++i) {
                    var error = $root.server.Contact.verify(message.contacts[i]);
                    if (error)
                        return "contacts." + error;
                }
            }
            return null;
        };

        /**
         * Creates a ContactList message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.ContactList
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.ContactList} ContactList
         */
        ContactList.fromObject = function fromObject(object) {
            if (object instanceof $root.server.ContactList)
                return object;
            var message = new $root.server.ContactList();
            switch (object.type) {
            case "FULL":
            case 0:
                message.type = 0;
                break;
            case "DELTA":
            case 1:
                message.type = 1;
                break;
            case "NORMAL":
            case 2:
                message.type = 2;
                break;
            case "FRIEND_NOTICE":
            case 3:
                message.type = 3;
                break;
            case "INVITER_NOTICE":
            case 4:
                message.type = 4;
                break;
            }
            if (object.syncId != null)
                message.syncId = String(object.syncId);
            if (object.batchIndex != null)
                message.batchIndex = object.batchIndex | 0;
            if (object.isLast != null)
                message.isLast = Boolean(object.isLast);
            if (object.contacts) {
                if (!Array.isArray(object.contacts))
                    throw TypeError(".server.ContactList.contacts: array expected");
                message.contacts = [];
                for (var i = 0; i < object.contacts.length; ++i) {
                    if (typeof object.contacts[i] !== "object")
                        throw TypeError(".server.ContactList.contacts: object expected");
                    message.contacts[i] = $root.server.Contact.fromObject(object.contacts[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a ContactList message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.ContactList
         * @static
         * @param {server.ContactList} message ContactList
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ContactList.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.contacts = [];
            if (options.defaults) {
                object.type = options.enums === String ? "FULL" : 0;
                object.syncId = "";
                object.batchIndex = 0;
                object.isLast = false;
            }
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.server.ContactList.Type[message.type] : message.type;
            if (message.syncId != null && message.hasOwnProperty("syncId"))
                object.syncId = message.syncId;
            if (message.batchIndex != null && message.hasOwnProperty("batchIndex"))
                object.batchIndex = message.batchIndex;
            if (message.isLast != null && message.hasOwnProperty("isLast"))
                object.isLast = message.isLast;
            if (message.contacts && message.contacts.length) {
                object.contacts = [];
                for (var j = 0; j < message.contacts.length; ++j)
                    object.contacts[j] = $root.server.Contact.toObject(message.contacts[j], options);
            }
            return object;
        };

        /**
         * Converts this ContactList to JSON.
         * @function toJSON
         * @memberof server.ContactList
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ContactList.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Type enum.
         * @name server.ContactList.Type
         * @enum {number}
         * @property {number} FULL=0 FULL value
         * @property {number} DELTA=1 DELTA value
         * @property {number} NORMAL=2 NORMAL value
         * @property {number} FRIEND_NOTICE=3 FRIEND_NOTICE value
         * @property {number} INVITER_NOTICE=4 INVITER_NOTICE value
         */
        ContactList.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "FULL"] = 0;
            values[valuesById[1] = "DELTA"] = 1;
            values[valuesById[2] = "NORMAL"] = 2;
            values[valuesById[3] = "FRIEND_NOTICE"] = 3;
            values[valuesById[4] = "INVITER_NOTICE"] = 4;
            return values;
        })();

        return ContactList;
    })();

    server.ContactHash = (function() {

        /**
         * Properties of a ContactHash.
         * @memberof server
         * @interface IContactHash
         * @property {Uint8Array|null} [hash] ContactHash hash
         */

        /**
         * Constructs a new ContactHash.
         * @memberof server
         * @classdesc Represents a ContactHash.
         * @implements IContactHash
         * @constructor
         * @param {server.IContactHash=} [properties] Properties to set
         */
        function ContactHash(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ContactHash hash.
         * @member {Uint8Array} hash
         * @memberof server.ContactHash
         * @instance
         */
        ContactHash.prototype.hash = $util.newBuffer([]);

        /**
         * Creates a new ContactHash instance using the specified properties.
         * @function create
         * @memberof server.ContactHash
         * @static
         * @param {server.IContactHash=} [properties] Properties to set
         * @returns {server.ContactHash} ContactHash instance
         */
        ContactHash.create = function create(properties) {
            return new ContactHash(properties);
        };

        /**
         * Encodes the specified ContactHash message. Does not implicitly {@link server.ContactHash.verify|verify} messages.
         * @function encode
         * @memberof server.ContactHash
         * @static
         * @param {server.IContactHash} message ContactHash message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ContactHash.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.hash != null && Object.hasOwnProperty.call(message, "hash"))
                writer.uint32(/* id 1, wireType 2 =*/10).bytes(message.hash);
            return writer;
        };

        /**
         * Encodes the specified ContactHash message, length delimited. Does not implicitly {@link server.ContactHash.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.ContactHash
         * @static
         * @param {server.IContactHash} message ContactHash message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ContactHash.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ContactHash message from the specified reader or buffer.
         * @function decode
         * @memberof server.ContactHash
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.ContactHash} ContactHash
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ContactHash.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.ContactHash();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.hash = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ContactHash message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.ContactHash
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.ContactHash} ContactHash
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ContactHash.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ContactHash message.
         * @function verify
         * @memberof server.ContactHash
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ContactHash.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.hash != null && message.hasOwnProperty("hash"))
                if (!(message.hash && typeof message.hash.length === "number" || $util.isString(message.hash)))
                    return "hash: buffer expected";
            return null;
        };

        /**
         * Creates a ContactHash message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.ContactHash
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.ContactHash} ContactHash
         */
        ContactHash.fromObject = function fromObject(object) {
            if (object instanceof $root.server.ContactHash)
                return object;
            var message = new $root.server.ContactHash();
            if (object.hash != null)
                if (typeof object.hash === "string")
                    $util.base64.decode(object.hash, message.hash = $util.newBuffer($util.base64.length(object.hash)), 0);
                else if (object.hash.length)
                    message.hash = object.hash;
            return message;
        };

        /**
         * Creates a plain object from a ContactHash message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.ContactHash
         * @static
         * @param {server.ContactHash} message ContactHash
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ContactHash.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults)
                if (options.bytes === String)
                    object.hash = "";
                else {
                    object.hash = [];
                    if (options.bytes !== Array)
                        object.hash = $util.newBuffer(object.hash);
                }
            if (message.hash != null && message.hasOwnProperty("hash"))
                object.hash = options.bytes === String ? $util.base64.encode(message.hash, 0, message.hash.length) : options.bytes === Array ? Array.prototype.slice.call(message.hash) : message.hash;
            return object;
        };

        /**
         * Converts this ContactHash to JSON.
         * @function toJSON
         * @memberof server.ContactHash
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ContactHash.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ContactHash;
    })();

    server.Audience = (function() {

        /**
         * Properties of an Audience.
         * @memberof server
         * @interface IAudience
         * @property {server.Audience.Type|null} [type] Audience type
         * @property {Array.<number|Long>|null} [uids] Audience uids
         */

        /**
         * Constructs a new Audience.
         * @memberof server
         * @classdesc Represents an Audience.
         * @implements IAudience
         * @constructor
         * @param {server.IAudience=} [properties] Properties to set
         */
        function Audience(properties) {
            this.uids = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Audience type.
         * @member {server.Audience.Type} type
         * @memberof server.Audience
         * @instance
         */
        Audience.prototype.type = 0;

        /**
         * Audience uids.
         * @member {Array.<number|Long>} uids
         * @memberof server.Audience
         * @instance
         */
        Audience.prototype.uids = $util.emptyArray;

        /**
         * Creates a new Audience instance using the specified properties.
         * @function create
         * @memberof server.Audience
         * @static
         * @param {server.IAudience=} [properties] Properties to set
         * @returns {server.Audience} Audience instance
         */
        Audience.create = function create(properties) {
            return new Audience(properties);
        };

        /**
         * Encodes the specified Audience message. Does not implicitly {@link server.Audience.verify|verify} messages.
         * @function encode
         * @memberof server.Audience
         * @static
         * @param {server.IAudience} message Audience message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Audience.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.type);
            if (message.uids != null && message.uids.length) {
                writer.uint32(/* id 2, wireType 2 =*/18).fork();
                for (var i = 0; i < message.uids.length; ++i)
                    writer.int64(message.uids[i]);
                writer.ldelim();
            }
            return writer;
        };

        /**
         * Encodes the specified Audience message, length delimited. Does not implicitly {@link server.Audience.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Audience
         * @static
         * @param {server.IAudience} message Audience message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Audience.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an Audience message from the specified reader or buffer.
         * @function decode
         * @memberof server.Audience
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Audience} Audience
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Audience.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Audience();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.type = reader.int32();
                    break;
                case 2:
                    if (!(message.uids && message.uids.length))
                        message.uids = [];
                    if ((tag & 7) === 2) {
                        var end2 = reader.uint32() + reader.pos;
                        while (reader.pos < end2)
                            message.uids.push(reader.int64());
                    } else
                        message.uids.push(reader.int64());
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an Audience message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Audience
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Audience} Audience
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Audience.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an Audience message.
         * @function verify
         * @memberof server.Audience
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Audience.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.type != null && message.hasOwnProperty("type"))
                switch (message.type) {
                default:
                    return "type: enum value expected";
                case 0:
                case 1:
                case 2:
                    break;
                }
            if (message.uids != null && message.hasOwnProperty("uids")) {
                if (!Array.isArray(message.uids))
                    return "uids: array expected";
                for (var i = 0; i < message.uids.length; ++i)
                    if (!$util.isInteger(message.uids[i]) && !(message.uids[i] && $util.isInteger(message.uids[i].low) && $util.isInteger(message.uids[i].high)))
                        return "uids: integer|Long[] expected";
            }
            return null;
        };

        /**
         * Creates an Audience message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Audience
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Audience} Audience
         */
        Audience.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Audience)
                return object;
            var message = new $root.server.Audience();
            switch (object.type) {
            case "ALL":
            case 0:
                message.type = 0;
                break;
            case "EXCEPT":
            case 1:
                message.type = 1;
                break;
            case "ONLY":
            case 2:
                message.type = 2;
                break;
            }
            if (object.uids) {
                if (!Array.isArray(object.uids))
                    throw TypeError(".server.Audience.uids: array expected");
                message.uids = [];
                for (var i = 0; i < object.uids.length; ++i)
                    if ($util.Long)
                        (message.uids[i] = $util.Long.fromValue(object.uids[i])).unsigned = false;
                    else if (typeof object.uids[i] === "string")
                        message.uids[i] = parseInt(object.uids[i], 10);
                    else if (typeof object.uids[i] === "number")
                        message.uids[i] = object.uids[i];
                    else if (typeof object.uids[i] === "object")
                        message.uids[i] = new $util.LongBits(object.uids[i].low >>> 0, object.uids[i].high >>> 0).toNumber();
            }
            return message;
        };

        /**
         * Creates a plain object from an Audience message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Audience
         * @static
         * @param {server.Audience} message Audience
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Audience.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.uids = [];
            if (options.defaults)
                object.type = options.enums === String ? "ALL" : 0;
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.server.Audience.Type[message.type] : message.type;
            if (message.uids && message.uids.length) {
                object.uids = [];
                for (var j = 0; j < message.uids.length; ++j)
                    if (typeof message.uids[j] === "number")
                        object.uids[j] = options.longs === String ? String(message.uids[j]) : message.uids[j];
                    else
                        object.uids[j] = options.longs === String ? $util.Long.prototype.toString.call(message.uids[j]) : options.longs === Number ? new $util.LongBits(message.uids[j].low >>> 0, message.uids[j].high >>> 0).toNumber() : message.uids[j];
            }
            return object;
        };

        /**
         * Converts this Audience to JSON.
         * @function toJSON
         * @memberof server.Audience
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Audience.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Type enum.
         * @name server.Audience.Type
         * @enum {number}
         * @property {number} ALL=0 ALL value
         * @property {number} EXCEPT=1 EXCEPT value
         * @property {number} ONLY=2 ONLY value
         */
        Audience.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "ALL"] = 0;
            values[valuesById[1] = "EXCEPT"] = 1;
            values[valuesById[2] = "ONLY"] = 2;
            return values;
        })();

        return Audience;
    })();

    server.Post = (function() {

        /**
         * Properties of a Post.
         * @memberof server
         * @interface IPost
         * @property {string|null} [id] Post id
         * @property {number|Long|null} [publisherUid] Post publisherUid
         * @property {Uint8Array|null} [payload] Post payload
         * @property {server.IAudience|null} [audience] Post audience
         * @property {number|Long|null} [timestamp] Post timestamp
         * @property {string|null} [publisherName] Post publisherName
         */

        /**
         * Constructs a new Post.
         * @memberof server
         * @classdesc Represents a Post.
         * @implements IPost
         * @constructor
         * @param {server.IPost=} [properties] Properties to set
         */
        function Post(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Post id.
         * @member {string} id
         * @memberof server.Post
         * @instance
         */
        Post.prototype.id = "";

        /**
         * Post publisherUid.
         * @member {number|Long} publisherUid
         * @memberof server.Post
         * @instance
         */
        Post.prototype.publisherUid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Post payload.
         * @member {Uint8Array} payload
         * @memberof server.Post
         * @instance
         */
        Post.prototype.payload = $util.newBuffer([]);

        /**
         * Post audience.
         * @member {server.IAudience|null|undefined} audience
         * @memberof server.Post
         * @instance
         */
        Post.prototype.audience = null;

        /**
         * Post timestamp.
         * @member {number|Long} timestamp
         * @memberof server.Post
         * @instance
         */
        Post.prototype.timestamp = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Post publisherName.
         * @member {string} publisherName
         * @memberof server.Post
         * @instance
         */
        Post.prototype.publisherName = "";

        /**
         * Creates a new Post instance using the specified properties.
         * @function create
         * @memberof server.Post
         * @static
         * @param {server.IPost=} [properties] Properties to set
         * @returns {server.Post} Post instance
         */
        Post.create = function create(properties) {
            return new Post(properties);
        };

        /**
         * Encodes the specified Post message. Does not implicitly {@link server.Post.verify|verify} messages.
         * @function encode
         * @memberof server.Post
         * @static
         * @param {server.IPost} message Post message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Post.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.publisherUid != null && Object.hasOwnProperty.call(message, "publisherUid"))
                writer.uint32(/* id 2, wireType 0 =*/16).int64(message.publisherUid);
            if (message.payload != null && Object.hasOwnProperty.call(message, "payload"))
                writer.uint32(/* id 3, wireType 2 =*/26).bytes(message.payload);
            if (message.audience != null && Object.hasOwnProperty.call(message, "audience"))
                $root.server.Audience.encode(message.audience, writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            if (message.timestamp != null && Object.hasOwnProperty.call(message, "timestamp"))
                writer.uint32(/* id 5, wireType 0 =*/40).int64(message.timestamp);
            if (message.publisherName != null && Object.hasOwnProperty.call(message, "publisherName"))
                writer.uint32(/* id 6, wireType 2 =*/50).string(message.publisherName);
            return writer;
        };

        /**
         * Encodes the specified Post message, length delimited. Does not implicitly {@link server.Post.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Post
         * @static
         * @param {server.IPost} message Post message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Post.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Post message from the specified reader or buffer.
         * @function decode
         * @memberof server.Post
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Post} Post
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Post.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Post();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.publisherUid = reader.int64();
                    break;
                case 3:
                    message.payload = reader.bytes();
                    break;
                case 4:
                    message.audience = $root.server.Audience.decode(reader, reader.uint32());
                    break;
                case 5:
                    message.timestamp = reader.int64();
                    break;
                case 6:
                    message.publisherName = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Post message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Post
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Post} Post
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Post.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Post message.
         * @function verify
         * @memberof server.Post
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Post.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.publisherUid != null && message.hasOwnProperty("publisherUid"))
                if (!$util.isInteger(message.publisherUid) && !(message.publisherUid && $util.isInteger(message.publisherUid.low) && $util.isInteger(message.publisherUid.high)))
                    return "publisherUid: integer|Long expected";
            if (message.payload != null && message.hasOwnProperty("payload"))
                if (!(message.payload && typeof message.payload.length === "number" || $util.isString(message.payload)))
                    return "payload: buffer expected";
            if (message.audience != null && message.hasOwnProperty("audience")) {
                var error = $root.server.Audience.verify(message.audience);
                if (error)
                    return "audience." + error;
            }
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (!$util.isInteger(message.timestamp) && !(message.timestamp && $util.isInteger(message.timestamp.low) && $util.isInteger(message.timestamp.high)))
                    return "timestamp: integer|Long expected";
            if (message.publisherName != null && message.hasOwnProperty("publisherName"))
                if (!$util.isString(message.publisherName))
                    return "publisherName: string expected";
            return null;
        };

        /**
         * Creates a Post message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Post
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Post} Post
         */
        Post.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Post)
                return object;
            var message = new $root.server.Post();
            if (object.id != null)
                message.id = String(object.id);
            if (object.publisherUid != null)
                if ($util.Long)
                    (message.publisherUid = $util.Long.fromValue(object.publisherUid)).unsigned = false;
                else if (typeof object.publisherUid === "string")
                    message.publisherUid = parseInt(object.publisherUid, 10);
                else if (typeof object.publisherUid === "number")
                    message.publisherUid = object.publisherUid;
                else if (typeof object.publisherUid === "object")
                    message.publisherUid = new $util.LongBits(object.publisherUid.low >>> 0, object.publisherUid.high >>> 0).toNumber();
            if (object.payload != null)
                if (typeof object.payload === "string")
                    $util.base64.decode(object.payload, message.payload = $util.newBuffer($util.base64.length(object.payload)), 0);
                else if (object.payload.length)
                    message.payload = object.payload;
            if (object.audience != null) {
                if (typeof object.audience !== "object")
                    throw TypeError(".server.Post.audience: object expected");
                message.audience = $root.server.Audience.fromObject(object.audience);
            }
            if (object.timestamp != null)
                if ($util.Long)
                    (message.timestamp = $util.Long.fromValue(object.timestamp)).unsigned = false;
                else if (typeof object.timestamp === "string")
                    message.timestamp = parseInt(object.timestamp, 10);
                else if (typeof object.timestamp === "number")
                    message.timestamp = object.timestamp;
                else if (typeof object.timestamp === "object")
                    message.timestamp = new $util.LongBits(object.timestamp.low >>> 0, object.timestamp.high >>> 0).toNumber();
            if (object.publisherName != null)
                message.publisherName = String(object.publisherName);
            return message;
        };

        /**
         * Creates a plain object from a Post message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Post
         * @static
         * @param {server.Post} message Post
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Post.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.publisherUid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.publisherUid = options.longs === String ? "0" : 0;
                if (options.bytes === String)
                    object.payload = "";
                else {
                    object.payload = [];
                    if (options.bytes !== Array)
                        object.payload = $util.newBuffer(object.payload);
                }
                object.audience = null;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.timestamp = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.timestamp = options.longs === String ? "0" : 0;
                object.publisherName = "";
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.publisherUid != null && message.hasOwnProperty("publisherUid"))
                if (typeof message.publisherUid === "number")
                    object.publisherUid = options.longs === String ? String(message.publisherUid) : message.publisherUid;
                else
                    object.publisherUid = options.longs === String ? $util.Long.prototype.toString.call(message.publisherUid) : options.longs === Number ? new $util.LongBits(message.publisherUid.low >>> 0, message.publisherUid.high >>> 0).toNumber() : message.publisherUid;
            if (message.payload != null && message.hasOwnProperty("payload"))
                object.payload = options.bytes === String ? $util.base64.encode(message.payload, 0, message.payload.length) : options.bytes === Array ? Array.prototype.slice.call(message.payload) : message.payload;
            if (message.audience != null && message.hasOwnProperty("audience"))
                object.audience = $root.server.Audience.toObject(message.audience, options);
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (typeof message.timestamp === "number")
                    object.timestamp = options.longs === String ? String(message.timestamp) : message.timestamp;
                else
                    object.timestamp = options.longs === String ? $util.Long.prototype.toString.call(message.timestamp) : options.longs === Number ? new $util.LongBits(message.timestamp.low >>> 0, message.timestamp.high >>> 0).toNumber() : message.timestamp;
            if (message.publisherName != null && message.hasOwnProperty("publisherName"))
                object.publisherName = message.publisherName;
            return object;
        };

        /**
         * Converts this Post to JSON.
         * @function toJSON
         * @memberof server.Post
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Post.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Post;
    })();

    server.Comment = (function() {

        /**
         * Properties of a Comment.
         * @memberof server
         * @interface IComment
         * @property {string|null} [id] Comment id
         * @property {string|null} [postId] Comment postId
         * @property {string|null} [parentCommentId] Comment parentCommentId
         * @property {number|Long|null} [publisherUid] Comment publisherUid
         * @property {string|null} [publisherName] Comment publisherName
         * @property {Uint8Array|null} [payload] Comment payload
         * @property {number|Long|null} [timestamp] Comment timestamp
         */

        /**
         * Constructs a new Comment.
         * @memberof server
         * @classdesc Represents a Comment.
         * @implements IComment
         * @constructor
         * @param {server.IComment=} [properties] Properties to set
         */
        function Comment(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Comment id.
         * @member {string} id
         * @memberof server.Comment
         * @instance
         */
        Comment.prototype.id = "";

        /**
         * Comment postId.
         * @member {string} postId
         * @memberof server.Comment
         * @instance
         */
        Comment.prototype.postId = "";

        /**
         * Comment parentCommentId.
         * @member {string} parentCommentId
         * @memberof server.Comment
         * @instance
         */
        Comment.prototype.parentCommentId = "";

        /**
         * Comment publisherUid.
         * @member {number|Long} publisherUid
         * @memberof server.Comment
         * @instance
         */
        Comment.prototype.publisherUid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Comment publisherName.
         * @member {string} publisherName
         * @memberof server.Comment
         * @instance
         */
        Comment.prototype.publisherName = "";

        /**
         * Comment payload.
         * @member {Uint8Array} payload
         * @memberof server.Comment
         * @instance
         */
        Comment.prototype.payload = $util.newBuffer([]);

        /**
         * Comment timestamp.
         * @member {number|Long} timestamp
         * @memberof server.Comment
         * @instance
         */
        Comment.prototype.timestamp = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Creates a new Comment instance using the specified properties.
         * @function create
         * @memberof server.Comment
         * @static
         * @param {server.IComment=} [properties] Properties to set
         * @returns {server.Comment} Comment instance
         */
        Comment.create = function create(properties) {
            return new Comment(properties);
        };

        /**
         * Encodes the specified Comment message. Does not implicitly {@link server.Comment.verify|verify} messages.
         * @function encode
         * @memberof server.Comment
         * @static
         * @param {server.IComment} message Comment message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Comment.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.postId != null && Object.hasOwnProperty.call(message, "postId"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.postId);
            if (message.parentCommentId != null && Object.hasOwnProperty.call(message, "parentCommentId"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.parentCommentId);
            if (message.publisherUid != null && Object.hasOwnProperty.call(message, "publisherUid"))
                writer.uint32(/* id 4, wireType 0 =*/32).int64(message.publisherUid);
            if (message.publisherName != null && Object.hasOwnProperty.call(message, "publisherName"))
                writer.uint32(/* id 5, wireType 2 =*/42).string(message.publisherName);
            if (message.payload != null && Object.hasOwnProperty.call(message, "payload"))
                writer.uint32(/* id 6, wireType 2 =*/50).bytes(message.payload);
            if (message.timestamp != null && Object.hasOwnProperty.call(message, "timestamp"))
                writer.uint32(/* id 7, wireType 0 =*/56).int64(message.timestamp);
            return writer;
        };

        /**
         * Encodes the specified Comment message, length delimited. Does not implicitly {@link server.Comment.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Comment
         * @static
         * @param {server.IComment} message Comment message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Comment.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Comment message from the specified reader or buffer.
         * @function decode
         * @memberof server.Comment
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Comment} Comment
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Comment.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Comment();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.postId = reader.string();
                    break;
                case 3:
                    message.parentCommentId = reader.string();
                    break;
                case 4:
                    message.publisherUid = reader.int64();
                    break;
                case 5:
                    message.publisherName = reader.string();
                    break;
                case 6:
                    message.payload = reader.bytes();
                    break;
                case 7:
                    message.timestamp = reader.int64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Comment message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Comment
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Comment} Comment
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Comment.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Comment message.
         * @function verify
         * @memberof server.Comment
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Comment.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.postId != null && message.hasOwnProperty("postId"))
                if (!$util.isString(message.postId))
                    return "postId: string expected";
            if (message.parentCommentId != null && message.hasOwnProperty("parentCommentId"))
                if (!$util.isString(message.parentCommentId))
                    return "parentCommentId: string expected";
            if (message.publisherUid != null && message.hasOwnProperty("publisherUid"))
                if (!$util.isInteger(message.publisherUid) && !(message.publisherUid && $util.isInteger(message.publisherUid.low) && $util.isInteger(message.publisherUid.high)))
                    return "publisherUid: integer|Long expected";
            if (message.publisherName != null && message.hasOwnProperty("publisherName"))
                if (!$util.isString(message.publisherName))
                    return "publisherName: string expected";
            if (message.payload != null && message.hasOwnProperty("payload"))
                if (!(message.payload && typeof message.payload.length === "number" || $util.isString(message.payload)))
                    return "payload: buffer expected";
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (!$util.isInteger(message.timestamp) && !(message.timestamp && $util.isInteger(message.timestamp.low) && $util.isInteger(message.timestamp.high)))
                    return "timestamp: integer|Long expected";
            return null;
        };

        /**
         * Creates a Comment message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Comment
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Comment} Comment
         */
        Comment.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Comment)
                return object;
            var message = new $root.server.Comment();
            if (object.id != null)
                message.id = String(object.id);
            if (object.postId != null)
                message.postId = String(object.postId);
            if (object.parentCommentId != null)
                message.parentCommentId = String(object.parentCommentId);
            if (object.publisherUid != null)
                if ($util.Long)
                    (message.publisherUid = $util.Long.fromValue(object.publisherUid)).unsigned = false;
                else if (typeof object.publisherUid === "string")
                    message.publisherUid = parseInt(object.publisherUid, 10);
                else if (typeof object.publisherUid === "number")
                    message.publisherUid = object.publisherUid;
                else if (typeof object.publisherUid === "object")
                    message.publisherUid = new $util.LongBits(object.publisherUid.low >>> 0, object.publisherUid.high >>> 0).toNumber();
            if (object.publisherName != null)
                message.publisherName = String(object.publisherName);
            if (object.payload != null)
                if (typeof object.payload === "string")
                    $util.base64.decode(object.payload, message.payload = $util.newBuffer($util.base64.length(object.payload)), 0);
                else if (object.payload.length)
                    message.payload = object.payload;
            if (object.timestamp != null)
                if ($util.Long)
                    (message.timestamp = $util.Long.fromValue(object.timestamp)).unsigned = false;
                else if (typeof object.timestamp === "string")
                    message.timestamp = parseInt(object.timestamp, 10);
                else if (typeof object.timestamp === "number")
                    message.timestamp = object.timestamp;
                else if (typeof object.timestamp === "object")
                    message.timestamp = new $util.LongBits(object.timestamp.low >>> 0, object.timestamp.high >>> 0).toNumber();
            return message;
        };

        /**
         * Creates a plain object from a Comment message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Comment
         * @static
         * @param {server.Comment} message Comment
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Comment.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.postId = "";
                object.parentCommentId = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.publisherUid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.publisherUid = options.longs === String ? "0" : 0;
                object.publisherName = "";
                if (options.bytes === String)
                    object.payload = "";
                else {
                    object.payload = [];
                    if (options.bytes !== Array)
                        object.payload = $util.newBuffer(object.payload);
                }
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.timestamp = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.timestamp = options.longs === String ? "0" : 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.postId != null && message.hasOwnProperty("postId"))
                object.postId = message.postId;
            if (message.parentCommentId != null && message.hasOwnProperty("parentCommentId"))
                object.parentCommentId = message.parentCommentId;
            if (message.publisherUid != null && message.hasOwnProperty("publisherUid"))
                if (typeof message.publisherUid === "number")
                    object.publisherUid = options.longs === String ? String(message.publisherUid) : message.publisherUid;
                else
                    object.publisherUid = options.longs === String ? $util.Long.prototype.toString.call(message.publisherUid) : options.longs === Number ? new $util.LongBits(message.publisherUid.low >>> 0, message.publisherUid.high >>> 0).toNumber() : message.publisherUid;
            if (message.publisherName != null && message.hasOwnProperty("publisherName"))
                object.publisherName = message.publisherName;
            if (message.payload != null && message.hasOwnProperty("payload"))
                object.payload = options.bytes === String ? $util.base64.encode(message.payload, 0, message.payload.length) : options.bytes === Array ? Array.prototype.slice.call(message.payload) : message.payload;
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (typeof message.timestamp === "number")
                    object.timestamp = options.longs === String ? String(message.timestamp) : message.timestamp;
                else
                    object.timestamp = options.longs === String ? $util.Long.prototype.toString.call(message.timestamp) : options.longs === Number ? new $util.LongBits(message.timestamp.low >>> 0, message.timestamp.high >>> 0).toNumber() : message.timestamp;
            return object;
        };

        /**
         * Converts this Comment to JSON.
         * @function toJSON
         * @memberof server.Comment
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Comment.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Comment;
    })();

    server.ShareStanza = (function() {

        /**
         * Properties of a ShareStanza.
         * @memberof server
         * @interface IShareStanza
         * @property {number|Long|null} [uid] ShareStanza uid
         * @property {Array.<string>|null} [postIds] ShareStanza postIds
         * @property {string|null} [result] ShareStanza result
         * @property {string|null} [reason] ShareStanza reason
         */

        /**
         * Constructs a new ShareStanza.
         * @memberof server
         * @classdesc Represents a ShareStanza.
         * @implements IShareStanza
         * @constructor
         * @param {server.IShareStanza=} [properties] Properties to set
         */
        function ShareStanza(properties) {
            this.postIds = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ShareStanza uid.
         * @member {number|Long} uid
         * @memberof server.ShareStanza
         * @instance
         */
        ShareStanza.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * ShareStanza postIds.
         * @member {Array.<string>} postIds
         * @memberof server.ShareStanza
         * @instance
         */
        ShareStanza.prototype.postIds = $util.emptyArray;

        /**
         * ShareStanza result.
         * @member {string} result
         * @memberof server.ShareStanza
         * @instance
         */
        ShareStanza.prototype.result = "";

        /**
         * ShareStanza reason.
         * @member {string} reason
         * @memberof server.ShareStanza
         * @instance
         */
        ShareStanza.prototype.reason = "";

        /**
         * Creates a new ShareStanza instance using the specified properties.
         * @function create
         * @memberof server.ShareStanza
         * @static
         * @param {server.IShareStanza=} [properties] Properties to set
         * @returns {server.ShareStanza} ShareStanza instance
         */
        ShareStanza.create = function create(properties) {
            return new ShareStanza(properties);
        };

        /**
         * Encodes the specified ShareStanza message. Does not implicitly {@link server.ShareStanza.verify|verify} messages.
         * @function encode
         * @memberof server.ShareStanza
         * @static
         * @param {server.IShareStanza} message ShareStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ShareStanza.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 1, wireType 0 =*/8).int64(message.uid);
            if (message.postIds != null && message.postIds.length)
                for (var i = 0; i < message.postIds.length; ++i)
                    writer.uint32(/* id 2, wireType 2 =*/18).string(message.postIds[i]);
            if (message.result != null && Object.hasOwnProperty.call(message, "result"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.result);
            if (message.reason != null && Object.hasOwnProperty.call(message, "reason"))
                writer.uint32(/* id 4, wireType 2 =*/34).string(message.reason);
            return writer;
        };

        /**
         * Encodes the specified ShareStanza message, length delimited. Does not implicitly {@link server.ShareStanza.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.ShareStanza
         * @static
         * @param {server.IShareStanza} message ShareStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ShareStanza.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ShareStanza message from the specified reader or buffer.
         * @function decode
         * @memberof server.ShareStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.ShareStanza} ShareStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ShareStanza.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.ShareStanza();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.uid = reader.int64();
                    break;
                case 2:
                    if (!(message.postIds && message.postIds.length))
                        message.postIds = [];
                    message.postIds.push(reader.string());
                    break;
                case 3:
                    message.result = reader.string();
                    break;
                case 4:
                    message.reason = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ShareStanza message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.ShareStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.ShareStanza} ShareStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ShareStanza.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ShareStanza message.
         * @function verify
         * @memberof server.ShareStanza
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ShareStanza.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            if (message.postIds != null && message.hasOwnProperty("postIds")) {
                if (!Array.isArray(message.postIds))
                    return "postIds: array expected";
                for (var i = 0; i < message.postIds.length; ++i)
                    if (!$util.isString(message.postIds[i]))
                        return "postIds: string[] expected";
            }
            if (message.result != null && message.hasOwnProperty("result"))
                if (!$util.isString(message.result))
                    return "result: string expected";
            if (message.reason != null && message.hasOwnProperty("reason"))
                if (!$util.isString(message.reason))
                    return "reason: string expected";
            return null;
        };

        /**
         * Creates a ShareStanza message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.ShareStanza
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.ShareStanza} ShareStanza
         */
        ShareStanza.fromObject = function fromObject(object) {
            if (object instanceof $root.server.ShareStanza)
                return object;
            var message = new $root.server.ShareStanza();
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = false;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber();
            if (object.postIds) {
                if (!Array.isArray(object.postIds))
                    throw TypeError(".server.ShareStanza.postIds: array expected");
                message.postIds = [];
                for (var i = 0; i < object.postIds.length; ++i)
                    message.postIds[i] = String(object.postIds[i]);
            }
            if (object.result != null)
                message.result = String(object.result);
            if (object.reason != null)
                message.reason = String(object.reason);
            return message;
        };

        /**
         * Creates a plain object from a ShareStanza message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.ShareStanza
         * @static
         * @param {server.ShareStanza} message ShareStanza
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ShareStanza.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.postIds = [];
            if (options.defaults) {
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
                object.result = "";
                object.reason = "";
            }
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber() : message.uid;
            if (message.postIds && message.postIds.length) {
                object.postIds = [];
                for (var j = 0; j < message.postIds.length; ++j)
                    object.postIds[j] = message.postIds[j];
            }
            if (message.result != null && message.hasOwnProperty("result"))
                object.result = message.result;
            if (message.reason != null && message.hasOwnProperty("reason"))
                object.reason = message.reason;
            return object;
        };

        /**
         * Converts this ShareStanza to JSON.
         * @function toJSON
         * @memberof server.ShareStanza
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ShareStanza.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ShareStanza;
    })();

    server.FeedItem = (function() {

        /**
         * Properties of a FeedItem.
         * @memberof server
         * @interface IFeedItem
         * @property {server.FeedItem.Action|null} [action] FeedItem action
         * @property {server.IPost|null} [post] FeedItem post
         * @property {server.IComment|null} [comment] FeedItem comment
         * @property {Array.<server.IShareStanza>|null} [shareStanzas] FeedItem shareStanzas
         */

        /**
         * Constructs a new FeedItem.
         * @memberof server
         * @classdesc Represents a FeedItem.
         * @implements IFeedItem
         * @constructor
         * @param {server.IFeedItem=} [properties] Properties to set
         */
        function FeedItem(properties) {
            this.shareStanzas = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * FeedItem action.
         * @member {server.FeedItem.Action} action
         * @memberof server.FeedItem
         * @instance
         */
        FeedItem.prototype.action = 0;

        /**
         * FeedItem post.
         * @member {server.IPost|null|undefined} post
         * @memberof server.FeedItem
         * @instance
         */
        FeedItem.prototype.post = null;

        /**
         * FeedItem comment.
         * @member {server.IComment|null|undefined} comment
         * @memberof server.FeedItem
         * @instance
         */
        FeedItem.prototype.comment = null;

        /**
         * FeedItem shareStanzas.
         * @member {Array.<server.IShareStanza>} shareStanzas
         * @memberof server.FeedItem
         * @instance
         */
        FeedItem.prototype.shareStanzas = $util.emptyArray;

        // OneOf field names bound to virtual getters and setters
        var $oneOfFields;

        /**
         * FeedItem item.
         * @member {"post"|"comment"|undefined} item
         * @memberof server.FeedItem
         * @instance
         */
        Object.defineProperty(FeedItem.prototype, "item", {
            get: $util.oneOfGetter($oneOfFields = ["post", "comment"]),
            set: $util.oneOfSetter($oneOfFields)
        });

        /**
         * Creates a new FeedItem instance using the specified properties.
         * @function create
         * @memberof server.FeedItem
         * @static
         * @param {server.IFeedItem=} [properties] Properties to set
         * @returns {server.FeedItem} FeedItem instance
         */
        FeedItem.create = function create(properties) {
            return new FeedItem(properties);
        };

        /**
         * Encodes the specified FeedItem message. Does not implicitly {@link server.FeedItem.verify|verify} messages.
         * @function encode
         * @memberof server.FeedItem
         * @static
         * @param {server.IFeedItem} message FeedItem message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        FeedItem.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.action != null && Object.hasOwnProperty.call(message, "action"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.action);
            if (message.post != null && Object.hasOwnProperty.call(message, "post"))
                $root.server.Post.encode(message.post, writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            if (message.comment != null && Object.hasOwnProperty.call(message, "comment"))
                $root.server.Comment.encode(message.comment, writer.uint32(/* id 3, wireType 2 =*/26).fork()).ldelim();
            if (message.shareStanzas != null && message.shareStanzas.length)
                for (var i = 0; i < message.shareStanzas.length; ++i)
                    $root.server.ShareStanza.encode(message.shareStanzas[i], writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified FeedItem message, length delimited. Does not implicitly {@link server.FeedItem.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.FeedItem
         * @static
         * @param {server.IFeedItem} message FeedItem message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        FeedItem.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a FeedItem message from the specified reader or buffer.
         * @function decode
         * @memberof server.FeedItem
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.FeedItem} FeedItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        FeedItem.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.FeedItem();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.action = reader.int32();
                    break;
                case 2:
                    message.post = $root.server.Post.decode(reader, reader.uint32());
                    break;
                case 3:
                    message.comment = $root.server.Comment.decode(reader, reader.uint32());
                    break;
                case 4:
                    if (!(message.shareStanzas && message.shareStanzas.length))
                        message.shareStanzas = [];
                    message.shareStanzas.push($root.server.ShareStanza.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a FeedItem message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.FeedItem
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.FeedItem} FeedItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        FeedItem.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a FeedItem message.
         * @function verify
         * @memberof server.FeedItem
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        FeedItem.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            var properties = {};
            if (message.action != null && message.hasOwnProperty("action"))
                switch (message.action) {
                default:
                    return "action: enum value expected";
                case 0:
                case 1:
                case 2:
                    break;
                }
            if (message.post != null && message.hasOwnProperty("post")) {
                properties.item = 1;
                {
                    var error = $root.server.Post.verify(message.post);
                    if (error)
                        return "post." + error;
                }
            }
            if (message.comment != null && message.hasOwnProperty("comment")) {
                if (properties.item === 1)
                    return "item: multiple values";
                properties.item = 1;
                {
                    var error = $root.server.Comment.verify(message.comment);
                    if (error)
                        return "comment." + error;
                }
            }
            if (message.shareStanzas != null && message.hasOwnProperty("shareStanzas")) {
                if (!Array.isArray(message.shareStanzas))
                    return "shareStanzas: array expected";
                for (var i = 0; i < message.shareStanzas.length; ++i) {
                    var error = $root.server.ShareStanza.verify(message.shareStanzas[i]);
                    if (error)
                        return "shareStanzas." + error;
                }
            }
            return null;
        };

        /**
         * Creates a FeedItem message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.FeedItem
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.FeedItem} FeedItem
         */
        FeedItem.fromObject = function fromObject(object) {
            if (object instanceof $root.server.FeedItem)
                return object;
            var message = new $root.server.FeedItem();
            switch (object.action) {
            case "PUBLISH":
            case 0:
                message.action = 0;
                break;
            case "RETRACT":
            case 1:
                message.action = 1;
                break;
            case "SHARE":
            case 2:
                message.action = 2;
                break;
            }
            if (object.post != null) {
                if (typeof object.post !== "object")
                    throw TypeError(".server.FeedItem.post: object expected");
                message.post = $root.server.Post.fromObject(object.post);
            }
            if (object.comment != null) {
                if (typeof object.comment !== "object")
                    throw TypeError(".server.FeedItem.comment: object expected");
                message.comment = $root.server.Comment.fromObject(object.comment);
            }
            if (object.shareStanzas) {
                if (!Array.isArray(object.shareStanzas))
                    throw TypeError(".server.FeedItem.shareStanzas: array expected");
                message.shareStanzas = [];
                for (var i = 0; i < object.shareStanzas.length; ++i) {
                    if (typeof object.shareStanzas[i] !== "object")
                        throw TypeError(".server.FeedItem.shareStanzas: object expected");
                    message.shareStanzas[i] = $root.server.ShareStanza.fromObject(object.shareStanzas[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a FeedItem message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.FeedItem
         * @static
         * @param {server.FeedItem} message FeedItem
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        FeedItem.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.shareStanzas = [];
            if (options.defaults)
                object.action = options.enums === String ? "PUBLISH" : 0;
            if (message.action != null && message.hasOwnProperty("action"))
                object.action = options.enums === String ? $root.server.FeedItem.Action[message.action] : message.action;
            if (message.post != null && message.hasOwnProperty("post")) {
                object.post = $root.server.Post.toObject(message.post, options);
                if (options.oneofs)
                    object.item = "post";
            }
            if (message.comment != null && message.hasOwnProperty("comment")) {
                object.comment = $root.server.Comment.toObject(message.comment, options);
                if (options.oneofs)
                    object.item = "comment";
            }
            if (message.shareStanzas && message.shareStanzas.length) {
                object.shareStanzas = [];
                for (var j = 0; j < message.shareStanzas.length; ++j)
                    object.shareStanzas[j] = $root.server.ShareStanza.toObject(message.shareStanzas[j], options);
            }
            return object;
        };

        /**
         * Converts this FeedItem to JSON.
         * @function toJSON
         * @memberof server.FeedItem
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        FeedItem.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Action enum.
         * @name server.FeedItem.Action
         * @enum {number}
         * @property {number} PUBLISH=0 PUBLISH value
         * @property {number} RETRACT=1 RETRACT value
         * @property {number} SHARE=2 SHARE value
         */
        FeedItem.Action = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "PUBLISH"] = 0;
            values[valuesById[1] = "RETRACT"] = 1;
            values[valuesById[2] = "SHARE"] = 2;
            return values;
        })();

        return FeedItem;
    })();

    server.FeedItems = (function() {

        /**
         * Properties of a FeedItems.
         * @memberof server
         * @interface IFeedItems
         * @property {number|Long|null} [uid] FeedItems uid
         * @property {Array.<server.IFeedItem>|null} [items] FeedItems items
         */

        /**
         * Constructs a new FeedItems.
         * @memberof server
         * @classdesc Represents a FeedItems.
         * @implements IFeedItems
         * @constructor
         * @param {server.IFeedItems=} [properties] Properties to set
         */
        function FeedItems(properties) {
            this.items = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * FeedItems uid.
         * @member {number|Long} uid
         * @memberof server.FeedItems
         * @instance
         */
        FeedItems.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * FeedItems items.
         * @member {Array.<server.IFeedItem>} items
         * @memberof server.FeedItems
         * @instance
         */
        FeedItems.prototype.items = $util.emptyArray;

        /**
         * Creates a new FeedItems instance using the specified properties.
         * @function create
         * @memberof server.FeedItems
         * @static
         * @param {server.IFeedItems=} [properties] Properties to set
         * @returns {server.FeedItems} FeedItems instance
         */
        FeedItems.create = function create(properties) {
            return new FeedItems(properties);
        };

        /**
         * Encodes the specified FeedItems message. Does not implicitly {@link server.FeedItems.verify|verify} messages.
         * @function encode
         * @memberof server.FeedItems
         * @static
         * @param {server.IFeedItems} message FeedItems message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        FeedItems.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 1, wireType 0 =*/8).int64(message.uid);
            if (message.items != null && message.items.length)
                for (var i = 0; i < message.items.length; ++i)
                    $root.server.FeedItem.encode(message.items[i], writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified FeedItems message, length delimited. Does not implicitly {@link server.FeedItems.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.FeedItems
         * @static
         * @param {server.IFeedItems} message FeedItems message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        FeedItems.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a FeedItems message from the specified reader or buffer.
         * @function decode
         * @memberof server.FeedItems
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.FeedItems} FeedItems
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        FeedItems.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.FeedItems();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.uid = reader.int64();
                    break;
                case 2:
                    if (!(message.items && message.items.length))
                        message.items = [];
                    message.items.push($root.server.FeedItem.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a FeedItems message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.FeedItems
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.FeedItems} FeedItems
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        FeedItems.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a FeedItems message.
         * @function verify
         * @memberof server.FeedItems
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        FeedItems.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            if (message.items != null && message.hasOwnProperty("items")) {
                if (!Array.isArray(message.items))
                    return "items: array expected";
                for (var i = 0; i < message.items.length; ++i) {
                    var error = $root.server.FeedItem.verify(message.items[i]);
                    if (error)
                        return "items." + error;
                }
            }
            return null;
        };

        /**
         * Creates a FeedItems message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.FeedItems
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.FeedItems} FeedItems
         */
        FeedItems.fromObject = function fromObject(object) {
            if (object instanceof $root.server.FeedItems)
                return object;
            var message = new $root.server.FeedItems();
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = false;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber();
            if (object.items) {
                if (!Array.isArray(object.items))
                    throw TypeError(".server.FeedItems.items: array expected");
                message.items = [];
                for (var i = 0; i < object.items.length; ++i) {
                    if (typeof object.items[i] !== "object")
                        throw TypeError(".server.FeedItems.items: object expected");
                    message.items[i] = $root.server.FeedItem.fromObject(object.items[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a FeedItems message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.FeedItems
         * @static
         * @param {server.FeedItems} message FeedItems
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        FeedItems.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.items = [];
            if (options.defaults)
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber() : message.uid;
            if (message.items && message.items.length) {
                object.items = [];
                for (var j = 0; j < message.items.length; ++j)
                    object.items[j] = $root.server.FeedItem.toObject(message.items[j], options);
            }
            return object;
        };

        /**
         * Converts this FeedItems to JSON.
         * @function toJSON
         * @memberof server.FeedItems
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        FeedItems.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return FeedItems;
    })();

    server.GroupFeedItem = (function() {

        /**
         * Properties of a GroupFeedItem.
         * @memberof server
         * @interface IGroupFeedItem
         * @property {server.GroupFeedItem.Action|null} [action] GroupFeedItem action
         * @property {string|null} [gid] GroupFeedItem gid
         * @property {string|null} [name] GroupFeedItem name
         * @property {string|null} [avatarId] GroupFeedItem avatarId
         * @property {server.IPost|null} [post] GroupFeedItem post
         * @property {server.IComment|null} [comment] GroupFeedItem comment
         */

        /**
         * Constructs a new GroupFeedItem.
         * @memberof server
         * @classdesc Represents a GroupFeedItem.
         * @implements IGroupFeedItem
         * @constructor
         * @param {server.IGroupFeedItem=} [properties] Properties to set
         */
        function GroupFeedItem(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * GroupFeedItem action.
         * @member {server.GroupFeedItem.Action} action
         * @memberof server.GroupFeedItem
         * @instance
         */
        GroupFeedItem.prototype.action = 0;

        /**
         * GroupFeedItem gid.
         * @member {string} gid
         * @memberof server.GroupFeedItem
         * @instance
         */
        GroupFeedItem.prototype.gid = "";

        /**
         * GroupFeedItem name.
         * @member {string} name
         * @memberof server.GroupFeedItem
         * @instance
         */
        GroupFeedItem.prototype.name = "";

        /**
         * GroupFeedItem avatarId.
         * @member {string} avatarId
         * @memberof server.GroupFeedItem
         * @instance
         */
        GroupFeedItem.prototype.avatarId = "";

        /**
         * GroupFeedItem post.
         * @member {server.IPost|null|undefined} post
         * @memberof server.GroupFeedItem
         * @instance
         */
        GroupFeedItem.prototype.post = null;

        /**
         * GroupFeedItem comment.
         * @member {server.IComment|null|undefined} comment
         * @memberof server.GroupFeedItem
         * @instance
         */
        GroupFeedItem.prototype.comment = null;

        // OneOf field names bound to virtual getters and setters
        var $oneOfFields;

        /**
         * GroupFeedItem item.
         * @member {"post"|"comment"|undefined} item
         * @memberof server.GroupFeedItem
         * @instance
         */
        Object.defineProperty(GroupFeedItem.prototype, "item", {
            get: $util.oneOfGetter($oneOfFields = ["post", "comment"]),
            set: $util.oneOfSetter($oneOfFields)
        });

        /**
         * Creates a new GroupFeedItem instance using the specified properties.
         * @function create
         * @memberof server.GroupFeedItem
         * @static
         * @param {server.IGroupFeedItem=} [properties] Properties to set
         * @returns {server.GroupFeedItem} GroupFeedItem instance
         */
        GroupFeedItem.create = function create(properties) {
            return new GroupFeedItem(properties);
        };

        /**
         * Encodes the specified GroupFeedItem message. Does not implicitly {@link server.GroupFeedItem.verify|verify} messages.
         * @function encode
         * @memberof server.GroupFeedItem
         * @static
         * @param {server.IGroupFeedItem} message GroupFeedItem message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupFeedItem.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.action != null && Object.hasOwnProperty.call(message, "action"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.action);
            if (message.gid != null && Object.hasOwnProperty.call(message, "gid"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.gid);
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.name);
            if (message.avatarId != null && Object.hasOwnProperty.call(message, "avatarId"))
                writer.uint32(/* id 4, wireType 2 =*/34).string(message.avatarId);
            if (message.post != null && Object.hasOwnProperty.call(message, "post"))
                $root.server.Post.encode(message.post, writer.uint32(/* id 5, wireType 2 =*/42).fork()).ldelim();
            if (message.comment != null && Object.hasOwnProperty.call(message, "comment"))
                $root.server.Comment.encode(message.comment, writer.uint32(/* id 6, wireType 2 =*/50).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified GroupFeedItem message, length delimited. Does not implicitly {@link server.GroupFeedItem.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.GroupFeedItem
         * @static
         * @param {server.IGroupFeedItem} message GroupFeedItem message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupFeedItem.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a GroupFeedItem message from the specified reader or buffer.
         * @function decode
         * @memberof server.GroupFeedItem
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.GroupFeedItem} GroupFeedItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupFeedItem.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.GroupFeedItem();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.action = reader.int32();
                    break;
                case 2:
                    message.gid = reader.string();
                    break;
                case 3:
                    message.name = reader.string();
                    break;
                case 4:
                    message.avatarId = reader.string();
                    break;
                case 5:
                    message.post = $root.server.Post.decode(reader, reader.uint32());
                    break;
                case 6:
                    message.comment = $root.server.Comment.decode(reader, reader.uint32());
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a GroupFeedItem message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.GroupFeedItem
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.GroupFeedItem} GroupFeedItem
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupFeedItem.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a GroupFeedItem message.
         * @function verify
         * @memberof server.GroupFeedItem
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        GroupFeedItem.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            var properties = {};
            if (message.action != null && message.hasOwnProperty("action"))
                switch (message.action) {
                default:
                    return "action: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.gid != null && message.hasOwnProperty("gid"))
                if (!$util.isString(message.gid))
                    return "gid: string expected";
            if (message.name != null && message.hasOwnProperty("name"))
                if (!$util.isString(message.name))
                    return "name: string expected";
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                if (!$util.isString(message.avatarId))
                    return "avatarId: string expected";
            if (message.post != null && message.hasOwnProperty("post")) {
                properties.item = 1;
                {
                    var error = $root.server.Post.verify(message.post);
                    if (error)
                        return "post." + error;
                }
            }
            if (message.comment != null && message.hasOwnProperty("comment")) {
                if (properties.item === 1)
                    return "item: multiple values";
                properties.item = 1;
                {
                    var error = $root.server.Comment.verify(message.comment);
                    if (error)
                        return "comment." + error;
                }
            }
            return null;
        };

        /**
         * Creates a GroupFeedItem message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.GroupFeedItem
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.GroupFeedItem} GroupFeedItem
         */
        GroupFeedItem.fromObject = function fromObject(object) {
            if (object instanceof $root.server.GroupFeedItem)
                return object;
            var message = new $root.server.GroupFeedItem();
            switch (object.action) {
            case "PUBLISH":
            case 0:
                message.action = 0;
                break;
            case "RETRACT":
            case 1:
                message.action = 1;
                break;
            }
            if (object.gid != null)
                message.gid = String(object.gid);
            if (object.name != null)
                message.name = String(object.name);
            if (object.avatarId != null)
                message.avatarId = String(object.avatarId);
            if (object.post != null) {
                if (typeof object.post !== "object")
                    throw TypeError(".server.GroupFeedItem.post: object expected");
                message.post = $root.server.Post.fromObject(object.post);
            }
            if (object.comment != null) {
                if (typeof object.comment !== "object")
                    throw TypeError(".server.GroupFeedItem.comment: object expected");
                message.comment = $root.server.Comment.fromObject(object.comment);
            }
            return message;
        };

        /**
         * Creates a plain object from a GroupFeedItem message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.GroupFeedItem
         * @static
         * @param {server.GroupFeedItem} message GroupFeedItem
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        GroupFeedItem.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.action = options.enums === String ? "PUBLISH" : 0;
                object.gid = "";
                object.name = "";
                object.avatarId = "";
            }
            if (message.action != null && message.hasOwnProperty("action"))
                object.action = options.enums === String ? $root.server.GroupFeedItem.Action[message.action] : message.action;
            if (message.gid != null && message.hasOwnProperty("gid"))
                object.gid = message.gid;
            if (message.name != null && message.hasOwnProperty("name"))
                object.name = message.name;
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                object.avatarId = message.avatarId;
            if (message.post != null && message.hasOwnProperty("post")) {
                object.post = $root.server.Post.toObject(message.post, options);
                if (options.oneofs)
                    object.item = "post";
            }
            if (message.comment != null && message.hasOwnProperty("comment")) {
                object.comment = $root.server.Comment.toObject(message.comment, options);
                if (options.oneofs)
                    object.item = "comment";
            }
            return object;
        };

        /**
         * Converts this GroupFeedItem to JSON.
         * @function toJSON
         * @memberof server.GroupFeedItem
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        GroupFeedItem.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Action enum.
         * @name server.GroupFeedItem.Action
         * @enum {number}
         * @property {number} PUBLISH=0 PUBLISH value
         * @property {number} RETRACT=1 RETRACT value
         */
        GroupFeedItem.Action = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "PUBLISH"] = 0;
            values[valuesById[1] = "RETRACT"] = 1;
            return values;
        })();

        return GroupFeedItem;
    })();

    server.GroupFeedItems = (function() {

        /**
         * Properties of a GroupFeedItems.
         * @memberof server
         * @interface IGroupFeedItems
         * @property {string|null} [gid] GroupFeedItems gid
         * @property {string|null} [name] GroupFeedItems name
         * @property {string|null} [avatarId] GroupFeedItems avatarId
         * @property {Array.<server.IGroupFeedItem>|null} [items] GroupFeedItems items
         */

        /**
         * Constructs a new GroupFeedItems.
         * @memberof server
         * @classdesc Represents a GroupFeedItems.
         * @implements IGroupFeedItems
         * @constructor
         * @param {server.IGroupFeedItems=} [properties] Properties to set
         */
        function GroupFeedItems(properties) {
            this.items = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * GroupFeedItems gid.
         * @member {string} gid
         * @memberof server.GroupFeedItems
         * @instance
         */
        GroupFeedItems.prototype.gid = "";

        /**
         * GroupFeedItems name.
         * @member {string} name
         * @memberof server.GroupFeedItems
         * @instance
         */
        GroupFeedItems.prototype.name = "";

        /**
         * GroupFeedItems avatarId.
         * @member {string} avatarId
         * @memberof server.GroupFeedItems
         * @instance
         */
        GroupFeedItems.prototype.avatarId = "";

        /**
         * GroupFeedItems items.
         * @member {Array.<server.IGroupFeedItem>} items
         * @memberof server.GroupFeedItems
         * @instance
         */
        GroupFeedItems.prototype.items = $util.emptyArray;

        /**
         * Creates a new GroupFeedItems instance using the specified properties.
         * @function create
         * @memberof server.GroupFeedItems
         * @static
         * @param {server.IGroupFeedItems=} [properties] Properties to set
         * @returns {server.GroupFeedItems} GroupFeedItems instance
         */
        GroupFeedItems.create = function create(properties) {
            return new GroupFeedItems(properties);
        };

        /**
         * Encodes the specified GroupFeedItems message. Does not implicitly {@link server.GroupFeedItems.verify|verify} messages.
         * @function encode
         * @memberof server.GroupFeedItems
         * @static
         * @param {server.IGroupFeedItems} message GroupFeedItems message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupFeedItems.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.gid != null && Object.hasOwnProperty.call(message, "gid"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.gid);
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.name);
            if (message.avatarId != null && Object.hasOwnProperty.call(message, "avatarId"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.avatarId);
            if (message.items != null && message.items.length)
                for (var i = 0; i < message.items.length; ++i)
                    $root.server.GroupFeedItem.encode(message.items[i], writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified GroupFeedItems message, length delimited. Does not implicitly {@link server.GroupFeedItems.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.GroupFeedItems
         * @static
         * @param {server.IGroupFeedItems} message GroupFeedItems message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupFeedItems.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a GroupFeedItems message from the specified reader or buffer.
         * @function decode
         * @memberof server.GroupFeedItems
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.GroupFeedItems} GroupFeedItems
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupFeedItems.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.GroupFeedItems();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.gid = reader.string();
                    break;
                case 2:
                    message.name = reader.string();
                    break;
                case 3:
                    message.avatarId = reader.string();
                    break;
                case 4:
                    if (!(message.items && message.items.length))
                        message.items = [];
                    message.items.push($root.server.GroupFeedItem.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a GroupFeedItems message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.GroupFeedItems
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.GroupFeedItems} GroupFeedItems
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupFeedItems.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a GroupFeedItems message.
         * @function verify
         * @memberof server.GroupFeedItems
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        GroupFeedItems.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.gid != null && message.hasOwnProperty("gid"))
                if (!$util.isString(message.gid))
                    return "gid: string expected";
            if (message.name != null && message.hasOwnProperty("name"))
                if (!$util.isString(message.name))
                    return "name: string expected";
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                if (!$util.isString(message.avatarId))
                    return "avatarId: string expected";
            if (message.items != null && message.hasOwnProperty("items")) {
                if (!Array.isArray(message.items))
                    return "items: array expected";
                for (var i = 0; i < message.items.length; ++i) {
                    var error = $root.server.GroupFeedItem.verify(message.items[i]);
                    if (error)
                        return "items." + error;
                }
            }
            return null;
        };

        /**
         * Creates a GroupFeedItems message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.GroupFeedItems
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.GroupFeedItems} GroupFeedItems
         */
        GroupFeedItems.fromObject = function fromObject(object) {
            if (object instanceof $root.server.GroupFeedItems)
                return object;
            var message = new $root.server.GroupFeedItems();
            if (object.gid != null)
                message.gid = String(object.gid);
            if (object.name != null)
                message.name = String(object.name);
            if (object.avatarId != null)
                message.avatarId = String(object.avatarId);
            if (object.items) {
                if (!Array.isArray(object.items))
                    throw TypeError(".server.GroupFeedItems.items: array expected");
                message.items = [];
                for (var i = 0; i < object.items.length; ++i) {
                    if (typeof object.items[i] !== "object")
                        throw TypeError(".server.GroupFeedItems.items: object expected");
                    message.items[i] = $root.server.GroupFeedItem.fromObject(object.items[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a GroupFeedItems message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.GroupFeedItems
         * @static
         * @param {server.GroupFeedItems} message GroupFeedItems
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        GroupFeedItems.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.items = [];
            if (options.defaults) {
                object.gid = "";
                object.name = "";
                object.avatarId = "";
            }
            if (message.gid != null && message.hasOwnProperty("gid"))
                object.gid = message.gid;
            if (message.name != null && message.hasOwnProperty("name"))
                object.name = message.name;
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                object.avatarId = message.avatarId;
            if (message.items && message.items.length) {
                object.items = [];
                for (var j = 0; j < message.items.length; ++j)
                    object.items[j] = $root.server.GroupFeedItem.toObject(message.items[j], options);
            }
            return object;
        };

        /**
         * Converts this GroupFeedItems to JSON.
         * @function toJSON
         * @memberof server.GroupFeedItems
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        GroupFeedItems.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return GroupFeedItems;
    })();

    server.GroupMember = (function() {

        /**
         * Properties of a GroupMember.
         * @memberof server
         * @interface IGroupMember
         * @property {server.GroupMember.Action|null} [action] GroupMember action
         * @property {number|Long|null} [uid] GroupMember uid
         * @property {server.GroupMember.Type|null} [type] GroupMember type
         * @property {string|null} [name] GroupMember name
         * @property {string|null} [avatarId] GroupMember avatarId
         * @property {string|null} [result] GroupMember result
         * @property {string|null} [reason] GroupMember reason
         */

        /**
         * Constructs a new GroupMember.
         * @memberof server
         * @classdesc Represents a GroupMember.
         * @implements IGroupMember
         * @constructor
         * @param {server.IGroupMember=} [properties] Properties to set
         */
        function GroupMember(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * GroupMember action.
         * @member {server.GroupMember.Action} action
         * @memberof server.GroupMember
         * @instance
         */
        GroupMember.prototype.action = 0;

        /**
         * GroupMember uid.
         * @member {number|Long} uid
         * @memberof server.GroupMember
         * @instance
         */
        GroupMember.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * GroupMember type.
         * @member {server.GroupMember.Type} type
         * @memberof server.GroupMember
         * @instance
         */
        GroupMember.prototype.type = 0;

        /**
         * GroupMember name.
         * @member {string} name
         * @memberof server.GroupMember
         * @instance
         */
        GroupMember.prototype.name = "";

        /**
         * GroupMember avatarId.
         * @member {string} avatarId
         * @memberof server.GroupMember
         * @instance
         */
        GroupMember.prototype.avatarId = "";

        /**
         * GroupMember result.
         * @member {string} result
         * @memberof server.GroupMember
         * @instance
         */
        GroupMember.prototype.result = "";

        /**
         * GroupMember reason.
         * @member {string} reason
         * @memberof server.GroupMember
         * @instance
         */
        GroupMember.prototype.reason = "";

        /**
         * Creates a new GroupMember instance using the specified properties.
         * @function create
         * @memberof server.GroupMember
         * @static
         * @param {server.IGroupMember=} [properties] Properties to set
         * @returns {server.GroupMember} GroupMember instance
         */
        GroupMember.create = function create(properties) {
            return new GroupMember(properties);
        };

        /**
         * Encodes the specified GroupMember message. Does not implicitly {@link server.GroupMember.verify|verify} messages.
         * @function encode
         * @memberof server.GroupMember
         * @static
         * @param {server.IGroupMember} message GroupMember message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupMember.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.action != null && Object.hasOwnProperty.call(message, "action"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.action);
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 2, wireType 0 =*/16).int64(message.uid);
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 3, wireType 0 =*/24).int32(message.type);
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                writer.uint32(/* id 4, wireType 2 =*/34).string(message.name);
            if (message.avatarId != null && Object.hasOwnProperty.call(message, "avatarId"))
                writer.uint32(/* id 5, wireType 2 =*/42).string(message.avatarId);
            if (message.result != null && Object.hasOwnProperty.call(message, "result"))
                writer.uint32(/* id 6, wireType 2 =*/50).string(message.result);
            if (message.reason != null && Object.hasOwnProperty.call(message, "reason"))
                writer.uint32(/* id 7, wireType 2 =*/58).string(message.reason);
            return writer;
        };

        /**
         * Encodes the specified GroupMember message, length delimited. Does not implicitly {@link server.GroupMember.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.GroupMember
         * @static
         * @param {server.IGroupMember} message GroupMember message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupMember.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a GroupMember message from the specified reader or buffer.
         * @function decode
         * @memberof server.GroupMember
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.GroupMember} GroupMember
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupMember.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.GroupMember();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.action = reader.int32();
                    break;
                case 2:
                    message.uid = reader.int64();
                    break;
                case 3:
                    message.type = reader.int32();
                    break;
                case 4:
                    message.name = reader.string();
                    break;
                case 5:
                    message.avatarId = reader.string();
                    break;
                case 6:
                    message.result = reader.string();
                    break;
                case 7:
                    message.reason = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a GroupMember message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.GroupMember
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.GroupMember} GroupMember
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupMember.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a GroupMember message.
         * @function verify
         * @memberof server.GroupMember
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        GroupMember.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.action != null && message.hasOwnProperty("action"))
                switch (message.action) {
                default:
                    return "action: enum value expected";
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    break;
                }
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            if (message.type != null && message.hasOwnProperty("type"))
                switch (message.type) {
                default:
                    return "type: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.name != null && message.hasOwnProperty("name"))
                if (!$util.isString(message.name))
                    return "name: string expected";
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                if (!$util.isString(message.avatarId))
                    return "avatarId: string expected";
            if (message.result != null && message.hasOwnProperty("result"))
                if (!$util.isString(message.result))
                    return "result: string expected";
            if (message.reason != null && message.hasOwnProperty("reason"))
                if (!$util.isString(message.reason))
                    return "reason: string expected";
            return null;
        };

        /**
         * Creates a GroupMember message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.GroupMember
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.GroupMember} GroupMember
         */
        GroupMember.fromObject = function fromObject(object) {
            if (object instanceof $root.server.GroupMember)
                return object;
            var message = new $root.server.GroupMember();
            switch (object.action) {
            case "ADD":
            case 0:
                message.action = 0;
                break;
            case "REMOVE":
            case 1:
                message.action = 1;
                break;
            case "PROMOTE":
            case 2:
                message.action = 2;
                break;
            case "DEMOTE":
            case 3:
                message.action = 3;
                break;
            case "LEAVE":
            case 4:
                message.action = 4;
                break;
            }
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = false;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber();
            switch (object.type) {
            case "MEMBER":
            case 0:
                message.type = 0;
                break;
            case "ADMIN":
            case 1:
                message.type = 1;
                break;
            }
            if (object.name != null)
                message.name = String(object.name);
            if (object.avatarId != null)
                message.avatarId = String(object.avatarId);
            if (object.result != null)
                message.result = String(object.result);
            if (object.reason != null)
                message.reason = String(object.reason);
            return message;
        };

        /**
         * Creates a plain object from a GroupMember message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.GroupMember
         * @static
         * @param {server.GroupMember} message GroupMember
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        GroupMember.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.action = options.enums === String ? "ADD" : 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
                object.type = options.enums === String ? "MEMBER" : 0;
                object.name = "";
                object.avatarId = "";
                object.result = "";
                object.reason = "";
            }
            if (message.action != null && message.hasOwnProperty("action"))
                object.action = options.enums === String ? $root.server.GroupMember.Action[message.action] : message.action;
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber() : message.uid;
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.server.GroupMember.Type[message.type] : message.type;
            if (message.name != null && message.hasOwnProperty("name"))
                object.name = message.name;
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                object.avatarId = message.avatarId;
            if (message.result != null && message.hasOwnProperty("result"))
                object.result = message.result;
            if (message.reason != null && message.hasOwnProperty("reason"))
                object.reason = message.reason;
            return object;
        };

        /**
         * Converts this GroupMember to JSON.
         * @function toJSON
         * @memberof server.GroupMember
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        GroupMember.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Action enum.
         * @name server.GroupMember.Action
         * @enum {number}
         * @property {number} ADD=0 ADD value
         * @property {number} REMOVE=1 REMOVE value
         * @property {number} PROMOTE=2 PROMOTE value
         * @property {number} DEMOTE=3 DEMOTE value
         * @property {number} LEAVE=4 LEAVE value
         */
        GroupMember.Action = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "ADD"] = 0;
            values[valuesById[1] = "REMOVE"] = 1;
            values[valuesById[2] = "PROMOTE"] = 2;
            values[valuesById[3] = "DEMOTE"] = 3;
            values[valuesById[4] = "LEAVE"] = 4;
            return values;
        })();

        /**
         * Type enum.
         * @name server.GroupMember.Type
         * @enum {number}
         * @property {number} MEMBER=0 MEMBER value
         * @property {number} ADMIN=1 ADMIN value
         */
        GroupMember.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "MEMBER"] = 0;
            values[valuesById[1] = "ADMIN"] = 1;
            return values;
        })();

        return GroupMember;
    })();

    server.GroupStanza = (function() {

        /**
         * Properties of a GroupStanza.
         * @memberof server
         * @interface IGroupStanza
         * @property {server.GroupStanza.Action|null} [action] GroupStanza action
         * @property {string|null} [gid] GroupStanza gid
         * @property {string|null} [name] GroupStanza name
         * @property {string|null} [avatarId] GroupStanza avatarId
         * @property {number|Long|null} [senderUid] GroupStanza senderUid
         * @property {string|null} [senderName] GroupStanza senderName
         * @property {Array.<server.IGroupMember>|null} [members] GroupStanza members
         */

        /**
         * Constructs a new GroupStanza.
         * @memberof server
         * @classdesc Represents a GroupStanza.
         * @implements IGroupStanza
         * @constructor
         * @param {server.IGroupStanza=} [properties] Properties to set
         */
        function GroupStanza(properties) {
            this.members = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * GroupStanza action.
         * @member {server.GroupStanza.Action} action
         * @memberof server.GroupStanza
         * @instance
         */
        GroupStanza.prototype.action = 0;

        /**
         * GroupStanza gid.
         * @member {string} gid
         * @memberof server.GroupStanza
         * @instance
         */
        GroupStanza.prototype.gid = "";

        /**
         * GroupStanza name.
         * @member {string} name
         * @memberof server.GroupStanza
         * @instance
         */
        GroupStanza.prototype.name = "";

        /**
         * GroupStanza avatarId.
         * @member {string} avatarId
         * @memberof server.GroupStanza
         * @instance
         */
        GroupStanza.prototype.avatarId = "";

        /**
         * GroupStanza senderUid.
         * @member {number|Long} senderUid
         * @memberof server.GroupStanza
         * @instance
         */
        GroupStanza.prototype.senderUid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * GroupStanza senderName.
         * @member {string} senderName
         * @memberof server.GroupStanza
         * @instance
         */
        GroupStanza.prototype.senderName = "";

        /**
         * GroupStanza members.
         * @member {Array.<server.IGroupMember>} members
         * @memberof server.GroupStanza
         * @instance
         */
        GroupStanza.prototype.members = $util.emptyArray;

        /**
         * Creates a new GroupStanza instance using the specified properties.
         * @function create
         * @memberof server.GroupStanza
         * @static
         * @param {server.IGroupStanza=} [properties] Properties to set
         * @returns {server.GroupStanza} GroupStanza instance
         */
        GroupStanza.create = function create(properties) {
            return new GroupStanza(properties);
        };

        /**
         * Encodes the specified GroupStanza message. Does not implicitly {@link server.GroupStanza.verify|verify} messages.
         * @function encode
         * @memberof server.GroupStanza
         * @static
         * @param {server.IGroupStanza} message GroupStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupStanza.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.action != null && Object.hasOwnProperty.call(message, "action"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.action);
            if (message.gid != null && Object.hasOwnProperty.call(message, "gid"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.gid);
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.name);
            if (message.avatarId != null && Object.hasOwnProperty.call(message, "avatarId"))
                writer.uint32(/* id 4, wireType 2 =*/34).string(message.avatarId);
            if (message.senderUid != null && Object.hasOwnProperty.call(message, "senderUid"))
                writer.uint32(/* id 5, wireType 0 =*/40).int64(message.senderUid);
            if (message.senderName != null && Object.hasOwnProperty.call(message, "senderName"))
                writer.uint32(/* id 6, wireType 2 =*/50).string(message.senderName);
            if (message.members != null && message.members.length)
                for (var i = 0; i < message.members.length; ++i)
                    $root.server.GroupMember.encode(message.members[i], writer.uint32(/* id 7, wireType 2 =*/58).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified GroupStanza message, length delimited. Does not implicitly {@link server.GroupStanza.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.GroupStanza
         * @static
         * @param {server.IGroupStanza} message GroupStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupStanza.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a GroupStanza message from the specified reader or buffer.
         * @function decode
         * @memberof server.GroupStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.GroupStanza} GroupStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupStanza.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.GroupStanza();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.action = reader.int32();
                    break;
                case 2:
                    message.gid = reader.string();
                    break;
                case 3:
                    message.name = reader.string();
                    break;
                case 4:
                    message.avatarId = reader.string();
                    break;
                case 5:
                    message.senderUid = reader.int64();
                    break;
                case 6:
                    message.senderName = reader.string();
                    break;
                case 7:
                    if (!(message.members && message.members.length))
                        message.members = [];
                    message.members.push($root.server.GroupMember.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a GroupStanza message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.GroupStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.GroupStanza} GroupStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupStanza.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a GroupStanza message.
         * @function verify
         * @memberof server.GroupStanza
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        GroupStanza.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.action != null && message.hasOwnProperty("action"))
                switch (message.action) {
                default:
                    return "action: enum value expected";
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    break;
                }
            if (message.gid != null && message.hasOwnProperty("gid"))
                if (!$util.isString(message.gid))
                    return "gid: string expected";
            if (message.name != null && message.hasOwnProperty("name"))
                if (!$util.isString(message.name))
                    return "name: string expected";
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                if (!$util.isString(message.avatarId))
                    return "avatarId: string expected";
            if (message.senderUid != null && message.hasOwnProperty("senderUid"))
                if (!$util.isInteger(message.senderUid) && !(message.senderUid && $util.isInteger(message.senderUid.low) && $util.isInteger(message.senderUid.high)))
                    return "senderUid: integer|Long expected";
            if (message.senderName != null && message.hasOwnProperty("senderName"))
                if (!$util.isString(message.senderName))
                    return "senderName: string expected";
            if (message.members != null && message.hasOwnProperty("members")) {
                if (!Array.isArray(message.members))
                    return "members: array expected";
                for (var i = 0; i < message.members.length; ++i) {
                    var error = $root.server.GroupMember.verify(message.members[i]);
                    if (error)
                        return "members." + error;
                }
            }
            return null;
        };

        /**
         * Creates a GroupStanza message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.GroupStanza
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.GroupStanza} GroupStanza
         */
        GroupStanza.fromObject = function fromObject(object) {
            if (object instanceof $root.server.GroupStanza)
                return object;
            var message = new $root.server.GroupStanza();
            switch (object.action) {
            case "SET":
            case 0:
                message.action = 0;
                break;
            case "GET":
            case 1:
                message.action = 1;
                break;
            case "CREATE":
            case 2:
                message.action = 2;
                break;
            case "DELETE":
            case 3:
                message.action = 3;
                break;
            case "LEAVE":
            case 4:
                message.action = 4;
                break;
            case "CHANGE_AVATAR":
            case 5:
                message.action = 5;
                break;
            case "CHANGE_NAME":
            case 6:
                message.action = 6;
                break;
            case "MODIFY_ADMINS":
            case 7:
                message.action = 7;
                break;
            case "MODIFY_MEMBERS":
            case 8:
                message.action = 8;
                break;
            case "AUTO_PROMOTE_ADMINS":
            case 9:
                message.action = 9;
                break;
            case "SET_NAME":
            case 10:
                message.action = 10;
                break;
            }
            if (object.gid != null)
                message.gid = String(object.gid);
            if (object.name != null)
                message.name = String(object.name);
            if (object.avatarId != null)
                message.avatarId = String(object.avatarId);
            if (object.senderUid != null)
                if ($util.Long)
                    (message.senderUid = $util.Long.fromValue(object.senderUid)).unsigned = false;
                else if (typeof object.senderUid === "string")
                    message.senderUid = parseInt(object.senderUid, 10);
                else if (typeof object.senderUid === "number")
                    message.senderUid = object.senderUid;
                else if (typeof object.senderUid === "object")
                    message.senderUid = new $util.LongBits(object.senderUid.low >>> 0, object.senderUid.high >>> 0).toNumber();
            if (object.senderName != null)
                message.senderName = String(object.senderName);
            if (object.members) {
                if (!Array.isArray(object.members))
                    throw TypeError(".server.GroupStanza.members: array expected");
                message.members = [];
                for (var i = 0; i < object.members.length; ++i) {
                    if (typeof object.members[i] !== "object")
                        throw TypeError(".server.GroupStanza.members: object expected");
                    message.members[i] = $root.server.GroupMember.fromObject(object.members[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a GroupStanza message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.GroupStanza
         * @static
         * @param {server.GroupStanza} message GroupStanza
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        GroupStanza.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.members = [];
            if (options.defaults) {
                object.action = options.enums === String ? "SET" : 0;
                object.gid = "";
                object.name = "";
                object.avatarId = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.senderUid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.senderUid = options.longs === String ? "0" : 0;
                object.senderName = "";
            }
            if (message.action != null && message.hasOwnProperty("action"))
                object.action = options.enums === String ? $root.server.GroupStanza.Action[message.action] : message.action;
            if (message.gid != null && message.hasOwnProperty("gid"))
                object.gid = message.gid;
            if (message.name != null && message.hasOwnProperty("name"))
                object.name = message.name;
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                object.avatarId = message.avatarId;
            if (message.senderUid != null && message.hasOwnProperty("senderUid"))
                if (typeof message.senderUid === "number")
                    object.senderUid = options.longs === String ? String(message.senderUid) : message.senderUid;
                else
                    object.senderUid = options.longs === String ? $util.Long.prototype.toString.call(message.senderUid) : options.longs === Number ? new $util.LongBits(message.senderUid.low >>> 0, message.senderUid.high >>> 0).toNumber() : message.senderUid;
            if (message.senderName != null && message.hasOwnProperty("senderName"))
                object.senderName = message.senderName;
            if (message.members && message.members.length) {
                object.members = [];
                for (var j = 0; j < message.members.length; ++j)
                    object.members[j] = $root.server.GroupMember.toObject(message.members[j], options);
            }
            return object;
        };

        /**
         * Converts this GroupStanza to JSON.
         * @function toJSON
         * @memberof server.GroupStanza
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        GroupStanza.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Action enum.
         * @name server.GroupStanza.Action
         * @enum {number}
         * @property {number} SET=0 SET value
         * @property {number} GET=1 GET value
         * @property {number} CREATE=2 CREATE value
         * @property {number} DELETE=3 DELETE value
         * @property {number} LEAVE=4 LEAVE value
         * @property {number} CHANGE_AVATAR=5 CHANGE_AVATAR value
         * @property {number} CHANGE_NAME=6 CHANGE_NAME value
         * @property {number} MODIFY_ADMINS=7 MODIFY_ADMINS value
         * @property {number} MODIFY_MEMBERS=8 MODIFY_MEMBERS value
         * @property {number} AUTO_PROMOTE_ADMINS=9 AUTO_PROMOTE_ADMINS value
         * @property {number} SET_NAME=10 SET_NAME value
         */
        GroupStanza.Action = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "SET"] = 0;
            values[valuesById[1] = "GET"] = 1;
            values[valuesById[2] = "CREATE"] = 2;
            values[valuesById[3] = "DELETE"] = 3;
            values[valuesById[4] = "LEAVE"] = 4;
            values[valuesById[5] = "CHANGE_AVATAR"] = 5;
            values[valuesById[6] = "CHANGE_NAME"] = 6;
            values[valuesById[7] = "MODIFY_ADMINS"] = 7;
            values[valuesById[8] = "MODIFY_MEMBERS"] = 8;
            values[valuesById[9] = "AUTO_PROMOTE_ADMINS"] = 9;
            values[valuesById[10] = "SET_NAME"] = 10;
            return values;
        })();

        return GroupStanza;
    })();

    server.GroupChat = (function() {

        /**
         * Properties of a GroupChat.
         * @memberof server
         * @interface IGroupChat
         * @property {string|null} [gid] GroupChat gid
         * @property {string|null} [name] GroupChat name
         * @property {string|null} [avatarId] GroupChat avatarId
         * @property {number|Long|null} [senderUid] GroupChat senderUid
         * @property {string|null} [senderName] GroupChat senderName
         * @property {number|Long|null} [timestamp] GroupChat timestamp
         * @property {Uint8Array|null} [payload] GroupChat payload
         */

        /**
         * Constructs a new GroupChat.
         * @memberof server
         * @classdesc Represents a GroupChat.
         * @implements IGroupChat
         * @constructor
         * @param {server.IGroupChat=} [properties] Properties to set
         */
        function GroupChat(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * GroupChat gid.
         * @member {string} gid
         * @memberof server.GroupChat
         * @instance
         */
        GroupChat.prototype.gid = "";

        /**
         * GroupChat name.
         * @member {string} name
         * @memberof server.GroupChat
         * @instance
         */
        GroupChat.prototype.name = "";

        /**
         * GroupChat avatarId.
         * @member {string} avatarId
         * @memberof server.GroupChat
         * @instance
         */
        GroupChat.prototype.avatarId = "";

        /**
         * GroupChat senderUid.
         * @member {number|Long} senderUid
         * @memberof server.GroupChat
         * @instance
         */
        GroupChat.prototype.senderUid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * GroupChat senderName.
         * @member {string} senderName
         * @memberof server.GroupChat
         * @instance
         */
        GroupChat.prototype.senderName = "";

        /**
         * GroupChat timestamp.
         * @member {number|Long} timestamp
         * @memberof server.GroupChat
         * @instance
         */
        GroupChat.prototype.timestamp = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * GroupChat payload.
         * @member {Uint8Array} payload
         * @memberof server.GroupChat
         * @instance
         */
        GroupChat.prototype.payload = $util.newBuffer([]);

        /**
         * Creates a new GroupChat instance using the specified properties.
         * @function create
         * @memberof server.GroupChat
         * @static
         * @param {server.IGroupChat=} [properties] Properties to set
         * @returns {server.GroupChat} GroupChat instance
         */
        GroupChat.create = function create(properties) {
            return new GroupChat(properties);
        };

        /**
         * Encodes the specified GroupChat message. Does not implicitly {@link server.GroupChat.verify|verify} messages.
         * @function encode
         * @memberof server.GroupChat
         * @static
         * @param {server.IGroupChat} message GroupChat message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupChat.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.gid != null && Object.hasOwnProperty.call(message, "gid"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.gid);
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.name);
            if (message.avatarId != null && Object.hasOwnProperty.call(message, "avatarId"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.avatarId);
            if (message.senderUid != null && Object.hasOwnProperty.call(message, "senderUid"))
                writer.uint32(/* id 4, wireType 0 =*/32).int64(message.senderUid);
            if (message.senderName != null && Object.hasOwnProperty.call(message, "senderName"))
                writer.uint32(/* id 5, wireType 2 =*/42).string(message.senderName);
            if (message.timestamp != null && Object.hasOwnProperty.call(message, "timestamp"))
                writer.uint32(/* id 6, wireType 0 =*/48).int64(message.timestamp);
            if (message.payload != null && Object.hasOwnProperty.call(message, "payload"))
                writer.uint32(/* id 7, wireType 2 =*/58).bytes(message.payload);
            return writer;
        };

        /**
         * Encodes the specified GroupChat message, length delimited. Does not implicitly {@link server.GroupChat.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.GroupChat
         * @static
         * @param {server.IGroupChat} message GroupChat message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupChat.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a GroupChat message from the specified reader or buffer.
         * @function decode
         * @memberof server.GroupChat
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.GroupChat} GroupChat
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupChat.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.GroupChat();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.gid = reader.string();
                    break;
                case 2:
                    message.name = reader.string();
                    break;
                case 3:
                    message.avatarId = reader.string();
                    break;
                case 4:
                    message.senderUid = reader.int64();
                    break;
                case 5:
                    message.senderName = reader.string();
                    break;
                case 6:
                    message.timestamp = reader.int64();
                    break;
                case 7:
                    message.payload = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a GroupChat message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.GroupChat
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.GroupChat} GroupChat
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupChat.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a GroupChat message.
         * @function verify
         * @memberof server.GroupChat
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        GroupChat.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.gid != null && message.hasOwnProperty("gid"))
                if (!$util.isString(message.gid))
                    return "gid: string expected";
            if (message.name != null && message.hasOwnProperty("name"))
                if (!$util.isString(message.name))
                    return "name: string expected";
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                if (!$util.isString(message.avatarId))
                    return "avatarId: string expected";
            if (message.senderUid != null && message.hasOwnProperty("senderUid"))
                if (!$util.isInteger(message.senderUid) && !(message.senderUid && $util.isInteger(message.senderUid.low) && $util.isInteger(message.senderUid.high)))
                    return "senderUid: integer|Long expected";
            if (message.senderName != null && message.hasOwnProperty("senderName"))
                if (!$util.isString(message.senderName))
                    return "senderName: string expected";
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (!$util.isInteger(message.timestamp) && !(message.timestamp && $util.isInteger(message.timestamp.low) && $util.isInteger(message.timestamp.high)))
                    return "timestamp: integer|Long expected";
            if (message.payload != null && message.hasOwnProperty("payload"))
                if (!(message.payload && typeof message.payload.length === "number" || $util.isString(message.payload)))
                    return "payload: buffer expected";
            return null;
        };

        /**
         * Creates a GroupChat message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.GroupChat
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.GroupChat} GroupChat
         */
        GroupChat.fromObject = function fromObject(object) {
            if (object instanceof $root.server.GroupChat)
                return object;
            var message = new $root.server.GroupChat();
            if (object.gid != null)
                message.gid = String(object.gid);
            if (object.name != null)
                message.name = String(object.name);
            if (object.avatarId != null)
                message.avatarId = String(object.avatarId);
            if (object.senderUid != null)
                if ($util.Long)
                    (message.senderUid = $util.Long.fromValue(object.senderUid)).unsigned = false;
                else if (typeof object.senderUid === "string")
                    message.senderUid = parseInt(object.senderUid, 10);
                else if (typeof object.senderUid === "number")
                    message.senderUid = object.senderUid;
                else if (typeof object.senderUid === "object")
                    message.senderUid = new $util.LongBits(object.senderUid.low >>> 0, object.senderUid.high >>> 0).toNumber();
            if (object.senderName != null)
                message.senderName = String(object.senderName);
            if (object.timestamp != null)
                if ($util.Long)
                    (message.timestamp = $util.Long.fromValue(object.timestamp)).unsigned = false;
                else if (typeof object.timestamp === "string")
                    message.timestamp = parseInt(object.timestamp, 10);
                else if (typeof object.timestamp === "number")
                    message.timestamp = object.timestamp;
                else if (typeof object.timestamp === "object")
                    message.timestamp = new $util.LongBits(object.timestamp.low >>> 0, object.timestamp.high >>> 0).toNumber();
            if (object.payload != null)
                if (typeof object.payload === "string")
                    $util.base64.decode(object.payload, message.payload = $util.newBuffer($util.base64.length(object.payload)), 0);
                else if (object.payload.length)
                    message.payload = object.payload;
            return message;
        };

        /**
         * Creates a plain object from a GroupChat message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.GroupChat
         * @static
         * @param {server.GroupChat} message GroupChat
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        GroupChat.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.gid = "";
                object.name = "";
                object.avatarId = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.senderUid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.senderUid = options.longs === String ? "0" : 0;
                object.senderName = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.timestamp = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.timestamp = options.longs === String ? "0" : 0;
                if (options.bytes === String)
                    object.payload = "";
                else {
                    object.payload = [];
                    if (options.bytes !== Array)
                        object.payload = $util.newBuffer(object.payload);
                }
            }
            if (message.gid != null && message.hasOwnProperty("gid"))
                object.gid = message.gid;
            if (message.name != null && message.hasOwnProperty("name"))
                object.name = message.name;
            if (message.avatarId != null && message.hasOwnProperty("avatarId"))
                object.avatarId = message.avatarId;
            if (message.senderUid != null && message.hasOwnProperty("senderUid"))
                if (typeof message.senderUid === "number")
                    object.senderUid = options.longs === String ? String(message.senderUid) : message.senderUid;
                else
                    object.senderUid = options.longs === String ? $util.Long.prototype.toString.call(message.senderUid) : options.longs === Number ? new $util.LongBits(message.senderUid.low >>> 0, message.senderUid.high >>> 0).toNumber() : message.senderUid;
            if (message.senderName != null && message.hasOwnProperty("senderName"))
                object.senderName = message.senderName;
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (typeof message.timestamp === "number")
                    object.timestamp = options.longs === String ? String(message.timestamp) : message.timestamp;
                else
                    object.timestamp = options.longs === String ? $util.Long.prototype.toString.call(message.timestamp) : options.longs === Number ? new $util.LongBits(message.timestamp.low >>> 0, message.timestamp.high >>> 0).toNumber() : message.timestamp;
            if (message.payload != null && message.hasOwnProperty("payload"))
                object.payload = options.bytes === String ? $util.base64.encode(message.payload, 0, message.payload.length) : options.bytes === Array ? Array.prototype.slice.call(message.payload) : message.payload;
            return object;
        };

        /**
         * Converts this GroupChat to JSON.
         * @function toJSON
         * @memberof server.GroupChat
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        GroupChat.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return GroupChat;
    })();

    server.GroupsStanza = (function() {

        /**
         * Properties of a GroupsStanza.
         * @memberof server
         * @interface IGroupsStanza
         * @property {server.GroupsStanza.Action|null} [action] GroupsStanza action
         * @property {Array.<server.IGroupStanza>|null} [groupStanzas] GroupsStanza groupStanzas
         */

        /**
         * Constructs a new GroupsStanza.
         * @memberof server
         * @classdesc Represents a GroupsStanza.
         * @implements IGroupsStanza
         * @constructor
         * @param {server.IGroupsStanza=} [properties] Properties to set
         */
        function GroupsStanza(properties) {
            this.groupStanzas = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * GroupsStanza action.
         * @member {server.GroupsStanza.Action} action
         * @memberof server.GroupsStanza
         * @instance
         */
        GroupsStanza.prototype.action = 0;

        /**
         * GroupsStanza groupStanzas.
         * @member {Array.<server.IGroupStanza>} groupStanzas
         * @memberof server.GroupsStanza
         * @instance
         */
        GroupsStanza.prototype.groupStanzas = $util.emptyArray;

        /**
         * Creates a new GroupsStanza instance using the specified properties.
         * @function create
         * @memberof server.GroupsStanza
         * @static
         * @param {server.IGroupsStanza=} [properties] Properties to set
         * @returns {server.GroupsStanza} GroupsStanza instance
         */
        GroupsStanza.create = function create(properties) {
            return new GroupsStanza(properties);
        };

        /**
         * Encodes the specified GroupsStanza message. Does not implicitly {@link server.GroupsStanza.verify|verify} messages.
         * @function encode
         * @memberof server.GroupsStanza
         * @static
         * @param {server.IGroupsStanza} message GroupsStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupsStanza.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.action != null && Object.hasOwnProperty.call(message, "action"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.action);
            if (message.groupStanzas != null && message.groupStanzas.length)
                for (var i = 0; i < message.groupStanzas.length; ++i)
                    $root.server.GroupStanza.encode(message.groupStanzas[i], writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified GroupsStanza message, length delimited. Does not implicitly {@link server.GroupsStanza.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.GroupsStanza
         * @static
         * @param {server.IGroupsStanza} message GroupsStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupsStanza.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a GroupsStanza message from the specified reader or buffer.
         * @function decode
         * @memberof server.GroupsStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.GroupsStanza} GroupsStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupsStanza.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.GroupsStanza();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.action = reader.int32();
                    break;
                case 2:
                    if (!(message.groupStanzas && message.groupStanzas.length))
                        message.groupStanzas = [];
                    message.groupStanzas.push($root.server.GroupStanza.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a GroupsStanza message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.GroupsStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.GroupsStanza} GroupsStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupsStanza.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a GroupsStanza message.
         * @function verify
         * @memberof server.GroupsStanza
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        GroupsStanza.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.action != null && message.hasOwnProperty("action"))
                switch (message.action) {
                default:
                    return "action: enum value expected";
                case 0:
                    break;
                }
            if (message.groupStanzas != null && message.hasOwnProperty("groupStanzas")) {
                if (!Array.isArray(message.groupStanzas))
                    return "groupStanzas: array expected";
                for (var i = 0; i < message.groupStanzas.length; ++i) {
                    var error = $root.server.GroupStanza.verify(message.groupStanzas[i]);
                    if (error)
                        return "groupStanzas." + error;
                }
            }
            return null;
        };

        /**
         * Creates a GroupsStanza message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.GroupsStanza
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.GroupsStanza} GroupsStanza
         */
        GroupsStanza.fromObject = function fromObject(object) {
            if (object instanceof $root.server.GroupsStanza)
                return object;
            var message = new $root.server.GroupsStanza();
            switch (object.action) {
            case "GET":
            case 0:
                message.action = 0;
                break;
            }
            if (object.groupStanzas) {
                if (!Array.isArray(object.groupStanzas))
                    throw TypeError(".server.GroupsStanza.groupStanzas: array expected");
                message.groupStanzas = [];
                for (var i = 0; i < object.groupStanzas.length; ++i) {
                    if (typeof object.groupStanzas[i] !== "object")
                        throw TypeError(".server.GroupsStanza.groupStanzas: object expected");
                    message.groupStanzas[i] = $root.server.GroupStanza.fromObject(object.groupStanzas[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a GroupsStanza message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.GroupsStanza
         * @static
         * @param {server.GroupsStanza} message GroupsStanza
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        GroupsStanza.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.groupStanzas = [];
            if (options.defaults)
                object.action = options.enums === String ? "GET" : 0;
            if (message.action != null && message.hasOwnProperty("action"))
                object.action = options.enums === String ? $root.server.GroupsStanza.Action[message.action] : message.action;
            if (message.groupStanzas && message.groupStanzas.length) {
                object.groupStanzas = [];
                for (var j = 0; j < message.groupStanzas.length; ++j)
                    object.groupStanzas[j] = $root.server.GroupStanza.toObject(message.groupStanzas[j], options);
            }
            return object;
        };

        /**
         * Converts this GroupsStanza to JSON.
         * @function toJSON
         * @memberof server.GroupsStanza
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        GroupsStanza.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Action enum.
         * @name server.GroupsStanza.Action
         * @enum {number}
         * @property {number} GET=0 GET value
         */
        GroupsStanza.Action = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "GET"] = 0;
            return values;
        })();

        return GroupsStanza;
    })();

    server.AuthRequest = (function() {

        /**
         * Properties of an AuthRequest.
         * @memberof server
         * @interface IAuthRequest
         * @property {number|Long|null} [uid] AuthRequest uid
         * @property {string|null} [pwd] AuthRequest pwd
         * @property {server.IClientMode|null} [clientMode] AuthRequest clientMode
         * @property {server.IClientVersion|null} [clientVersion] AuthRequest clientVersion
         * @property {string|null} [resource] AuthRequest resource
         */

        /**
         * Constructs a new AuthRequest.
         * @memberof server
         * @classdesc Represents an AuthRequest.
         * @implements IAuthRequest
         * @constructor
         * @param {server.IAuthRequest=} [properties] Properties to set
         */
        function AuthRequest(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * AuthRequest uid.
         * @member {number|Long} uid
         * @memberof server.AuthRequest
         * @instance
         */
        AuthRequest.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * AuthRequest pwd.
         * @member {string} pwd
         * @memberof server.AuthRequest
         * @instance
         */
        AuthRequest.prototype.pwd = "";

        /**
         * AuthRequest clientMode.
         * @member {server.IClientMode|null|undefined} clientMode
         * @memberof server.AuthRequest
         * @instance
         */
        AuthRequest.prototype.clientMode = null;

        /**
         * AuthRequest clientVersion.
         * @member {server.IClientVersion|null|undefined} clientVersion
         * @memberof server.AuthRequest
         * @instance
         */
        AuthRequest.prototype.clientVersion = null;

        /**
         * AuthRequest resource.
         * @member {string} resource
         * @memberof server.AuthRequest
         * @instance
         */
        AuthRequest.prototype.resource = "";

        /**
         * Creates a new AuthRequest instance using the specified properties.
         * @function create
         * @memberof server.AuthRequest
         * @static
         * @param {server.IAuthRequest=} [properties] Properties to set
         * @returns {server.AuthRequest} AuthRequest instance
         */
        AuthRequest.create = function create(properties) {
            return new AuthRequest(properties);
        };

        /**
         * Encodes the specified AuthRequest message. Does not implicitly {@link server.AuthRequest.verify|verify} messages.
         * @function encode
         * @memberof server.AuthRequest
         * @static
         * @param {server.IAuthRequest} message AuthRequest message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        AuthRequest.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 1, wireType 0 =*/8).int64(message.uid);
            if (message.pwd != null && Object.hasOwnProperty.call(message, "pwd"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.pwd);
            if (message.clientMode != null && Object.hasOwnProperty.call(message, "clientMode"))
                $root.server.ClientMode.encode(message.clientMode, writer.uint32(/* id 3, wireType 2 =*/26).fork()).ldelim();
            if (message.clientVersion != null && Object.hasOwnProperty.call(message, "clientVersion"))
                $root.server.ClientVersion.encode(message.clientVersion, writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            if (message.resource != null && Object.hasOwnProperty.call(message, "resource"))
                writer.uint32(/* id 5, wireType 2 =*/42).string(message.resource);
            return writer;
        };

        /**
         * Encodes the specified AuthRequest message, length delimited. Does not implicitly {@link server.AuthRequest.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.AuthRequest
         * @static
         * @param {server.IAuthRequest} message AuthRequest message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        AuthRequest.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an AuthRequest message from the specified reader or buffer.
         * @function decode
         * @memberof server.AuthRequest
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.AuthRequest} AuthRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        AuthRequest.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.AuthRequest();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.uid = reader.int64();
                    break;
                case 2:
                    message.pwd = reader.string();
                    break;
                case 3:
                    message.clientMode = $root.server.ClientMode.decode(reader, reader.uint32());
                    break;
                case 4:
                    message.clientVersion = $root.server.ClientVersion.decode(reader, reader.uint32());
                    break;
                case 5:
                    message.resource = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an AuthRequest message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.AuthRequest
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.AuthRequest} AuthRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        AuthRequest.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an AuthRequest message.
         * @function verify
         * @memberof server.AuthRequest
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        AuthRequest.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            if (message.pwd != null && message.hasOwnProperty("pwd"))
                if (!$util.isString(message.pwd))
                    return "pwd: string expected";
            if (message.clientMode != null && message.hasOwnProperty("clientMode")) {
                var error = $root.server.ClientMode.verify(message.clientMode);
                if (error)
                    return "clientMode." + error;
            }
            if (message.clientVersion != null && message.hasOwnProperty("clientVersion")) {
                var error = $root.server.ClientVersion.verify(message.clientVersion);
                if (error)
                    return "clientVersion." + error;
            }
            if (message.resource != null && message.hasOwnProperty("resource"))
                if (!$util.isString(message.resource))
                    return "resource: string expected";
            return null;
        };

        /**
         * Creates an AuthRequest message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.AuthRequest
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.AuthRequest} AuthRequest
         */
        AuthRequest.fromObject = function fromObject(object) {
            if (object instanceof $root.server.AuthRequest)
                return object;
            var message = new $root.server.AuthRequest();
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = false;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber();
            if (object.pwd != null)
                message.pwd = String(object.pwd);
            if (object.clientMode != null) {
                if (typeof object.clientMode !== "object")
                    throw TypeError(".server.AuthRequest.clientMode: object expected");
                message.clientMode = $root.server.ClientMode.fromObject(object.clientMode);
            }
            if (object.clientVersion != null) {
                if (typeof object.clientVersion !== "object")
                    throw TypeError(".server.AuthRequest.clientVersion: object expected");
                message.clientVersion = $root.server.ClientVersion.fromObject(object.clientVersion);
            }
            if (object.resource != null)
                message.resource = String(object.resource);
            return message;
        };

        /**
         * Creates a plain object from an AuthRequest message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.AuthRequest
         * @static
         * @param {server.AuthRequest} message AuthRequest
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        AuthRequest.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
                object.pwd = "";
                object.clientMode = null;
                object.clientVersion = null;
                object.resource = "";
            }
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber() : message.uid;
            if (message.pwd != null && message.hasOwnProperty("pwd"))
                object.pwd = message.pwd;
            if (message.clientMode != null && message.hasOwnProperty("clientMode"))
                object.clientMode = $root.server.ClientMode.toObject(message.clientMode, options);
            if (message.clientVersion != null && message.hasOwnProperty("clientVersion"))
                object.clientVersion = $root.server.ClientVersion.toObject(message.clientVersion, options);
            if (message.resource != null && message.hasOwnProperty("resource"))
                object.resource = message.resource;
            return object;
        };

        /**
         * Converts this AuthRequest to JSON.
         * @function toJSON
         * @memberof server.AuthRequest
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        AuthRequest.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return AuthRequest;
    })();

    server.AuthResult = (function() {

        /**
         * Properties of an AuthResult.
         * @memberof server
         * @interface IAuthResult
         * @property {string|null} [result] AuthResult result
         * @property {string|null} [reason] AuthResult reason
         * @property {Uint8Array|null} [propsHash] AuthResult propsHash
         */

        /**
         * Constructs a new AuthResult.
         * @memberof server
         * @classdesc Represents an AuthResult.
         * @implements IAuthResult
         * @constructor
         * @param {server.IAuthResult=} [properties] Properties to set
         */
        function AuthResult(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * AuthResult result.
         * @member {string} result
         * @memberof server.AuthResult
         * @instance
         */
        AuthResult.prototype.result = "";

        /**
         * AuthResult reason.
         * @member {string} reason
         * @memberof server.AuthResult
         * @instance
         */
        AuthResult.prototype.reason = "";

        /**
         * AuthResult propsHash.
         * @member {Uint8Array} propsHash
         * @memberof server.AuthResult
         * @instance
         */
        AuthResult.prototype.propsHash = $util.newBuffer([]);

        /**
         * Creates a new AuthResult instance using the specified properties.
         * @function create
         * @memberof server.AuthResult
         * @static
         * @param {server.IAuthResult=} [properties] Properties to set
         * @returns {server.AuthResult} AuthResult instance
         */
        AuthResult.create = function create(properties) {
            return new AuthResult(properties);
        };

        /**
         * Encodes the specified AuthResult message. Does not implicitly {@link server.AuthResult.verify|verify} messages.
         * @function encode
         * @memberof server.AuthResult
         * @static
         * @param {server.IAuthResult} message AuthResult message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        AuthResult.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.result != null && Object.hasOwnProperty.call(message, "result"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.result);
            if (message.reason != null && Object.hasOwnProperty.call(message, "reason"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.reason);
            if (message.propsHash != null && Object.hasOwnProperty.call(message, "propsHash"))
                writer.uint32(/* id 3, wireType 2 =*/26).bytes(message.propsHash);
            return writer;
        };

        /**
         * Encodes the specified AuthResult message, length delimited. Does not implicitly {@link server.AuthResult.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.AuthResult
         * @static
         * @param {server.IAuthResult} message AuthResult message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        AuthResult.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an AuthResult message from the specified reader or buffer.
         * @function decode
         * @memberof server.AuthResult
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.AuthResult} AuthResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        AuthResult.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.AuthResult();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.result = reader.string();
                    break;
                case 2:
                    message.reason = reader.string();
                    break;
                case 3:
                    message.propsHash = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an AuthResult message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.AuthResult
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.AuthResult} AuthResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        AuthResult.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an AuthResult message.
         * @function verify
         * @memberof server.AuthResult
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        AuthResult.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.result != null && message.hasOwnProperty("result"))
                if (!$util.isString(message.result))
                    return "result: string expected";
            if (message.reason != null && message.hasOwnProperty("reason"))
                if (!$util.isString(message.reason))
                    return "reason: string expected";
            if (message.propsHash != null && message.hasOwnProperty("propsHash"))
                if (!(message.propsHash && typeof message.propsHash.length === "number" || $util.isString(message.propsHash)))
                    return "propsHash: buffer expected";
            return null;
        };

        /**
         * Creates an AuthResult message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.AuthResult
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.AuthResult} AuthResult
         */
        AuthResult.fromObject = function fromObject(object) {
            if (object instanceof $root.server.AuthResult)
                return object;
            var message = new $root.server.AuthResult();
            if (object.result != null)
                message.result = String(object.result);
            if (object.reason != null)
                message.reason = String(object.reason);
            if (object.propsHash != null)
                if (typeof object.propsHash === "string")
                    $util.base64.decode(object.propsHash, message.propsHash = $util.newBuffer($util.base64.length(object.propsHash)), 0);
                else if (object.propsHash.length)
                    message.propsHash = object.propsHash;
            return message;
        };

        /**
         * Creates a plain object from an AuthResult message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.AuthResult
         * @static
         * @param {server.AuthResult} message AuthResult
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        AuthResult.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.result = "";
                object.reason = "";
                if (options.bytes === String)
                    object.propsHash = "";
                else {
                    object.propsHash = [];
                    if (options.bytes !== Array)
                        object.propsHash = $util.newBuffer(object.propsHash);
                }
            }
            if (message.result != null && message.hasOwnProperty("result"))
                object.result = message.result;
            if (message.reason != null && message.hasOwnProperty("reason"))
                object.reason = message.reason;
            if (message.propsHash != null && message.hasOwnProperty("propsHash"))
                object.propsHash = options.bytes === String ? $util.base64.encode(message.propsHash, 0, message.propsHash.length) : options.bytes === Array ? Array.prototype.slice.call(message.propsHash) : message.propsHash;
            return object;
        };

        /**
         * Converts this AuthResult to JSON.
         * @function toJSON
         * @memberof server.AuthResult
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        AuthResult.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return AuthResult;
    })();

    server.Invite = (function() {

        /**
         * Properties of an Invite.
         * @memberof server
         * @interface IInvite
         * @property {string|null} [phone] Invite phone
         * @property {string|null} [result] Invite result
         * @property {string|null} [reason] Invite reason
         */

        /**
         * Constructs a new Invite.
         * @memberof server
         * @classdesc Represents an Invite.
         * @implements IInvite
         * @constructor
         * @param {server.IInvite=} [properties] Properties to set
         */
        function Invite(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Invite phone.
         * @member {string} phone
         * @memberof server.Invite
         * @instance
         */
        Invite.prototype.phone = "";

        /**
         * Invite result.
         * @member {string} result
         * @memberof server.Invite
         * @instance
         */
        Invite.prototype.result = "";

        /**
         * Invite reason.
         * @member {string} reason
         * @memberof server.Invite
         * @instance
         */
        Invite.prototype.reason = "";

        /**
         * Creates a new Invite instance using the specified properties.
         * @function create
         * @memberof server.Invite
         * @static
         * @param {server.IInvite=} [properties] Properties to set
         * @returns {server.Invite} Invite instance
         */
        Invite.create = function create(properties) {
            return new Invite(properties);
        };

        /**
         * Encodes the specified Invite message. Does not implicitly {@link server.Invite.verify|verify} messages.
         * @function encode
         * @memberof server.Invite
         * @static
         * @param {server.IInvite} message Invite message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Invite.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.phone != null && Object.hasOwnProperty.call(message, "phone"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.phone);
            if (message.result != null && Object.hasOwnProperty.call(message, "result"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.result);
            if (message.reason != null && Object.hasOwnProperty.call(message, "reason"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.reason);
            return writer;
        };

        /**
         * Encodes the specified Invite message, length delimited. Does not implicitly {@link server.Invite.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Invite
         * @static
         * @param {server.IInvite} message Invite message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Invite.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an Invite message from the specified reader or buffer.
         * @function decode
         * @memberof server.Invite
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Invite} Invite
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Invite.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Invite();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.phone = reader.string();
                    break;
                case 2:
                    message.result = reader.string();
                    break;
                case 3:
                    message.reason = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an Invite message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Invite
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Invite} Invite
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Invite.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an Invite message.
         * @function verify
         * @memberof server.Invite
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Invite.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.phone != null && message.hasOwnProperty("phone"))
                if (!$util.isString(message.phone))
                    return "phone: string expected";
            if (message.result != null && message.hasOwnProperty("result"))
                if (!$util.isString(message.result))
                    return "result: string expected";
            if (message.reason != null && message.hasOwnProperty("reason"))
                if (!$util.isString(message.reason))
                    return "reason: string expected";
            return null;
        };

        /**
         * Creates an Invite message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Invite
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Invite} Invite
         */
        Invite.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Invite)
                return object;
            var message = new $root.server.Invite();
            if (object.phone != null)
                message.phone = String(object.phone);
            if (object.result != null)
                message.result = String(object.result);
            if (object.reason != null)
                message.reason = String(object.reason);
            return message;
        };

        /**
         * Creates a plain object from an Invite message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Invite
         * @static
         * @param {server.Invite} message Invite
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Invite.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.phone = "";
                object.result = "";
                object.reason = "";
            }
            if (message.phone != null && message.hasOwnProperty("phone"))
                object.phone = message.phone;
            if (message.result != null && message.hasOwnProperty("result"))
                object.result = message.result;
            if (message.reason != null && message.hasOwnProperty("reason"))
                object.reason = message.reason;
            return object;
        };

        /**
         * Converts this Invite to JSON.
         * @function toJSON
         * @memberof server.Invite
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Invite.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Invite;
    })();

    server.InvitesRequest = (function() {

        /**
         * Properties of an InvitesRequest.
         * @memberof server
         * @interface IInvitesRequest
         * @property {Array.<server.IInvite>|null} [invites] InvitesRequest invites
         */

        /**
         * Constructs a new InvitesRequest.
         * @memberof server
         * @classdesc Represents an InvitesRequest.
         * @implements IInvitesRequest
         * @constructor
         * @param {server.IInvitesRequest=} [properties] Properties to set
         */
        function InvitesRequest(properties) {
            this.invites = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * InvitesRequest invites.
         * @member {Array.<server.IInvite>} invites
         * @memberof server.InvitesRequest
         * @instance
         */
        InvitesRequest.prototype.invites = $util.emptyArray;

        /**
         * Creates a new InvitesRequest instance using the specified properties.
         * @function create
         * @memberof server.InvitesRequest
         * @static
         * @param {server.IInvitesRequest=} [properties] Properties to set
         * @returns {server.InvitesRequest} InvitesRequest instance
         */
        InvitesRequest.create = function create(properties) {
            return new InvitesRequest(properties);
        };

        /**
         * Encodes the specified InvitesRequest message. Does not implicitly {@link server.InvitesRequest.verify|verify} messages.
         * @function encode
         * @memberof server.InvitesRequest
         * @static
         * @param {server.IInvitesRequest} message InvitesRequest message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InvitesRequest.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.invites != null && message.invites.length)
                for (var i = 0; i < message.invites.length; ++i)
                    $root.server.Invite.encode(message.invites[i], writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified InvitesRequest message, length delimited. Does not implicitly {@link server.InvitesRequest.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.InvitesRequest
         * @static
         * @param {server.IInvitesRequest} message InvitesRequest message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InvitesRequest.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an InvitesRequest message from the specified reader or buffer.
         * @function decode
         * @memberof server.InvitesRequest
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.InvitesRequest} InvitesRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        InvitesRequest.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.InvitesRequest();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    if (!(message.invites && message.invites.length))
                        message.invites = [];
                    message.invites.push($root.server.Invite.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an InvitesRequest message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.InvitesRequest
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.InvitesRequest} InvitesRequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        InvitesRequest.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an InvitesRequest message.
         * @function verify
         * @memberof server.InvitesRequest
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        InvitesRequest.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.invites != null && message.hasOwnProperty("invites")) {
                if (!Array.isArray(message.invites))
                    return "invites: array expected";
                for (var i = 0; i < message.invites.length; ++i) {
                    var error = $root.server.Invite.verify(message.invites[i]);
                    if (error)
                        return "invites." + error;
                }
            }
            return null;
        };

        /**
         * Creates an InvitesRequest message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.InvitesRequest
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.InvitesRequest} InvitesRequest
         */
        InvitesRequest.fromObject = function fromObject(object) {
            if (object instanceof $root.server.InvitesRequest)
                return object;
            var message = new $root.server.InvitesRequest();
            if (object.invites) {
                if (!Array.isArray(object.invites))
                    throw TypeError(".server.InvitesRequest.invites: array expected");
                message.invites = [];
                for (var i = 0; i < object.invites.length; ++i) {
                    if (typeof object.invites[i] !== "object")
                        throw TypeError(".server.InvitesRequest.invites: object expected");
                    message.invites[i] = $root.server.Invite.fromObject(object.invites[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from an InvitesRequest message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.InvitesRequest
         * @static
         * @param {server.InvitesRequest} message InvitesRequest
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        InvitesRequest.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.invites = [];
            if (message.invites && message.invites.length) {
                object.invites = [];
                for (var j = 0; j < message.invites.length; ++j)
                    object.invites[j] = $root.server.Invite.toObject(message.invites[j], options);
            }
            return object;
        };

        /**
         * Converts this InvitesRequest to JSON.
         * @function toJSON
         * @memberof server.InvitesRequest
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        InvitesRequest.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return InvitesRequest;
    })();

    server.InvitesResponse = (function() {

        /**
         * Properties of an InvitesResponse.
         * @memberof server
         * @interface IInvitesResponse
         * @property {number|null} [invitesLeft] InvitesResponse invitesLeft
         * @property {number|Long|null} [timeUntilRefresh] InvitesResponse timeUntilRefresh
         * @property {Array.<server.IInvite>|null} [invites] InvitesResponse invites
         */

        /**
         * Constructs a new InvitesResponse.
         * @memberof server
         * @classdesc Represents an InvitesResponse.
         * @implements IInvitesResponse
         * @constructor
         * @param {server.IInvitesResponse=} [properties] Properties to set
         */
        function InvitesResponse(properties) {
            this.invites = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * InvitesResponse invitesLeft.
         * @member {number} invitesLeft
         * @memberof server.InvitesResponse
         * @instance
         */
        InvitesResponse.prototype.invitesLeft = 0;

        /**
         * InvitesResponse timeUntilRefresh.
         * @member {number|Long} timeUntilRefresh
         * @memberof server.InvitesResponse
         * @instance
         */
        InvitesResponse.prototype.timeUntilRefresh = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * InvitesResponse invites.
         * @member {Array.<server.IInvite>} invites
         * @memberof server.InvitesResponse
         * @instance
         */
        InvitesResponse.prototype.invites = $util.emptyArray;

        /**
         * Creates a new InvitesResponse instance using the specified properties.
         * @function create
         * @memberof server.InvitesResponse
         * @static
         * @param {server.IInvitesResponse=} [properties] Properties to set
         * @returns {server.InvitesResponse} InvitesResponse instance
         */
        InvitesResponse.create = function create(properties) {
            return new InvitesResponse(properties);
        };

        /**
         * Encodes the specified InvitesResponse message. Does not implicitly {@link server.InvitesResponse.verify|verify} messages.
         * @function encode
         * @memberof server.InvitesResponse
         * @static
         * @param {server.IInvitesResponse} message InvitesResponse message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InvitesResponse.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.invitesLeft != null && Object.hasOwnProperty.call(message, "invitesLeft"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.invitesLeft);
            if (message.timeUntilRefresh != null && Object.hasOwnProperty.call(message, "timeUntilRefresh"))
                writer.uint32(/* id 2, wireType 0 =*/16).int64(message.timeUntilRefresh);
            if (message.invites != null && message.invites.length)
                for (var i = 0; i < message.invites.length; ++i)
                    $root.server.Invite.encode(message.invites[i], writer.uint32(/* id 3, wireType 2 =*/26).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified InvitesResponse message, length delimited. Does not implicitly {@link server.InvitesResponse.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.InvitesResponse
         * @static
         * @param {server.IInvitesResponse} message InvitesResponse message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        InvitesResponse.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an InvitesResponse message from the specified reader or buffer.
         * @function decode
         * @memberof server.InvitesResponse
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.InvitesResponse} InvitesResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        InvitesResponse.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.InvitesResponse();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.invitesLeft = reader.int32();
                    break;
                case 2:
                    message.timeUntilRefresh = reader.int64();
                    break;
                case 3:
                    if (!(message.invites && message.invites.length))
                        message.invites = [];
                    message.invites.push($root.server.Invite.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an InvitesResponse message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.InvitesResponse
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.InvitesResponse} InvitesResponse
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        InvitesResponse.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an InvitesResponse message.
         * @function verify
         * @memberof server.InvitesResponse
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        InvitesResponse.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.invitesLeft != null && message.hasOwnProperty("invitesLeft"))
                if (!$util.isInteger(message.invitesLeft))
                    return "invitesLeft: integer expected";
            if (message.timeUntilRefresh != null && message.hasOwnProperty("timeUntilRefresh"))
                if (!$util.isInteger(message.timeUntilRefresh) && !(message.timeUntilRefresh && $util.isInteger(message.timeUntilRefresh.low) && $util.isInteger(message.timeUntilRefresh.high)))
                    return "timeUntilRefresh: integer|Long expected";
            if (message.invites != null && message.hasOwnProperty("invites")) {
                if (!Array.isArray(message.invites))
                    return "invites: array expected";
                for (var i = 0; i < message.invites.length; ++i) {
                    var error = $root.server.Invite.verify(message.invites[i]);
                    if (error)
                        return "invites." + error;
                }
            }
            return null;
        };

        /**
         * Creates an InvitesResponse message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.InvitesResponse
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.InvitesResponse} InvitesResponse
         */
        InvitesResponse.fromObject = function fromObject(object) {
            if (object instanceof $root.server.InvitesResponse)
                return object;
            var message = new $root.server.InvitesResponse();
            if (object.invitesLeft != null)
                message.invitesLeft = object.invitesLeft | 0;
            if (object.timeUntilRefresh != null)
                if ($util.Long)
                    (message.timeUntilRefresh = $util.Long.fromValue(object.timeUntilRefresh)).unsigned = false;
                else if (typeof object.timeUntilRefresh === "string")
                    message.timeUntilRefresh = parseInt(object.timeUntilRefresh, 10);
                else if (typeof object.timeUntilRefresh === "number")
                    message.timeUntilRefresh = object.timeUntilRefresh;
                else if (typeof object.timeUntilRefresh === "object")
                    message.timeUntilRefresh = new $util.LongBits(object.timeUntilRefresh.low >>> 0, object.timeUntilRefresh.high >>> 0).toNumber();
            if (object.invites) {
                if (!Array.isArray(object.invites))
                    throw TypeError(".server.InvitesResponse.invites: array expected");
                message.invites = [];
                for (var i = 0; i < object.invites.length; ++i) {
                    if (typeof object.invites[i] !== "object")
                        throw TypeError(".server.InvitesResponse.invites: object expected");
                    message.invites[i] = $root.server.Invite.fromObject(object.invites[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from an InvitesResponse message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.InvitesResponse
         * @static
         * @param {server.InvitesResponse} message InvitesResponse
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        InvitesResponse.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.invites = [];
            if (options.defaults) {
                object.invitesLeft = 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.timeUntilRefresh = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.timeUntilRefresh = options.longs === String ? "0" : 0;
            }
            if (message.invitesLeft != null && message.hasOwnProperty("invitesLeft"))
                object.invitesLeft = message.invitesLeft;
            if (message.timeUntilRefresh != null && message.hasOwnProperty("timeUntilRefresh"))
                if (typeof message.timeUntilRefresh === "number")
                    object.timeUntilRefresh = options.longs === String ? String(message.timeUntilRefresh) : message.timeUntilRefresh;
                else
                    object.timeUntilRefresh = options.longs === String ? $util.Long.prototype.toString.call(message.timeUntilRefresh) : options.longs === Number ? new $util.LongBits(message.timeUntilRefresh.low >>> 0, message.timeUntilRefresh.high >>> 0).toNumber() : message.timeUntilRefresh;
            if (message.invites && message.invites.length) {
                object.invites = [];
                for (var j = 0; j < message.invites.length; ++j)
                    object.invites[j] = $root.server.Invite.toObject(message.invites[j], options);
            }
            return object;
        };

        /**
         * Converts this InvitesResponse to JSON.
         * @function toJSON
         * @memberof server.InvitesResponse
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        InvitesResponse.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return InvitesResponse;
    })();

    server.MediaUrl = (function() {

        /**
         * Properties of a MediaUrl.
         * @memberof server
         * @interface IMediaUrl
         * @property {string|null} [get] MediaUrl get
         * @property {string|null} [put] MediaUrl put
         * @property {string|null} [patch] MediaUrl patch
         */

        /**
         * Constructs a new MediaUrl.
         * @memberof server
         * @classdesc Represents a MediaUrl.
         * @implements IMediaUrl
         * @constructor
         * @param {server.IMediaUrl=} [properties] Properties to set
         */
        function MediaUrl(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * MediaUrl get.
         * @member {string} get
         * @memberof server.MediaUrl
         * @instance
         */
        MediaUrl.prototype.get = "";

        /**
         * MediaUrl put.
         * @member {string} put
         * @memberof server.MediaUrl
         * @instance
         */
        MediaUrl.prototype.put = "";

        /**
         * MediaUrl patch.
         * @member {string} patch
         * @memberof server.MediaUrl
         * @instance
         */
        MediaUrl.prototype.patch = "";

        /**
         * Creates a new MediaUrl instance using the specified properties.
         * @function create
         * @memberof server.MediaUrl
         * @static
         * @param {server.IMediaUrl=} [properties] Properties to set
         * @returns {server.MediaUrl} MediaUrl instance
         */
        MediaUrl.create = function create(properties) {
            return new MediaUrl(properties);
        };

        /**
         * Encodes the specified MediaUrl message. Does not implicitly {@link server.MediaUrl.verify|verify} messages.
         * @function encode
         * @memberof server.MediaUrl
         * @static
         * @param {server.IMediaUrl} message MediaUrl message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MediaUrl.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.get != null && Object.hasOwnProperty.call(message, "get"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.get);
            if (message.put != null && Object.hasOwnProperty.call(message, "put"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.put);
            if (message.patch != null && Object.hasOwnProperty.call(message, "patch"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.patch);
            return writer;
        };

        /**
         * Encodes the specified MediaUrl message, length delimited. Does not implicitly {@link server.MediaUrl.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.MediaUrl
         * @static
         * @param {server.IMediaUrl} message MediaUrl message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MediaUrl.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a MediaUrl message from the specified reader or buffer.
         * @function decode
         * @memberof server.MediaUrl
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.MediaUrl} MediaUrl
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        MediaUrl.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.MediaUrl();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.get = reader.string();
                    break;
                case 2:
                    message.put = reader.string();
                    break;
                case 3:
                    message.patch = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a MediaUrl message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.MediaUrl
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.MediaUrl} MediaUrl
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        MediaUrl.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a MediaUrl message.
         * @function verify
         * @memberof server.MediaUrl
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        MediaUrl.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.get != null && message.hasOwnProperty("get"))
                if (!$util.isString(message.get))
                    return "get: string expected";
            if (message.put != null && message.hasOwnProperty("put"))
                if (!$util.isString(message.put))
                    return "put: string expected";
            if (message.patch != null && message.hasOwnProperty("patch"))
                if (!$util.isString(message.patch))
                    return "patch: string expected";
            return null;
        };

        /**
         * Creates a MediaUrl message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.MediaUrl
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.MediaUrl} MediaUrl
         */
        MediaUrl.fromObject = function fromObject(object) {
            if (object instanceof $root.server.MediaUrl)
                return object;
            var message = new $root.server.MediaUrl();
            if (object.get != null)
                message.get = String(object.get);
            if (object.put != null)
                message.put = String(object.put);
            if (object.patch != null)
                message.patch = String(object.patch);
            return message;
        };

        /**
         * Creates a plain object from a MediaUrl message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.MediaUrl
         * @static
         * @param {server.MediaUrl} message MediaUrl
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        MediaUrl.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.get = "";
                object.put = "";
                object.patch = "";
            }
            if (message.get != null && message.hasOwnProperty("get"))
                object.get = message.get;
            if (message.put != null && message.hasOwnProperty("put"))
                object.put = message.put;
            if (message.patch != null && message.hasOwnProperty("patch"))
                object.patch = message.patch;
            return object;
        };

        /**
         * Converts this MediaUrl to JSON.
         * @function toJSON
         * @memberof server.MediaUrl
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        MediaUrl.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return MediaUrl;
    })();

    server.UploadMedia = (function() {

        /**
         * Properties of an UploadMedia.
         * @memberof server
         * @interface IUploadMedia
         * @property {number|Long|null} [size] UploadMedia size
         * @property {server.IMediaUrl|null} [url] UploadMedia url
         */

        /**
         * Constructs a new UploadMedia.
         * @memberof server
         * @classdesc Represents an UploadMedia.
         * @implements IUploadMedia
         * @constructor
         * @param {server.IUploadMedia=} [properties] Properties to set
         */
        function UploadMedia(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * UploadMedia size.
         * @member {number|Long} size
         * @memberof server.UploadMedia
         * @instance
         */
        UploadMedia.prototype.size = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * UploadMedia url.
         * @member {server.IMediaUrl|null|undefined} url
         * @memberof server.UploadMedia
         * @instance
         */
        UploadMedia.prototype.url = null;

        /**
         * Creates a new UploadMedia instance using the specified properties.
         * @function create
         * @memberof server.UploadMedia
         * @static
         * @param {server.IUploadMedia=} [properties] Properties to set
         * @returns {server.UploadMedia} UploadMedia instance
         */
        UploadMedia.create = function create(properties) {
            return new UploadMedia(properties);
        };

        /**
         * Encodes the specified UploadMedia message. Does not implicitly {@link server.UploadMedia.verify|verify} messages.
         * @function encode
         * @memberof server.UploadMedia
         * @static
         * @param {server.IUploadMedia} message UploadMedia message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UploadMedia.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.size != null && Object.hasOwnProperty.call(message, "size"))
                writer.uint32(/* id 1, wireType 0 =*/8).int64(message.size);
            if (message.url != null && Object.hasOwnProperty.call(message, "url"))
                $root.server.MediaUrl.encode(message.url, writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified UploadMedia message, length delimited. Does not implicitly {@link server.UploadMedia.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.UploadMedia
         * @static
         * @param {server.IUploadMedia} message UploadMedia message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UploadMedia.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an UploadMedia message from the specified reader or buffer.
         * @function decode
         * @memberof server.UploadMedia
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.UploadMedia} UploadMedia
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        UploadMedia.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.UploadMedia();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.size = reader.int64();
                    break;
                case 2:
                    message.url = $root.server.MediaUrl.decode(reader, reader.uint32());
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an UploadMedia message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.UploadMedia
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.UploadMedia} UploadMedia
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        UploadMedia.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an UploadMedia message.
         * @function verify
         * @memberof server.UploadMedia
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        UploadMedia.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.size != null && message.hasOwnProperty("size"))
                if (!$util.isInteger(message.size) && !(message.size && $util.isInteger(message.size.low) && $util.isInteger(message.size.high)))
                    return "size: integer|Long expected";
            if (message.url != null && message.hasOwnProperty("url")) {
                var error = $root.server.MediaUrl.verify(message.url);
                if (error)
                    return "url." + error;
            }
            return null;
        };

        /**
         * Creates an UploadMedia message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.UploadMedia
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.UploadMedia} UploadMedia
         */
        UploadMedia.fromObject = function fromObject(object) {
            if (object instanceof $root.server.UploadMedia)
                return object;
            var message = new $root.server.UploadMedia();
            if (object.size != null)
                if ($util.Long)
                    (message.size = $util.Long.fromValue(object.size)).unsigned = false;
                else if (typeof object.size === "string")
                    message.size = parseInt(object.size, 10);
                else if (typeof object.size === "number")
                    message.size = object.size;
                else if (typeof object.size === "object")
                    message.size = new $util.LongBits(object.size.low >>> 0, object.size.high >>> 0).toNumber();
            if (object.url != null) {
                if (typeof object.url !== "object")
                    throw TypeError(".server.UploadMedia.url: object expected");
                message.url = $root.server.MediaUrl.fromObject(object.url);
            }
            return message;
        };

        /**
         * Creates a plain object from an UploadMedia message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.UploadMedia
         * @static
         * @param {server.UploadMedia} message UploadMedia
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        UploadMedia.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.size = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.size = options.longs === String ? "0" : 0;
                object.url = null;
            }
            if (message.size != null && message.hasOwnProperty("size"))
                if (typeof message.size === "number")
                    object.size = options.longs === String ? String(message.size) : message.size;
                else
                    object.size = options.longs === String ? $util.Long.prototype.toString.call(message.size) : options.longs === Number ? new $util.LongBits(message.size.low >>> 0, message.size.high >>> 0).toNumber() : message.size;
            if (message.url != null && message.hasOwnProperty("url"))
                object.url = $root.server.MediaUrl.toObject(message.url, options);
            return object;
        };

        /**
         * Converts this UploadMedia to JSON.
         * @function toJSON
         * @memberof server.UploadMedia
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        UploadMedia.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return UploadMedia;
    })();

    server.ChatStanza = (function() {

        /**
         * Properties of a ChatStanza.
         * @memberof server
         * @interface IChatStanza
         * @property {number|Long|null} [timestamp] ChatStanza timestamp
         * @property {Uint8Array|null} [payload] ChatStanza payload
         * @property {Uint8Array|null} [encPayload] ChatStanza encPayload
         * @property {Uint8Array|null} [publicKey] ChatStanza publicKey
         * @property {number|Long|null} [oneTimePreKeyId] ChatStanza oneTimePreKeyId
         * @property {string|null} [senderName] ChatStanza senderName
         * @property {string|null} [senderLogInfo] ChatStanza senderLogInfo
         * @property {string|null} [senderClientVersion] ChatStanza senderClientVersion
         */

        /**
         * Constructs a new ChatStanza.
         * @memberof server
         * @classdesc Represents a ChatStanza.
         * @implements IChatStanza
         * @constructor
         * @param {server.IChatStanza=} [properties] Properties to set
         */
        function ChatStanza(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ChatStanza timestamp.
         * @member {number|Long} timestamp
         * @memberof server.ChatStanza
         * @instance
         */
        ChatStanza.prototype.timestamp = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * ChatStanza payload.
         * @member {Uint8Array} payload
         * @memberof server.ChatStanza
         * @instance
         */
        ChatStanza.prototype.payload = $util.newBuffer([]);

        /**
         * ChatStanza encPayload.
         * @member {Uint8Array} encPayload
         * @memberof server.ChatStanza
         * @instance
         */
        ChatStanza.prototype.encPayload = $util.newBuffer([]);

        /**
         * ChatStanza publicKey.
         * @member {Uint8Array} publicKey
         * @memberof server.ChatStanza
         * @instance
         */
        ChatStanza.prototype.publicKey = $util.newBuffer([]);

        /**
         * ChatStanza oneTimePreKeyId.
         * @member {number|Long} oneTimePreKeyId
         * @memberof server.ChatStanza
         * @instance
         */
        ChatStanza.prototype.oneTimePreKeyId = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * ChatStanza senderName.
         * @member {string} senderName
         * @memberof server.ChatStanza
         * @instance
         */
        ChatStanza.prototype.senderName = "";

        /**
         * ChatStanza senderLogInfo.
         * @member {string} senderLogInfo
         * @memberof server.ChatStanza
         * @instance
         */
        ChatStanza.prototype.senderLogInfo = "";

        /**
         * ChatStanza senderClientVersion.
         * @member {string} senderClientVersion
         * @memberof server.ChatStanza
         * @instance
         */
        ChatStanza.prototype.senderClientVersion = "";

        /**
         * Creates a new ChatStanza instance using the specified properties.
         * @function create
         * @memberof server.ChatStanza
         * @static
         * @param {server.IChatStanza=} [properties] Properties to set
         * @returns {server.ChatStanza} ChatStanza instance
         */
        ChatStanza.create = function create(properties) {
            return new ChatStanza(properties);
        };

        /**
         * Encodes the specified ChatStanza message. Does not implicitly {@link server.ChatStanza.verify|verify} messages.
         * @function encode
         * @memberof server.ChatStanza
         * @static
         * @param {server.IChatStanza} message ChatStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ChatStanza.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.timestamp != null && Object.hasOwnProperty.call(message, "timestamp"))
                writer.uint32(/* id 1, wireType 0 =*/8).int64(message.timestamp);
            if (message.payload != null && Object.hasOwnProperty.call(message, "payload"))
                writer.uint32(/* id 2, wireType 2 =*/18).bytes(message.payload);
            if (message.encPayload != null && Object.hasOwnProperty.call(message, "encPayload"))
                writer.uint32(/* id 3, wireType 2 =*/26).bytes(message.encPayload);
            if (message.publicKey != null && Object.hasOwnProperty.call(message, "publicKey"))
                writer.uint32(/* id 4, wireType 2 =*/34).bytes(message.publicKey);
            if (message.oneTimePreKeyId != null && Object.hasOwnProperty.call(message, "oneTimePreKeyId"))
                writer.uint32(/* id 5, wireType 0 =*/40).int64(message.oneTimePreKeyId);
            if (message.senderName != null && Object.hasOwnProperty.call(message, "senderName"))
                writer.uint32(/* id 6, wireType 2 =*/50).string(message.senderName);
            if (message.senderLogInfo != null && Object.hasOwnProperty.call(message, "senderLogInfo"))
                writer.uint32(/* id 16, wireType 2 =*/130).string(message.senderLogInfo);
            if (message.senderClientVersion != null && Object.hasOwnProperty.call(message, "senderClientVersion"))
                writer.uint32(/* id 17, wireType 2 =*/138).string(message.senderClientVersion);
            return writer;
        };

        /**
         * Encodes the specified ChatStanza message, length delimited. Does not implicitly {@link server.ChatStanza.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.ChatStanza
         * @static
         * @param {server.IChatStanza} message ChatStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ChatStanza.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ChatStanza message from the specified reader or buffer.
         * @function decode
         * @memberof server.ChatStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.ChatStanza} ChatStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ChatStanza.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.ChatStanza();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.timestamp = reader.int64();
                    break;
                case 2:
                    message.payload = reader.bytes();
                    break;
                case 3:
                    message.encPayload = reader.bytes();
                    break;
                case 4:
                    message.publicKey = reader.bytes();
                    break;
                case 5:
                    message.oneTimePreKeyId = reader.int64();
                    break;
                case 6:
                    message.senderName = reader.string();
                    break;
                case 16:
                    message.senderLogInfo = reader.string();
                    break;
                case 17:
                    message.senderClientVersion = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ChatStanza message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.ChatStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.ChatStanza} ChatStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ChatStanza.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ChatStanza message.
         * @function verify
         * @memberof server.ChatStanza
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ChatStanza.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (!$util.isInteger(message.timestamp) && !(message.timestamp && $util.isInteger(message.timestamp.low) && $util.isInteger(message.timestamp.high)))
                    return "timestamp: integer|Long expected";
            if (message.payload != null && message.hasOwnProperty("payload"))
                if (!(message.payload && typeof message.payload.length === "number" || $util.isString(message.payload)))
                    return "payload: buffer expected";
            if (message.encPayload != null && message.hasOwnProperty("encPayload"))
                if (!(message.encPayload && typeof message.encPayload.length === "number" || $util.isString(message.encPayload)))
                    return "encPayload: buffer expected";
            if (message.publicKey != null && message.hasOwnProperty("publicKey"))
                if (!(message.publicKey && typeof message.publicKey.length === "number" || $util.isString(message.publicKey)))
                    return "publicKey: buffer expected";
            if (message.oneTimePreKeyId != null && message.hasOwnProperty("oneTimePreKeyId"))
                if (!$util.isInteger(message.oneTimePreKeyId) && !(message.oneTimePreKeyId && $util.isInteger(message.oneTimePreKeyId.low) && $util.isInteger(message.oneTimePreKeyId.high)))
                    return "oneTimePreKeyId: integer|Long expected";
            if (message.senderName != null && message.hasOwnProperty("senderName"))
                if (!$util.isString(message.senderName))
                    return "senderName: string expected";
            if (message.senderLogInfo != null && message.hasOwnProperty("senderLogInfo"))
                if (!$util.isString(message.senderLogInfo))
                    return "senderLogInfo: string expected";
            if (message.senderClientVersion != null && message.hasOwnProperty("senderClientVersion"))
                if (!$util.isString(message.senderClientVersion))
                    return "senderClientVersion: string expected";
            return null;
        };

        /**
         * Creates a ChatStanza message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.ChatStanza
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.ChatStanza} ChatStanza
         */
        ChatStanza.fromObject = function fromObject(object) {
            if (object instanceof $root.server.ChatStanza)
                return object;
            var message = new $root.server.ChatStanza();
            if (object.timestamp != null)
                if ($util.Long)
                    (message.timestamp = $util.Long.fromValue(object.timestamp)).unsigned = false;
                else if (typeof object.timestamp === "string")
                    message.timestamp = parseInt(object.timestamp, 10);
                else if (typeof object.timestamp === "number")
                    message.timestamp = object.timestamp;
                else if (typeof object.timestamp === "object")
                    message.timestamp = new $util.LongBits(object.timestamp.low >>> 0, object.timestamp.high >>> 0).toNumber();
            if (object.payload != null)
                if (typeof object.payload === "string")
                    $util.base64.decode(object.payload, message.payload = $util.newBuffer($util.base64.length(object.payload)), 0);
                else if (object.payload.length)
                    message.payload = object.payload;
            if (object.encPayload != null)
                if (typeof object.encPayload === "string")
                    $util.base64.decode(object.encPayload, message.encPayload = $util.newBuffer($util.base64.length(object.encPayload)), 0);
                else if (object.encPayload.length)
                    message.encPayload = object.encPayload;
            if (object.publicKey != null)
                if (typeof object.publicKey === "string")
                    $util.base64.decode(object.publicKey, message.publicKey = $util.newBuffer($util.base64.length(object.publicKey)), 0);
                else if (object.publicKey.length)
                    message.publicKey = object.publicKey;
            if (object.oneTimePreKeyId != null)
                if ($util.Long)
                    (message.oneTimePreKeyId = $util.Long.fromValue(object.oneTimePreKeyId)).unsigned = false;
                else if (typeof object.oneTimePreKeyId === "string")
                    message.oneTimePreKeyId = parseInt(object.oneTimePreKeyId, 10);
                else if (typeof object.oneTimePreKeyId === "number")
                    message.oneTimePreKeyId = object.oneTimePreKeyId;
                else if (typeof object.oneTimePreKeyId === "object")
                    message.oneTimePreKeyId = new $util.LongBits(object.oneTimePreKeyId.low >>> 0, object.oneTimePreKeyId.high >>> 0).toNumber();
            if (object.senderName != null)
                message.senderName = String(object.senderName);
            if (object.senderLogInfo != null)
                message.senderLogInfo = String(object.senderLogInfo);
            if (object.senderClientVersion != null)
                message.senderClientVersion = String(object.senderClientVersion);
            return message;
        };

        /**
         * Creates a plain object from a ChatStanza message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.ChatStanza
         * @static
         * @param {server.ChatStanza} message ChatStanza
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ChatStanza.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.timestamp = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.timestamp = options.longs === String ? "0" : 0;
                if (options.bytes === String)
                    object.payload = "";
                else {
                    object.payload = [];
                    if (options.bytes !== Array)
                        object.payload = $util.newBuffer(object.payload);
                }
                if (options.bytes === String)
                    object.encPayload = "";
                else {
                    object.encPayload = [];
                    if (options.bytes !== Array)
                        object.encPayload = $util.newBuffer(object.encPayload);
                }
                if (options.bytes === String)
                    object.publicKey = "";
                else {
                    object.publicKey = [];
                    if (options.bytes !== Array)
                        object.publicKey = $util.newBuffer(object.publicKey);
                }
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.oneTimePreKeyId = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.oneTimePreKeyId = options.longs === String ? "0" : 0;
                object.senderName = "";
                object.senderLogInfo = "";
                object.senderClientVersion = "";
            }
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (typeof message.timestamp === "number")
                    object.timestamp = options.longs === String ? String(message.timestamp) : message.timestamp;
                else
                    object.timestamp = options.longs === String ? $util.Long.prototype.toString.call(message.timestamp) : options.longs === Number ? new $util.LongBits(message.timestamp.low >>> 0, message.timestamp.high >>> 0).toNumber() : message.timestamp;
            if (message.payload != null && message.hasOwnProperty("payload"))
                object.payload = options.bytes === String ? $util.base64.encode(message.payload, 0, message.payload.length) : options.bytes === Array ? Array.prototype.slice.call(message.payload) : message.payload;
            if (message.encPayload != null && message.hasOwnProperty("encPayload"))
                object.encPayload = options.bytes === String ? $util.base64.encode(message.encPayload, 0, message.encPayload.length) : options.bytes === Array ? Array.prototype.slice.call(message.encPayload) : message.encPayload;
            if (message.publicKey != null && message.hasOwnProperty("publicKey"))
                object.publicKey = options.bytes === String ? $util.base64.encode(message.publicKey, 0, message.publicKey.length) : options.bytes === Array ? Array.prototype.slice.call(message.publicKey) : message.publicKey;
            if (message.oneTimePreKeyId != null && message.hasOwnProperty("oneTimePreKeyId"))
                if (typeof message.oneTimePreKeyId === "number")
                    object.oneTimePreKeyId = options.longs === String ? String(message.oneTimePreKeyId) : message.oneTimePreKeyId;
                else
                    object.oneTimePreKeyId = options.longs === String ? $util.Long.prototype.toString.call(message.oneTimePreKeyId) : options.longs === Number ? new $util.LongBits(message.oneTimePreKeyId.low >>> 0, message.oneTimePreKeyId.high >>> 0).toNumber() : message.oneTimePreKeyId;
            if (message.senderName != null && message.hasOwnProperty("senderName"))
                object.senderName = message.senderName;
            if (message.senderLogInfo != null && message.hasOwnProperty("senderLogInfo"))
                object.senderLogInfo = message.senderLogInfo;
            if (message.senderClientVersion != null && message.hasOwnProperty("senderClientVersion"))
                object.senderClientVersion = message.senderClientVersion;
            return object;
        };

        /**
         * Converts this ChatStanza to JSON.
         * @function toJSON
         * @memberof server.ChatStanza
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ChatStanza.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ChatStanza;
    })();

    server.SilentChatStanza = (function() {

        /**
         * Properties of a SilentChatStanza.
         * @memberof server
         * @interface ISilentChatStanza
         * @property {server.IChatStanza|null} [chatStanza] SilentChatStanza chatStanza
         */

        /**
         * Constructs a new SilentChatStanza.
         * @memberof server
         * @classdesc Represents a SilentChatStanza.
         * @implements ISilentChatStanza
         * @constructor
         * @param {server.ISilentChatStanza=} [properties] Properties to set
         */
        function SilentChatStanza(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * SilentChatStanza chatStanza.
         * @member {server.IChatStanza|null|undefined} chatStanza
         * @memberof server.SilentChatStanza
         * @instance
         */
        SilentChatStanza.prototype.chatStanza = null;

        /**
         * Creates a new SilentChatStanza instance using the specified properties.
         * @function create
         * @memberof server.SilentChatStanza
         * @static
         * @param {server.ISilentChatStanza=} [properties] Properties to set
         * @returns {server.SilentChatStanza} SilentChatStanza instance
         */
        SilentChatStanza.create = function create(properties) {
            return new SilentChatStanza(properties);
        };

        /**
         * Encodes the specified SilentChatStanza message. Does not implicitly {@link server.SilentChatStanza.verify|verify} messages.
         * @function encode
         * @memberof server.SilentChatStanza
         * @static
         * @param {server.ISilentChatStanza} message SilentChatStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        SilentChatStanza.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.chatStanza != null && Object.hasOwnProperty.call(message, "chatStanza"))
                $root.server.ChatStanza.encode(message.chatStanza, writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified SilentChatStanza message, length delimited. Does not implicitly {@link server.SilentChatStanza.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.SilentChatStanza
         * @static
         * @param {server.ISilentChatStanza} message SilentChatStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        SilentChatStanza.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a SilentChatStanza message from the specified reader or buffer.
         * @function decode
         * @memberof server.SilentChatStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.SilentChatStanza} SilentChatStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        SilentChatStanza.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.SilentChatStanza();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.chatStanza = $root.server.ChatStanza.decode(reader, reader.uint32());
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a SilentChatStanza message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.SilentChatStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.SilentChatStanza} SilentChatStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        SilentChatStanza.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a SilentChatStanza message.
         * @function verify
         * @memberof server.SilentChatStanza
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        SilentChatStanza.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.chatStanza != null && message.hasOwnProperty("chatStanza")) {
                var error = $root.server.ChatStanza.verify(message.chatStanza);
                if (error)
                    return "chatStanza." + error;
            }
            return null;
        };

        /**
         * Creates a SilentChatStanza message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.SilentChatStanza
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.SilentChatStanza} SilentChatStanza
         */
        SilentChatStanza.fromObject = function fromObject(object) {
            if (object instanceof $root.server.SilentChatStanza)
                return object;
            var message = new $root.server.SilentChatStanza();
            if (object.chatStanza != null) {
                if (typeof object.chatStanza !== "object")
                    throw TypeError(".server.SilentChatStanza.chatStanza: object expected");
                message.chatStanza = $root.server.ChatStanza.fromObject(object.chatStanza);
            }
            return message;
        };

        /**
         * Creates a plain object from a SilentChatStanza message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.SilentChatStanza
         * @static
         * @param {server.SilentChatStanza} message SilentChatStanza
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        SilentChatStanza.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults)
                object.chatStanza = null;
            if (message.chatStanza != null && message.hasOwnProperty("chatStanza"))
                object.chatStanza = $root.server.ChatStanza.toObject(message.chatStanza, options);
            return object;
        };

        /**
         * Converts this SilentChatStanza to JSON.
         * @function toJSON
         * @memberof server.SilentChatStanza
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        SilentChatStanza.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return SilentChatStanza;
    })();

    server.Ping = (function() {

        /**
         * Properties of a Ping.
         * @memberof server
         * @interface IPing
         */

        /**
         * Constructs a new Ping.
         * @memberof server
         * @classdesc Represents a Ping.
         * @implements IPing
         * @constructor
         * @param {server.IPing=} [properties] Properties to set
         */
        function Ping(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Creates a new Ping instance using the specified properties.
         * @function create
         * @memberof server.Ping
         * @static
         * @param {server.IPing=} [properties] Properties to set
         * @returns {server.Ping} Ping instance
         */
        Ping.create = function create(properties) {
            return new Ping(properties);
        };

        /**
         * Encodes the specified Ping message. Does not implicitly {@link server.Ping.verify|verify} messages.
         * @function encode
         * @memberof server.Ping
         * @static
         * @param {server.IPing} message Ping message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Ping.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            return writer;
        };

        /**
         * Encodes the specified Ping message, length delimited. Does not implicitly {@link server.Ping.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Ping
         * @static
         * @param {server.IPing} message Ping message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Ping.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Ping message from the specified reader or buffer.
         * @function decode
         * @memberof server.Ping
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Ping} Ping
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Ping.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Ping();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Ping message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Ping
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Ping} Ping
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Ping.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Ping message.
         * @function verify
         * @memberof server.Ping
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Ping.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            return null;
        };

        /**
         * Creates a Ping message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Ping
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Ping} Ping
         */
        Ping.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Ping)
                return object;
            return new $root.server.Ping();
        };

        /**
         * Creates a plain object from a Ping message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Ping
         * @static
         * @param {server.Ping} message Ping
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Ping.toObject = function toObject() {
            return {};
        };

        /**
         * Converts this Ping to JSON.
         * @function toJSON
         * @memberof server.Ping
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Ping.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Ping;
    })();

    server.ErrorStanza = (function() {

        /**
         * Properties of an ErrorStanza.
         * @memberof server
         * @interface IErrorStanza
         * @property {string|null} [reason] ErrorStanza reason
         */

        /**
         * Constructs a new ErrorStanza.
         * @memberof server
         * @classdesc Represents an ErrorStanza.
         * @implements IErrorStanza
         * @constructor
         * @param {server.IErrorStanza=} [properties] Properties to set
         */
        function ErrorStanza(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ErrorStanza reason.
         * @member {string} reason
         * @memberof server.ErrorStanza
         * @instance
         */
        ErrorStanza.prototype.reason = "";

        /**
         * Creates a new ErrorStanza instance using the specified properties.
         * @function create
         * @memberof server.ErrorStanza
         * @static
         * @param {server.IErrorStanza=} [properties] Properties to set
         * @returns {server.ErrorStanza} ErrorStanza instance
         */
        ErrorStanza.create = function create(properties) {
            return new ErrorStanza(properties);
        };

        /**
         * Encodes the specified ErrorStanza message. Does not implicitly {@link server.ErrorStanza.verify|verify} messages.
         * @function encode
         * @memberof server.ErrorStanza
         * @static
         * @param {server.IErrorStanza} message ErrorStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ErrorStanza.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.reason != null && Object.hasOwnProperty.call(message, "reason"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.reason);
            return writer;
        };

        /**
         * Encodes the specified ErrorStanza message, length delimited. Does not implicitly {@link server.ErrorStanza.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.ErrorStanza
         * @static
         * @param {server.IErrorStanza} message ErrorStanza message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ErrorStanza.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an ErrorStanza message from the specified reader or buffer.
         * @function decode
         * @memberof server.ErrorStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.ErrorStanza} ErrorStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ErrorStanza.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.ErrorStanza();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.reason = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an ErrorStanza message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.ErrorStanza
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.ErrorStanza} ErrorStanza
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ErrorStanza.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an ErrorStanza message.
         * @function verify
         * @memberof server.ErrorStanza
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ErrorStanza.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.reason != null && message.hasOwnProperty("reason"))
                if (!$util.isString(message.reason))
                    return "reason: string expected";
            return null;
        };

        /**
         * Creates an ErrorStanza message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.ErrorStanza
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.ErrorStanza} ErrorStanza
         */
        ErrorStanza.fromObject = function fromObject(object) {
            if (object instanceof $root.server.ErrorStanza)
                return object;
            var message = new $root.server.ErrorStanza();
            if (object.reason != null)
                message.reason = String(object.reason);
            return message;
        };

        /**
         * Creates a plain object from an ErrorStanza message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.ErrorStanza
         * @static
         * @param {server.ErrorStanza} message ErrorStanza
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ErrorStanza.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults)
                object.reason = "";
            if (message.reason != null && message.hasOwnProperty("reason"))
                object.reason = message.reason;
            return object;
        };

        /**
         * Converts this ErrorStanza to JSON.
         * @function toJSON
         * @memberof server.ErrorStanza
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ErrorStanza.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ErrorStanza;
    })();

    server.Name = (function() {

        /**
         * Properties of a Name.
         * @memberof server
         * @interface IName
         * @property {number|Long|null} [uid] Name uid
         * @property {string|null} [name] Name name
         */

        /**
         * Constructs a new Name.
         * @memberof server
         * @classdesc Represents a Name.
         * @implements IName
         * @constructor
         * @param {server.IName=} [properties] Properties to set
         */
        function Name(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Name uid.
         * @member {number|Long} uid
         * @memberof server.Name
         * @instance
         */
        Name.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Name name.
         * @member {string} name
         * @memberof server.Name
         * @instance
         */
        Name.prototype.name = "";

        /**
         * Creates a new Name instance using the specified properties.
         * @function create
         * @memberof server.Name
         * @static
         * @param {server.IName=} [properties] Properties to set
         * @returns {server.Name} Name instance
         */
        Name.create = function create(properties) {
            return new Name(properties);
        };

        /**
         * Encodes the specified Name message. Does not implicitly {@link server.Name.verify|verify} messages.
         * @function encode
         * @memberof server.Name
         * @static
         * @param {server.IName} message Name message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Name.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 1, wireType 0 =*/8).int64(message.uid);
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.name);
            return writer;
        };

        /**
         * Encodes the specified Name message, length delimited. Does not implicitly {@link server.Name.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Name
         * @static
         * @param {server.IName} message Name message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Name.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Name message from the specified reader or buffer.
         * @function decode
         * @memberof server.Name
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Name} Name
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Name.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Name();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.uid = reader.int64();
                    break;
                case 2:
                    message.name = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Name message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Name
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Name} Name
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Name.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Name message.
         * @function verify
         * @memberof server.Name
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Name.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            if (message.name != null && message.hasOwnProperty("name"))
                if (!$util.isString(message.name))
                    return "name: string expected";
            return null;
        };

        /**
         * Creates a Name message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Name
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Name} Name
         */
        Name.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Name)
                return object;
            var message = new $root.server.Name();
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = false;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber();
            if (object.name != null)
                message.name = String(object.name);
            return message;
        };

        /**
         * Creates a plain object from a Name message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Name
         * @static
         * @param {server.Name} message Name
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Name.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
                object.name = "";
            }
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber() : message.uid;
            if (message.name != null && message.hasOwnProperty("name"))
                object.name = message.name;
            return object;
        };

        /**
         * Converts this Name to JSON.
         * @function toJSON
         * @memberof server.Name
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Name.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Name;
    })();

    server.EndOfQueue = (function() {

        /**
         * Properties of an EndOfQueue.
         * @memberof server
         * @interface IEndOfQueue
         */

        /**
         * Constructs a new EndOfQueue.
         * @memberof server
         * @classdesc Represents an EndOfQueue.
         * @implements IEndOfQueue
         * @constructor
         * @param {server.IEndOfQueue=} [properties] Properties to set
         */
        function EndOfQueue(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Creates a new EndOfQueue instance using the specified properties.
         * @function create
         * @memberof server.EndOfQueue
         * @static
         * @param {server.IEndOfQueue=} [properties] Properties to set
         * @returns {server.EndOfQueue} EndOfQueue instance
         */
        EndOfQueue.create = function create(properties) {
            return new EndOfQueue(properties);
        };

        /**
         * Encodes the specified EndOfQueue message. Does not implicitly {@link server.EndOfQueue.verify|verify} messages.
         * @function encode
         * @memberof server.EndOfQueue
         * @static
         * @param {server.IEndOfQueue} message EndOfQueue message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        EndOfQueue.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            return writer;
        };

        /**
         * Encodes the specified EndOfQueue message, length delimited. Does not implicitly {@link server.EndOfQueue.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.EndOfQueue
         * @static
         * @param {server.IEndOfQueue} message EndOfQueue message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        EndOfQueue.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an EndOfQueue message from the specified reader or buffer.
         * @function decode
         * @memberof server.EndOfQueue
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.EndOfQueue} EndOfQueue
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        EndOfQueue.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.EndOfQueue();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an EndOfQueue message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.EndOfQueue
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.EndOfQueue} EndOfQueue
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        EndOfQueue.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an EndOfQueue message.
         * @function verify
         * @memberof server.EndOfQueue
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        EndOfQueue.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            return null;
        };

        /**
         * Creates an EndOfQueue message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.EndOfQueue
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.EndOfQueue} EndOfQueue
         */
        EndOfQueue.fromObject = function fromObject(object) {
            if (object instanceof $root.server.EndOfQueue)
                return object;
            return new $root.server.EndOfQueue();
        };

        /**
         * Creates a plain object from an EndOfQueue message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.EndOfQueue
         * @static
         * @param {server.EndOfQueue} message EndOfQueue
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        EndOfQueue.toObject = function toObject() {
            return {};
        };

        /**
         * Converts this EndOfQueue to JSON.
         * @function toJSON
         * @memberof server.EndOfQueue
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        EndOfQueue.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return EndOfQueue;
    })();

    server.Iq = (function() {

        /**
         * Properties of an Iq.
         * @memberof server
         * @interface IIq
         * @property {string|null} [id] Iq id
         * @property {server.Iq.Type|null} [type] Iq type
         * @property {server.IUploadMedia|null} [uploadMedia] Iq uploadMedia
         * @property {server.IContactList|null} [contactList] Iq contactList
         * @property {server.IUploadAvatar|null} [uploadAvatar] Iq uploadAvatar
         * @property {server.IAvatar|null} [avatar] Iq avatar
         * @property {server.IAvatars|null} [avatars] Iq avatars
         * @property {server.IClientMode|null} [clientMode] Iq clientMode
         * @property {server.IClientVersion|null} [clientVersion] Iq clientVersion
         * @property {server.IPushRegister|null} [pushRegister] Iq pushRegister
         * @property {server.IWhisperKeys|null} [whisperKeys] Iq whisperKeys
         * @property {server.IPing|null} [ping] Iq ping
         * @property {server.IFeedItem|null} [feedItem] Iq feedItem
         * @property {server.IPrivacyList|null} [privacyList] Iq privacyList
         * @property {server.IPrivacyListResult|null} [privacyListResult] Iq privacyListResult
         * @property {server.IPrivacyLists|null} [privacyLists] Iq privacyLists
         * @property {server.IGroupStanza|null} [groupStanza] Iq groupStanza
         * @property {server.IGroupsStanza|null} [groupsStanza] Iq groupsStanza
         * @property {server.IClientLog|null} [clientLog] Iq clientLog
         * @property {server.IName|null} [name] Iq name
         * @property {server.IErrorStanza|null} [errorStanza] Iq errorStanza
         * @property {server.IProps|null} [props] Iq props
         * @property {server.IInvitesRequest|null} [invitesRequest] Iq invitesRequest
         * @property {server.IInvitesResponse|null} [invitesResponse] Iq invitesResponse
         * @property {server.INotificationPrefs|null} [notificationPrefs] Iq notificationPrefs
         * @property {server.IGroupFeedItem|null} [groupFeedItem] Iq groupFeedItem
         * @property {server.IUploadGroupAvatar|null} [groupAvatar] Iq groupAvatar
         * @property {server.IDeleteAccount|null} [deleteAccount] Iq deleteAccount
         */

        /**
         * Constructs a new Iq.
         * @memberof server
         * @classdesc Represents an Iq.
         * @implements IIq
         * @constructor
         * @param {server.IIq=} [properties] Properties to set
         */
        function Iq(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Iq id.
         * @member {string} id
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.id = "";

        /**
         * Iq type.
         * @member {server.Iq.Type} type
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.type = 0;

        /**
         * Iq uploadMedia.
         * @member {server.IUploadMedia|null|undefined} uploadMedia
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.uploadMedia = null;

        /**
         * Iq contactList.
         * @member {server.IContactList|null|undefined} contactList
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.contactList = null;

        /**
         * Iq uploadAvatar.
         * @member {server.IUploadAvatar|null|undefined} uploadAvatar
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.uploadAvatar = null;

        /**
         * Iq avatar.
         * @member {server.IAvatar|null|undefined} avatar
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.avatar = null;

        /**
         * Iq avatars.
         * @member {server.IAvatars|null|undefined} avatars
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.avatars = null;

        /**
         * Iq clientMode.
         * @member {server.IClientMode|null|undefined} clientMode
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.clientMode = null;

        /**
         * Iq clientVersion.
         * @member {server.IClientVersion|null|undefined} clientVersion
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.clientVersion = null;

        /**
         * Iq pushRegister.
         * @member {server.IPushRegister|null|undefined} pushRegister
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.pushRegister = null;

        /**
         * Iq whisperKeys.
         * @member {server.IWhisperKeys|null|undefined} whisperKeys
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.whisperKeys = null;

        /**
         * Iq ping.
         * @member {server.IPing|null|undefined} ping
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.ping = null;

        /**
         * Iq feedItem.
         * @member {server.IFeedItem|null|undefined} feedItem
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.feedItem = null;

        /**
         * Iq privacyList.
         * @member {server.IPrivacyList|null|undefined} privacyList
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.privacyList = null;

        /**
         * Iq privacyListResult.
         * @member {server.IPrivacyListResult|null|undefined} privacyListResult
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.privacyListResult = null;

        /**
         * Iq privacyLists.
         * @member {server.IPrivacyLists|null|undefined} privacyLists
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.privacyLists = null;

        /**
         * Iq groupStanza.
         * @member {server.IGroupStanza|null|undefined} groupStanza
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.groupStanza = null;

        /**
         * Iq groupsStanza.
         * @member {server.IGroupsStanza|null|undefined} groupsStanza
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.groupsStanza = null;

        /**
         * Iq clientLog.
         * @member {server.IClientLog|null|undefined} clientLog
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.clientLog = null;

        /**
         * Iq name.
         * @member {server.IName|null|undefined} name
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.name = null;

        /**
         * Iq errorStanza.
         * @member {server.IErrorStanza|null|undefined} errorStanza
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.errorStanza = null;

        /**
         * Iq props.
         * @member {server.IProps|null|undefined} props
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.props = null;

        /**
         * Iq invitesRequest.
         * @member {server.IInvitesRequest|null|undefined} invitesRequest
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.invitesRequest = null;

        /**
         * Iq invitesResponse.
         * @member {server.IInvitesResponse|null|undefined} invitesResponse
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.invitesResponse = null;

        /**
         * Iq notificationPrefs.
         * @member {server.INotificationPrefs|null|undefined} notificationPrefs
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.notificationPrefs = null;

        /**
         * Iq groupFeedItem.
         * @member {server.IGroupFeedItem|null|undefined} groupFeedItem
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.groupFeedItem = null;

        /**
         * Iq groupAvatar.
         * @member {server.IUploadGroupAvatar|null|undefined} groupAvatar
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.groupAvatar = null;

        /**
         * Iq deleteAccount.
         * @member {server.IDeleteAccount|null|undefined} deleteAccount
         * @memberof server.Iq
         * @instance
         */
        Iq.prototype.deleteAccount = null;

        // OneOf field names bound to virtual getters and setters
        var $oneOfFields;

        /**
         * Iq payload.
         * @member {"uploadMedia"|"contactList"|"uploadAvatar"|"avatar"|"avatars"|"clientMode"|"clientVersion"|"pushRegister"|"whisperKeys"|"ping"|"feedItem"|"privacyList"|"privacyListResult"|"privacyLists"|"groupStanza"|"groupsStanza"|"clientLog"|"name"|"errorStanza"|"props"|"invitesRequest"|"invitesResponse"|"notificationPrefs"|"groupFeedItem"|"groupAvatar"|"deleteAccount"|undefined} payload
         * @memberof server.Iq
         * @instance
         */
        Object.defineProperty(Iq.prototype, "payload", {
            get: $util.oneOfGetter($oneOfFields = ["uploadMedia", "contactList", "uploadAvatar", "avatar", "avatars", "clientMode", "clientVersion", "pushRegister", "whisperKeys", "ping", "feedItem", "privacyList", "privacyListResult", "privacyLists", "groupStanza", "groupsStanza", "clientLog", "name", "errorStanza", "props", "invitesRequest", "invitesResponse", "notificationPrefs", "groupFeedItem", "groupAvatar", "deleteAccount"]),
            set: $util.oneOfSetter($oneOfFields)
        });

        /**
         * Creates a new Iq instance using the specified properties.
         * @function create
         * @memberof server.Iq
         * @static
         * @param {server.IIq=} [properties] Properties to set
         * @returns {server.Iq} Iq instance
         */
        Iq.create = function create(properties) {
            return new Iq(properties);
        };

        /**
         * Encodes the specified Iq message. Does not implicitly {@link server.Iq.verify|verify} messages.
         * @function encode
         * @memberof server.Iq
         * @static
         * @param {server.IIq} message Iq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Iq.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.type);
            if (message.uploadMedia != null && Object.hasOwnProperty.call(message, "uploadMedia"))
                $root.server.UploadMedia.encode(message.uploadMedia, writer.uint32(/* id 3, wireType 2 =*/26).fork()).ldelim();
            if (message.contactList != null && Object.hasOwnProperty.call(message, "contactList"))
                $root.server.ContactList.encode(message.contactList, writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            if (message.uploadAvatar != null && Object.hasOwnProperty.call(message, "uploadAvatar"))
                $root.server.UploadAvatar.encode(message.uploadAvatar, writer.uint32(/* id 5, wireType 2 =*/42).fork()).ldelim();
            if (message.avatar != null && Object.hasOwnProperty.call(message, "avatar"))
                $root.server.Avatar.encode(message.avatar, writer.uint32(/* id 6, wireType 2 =*/50).fork()).ldelim();
            if (message.avatars != null && Object.hasOwnProperty.call(message, "avatars"))
                $root.server.Avatars.encode(message.avatars, writer.uint32(/* id 7, wireType 2 =*/58).fork()).ldelim();
            if (message.clientMode != null && Object.hasOwnProperty.call(message, "clientMode"))
                $root.server.ClientMode.encode(message.clientMode, writer.uint32(/* id 8, wireType 2 =*/66).fork()).ldelim();
            if (message.clientVersion != null && Object.hasOwnProperty.call(message, "clientVersion"))
                $root.server.ClientVersion.encode(message.clientVersion, writer.uint32(/* id 9, wireType 2 =*/74).fork()).ldelim();
            if (message.pushRegister != null && Object.hasOwnProperty.call(message, "pushRegister"))
                $root.server.PushRegister.encode(message.pushRegister, writer.uint32(/* id 10, wireType 2 =*/82).fork()).ldelim();
            if (message.whisperKeys != null && Object.hasOwnProperty.call(message, "whisperKeys"))
                $root.server.WhisperKeys.encode(message.whisperKeys, writer.uint32(/* id 11, wireType 2 =*/90).fork()).ldelim();
            if (message.ping != null && Object.hasOwnProperty.call(message, "ping"))
                $root.server.Ping.encode(message.ping, writer.uint32(/* id 12, wireType 2 =*/98).fork()).ldelim();
            if (message.feedItem != null && Object.hasOwnProperty.call(message, "feedItem"))
                $root.server.FeedItem.encode(message.feedItem, writer.uint32(/* id 13, wireType 2 =*/106).fork()).ldelim();
            if (message.privacyList != null && Object.hasOwnProperty.call(message, "privacyList"))
                $root.server.PrivacyList.encode(message.privacyList, writer.uint32(/* id 14, wireType 2 =*/114).fork()).ldelim();
            if (message.privacyListResult != null && Object.hasOwnProperty.call(message, "privacyListResult"))
                $root.server.PrivacyListResult.encode(message.privacyListResult, writer.uint32(/* id 15, wireType 2 =*/122).fork()).ldelim();
            if (message.privacyLists != null && Object.hasOwnProperty.call(message, "privacyLists"))
                $root.server.PrivacyLists.encode(message.privacyLists, writer.uint32(/* id 16, wireType 2 =*/130).fork()).ldelim();
            if (message.groupStanza != null && Object.hasOwnProperty.call(message, "groupStanza"))
                $root.server.GroupStanza.encode(message.groupStanza, writer.uint32(/* id 17, wireType 2 =*/138).fork()).ldelim();
            if (message.groupsStanza != null && Object.hasOwnProperty.call(message, "groupsStanza"))
                $root.server.GroupsStanza.encode(message.groupsStanza, writer.uint32(/* id 18, wireType 2 =*/146).fork()).ldelim();
            if (message.clientLog != null && Object.hasOwnProperty.call(message, "clientLog"))
                $root.server.ClientLog.encode(message.clientLog, writer.uint32(/* id 19, wireType 2 =*/154).fork()).ldelim();
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                $root.server.Name.encode(message.name, writer.uint32(/* id 20, wireType 2 =*/162).fork()).ldelim();
            if (message.errorStanza != null && Object.hasOwnProperty.call(message, "errorStanza"))
                $root.server.ErrorStanza.encode(message.errorStanza, writer.uint32(/* id 21, wireType 2 =*/170).fork()).ldelim();
            if (message.props != null && Object.hasOwnProperty.call(message, "props"))
                $root.server.Props.encode(message.props, writer.uint32(/* id 22, wireType 2 =*/178).fork()).ldelim();
            if (message.invitesRequest != null && Object.hasOwnProperty.call(message, "invitesRequest"))
                $root.server.InvitesRequest.encode(message.invitesRequest, writer.uint32(/* id 23, wireType 2 =*/186).fork()).ldelim();
            if (message.invitesResponse != null && Object.hasOwnProperty.call(message, "invitesResponse"))
                $root.server.InvitesResponse.encode(message.invitesResponse, writer.uint32(/* id 24, wireType 2 =*/194).fork()).ldelim();
            if (message.notificationPrefs != null && Object.hasOwnProperty.call(message, "notificationPrefs"))
                $root.server.NotificationPrefs.encode(message.notificationPrefs, writer.uint32(/* id 25, wireType 2 =*/202).fork()).ldelim();
            if (message.groupFeedItem != null && Object.hasOwnProperty.call(message, "groupFeedItem"))
                $root.server.GroupFeedItem.encode(message.groupFeedItem, writer.uint32(/* id 26, wireType 2 =*/210).fork()).ldelim();
            if (message.groupAvatar != null && Object.hasOwnProperty.call(message, "groupAvatar"))
                $root.server.UploadGroupAvatar.encode(message.groupAvatar, writer.uint32(/* id 27, wireType 2 =*/218).fork()).ldelim();
            if (message.deleteAccount != null && Object.hasOwnProperty.call(message, "deleteAccount"))
                $root.server.DeleteAccount.encode(message.deleteAccount, writer.uint32(/* id 28, wireType 2 =*/226).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified Iq message, length delimited. Does not implicitly {@link server.Iq.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Iq
         * @static
         * @param {server.IIq} message Iq message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Iq.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an Iq message from the specified reader or buffer.
         * @function decode
         * @memberof server.Iq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Iq} Iq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Iq.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Iq();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.type = reader.int32();
                    break;
                case 3:
                    message.uploadMedia = $root.server.UploadMedia.decode(reader, reader.uint32());
                    break;
                case 4:
                    message.contactList = $root.server.ContactList.decode(reader, reader.uint32());
                    break;
                case 5:
                    message.uploadAvatar = $root.server.UploadAvatar.decode(reader, reader.uint32());
                    break;
                case 6:
                    message.avatar = $root.server.Avatar.decode(reader, reader.uint32());
                    break;
                case 7:
                    message.avatars = $root.server.Avatars.decode(reader, reader.uint32());
                    break;
                case 8:
                    message.clientMode = $root.server.ClientMode.decode(reader, reader.uint32());
                    break;
                case 9:
                    message.clientVersion = $root.server.ClientVersion.decode(reader, reader.uint32());
                    break;
                case 10:
                    message.pushRegister = $root.server.PushRegister.decode(reader, reader.uint32());
                    break;
                case 11:
                    message.whisperKeys = $root.server.WhisperKeys.decode(reader, reader.uint32());
                    break;
                case 12:
                    message.ping = $root.server.Ping.decode(reader, reader.uint32());
                    break;
                case 13:
                    message.feedItem = $root.server.FeedItem.decode(reader, reader.uint32());
                    break;
                case 14:
                    message.privacyList = $root.server.PrivacyList.decode(reader, reader.uint32());
                    break;
                case 15:
                    message.privacyListResult = $root.server.PrivacyListResult.decode(reader, reader.uint32());
                    break;
                case 16:
                    message.privacyLists = $root.server.PrivacyLists.decode(reader, reader.uint32());
                    break;
                case 17:
                    message.groupStanza = $root.server.GroupStanza.decode(reader, reader.uint32());
                    break;
                case 18:
                    message.groupsStanza = $root.server.GroupsStanza.decode(reader, reader.uint32());
                    break;
                case 19:
                    message.clientLog = $root.server.ClientLog.decode(reader, reader.uint32());
                    break;
                case 20:
                    message.name = $root.server.Name.decode(reader, reader.uint32());
                    break;
                case 21:
                    message.errorStanza = $root.server.ErrorStanza.decode(reader, reader.uint32());
                    break;
                case 22:
                    message.props = $root.server.Props.decode(reader, reader.uint32());
                    break;
                case 23:
                    message.invitesRequest = $root.server.InvitesRequest.decode(reader, reader.uint32());
                    break;
                case 24:
                    message.invitesResponse = $root.server.InvitesResponse.decode(reader, reader.uint32());
                    break;
                case 25:
                    message.notificationPrefs = $root.server.NotificationPrefs.decode(reader, reader.uint32());
                    break;
                case 26:
                    message.groupFeedItem = $root.server.GroupFeedItem.decode(reader, reader.uint32());
                    break;
                case 27:
                    message.groupAvatar = $root.server.UploadGroupAvatar.decode(reader, reader.uint32());
                    break;
                case 28:
                    message.deleteAccount = $root.server.DeleteAccount.decode(reader, reader.uint32());
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an Iq message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Iq
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Iq} Iq
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Iq.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an Iq message.
         * @function verify
         * @memberof server.Iq
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Iq.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            var properties = {};
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.type != null && message.hasOwnProperty("type"))
                switch (message.type) {
                default:
                    return "type: enum value expected";
                case 0:
                case 1:
                case 2:
                case 3:
                    break;
                }
            if (message.uploadMedia != null && message.hasOwnProperty("uploadMedia")) {
                properties.payload = 1;
                {
                    var error = $root.server.UploadMedia.verify(message.uploadMedia);
                    if (error)
                        return "uploadMedia." + error;
                }
            }
            if (message.contactList != null && message.hasOwnProperty("contactList")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.ContactList.verify(message.contactList);
                    if (error)
                        return "contactList." + error;
                }
            }
            if (message.uploadAvatar != null && message.hasOwnProperty("uploadAvatar")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.UploadAvatar.verify(message.uploadAvatar);
                    if (error)
                        return "uploadAvatar." + error;
                }
            }
            if (message.avatar != null && message.hasOwnProperty("avatar")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.Avatar.verify(message.avatar);
                    if (error)
                        return "avatar." + error;
                }
            }
            if (message.avatars != null && message.hasOwnProperty("avatars")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.Avatars.verify(message.avatars);
                    if (error)
                        return "avatars." + error;
                }
            }
            if (message.clientMode != null && message.hasOwnProperty("clientMode")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.ClientMode.verify(message.clientMode);
                    if (error)
                        return "clientMode." + error;
                }
            }
            if (message.clientVersion != null && message.hasOwnProperty("clientVersion")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.ClientVersion.verify(message.clientVersion);
                    if (error)
                        return "clientVersion." + error;
                }
            }
            if (message.pushRegister != null && message.hasOwnProperty("pushRegister")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.PushRegister.verify(message.pushRegister);
                    if (error)
                        return "pushRegister." + error;
                }
            }
            if (message.whisperKeys != null && message.hasOwnProperty("whisperKeys")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.WhisperKeys.verify(message.whisperKeys);
                    if (error)
                        return "whisperKeys." + error;
                }
            }
            if (message.ping != null && message.hasOwnProperty("ping")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.Ping.verify(message.ping);
                    if (error)
                        return "ping." + error;
                }
            }
            if (message.feedItem != null && message.hasOwnProperty("feedItem")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.FeedItem.verify(message.feedItem);
                    if (error)
                        return "feedItem." + error;
                }
            }
            if (message.privacyList != null && message.hasOwnProperty("privacyList")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.PrivacyList.verify(message.privacyList);
                    if (error)
                        return "privacyList." + error;
                }
            }
            if (message.privacyListResult != null && message.hasOwnProperty("privacyListResult")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.PrivacyListResult.verify(message.privacyListResult);
                    if (error)
                        return "privacyListResult." + error;
                }
            }
            if (message.privacyLists != null && message.hasOwnProperty("privacyLists")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.PrivacyLists.verify(message.privacyLists);
                    if (error)
                        return "privacyLists." + error;
                }
            }
            if (message.groupStanza != null && message.hasOwnProperty("groupStanza")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.GroupStanza.verify(message.groupStanza);
                    if (error)
                        return "groupStanza." + error;
                }
            }
            if (message.groupsStanza != null && message.hasOwnProperty("groupsStanza")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.GroupsStanza.verify(message.groupsStanza);
                    if (error)
                        return "groupsStanza." + error;
                }
            }
            if (message.clientLog != null && message.hasOwnProperty("clientLog")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.ClientLog.verify(message.clientLog);
                    if (error)
                        return "clientLog." + error;
                }
            }
            if (message.name != null && message.hasOwnProperty("name")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.Name.verify(message.name);
                    if (error)
                        return "name." + error;
                }
            }
            if (message.errorStanza != null && message.hasOwnProperty("errorStanza")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.ErrorStanza.verify(message.errorStanza);
                    if (error)
                        return "errorStanza." + error;
                }
            }
            if (message.props != null && message.hasOwnProperty("props")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.Props.verify(message.props);
                    if (error)
                        return "props." + error;
                }
            }
            if (message.invitesRequest != null && message.hasOwnProperty("invitesRequest")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.InvitesRequest.verify(message.invitesRequest);
                    if (error)
                        return "invitesRequest." + error;
                }
            }
            if (message.invitesResponse != null && message.hasOwnProperty("invitesResponse")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.InvitesResponse.verify(message.invitesResponse);
                    if (error)
                        return "invitesResponse." + error;
                }
            }
            if (message.notificationPrefs != null && message.hasOwnProperty("notificationPrefs")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.NotificationPrefs.verify(message.notificationPrefs);
                    if (error)
                        return "notificationPrefs." + error;
                }
            }
            if (message.groupFeedItem != null && message.hasOwnProperty("groupFeedItem")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.GroupFeedItem.verify(message.groupFeedItem);
                    if (error)
                        return "groupFeedItem." + error;
                }
            }
            if (message.groupAvatar != null && message.hasOwnProperty("groupAvatar")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.UploadGroupAvatar.verify(message.groupAvatar);
                    if (error)
                        return "groupAvatar." + error;
                }
            }
            if (message.deleteAccount != null && message.hasOwnProperty("deleteAccount")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.DeleteAccount.verify(message.deleteAccount);
                    if (error)
                        return "deleteAccount." + error;
                }
            }
            return null;
        };

        /**
         * Creates an Iq message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Iq
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Iq} Iq
         */
        Iq.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Iq)
                return object;
            var message = new $root.server.Iq();
            if (object.id != null)
                message.id = String(object.id);
            switch (object.type) {
            case "GET":
            case 0:
                message.type = 0;
                break;
            case "SET":
            case 1:
                message.type = 1;
                break;
            case "RESULT":
            case 2:
                message.type = 2;
                break;
            case "ERROR":
            case 3:
                message.type = 3;
                break;
            }
            if (object.uploadMedia != null) {
                if (typeof object.uploadMedia !== "object")
                    throw TypeError(".server.Iq.uploadMedia: object expected");
                message.uploadMedia = $root.server.UploadMedia.fromObject(object.uploadMedia);
            }
            if (object.contactList != null) {
                if (typeof object.contactList !== "object")
                    throw TypeError(".server.Iq.contactList: object expected");
                message.contactList = $root.server.ContactList.fromObject(object.contactList);
            }
            if (object.uploadAvatar != null) {
                if (typeof object.uploadAvatar !== "object")
                    throw TypeError(".server.Iq.uploadAvatar: object expected");
                message.uploadAvatar = $root.server.UploadAvatar.fromObject(object.uploadAvatar);
            }
            if (object.avatar != null) {
                if (typeof object.avatar !== "object")
                    throw TypeError(".server.Iq.avatar: object expected");
                message.avatar = $root.server.Avatar.fromObject(object.avatar);
            }
            if (object.avatars != null) {
                if (typeof object.avatars !== "object")
                    throw TypeError(".server.Iq.avatars: object expected");
                message.avatars = $root.server.Avatars.fromObject(object.avatars);
            }
            if (object.clientMode != null) {
                if (typeof object.clientMode !== "object")
                    throw TypeError(".server.Iq.clientMode: object expected");
                message.clientMode = $root.server.ClientMode.fromObject(object.clientMode);
            }
            if (object.clientVersion != null) {
                if (typeof object.clientVersion !== "object")
                    throw TypeError(".server.Iq.clientVersion: object expected");
                message.clientVersion = $root.server.ClientVersion.fromObject(object.clientVersion);
            }
            if (object.pushRegister != null) {
                if (typeof object.pushRegister !== "object")
                    throw TypeError(".server.Iq.pushRegister: object expected");
                message.pushRegister = $root.server.PushRegister.fromObject(object.pushRegister);
            }
            if (object.whisperKeys != null) {
                if (typeof object.whisperKeys !== "object")
                    throw TypeError(".server.Iq.whisperKeys: object expected");
                message.whisperKeys = $root.server.WhisperKeys.fromObject(object.whisperKeys);
            }
            if (object.ping != null) {
                if (typeof object.ping !== "object")
                    throw TypeError(".server.Iq.ping: object expected");
                message.ping = $root.server.Ping.fromObject(object.ping);
            }
            if (object.feedItem != null) {
                if (typeof object.feedItem !== "object")
                    throw TypeError(".server.Iq.feedItem: object expected");
                message.feedItem = $root.server.FeedItem.fromObject(object.feedItem);
            }
            if (object.privacyList != null) {
                if (typeof object.privacyList !== "object")
                    throw TypeError(".server.Iq.privacyList: object expected");
                message.privacyList = $root.server.PrivacyList.fromObject(object.privacyList);
            }
            if (object.privacyListResult != null) {
                if (typeof object.privacyListResult !== "object")
                    throw TypeError(".server.Iq.privacyListResult: object expected");
                message.privacyListResult = $root.server.PrivacyListResult.fromObject(object.privacyListResult);
            }
            if (object.privacyLists != null) {
                if (typeof object.privacyLists !== "object")
                    throw TypeError(".server.Iq.privacyLists: object expected");
                message.privacyLists = $root.server.PrivacyLists.fromObject(object.privacyLists);
            }
            if (object.groupStanza != null) {
                if (typeof object.groupStanza !== "object")
                    throw TypeError(".server.Iq.groupStanza: object expected");
                message.groupStanza = $root.server.GroupStanza.fromObject(object.groupStanza);
            }
            if (object.groupsStanza != null) {
                if (typeof object.groupsStanza !== "object")
                    throw TypeError(".server.Iq.groupsStanza: object expected");
                message.groupsStanza = $root.server.GroupsStanza.fromObject(object.groupsStanza);
            }
            if (object.clientLog != null) {
                if (typeof object.clientLog !== "object")
                    throw TypeError(".server.Iq.clientLog: object expected");
                message.clientLog = $root.server.ClientLog.fromObject(object.clientLog);
            }
            if (object.name != null) {
                if (typeof object.name !== "object")
                    throw TypeError(".server.Iq.name: object expected");
                message.name = $root.server.Name.fromObject(object.name);
            }
            if (object.errorStanza != null) {
                if (typeof object.errorStanza !== "object")
                    throw TypeError(".server.Iq.errorStanza: object expected");
                message.errorStanza = $root.server.ErrorStanza.fromObject(object.errorStanza);
            }
            if (object.props != null) {
                if (typeof object.props !== "object")
                    throw TypeError(".server.Iq.props: object expected");
                message.props = $root.server.Props.fromObject(object.props);
            }
            if (object.invitesRequest != null) {
                if (typeof object.invitesRequest !== "object")
                    throw TypeError(".server.Iq.invitesRequest: object expected");
                message.invitesRequest = $root.server.InvitesRequest.fromObject(object.invitesRequest);
            }
            if (object.invitesResponse != null) {
                if (typeof object.invitesResponse !== "object")
                    throw TypeError(".server.Iq.invitesResponse: object expected");
                message.invitesResponse = $root.server.InvitesResponse.fromObject(object.invitesResponse);
            }
            if (object.notificationPrefs != null) {
                if (typeof object.notificationPrefs !== "object")
                    throw TypeError(".server.Iq.notificationPrefs: object expected");
                message.notificationPrefs = $root.server.NotificationPrefs.fromObject(object.notificationPrefs);
            }
            if (object.groupFeedItem != null) {
                if (typeof object.groupFeedItem !== "object")
                    throw TypeError(".server.Iq.groupFeedItem: object expected");
                message.groupFeedItem = $root.server.GroupFeedItem.fromObject(object.groupFeedItem);
            }
            if (object.groupAvatar != null) {
                if (typeof object.groupAvatar !== "object")
                    throw TypeError(".server.Iq.groupAvatar: object expected");
                message.groupAvatar = $root.server.UploadGroupAvatar.fromObject(object.groupAvatar);
            }
            if (object.deleteAccount != null) {
                if (typeof object.deleteAccount !== "object")
                    throw TypeError(".server.Iq.deleteAccount: object expected");
                message.deleteAccount = $root.server.DeleteAccount.fromObject(object.deleteAccount);
            }
            return message;
        };

        /**
         * Creates a plain object from an Iq message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Iq
         * @static
         * @param {server.Iq} message Iq
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Iq.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.type = options.enums === String ? "GET" : 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.server.Iq.Type[message.type] : message.type;
            if (message.uploadMedia != null && message.hasOwnProperty("uploadMedia")) {
                object.uploadMedia = $root.server.UploadMedia.toObject(message.uploadMedia, options);
                if (options.oneofs)
                    object.payload = "uploadMedia";
            }
            if (message.contactList != null && message.hasOwnProperty("contactList")) {
                object.contactList = $root.server.ContactList.toObject(message.contactList, options);
                if (options.oneofs)
                    object.payload = "contactList";
            }
            if (message.uploadAvatar != null && message.hasOwnProperty("uploadAvatar")) {
                object.uploadAvatar = $root.server.UploadAvatar.toObject(message.uploadAvatar, options);
                if (options.oneofs)
                    object.payload = "uploadAvatar";
            }
            if (message.avatar != null && message.hasOwnProperty("avatar")) {
                object.avatar = $root.server.Avatar.toObject(message.avatar, options);
                if (options.oneofs)
                    object.payload = "avatar";
            }
            if (message.avatars != null && message.hasOwnProperty("avatars")) {
                object.avatars = $root.server.Avatars.toObject(message.avatars, options);
                if (options.oneofs)
                    object.payload = "avatars";
            }
            if (message.clientMode != null && message.hasOwnProperty("clientMode")) {
                object.clientMode = $root.server.ClientMode.toObject(message.clientMode, options);
                if (options.oneofs)
                    object.payload = "clientMode";
            }
            if (message.clientVersion != null && message.hasOwnProperty("clientVersion")) {
                object.clientVersion = $root.server.ClientVersion.toObject(message.clientVersion, options);
                if (options.oneofs)
                    object.payload = "clientVersion";
            }
            if (message.pushRegister != null && message.hasOwnProperty("pushRegister")) {
                object.pushRegister = $root.server.PushRegister.toObject(message.pushRegister, options);
                if (options.oneofs)
                    object.payload = "pushRegister";
            }
            if (message.whisperKeys != null && message.hasOwnProperty("whisperKeys")) {
                object.whisperKeys = $root.server.WhisperKeys.toObject(message.whisperKeys, options);
                if (options.oneofs)
                    object.payload = "whisperKeys";
            }
            if (message.ping != null && message.hasOwnProperty("ping")) {
                object.ping = $root.server.Ping.toObject(message.ping, options);
                if (options.oneofs)
                    object.payload = "ping";
            }
            if (message.feedItem != null && message.hasOwnProperty("feedItem")) {
                object.feedItem = $root.server.FeedItem.toObject(message.feedItem, options);
                if (options.oneofs)
                    object.payload = "feedItem";
            }
            if (message.privacyList != null && message.hasOwnProperty("privacyList")) {
                object.privacyList = $root.server.PrivacyList.toObject(message.privacyList, options);
                if (options.oneofs)
                    object.payload = "privacyList";
            }
            if (message.privacyListResult != null && message.hasOwnProperty("privacyListResult")) {
                object.privacyListResult = $root.server.PrivacyListResult.toObject(message.privacyListResult, options);
                if (options.oneofs)
                    object.payload = "privacyListResult";
            }
            if (message.privacyLists != null && message.hasOwnProperty("privacyLists")) {
                object.privacyLists = $root.server.PrivacyLists.toObject(message.privacyLists, options);
                if (options.oneofs)
                    object.payload = "privacyLists";
            }
            if (message.groupStanza != null && message.hasOwnProperty("groupStanza")) {
                object.groupStanza = $root.server.GroupStanza.toObject(message.groupStanza, options);
                if (options.oneofs)
                    object.payload = "groupStanza";
            }
            if (message.groupsStanza != null && message.hasOwnProperty("groupsStanza")) {
                object.groupsStanza = $root.server.GroupsStanza.toObject(message.groupsStanza, options);
                if (options.oneofs)
                    object.payload = "groupsStanza";
            }
            if (message.clientLog != null && message.hasOwnProperty("clientLog")) {
                object.clientLog = $root.server.ClientLog.toObject(message.clientLog, options);
                if (options.oneofs)
                    object.payload = "clientLog";
            }
            if (message.name != null && message.hasOwnProperty("name")) {
                object.name = $root.server.Name.toObject(message.name, options);
                if (options.oneofs)
                    object.payload = "name";
            }
            if (message.errorStanza != null && message.hasOwnProperty("errorStanza")) {
                object.errorStanza = $root.server.ErrorStanza.toObject(message.errorStanza, options);
                if (options.oneofs)
                    object.payload = "errorStanza";
            }
            if (message.props != null && message.hasOwnProperty("props")) {
                object.props = $root.server.Props.toObject(message.props, options);
                if (options.oneofs)
                    object.payload = "props";
            }
            if (message.invitesRequest != null && message.hasOwnProperty("invitesRequest")) {
                object.invitesRequest = $root.server.InvitesRequest.toObject(message.invitesRequest, options);
                if (options.oneofs)
                    object.payload = "invitesRequest";
            }
            if (message.invitesResponse != null && message.hasOwnProperty("invitesResponse")) {
                object.invitesResponse = $root.server.InvitesResponse.toObject(message.invitesResponse, options);
                if (options.oneofs)
                    object.payload = "invitesResponse";
            }
            if (message.notificationPrefs != null && message.hasOwnProperty("notificationPrefs")) {
                object.notificationPrefs = $root.server.NotificationPrefs.toObject(message.notificationPrefs, options);
                if (options.oneofs)
                    object.payload = "notificationPrefs";
            }
            if (message.groupFeedItem != null && message.hasOwnProperty("groupFeedItem")) {
                object.groupFeedItem = $root.server.GroupFeedItem.toObject(message.groupFeedItem, options);
                if (options.oneofs)
                    object.payload = "groupFeedItem";
            }
            if (message.groupAvatar != null && message.hasOwnProperty("groupAvatar")) {
                object.groupAvatar = $root.server.UploadGroupAvatar.toObject(message.groupAvatar, options);
                if (options.oneofs)
                    object.payload = "groupAvatar";
            }
            if (message.deleteAccount != null && message.hasOwnProperty("deleteAccount")) {
                object.deleteAccount = $root.server.DeleteAccount.toObject(message.deleteAccount, options);
                if (options.oneofs)
                    object.payload = "deleteAccount";
            }
            return object;
        };

        /**
         * Converts this Iq to JSON.
         * @function toJSON
         * @memberof server.Iq
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Iq.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Type enum.
         * @name server.Iq.Type
         * @enum {number}
         * @property {number} GET=0 GET value
         * @property {number} SET=1 SET value
         * @property {number} RESULT=2 RESULT value
         * @property {number} ERROR=3 ERROR value
         */
        Iq.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "GET"] = 0;
            values[valuesById[1] = "SET"] = 1;
            values[valuesById[2] = "RESULT"] = 2;
            values[valuesById[3] = "ERROR"] = 3;
            return values;
        })();

        return Iq;
    })();

    server.Msg = (function() {

        /**
         * Properties of a Msg.
         * @memberof server
         * @interface IMsg
         * @property {string|null} [id] Msg id
         * @property {server.Msg.Type|null} [type] Msg type
         * @property {number|Long|null} [toUid] Msg toUid
         * @property {number|Long|null} [fromUid] Msg fromUid
         * @property {server.IContactList|null} [contactList] Msg contactList
         * @property {server.IAvatar|null} [avatar] Msg avatar
         * @property {server.IWhisperKeys|null} [whisperKeys] Msg whisperKeys
         * @property {server.ISeenReceipt|null} [seenReceipt] Msg seenReceipt
         * @property {server.IDeliveryReceipt|null} [deliveryReceipt] Msg deliveryReceipt
         * @property {server.IChatStanza|null} [chatStanza] Msg chatStanza
         * @property {server.IFeedItem|null} [feedItem] Msg feedItem
         * @property {server.IFeedItems|null} [feedItems] Msg feedItems
         * @property {server.IContactHash|null} [contactHash] Msg contactHash
         * @property {server.IGroupStanza|null} [groupStanza] Msg groupStanza
         * @property {server.IGroupChat|null} [groupChat] Msg groupChat
         * @property {server.IName|null} [name] Msg name
         * @property {server.IErrorStanza|null} [errorStanza] Msg errorStanza
         * @property {server.IGroupChatRetract|null} [groupchatRetract] Msg groupchatRetract
         * @property {server.IChatRetract|null} [chatRetract] Msg chatRetract
         * @property {server.IGroupFeedItem|null} [groupFeedItem] Msg groupFeedItem
         * @property {server.IRerequest|null} [rerequest] Msg rerequest
         * @property {server.ISilentChatStanza|null} [silentChatStanza] Msg silentChatStanza
         * @property {server.IGroupFeedItems|null} [groupFeedItems] Msg groupFeedItems
         * @property {server.IEndOfQueue|null} [endOfQueue] Msg endOfQueue
         * @property {number|null} [retryCount] Msg retryCount
         * @property {number|null} [rerequestCount] Msg rerequestCount
         */

        /**
         * Constructs a new Msg.
         * @memberof server
         * @classdesc Represents a Msg.
         * @implements IMsg
         * @constructor
         * @param {server.IMsg=} [properties] Properties to set
         */
        function Msg(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Msg id.
         * @member {string} id
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.id = "";

        /**
         * Msg type.
         * @member {server.Msg.Type} type
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.type = 0;

        /**
         * Msg toUid.
         * @member {number|Long} toUid
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.toUid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Msg fromUid.
         * @member {number|Long} fromUid
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.fromUid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Msg contactList.
         * @member {server.IContactList|null|undefined} contactList
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.contactList = null;

        /**
         * Msg avatar.
         * @member {server.IAvatar|null|undefined} avatar
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.avatar = null;

        /**
         * Msg whisperKeys.
         * @member {server.IWhisperKeys|null|undefined} whisperKeys
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.whisperKeys = null;

        /**
         * Msg seenReceipt.
         * @member {server.ISeenReceipt|null|undefined} seenReceipt
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.seenReceipt = null;

        /**
         * Msg deliveryReceipt.
         * @member {server.IDeliveryReceipt|null|undefined} deliveryReceipt
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.deliveryReceipt = null;

        /**
         * Msg chatStanza.
         * @member {server.IChatStanza|null|undefined} chatStanza
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.chatStanza = null;

        /**
         * Msg feedItem.
         * @member {server.IFeedItem|null|undefined} feedItem
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.feedItem = null;

        /**
         * Msg feedItems.
         * @member {server.IFeedItems|null|undefined} feedItems
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.feedItems = null;

        /**
         * Msg contactHash.
         * @member {server.IContactHash|null|undefined} contactHash
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.contactHash = null;

        /**
         * Msg groupStanza.
         * @member {server.IGroupStanza|null|undefined} groupStanza
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.groupStanza = null;

        /**
         * Msg groupChat.
         * @member {server.IGroupChat|null|undefined} groupChat
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.groupChat = null;

        /**
         * Msg name.
         * @member {server.IName|null|undefined} name
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.name = null;

        /**
         * Msg errorStanza.
         * @member {server.IErrorStanza|null|undefined} errorStanza
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.errorStanza = null;

        /**
         * Msg groupchatRetract.
         * @member {server.IGroupChatRetract|null|undefined} groupchatRetract
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.groupchatRetract = null;

        /**
         * Msg chatRetract.
         * @member {server.IChatRetract|null|undefined} chatRetract
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.chatRetract = null;

        /**
         * Msg groupFeedItem.
         * @member {server.IGroupFeedItem|null|undefined} groupFeedItem
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.groupFeedItem = null;

        /**
         * Msg rerequest.
         * @member {server.IRerequest|null|undefined} rerequest
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.rerequest = null;

        /**
         * Msg silentChatStanza.
         * @member {server.ISilentChatStanza|null|undefined} silentChatStanza
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.silentChatStanza = null;

        /**
         * Msg groupFeedItems.
         * @member {server.IGroupFeedItems|null|undefined} groupFeedItems
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.groupFeedItems = null;

        /**
         * Msg endOfQueue.
         * @member {server.IEndOfQueue|null|undefined} endOfQueue
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.endOfQueue = null;

        /**
         * Msg retryCount.
         * @member {number} retryCount
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.retryCount = 0;

        /**
         * Msg rerequestCount.
         * @member {number} rerequestCount
         * @memberof server.Msg
         * @instance
         */
        Msg.prototype.rerequestCount = 0;

        // OneOf field names bound to virtual getters and setters
        var $oneOfFields;

        /**
         * Msg payload.
         * @member {"contactList"|"avatar"|"whisperKeys"|"seenReceipt"|"deliveryReceipt"|"chatStanza"|"feedItem"|"feedItems"|"contactHash"|"groupStanza"|"groupChat"|"name"|"errorStanza"|"groupchatRetract"|"chatRetract"|"groupFeedItem"|"rerequest"|"silentChatStanza"|"groupFeedItems"|"endOfQueue"|undefined} payload
         * @memberof server.Msg
         * @instance
         */
        Object.defineProperty(Msg.prototype, "payload", {
            get: $util.oneOfGetter($oneOfFields = ["contactList", "avatar", "whisperKeys", "seenReceipt", "deliveryReceipt", "chatStanza", "feedItem", "feedItems", "contactHash", "groupStanza", "groupChat", "name", "errorStanza", "groupchatRetract", "chatRetract", "groupFeedItem", "rerequest", "silentChatStanza", "groupFeedItems", "endOfQueue"]),
            set: $util.oneOfSetter($oneOfFields)
        });

        /**
         * Creates a new Msg instance using the specified properties.
         * @function create
         * @memberof server.Msg
         * @static
         * @param {server.IMsg=} [properties] Properties to set
         * @returns {server.Msg} Msg instance
         */
        Msg.create = function create(properties) {
            return new Msg(properties);
        };

        /**
         * Encodes the specified Msg message. Does not implicitly {@link server.Msg.verify|verify} messages.
         * @function encode
         * @memberof server.Msg
         * @static
         * @param {server.IMsg} message Msg message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Msg.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.type);
            if (message.toUid != null && Object.hasOwnProperty.call(message, "toUid"))
                writer.uint32(/* id 3, wireType 0 =*/24).int64(message.toUid);
            if (message.fromUid != null && Object.hasOwnProperty.call(message, "fromUid"))
                writer.uint32(/* id 4, wireType 0 =*/32).int64(message.fromUid);
            if (message.contactList != null && Object.hasOwnProperty.call(message, "contactList"))
                $root.server.ContactList.encode(message.contactList, writer.uint32(/* id 5, wireType 2 =*/42).fork()).ldelim();
            if (message.avatar != null && Object.hasOwnProperty.call(message, "avatar"))
                $root.server.Avatar.encode(message.avatar, writer.uint32(/* id 6, wireType 2 =*/50).fork()).ldelim();
            if (message.whisperKeys != null && Object.hasOwnProperty.call(message, "whisperKeys"))
                $root.server.WhisperKeys.encode(message.whisperKeys, writer.uint32(/* id 7, wireType 2 =*/58).fork()).ldelim();
            if (message.seenReceipt != null && Object.hasOwnProperty.call(message, "seenReceipt"))
                $root.server.SeenReceipt.encode(message.seenReceipt, writer.uint32(/* id 8, wireType 2 =*/66).fork()).ldelim();
            if (message.deliveryReceipt != null && Object.hasOwnProperty.call(message, "deliveryReceipt"))
                $root.server.DeliveryReceipt.encode(message.deliveryReceipt, writer.uint32(/* id 9, wireType 2 =*/74).fork()).ldelim();
            if (message.chatStanza != null && Object.hasOwnProperty.call(message, "chatStanza"))
                $root.server.ChatStanza.encode(message.chatStanza, writer.uint32(/* id 10, wireType 2 =*/82).fork()).ldelim();
            if (message.feedItem != null && Object.hasOwnProperty.call(message, "feedItem"))
                $root.server.FeedItem.encode(message.feedItem, writer.uint32(/* id 11, wireType 2 =*/90).fork()).ldelim();
            if (message.feedItems != null && Object.hasOwnProperty.call(message, "feedItems"))
                $root.server.FeedItems.encode(message.feedItems, writer.uint32(/* id 12, wireType 2 =*/98).fork()).ldelim();
            if (message.contactHash != null && Object.hasOwnProperty.call(message, "contactHash"))
                $root.server.ContactHash.encode(message.contactHash, writer.uint32(/* id 13, wireType 2 =*/106).fork()).ldelim();
            if (message.groupStanza != null && Object.hasOwnProperty.call(message, "groupStanza"))
                $root.server.GroupStanza.encode(message.groupStanza, writer.uint32(/* id 14, wireType 2 =*/114).fork()).ldelim();
            if (message.groupChat != null && Object.hasOwnProperty.call(message, "groupChat"))
                $root.server.GroupChat.encode(message.groupChat, writer.uint32(/* id 15, wireType 2 =*/122).fork()).ldelim();
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                $root.server.Name.encode(message.name, writer.uint32(/* id 16, wireType 2 =*/130).fork()).ldelim();
            if (message.errorStanza != null && Object.hasOwnProperty.call(message, "errorStanza"))
                $root.server.ErrorStanza.encode(message.errorStanza, writer.uint32(/* id 17, wireType 2 =*/138).fork()).ldelim();
            if (message.groupchatRetract != null && Object.hasOwnProperty.call(message, "groupchatRetract"))
                $root.server.GroupChatRetract.encode(message.groupchatRetract, writer.uint32(/* id 18, wireType 2 =*/146).fork()).ldelim();
            if (message.chatRetract != null && Object.hasOwnProperty.call(message, "chatRetract"))
                $root.server.ChatRetract.encode(message.chatRetract, writer.uint32(/* id 19, wireType 2 =*/154).fork()).ldelim();
            if (message.groupFeedItem != null && Object.hasOwnProperty.call(message, "groupFeedItem"))
                $root.server.GroupFeedItem.encode(message.groupFeedItem, writer.uint32(/* id 20, wireType 2 =*/162).fork()).ldelim();
            if (message.retryCount != null && Object.hasOwnProperty.call(message, "retryCount"))
                writer.uint32(/* id 21, wireType 0 =*/168).int32(message.retryCount);
            if (message.rerequest != null && Object.hasOwnProperty.call(message, "rerequest"))
                $root.server.Rerequest.encode(message.rerequest, writer.uint32(/* id 22, wireType 2 =*/178).fork()).ldelim();
            if (message.silentChatStanza != null && Object.hasOwnProperty.call(message, "silentChatStanza"))
                $root.server.SilentChatStanza.encode(message.silentChatStanza, writer.uint32(/* id 23, wireType 2 =*/186).fork()).ldelim();
            if (message.groupFeedItems != null && Object.hasOwnProperty.call(message, "groupFeedItems"))
                $root.server.GroupFeedItems.encode(message.groupFeedItems, writer.uint32(/* id 24, wireType 2 =*/194).fork()).ldelim();
            if (message.rerequestCount != null && Object.hasOwnProperty.call(message, "rerequestCount"))
                writer.uint32(/* id 25, wireType 0 =*/200).int32(message.rerequestCount);
            if (message.endOfQueue != null && Object.hasOwnProperty.call(message, "endOfQueue"))
                $root.server.EndOfQueue.encode(message.endOfQueue, writer.uint32(/* id 26, wireType 2 =*/210).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified Msg message, length delimited. Does not implicitly {@link server.Msg.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Msg
         * @static
         * @param {server.IMsg} message Msg message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Msg.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Msg message from the specified reader or buffer.
         * @function decode
         * @memberof server.Msg
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Msg} Msg
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Msg.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Msg();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.type = reader.int32();
                    break;
                case 3:
                    message.toUid = reader.int64();
                    break;
                case 4:
                    message.fromUid = reader.int64();
                    break;
                case 5:
                    message.contactList = $root.server.ContactList.decode(reader, reader.uint32());
                    break;
                case 6:
                    message.avatar = $root.server.Avatar.decode(reader, reader.uint32());
                    break;
                case 7:
                    message.whisperKeys = $root.server.WhisperKeys.decode(reader, reader.uint32());
                    break;
                case 8:
                    message.seenReceipt = $root.server.SeenReceipt.decode(reader, reader.uint32());
                    break;
                case 9:
                    message.deliveryReceipt = $root.server.DeliveryReceipt.decode(reader, reader.uint32());
                    break;
                case 10:
                    message.chatStanza = $root.server.ChatStanza.decode(reader, reader.uint32());
                    break;
                case 11:
                    message.feedItem = $root.server.FeedItem.decode(reader, reader.uint32());
                    break;
                case 12:
                    message.feedItems = $root.server.FeedItems.decode(reader, reader.uint32());
                    break;
                case 13:
                    message.contactHash = $root.server.ContactHash.decode(reader, reader.uint32());
                    break;
                case 14:
                    message.groupStanza = $root.server.GroupStanza.decode(reader, reader.uint32());
                    break;
                case 15:
                    message.groupChat = $root.server.GroupChat.decode(reader, reader.uint32());
                    break;
                case 16:
                    message.name = $root.server.Name.decode(reader, reader.uint32());
                    break;
                case 17:
                    message.errorStanza = $root.server.ErrorStanza.decode(reader, reader.uint32());
                    break;
                case 18:
                    message.groupchatRetract = $root.server.GroupChatRetract.decode(reader, reader.uint32());
                    break;
                case 19:
                    message.chatRetract = $root.server.ChatRetract.decode(reader, reader.uint32());
                    break;
                case 20:
                    message.groupFeedItem = $root.server.GroupFeedItem.decode(reader, reader.uint32());
                    break;
                case 22:
                    message.rerequest = $root.server.Rerequest.decode(reader, reader.uint32());
                    break;
                case 23:
                    message.silentChatStanza = $root.server.SilentChatStanza.decode(reader, reader.uint32());
                    break;
                case 24:
                    message.groupFeedItems = $root.server.GroupFeedItems.decode(reader, reader.uint32());
                    break;
                case 26:
                    message.endOfQueue = $root.server.EndOfQueue.decode(reader, reader.uint32());
                    break;
                case 21:
                    message.retryCount = reader.int32();
                    break;
                case 25:
                    message.rerequestCount = reader.int32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Msg message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Msg
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Msg} Msg
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Msg.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Msg message.
         * @function verify
         * @memberof server.Msg
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Msg.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            var properties = {};
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.type != null && message.hasOwnProperty("type"))
                switch (message.type) {
                default:
                    return "type: enum value expected";
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    break;
                }
            if (message.toUid != null && message.hasOwnProperty("toUid"))
                if (!$util.isInteger(message.toUid) && !(message.toUid && $util.isInteger(message.toUid.low) && $util.isInteger(message.toUid.high)))
                    return "toUid: integer|Long expected";
            if (message.fromUid != null && message.hasOwnProperty("fromUid"))
                if (!$util.isInteger(message.fromUid) && !(message.fromUid && $util.isInteger(message.fromUid.low) && $util.isInteger(message.fromUid.high)))
                    return "fromUid: integer|Long expected";
            if (message.contactList != null && message.hasOwnProperty("contactList")) {
                properties.payload = 1;
                {
                    var error = $root.server.ContactList.verify(message.contactList);
                    if (error)
                        return "contactList." + error;
                }
            }
            if (message.avatar != null && message.hasOwnProperty("avatar")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.Avatar.verify(message.avatar);
                    if (error)
                        return "avatar." + error;
                }
            }
            if (message.whisperKeys != null && message.hasOwnProperty("whisperKeys")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.WhisperKeys.verify(message.whisperKeys);
                    if (error)
                        return "whisperKeys." + error;
                }
            }
            if (message.seenReceipt != null && message.hasOwnProperty("seenReceipt")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.SeenReceipt.verify(message.seenReceipt);
                    if (error)
                        return "seenReceipt." + error;
                }
            }
            if (message.deliveryReceipt != null && message.hasOwnProperty("deliveryReceipt")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.DeliveryReceipt.verify(message.deliveryReceipt);
                    if (error)
                        return "deliveryReceipt." + error;
                }
            }
            if (message.chatStanza != null && message.hasOwnProperty("chatStanza")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.ChatStanza.verify(message.chatStanza);
                    if (error)
                        return "chatStanza." + error;
                }
            }
            if (message.feedItem != null && message.hasOwnProperty("feedItem")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.FeedItem.verify(message.feedItem);
                    if (error)
                        return "feedItem." + error;
                }
            }
            if (message.feedItems != null && message.hasOwnProperty("feedItems")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.FeedItems.verify(message.feedItems);
                    if (error)
                        return "feedItems." + error;
                }
            }
            if (message.contactHash != null && message.hasOwnProperty("contactHash")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.ContactHash.verify(message.contactHash);
                    if (error)
                        return "contactHash." + error;
                }
            }
            if (message.groupStanza != null && message.hasOwnProperty("groupStanza")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.GroupStanza.verify(message.groupStanza);
                    if (error)
                        return "groupStanza." + error;
                }
            }
            if (message.groupChat != null && message.hasOwnProperty("groupChat")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.GroupChat.verify(message.groupChat);
                    if (error)
                        return "groupChat." + error;
                }
            }
            if (message.name != null && message.hasOwnProperty("name")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.Name.verify(message.name);
                    if (error)
                        return "name." + error;
                }
            }
            if (message.errorStanza != null && message.hasOwnProperty("errorStanza")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.ErrorStanza.verify(message.errorStanza);
                    if (error)
                        return "errorStanza." + error;
                }
            }
            if (message.groupchatRetract != null && message.hasOwnProperty("groupchatRetract")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.GroupChatRetract.verify(message.groupchatRetract);
                    if (error)
                        return "groupchatRetract." + error;
                }
            }
            if (message.chatRetract != null && message.hasOwnProperty("chatRetract")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.ChatRetract.verify(message.chatRetract);
                    if (error)
                        return "chatRetract." + error;
                }
            }
            if (message.groupFeedItem != null && message.hasOwnProperty("groupFeedItem")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.GroupFeedItem.verify(message.groupFeedItem);
                    if (error)
                        return "groupFeedItem." + error;
                }
            }
            if (message.rerequest != null && message.hasOwnProperty("rerequest")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.Rerequest.verify(message.rerequest);
                    if (error)
                        return "rerequest." + error;
                }
            }
            if (message.silentChatStanza != null && message.hasOwnProperty("silentChatStanza")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.SilentChatStanza.verify(message.silentChatStanza);
                    if (error)
                        return "silentChatStanza." + error;
                }
            }
            if (message.groupFeedItems != null && message.hasOwnProperty("groupFeedItems")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.GroupFeedItems.verify(message.groupFeedItems);
                    if (error)
                        return "groupFeedItems." + error;
                }
            }
            if (message.endOfQueue != null && message.hasOwnProperty("endOfQueue")) {
                if (properties.payload === 1)
                    return "payload: multiple values";
                properties.payload = 1;
                {
                    var error = $root.server.EndOfQueue.verify(message.endOfQueue);
                    if (error)
                        return "endOfQueue." + error;
                }
            }
            if (message.retryCount != null && message.hasOwnProperty("retryCount"))
                if (!$util.isInteger(message.retryCount))
                    return "retryCount: integer expected";
            if (message.rerequestCount != null && message.hasOwnProperty("rerequestCount"))
                if (!$util.isInteger(message.rerequestCount))
                    return "rerequestCount: integer expected";
            return null;
        };

        /**
         * Creates a Msg message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Msg
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Msg} Msg
         */
        Msg.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Msg)
                return object;
            var message = new $root.server.Msg();
            if (object.id != null)
                message.id = String(object.id);
            switch (object.type) {
            case "NORMAL":
            case 0:
                message.type = 0;
                break;
            case "ERROR":
            case 1:
                message.type = 1;
                break;
            case "GROUPCHAT":
            case 2:
                message.type = 2;
                break;
            case "HEADLINE":
            case 3:
                message.type = 3;
                break;
            case "CHAT":
            case 4:
                message.type = 4;
                break;
            }
            if (object.toUid != null)
                if ($util.Long)
                    (message.toUid = $util.Long.fromValue(object.toUid)).unsigned = false;
                else if (typeof object.toUid === "string")
                    message.toUid = parseInt(object.toUid, 10);
                else if (typeof object.toUid === "number")
                    message.toUid = object.toUid;
                else if (typeof object.toUid === "object")
                    message.toUid = new $util.LongBits(object.toUid.low >>> 0, object.toUid.high >>> 0).toNumber();
            if (object.fromUid != null)
                if ($util.Long)
                    (message.fromUid = $util.Long.fromValue(object.fromUid)).unsigned = false;
                else if (typeof object.fromUid === "string")
                    message.fromUid = parseInt(object.fromUid, 10);
                else if (typeof object.fromUid === "number")
                    message.fromUid = object.fromUid;
                else if (typeof object.fromUid === "object")
                    message.fromUid = new $util.LongBits(object.fromUid.low >>> 0, object.fromUid.high >>> 0).toNumber();
            if (object.contactList != null) {
                if (typeof object.contactList !== "object")
                    throw TypeError(".server.Msg.contactList: object expected");
                message.contactList = $root.server.ContactList.fromObject(object.contactList);
            }
            if (object.avatar != null) {
                if (typeof object.avatar !== "object")
                    throw TypeError(".server.Msg.avatar: object expected");
                message.avatar = $root.server.Avatar.fromObject(object.avatar);
            }
            if (object.whisperKeys != null) {
                if (typeof object.whisperKeys !== "object")
                    throw TypeError(".server.Msg.whisperKeys: object expected");
                message.whisperKeys = $root.server.WhisperKeys.fromObject(object.whisperKeys);
            }
            if (object.seenReceipt != null) {
                if (typeof object.seenReceipt !== "object")
                    throw TypeError(".server.Msg.seenReceipt: object expected");
                message.seenReceipt = $root.server.SeenReceipt.fromObject(object.seenReceipt);
            }
            if (object.deliveryReceipt != null) {
                if (typeof object.deliveryReceipt !== "object")
                    throw TypeError(".server.Msg.deliveryReceipt: object expected");
                message.deliveryReceipt = $root.server.DeliveryReceipt.fromObject(object.deliveryReceipt);
            }
            if (object.chatStanza != null) {
                if (typeof object.chatStanza !== "object")
                    throw TypeError(".server.Msg.chatStanza: object expected");
                message.chatStanza = $root.server.ChatStanza.fromObject(object.chatStanza);
            }
            if (object.feedItem != null) {
                if (typeof object.feedItem !== "object")
                    throw TypeError(".server.Msg.feedItem: object expected");
                message.feedItem = $root.server.FeedItem.fromObject(object.feedItem);
            }
            if (object.feedItems != null) {
                if (typeof object.feedItems !== "object")
                    throw TypeError(".server.Msg.feedItems: object expected");
                message.feedItems = $root.server.FeedItems.fromObject(object.feedItems);
            }
            if (object.contactHash != null) {
                if (typeof object.contactHash !== "object")
                    throw TypeError(".server.Msg.contactHash: object expected");
                message.contactHash = $root.server.ContactHash.fromObject(object.contactHash);
            }
            if (object.groupStanza != null) {
                if (typeof object.groupStanza !== "object")
                    throw TypeError(".server.Msg.groupStanza: object expected");
                message.groupStanza = $root.server.GroupStanza.fromObject(object.groupStanza);
            }
            if (object.groupChat != null) {
                if (typeof object.groupChat !== "object")
                    throw TypeError(".server.Msg.groupChat: object expected");
                message.groupChat = $root.server.GroupChat.fromObject(object.groupChat);
            }
            if (object.name != null) {
                if (typeof object.name !== "object")
                    throw TypeError(".server.Msg.name: object expected");
                message.name = $root.server.Name.fromObject(object.name);
            }
            if (object.errorStanza != null) {
                if (typeof object.errorStanza !== "object")
                    throw TypeError(".server.Msg.errorStanza: object expected");
                message.errorStanza = $root.server.ErrorStanza.fromObject(object.errorStanza);
            }
            if (object.groupchatRetract != null) {
                if (typeof object.groupchatRetract !== "object")
                    throw TypeError(".server.Msg.groupchatRetract: object expected");
                message.groupchatRetract = $root.server.GroupChatRetract.fromObject(object.groupchatRetract);
            }
            if (object.chatRetract != null) {
                if (typeof object.chatRetract !== "object")
                    throw TypeError(".server.Msg.chatRetract: object expected");
                message.chatRetract = $root.server.ChatRetract.fromObject(object.chatRetract);
            }
            if (object.groupFeedItem != null) {
                if (typeof object.groupFeedItem !== "object")
                    throw TypeError(".server.Msg.groupFeedItem: object expected");
                message.groupFeedItem = $root.server.GroupFeedItem.fromObject(object.groupFeedItem);
            }
            if (object.rerequest != null) {
                if (typeof object.rerequest !== "object")
                    throw TypeError(".server.Msg.rerequest: object expected");
                message.rerequest = $root.server.Rerequest.fromObject(object.rerequest);
            }
            if (object.silentChatStanza != null) {
                if (typeof object.silentChatStanza !== "object")
                    throw TypeError(".server.Msg.silentChatStanza: object expected");
                message.silentChatStanza = $root.server.SilentChatStanza.fromObject(object.silentChatStanza);
            }
            if (object.groupFeedItems != null) {
                if (typeof object.groupFeedItems !== "object")
                    throw TypeError(".server.Msg.groupFeedItems: object expected");
                message.groupFeedItems = $root.server.GroupFeedItems.fromObject(object.groupFeedItems);
            }
            if (object.endOfQueue != null) {
                if (typeof object.endOfQueue !== "object")
                    throw TypeError(".server.Msg.endOfQueue: object expected");
                message.endOfQueue = $root.server.EndOfQueue.fromObject(object.endOfQueue);
            }
            if (object.retryCount != null)
                message.retryCount = object.retryCount | 0;
            if (object.rerequestCount != null)
                message.rerequestCount = object.rerequestCount | 0;
            return message;
        };

        /**
         * Creates a plain object from a Msg message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Msg
         * @static
         * @param {server.Msg} message Msg
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Msg.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.type = options.enums === String ? "NORMAL" : 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.toUid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.toUid = options.longs === String ? "0" : 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.fromUid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.fromUid = options.longs === String ? "0" : 0;
                object.retryCount = 0;
                object.rerequestCount = 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.server.Msg.Type[message.type] : message.type;
            if (message.toUid != null && message.hasOwnProperty("toUid"))
                if (typeof message.toUid === "number")
                    object.toUid = options.longs === String ? String(message.toUid) : message.toUid;
                else
                    object.toUid = options.longs === String ? $util.Long.prototype.toString.call(message.toUid) : options.longs === Number ? new $util.LongBits(message.toUid.low >>> 0, message.toUid.high >>> 0).toNumber() : message.toUid;
            if (message.fromUid != null && message.hasOwnProperty("fromUid"))
                if (typeof message.fromUid === "number")
                    object.fromUid = options.longs === String ? String(message.fromUid) : message.fromUid;
                else
                    object.fromUid = options.longs === String ? $util.Long.prototype.toString.call(message.fromUid) : options.longs === Number ? new $util.LongBits(message.fromUid.low >>> 0, message.fromUid.high >>> 0).toNumber() : message.fromUid;
            if (message.contactList != null && message.hasOwnProperty("contactList")) {
                object.contactList = $root.server.ContactList.toObject(message.contactList, options);
                if (options.oneofs)
                    object.payload = "contactList";
            }
            if (message.avatar != null && message.hasOwnProperty("avatar")) {
                object.avatar = $root.server.Avatar.toObject(message.avatar, options);
                if (options.oneofs)
                    object.payload = "avatar";
            }
            if (message.whisperKeys != null && message.hasOwnProperty("whisperKeys")) {
                object.whisperKeys = $root.server.WhisperKeys.toObject(message.whisperKeys, options);
                if (options.oneofs)
                    object.payload = "whisperKeys";
            }
            if (message.seenReceipt != null && message.hasOwnProperty("seenReceipt")) {
                object.seenReceipt = $root.server.SeenReceipt.toObject(message.seenReceipt, options);
                if (options.oneofs)
                    object.payload = "seenReceipt";
            }
            if (message.deliveryReceipt != null && message.hasOwnProperty("deliveryReceipt")) {
                object.deliveryReceipt = $root.server.DeliveryReceipt.toObject(message.deliveryReceipt, options);
                if (options.oneofs)
                    object.payload = "deliveryReceipt";
            }
            if (message.chatStanza != null && message.hasOwnProperty("chatStanza")) {
                object.chatStanza = $root.server.ChatStanza.toObject(message.chatStanza, options);
                if (options.oneofs)
                    object.payload = "chatStanza";
            }
            if (message.feedItem != null && message.hasOwnProperty("feedItem")) {
                object.feedItem = $root.server.FeedItem.toObject(message.feedItem, options);
                if (options.oneofs)
                    object.payload = "feedItem";
            }
            if (message.feedItems != null && message.hasOwnProperty("feedItems")) {
                object.feedItems = $root.server.FeedItems.toObject(message.feedItems, options);
                if (options.oneofs)
                    object.payload = "feedItems";
            }
            if (message.contactHash != null && message.hasOwnProperty("contactHash")) {
                object.contactHash = $root.server.ContactHash.toObject(message.contactHash, options);
                if (options.oneofs)
                    object.payload = "contactHash";
            }
            if (message.groupStanza != null && message.hasOwnProperty("groupStanza")) {
                object.groupStanza = $root.server.GroupStanza.toObject(message.groupStanza, options);
                if (options.oneofs)
                    object.payload = "groupStanza";
            }
            if (message.groupChat != null && message.hasOwnProperty("groupChat")) {
                object.groupChat = $root.server.GroupChat.toObject(message.groupChat, options);
                if (options.oneofs)
                    object.payload = "groupChat";
            }
            if (message.name != null && message.hasOwnProperty("name")) {
                object.name = $root.server.Name.toObject(message.name, options);
                if (options.oneofs)
                    object.payload = "name";
            }
            if (message.errorStanza != null && message.hasOwnProperty("errorStanza")) {
                object.errorStanza = $root.server.ErrorStanza.toObject(message.errorStanza, options);
                if (options.oneofs)
                    object.payload = "errorStanza";
            }
            if (message.groupchatRetract != null && message.hasOwnProperty("groupchatRetract")) {
                object.groupchatRetract = $root.server.GroupChatRetract.toObject(message.groupchatRetract, options);
                if (options.oneofs)
                    object.payload = "groupchatRetract";
            }
            if (message.chatRetract != null && message.hasOwnProperty("chatRetract")) {
                object.chatRetract = $root.server.ChatRetract.toObject(message.chatRetract, options);
                if (options.oneofs)
                    object.payload = "chatRetract";
            }
            if (message.groupFeedItem != null && message.hasOwnProperty("groupFeedItem")) {
                object.groupFeedItem = $root.server.GroupFeedItem.toObject(message.groupFeedItem, options);
                if (options.oneofs)
                    object.payload = "groupFeedItem";
            }
            if (message.retryCount != null && message.hasOwnProperty("retryCount"))
                object.retryCount = message.retryCount;
            if (message.rerequest != null && message.hasOwnProperty("rerequest")) {
                object.rerequest = $root.server.Rerequest.toObject(message.rerequest, options);
                if (options.oneofs)
                    object.payload = "rerequest";
            }
            if (message.silentChatStanza != null && message.hasOwnProperty("silentChatStanza")) {
                object.silentChatStanza = $root.server.SilentChatStanza.toObject(message.silentChatStanza, options);
                if (options.oneofs)
                    object.payload = "silentChatStanza";
            }
            if (message.groupFeedItems != null && message.hasOwnProperty("groupFeedItems")) {
                object.groupFeedItems = $root.server.GroupFeedItems.toObject(message.groupFeedItems, options);
                if (options.oneofs)
                    object.payload = "groupFeedItems";
            }
            if (message.rerequestCount != null && message.hasOwnProperty("rerequestCount"))
                object.rerequestCount = message.rerequestCount;
            if (message.endOfQueue != null && message.hasOwnProperty("endOfQueue")) {
                object.endOfQueue = $root.server.EndOfQueue.toObject(message.endOfQueue, options);
                if (options.oneofs)
                    object.payload = "endOfQueue";
            }
            return object;
        };

        /**
         * Converts this Msg to JSON.
         * @function toJSON
         * @memberof server.Msg
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Msg.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Type enum.
         * @name server.Msg.Type
         * @enum {number}
         * @property {number} NORMAL=0 NORMAL value
         * @property {number} ERROR=1 ERROR value
         * @property {number} GROUPCHAT=2 GROUPCHAT value
         * @property {number} HEADLINE=3 HEADLINE value
         * @property {number} CHAT=4 CHAT value
         */
        Msg.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "NORMAL"] = 0;
            values[valuesById[1] = "ERROR"] = 1;
            values[valuesById[2] = "GROUPCHAT"] = 2;
            values[valuesById[3] = "HEADLINE"] = 3;
            values[valuesById[4] = "CHAT"] = 4;
            return values;
        })();

        return Msg;
    })();

    server.Presence = (function() {

        /**
         * Properties of a Presence.
         * @memberof server
         * @interface IPresence
         * @property {string|null} [id] Presence id
         * @property {server.Presence.Type|null} [type] Presence type
         * @property {number|Long|null} [uid] Presence uid
         * @property {number|Long|null} [lastSeen] Presence lastSeen
         * @property {number|Long|null} [toUid] Presence toUid
         * @property {number|Long|null} [fromUid] Presence fromUid
         */

        /**
         * Constructs a new Presence.
         * @memberof server
         * @classdesc Represents a Presence.
         * @implements IPresence
         * @constructor
         * @param {server.IPresence=} [properties] Properties to set
         */
        function Presence(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Presence id.
         * @member {string} id
         * @memberof server.Presence
         * @instance
         */
        Presence.prototype.id = "";

        /**
         * Presence type.
         * @member {server.Presence.Type} type
         * @memberof server.Presence
         * @instance
         */
        Presence.prototype.type = 0;

        /**
         * Presence uid.
         * @member {number|Long} uid
         * @memberof server.Presence
         * @instance
         */
        Presence.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Presence lastSeen.
         * @member {number|Long} lastSeen
         * @memberof server.Presence
         * @instance
         */
        Presence.prototype.lastSeen = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Presence toUid.
         * @member {number|Long} toUid
         * @memberof server.Presence
         * @instance
         */
        Presence.prototype.toUid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Presence fromUid.
         * @member {number|Long} fromUid
         * @memberof server.Presence
         * @instance
         */
        Presence.prototype.fromUid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Creates a new Presence instance using the specified properties.
         * @function create
         * @memberof server.Presence
         * @static
         * @param {server.IPresence=} [properties] Properties to set
         * @returns {server.Presence} Presence instance
         */
        Presence.create = function create(properties) {
            return new Presence(properties);
        };

        /**
         * Encodes the specified Presence message. Does not implicitly {@link server.Presence.verify|verify} messages.
         * @function encode
         * @memberof server.Presence
         * @static
         * @param {server.IPresence} message Presence message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Presence.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.type);
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 3, wireType 0 =*/24).int64(message.uid);
            if (message.lastSeen != null && Object.hasOwnProperty.call(message, "lastSeen"))
                writer.uint32(/* id 4, wireType 0 =*/32).int64(message.lastSeen);
            if (message.toUid != null && Object.hasOwnProperty.call(message, "toUid"))
                writer.uint32(/* id 5, wireType 0 =*/40).int64(message.toUid);
            if (message.fromUid != null && Object.hasOwnProperty.call(message, "fromUid"))
                writer.uint32(/* id 6, wireType 0 =*/48).int64(message.fromUid);
            return writer;
        };

        /**
         * Encodes the specified Presence message, length delimited. Does not implicitly {@link server.Presence.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Presence
         * @static
         * @param {server.IPresence} message Presence message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Presence.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Presence message from the specified reader or buffer.
         * @function decode
         * @memberof server.Presence
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Presence} Presence
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Presence.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Presence();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.type = reader.int32();
                    break;
                case 3:
                    message.uid = reader.int64();
                    break;
                case 4:
                    message.lastSeen = reader.int64();
                    break;
                case 5:
                    message.toUid = reader.int64();
                    break;
                case 6:
                    message.fromUid = reader.int64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Presence message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Presence
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Presence} Presence
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Presence.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Presence message.
         * @function verify
         * @memberof server.Presence
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Presence.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.type != null && message.hasOwnProperty("type"))
                switch (message.type) {
                default:
                    return "type: enum value expected";
                case 0:
                case 1:
                case 2:
                case 3:
                    break;
                }
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            if (message.lastSeen != null && message.hasOwnProperty("lastSeen"))
                if (!$util.isInteger(message.lastSeen) && !(message.lastSeen && $util.isInteger(message.lastSeen.low) && $util.isInteger(message.lastSeen.high)))
                    return "lastSeen: integer|Long expected";
            if (message.toUid != null && message.hasOwnProperty("toUid"))
                if (!$util.isInteger(message.toUid) && !(message.toUid && $util.isInteger(message.toUid.low) && $util.isInteger(message.toUid.high)))
                    return "toUid: integer|Long expected";
            if (message.fromUid != null && message.hasOwnProperty("fromUid"))
                if (!$util.isInteger(message.fromUid) && !(message.fromUid && $util.isInteger(message.fromUid.low) && $util.isInteger(message.fromUid.high)))
                    return "fromUid: integer|Long expected";
            return null;
        };

        /**
         * Creates a Presence message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Presence
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Presence} Presence
         */
        Presence.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Presence)
                return object;
            var message = new $root.server.Presence();
            if (object.id != null)
                message.id = String(object.id);
            switch (object.type) {
            case "AVAILABLE":
            case 0:
                message.type = 0;
                break;
            case "AWAY":
            case 1:
                message.type = 1;
                break;
            case "SUBSCRIBE":
            case 2:
                message.type = 2;
                break;
            case "UNSUBSCRIBE":
            case 3:
                message.type = 3;
                break;
            }
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = false;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber();
            if (object.lastSeen != null)
                if ($util.Long)
                    (message.lastSeen = $util.Long.fromValue(object.lastSeen)).unsigned = false;
                else if (typeof object.lastSeen === "string")
                    message.lastSeen = parseInt(object.lastSeen, 10);
                else if (typeof object.lastSeen === "number")
                    message.lastSeen = object.lastSeen;
                else if (typeof object.lastSeen === "object")
                    message.lastSeen = new $util.LongBits(object.lastSeen.low >>> 0, object.lastSeen.high >>> 0).toNumber();
            if (object.toUid != null)
                if ($util.Long)
                    (message.toUid = $util.Long.fromValue(object.toUid)).unsigned = false;
                else if (typeof object.toUid === "string")
                    message.toUid = parseInt(object.toUid, 10);
                else if (typeof object.toUid === "number")
                    message.toUid = object.toUid;
                else if (typeof object.toUid === "object")
                    message.toUid = new $util.LongBits(object.toUid.low >>> 0, object.toUid.high >>> 0).toNumber();
            if (object.fromUid != null)
                if ($util.Long)
                    (message.fromUid = $util.Long.fromValue(object.fromUid)).unsigned = false;
                else if (typeof object.fromUid === "string")
                    message.fromUid = parseInt(object.fromUid, 10);
                else if (typeof object.fromUid === "number")
                    message.fromUid = object.fromUid;
                else if (typeof object.fromUid === "object")
                    message.fromUid = new $util.LongBits(object.fromUid.low >>> 0, object.fromUid.high >>> 0).toNumber();
            return message;
        };

        /**
         * Creates a plain object from a Presence message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Presence
         * @static
         * @param {server.Presence} message Presence
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Presence.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.type = options.enums === String ? "AVAILABLE" : 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.lastSeen = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.lastSeen = options.longs === String ? "0" : 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.toUid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.toUid = options.longs === String ? "0" : 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.fromUid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.fromUid = options.longs === String ? "0" : 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.server.Presence.Type[message.type] : message.type;
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber() : message.uid;
            if (message.lastSeen != null && message.hasOwnProperty("lastSeen"))
                if (typeof message.lastSeen === "number")
                    object.lastSeen = options.longs === String ? String(message.lastSeen) : message.lastSeen;
                else
                    object.lastSeen = options.longs === String ? $util.Long.prototype.toString.call(message.lastSeen) : options.longs === Number ? new $util.LongBits(message.lastSeen.low >>> 0, message.lastSeen.high >>> 0).toNumber() : message.lastSeen;
            if (message.toUid != null && message.hasOwnProperty("toUid"))
                if (typeof message.toUid === "number")
                    object.toUid = options.longs === String ? String(message.toUid) : message.toUid;
                else
                    object.toUid = options.longs === String ? $util.Long.prototype.toString.call(message.toUid) : options.longs === Number ? new $util.LongBits(message.toUid.low >>> 0, message.toUid.high >>> 0).toNumber() : message.toUid;
            if (message.fromUid != null && message.hasOwnProperty("fromUid"))
                if (typeof message.fromUid === "number")
                    object.fromUid = options.longs === String ? String(message.fromUid) : message.fromUid;
                else
                    object.fromUid = options.longs === String ? $util.Long.prototype.toString.call(message.fromUid) : options.longs === Number ? new $util.LongBits(message.fromUid.low >>> 0, message.fromUid.high >>> 0).toNumber() : message.fromUid;
            return object;
        };

        /**
         * Converts this Presence to JSON.
         * @function toJSON
         * @memberof server.Presence
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Presence.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Type enum.
         * @name server.Presence.Type
         * @enum {number}
         * @property {number} AVAILABLE=0 AVAILABLE value
         * @property {number} AWAY=1 AWAY value
         * @property {number} SUBSCRIBE=2 SUBSCRIBE value
         * @property {number} UNSUBSCRIBE=3 UNSUBSCRIBE value
         */
        Presence.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "AVAILABLE"] = 0;
            values[valuesById[1] = "AWAY"] = 1;
            values[valuesById[2] = "SUBSCRIBE"] = 2;
            values[valuesById[3] = "UNSUBSCRIBE"] = 3;
            return values;
        })();

        return Presence;
    })();

    server.ChatState = (function() {

        /**
         * Properties of a ChatState.
         * @memberof server
         * @interface IChatState
         * @property {server.ChatState.Type|null} [type] ChatState type
         * @property {string|null} [threadId] ChatState threadId
         * @property {server.ChatState.ThreadType|null} [threadType] ChatState threadType
         * @property {number|Long|null} [fromUid] ChatState fromUid
         */

        /**
         * Constructs a new ChatState.
         * @memberof server
         * @classdesc Represents a ChatState.
         * @implements IChatState
         * @constructor
         * @param {server.IChatState=} [properties] Properties to set
         */
        function ChatState(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ChatState type.
         * @member {server.ChatState.Type} type
         * @memberof server.ChatState
         * @instance
         */
        ChatState.prototype.type = 0;

        /**
         * ChatState threadId.
         * @member {string} threadId
         * @memberof server.ChatState
         * @instance
         */
        ChatState.prototype.threadId = "";

        /**
         * ChatState threadType.
         * @member {server.ChatState.ThreadType} threadType
         * @memberof server.ChatState
         * @instance
         */
        ChatState.prototype.threadType = 0;

        /**
         * ChatState fromUid.
         * @member {number|Long} fromUid
         * @memberof server.ChatState
         * @instance
         */
        ChatState.prototype.fromUid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Creates a new ChatState instance using the specified properties.
         * @function create
         * @memberof server.ChatState
         * @static
         * @param {server.IChatState=} [properties] Properties to set
         * @returns {server.ChatState} ChatState instance
         */
        ChatState.create = function create(properties) {
            return new ChatState(properties);
        };

        /**
         * Encodes the specified ChatState message. Does not implicitly {@link server.ChatState.verify|verify} messages.
         * @function encode
         * @memberof server.ChatState
         * @static
         * @param {server.IChatState} message ChatState message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ChatState.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.type);
            if (message.threadId != null && Object.hasOwnProperty.call(message, "threadId"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.threadId);
            if (message.threadType != null && Object.hasOwnProperty.call(message, "threadType"))
                writer.uint32(/* id 3, wireType 0 =*/24).int32(message.threadType);
            if (message.fromUid != null && Object.hasOwnProperty.call(message, "fromUid"))
                writer.uint32(/* id 4, wireType 0 =*/32).int64(message.fromUid);
            return writer;
        };

        /**
         * Encodes the specified ChatState message, length delimited. Does not implicitly {@link server.ChatState.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.ChatState
         * @static
         * @param {server.IChatState} message ChatState message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ChatState.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ChatState message from the specified reader or buffer.
         * @function decode
         * @memberof server.ChatState
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.ChatState} ChatState
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ChatState.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.ChatState();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.type = reader.int32();
                    break;
                case 2:
                    message.threadId = reader.string();
                    break;
                case 3:
                    message.threadType = reader.int32();
                    break;
                case 4:
                    message.fromUid = reader.int64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ChatState message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.ChatState
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.ChatState} ChatState
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ChatState.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ChatState message.
         * @function verify
         * @memberof server.ChatState
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ChatState.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.type != null && message.hasOwnProperty("type"))
                switch (message.type) {
                default:
                    return "type: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.threadId != null && message.hasOwnProperty("threadId"))
                if (!$util.isString(message.threadId))
                    return "threadId: string expected";
            if (message.threadType != null && message.hasOwnProperty("threadType"))
                switch (message.threadType) {
                default:
                    return "threadType: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.fromUid != null && message.hasOwnProperty("fromUid"))
                if (!$util.isInteger(message.fromUid) && !(message.fromUid && $util.isInteger(message.fromUid.low) && $util.isInteger(message.fromUid.high)))
                    return "fromUid: integer|Long expected";
            return null;
        };

        /**
         * Creates a ChatState message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.ChatState
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.ChatState} ChatState
         */
        ChatState.fromObject = function fromObject(object) {
            if (object instanceof $root.server.ChatState)
                return object;
            var message = new $root.server.ChatState();
            switch (object.type) {
            case "AVAILABLE":
            case 0:
                message.type = 0;
                break;
            case "TYPING":
            case 1:
                message.type = 1;
                break;
            }
            if (object.threadId != null)
                message.threadId = String(object.threadId);
            switch (object.threadType) {
            case "CHAT":
            case 0:
                message.threadType = 0;
                break;
            case "GROUP_CHAT":
            case 1:
                message.threadType = 1;
                break;
            }
            if (object.fromUid != null)
                if ($util.Long)
                    (message.fromUid = $util.Long.fromValue(object.fromUid)).unsigned = false;
                else if (typeof object.fromUid === "string")
                    message.fromUid = parseInt(object.fromUid, 10);
                else if (typeof object.fromUid === "number")
                    message.fromUid = object.fromUid;
                else if (typeof object.fromUid === "object")
                    message.fromUid = new $util.LongBits(object.fromUid.low >>> 0, object.fromUid.high >>> 0).toNumber();
            return message;
        };

        /**
         * Creates a plain object from a ChatState message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.ChatState
         * @static
         * @param {server.ChatState} message ChatState
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ChatState.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.type = options.enums === String ? "AVAILABLE" : 0;
                object.threadId = "";
                object.threadType = options.enums === String ? "CHAT" : 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.fromUid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.fromUid = options.longs === String ? "0" : 0;
            }
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.server.ChatState.Type[message.type] : message.type;
            if (message.threadId != null && message.hasOwnProperty("threadId"))
                object.threadId = message.threadId;
            if (message.threadType != null && message.hasOwnProperty("threadType"))
                object.threadType = options.enums === String ? $root.server.ChatState.ThreadType[message.threadType] : message.threadType;
            if (message.fromUid != null && message.hasOwnProperty("fromUid"))
                if (typeof message.fromUid === "number")
                    object.fromUid = options.longs === String ? String(message.fromUid) : message.fromUid;
                else
                    object.fromUid = options.longs === String ? $util.Long.prototype.toString.call(message.fromUid) : options.longs === Number ? new $util.LongBits(message.fromUid.low >>> 0, message.fromUid.high >>> 0).toNumber() : message.fromUid;
            return object;
        };

        /**
         * Converts this ChatState to JSON.
         * @function toJSON
         * @memberof server.ChatState
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ChatState.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Type enum.
         * @name server.ChatState.Type
         * @enum {number}
         * @property {number} AVAILABLE=0 AVAILABLE value
         * @property {number} TYPING=1 TYPING value
         */
        ChatState.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "AVAILABLE"] = 0;
            values[valuesById[1] = "TYPING"] = 1;
            return values;
        })();

        /**
         * ThreadType enum.
         * @name server.ChatState.ThreadType
         * @enum {number}
         * @property {number} CHAT=0 CHAT value
         * @property {number} GROUP_CHAT=1 GROUP_CHAT value
         */
        ChatState.ThreadType = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "CHAT"] = 0;
            values[valuesById[1] = "GROUP_CHAT"] = 1;
            return values;
        })();

        return ChatState;
    })();

    server.Ack = (function() {

        /**
         * Properties of an Ack.
         * @memberof server
         * @interface IAck
         * @property {string|null} [id] Ack id
         * @property {number|Long|null} [timestamp] Ack timestamp
         */

        /**
         * Constructs a new Ack.
         * @memberof server
         * @classdesc Represents an Ack.
         * @implements IAck
         * @constructor
         * @param {server.IAck=} [properties] Properties to set
         */
        function Ack(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Ack id.
         * @member {string} id
         * @memberof server.Ack
         * @instance
         */
        Ack.prototype.id = "";

        /**
         * Ack timestamp.
         * @member {number|Long} timestamp
         * @memberof server.Ack
         * @instance
         */
        Ack.prototype.timestamp = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Creates a new Ack instance using the specified properties.
         * @function create
         * @memberof server.Ack
         * @static
         * @param {server.IAck=} [properties] Properties to set
         * @returns {server.Ack} Ack instance
         */
        Ack.create = function create(properties) {
            return new Ack(properties);
        };

        /**
         * Encodes the specified Ack message. Does not implicitly {@link server.Ack.verify|verify} messages.
         * @function encode
         * @memberof server.Ack
         * @static
         * @param {server.IAck} message Ack message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Ack.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.timestamp != null && Object.hasOwnProperty.call(message, "timestamp"))
                writer.uint32(/* id 2, wireType 0 =*/16).int64(message.timestamp);
            return writer;
        };

        /**
         * Encodes the specified Ack message, length delimited. Does not implicitly {@link server.Ack.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Ack
         * @static
         * @param {server.IAck} message Ack message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Ack.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an Ack message from the specified reader or buffer.
         * @function decode
         * @memberof server.Ack
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Ack} Ack
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Ack.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Ack();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.timestamp = reader.int64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an Ack message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Ack
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Ack} Ack
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Ack.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an Ack message.
         * @function verify
         * @memberof server.Ack
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Ack.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (!$util.isInteger(message.timestamp) && !(message.timestamp && $util.isInteger(message.timestamp.low) && $util.isInteger(message.timestamp.high)))
                    return "timestamp: integer|Long expected";
            return null;
        };

        /**
         * Creates an Ack message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Ack
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Ack} Ack
         */
        Ack.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Ack)
                return object;
            var message = new $root.server.Ack();
            if (object.id != null)
                message.id = String(object.id);
            if (object.timestamp != null)
                if ($util.Long)
                    (message.timestamp = $util.Long.fromValue(object.timestamp)).unsigned = false;
                else if (typeof object.timestamp === "string")
                    message.timestamp = parseInt(object.timestamp, 10);
                else if (typeof object.timestamp === "number")
                    message.timestamp = object.timestamp;
                else if (typeof object.timestamp === "object")
                    message.timestamp = new $util.LongBits(object.timestamp.low >>> 0, object.timestamp.high >>> 0).toNumber();
            return message;
        };

        /**
         * Creates a plain object from an Ack message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Ack
         * @static
         * @param {server.Ack} message Ack
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Ack.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.timestamp = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.timestamp = options.longs === String ? "0" : 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (typeof message.timestamp === "number")
                    object.timestamp = options.longs === String ? String(message.timestamp) : message.timestamp;
                else
                    object.timestamp = options.longs === String ? $util.Long.prototype.toString.call(message.timestamp) : options.longs === Number ? new $util.LongBits(message.timestamp.low >>> 0, message.timestamp.high >>> 0).toNumber() : message.timestamp;
            return object;
        };

        /**
         * Converts this Ack to JSON.
         * @function toJSON
         * @memberof server.Ack
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Ack.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Ack;
    })();

    server.HaError = (function() {

        /**
         * Properties of a HaError.
         * @memberof server
         * @interface IHaError
         * @property {string|null} [reason] HaError reason
         */

        /**
         * Constructs a new HaError.
         * @memberof server
         * @classdesc Represents a HaError.
         * @implements IHaError
         * @constructor
         * @param {server.IHaError=} [properties] Properties to set
         */
        function HaError(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * HaError reason.
         * @member {string} reason
         * @memberof server.HaError
         * @instance
         */
        HaError.prototype.reason = "";

        /**
         * Creates a new HaError instance using the specified properties.
         * @function create
         * @memberof server.HaError
         * @static
         * @param {server.IHaError=} [properties] Properties to set
         * @returns {server.HaError} HaError instance
         */
        HaError.create = function create(properties) {
            return new HaError(properties);
        };

        /**
         * Encodes the specified HaError message. Does not implicitly {@link server.HaError.verify|verify} messages.
         * @function encode
         * @memberof server.HaError
         * @static
         * @param {server.IHaError} message HaError message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        HaError.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.reason != null && Object.hasOwnProperty.call(message, "reason"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.reason);
            return writer;
        };

        /**
         * Encodes the specified HaError message, length delimited. Does not implicitly {@link server.HaError.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.HaError
         * @static
         * @param {server.IHaError} message HaError message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        HaError.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a HaError message from the specified reader or buffer.
         * @function decode
         * @memberof server.HaError
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.HaError} HaError
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        HaError.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.HaError();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.reason = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a HaError message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.HaError
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.HaError} HaError
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        HaError.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a HaError message.
         * @function verify
         * @memberof server.HaError
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        HaError.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.reason != null && message.hasOwnProperty("reason"))
                if (!$util.isString(message.reason))
                    return "reason: string expected";
            return null;
        };

        /**
         * Creates a HaError message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.HaError
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.HaError} HaError
         */
        HaError.fromObject = function fromObject(object) {
            if (object instanceof $root.server.HaError)
                return object;
            var message = new $root.server.HaError();
            if (object.reason != null)
                message.reason = String(object.reason);
            return message;
        };

        /**
         * Creates a plain object from a HaError message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.HaError
         * @static
         * @param {server.HaError} message HaError
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        HaError.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults)
                object.reason = "";
            if (message.reason != null && message.hasOwnProperty("reason"))
                object.reason = message.reason;
            return object;
        };

        /**
         * Converts this HaError to JSON.
         * @function toJSON
         * @memberof server.HaError
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        HaError.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return HaError;
    })();

    server.Packet = (function() {

        /**
         * Properties of a Packet.
         * @memberof server
         * @interface IPacket
         * @property {server.IMsg|null} [msg] Packet msg
         * @property {server.IIq|null} [iq] Packet iq
         * @property {server.IAck|null} [ack] Packet ack
         * @property {server.IPresence|null} [presence] Packet presence
         * @property {server.IHaError|null} [haError] Packet haError
         * @property {server.IChatState|null} [chatState] Packet chatState
         */

        /**
         * Constructs a new Packet.
         * @memberof server
         * @classdesc Represents a Packet.
         * @implements IPacket
         * @constructor
         * @param {server.IPacket=} [properties] Properties to set
         */
        function Packet(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Packet msg.
         * @member {server.IMsg|null|undefined} msg
         * @memberof server.Packet
         * @instance
         */
        Packet.prototype.msg = null;

        /**
         * Packet iq.
         * @member {server.IIq|null|undefined} iq
         * @memberof server.Packet
         * @instance
         */
        Packet.prototype.iq = null;

        /**
         * Packet ack.
         * @member {server.IAck|null|undefined} ack
         * @memberof server.Packet
         * @instance
         */
        Packet.prototype.ack = null;

        /**
         * Packet presence.
         * @member {server.IPresence|null|undefined} presence
         * @memberof server.Packet
         * @instance
         */
        Packet.prototype.presence = null;

        /**
         * Packet haError.
         * @member {server.IHaError|null|undefined} haError
         * @memberof server.Packet
         * @instance
         */
        Packet.prototype.haError = null;

        /**
         * Packet chatState.
         * @member {server.IChatState|null|undefined} chatState
         * @memberof server.Packet
         * @instance
         */
        Packet.prototype.chatState = null;

        // OneOf field names bound to virtual getters and setters
        var $oneOfFields;

        /**
         * Packet stanza.
         * @member {"msg"|"iq"|"ack"|"presence"|"haError"|"chatState"|undefined} stanza
         * @memberof server.Packet
         * @instance
         */
        Object.defineProperty(Packet.prototype, "stanza", {
            get: $util.oneOfGetter($oneOfFields = ["msg", "iq", "ack", "presence", "haError", "chatState"]),
            set: $util.oneOfSetter($oneOfFields)
        });

        /**
         * Creates a new Packet instance using the specified properties.
         * @function create
         * @memberof server.Packet
         * @static
         * @param {server.IPacket=} [properties] Properties to set
         * @returns {server.Packet} Packet instance
         */
        Packet.create = function create(properties) {
            return new Packet(properties);
        };

        /**
         * Encodes the specified Packet message. Does not implicitly {@link server.Packet.verify|verify} messages.
         * @function encode
         * @memberof server.Packet
         * @static
         * @param {server.IPacket} message Packet message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Packet.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.msg != null && Object.hasOwnProperty.call(message, "msg"))
                $root.server.Msg.encode(message.msg, writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
            if (message.iq != null && Object.hasOwnProperty.call(message, "iq"))
                $root.server.Iq.encode(message.iq, writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            if (message.ack != null && Object.hasOwnProperty.call(message, "ack"))
                $root.server.Ack.encode(message.ack, writer.uint32(/* id 3, wireType 2 =*/26).fork()).ldelim();
            if (message.presence != null && Object.hasOwnProperty.call(message, "presence"))
                $root.server.Presence.encode(message.presence, writer.uint32(/* id 4, wireType 2 =*/34).fork()).ldelim();
            if (message.haError != null && Object.hasOwnProperty.call(message, "haError"))
                $root.server.HaError.encode(message.haError, writer.uint32(/* id 5, wireType 2 =*/42).fork()).ldelim();
            if (message.chatState != null && Object.hasOwnProperty.call(message, "chatState"))
                $root.server.ChatState.encode(message.chatState, writer.uint32(/* id 6, wireType 2 =*/50).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified Packet message, length delimited. Does not implicitly {@link server.Packet.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Packet
         * @static
         * @param {server.IPacket} message Packet message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Packet.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Packet message from the specified reader or buffer.
         * @function decode
         * @memberof server.Packet
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Packet} Packet
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Packet.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Packet();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.msg = $root.server.Msg.decode(reader, reader.uint32());
                    break;
                case 2:
                    message.iq = $root.server.Iq.decode(reader, reader.uint32());
                    break;
                case 3:
                    message.ack = $root.server.Ack.decode(reader, reader.uint32());
                    break;
                case 4:
                    message.presence = $root.server.Presence.decode(reader, reader.uint32());
                    break;
                case 5:
                    message.haError = $root.server.HaError.decode(reader, reader.uint32());
                    break;
                case 6:
                    message.chatState = $root.server.ChatState.decode(reader, reader.uint32());
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Packet message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Packet
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Packet} Packet
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Packet.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Packet message.
         * @function verify
         * @memberof server.Packet
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Packet.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            var properties = {};
            if (message.msg != null && message.hasOwnProperty("msg")) {
                properties.stanza = 1;
                {
                    var error = $root.server.Msg.verify(message.msg);
                    if (error)
                        return "msg." + error;
                }
            }
            if (message.iq != null && message.hasOwnProperty("iq")) {
                if (properties.stanza === 1)
                    return "stanza: multiple values";
                properties.stanza = 1;
                {
                    var error = $root.server.Iq.verify(message.iq);
                    if (error)
                        return "iq." + error;
                }
            }
            if (message.ack != null && message.hasOwnProperty("ack")) {
                if (properties.stanza === 1)
                    return "stanza: multiple values";
                properties.stanza = 1;
                {
                    var error = $root.server.Ack.verify(message.ack);
                    if (error)
                        return "ack." + error;
                }
            }
            if (message.presence != null && message.hasOwnProperty("presence")) {
                if (properties.stanza === 1)
                    return "stanza: multiple values";
                properties.stanza = 1;
                {
                    var error = $root.server.Presence.verify(message.presence);
                    if (error)
                        return "presence." + error;
                }
            }
            if (message.haError != null && message.hasOwnProperty("haError")) {
                if (properties.stanza === 1)
                    return "stanza: multiple values";
                properties.stanza = 1;
                {
                    var error = $root.server.HaError.verify(message.haError);
                    if (error)
                        return "haError." + error;
                }
            }
            if (message.chatState != null && message.hasOwnProperty("chatState")) {
                if (properties.stanza === 1)
                    return "stanza: multiple values";
                properties.stanza = 1;
                {
                    var error = $root.server.ChatState.verify(message.chatState);
                    if (error)
                        return "chatState." + error;
                }
            }
            return null;
        };

        /**
         * Creates a Packet message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Packet
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Packet} Packet
         */
        Packet.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Packet)
                return object;
            var message = new $root.server.Packet();
            if (object.msg != null) {
                if (typeof object.msg !== "object")
                    throw TypeError(".server.Packet.msg: object expected");
                message.msg = $root.server.Msg.fromObject(object.msg);
            }
            if (object.iq != null) {
                if (typeof object.iq !== "object")
                    throw TypeError(".server.Packet.iq: object expected");
                message.iq = $root.server.Iq.fromObject(object.iq);
            }
            if (object.ack != null) {
                if (typeof object.ack !== "object")
                    throw TypeError(".server.Packet.ack: object expected");
                message.ack = $root.server.Ack.fromObject(object.ack);
            }
            if (object.presence != null) {
                if (typeof object.presence !== "object")
                    throw TypeError(".server.Packet.presence: object expected");
                message.presence = $root.server.Presence.fromObject(object.presence);
            }
            if (object.haError != null) {
                if (typeof object.haError !== "object")
                    throw TypeError(".server.Packet.haError: object expected");
                message.haError = $root.server.HaError.fromObject(object.haError);
            }
            if (object.chatState != null) {
                if (typeof object.chatState !== "object")
                    throw TypeError(".server.Packet.chatState: object expected");
                message.chatState = $root.server.ChatState.fromObject(object.chatState);
            }
            return message;
        };

        /**
         * Creates a plain object from a Packet message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Packet
         * @static
         * @param {server.Packet} message Packet
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Packet.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (message.msg != null && message.hasOwnProperty("msg")) {
                object.msg = $root.server.Msg.toObject(message.msg, options);
                if (options.oneofs)
                    object.stanza = "msg";
            }
            if (message.iq != null && message.hasOwnProperty("iq")) {
                object.iq = $root.server.Iq.toObject(message.iq, options);
                if (options.oneofs)
                    object.stanza = "iq";
            }
            if (message.ack != null && message.hasOwnProperty("ack")) {
                object.ack = $root.server.Ack.toObject(message.ack, options);
                if (options.oneofs)
                    object.stanza = "ack";
            }
            if (message.presence != null && message.hasOwnProperty("presence")) {
                object.presence = $root.server.Presence.toObject(message.presence, options);
                if (options.oneofs)
                    object.stanza = "presence";
            }
            if (message.haError != null && message.hasOwnProperty("haError")) {
                object.haError = $root.server.HaError.toObject(message.haError, options);
                if (options.oneofs)
                    object.stanza = "haError";
            }
            if (message.chatState != null && message.hasOwnProperty("chatState")) {
                object.chatState = $root.server.ChatState.toObject(message.chatState, options);
                if (options.oneofs)
                    object.stanza = "chatState";
            }
            return object;
        };

        /**
         * Converts this Packet to JSON.
         * @function toJSON
         * @memberof server.Packet
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Packet.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Packet;
    })();

    server.UidElement = (function() {

        /**
         * Properties of an UidElement.
         * @memberof server
         * @interface IUidElement
         * @property {server.UidElement.Action|null} [action] UidElement action
         * @property {number|Long|null} [uid] UidElement uid
         */

        /**
         * Constructs a new UidElement.
         * @memberof server
         * @classdesc Represents an UidElement.
         * @implements IUidElement
         * @constructor
         * @param {server.IUidElement=} [properties] Properties to set
         */
        function UidElement(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * UidElement action.
         * @member {server.UidElement.Action} action
         * @memberof server.UidElement
         * @instance
         */
        UidElement.prototype.action = 0;

        /**
         * UidElement uid.
         * @member {number|Long} uid
         * @memberof server.UidElement
         * @instance
         */
        UidElement.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Creates a new UidElement instance using the specified properties.
         * @function create
         * @memberof server.UidElement
         * @static
         * @param {server.IUidElement=} [properties] Properties to set
         * @returns {server.UidElement} UidElement instance
         */
        UidElement.create = function create(properties) {
            return new UidElement(properties);
        };

        /**
         * Encodes the specified UidElement message. Does not implicitly {@link server.UidElement.verify|verify} messages.
         * @function encode
         * @memberof server.UidElement
         * @static
         * @param {server.IUidElement} message UidElement message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UidElement.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.action != null && Object.hasOwnProperty.call(message, "action"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.action);
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 2, wireType 0 =*/16).int64(message.uid);
            return writer;
        };

        /**
         * Encodes the specified UidElement message, length delimited. Does not implicitly {@link server.UidElement.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.UidElement
         * @static
         * @param {server.IUidElement} message UidElement message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        UidElement.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an UidElement message from the specified reader or buffer.
         * @function decode
         * @memberof server.UidElement
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.UidElement} UidElement
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        UidElement.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.UidElement();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.action = reader.int32();
                    break;
                case 2:
                    message.uid = reader.int64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an UidElement message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.UidElement
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.UidElement} UidElement
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        UidElement.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an UidElement message.
         * @function verify
         * @memberof server.UidElement
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        UidElement.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.action != null && message.hasOwnProperty("action"))
                switch (message.action) {
                default:
                    return "action: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            return null;
        };

        /**
         * Creates an UidElement message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.UidElement
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.UidElement} UidElement
         */
        UidElement.fromObject = function fromObject(object) {
            if (object instanceof $root.server.UidElement)
                return object;
            var message = new $root.server.UidElement();
            switch (object.action) {
            case "ADD":
            case 0:
                message.action = 0;
                break;
            case "DELETE":
            case 1:
                message.action = 1;
                break;
            }
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = false;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber();
            return message;
        };

        /**
         * Creates a plain object from an UidElement message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.UidElement
         * @static
         * @param {server.UidElement} message UidElement
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        UidElement.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.action = options.enums === String ? "ADD" : 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
            }
            if (message.action != null && message.hasOwnProperty("action"))
                object.action = options.enums === String ? $root.server.UidElement.Action[message.action] : message.action;
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber() : message.uid;
            return object;
        };

        /**
         * Converts this UidElement to JSON.
         * @function toJSON
         * @memberof server.UidElement
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        UidElement.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Action enum.
         * @name server.UidElement.Action
         * @enum {number}
         * @property {number} ADD=0 ADD value
         * @property {number} DELETE=1 DELETE value
         */
        UidElement.Action = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "ADD"] = 0;
            values[valuesById[1] = "DELETE"] = 1;
            return values;
        })();

        return UidElement;
    })();

    server.PrivacyList = (function() {

        /**
         * Properties of a PrivacyList.
         * @memberof server
         * @interface IPrivacyList
         * @property {server.PrivacyList.Type|null} [type] PrivacyList type
         * @property {Array.<server.IUidElement>|null} [uidElements] PrivacyList uidElements
         * @property {Uint8Array|null} [hash] PrivacyList hash
         */

        /**
         * Constructs a new PrivacyList.
         * @memberof server
         * @classdesc Represents a PrivacyList.
         * @implements IPrivacyList
         * @constructor
         * @param {server.IPrivacyList=} [properties] Properties to set
         */
        function PrivacyList(properties) {
            this.uidElements = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * PrivacyList type.
         * @member {server.PrivacyList.Type} type
         * @memberof server.PrivacyList
         * @instance
         */
        PrivacyList.prototype.type = 0;

        /**
         * PrivacyList uidElements.
         * @member {Array.<server.IUidElement>} uidElements
         * @memberof server.PrivacyList
         * @instance
         */
        PrivacyList.prototype.uidElements = $util.emptyArray;

        /**
         * PrivacyList hash.
         * @member {Uint8Array} hash
         * @memberof server.PrivacyList
         * @instance
         */
        PrivacyList.prototype.hash = $util.newBuffer([]);

        /**
         * Creates a new PrivacyList instance using the specified properties.
         * @function create
         * @memberof server.PrivacyList
         * @static
         * @param {server.IPrivacyList=} [properties] Properties to set
         * @returns {server.PrivacyList} PrivacyList instance
         */
        PrivacyList.create = function create(properties) {
            return new PrivacyList(properties);
        };

        /**
         * Encodes the specified PrivacyList message. Does not implicitly {@link server.PrivacyList.verify|verify} messages.
         * @function encode
         * @memberof server.PrivacyList
         * @static
         * @param {server.IPrivacyList} message PrivacyList message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PrivacyList.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.type);
            if (message.uidElements != null && message.uidElements.length)
                for (var i = 0; i < message.uidElements.length; ++i)
                    $root.server.UidElement.encode(message.uidElements[i], writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            if (message.hash != null && Object.hasOwnProperty.call(message, "hash"))
                writer.uint32(/* id 3, wireType 2 =*/26).bytes(message.hash);
            return writer;
        };

        /**
         * Encodes the specified PrivacyList message, length delimited. Does not implicitly {@link server.PrivacyList.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.PrivacyList
         * @static
         * @param {server.IPrivacyList} message PrivacyList message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PrivacyList.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a PrivacyList message from the specified reader or buffer.
         * @function decode
         * @memberof server.PrivacyList
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.PrivacyList} PrivacyList
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PrivacyList.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.PrivacyList();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.type = reader.int32();
                    break;
                case 2:
                    if (!(message.uidElements && message.uidElements.length))
                        message.uidElements = [];
                    message.uidElements.push($root.server.UidElement.decode(reader, reader.uint32()));
                    break;
                case 3:
                    message.hash = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a PrivacyList message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.PrivacyList
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.PrivacyList} PrivacyList
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PrivacyList.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a PrivacyList message.
         * @function verify
         * @memberof server.PrivacyList
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        PrivacyList.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.type != null && message.hasOwnProperty("type"))
                switch (message.type) {
                default:
                    return "type: enum value expected";
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    break;
                }
            if (message.uidElements != null && message.hasOwnProperty("uidElements")) {
                if (!Array.isArray(message.uidElements))
                    return "uidElements: array expected";
                for (var i = 0; i < message.uidElements.length; ++i) {
                    var error = $root.server.UidElement.verify(message.uidElements[i]);
                    if (error)
                        return "uidElements." + error;
                }
            }
            if (message.hash != null && message.hasOwnProperty("hash"))
                if (!(message.hash && typeof message.hash.length === "number" || $util.isString(message.hash)))
                    return "hash: buffer expected";
            return null;
        };

        /**
         * Creates a PrivacyList message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.PrivacyList
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.PrivacyList} PrivacyList
         */
        PrivacyList.fromObject = function fromObject(object) {
            if (object instanceof $root.server.PrivacyList)
                return object;
            var message = new $root.server.PrivacyList();
            switch (object.type) {
            case "ALL":
            case 0:
                message.type = 0;
                break;
            case "BLOCK":
            case 1:
                message.type = 1;
                break;
            case "EXCEPT":
            case 2:
                message.type = 2;
                break;
            case "MUTE":
            case 3:
                message.type = 3;
                break;
            case "ONLY":
            case 4:
                message.type = 4;
                break;
            }
            if (object.uidElements) {
                if (!Array.isArray(object.uidElements))
                    throw TypeError(".server.PrivacyList.uidElements: array expected");
                message.uidElements = [];
                for (var i = 0; i < object.uidElements.length; ++i) {
                    if (typeof object.uidElements[i] !== "object")
                        throw TypeError(".server.PrivacyList.uidElements: object expected");
                    message.uidElements[i] = $root.server.UidElement.fromObject(object.uidElements[i]);
                }
            }
            if (object.hash != null)
                if (typeof object.hash === "string")
                    $util.base64.decode(object.hash, message.hash = $util.newBuffer($util.base64.length(object.hash)), 0);
                else if (object.hash.length)
                    message.hash = object.hash;
            return message;
        };

        /**
         * Creates a plain object from a PrivacyList message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.PrivacyList
         * @static
         * @param {server.PrivacyList} message PrivacyList
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        PrivacyList.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.uidElements = [];
            if (options.defaults) {
                object.type = options.enums === String ? "ALL" : 0;
                if (options.bytes === String)
                    object.hash = "";
                else {
                    object.hash = [];
                    if (options.bytes !== Array)
                        object.hash = $util.newBuffer(object.hash);
                }
            }
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.server.PrivacyList.Type[message.type] : message.type;
            if (message.uidElements && message.uidElements.length) {
                object.uidElements = [];
                for (var j = 0; j < message.uidElements.length; ++j)
                    object.uidElements[j] = $root.server.UidElement.toObject(message.uidElements[j], options);
            }
            if (message.hash != null && message.hasOwnProperty("hash"))
                object.hash = options.bytes === String ? $util.base64.encode(message.hash, 0, message.hash.length) : options.bytes === Array ? Array.prototype.slice.call(message.hash) : message.hash;
            return object;
        };

        /**
         * Converts this PrivacyList to JSON.
         * @function toJSON
         * @memberof server.PrivacyList
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        PrivacyList.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Type enum.
         * @name server.PrivacyList.Type
         * @enum {number}
         * @property {number} ALL=0 ALL value
         * @property {number} BLOCK=1 BLOCK value
         * @property {number} EXCEPT=2 EXCEPT value
         * @property {number} MUTE=3 MUTE value
         * @property {number} ONLY=4 ONLY value
         */
        PrivacyList.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "ALL"] = 0;
            values[valuesById[1] = "BLOCK"] = 1;
            values[valuesById[2] = "EXCEPT"] = 2;
            values[valuesById[3] = "MUTE"] = 3;
            values[valuesById[4] = "ONLY"] = 4;
            return values;
        })();

        return PrivacyList;
    })();

    server.PrivacyListResult = (function() {

        /**
         * Properties of a PrivacyListResult.
         * @memberof server
         * @interface IPrivacyListResult
         * @property {string|null} [result] PrivacyListResult result
         * @property {string|null} [reason] PrivacyListResult reason
         * @property {Uint8Array|null} [hash] PrivacyListResult hash
         */

        /**
         * Constructs a new PrivacyListResult.
         * @memberof server
         * @classdesc Represents a PrivacyListResult.
         * @implements IPrivacyListResult
         * @constructor
         * @param {server.IPrivacyListResult=} [properties] Properties to set
         */
        function PrivacyListResult(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * PrivacyListResult result.
         * @member {string} result
         * @memberof server.PrivacyListResult
         * @instance
         */
        PrivacyListResult.prototype.result = "";

        /**
         * PrivacyListResult reason.
         * @member {string} reason
         * @memberof server.PrivacyListResult
         * @instance
         */
        PrivacyListResult.prototype.reason = "";

        /**
         * PrivacyListResult hash.
         * @member {Uint8Array} hash
         * @memberof server.PrivacyListResult
         * @instance
         */
        PrivacyListResult.prototype.hash = $util.newBuffer([]);

        /**
         * Creates a new PrivacyListResult instance using the specified properties.
         * @function create
         * @memberof server.PrivacyListResult
         * @static
         * @param {server.IPrivacyListResult=} [properties] Properties to set
         * @returns {server.PrivacyListResult} PrivacyListResult instance
         */
        PrivacyListResult.create = function create(properties) {
            return new PrivacyListResult(properties);
        };

        /**
         * Encodes the specified PrivacyListResult message. Does not implicitly {@link server.PrivacyListResult.verify|verify} messages.
         * @function encode
         * @memberof server.PrivacyListResult
         * @static
         * @param {server.IPrivacyListResult} message PrivacyListResult message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PrivacyListResult.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.result != null && Object.hasOwnProperty.call(message, "result"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.result);
            if (message.reason != null && Object.hasOwnProperty.call(message, "reason"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.reason);
            if (message.hash != null && Object.hasOwnProperty.call(message, "hash"))
                writer.uint32(/* id 3, wireType 2 =*/26).bytes(message.hash);
            return writer;
        };

        /**
         * Encodes the specified PrivacyListResult message, length delimited. Does not implicitly {@link server.PrivacyListResult.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.PrivacyListResult
         * @static
         * @param {server.IPrivacyListResult} message PrivacyListResult message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PrivacyListResult.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a PrivacyListResult message from the specified reader or buffer.
         * @function decode
         * @memberof server.PrivacyListResult
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.PrivacyListResult} PrivacyListResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PrivacyListResult.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.PrivacyListResult();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.result = reader.string();
                    break;
                case 2:
                    message.reason = reader.string();
                    break;
                case 3:
                    message.hash = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a PrivacyListResult message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.PrivacyListResult
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.PrivacyListResult} PrivacyListResult
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PrivacyListResult.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a PrivacyListResult message.
         * @function verify
         * @memberof server.PrivacyListResult
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        PrivacyListResult.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.result != null && message.hasOwnProperty("result"))
                if (!$util.isString(message.result))
                    return "result: string expected";
            if (message.reason != null && message.hasOwnProperty("reason"))
                if (!$util.isString(message.reason))
                    return "reason: string expected";
            if (message.hash != null && message.hasOwnProperty("hash"))
                if (!(message.hash && typeof message.hash.length === "number" || $util.isString(message.hash)))
                    return "hash: buffer expected";
            return null;
        };

        /**
         * Creates a PrivacyListResult message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.PrivacyListResult
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.PrivacyListResult} PrivacyListResult
         */
        PrivacyListResult.fromObject = function fromObject(object) {
            if (object instanceof $root.server.PrivacyListResult)
                return object;
            var message = new $root.server.PrivacyListResult();
            if (object.result != null)
                message.result = String(object.result);
            if (object.reason != null)
                message.reason = String(object.reason);
            if (object.hash != null)
                if (typeof object.hash === "string")
                    $util.base64.decode(object.hash, message.hash = $util.newBuffer($util.base64.length(object.hash)), 0);
                else if (object.hash.length)
                    message.hash = object.hash;
            return message;
        };

        /**
         * Creates a plain object from a PrivacyListResult message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.PrivacyListResult
         * @static
         * @param {server.PrivacyListResult} message PrivacyListResult
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        PrivacyListResult.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.result = "";
                object.reason = "";
                if (options.bytes === String)
                    object.hash = "";
                else {
                    object.hash = [];
                    if (options.bytes !== Array)
                        object.hash = $util.newBuffer(object.hash);
                }
            }
            if (message.result != null && message.hasOwnProperty("result"))
                object.result = message.result;
            if (message.reason != null && message.hasOwnProperty("reason"))
                object.reason = message.reason;
            if (message.hash != null && message.hasOwnProperty("hash"))
                object.hash = options.bytes === String ? $util.base64.encode(message.hash, 0, message.hash.length) : options.bytes === Array ? Array.prototype.slice.call(message.hash) : message.hash;
            return object;
        };

        /**
         * Converts this PrivacyListResult to JSON.
         * @function toJSON
         * @memberof server.PrivacyListResult
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        PrivacyListResult.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return PrivacyListResult;
    })();

    server.PrivacyLists = (function() {

        /**
         * Properties of a PrivacyLists.
         * @memberof server
         * @interface IPrivacyLists
         * @property {server.PrivacyLists.Type|null} [activeType] PrivacyLists activeType
         * @property {Array.<server.IPrivacyList>|null} [lists] PrivacyLists lists
         */

        /**
         * Constructs a new PrivacyLists.
         * @memberof server
         * @classdesc Represents a PrivacyLists.
         * @implements IPrivacyLists
         * @constructor
         * @param {server.IPrivacyLists=} [properties] Properties to set
         */
        function PrivacyLists(properties) {
            this.lists = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * PrivacyLists activeType.
         * @member {server.PrivacyLists.Type} activeType
         * @memberof server.PrivacyLists
         * @instance
         */
        PrivacyLists.prototype.activeType = 0;

        /**
         * PrivacyLists lists.
         * @member {Array.<server.IPrivacyList>} lists
         * @memberof server.PrivacyLists
         * @instance
         */
        PrivacyLists.prototype.lists = $util.emptyArray;

        /**
         * Creates a new PrivacyLists instance using the specified properties.
         * @function create
         * @memberof server.PrivacyLists
         * @static
         * @param {server.IPrivacyLists=} [properties] Properties to set
         * @returns {server.PrivacyLists} PrivacyLists instance
         */
        PrivacyLists.create = function create(properties) {
            return new PrivacyLists(properties);
        };

        /**
         * Encodes the specified PrivacyLists message. Does not implicitly {@link server.PrivacyLists.verify|verify} messages.
         * @function encode
         * @memberof server.PrivacyLists
         * @static
         * @param {server.IPrivacyLists} message PrivacyLists message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PrivacyLists.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.activeType != null && Object.hasOwnProperty.call(message, "activeType"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.activeType);
            if (message.lists != null && message.lists.length)
                for (var i = 0; i < message.lists.length; ++i)
                    $root.server.PrivacyList.encode(message.lists[i], writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified PrivacyLists message, length delimited. Does not implicitly {@link server.PrivacyLists.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.PrivacyLists
         * @static
         * @param {server.IPrivacyLists} message PrivacyLists message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PrivacyLists.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a PrivacyLists message from the specified reader or buffer.
         * @function decode
         * @memberof server.PrivacyLists
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.PrivacyLists} PrivacyLists
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PrivacyLists.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.PrivacyLists();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.activeType = reader.int32();
                    break;
                case 2:
                    if (!(message.lists && message.lists.length))
                        message.lists = [];
                    message.lists.push($root.server.PrivacyList.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a PrivacyLists message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.PrivacyLists
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.PrivacyLists} PrivacyLists
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PrivacyLists.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a PrivacyLists message.
         * @function verify
         * @memberof server.PrivacyLists
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        PrivacyLists.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.activeType != null && message.hasOwnProperty("activeType"))
                switch (message.activeType) {
                default:
                    return "activeType: enum value expected";
                case 0:
                case 1:
                case 2:
                case 3:
                    break;
                }
            if (message.lists != null && message.hasOwnProperty("lists")) {
                if (!Array.isArray(message.lists))
                    return "lists: array expected";
                for (var i = 0; i < message.lists.length; ++i) {
                    var error = $root.server.PrivacyList.verify(message.lists[i]);
                    if (error)
                        return "lists." + error;
                }
            }
            return null;
        };

        /**
         * Creates a PrivacyLists message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.PrivacyLists
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.PrivacyLists} PrivacyLists
         */
        PrivacyLists.fromObject = function fromObject(object) {
            if (object instanceof $root.server.PrivacyLists)
                return object;
            var message = new $root.server.PrivacyLists();
            switch (object.activeType) {
            case "ALL":
            case 0:
                message.activeType = 0;
                break;
            case "BLOCK":
            case 1:
                message.activeType = 1;
                break;
            case "EXCEPT":
            case 2:
                message.activeType = 2;
                break;
            case "ONLY":
            case 3:
                message.activeType = 3;
                break;
            }
            if (object.lists) {
                if (!Array.isArray(object.lists))
                    throw TypeError(".server.PrivacyLists.lists: array expected");
                message.lists = [];
                for (var i = 0; i < object.lists.length; ++i) {
                    if (typeof object.lists[i] !== "object")
                        throw TypeError(".server.PrivacyLists.lists: object expected");
                    message.lists[i] = $root.server.PrivacyList.fromObject(object.lists[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a PrivacyLists message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.PrivacyLists
         * @static
         * @param {server.PrivacyLists} message PrivacyLists
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        PrivacyLists.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.lists = [];
            if (options.defaults)
                object.activeType = options.enums === String ? "ALL" : 0;
            if (message.activeType != null && message.hasOwnProperty("activeType"))
                object.activeType = options.enums === String ? $root.server.PrivacyLists.Type[message.activeType] : message.activeType;
            if (message.lists && message.lists.length) {
                object.lists = [];
                for (var j = 0; j < message.lists.length; ++j)
                    object.lists[j] = $root.server.PrivacyList.toObject(message.lists[j], options);
            }
            return object;
        };

        /**
         * Converts this PrivacyLists to JSON.
         * @function toJSON
         * @memberof server.PrivacyLists
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        PrivacyLists.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Type enum.
         * @name server.PrivacyLists.Type
         * @enum {number}
         * @property {number} ALL=0 ALL value
         * @property {number} BLOCK=1 BLOCK value
         * @property {number} EXCEPT=2 EXCEPT value
         * @property {number} ONLY=3 ONLY value
         */
        PrivacyLists.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "ALL"] = 0;
            values[valuesById[1] = "BLOCK"] = 1;
            values[valuesById[2] = "EXCEPT"] = 2;
            values[valuesById[3] = "ONLY"] = 3;
            return values;
        })();

        return PrivacyLists;
    })();

    server.PushToken = (function() {

        /**
         * Properties of a PushToken.
         * @memberof server
         * @interface IPushToken
         * @property {server.PushToken.Os|null} [os] PushToken os
         * @property {string|null} [token] PushToken token
         */

        /**
         * Constructs a new PushToken.
         * @memberof server
         * @classdesc Represents a PushToken.
         * @implements IPushToken
         * @constructor
         * @param {server.IPushToken=} [properties] Properties to set
         */
        function PushToken(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * PushToken os.
         * @member {server.PushToken.Os} os
         * @memberof server.PushToken
         * @instance
         */
        PushToken.prototype.os = 0;

        /**
         * PushToken token.
         * @member {string} token
         * @memberof server.PushToken
         * @instance
         */
        PushToken.prototype.token = "";

        /**
         * Creates a new PushToken instance using the specified properties.
         * @function create
         * @memberof server.PushToken
         * @static
         * @param {server.IPushToken=} [properties] Properties to set
         * @returns {server.PushToken} PushToken instance
         */
        PushToken.create = function create(properties) {
            return new PushToken(properties);
        };

        /**
         * Encodes the specified PushToken message. Does not implicitly {@link server.PushToken.verify|verify} messages.
         * @function encode
         * @memberof server.PushToken
         * @static
         * @param {server.IPushToken} message PushToken message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PushToken.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.os != null && Object.hasOwnProperty.call(message, "os"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.os);
            if (message.token != null && Object.hasOwnProperty.call(message, "token"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.token);
            return writer;
        };

        /**
         * Encodes the specified PushToken message, length delimited. Does not implicitly {@link server.PushToken.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.PushToken
         * @static
         * @param {server.IPushToken} message PushToken message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PushToken.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a PushToken message from the specified reader or buffer.
         * @function decode
         * @memberof server.PushToken
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.PushToken} PushToken
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PushToken.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.PushToken();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.os = reader.int32();
                    break;
                case 2:
                    message.token = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a PushToken message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.PushToken
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.PushToken} PushToken
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PushToken.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a PushToken message.
         * @function verify
         * @memberof server.PushToken
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        PushToken.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.os != null && message.hasOwnProperty("os"))
                switch (message.os) {
                default:
                    return "os: enum value expected";
                case 0:
                case 1:
                case 2:
                    break;
                }
            if (message.token != null && message.hasOwnProperty("token"))
                if (!$util.isString(message.token))
                    return "token: string expected";
            return null;
        };

        /**
         * Creates a PushToken message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.PushToken
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.PushToken} PushToken
         */
        PushToken.fromObject = function fromObject(object) {
            if (object instanceof $root.server.PushToken)
                return object;
            var message = new $root.server.PushToken();
            switch (object.os) {
            case "ANDROID":
            case 0:
                message.os = 0;
                break;
            case "IOS":
            case 1:
                message.os = 1;
                break;
            case "IOS_DEV":
            case 2:
                message.os = 2;
                break;
            }
            if (object.token != null)
                message.token = String(object.token);
            return message;
        };

        /**
         * Creates a plain object from a PushToken message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.PushToken
         * @static
         * @param {server.PushToken} message PushToken
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        PushToken.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.os = options.enums === String ? "ANDROID" : 0;
                object.token = "";
            }
            if (message.os != null && message.hasOwnProperty("os"))
                object.os = options.enums === String ? $root.server.PushToken.Os[message.os] : message.os;
            if (message.token != null && message.hasOwnProperty("token"))
                object.token = message.token;
            return object;
        };

        /**
         * Converts this PushToken to JSON.
         * @function toJSON
         * @memberof server.PushToken
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        PushToken.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Os enum.
         * @name server.PushToken.Os
         * @enum {number}
         * @property {number} ANDROID=0 ANDROID value
         * @property {number} IOS=1 IOS value
         * @property {number} IOS_DEV=2 IOS_DEV value
         */
        PushToken.Os = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "ANDROID"] = 0;
            values[valuesById[1] = "IOS"] = 1;
            values[valuesById[2] = "IOS_DEV"] = 2;
            return values;
        })();

        return PushToken;
    })();

    server.PushRegister = (function() {

        /**
         * Properties of a PushRegister.
         * @memberof server
         * @interface IPushRegister
         * @property {server.IPushToken|null} [pushToken] PushRegister pushToken
         */

        /**
         * Constructs a new PushRegister.
         * @memberof server
         * @classdesc Represents a PushRegister.
         * @implements IPushRegister
         * @constructor
         * @param {server.IPushRegister=} [properties] Properties to set
         */
        function PushRegister(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * PushRegister pushToken.
         * @member {server.IPushToken|null|undefined} pushToken
         * @memberof server.PushRegister
         * @instance
         */
        PushRegister.prototype.pushToken = null;

        /**
         * Creates a new PushRegister instance using the specified properties.
         * @function create
         * @memberof server.PushRegister
         * @static
         * @param {server.IPushRegister=} [properties] Properties to set
         * @returns {server.PushRegister} PushRegister instance
         */
        PushRegister.create = function create(properties) {
            return new PushRegister(properties);
        };

        /**
         * Encodes the specified PushRegister message. Does not implicitly {@link server.PushRegister.verify|verify} messages.
         * @function encode
         * @memberof server.PushRegister
         * @static
         * @param {server.IPushRegister} message PushRegister message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PushRegister.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.pushToken != null && Object.hasOwnProperty.call(message, "pushToken"))
                $root.server.PushToken.encode(message.pushToken, writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified PushRegister message, length delimited. Does not implicitly {@link server.PushRegister.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.PushRegister
         * @static
         * @param {server.IPushRegister} message PushRegister message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PushRegister.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a PushRegister message from the specified reader or buffer.
         * @function decode
         * @memberof server.PushRegister
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.PushRegister} PushRegister
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PushRegister.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.PushRegister();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.pushToken = $root.server.PushToken.decode(reader, reader.uint32());
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a PushRegister message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.PushRegister
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.PushRegister} PushRegister
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PushRegister.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a PushRegister message.
         * @function verify
         * @memberof server.PushRegister
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        PushRegister.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.pushToken != null && message.hasOwnProperty("pushToken")) {
                var error = $root.server.PushToken.verify(message.pushToken);
                if (error)
                    return "pushToken." + error;
            }
            return null;
        };

        /**
         * Creates a PushRegister message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.PushRegister
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.PushRegister} PushRegister
         */
        PushRegister.fromObject = function fromObject(object) {
            if (object instanceof $root.server.PushRegister)
                return object;
            var message = new $root.server.PushRegister();
            if (object.pushToken != null) {
                if (typeof object.pushToken !== "object")
                    throw TypeError(".server.PushRegister.pushToken: object expected");
                message.pushToken = $root.server.PushToken.fromObject(object.pushToken);
            }
            return message;
        };

        /**
         * Creates a plain object from a PushRegister message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.PushRegister
         * @static
         * @param {server.PushRegister} message PushRegister
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        PushRegister.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults)
                object.pushToken = null;
            if (message.pushToken != null && message.hasOwnProperty("pushToken"))
                object.pushToken = $root.server.PushToken.toObject(message.pushToken, options);
            return object;
        };

        /**
         * Converts this PushRegister to JSON.
         * @function toJSON
         * @memberof server.PushRegister
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        PushRegister.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return PushRegister;
    })();

    server.PushPref = (function() {

        /**
         * Properties of a PushPref.
         * @memberof server
         * @interface IPushPref
         * @property {server.PushPref.Name|null} [name] PushPref name
         * @property {boolean|null} [value] PushPref value
         */

        /**
         * Constructs a new PushPref.
         * @memberof server
         * @classdesc Represents a PushPref.
         * @implements IPushPref
         * @constructor
         * @param {server.IPushPref=} [properties] Properties to set
         */
        function PushPref(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * PushPref name.
         * @member {server.PushPref.Name} name
         * @memberof server.PushPref
         * @instance
         */
        PushPref.prototype.name = 0;

        /**
         * PushPref value.
         * @member {boolean} value
         * @memberof server.PushPref
         * @instance
         */
        PushPref.prototype.value = false;

        /**
         * Creates a new PushPref instance using the specified properties.
         * @function create
         * @memberof server.PushPref
         * @static
         * @param {server.IPushPref=} [properties] Properties to set
         * @returns {server.PushPref} PushPref instance
         */
        PushPref.create = function create(properties) {
            return new PushPref(properties);
        };

        /**
         * Encodes the specified PushPref message. Does not implicitly {@link server.PushPref.verify|verify} messages.
         * @function encode
         * @memberof server.PushPref
         * @static
         * @param {server.IPushPref} message PushPref message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PushPref.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.name);
            if (message.value != null && Object.hasOwnProperty.call(message, "value"))
                writer.uint32(/* id 2, wireType 0 =*/16).bool(message.value);
            return writer;
        };

        /**
         * Encodes the specified PushPref message, length delimited. Does not implicitly {@link server.PushPref.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.PushPref
         * @static
         * @param {server.IPushPref} message PushPref message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PushPref.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a PushPref message from the specified reader or buffer.
         * @function decode
         * @memberof server.PushPref
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.PushPref} PushPref
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PushPref.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.PushPref();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.name = reader.int32();
                    break;
                case 2:
                    message.value = reader.bool();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a PushPref message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.PushPref
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.PushPref} PushPref
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PushPref.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a PushPref message.
         * @function verify
         * @memberof server.PushPref
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        PushPref.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.name != null && message.hasOwnProperty("name"))
                switch (message.name) {
                default:
                    return "name: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.value != null && message.hasOwnProperty("value"))
                if (typeof message.value !== "boolean")
                    return "value: boolean expected";
            return null;
        };

        /**
         * Creates a PushPref message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.PushPref
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.PushPref} PushPref
         */
        PushPref.fromObject = function fromObject(object) {
            if (object instanceof $root.server.PushPref)
                return object;
            var message = new $root.server.PushPref();
            switch (object.name) {
            case "POST":
            case 0:
                message.name = 0;
                break;
            case "COMMENT":
            case 1:
                message.name = 1;
                break;
            }
            if (object.value != null)
                message.value = Boolean(object.value);
            return message;
        };

        /**
         * Creates a plain object from a PushPref message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.PushPref
         * @static
         * @param {server.PushPref} message PushPref
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        PushPref.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.name = options.enums === String ? "POST" : 0;
                object.value = false;
            }
            if (message.name != null && message.hasOwnProperty("name"))
                object.name = options.enums === String ? $root.server.PushPref.Name[message.name] : message.name;
            if (message.value != null && message.hasOwnProperty("value"))
                object.value = message.value;
            return object;
        };

        /**
         * Converts this PushPref to JSON.
         * @function toJSON
         * @memberof server.PushPref
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        PushPref.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Name enum.
         * @name server.PushPref.Name
         * @enum {number}
         * @property {number} POST=0 POST value
         * @property {number} COMMENT=1 COMMENT value
         */
        PushPref.Name = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "POST"] = 0;
            values[valuesById[1] = "COMMENT"] = 1;
            return values;
        })();

        return PushPref;
    })();

    server.NotificationPrefs = (function() {

        /**
         * Properties of a NotificationPrefs.
         * @memberof server
         * @interface INotificationPrefs
         * @property {Array.<server.IPushPref>|null} [pushPrefs] NotificationPrefs pushPrefs
         */

        /**
         * Constructs a new NotificationPrefs.
         * @memberof server
         * @classdesc Represents a NotificationPrefs.
         * @implements INotificationPrefs
         * @constructor
         * @param {server.INotificationPrefs=} [properties] Properties to set
         */
        function NotificationPrefs(properties) {
            this.pushPrefs = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * NotificationPrefs pushPrefs.
         * @member {Array.<server.IPushPref>} pushPrefs
         * @memberof server.NotificationPrefs
         * @instance
         */
        NotificationPrefs.prototype.pushPrefs = $util.emptyArray;

        /**
         * Creates a new NotificationPrefs instance using the specified properties.
         * @function create
         * @memberof server.NotificationPrefs
         * @static
         * @param {server.INotificationPrefs=} [properties] Properties to set
         * @returns {server.NotificationPrefs} NotificationPrefs instance
         */
        NotificationPrefs.create = function create(properties) {
            return new NotificationPrefs(properties);
        };

        /**
         * Encodes the specified NotificationPrefs message. Does not implicitly {@link server.NotificationPrefs.verify|verify} messages.
         * @function encode
         * @memberof server.NotificationPrefs
         * @static
         * @param {server.INotificationPrefs} message NotificationPrefs message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        NotificationPrefs.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.pushPrefs != null && message.pushPrefs.length)
                for (var i = 0; i < message.pushPrefs.length; ++i)
                    $root.server.PushPref.encode(message.pushPrefs[i], writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified NotificationPrefs message, length delimited. Does not implicitly {@link server.NotificationPrefs.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.NotificationPrefs
         * @static
         * @param {server.INotificationPrefs} message NotificationPrefs message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        NotificationPrefs.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a NotificationPrefs message from the specified reader or buffer.
         * @function decode
         * @memberof server.NotificationPrefs
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.NotificationPrefs} NotificationPrefs
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        NotificationPrefs.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.NotificationPrefs();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    if (!(message.pushPrefs && message.pushPrefs.length))
                        message.pushPrefs = [];
                    message.pushPrefs.push($root.server.PushPref.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a NotificationPrefs message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.NotificationPrefs
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.NotificationPrefs} NotificationPrefs
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        NotificationPrefs.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a NotificationPrefs message.
         * @function verify
         * @memberof server.NotificationPrefs
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        NotificationPrefs.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.pushPrefs != null && message.hasOwnProperty("pushPrefs")) {
                if (!Array.isArray(message.pushPrefs))
                    return "pushPrefs: array expected";
                for (var i = 0; i < message.pushPrefs.length; ++i) {
                    var error = $root.server.PushPref.verify(message.pushPrefs[i]);
                    if (error)
                        return "pushPrefs." + error;
                }
            }
            return null;
        };

        /**
         * Creates a NotificationPrefs message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.NotificationPrefs
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.NotificationPrefs} NotificationPrefs
         */
        NotificationPrefs.fromObject = function fromObject(object) {
            if (object instanceof $root.server.NotificationPrefs)
                return object;
            var message = new $root.server.NotificationPrefs();
            if (object.pushPrefs) {
                if (!Array.isArray(object.pushPrefs))
                    throw TypeError(".server.NotificationPrefs.pushPrefs: array expected");
                message.pushPrefs = [];
                for (var i = 0; i < object.pushPrefs.length; ++i) {
                    if (typeof object.pushPrefs[i] !== "object")
                        throw TypeError(".server.NotificationPrefs.pushPrefs: object expected");
                    message.pushPrefs[i] = $root.server.PushPref.fromObject(object.pushPrefs[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a NotificationPrefs message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.NotificationPrefs
         * @static
         * @param {server.NotificationPrefs} message NotificationPrefs
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        NotificationPrefs.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.pushPrefs = [];
            if (message.pushPrefs && message.pushPrefs.length) {
                object.pushPrefs = [];
                for (var j = 0; j < message.pushPrefs.length; ++j)
                    object.pushPrefs[j] = $root.server.PushPref.toObject(message.pushPrefs[j], options);
            }
            return object;
        };

        /**
         * Converts this NotificationPrefs to JSON.
         * @function toJSON
         * @memberof server.NotificationPrefs
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        NotificationPrefs.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return NotificationPrefs;
    })();

    server.Rerequest = (function() {

        /**
         * Properties of a Rerequest.
         * @memberof server
         * @interface IRerequest
         * @property {string|null} [id] Rerequest id
         * @property {Uint8Array|null} [identityKey] Rerequest identityKey
         * @property {number|Long|null} [signedPreKeyId] Rerequest signedPreKeyId
         * @property {number|Long|null} [oneTimePreKeyId] Rerequest oneTimePreKeyId
         * @property {Uint8Array|null} [sessionSetupEphemeralKey] Rerequest sessionSetupEphemeralKey
         * @property {Uint8Array|null} [messageEphemeralKey] Rerequest messageEphemeralKey
         */

        /**
         * Constructs a new Rerequest.
         * @memberof server
         * @classdesc Represents a Rerequest.
         * @implements IRerequest
         * @constructor
         * @param {server.IRerequest=} [properties] Properties to set
         */
        function Rerequest(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Rerequest id.
         * @member {string} id
         * @memberof server.Rerequest
         * @instance
         */
        Rerequest.prototype.id = "";

        /**
         * Rerequest identityKey.
         * @member {Uint8Array} identityKey
         * @memberof server.Rerequest
         * @instance
         */
        Rerequest.prototype.identityKey = $util.newBuffer([]);

        /**
         * Rerequest signedPreKeyId.
         * @member {number|Long} signedPreKeyId
         * @memberof server.Rerequest
         * @instance
         */
        Rerequest.prototype.signedPreKeyId = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Rerequest oneTimePreKeyId.
         * @member {number|Long} oneTimePreKeyId
         * @memberof server.Rerequest
         * @instance
         */
        Rerequest.prototype.oneTimePreKeyId = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Rerequest sessionSetupEphemeralKey.
         * @member {Uint8Array} sessionSetupEphemeralKey
         * @memberof server.Rerequest
         * @instance
         */
        Rerequest.prototype.sessionSetupEphemeralKey = $util.newBuffer([]);

        /**
         * Rerequest messageEphemeralKey.
         * @member {Uint8Array} messageEphemeralKey
         * @memberof server.Rerequest
         * @instance
         */
        Rerequest.prototype.messageEphemeralKey = $util.newBuffer([]);

        /**
         * Creates a new Rerequest instance using the specified properties.
         * @function create
         * @memberof server.Rerequest
         * @static
         * @param {server.IRerequest=} [properties] Properties to set
         * @returns {server.Rerequest} Rerequest instance
         */
        Rerequest.create = function create(properties) {
            return new Rerequest(properties);
        };

        /**
         * Encodes the specified Rerequest message. Does not implicitly {@link server.Rerequest.verify|verify} messages.
         * @function encode
         * @memberof server.Rerequest
         * @static
         * @param {server.IRerequest} message Rerequest message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Rerequest.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.identityKey != null && Object.hasOwnProperty.call(message, "identityKey"))
                writer.uint32(/* id 2, wireType 2 =*/18).bytes(message.identityKey);
            if (message.signedPreKeyId != null && Object.hasOwnProperty.call(message, "signedPreKeyId"))
                writer.uint32(/* id 3, wireType 0 =*/24).int64(message.signedPreKeyId);
            if (message.oneTimePreKeyId != null && Object.hasOwnProperty.call(message, "oneTimePreKeyId"))
                writer.uint32(/* id 4, wireType 0 =*/32).int64(message.oneTimePreKeyId);
            if (message.sessionSetupEphemeralKey != null && Object.hasOwnProperty.call(message, "sessionSetupEphemeralKey"))
                writer.uint32(/* id 5, wireType 2 =*/42).bytes(message.sessionSetupEphemeralKey);
            if (message.messageEphemeralKey != null && Object.hasOwnProperty.call(message, "messageEphemeralKey"))
                writer.uint32(/* id 6, wireType 2 =*/50).bytes(message.messageEphemeralKey);
            return writer;
        };

        /**
         * Encodes the specified Rerequest message, length delimited. Does not implicitly {@link server.Rerequest.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Rerequest
         * @static
         * @param {server.IRerequest} message Rerequest message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Rerequest.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Rerequest message from the specified reader or buffer.
         * @function decode
         * @memberof server.Rerequest
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Rerequest} Rerequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Rerequest.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Rerequest();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.identityKey = reader.bytes();
                    break;
                case 3:
                    message.signedPreKeyId = reader.int64();
                    break;
                case 4:
                    message.oneTimePreKeyId = reader.int64();
                    break;
                case 5:
                    message.sessionSetupEphemeralKey = reader.bytes();
                    break;
                case 6:
                    message.messageEphemeralKey = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Rerequest message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Rerequest
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Rerequest} Rerequest
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Rerequest.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Rerequest message.
         * @function verify
         * @memberof server.Rerequest
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Rerequest.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.identityKey != null && message.hasOwnProperty("identityKey"))
                if (!(message.identityKey && typeof message.identityKey.length === "number" || $util.isString(message.identityKey)))
                    return "identityKey: buffer expected";
            if (message.signedPreKeyId != null && message.hasOwnProperty("signedPreKeyId"))
                if (!$util.isInteger(message.signedPreKeyId) && !(message.signedPreKeyId && $util.isInteger(message.signedPreKeyId.low) && $util.isInteger(message.signedPreKeyId.high)))
                    return "signedPreKeyId: integer|Long expected";
            if (message.oneTimePreKeyId != null && message.hasOwnProperty("oneTimePreKeyId"))
                if (!$util.isInteger(message.oneTimePreKeyId) && !(message.oneTimePreKeyId && $util.isInteger(message.oneTimePreKeyId.low) && $util.isInteger(message.oneTimePreKeyId.high)))
                    return "oneTimePreKeyId: integer|Long expected";
            if (message.sessionSetupEphemeralKey != null && message.hasOwnProperty("sessionSetupEphemeralKey"))
                if (!(message.sessionSetupEphemeralKey && typeof message.sessionSetupEphemeralKey.length === "number" || $util.isString(message.sessionSetupEphemeralKey)))
                    return "sessionSetupEphemeralKey: buffer expected";
            if (message.messageEphemeralKey != null && message.hasOwnProperty("messageEphemeralKey"))
                if (!(message.messageEphemeralKey && typeof message.messageEphemeralKey.length === "number" || $util.isString(message.messageEphemeralKey)))
                    return "messageEphemeralKey: buffer expected";
            return null;
        };

        /**
         * Creates a Rerequest message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Rerequest
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Rerequest} Rerequest
         */
        Rerequest.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Rerequest)
                return object;
            var message = new $root.server.Rerequest();
            if (object.id != null)
                message.id = String(object.id);
            if (object.identityKey != null)
                if (typeof object.identityKey === "string")
                    $util.base64.decode(object.identityKey, message.identityKey = $util.newBuffer($util.base64.length(object.identityKey)), 0);
                else if (object.identityKey.length)
                    message.identityKey = object.identityKey;
            if (object.signedPreKeyId != null)
                if ($util.Long)
                    (message.signedPreKeyId = $util.Long.fromValue(object.signedPreKeyId)).unsigned = false;
                else if (typeof object.signedPreKeyId === "string")
                    message.signedPreKeyId = parseInt(object.signedPreKeyId, 10);
                else if (typeof object.signedPreKeyId === "number")
                    message.signedPreKeyId = object.signedPreKeyId;
                else if (typeof object.signedPreKeyId === "object")
                    message.signedPreKeyId = new $util.LongBits(object.signedPreKeyId.low >>> 0, object.signedPreKeyId.high >>> 0).toNumber();
            if (object.oneTimePreKeyId != null)
                if ($util.Long)
                    (message.oneTimePreKeyId = $util.Long.fromValue(object.oneTimePreKeyId)).unsigned = false;
                else if (typeof object.oneTimePreKeyId === "string")
                    message.oneTimePreKeyId = parseInt(object.oneTimePreKeyId, 10);
                else if (typeof object.oneTimePreKeyId === "number")
                    message.oneTimePreKeyId = object.oneTimePreKeyId;
                else if (typeof object.oneTimePreKeyId === "object")
                    message.oneTimePreKeyId = new $util.LongBits(object.oneTimePreKeyId.low >>> 0, object.oneTimePreKeyId.high >>> 0).toNumber();
            if (object.sessionSetupEphemeralKey != null)
                if (typeof object.sessionSetupEphemeralKey === "string")
                    $util.base64.decode(object.sessionSetupEphemeralKey, message.sessionSetupEphemeralKey = $util.newBuffer($util.base64.length(object.sessionSetupEphemeralKey)), 0);
                else if (object.sessionSetupEphemeralKey.length)
                    message.sessionSetupEphemeralKey = object.sessionSetupEphemeralKey;
            if (object.messageEphemeralKey != null)
                if (typeof object.messageEphemeralKey === "string")
                    $util.base64.decode(object.messageEphemeralKey, message.messageEphemeralKey = $util.newBuffer($util.base64.length(object.messageEphemeralKey)), 0);
                else if (object.messageEphemeralKey.length)
                    message.messageEphemeralKey = object.messageEphemeralKey;
            return message;
        };

        /**
         * Creates a plain object from a Rerequest message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Rerequest
         * @static
         * @param {server.Rerequest} message Rerequest
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Rerequest.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                if (options.bytes === String)
                    object.identityKey = "";
                else {
                    object.identityKey = [];
                    if (options.bytes !== Array)
                        object.identityKey = $util.newBuffer(object.identityKey);
                }
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.signedPreKeyId = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.signedPreKeyId = options.longs === String ? "0" : 0;
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.oneTimePreKeyId = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.oneTimePreKeyId = options.longs === String ? "0" : 0;
                if (options.bytes === String)
                    object.sessionSetupEphemeralKey = "";
                else {
                    object.sessionSetupEphemeralKey = [];
                    if (options.bytes !== Array)
                        object.sessionSetupEphemeralKey = $util.newBuffer(object.sessionSetupEphemeralKey);
                }
                if (options.bytes === String)
                    object.messageEphemeralKey = "";
                else {
                    object.messageEphemeralKey = [];
                    if (options.bytes !== Array)
                        object.messageEphemeralKey = $util.newBuffer(object.messageEphemeralKey);
                }
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.identityKey != null && message.hasOwnProperty("identityKey"))
                object.identityKey = options.bytes === String ? $util.base64.encode(message.identityKey, 0, message.identityKey.length) : options.bytes === Array ? Array.prototype.slice.call(message.identityKey) : message.identityKey;
            if (message.signedPreKeyId != null && message.hasOwnProperty("signedPreKeyId"))
                if (typeof message.signedPreKeyId === "number")
                    object.signedPreKeyId = options.longs === String ? String(message.signedPreKeyId) : message.signedPreKeyId;
                else
                    object.signedPreKeyId = options.longs === String ? $util.Long.prototype.toString.call(message.signedPreKeyId) : options.longs === Number ? new $util.LongBits(message.signedPreKeyId.low >>> 0, message.signedPreKeyId.high >>> 0).toNumber() : message.signedPreKeyId;
            if (message.oneTimePreKeyId != null && message.hasOwnProperty("oneTimePreKeyId"))
                if (typeof message.oneTimePreKeyId === "number")
                    object.oneTimePreKeyId = options.longs === String ? String(message.oneTimePreKeyId) : message.oneTimePreKeyId;
                else
                    object.oneTimePreKeyId = options.longs === String ? $util.Long.prototype.toString.call(message.oneTimePreKeyId) : options.longs === Number ? new $util.LongBits(message.oneTimePreKeyId.low >>> 0, message.oneTimePreKeyId.high >>> 0).toNumber() : message.oneTimePreKeyId;
            if (message.sessionSetupEphemeralKey != null && message.hasOwnProperty("sessionSetupEphemeralKey"))
                object.sessionSetupEphemeralKey = options.bytes === String ? $util.base64.encode(message.sessionSetupEphemeralKey, 0, message.sessionSetupEphemeralKey.length) : options.bytes === Array ? Array.prototype.slice.call(message.sessionSetupEphemeralKey) : message.sessionSetupEphemeralKey;
            if (message.messageEphemeralKey != null && message.hasOwnProperty("messageEphemeralKey"))
                object.messageEphemeralKey = options.bytes === String ? $util.base64.encode(message.messageEphemeralKey, 0, message.messageEphemeralKey.length) : options.bytes === Array ? Array.prototype.slice.call(message.messageEphemeralKey) : message.messageEphemeralKey;
            return object;
        };

        /**
         * Converts this Rerequest to JSON.
         * @function toJSON
         * @memberof server.Rerequest
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Rerequest.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Rerequest;
    })();

    server.SeenReceipt = (function() {

        /**
         * Properties of a SeenReceipt.
         * @memberof server
         * @interface ISeenReceipt
         * @property {string|null} [id] SeenReceipt id
         * @property {string|null} [threadId] SeenReceipt threadId
         * @property {number|Long|null} [timestamp] SeenReceipt timestamp
         */

        /**
         * Constructs a new SeenReceipt.
         * @memberof server
         * @classdesc Represents a SeenReceipt.
         * @implements ISeenReceipt
         * @constructor
         * @param {server.ISeenReceipt=} [properties] Properties to set
         */
        function SeenReceipt(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * SeenReceipt id.
         * @member {string} id
         * @memberof server.SeenReceipt
         * @instance
         */
        SeenReceipt.prototype.id = "";

        /**
         * SeenReceipt threadId.
         * @member {string} threadId
         * @memberof server.SeenReceipt
         * @instance
         */
        SeenReceipt.prototype.threadId = "";

        /**
         * SeenReceipt timestamp.
         * @member {number|Long} timestamp
         * @memberof server.SeenReceipt
         * @instance
         */
        SeenReceipt.prototype.timestamp = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Creates a new SeenReceipt instance using the specified properties.
         * @function create
         * @memberof server.SeenReceipt
         * @static
         * @param {server.ISeenReceipt=} [properties] Properties to set
         * @returns {server.SeenReceipt} SeenReceipt instance
         */
        SeenReceipt.create = function create(properties) {
            return new SeenReceipt(properties);
        };

        /**
         * Encodes the specified SeenReceipt message. Does not implicitly {@link server.SeenReceipt.verify|verify} messages.
         * @function encode
         * @memberof server.SeenReceipt
         * @static
         * @param {server.ISeenReceipt} message SeenReceipt message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        SeenReceipt.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.threadId != null && Object.hasOwnProperty.call(message, "threadId"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.threadId);
            if (message.timestamp != null && Object.hasOwnProperty.call(message, "timestamp"))
                writer.uint32(/* id 3, wireType 0 =*/24).int64(message.timestamp);
            return writer;
        };

        /**
         * Encodes the specified SeenReceipt message, length delimited. Does not implicitly {@link server.SeenReceipt.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.SeenReceipt
         * @static
         * @param {server.ISeenReceipt} message SeenReceipt message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        SeenReceipt.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a SeenReceipt message from the specified reader or buffer.
         * @function decode
         * @memberof server.SeenReceipt
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.SeenReceipt} SeenReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        SeenReceipt.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.SeenReceipt();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.threadId = reader.string();
                    break;
                case 3:
                    message.timestamp = reader.int64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a SeenReceipt message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.SeenReceipt
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.SeenReceipt} SeenReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        SeenReceipt.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a SeenReceipt message.
         * @function verify
         * @memberof server.SeenReceipt
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        SeenReceipt.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.threadId != null && message.hasOwnProperty("threadId"))
                if (!$util.isString(message.threadId))
                    return "threadId: string expected";
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (!$util.isInteger(message.timestamp) && !(message.timestamp && $util.isInteger(message.timestamp.low) && $util.isInteger(message.timestamp.high)))
                    return "timestamp: integer|Long expected";
            return null;
        };

        /**
         * Creates a SeenReceipt message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.SeenReceipt
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.SeenReceipt} SeenReceipt
         */
        SeenReceipt.fromObject = function fromObject(object) {
            if (object instanceof $root.server.SeenReceipt)
                return object;
            var message = new $root.server.SeenReceipt();
            if (object.id != null)
                message.id = String(object.id);
            if (object.threadId != null)
                message.threadId = String(object.threadId);
            if (object.timestamp != null)
                if ($util.Long)
                    (message.timestamp = $util.Long.fromValue(object.timestamp)).unsigned = false;
                else if (typeof object.timestamp === "string")
                    message.timestamp = parseInt(object.timestamp, 10);
                else if (typeof object.timestamp === "number")
                    message.timestamp = object.timestamp;
                else if (typeof object.timestamp === "object")
                    message.timestamp = new $util.LongBits(object.timestamp.low >>> 0, object.timestamp.high >>> 0).toNumber();
            return message;
        };

        /**
         * Creates a plain object from a SeenReceipt message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.SeenReceipt
         * @static
         * @param {server.SeenReceipt} message SeenReceipt
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        SeenReceipt.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.threadId = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.timestamp = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.timestamp = options.longs === String ? "0" : 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.threadId != null && message.hasOwnProperty("threadId"))
                object.threadId = message.threadId;
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (typeof message.timestamp === "number")
                    object.timestamp = options.longs === String ? String(message.timestamp) : message.timestamp;
                else
                    object.timestamp = options.longs === String ? $util.Long.prototype.toString.call(message.timestamp) : options.longs === Number ? new $util.LongBits(message.timestamp.low >>> 0, message.timestamp.high >>> 0).toNumber() : message.timestamp;
            return object;
        };

        /**
         * Converts this SeenReceipt to JSON.
         * @function toJSON
         * @memberof server.SeenReceipt
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        SeenReceipt.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return SeenReceipt;
    })();

    server.DeliveryReceipt = (function() {

        /**
         * Properties of a DeliveryReceipt.
         * @memberof server
         * @interface IDeliveryReceipt
         * @property {string|null} [id] DeliveryReceipt id
         * @property {string|null} [threadId] DeliveryReceipt threadId
         * @property {number|Long|null} [timestamp] DeliveryReceipt timestamp
         */

        /**
         * Constructs a new DeliveryReceipt.
         * @memberof server
         * @classdesc Represents a DeliveryReceipt.
         * @implements IDeliveryReceipt
         * @constructor
         * @param {server.IDeliveryReceipt=} [properties] Properties to set
         */
        function DeliveryReceipt(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * DeliveryReceipt id.
         * @member {string} id
         * @memberof server.DeliveryReceipt
         * @instance
         */
        DeliveryReceipt.prototype.id = "";

        /**
         * DeliveryReceipt threadId.
         * @member {string} threadId
         * @memberof server.DeliveryReceipt
         * @instance
         */
        DeliveryReceipt.prototype.threadId = "";

        /**
         * DeliveryReceipt timestamp.
         * @member {number|Long} timestamp
         * @memberof server.DeliveryReceipt
         * @instance
         */
        DeliveryReceipt.prototype.timestamp = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * Creates a new DeliveryReceipt instance using the specified properties.
         * @function create
         * @memberof server.DeliveryReceipt
         * @static
         * @param {server.IDeliveryReceipt=} [properties] Properties to set
         * @returns {server.DeliveryReceipt} DeliveryReceipt instance
         */
        DeliveryReceipt.create = function create(properties) {
            return new DeliveryReceipt(properties);
        };

        /**
         * Encodes the specified DeliveryReceipt message. Does not implicitly {@link server.DeliveryReceipt.verify|verify} messages.
         * @function encode
         * @memberof server.DeliveryReceipt
         * @static
         * @param {server.IDeliveryReceipt} message DeliveryReceipt message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        DeliveryReceipt.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.threadId != null && Object.hasOwnProperty.call(message, "threadId"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.threadId);
            if (message.timestamp != null && Object.hasOwnProperty.call(message, "timestamp"))
                writer.uint32(/* id 3, wireType 0 =*/24).int64(message.timestamp);
            return writer;
        };

        /**
         * Encodes the specified DeliveryReceipt message, length delimited. Does not implicitly {@link server.DeliveryReceipt.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.DeliveryReceipt
         * @static
         * @param {server.IDeliveryReceipt} message DeliveryReceipt message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        DeliveryReceipt.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a DeliveryReceipt message from the specified reader or buffer.
         * @function decode
         * @memberof server.DeliveryReceipt
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.DeliveryReceipt} DeliveryReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        DeliveryReceipt.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.DeliveryReceipt();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.threadId = reader.string();
                    break;
                case 3:
                    message.timestamp = reader.int64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a DeliveryReceipt message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.DeliveryReceipt
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.DeliveryReceipt} DeliveryReceipt
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        DeliveryReceipt.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a DeliveryReceipt message.
         * @function verify
         * @memberof server.DeliveryReceipt
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        DeliveryReceipt.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.threadId != null && message.hasOwnProperty("threadId"))
                if (!$util.isString(message.threadId))
                    return "threadId: string expected";
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (!$util.isInteger(message.timestamp) && !(message.timestamp && $util.isInteger(message.timestamp.low) && $util.isInteger(message.timestamp.high)))
                    return "timestamp: integer|Long expected";
            return null;
        };

        /**
         * Creates a DeliveryReceipt message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.DeliveryReceipt
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.DeliveryReceipt} DeliveryReceipt
         */
        DeliveryReceipt.fromObject = function fromObject(object) {
            if (object instanceof $root.server.DeliveryReceipt)
                return object;
            var message = new $root.server.DeliveryReceipt();
            if (object.id != null)
                message.id = String(object.id);
            if (object.threadId != null)
                message.threadId = String(object.threadId);
            if (object.timestamp != null)
                if ($util.Long)
                    (message.timestamp = $util.Long.fromValue(object.timestamp)).unsigned = false;
                else if (typeof object.timestamp === "string")
                    message.timestamp = parseInt(object.timestamp, 10);
                else if (typeof object.timestamp === "number")
                    message.timestamp = object.timestamp;
                else if (typeof object.timestamp === "object")
                    message.timestamp = new $util.LongBits(object.timestamp.low >>> 0, object.timestamp.high >>> 0).toNumber();
            return message;
        };

        /**
         * Creates a plain object from a DeliveryReceipt message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.DeliveryReceipt
         * @static
         * @param {server.DeliveryReceipt} message DeliveryReceipt
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        DeliveryReceipt.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.threadId = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.timestamp = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.timestamp = options.longs === String ? "0" : 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.threadId != null && message.hasOwnProperty("threadId"))
                object.threadId = message.threadId;
            if (message.timestamp != null && message.hasOwnProperty("timestamp"))
                if (typeof message.timestamp === "number")
                    object.timestamp = options.longs === String ? String(message.timestamp) : message.timestamp;
                else
                    object.timestamp = options.longs === String ? $util.Long.prototype.toString.call(message.timestamp) : options.longs === Number ? new $util.LongBits(message.timestamp.low >>> 0, message.timestamp.high >>> 0).toNumber() : message.timestamp;
            return object;
        };

        /**
         * Converts this DeliveryReceipt to JSON.
         * @function toJSON
         * @memberof server.DeliveryReceipt
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        DeliveryReceipt.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return DeliveryReceipt;
    })();

    server.GroupChatRetract = (function() {

        /**
         * Properties of a GroupChatRetract.
         * @memberof server
         * @interface IGroupChatRetract
         * @property {string|null} [id] GroupChatRetract id
         * @property {string|null} [gid] GroupChatRetract gid
         */

        /**
         * Constructs a new GroupChatRetract.
         * @memberof server
         * @classdesc Represents a GroupChatRetract.
         * @implements IGroupChatRetract
         * @constructor
         * @param {server.IGroupChatRetract=} [properties] Properties to set
         */
        function GroupChatRetract(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * GroupChatRetract id.
         * @member {string} id
         * @memberof server.GroupChatRetract
         * @instance
         */
        GroupChatRetract.prototype.id = "";

        /**
         * GroupChatRetract gid.
         * @member {string} gid
         * @memberof server.GroupChatRetract
         * @instance
         */
        GroupChatRetract.prototype.gid = "";

        /**
         * Creates a new GroupChatRetract instance using the specified properties.
         * @function create
         * @memberof server.GroupChatRetract
         * @static
         * @param {server.IGroupChatRetract=} [properties] Properties to set
         * @returns {server.GroupChatRetract} GroupChatRetract instance
         */
        GroupChatRetract.create = function create(properties) {
            return new GroupChatRetract(properties);
        };

        /**
         * Encodes the specified GroupChatRetract message. Does not implicitly {@link server.GroupChatRetract.verify|verify} messages.
         * @function encode
         * @memberof server.GroupChatRetract
         * @static
         * @param {server.IGroupChatRetract} message GroupChatRetract message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupChatRetract.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.gid != null && Object.hasOwnProperty.call(message, "gid"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.gid);
            return writer;
        };

        /**
         * Encodes the specified GroupChatRetract message, length delimited. Does not implicitly {@link server.GroupChatRetract.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.GroupChatRetract
         * @static
         * @param {server.IGroupChatRetract} message GroupChatRetract message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        GroupChatRetract.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a GroupChatRetract message from the specified reader or buffer.
         * @function decode
         * @memberof server.GroupChatRetract
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.GroupChatRetract} GroupChatRetract
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupChatRetract.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.GroupChatRetract();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.gid = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a GroupChatRetract message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.GroupChatRetract
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.GroupChatRetract} GroupChatRetract
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        GroupChatRetract.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a GroupChatRetract message.
         * @function verify
         * @memberof server.GroupChatRetract
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        GroupChatRetract.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.gid != null && message.hasOwnProperty("gid"))
                if (!$util.isString(message.gid))
                    return "gid: string expected";
            return null;
        };

        /**
         * Creates a GroupChatRetract message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.GroupChatRetract
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.GroupChatRetract} GroupChatRetract
         */
        GroupChatRetract.fromObject = function fromObject(object) {
            if (object instanceof $root.server.GroupChatRetract)
                return object;
            var message = new $root.server.GroupChatRetract();
            if (object.id != null)
                message.id = String(object.id);
            if (object.gid != null)
                message.gid = String(object.gid);
            return message;
        };

        /**
         * Creates a plain object from a GroupChatRetract message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.GroupChatRetract
         * @static
         * @param {server.GroupChatRetract} message GroupChatRetract
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        GroupChatRetract.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.gid = "";
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.gid != null && message.hasOwnProperty("gid"))
                object.gid = message.gid;
            return object;
        };

        /**
         * Converts this GroupChatRetract to JSON.
         * @function toJSON
         * @memberof server.GroupChatRetract
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        GroupChatRetract.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return GroupChatRetract;
    })();

    server.ChatRetract = (function() {

        /**
         * Properties of a ChatRetract.
         * @memberof server
         * @interface IChatRetract
         * @property {string|null} [id] ChatRetract id
         */

        /**
         * Constructs a new ChatRetract.
         * @memberof server
         * @classdesc Represents a ChatRetract.
         * @implements IChatRetract
         * @constructor
         * @param {server.IChatRetract=} [properties] Properties to set
         */
        function ChatRetract(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * ChatRetract id.
         * @member {string} id
         * @memberof server.ChatRetract
         * @instance
         */
        ChatRetract.prototype.id = "";

        /**
         * Creates a new ChatRetract instance using the specified properties.
         * @function create
         * @memberof server.ChatRetract
         * @static
         * @param {server.IChatRetract=} [properties] Properties to set
         * @returns {server.ChatRetract} ChatRetract instance
         */
        ChatRetract.create = function create(properties) {
            return new ChatRetract(properties);
        };

        /**
         * Encodes the specified ChatRetract message. Does not implicitly {@link server.ChatRetract.verify|verify} messages.
         * @function encode
         * @memberof server.ChatRetract
         * @static
         * @param {server.IChatRetract} message ChatRetract message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ChatRetract.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            return writer;
        };

        /**
         * Encodes the specified ChatRetract message, length delimited. Does not implicitly {@link server.ChatRetract.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.ChatRetract
         * @static
         * @param {server.IChatRetract} message ChatRetract message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        ChatRetract.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a ChatRetract message from the specified reader or buffer.
         * @function decode
         * @memberof server.ChatRetract
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.ChatRetract} ChatRetract
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ChatRetract.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.ChatRetract();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a ChatRetract message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.ChatRetract
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.ChatRetract} ChatRetract
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        ChatRetract.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a ChatRetract message.
         * @function verify
         * @memberof server.ChatRetract
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        ChatRetract.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            return null;
        };

        /**
         * Creates a ChatRetract message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.ChatRetract
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.ChatRetract} ChatRetract
         */
        ChatRetract.fromObject = function fromObject(object) {
            if (object instanceof $root.server.ChatRetract)
                return object;
            var message = new $root.server.ChatRetract();
            if (object.id != null)
                message.id = String(object.id);
            return message;
        };

        /**
         * Creates a plain object from a ChatRetract message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.ChatRetract
         * @static
         * @param {server.ChatRetract} message ChatRetract
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        ChatRetract.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults)
                object.id = "";
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            return object;
        };

        /**
         * Converts this ChatRetract to JSON.
         * @function toJSON
         * @memberof server.ChatRetract
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        ChatRetract.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return ChatRetract;
    })();

    server.Prop = (function() {

        /**
         * Properties of a Prop.
         * @memberof server
         * @interface IProp
         * @property {string|null} [name] Prop name
         * @property {string|null} [value] Prop value
         */

        /**
         * Constructs a new Prop.
         * @memberof server
         * @classdesc Represents a Prop.
         * @implements IProp
         * @constructor
         * @param {server.IProp=} [properties] Properties to set
         */
        function Prop(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Prop name.
         * @member {string} name
         * @memberof server.Prop
         * @instance
         */
        Prop.prototype.name = "";

        /**
         * Prop value.
         * @member {string} value
         * @memberof server.Prop
         * @instance
         */
        Prop.prototype.value = "";

        /**
         * Creates a new Prop instance using the specified properties.
         * @function create
         * @memberof server.Prop
         * @static
         * @param {server.IProp=} [properties] Properties to set
         * @returns {server.Prop} Prop instance
         */
        Prop.create = function create(properties) {
            return new Prop(properties);
        };

        /**
         * Encodes the specified Prop message. Does not implicitly {@link server.Prop.verify|verify} messages.
         * @function encode
         * @memberof server.Prop
         * @static
         * @param {server.IProp} message Prop message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Prop.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.name != null && Object.hasOwnProperty.call(message, "name"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.name);
            if (message.value != null && Object.hasOwnProperty.call(message, "value"))
                writer.uint32(/* id 2, wireType 2 =*/18).string(message.value);
            return writer;
        };

        /**
         * Encodes the specified Prop message, length delimited. Does not implicitly {@link server.Prop.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Prop
         * @static
         * @param {server.IProp} message Prop message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Prop.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Prop message from the specified reader or buffer.
         * @function decode
         * @memberof server.Prop
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Prop} Prop
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Prop.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Prop();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.name = reader.string();
                    break;
                case 2:
                    message.value = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Prop message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Prop
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Prop} Prop
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Prop.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Prop message.
         * @function verify
         * @memberof server.Prop
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Prop.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.name != null && message.hasOwnProperty("name"))
                if (!$util.isString(message.name))
                    return "name: string expected";
            if (message.value != null && message.hasOwnProperty("value"))
                if (!$util.isString(message.value))
                    return "value: string expected";
            return null;
        };

        /**
         * Creates a Prop message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Prop
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Prop} Prop
         */
        Prop.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Prop)
                return object;
            var message = new $root.server.Prop();
            if (object.name != null)
                message.name = String(object.name);
            if (object.value != null)
                message.value = String(object.value);
            return message;
        };

        /**
         * Creates a plain object from a Prop message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Prop
         * @static
         * @param {server.Prop} message Prop
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Prop.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.name = "";
                object.value = "";
            }
            if (message.name != null && message.hasOwnProperty("name"))
                object.name = message.name;
            if (message.value != null && message.hasOwnProperty("value"))
                object.value = message.value;
            return object;
        };

        /**
         * Converts this Prop to JSON.
         * @function toJSON
         * @memberof server.Prop
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Prop.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Prop;
    })();

    server.Props = (function() {

        /**
         * Properties of a Props.
         * @memberof server
         * @interface IProps
         * @property {Uint8Array|null} [hash] Props hash
         * @property {Array.<server.IProp>|null} [props] Props props
         */

        /**
         * Constructs a new Props.
         * @memberof server
         * @classdesc Represents a Props.
         * @implements IProps
         * @constructor
         * @param {server.IProps=} [properties] Properties to set
         */
        function Props(properties) {
            this.props = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * Props hash.
         * @member {Uint8Array} hash
         * @memberof server.Props
         * @instance
         */
        Props.prototype.hash = $util.newBuffer([]);

        /**
         * Props props.
         * @member {Array.<server.IProp>} props
         * @memberof server.Props
         * @instance
         */
        Props.prototype.props = $util.emptyArray;

        /**
         * Creates a new Props instance using the specified properties.
         * @function create
         * @memberof server.Props
         * @static
         * @param {server.IProps=} [properties] Properties to set
         * @returns {server.Props} Props instance
         */
        Props.create = function create(properties) {
            return new Props(properties);
        };

        /**
         * Encodes the specified Props message. Does not implicitly {@link server.Props.verify|verify} messages.
         * @function encode
         * @memberof server.Props
         * @static
         * @param {server.IProps} message Props message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Props.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.hash != null && Object.hasOwnProperty.call(message, "hash"))
                writer.uint32(/* id 1, wireType 2 =*/10).bytes(message.hash);
            if (message.props != null && message.props.length)
                for (var i = 0; i < message.props.length; ++i)
                    $root.server.Prop.encode(message.props[i], writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified Props message, length delimited. Does not implicitly {@link server.Props.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.Props
         * @static
         * @param {server.IProps} message Props message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        Props.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a Props message from the specified reader or buffer.
         * @function decode
         * @memberof server.Props
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.Props} Props
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Props.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.Props();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.hash = reader.bytes();
                    break;
                case 2:
                    if (!(message.props && message.props.length))
                        message.props = [];
                    message.props.push($root.server.Prop.decode(reader, reader.uint32()));
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a Props message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.Props
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.Props} Props
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        Props.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a Props message.
         * @function verify
         * @memberof server.Props
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        Props.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.hash != null && message.hasOwnProperty("hash"))
                if (!(message.hash && typeof message.hash.length === "number" || $util.isString(message.hash)))
                    return "hash: buffer expected";
            if (message.props != null && message.hasOwnProperty("props")) {
                if (!Array.isArray(message.props))
                    return "props: array expected";
                for (var i = 0; i < message.props.length; ++i) {
                    var error = $root.server.Prop.verify(message.props[i]);
                    if (error)
                        return "props." + error;
                }
            }
            return null;
        };

        /**
         * Creates a Props message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.Props
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.Props} Props
         */
        Props.fromObject = function fromObject(object) {
            if (object instanceof $root.server.Props)
                return object;
            var message = new $root.server.Props();
            if (object.hash != null)
                if (typeof object.hash === "string")
                    $util.base64.decode(object.hash, message.hash = $util.newBuffer($util.base64.length(object.hash)), 0);
                else if (object.hash.length)
                    message.hash = object.hash;
            if (object.props) {
                if (!Array.isArray(object.props))
                    throw TypeError(".server.Props.props: array expected");
                message.props = [];
                for (var i = 0; i < object.props.length; ++i) {
                    if (typeof object.props[i] !== "object")
                        throw TypeError(".server.Props.props: object expected");
                    message.props[i] = $root.server.Prop.fromObject(object.props[i]);
                }
            }
            return message;
        };

        /**
         * Creates a plain object from a Props message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.Props
         * @static
         * @param {server.Props} message Props
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        Props.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.props = [];
            if (options.defaults)
                if (options.bytes === String)
                    object.hash = "";
                else {
                    object.hash = [];
                    if (options.bytes !== Array)
                        object.hash = $util.newBuffer(object.hash);
                }
            if (message.hash != null && message.hasOwnProperty("hash"))
                object.hash = options.bytes === String ? $util.base64.encode(message.hash, 0, message.hash.length) : options.bytes === Array ? Array.prototype.slice.call(message.hash) : message.hash;
            if (message.props && message.props.length) {
                object.props = [];
                for (var j = 0; j < message.props.length; ++j)
                    object.props[j] = $root.server.Prop.toObject(message.props[j], options);
            }
            return object;
        };

        /**
         * Converts this Props to JSON.
         * @function toJSON
         * @memberof server.Props
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        Props.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return Props;
    })();

    server.WhisperKeys = (function() {

        /**
         * Properties of a WhisperKeys.
         * @memberof server
         * @interface IWhisperKeys
         * @property {number|Long|null} [uid] WhisperKeys uid
         * @property {server.WhisperKeys.Action|null} [action] WhisperKeys action
         * @property {Uint8Array|null} [identityKey] WhisperKeys identityKey
         * @property {Uint8Array|null} [signedKey] WhisperKeys signedKey
         * @property {number|null} [otpKeyCount] WhisperKeys otpKeyCount
         * @property {Array.<Uint8Array>|null} [oneTimeKeys] WhisperKeys oneTimeKeys
         */

        /**
         * Constructs a new WhisperKeys.
         * @memberof server
         * @classdesc Represents a WhisperKeys.
         * @implements IWhisperKeys
         * @constructor
         * @param {server.IWhisperKeys=} [properties] Properties to set
         */
        function WhisperKeys(properties) {
            this.oneTimeKeys = [];
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * WhisperKeys uid.
         * @member {number|Long} uid
         * @memberof server.WhisperKeys
         * @instance
         */
        WhisperKeys.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

        /**
         * WhisperKeys action.
         * @member {server.WhisperKeys.Action} action
         * @memberof server.WhisperKeys
         * @instance
         */
        WhisperKeys.prototype.action = 0;

        /**
         * WhisperKeys identityKey.
         * @member {Uint8Array} identityKey
         * @memberof server.WhisperKeys
         * @instance
         */
        WhisperKeys.prototype.identityKey = $util.newBuffer([]);

        /**
         * WhisperKeys signedKey.
         * @member {Uint8Array} signedKey
         * @memberof server.WhisperKeys
         * @instance
         */
        WhisperKeys.prototype.signedKey = $util.newBuffer([]);

        /**
         * WhisperKeys otpKeyCount.
         * @member {number} otpKeyCount
         * @memberof server.WhisperKeys
         * @instance
         */
        WhisperKeys.prototype.otpKeyCount = 0;

        /**
         * WhisperKeys oneTimeKeys.
         * @member {Array.<Uint8Array>} oneTimeKeys
         * @memberof server.WhisperKeys
         * @instance
         */
        WhisperKeys.prototype.oneTimeKeys = $util.emptyArray;

        /**
         * Creates a new WhisperKeys instance using the specified properties.
         * @function create
         * @memberof server.WhisperKeys
         * @static
         * @param {server.IWhisperKeys=} [properties] Properties to set
         * @returns {server.WhisperKeys} WhisperKeys instance
         */
        WhisperKeys.create = function create(properties) {
            return new WhisperKeys(properties);
        };

        /**
         * Encodes the specified WhisperKeys message. Does not implicitly {@link server.WhisperKeys.verify|verify} messages.
         * @function encode
         * @memberof server.WhisperKeys
         * @static
         * @param {server.IWhisperKeys} message WhisperKeys message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        WhisperKeys.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 1, wireType 0 =*/8).int64(message.uid);
            if (message.action != null && Object.hasOwnProperty.call(message, "action"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.action);
            if (message.identityKey != null && Object.hasOwnProperty.call(message, "identityKey"))
                writer.uint32(/* id 3, wireType 2 =*/26).bytes(message.identityKey);
            if (message.signedKey != null && Object.hasOwnProperty.call(message, "signedKey"))
                writer.uint32(/* id 4, wireType 2 =*/34).bytes(message.signedKey);
            if (message.otpKeyCount != null && Object.hasOwnProperty.call(message, "otpKeyCount"))
                writer.uint32(/* id 5, wireType 0 =*/40).int32(message.otpKeyCount);
            if (message.oneTimeKeys != null && message.oneTimeKeys.length)
                for (var i = 0; i < message.oneTimeKeys.length; ++i)
                    writer.uint32(/* id 6, wireType 2 =*/50).bytes(message.oneTimeKeys[i]);
            return writer;
        };

        /**
         * Encodes the specified WhisperKeys message, length delimited. Does not implicitly {@link server.WhisperKeys.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.WhisperKeys
         * @static
         * @param {server.IWhisperKeys} message WhisperKeys message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        WhisperKeys.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a WhisperKeys message from the specified reader or buffer.
         * @function decode
         * @memberof server.WhisperKeys
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.WhisperKeys} WhisperKeys
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        WhisperKeys.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.WhisperKeys();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.uid = reader.int64();
                    break;
                case 2:
                    message.action = reader.int32();
                    break;
                case 3:
                    message.identityKey = reader.bytes();
                    break;
                case 4:
                    message.signedKey = reader.bytes();
                    break;
                case 5:
                    message.otpKeyCount = reader.int32();
                    break;
                case 6:
                    if (!(message.oneTimeKeys && message.oneTimeKeys.length))
                        message.oneTimeKeys = [];
                    message.oneTimeKeys.push(reader.bytes());
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a WhisperKeys message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.WhisperKeys
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.WhisperKeys} WhisperKeys
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        WhisperKeys.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a WhisperKeys message.
         * @function verify
         * @memberof server.WhisperKeys
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        WhisperKeys.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            if (message.action != null && message.hasOwnProperty("action"))
                switch (message.action) {
                default:
                    return "action: enum value expected";
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    break;
                }
            if (message.identityKey != null && message.hasOwnProperty("identityKey"))
                if (!(message.identityKey && typeof message.identityKey.length === "number" || $util.isString(message.identityKey)))
                    return "identityKey: buffer expected";
            if (message.signedKey != null && message.hasOwnProperty("signedKey"))
                if (!(message.signedKey && typeof message.signedKey.length === "number" || $util.isString(message.signedKey)))
                    return "signedKey: buffer expected";
            if (message.otpKeyCount != null && message.hasOwnProperty("otpKeyCount"))
                if (!$util.isInteger(message.otpKeyCount))
                    return "otpKeyCount: integer expected";
            if (message.oneTimeKeys != null && message.hasOwnProperty("oneTimeKeys")) {
                if (!Array.isArray(message.oneTimeKeys))
                    return "oneTimeKeys: array expected";
                for (var i = 0; i < message.oneTimeKeys.length; ++i)
                    if (!(message.oneTimeKeys[i] && typeof message.oneTimeKeys[i].length === "number" || $util.isString(message.oneTimeKeys[i])))
                        return "oneTimeKeys: buffer[] expected";
            }
            return null;
        };

        /**
         * Creates a WhisperKeys message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.WhisperKeys
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.WhisperKeys} WhisperKeys
         */
        WhisperKeys.fromObject = function fromObject(object) {
            if (object instanceof $root.server.WhisperKeys)
                return object;
            var message = new $root.server.WhisperKeys();
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = false;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber();
            switch (object.action) {
            case "NORMAL":
            case 0:
                message.action = 0;
                break;
            case "ADD":
            case 1:
                message.action = 1;
                break;
            case "COUNT":
            case 2:
                message.action = 2;
                break;
            case "GET":
            case 3:
                message.action = 3;
                break;
            case "SET":
            case 4:
                message.action = 4;
                break;
            case "UPDATE":
            case 5:
                message.action = 5;
                break;
            }
            if (object.identityKey != null)
                if (typeof object.identityKey === "string")
                    $util.base64.decode(object.identityKey, message.identityKey = $util.newBuffer($util.base64.length(object.identityKey)), 0);
                else if (object.identityKey.length)
                    message.identityKey = object.identityKey;
            if (object.signedKey != null)
                if (typeof object.signedKey === "string")
                    $util.base64.decode(object.signedKey, message.signedKey = $util.newBuffer($util.base64.length(object.signedKey)), 0);
                else if (object.signedKey.length)
                    message.signedKey = object.signedKey;
            if (object.otpKeyCount != null)
                message.otpKeyCount = object.otpKeyCount | 0;
            if (object.oneTimeKeys) {
                if (!Array.isArray(object.oneTimeKeys))
                    throw TypeError(".server.WhisperKeys.oneTimeKeys: array expected");
                message.oneTimeKeys = [];
                for (var i = 0; i < object.oneTimeKeys.length; ++i)
                    if (typeof object.oneTimeKeys[i] === "string")
                        $util.base64.decode(object.oneTimeKeys[i], message.oneTimeKeys[i] = $util.newBuffer($util.base64.length(object.oneTimeKeys[i])), 0);
                    else if (object.oneTimeKeys[i].length)
                        message.oneTimeKeys[i] = object.oneTimeKeys[i];
            }
            return message;
        };

        /**
         * Creates a plain object from a WhisperKeys message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.WhisperKeys
         * @static
         * @param {server.WhisperKeys} message WhisperKeys
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        WhisperKeys.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.arrays || options.defaults)
                object.oneTimeKeys = [];
            if (options.defaults) {
                if ($util.Long) {
                    var long = new $util.Long(0, 0, false);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
                object.action = options.enums === String ? "NORMAL" : 0;
                if (options.bytes === String)
                    object.identityKey = "";
                else {
                    object.identityKey = [];
                    if (options.bytes !== Array)
                        object.identityKey = $util.newBuffer(object.identityKey);
                }
                if (options.bytes === String)
                    object.signedKey = "";
                else {
                    object.signedKey = [];
                    if (options.bytes !== Array)
                        object.signedKey = $util.newBuffer(object.signedKey);
                }
                object.otpKeyCount = 0;
            }
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber() : message.uid;
            if (message.action != null && message.hasOwnProperty("action"))
                object.action = options.enums === String ? $root.server.WhisperKeys.Action[message.action] : message.action;
            if (message.identityKey != null && message.hasOwnProperty("identityKey"))
                object.identityKey = options.bytes === String ? $util.base64.encode(message.identityKey, 0, message.identityKey.length) : options.bytes === Array ? Array.prototype.slice.call(message.identityKey) : message.identityKey;
            if (message.signedKey != null && message.hasOwnProperty("signedKey"))
                object.signedKey = options.bytes === String ? $util.base64.encode(message.signedKey, 0, message.signedKey.length) : options.bytes === Array ? Array.prototype.slice.call(message.signedKey) : message.signedKey;
            if (message.otpKeyCount != null && message.hasOwnProperty("otpKeyCount"))
                object.otpKeyCount = message.otpKeyCount;
            if (message.oneTimeKeys && message.oneTimeKeys.length) {
                object.oneTimeKeys = [];
                for (var j = 0; j < message.oneTimeKeys.length; ++j)
                    object.oneTimeKeys[j] = options.bytes === String ? $util.base64.encode(message.oneTimeKeys[j], 0, message.oneTimeKeys[j].length) : options.bytes === Array ? Array.prototype.slice.call(message.oneTimeKeys[j]) : message.oneTimeKeys[j];
            }
            return object;
        };

        /**
         * Converts this WhisperKeys to JSON.
         * @function toJSON
         * @memberof server.WhisperKeys
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        WhisperKeys.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Action enum.
         * @name server.WhisperKeys.Action
         * @enum {number}
         * @property {number} NORMAL=0 NORMAL value
         * @property {number} ADD=1 ADD value
         * @property {number} COUNT=2 COUNT value
         * @property {number} GET=3 GET value
         * @property {number} SET=4 SET value
         * @property {number} UPDATE=5 UPDATE value
         */
        WhisperKeys.Action = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "NORMAL"] = 0;
            values[valuesById[1] = "ADD"] = 1;
            values[valuesById[2] = "COUNT"] = 2;
            values[valuesById[3] = "GET"] = 3;
            values[valuesById[4] = "SET"] = 4;
            values[valuesById[5] = "UPDATE"] = 5;
            return values;
        })();

        return WhisperKeys;
    })();

    server.NoiseMessage = (function() {

        /**
         * Properties of a NoiseMessage.
         * @memberof server
         * @interface INoiseMessage
         * @property {server.NoiseMessage.MessageType|null} [messageType] NoiseMessage messageType
         * @property {Uint8Array|null} [content] NoiseMessage content
         */

        /**
         * Constructs a new NoiseMessage.
         * @memberof server
         * @classdesc Represents a NoiseMessage.
         * @implements INoiseMessage
         * @constructor
         * @param {server.INoiseMessage=} [properties] Properties to set
         */
        function NoiseMessage(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * NoiseMessage messageType.
         * @member {server.NoiseMessage.MessageType} messageType
         * @memberof server.NoiseMessage
         * @instance
         */
        NoiseMessage.prototype.messageType = 0;

        /**
         * NoiseMessage content.
         * @member {Uint8Array} content
         * @memberof server.NoiseMessage
         * @instance
         */
        NoiseMessage.prototype.content = $util.newBuffer([]);

        /**
         * Creates a new NoiseMessage instance using the specified properties.
         * @function create
         * @memberof server.NoiseMessage
         * @static
         * @param {server.INoiseMessage=} [properties] Properties to set
         * @returns {server.NoiseMessage} NoiseMessage instance
         */
        NoiseMessage.create = function create(properties) {
            return new NoiseMessage(properties);
        };

        /**
         * Encodes the specified NoiseMessage message. Does not implicitly {@link server.NoiseMessage.verify|verify} messages.
         * @function encode
         * @memberof server.NoiseMessage
         * @static
         * @param {server.INoiseMessage} message NoiseMessage message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        NoiseMessage.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.messageType != null && Object.hasOwnProperty.call(message, "messageType"))
                writer.uint32(/* id 1, wireType 0 =*/8).int32(message.messageType);
            if (message.content != null && Object.hasOwnProperty.call(message, "content"))
                writer.uint32(/* id 2, wireType 2 =*/18).bytes(message.content);
            return writer;
        };

        /**
         * Encodes the specified NoiseMessage message, length delimited. Does not implicitly {@link server.NoiseMessage.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.NoiseMessage
         * @static
         * @param {server.INoiseMessage} message NoiseMessage message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        NoiseMessage.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a NoiseMessage message from the specified reader or buffer.
         * @function decode
         * @memberof server.NoiseMessage
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.NoiseMessage} NoiseMessage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        NoiseMessage.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.NoiseMessage();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.messageType = reader.int32();
                    break;
                case 2:
                    message.content = reader.bytes();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a NoiseMessage message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.NoiseMessage
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.NoiseMessage} NoiseMessage
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        NoiseMessage.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a NoiseMessage message.
         * @function verify
         * @memberof server.NoiseMessage
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        NoiseMessage.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.messageType != null && message.hasOwnProperty("messageType"))
                switch (message.messageType) {
                default:
                    return "messageType: enum value expected";
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    break;
                }
            if (message.content != null && message.hasOwnProperty("content"))
                if (!(message.content && typeof message.content.length === "number" || $util.isString(message.content)))
                    return "content: buffer expected";
            return null;
        };

        /**
         * Creates a NoiseMessage message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.NoiseMessage
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.NoiseMessage} NoiseMessage
         */
        NoiseMessage.fromObject = function fromObject(object) {
            if (object instanceof $root.server.NoiseMessage)
                return object;
            var message = new $root.server.NoiseMessage();
            switch (object.messageType) {
            case "XX_A":
            case 0:
                message.messageType = 0;
                break;
            case "XX_B":
            case 1:
                message.messageType = 1;
                break;
            case "XX_C":
            case 2:
                message.messageType = 2;
                break;
            case "IK_A":
            case 3:
                message.messageType = 3;
                break;
            case "IK_B":
            case 4:
                message.messageType = 4;
                break;
            case "XX_FALLBACK_A":
            case 5:
                message.messageType = 5;
                break;
            case "XX_FALLBACK_B":
            case 6:
                message.messageType = 6;
                break;
            }
            if (object.content != null)
                if (typeof object.content === "string")
                    $util.base64.decode(object.content, message.content = $util.newBuffer($util.base64.length(object.content)), 0);
                else if (object.content.length)
                    message.content = object.content;
            return message;
        };

        /**
         * Creates a plain object from a NoiseMessage message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.NoiseMessage
         * @static
         * @param {server.NoiseMessage} message NoiseMessage
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        NoiseMessage.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.messageType = options.enums === String ? "XX_A" : 0;
                if (options.bytes === String)
                    object.content = "";
                else {
                    object.content = [];
                    if (options.bytes !== Array)
                        object.content = $util.newBuffer(object.content);
                }
            }
            if (message.messageType != null && message.hasOwnProperty("messageType"))
                object.messageType = options.enums === String ? $root.server.NoiseMessage.MessageType[message.messageType] : message.messageType;
            if (message.content != null && message.hasOwnProperty("content"))
                object.content = options.bytes === String ? $util.base64.encode(message.content, 0, message.content.length) : options.bytes === Array ? Array.prototype.slice.call(message.content) : message.content;
            return object;
        };

        /**
         * Converts this NoiseMessage to JSON.
         * @function toJSON
         * @memberof server.NoiseMessage
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        NoiseMessage.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * MessageType enum.
         * @name server.NoiseMessage.MessageType
         * @enum {number}
         * @property {number} XX_A=0 XX_A value
         * @property {number} XX_B=1 XX_B value
         * @property {number} XX_C=2 XX_C value
         * @property {number} IK_A=3 IK_A value
         * @property {number} IK_B=4 IK_B value
         * @property {number} XX_FALLBACK_A=5 XX_FALLBACK_A value
         * @property {number} XX_FALLBACK_B=6 XX_FALLBACK_B value
         */
        NoiseMessage.MessageType = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "XX_A"] = 0;
            values[valuesById[1] = "XX_B"] = 1;
            values[valuesById[2] = "XX_C"] = 2;
            values[valuesById[3] = "IK_A"] = 3;
            values[valuesById[4] = "IK_B"] = 4;
            values[valuesById[5] = "XX_FALLBACK_A"] = 5;
            values[valuesById[6] = "XX_FALLBACK_B"] = 6;
            return values;
        })();

        return NoiseMessage;
    })();

    server.DeleteAccount = (function() {

        /**
         * Properties of a DeleteAccount.
         * @memberof server
         * @interface IDeleteAccount
         * @property {string|null} [phone] DeleteAccount phone
         */

        /**
         * Constructs a new DeleteAccount.
         * @memberof server
         * @classdesc Represents a DeleteAccount.
         * @implements IDeleteAccount
         * @constructor
         * @param {server.IDeleteAccount=} [properties] Properties to set
         */
        function DeleteAccount(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * DeleteAccount phone.
         * @member {string} phone
         * @memberof server.DeleteAccount
         * @instance
         */
        DeleteAccount.prototype.phone = "";

        /**
         * Creates a new DeleteAccount instance using the specified properties.
         * @function create
         * @memberof server.DeleteAccount
         * @static
         * @param {server.IDeleteAccount=} [properties] Properties to set
         * @returns {server.DeleteAccount} DeleteAccount instance
         */
        DeleteAccount.create = function create(properties) {
            return new DeleteAccount(properties);
        };

        /**
         * Encodes the specified DeleteAccount message. Does not implicitly {@link server.DeleteAccount.verify|verify} messages.
         * @function encode
         * @memberof server.DeleteAccount
         * @static
         * @param {server.IDeleteAccount} message DeleteAccount message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        DeleteAccount.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.phone != null && Object.hasOwnProperty.call(message, "phone"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.phone);
            return writer;
        };

        /**
         * Encodes the specified DeleteAccount message, length delimited. Does not implicitly {@link server.DeleteAccount.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.DeleteAccount
         * @static
         * @param {server.IDeleteAccount} message DeleteAccount message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        DeleteAccount.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a DeleteAccount message from the specified reader or buffer.
         * @function decode
         * @memberof server.DeleteAccount
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.DeleteAccount} DeleteAccount
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        DeleteAccount.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.DeleteAccount();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.phone = reader.string();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a DeleteAccount message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.DeleteAccount
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.DeleteAccount} DeleteAccount
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        DeleteAccount.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a DeleteAccount message.
         * @function verify
         * @memberof server.DeleteAccount
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        DeleteAccount.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.phone != null && message.hasOwnProperty("phone"))
                if (!$util.isString(message.phone))
                    return "phone: string expected";
            return null;
        };

        /**
         * Creates a DeleteAccount message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.DeleteAccount
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.DeleteAccount} DeleteAccount
         */
        DeleteAccount.fromObject = function fromObject(object) {
            if (object instanceof $root.server.DeleteAccount)
                return object;
            var message = new $root.server.DeleteAccount();
            if (object.phone != null)
                message.phone = String(object.phone);
            return message;
        };

        /**
         * Creates a plain object from a DeleteAccount message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.DeleteAccount
         * @static
         * @param {server.DeleteAccount} message DeleteAccount
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        DeleteAccount.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults)
                object.phone = "";
            if (message.phone != null && message.hasOwnProperty("phone"))
                object.phone = message.phone;
            return object;
        };

        /**
         * Converts this DeleteAccount to JSON.
         * @function toJSON
         * @memberof server.DeleteAccount
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        DeleteAccount.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return DeleteAccount;
    })();

    server.EventData = (function() {

        /**
         * Properties of an EventData.
         * @memberof server
         * @interface IEventData
         * @property {number|Long|null} [uid] EventData uid
         * @property {server.Platform|null} [platform] EventData platform
         * @property {string|null} [version] EventData version
         * @property {number|Long|null} [timestampMs] EventData timestampMs
         * @property {server.IMediaUpload|null} [mediaUpload] EventData mediaUpload
         * @property {server.IMediaDownload|null} [mediaDownload] EventData mediaDownload
         * @property {server.IMediaComposeLoad|null} [mediaComposeLoad] EventData mediaComposeLoad
         * @property {server.IPushReceived|null} [pushReceived] EventData pushReceived
         */

        /**
         * Constructs a new EventData.
         * @memberof server
         * @classdesc Represents an EventData.
         * @implements IEventData
         * @constructor
         * @param {server.IEventData=} [properties] Properties to set
         */
        function EventData(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * EventData uid.
         * @member {number|Long} uid
         * @memberof server.EventData
         * @instance
         */
        EventData.prototype.uid = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

        /**
         * EventData platform.
         * @member {server.Platform} platform
         * @memberof server.EventData
         * @instance
         */
        EventData.prototype.platform = 0;

        /**
         * EventData version.
         * @member {string} version
         * @memberof server.EventData
         * @instance
         */
        EventData.prototype.version = "";

        /**
         * EventData timestampMs.
         * @member {number|Long} timestampMs
         * @memberof server.EventData
         * @instance
         */
        EventData.prototype.timestampMs = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

        /**
         * EventData mediaUpload.
         * @member {server.IMediaUpload|null|undefined} mediaUpload
         * @memberof server.EventData
         * @instance
         */
        EventData.prototype.mediaUpload = null;

        /**
         * EventData mediaDownload.
         * @member {server.IMediaDownload|null|undefined} mediaDownload
         * @memberof server.EventData
         * @instance
         */
        EventData.prototype.mediaDownload = null;

        /**
         * EventData mediaComposeLoad.
         * @member {server.IMediaComposeLoad|null|undefined} mediaComposeLoad
         * @memberof server.EventData
         * @instance
         */
        EventData.prototype.mediaComposeLoad = null;

        /**
         * EventData pushReceived.
         * @member {server.IPushReceived|null|undefined} pushReceived
         * @memberof server.EventData
         * @instance
         */
        EventData.prototype.pushReceived = null;

        // OneOf field names bound to virtual getters and setters
        var $oneOfFields;

        /**
         * EventData edata.
         * @member {"mediaUpload"|"mediaDownload"|"mediaComposeLoad"|"pushReceived"|undefined} edata
         * @memberof server.EventData
         * @instance
         */
        Object.defineProperty(EventData.prototype, "edata", {
            get: $util.oneOfGetter($oneOfFields = ["mediaUpload", "mediaDownload", "mediaComposeLoad", "pushReceived"]),
            set: $util.oneOfSetter($oneOfFields)
        });

        /**
         * Creates a new EventData instance using the specified properties.
         * @function create
         * @memberof server.EventData
         * @static
         * @param {server.IEventData=} [properties] Properties to set
         * @returns {server.EventData} EventData instance
         */
        EventData.create = function create(properties) {
            return new EventData(properties);
        };

        /**
         * Encodes the specified EventData message. Does not implicitly {@link server.EventData.verify|verify} messages.
         * @function encode
         * @memberof server.EventData
         * @static
         * @param {server.IEventData} message EventData message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        EventData.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.uid != null && Object.hasOwnProperty.call(message, "uid"))
                writer.uint32(/* id 1, wireType 0 =*/8).uint64(message.uid);
            if (message.platform != null && Object.hasOwnProperty.call(message, "platform"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.platform);
            if (message.version != null && Object.hasOwnProperty.call(message, "version"))
                writer.uint32(/* id 3, wireType 2 =*/26).string(message.version);
            if (message.timestampMs != null && Object.hasOwnProperty.call(message, "timestampMs"))
                writer.uint32(/* id 4, wireType 0 =*/32).uint64(message.timestampMs);
            if (message.mediaUpload != null && Object.hasOwnProperty.call(message, "mediaUpload"))
                $root.server.MediaUpload.encode(message.mediaUpload, writer.uint32(/* id 10, wireType 2 =*/82).fork()).ldelim();
            if (message.mediaDownload != null && Object.hasOwnProperty.call(message, "mediaDownload"))
                $root.server.MediaDownload.encode(message.mediaDownload, writer.uint32(/* id 11, wireType 2 =*/90).fork()).ldelim();
            if (message.mediaComposeLoad != null && Object.hasOwnProperty.call(message, "mediaComposeLoad"))
                $root.server.MediaComposeLoad.encode(message.mediaComposeLoad, writer.uint32(/* id 12, wireType 2 =*/98).fork()).ldelim();
            if (message.pushReceived != null && Object.hasOwnProperty.call(message, "pushReceived"))
                $root.server.PushReceived.encode(message.pushReceived, writer.uint32(/* id 13, wireType 2 =*/106).fork()).ldelim();
            return writer;
        };

        /**
         * Encodes the specified EventData message, length delimited. Does not implicitly {@link server.EventData.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.EventData
         * @static
         * @param {server.IEventData} message EventData message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        EventData.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes an EventData message from the specified reader or buffer.
         * @function decode
         * @memberof server.EventData
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.EventData} EventData
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        EventData.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.EventData();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.uid = reader.uint64();
                    break;
                case 2:
                    message.platform = reader.int32();
                    break;
                case 3:
                    message.version = reader.string();
                    break;
                case 4:
                    message.timestampMs = reader.uint64();
                    break;
                case 10:
                    message.mediaUpload = $root.server.MediaUpload.decode(reader, reader.uint32());
                    break;
                case 11:
                    message.mediaDownload = $root.server.MediaDownload.decode(reader, reader.uint32());
                    break;
                case 12:
                    message.mediaComposeLoad = $root.server.MediaComposeLoad.decode(reader, reader.uint32());
                    break;
                case 13:
                    message.pushReceived = $root.server.PushReceived.decode(reader, reader.uint32());
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes an EventData message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.EventData
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.EventData} EventData
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        EventData.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies an EventData message.
         * @function verify
         * @memberof server.EventData
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        EventData.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            var properties = {};
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (!$util.isInteger(message.uid) && !(message.uid && $util.isInteger(message.uid.low) && $util.isInteger(message.uid.high)))
                    return "uid: integer|Long expected";
            if (message.platform != null && message.hasOwnProperty("platform"))
                switch (message.platform) {
                default:
                    return "platform: enum value expected";
                case 0:
                case 1:
                case 2:
                    break;
                }
            if (message.version != null && message.hasOwnProperty("version"))
                if (!$util.isString(message.version))
                    return "version: string expected";
            if (message.timestampMs != null && message.hasOwnProperty("timestampMs"))
                if (!$util.isInteger(message.timestampMs) && !(message.timestampMs && $util.isInteger(message.timestampMs.low) && $util.isInteger(message.timestampMs.high)))
                    return "timestampMs: integer|Long expected";
            if (message.mediaUpload != null && message.hasOwnProperty("mediaUpload")) {
                properties.edata = 1;
                {
                    var error = $root.server.MediaUpload.verify(message.mediaUpload);
                    if (error)
                        return "mediaUpload." + error;
                }
            }
            if (message.mediaDownload != null && message.hasOwnProperty("mediaDownload")) {
                if (properties.edata === 1)
                    return "edata: multiple values";
                properties.edata = 1;
                {
                    var error = $root.server.MediaDownload.verify(message.mediaDownload);
                    if (error)
                        return "mediaDownload." + error;
                }
            }
            if (message.mediaComposeLoad != null && message.hasOwnProperty("mediaComposeLoad")) {
                if (properties.edata === 1)
                    return "edata: multiple values";
                properties.edata = 1;
                {
                    var error = $root.server.MediaComposeLoad.verify(message.mediaComposeLoad);
                    if (error)
                        return "mediaComposeLoad." + error;
                }
            }
            if (message.pushReceived != null && message.hasOwnProperty("pushReceived")) {
                if (properties.edata === 1)
                    return "edata: multiple values";
                properties.edata = 1;
                {
                    var error = $root.server.PushReceived.verify(message.pushReceived);
                    if (error)
                        return "pushReceived." + error;
                }
            }
            return null;
        };

        /**
         * Creates an EventData message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.EventData
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.EventData} EventData
         */
        EventData.fromObject = function fromObject(object) {
            if (object instanceof $root.server.EventData)
                return object;
            var message = new $root.server.EventData();
            if (object.uid != null)
                if ($util.Long)
                    (message.uid = $util.Long.fromValue(object.uid)).unsigned = true;
                else if (typeof object.uid === "string")
                    message.uid = parseInt(object.uid, 10);
                else if (typeof object.uid === "number")
                    message.uid = object.uid;
                else if (typeof object.uid === "object")
                    message.uid = new $util.LongBits(object.uid.low >>> 0, object.uid.high >>> 0).toNumber(true);
            switch (object.platform) {
            case "UNKNOWN":
            case 0:
                message.platform = 0;
                break;
            case "IOS":
            case 1:
                message.platform = 1;
                break;
            case "ANDROID":
            case 2:
                message.platform = 2;
                break;
            }
            if (object.version != null)
                message.version = String(object.version);
            if (object.timestampMs != null)
                if ($util.Long)
                    (message.timestampMs = $util.Long.fromValue(object.timestampMs)).unsigned = true;
                else if (typeof object.timestampMs === "string")
                    message.timestampMs = parseInt(object.timestampMs, 10);
                else if (typeof object.timestampMs === "number")
                    message.timestampMs = object.timestampMs;
                else if (typeof object.timestampMs === "object")
                    message.timestampMs = new $util.LongBits(object.timestampMs.low >>> 0, object.timestampMs.high >>> 0).toNumber(true);
            if (object.mediaUpload != null) {
                if (typeof object.mediaUpload !== "object")
                    throw TypeError(".server.EventData.mediaUpload: object expected");
                message.mediaUpload = $root.server.MediaUpload.fromObject(object.mediaUpload);
            }
            if (object.mediaDownload != null) {
                if (typeof object.mediaDownload !== "object")
                    throw TypeError(".server.EventData.mediaDownload: object expected");
                message.mediaDownload = $root.server.MediaDownload.fromObject(object.mediaDownload);
            }
            if (object.mediaComposeLoad != null) {
                if (typeof object.mediaComposeLoad !== "object")
                    throw TypeError(".server.EventData.mediaComposeLoad: object expected");
                message.mediaComposeLoad = $root.server.MediaComposeLoad.fromObject(object.mediaComposeLoad);
            }
            if (object.pushReceived != null) {
                if (typeof object.pushReceived !== "object")
                    throw TypeError(".server.EventData.pushReceived: object expected");
                message.pushReceived = $root.server.PushReceived.fromObject(object.pushReceived);
            }
            return message;
        };

        /**
         * Creates a plain object from an EventData message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.EventData
         * @static
         * @param {server.EventData} message EventData
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        EventData.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                if ($util.Long) {
                    var long = new $util.Long(0, 0, true);
                    object.uid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.uid = options.longs === String ? "0" : 0;
                object.platform = options.enums === String ? "UNKNOWN" : 0;
                object.version = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, true);
                    object.timestampMs = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.timestampMs = options.longs === String ? "0" : 0;
            }
            if (message.uid != null && message.hasOwnProperty("uid"))
                if (typeof message.uid === "number")
                    object.uid = options.longs === String ? String(message.uid) : message.uid;
                else
                    object.uid = options.longs === String ? $util.Long.prototype.toString.call(message.uid) : options.longs === Number ? new $util.LongBits(message.uid.low >>> 0, message.uid.high >>> 0).toNumber(true) : message.uid;
            if (message.platform != null && message.hasOwnProperty("platform"))
                object.platform = options.enums === String ? $root.server.Platform[message.platform] : message.platform;
            if (message.version != null && message.hasOwnProperty("version"))
                object.version = message.version;
            if (message.timestampMs != null && message.hasOwnProperty("timestampMs"))
                if (typeof message.timestampMs === "number")
                    object.timestampMs = options.longs === String ? String(message.timestampMs) : message.timestampMs;
                else
                    object.timestampMs = options.longs === String ? $util.Long.prototype.toString.call(message.timestampMs) : options.longs === Number ? new $util.LongBits(message.timestampMs.low >>> 0, message.timestampMs.high >>> 0).toNumber(true) : message.timestampMs;
            if (message.mediaUpload != null && message.hasOwnProperty("mediaUpload")) {
                object.mediaUpload = $root.server.MediaUpload.toObject(message.mediaUpload, options);
                if (options.oneofs)
                    object.edata = "mediaUpload";
            }
            if (message.mediaDownload != null && message.hasOwnProperty("mediaDownload")) {
                object.mediaDownload = $root.server.MediaDownload.toObject(message.mediaDownload, options);
                if (options.oneofs)
                    object.edata = "mediaDownload";
            }
            if (message.mediaComposeLoad != null && message.hasOwnProperty("mediaComposeLoad")) {
                object.mediaComposeLoad = $root.server.MediaComposeLoad.toObject(message.mediaComposeLoad, options);
                if (options.oneofs)
                    object.edata = "mediaComposeLoad";
            }
            if (message.pushReceived != null && message.hasOwnProperty("pushReceived")) {
                object.pushReceived = $root.server.PushReceived.toObject(message.pushReceived, options);
                if (options.oneofs)
                    object.edata = "pushReceived";
            }
            return object;
        };

        /**
         * Converts this EventData to JSON.
         * @function toJSON
         * @memberof server.EventData
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        EventData.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return EventData;
    })();

    /**
     * Platform enum.
     * @name server.Platform
     * @enum {number}
     * @property {number} UNKNOWN=0 UNKNOWN value
     * @property {number} IOS=1 IOS value
     * @property {number} ANDROID=2 ANDROID value
     */
    server.Platform = (function() {
        var valuesById = {}, values = Object.create(valuesById);
        values[valuesById[0] = "UNKNOWN"] = 0;
        values[valuesById[1] = "IOS"] = 1;
        values[valuesById[2] = "ANDROID"] = 2;
        return values;
    })();

    server.MediaUpload = (function() {

        /**
         * Properties of a MediaUpload.
         * @memberof server
         * @interface IMediaUpload
         * @property {string|null} [id] MediaUpload id
         * @property {server.MediaUpload.Type|null} [type] MediaUpload type
         * @property {number|null} [durationMs] MediaUpload durationMs
         * @property {number|null} [numPhotos] MediaUpload numPhotos
         * @property {number|null} [numVideos] MediaUpload numVideos
         * @property {number|null} [totalSize] MediaUpload totalSize
         * @property {server.MediaUpload.Status|null} [status] MediaUpload status
         * @property {number|null} [retryCount] MediaUpload retryCount
         */

        /**
         * Constructs a new MediaUpload.
         * @memberof server
         * @classdesc Represents a MediaUpload.
         * @implements IMediaUpload
         * @constructor
         * @param {server.IMediaUpload=} [properties] Properties to set
         */
        function MediaUpload(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * MediaUpload id.
         * @member {string} id
         * @memberof server.MediaUpload
         * @instance
         */
        MediaUpload.prototype.id = "";

        /**
         * MediaUpload type.
         * @member {server.MediaUpload.Type} type
         * @memberof server.MediaUpload
         * @instance
         */
        MediaUpload.prototype.type = 0;

        /**
         * MediaUpload durationMs.
         * @member {number} durationMs
         * @memberof server.MediaUpload
         * @instance
         */
        MediaUpload.prototype.durationMs = 0;

        /**
         * MediaUpload numPhotos.
         * @member {number} numPhotos
         * @memberof server.MediaUpload
         * @instance
         */
        MediaUpload.prototype.numPhotos = 0;

        /**
         * MediaUpload numVideos.
         * @member {number} numVideos
         * @memberof server.MediaUpload
         * @instance
         */
        MediaUpload.prototype.numVideos = 0;

        /**
         * MediaUpload totalSize.
         * @member {number} totalSize
         * @memberof server.MediaUpload
         * @instance
         */
        MediaUpload.prototype.totalSize = 0;

        /**
         * MediaUpload status.
         * @member {server.MediaUpload.Status} status
         * @memberof server.MediaUpload
         * @instance
         */
        MediaUpload.prototype.status = 0;

        /**
         * MediaUpload retryCount.
         * @member {number} retryCount
         * @memberof server.MediaUpload
         * @instance
         */
        MediaUpload.prototype.retryCount = 0;

        /**
         * Creates a new MediaUpload instance using the specified properties.
         * @function create
         * @memberof server.MediaUpload
         * @static
         * @param {server.IMediaUpload=} [properties] Properties to set
         * @returns {server.MediaUpload} MediaUpload instance
         */
        MediaUpload.create = function create(properties) {
            return new MediaUpload(properties);
        };

        /**
         * Encodes the specified MediaUpload message. Does not implicitly {@link server.MediaUpload.verify|verify} messages.
         * @function encode
         * @memberof server.MediaUpload
         * @static
         * @param {server.IMediaUpload} message MediaUpload message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MediaUpload.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.type);
            if (message.durationMs != null && Object.hasOwnProperty.call(message, "durationMs"))
                writer.uint32(/* id 3, wireType 0 =*/24).uint32(message.durationMs);
            if (message.numPhotos != null && Object.hasOwnProperty.call(message, "numPhotos"))
                writer.uint32(/* id 4, wireType 0 =*/32).uint32(message.numPhotos);
            if (message.numVideos != null && Object.hasOwnProperty.call(message, "numVideos"))
                writer.uint32(/* id 5, wireType 0 =*/40).uint32(message.numVideos);
            if (message.totalSize != null && Object.hasOwnProperty.call(message, "totalSize"))
                writer.uint32(/* id 6, wireType 0 =*/48).uint32(message.totalSize);
            if (message.status != null && Object.hasOwnProperty.call(message, "status"))
                writer.uint32(/* id 7, wireType 0 =*/56).int32(message.status);
            if (message.retryCount != null && Object.hasOwnProperty.call(message, "retryCount"))
                writer.uint32(/* id 8, wireType 0 =*/64).uint32(message.retryCount);
            return writer;
        };

        /**
         * Encodes the specified MediaUpload message, length delimited. Does not implicitly {@link server.MediaUpload.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.MediaUpload
         * @static
         * @param {server.IMediaUpload} message MediaUpload message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MediaUpload.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a MediaUpload message from the specified reader or buffer.
         * @function decode
         * @memberof server.MediaUpload
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.MediaUpload} MediaUpload
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        MediaUpload.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.MediaUpload();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.type = reader.int32();
                    break;
                case 3:
                    message.durationMs = reader.uint32();
                    break;
                case 4:
                    message.numPhotos = reader.uint32();
                    break;
                case 5:
                    message.numVideos = reader.uint32();
                    break;
                case 6:
                    message.totalSize = reader.uint32();
                    break;
                case 7:
                    message.status = reader.int32();
                    break;
                case 8:
                    message.retryCount = reader.uint32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a MediaUpload message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.MediaUpload
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.MediaUpload} MediaUpload
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        MediaUpload.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a MediaUpload message.
         * @function verify
         * @memberof server.MediaUpload
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        MediaUpload.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.type != null && message.hasOwnProperty("type"))
                switch (message.type) {
                default:
                    return "type: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.durationMs != null && message.hasOwnProperty("durationMs"))
                if (!$util.isInteger(message.durationMs))
                    return "durationMs: integer expected";
            if (message.numPhotos != null && message.hasOwnProperty("numPhotos"))
                if (!$util.isInteger(message.numPhotos))
                    return "numPhotos: integer expected";
            if (message.numVideos != null && message.hasOwnProperty("numVideos"))
                if (!$util.isInteger(message.numVideos))
                    return "numVideos: integer expected";
            if (message.totalSize != null && message.hasOwnProperty("totalSize"))
                if (!$util.isInteger(message.totalSize))
                    return "totalSize: integer expected";
            if (message.status != null && message.hasOwnProperty("status"))
                switch (message.status) {
                default:
                    return "status: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.retryCount != null && message.hasOwnProperty("retryCount"))
                if (!$util.isInteger(message.retryCount))
                    return "retryCount: integer expected";
            return null;
        };

        /**
         * Creates a MediaUpload message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.MediaUpload
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.MediaUpload} MediaUpload
         */
        MediaUpload.fromObject = function fromObject(object) {
            if (object instanceof $root.server.MediaUpload)
                return object;
            var message = new $root.server.MediaUpload();
            if (object.id != null)
                message.id = String(object.id);
            switch (object.type) {
            case "POST":
            case 0:
                message.type = 0;
                break;
            case "MESSAGE":
            case 1:
                message.type = 1;
                break;
            }
            if (object.durationMs != null)
                message.durationMs = object.durationMs >>> 0;
            if (object.numPhotos != null)
                message.numPhotos = object.numPhotos >>> 0;
            if (object.numVideos != null)
                message.numVideos = object.numVideos >>> 0;
            if (object.totalSize != null)
                message.totalSize = object.totalSize >>> 0;
            switch (object.status) {
            case "OK":
            case 0:
                message.status = 0;
                break;
            case "FAIL":
            case 1:
                message.status = 1;
                break;
            }
            if (object.retryCount != null)
                message.retryCount = object.retryCount >>> 0;
            return message;
        };

        /**
         * Creates a plain object from a MediaUpload message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.MediaUpload
         * @static
         * @param {server.MediaUpload} message MediaUpload
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        MediaUpload.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.type = options.enums === String ? "POST" : 0;
                object.durationMs = 0;
                object.numPhotos = 0;
                object.numVideos = 0;
                object.totalSize = 0;
                object.status = options.enums === String ? "OK" : 0;
                object.retryCount = 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.server.MediaUpload.Type[message.type] : message.type;
            if (message.durationMs != null && message.hasOwnProperty("durationMs"))
                object.durationMs = message.durationMs;
            if (message.numPhotos != null && message.hasOwnProperty("numPhotos"))
                object.numPhotos = message.numPhotos;
            if (message.numVideos != null && message.hasOwnProperty("numVideos"))
                object.numVideos = message.numVideos;
            if (message.totalSize != null && message.hasOwnProperty("totalSize"))
                object.totalSize = message.totalSize;
            if (message.status != null && message.hasOwnProperty("status"))
                object.status = options.enums === String ? $root.server.MediaUpload.Status[message.status] : message.status;
            if (message.retryCount != null && message.hasOwnProperty("retryCount"))
                object.retryCount = message.retryCount;
            return object;
        };

        /**
         * Converts this MediaUpload to JSON.
         * @function toJSON
         * @memberof server.MediaUpload
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        MediaUpload.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Type enum.
         * @name server.MediaUpload.Type
         * @enum {number}
         * @property {number} POST=0 POST value
         * @property {number} MESSAGE=1 MESSAGE value
         */
        MediaUpload.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "POST"] = 0;
            values[valuesById[1] = "MESSAGE"] = 1;
            return values;
        })();

        /**
         * Status enum.
         * @name server.MediaUpload.Status
         * @enum {number}
         * @property {number} OK=0 OK value
         * @property {number} FAIL=1 FAIL value
         */
        MediaUpload.Status = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "OK"] = 0;
            values[valuesById[1] = "FAIL"] = 1;
            return values;
        })();

        return MediaUpload;
    })();

    server.MediaDownload = (function() {

        /**
         * Properties of a MediaDownload.
         * @memberof server
         * @interface IMediaDownload
         * @property {string|null} [id] MediaDownload id
         * @property {server.MediaDownload.Type|null} [type] MediaDownload type
         * @property {number|null} [durationMs] MediaDownload durationMs
         * @property {number|null} [numPhotos] MediaDownload numPhotos
         * @property {number|null} [numVideos] MediaDownload numVideos
         * @property {number|null} [totalSize] MediaDownload totalSize
         * @property {server.MediaDownload.Status|null} [status] MediaDownload status
         * @property {number|null} [retryCount] MediaDownload retryCount
         */

        /**
         * Constructs a new MediaDownload.
         * @memberof server
         * @classdesc Represents a MediaDownload.
         * @implements IMediaDownload
         * @constructor
         * @param {server.IMediaDownload=} [properties] Properties to set
         */
        function MediaDownload(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * MediaDownload id.
         * @member {string} id
         * @memberof server.MediaDownload
         * @instance
         */
        MediaDownload.prototype.id = "";

        /**
         * MediaDownload type.
         * @member {server.MediaDownload.Type} type
         * @memberof server.MediaDownload
         * @instance
         */
        MediaDownload.prototype.type = 0;

        /**
         * MediaDownload durationMs.
         * @member {number} durationMs
         * @memberof server.MediaDownload
         * @instance
         */
        MediaDownload.prototype.durationMs = 0;

        /**
         * MediaDownload numPhotos.
         * @member {number} numPhotos
         * @memberof server.MediaDownload
         * @instance
         */
        MediaDownload.prototype.numPhotos = 0;

        /**
         * MediaDownload numVideos.
         * @member {number} numVideos
         * @memberof server.MediaDownload
         * @instance
         */
        MediaDownload.prototype.numVideos = 0;

        /**
         * MediaDownload totalSize.
         * @member {number} totalSize
         * @memberof server.MediaDownload
         * @instance
         */
        MediaDownload.prototype.totalSize = 0;

        /**
         * MediaDownload status.
         * @member {server.MediaDownload.Status} status
         * @memberof server.MediaDownload
         * @instance
         */
        MediaDownload.prototype.status = 0;

        /**
         * MediaDownload retryCount.
         * @member {number} retryCount
         * @memberof server.MediaDownload
         * @instance
         */
        MediaDownload.prototype.retryCount = 0;

        /**
         * Creates a new MediaDownload instance using the specified properties.
         * @function create
         * @memberof server.MediaDownload
         * @static
         * @param {server.IMediaDownload=} [properties] Properties to set
         * @returns {server.MediaDownload} MediaDownload instance
         */
        MediaDownload.create = function create(properties) {
            return new MediaDownload(properties);
        };

        /**
         * Encodes the specified MediaDownload message. Does not implicitly {@link server.MediaDownload.verify|verify} messages.
         * @function encode
         * @memberof server.MediaDownload
         * @static
         * @param {server.IMediaDownload} message MediaDownload message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MediaDownload.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.type != null && Object.hasOwnProperty.call(message, "type"))
                writer.uint32(/* id 2, wireType 0 =*/16).int32(message.type);
            if (message.durationMs != null && Object.hasOwnProperty.call(message, "durationMs"))
                writer.uint32(/* id 3, wireType 0 =*/24).uint32(message.durationMs);
            if (message.numPhotos != null && Object.hasOwnProperty.call(message, "numPhotos"))
                writer.uint32(/* id 4, wireType 0 =*/32).uint32(message.numPhotos);
            if (message.numVideos != null && Object.hasOwnProperty.call(message, "numVideos"))
                writer.uint32(/* id 5, wireType 0 =*/40).uint32(message.numVideos);
            if (message.totalSize != null && Object.hasOwnProperty.call(message, "totalSize"))
                writer.uint32(/* id 6, wireType 0 =*/48).uint32(message.totalSize);
            if (message.status != null && Object.hasOwnProperty.call(message, "status"))
                writer.uint32(/* id 7, wireType 0 =*/56).int32(message.status);
            if (message.retryCount != null && Object.hasOwnProperty.call(message, "retryCount"))
                writer.uint32(/* id 8, wireType 0 =*/64).uint32(message.retryCount);
            return writer;
        };

        /**
         * Encodes the specified MediaDownload message, length delimited. Does not implicitly {@link server.MediaDownload.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.MediaDownload
         * @static
         * @param {server.IMediaDownload} message MediaDownload message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MediaDownload.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a MediaDownload message from the specified reader or buffer.
         * @function decode
         * @memberof server.MediaDownload
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.MediaDownload} MediaDownload
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        MediaDownload.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.MediaDownload();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.type = reader.int32();
                    break;
                case 3:
                    message.durationMs = reader.uint32();
                    break;
                case 4:
                    message.numPhotos = reader.uint32();
                    break;
                case 5:
                    message.numVideos = reader.uint32();
                    break;
                case 6:
                    message.totalSize = reader.uint32();
                    break;
                case 7:
                    message.status = reader.int32();
                    break;
                case 8:
                    message.retryCount = reader.uint32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a MediaDownload message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.MediaDownload
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.MediaDownload} MediaDownload
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        MediaDownload.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a MediaDownload message.
         * @function verify
         * @memberof server.MediaDownload
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        MediaDownload.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.type != null && message.hasOwnProperty("type"))
                switch (message.type) {
                default:
                    return "type: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.durationMs != null && message.hasOwnProperty("durationMs"))
                if (!$util.isInteger(message.durationMs))
                    return "durationMs: integer expected";
            if (message.numPhotos != null && message.hasOwnProperty("numPhotos"))
                if (!$util.isInteger(message.numPhotos))
                    return "numPhotos: integer expected";
            if (message.numVideos != null && message.hasOwnProperty("numVideos"))
                if (!$util.isInteger(message.numVideos))
                    return "numVideos: integer expected";
            if (message.totalSize != null && message.hasOwnProperty("totalSize"))
                if (!$util.isInteger(message.totalSize))
                    return "totalSize: integer expected";
            if (message.status != null && message.hasOwnProperty("status"))
                switch (message.status) {
                default:
                    return "status: enum value expected";
                case 0:
                case 1:
                    break;
                }
            if (message.retryCount != null && message.hasOwnProperty("retryCount"))
                if (!$util.isInteger(message.retryCount))
                    return "retryCount: integer expected";
            return null;
        };

        /**
         * Creates a MediaDownload message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.MediaDownload
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.MediaDownload} MediaDownload
         */
        MediaDownload.fromObject = function fromObject(object) {
            if (object instanceof $root.server.MediaDownload)
                return object;
            var message = new $root.server.MediaDownload();
            if (object.id != null)
                message.id = String(object.id);
            switch (object.type) {
            case "POST":
            case 0:
                message.type = 0;
                break;
            case "MESSAGE":
            case 1:
                message.type = 1;
                break;
            }
            if (object.durationMs != null)
                message.durationMs = object.durationMs >>> 0;
            if (object.numPhotos != null)
                message.numPhotos = object.numPhotos >>> 0;
            if (object.numVideos != null)
                message.numVideos = object.numVideos >>> 0;
            if (object.totalSize != null)
                message.totalSize = object.totalSize >>> 0;
            switch (object.status) {
            case "OK":
            case 0:
                message.status = 0;
                break;
            case "FAIL":
            case 1:
                message.status = 1;
                break;
            }
            if (object.retryCount != null)
                message.retryCount = object.retryCount >>> 0;
            return message;
        };

        /**
         * Creates a plain object from a MediaDownload message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.MediaDownload
         * @static
         * @param {server.MediaDownload} message MediaDownload
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        MediaDownload.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                object.type = options.enums === String ? "POST" : 0;
                object.durationMs = 0;
                object.numPhotos = 0;
                object.numVideos = 0;
                object.totalSize = 0;
                object.status = options.enums === String ? "OK" : 0;
                object.retryCount = 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.type != null && message.hasOwnProperty("type"))
                object.type = options.enums === String ? $root.server.MediaDownload.Type[message.type] : message.type;
            if (message.durationMs != null && message.hasOwnProperty("durationMs"))
                object.durationMs = message.durationMs;
            if (message.numPhotos != null && message.hasOwnProperty("numPhotos"))
                object.numPhotos = message.numPhotos;
            if (message.numVideos != null && message.hasOwnProperty("numVideos"))
                object.numVideos = message.numVideos;
            if (message.totalSize != null && message.hasOwnProperty("totalSize"))
                object.totalSize = message.totalSize;
            if (message.status != null && message.hasOwnProperty("status"))
                object.status = options.enums === String ? $root.server.MediaDownload.Status[message.status] : message.status;
            if (message.retryCount != null && message.hasOwnProperty("retryCount"))
                object.retryCount = message.retryCount;
            return object;
        };

        /**
         * Converts this MediaDownload to JSON.
         * @function toJSON
         * @memberof server.MediaDownload
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        MediaDownload.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        /**
         * Type enum.
         * @name server.MediaDownload.Type
         * @enum {number}
         * @property {number} POST=0 POST value
         * @property {number} MESSAGE=1 MESSAGE value
         */
        MediaDownload.Type = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "POST"] = 0;
            values[valuesById[1] = "MESSAGE"] = 1;
            return values;
        })();

        /**
         * Status enum.
         * @name server.MediaDownload.Status
         * @enum {number}
         * @property {number} OK=0 OK value
         * @property {number} FAIL=1 FAIL value
         */
        MediaDownload.Status = (function() {
            var valuesById = {}, values = Object.create(valuesById);
            values[valuesById[0] = "OK"] = 0;
            values[valuesById[1] = "FAIL"] = 1;
            return values;
        })();

        return MediaDownload;
    })();

    server.MediaComposeLoad = (function() {

        /**
         * Properties of a MediaComposeLoad.
         * @memberof server
         * @interface IMediaComposeLoad
         * @property {number|null} [durationMs] MediaComposeLoad durationMs
         * @property {number|null} [numPhotos] MediaComposeLoad numPhotos
         * @property {number|null} [numVideos] MediaComposeLoad numVideos
         * @property {number|null} [totalSize] MediaComposeLoad totalSize
         */

        /**
         * Constructs a new MediaComposeLoad.
         * @memberof server
         * @classdesc Represents a MediaComposeLoad.
         * @implements IMediaComposeLoad
         * @constructor
         * @param {server.IMediaComposeLoad=} [properties] Properties to set
         */
        function MediaComposeLoad(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * MediaComposeLoad durationMs.
         * @member {number} durationMs
         * @memberof server.MediaComposeLoad
         * @instance
         */
        MediaComposeLoad.prototype.durationMs = 0;

        /**
         * MediaComposeLoad numPhotos.
         * @member {number} numPhotos
         * @memberof server.MediaComposeLoad
         * @instance
         */
        MediaComposeLoad.prototype.numPhotos = 0;

        /**
         * MediaComposeLoad numVideos.
         * @member {number} numVideos
         * @memberof server.MediaComposeLoad
         * @instance
         */
        MediaComposeLoad.prototype.numVideos = 0;

        /**
         * MediaComposeLoad totalSize.
         * @member {number} totalSize
         * @memberof server.MediaComposeLoad
         * @instance
         */
        MediaComposeLoad.prototype.totalSize = 0;

        /**
         * Creates a new MediaComposeLoad instance using the specified properties.
         * @function create
         * @memberof server.MediaComposeLoad
         * @static
         * @param {server.IMediaComposeLoad=} [properties] Properties to set
         * @returns {server.MediaComposeLoad} MediaComposeLoad instance
         */
        MediaComposeLoad.create = function create(properties) {
            return new MediaComposeLoad(properties);
        };

        /**
         * Encodes the specified MediaComposeLoad message. Does not implicitly {@link server.MediaComposeLoad.verify|verify} messages.
         * @function encode
         * @memberof server.MediaComposeLoad
         * @static
         * @param {server.IMediaComposeLoad} message MediaComposeLoad message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MediaComposeLoad.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.durationMs != null && Object.hasOwnProperty.call(message, "durationMs"))
                writer.uint32(/* id 1, wireType 0 =*/8).uint32(message.durationMs);
            if (message.numPhotos != null && Object.hasOwnProperty.call(message, "numPhotos"))
                writer.uint32(/* id 2, wireType 0 =*/16).uint32(message.numPhotos);
            if (message.numVideos != null && Object.hasOwnProperty.call(message, "numVideos"))
                writer.uint32(/* id 3, wireType 0 =*/24).uint32(message.numVideos);
            if (message.totalSize != null && Object.hasOwnProperty.call(message, "totalSize"))
                writer.uint32(/* id 4, wireType 0 =*/32).uint32(message.totalSize);
            return writer;
        };

        /**
         * Encodes the specified MediaComposeLoad message, length delimited. Does not implicitly {@link server.MediaComposeLoad.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.MediaComposeLoad
         * @static
         * @param {server.IMediaComposeLoad} message MediaComposeLoad message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        MediaComposeLoad.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a MediaComposeLoad message from the specified reader or buffer.
         * @function decode
         * @memberof server.MediaComposeLoad
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.MediaComposeLoad} MediaComposeLoad
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        MediaComposeLoad.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.MediaComposeLoad();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.durationMs = reader.uint32();
                    break;
                case 2:
                    message.numPhotos = reader.uint32();
                    break;
                case 3:
                    message.numVideos = reader.uint32();
                    break;
                case 4:
                    message.totalSize = reader.uint32();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a MediaComposeLoad message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.MediaComposeLoad
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.MediaComposeLoad} MediaComposeLoad
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        MediaComposeLoad.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a MediaComposeLoad message.
         * @function verify
         * @memberof server.MediaComposeLoad
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        MediaComposeLoad.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.durationMs != null && message.hasOwnProperty("durationMs"))
                if (!$util.isInteger(message.durationMs))
                    return "durationMs: integer expected";
            if (message.numPhotos != null && message.hasOwnProperty("numPhotos"))
                if (!$util.isInteger(message.numPhotos))
                    return "numPhotos: integer expected";
            if (message.numVideos != null && message.hasOwnProperty("numVideos"))
                if (!$util.isInteger(message.numVideos))
                    return "numVideos: integer expected";
            if (message.totalSize != null && message.hasOwnProperty("totalSize"))
                if (!$util.isInteger(message.totalSize))
                    return "totalSize: integer expected";
            return null;
        };

        /**
         * Creates a MediaComposeLoad message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.MediaComposeLoad
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.MediaComposeLoad} MediaComposeLoad
         */
        MediaComposeLoad.fromObject = function fromObject(object) {
            if (object instanceof $root.server.MediaComposeLoad)
                return object;
            var message = new $root.server.MediaComposeLoad();
            if (object.durationMs != null)
                message.durationMs = object.durationMs >>> 0;
            if (object.numPhotos != null)
                message.numPhotos = object.numPhotos >>> 0;
            if (object.numVideos != null)
                message.numVideos = object.numVideos >>> 0;
            if (object.totalSize != null)
                message.totalSize = object.totalSize >>> 0;
            return message;
        };

        /**
         * Creates a plain object from a MediaComposeLoad message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.MediaComposeLoad
         * @static
         * @param {server.MediaComposeLoad} message MediaComposeLoad
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        MediaComposeLoad.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.durationMs = 0;
                object.numPhotos = 0;
                object.numVideos = 0;
                object.totalSize = 0;
            }
            if (message.durationMs != null && message.hasOwnProperty("durationMs"))
                object.durationMs = message.durationMs;
            if (message.numPhotos != null && message.hasOwnProperty("numPhotos"))
                object.numPhotos = message.numPhotos;
            if (message.numVideos != null && message.hasOwnProperty("numVideos"))
                object.numVideos = message.numVideos;
            if (message.totalSize != null && message.hasOwnProperty("totalSize"))
                object.totalSize = message.totalSize;
            return object;
        };

        /**
         * Converts this MediaComposeLoad to JSON.
         * @function toJSON
         * @memberof server.MediaComposeLoad
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        MediaComposeLoad.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return MediaComposeLoad;
    })();

    server.PushReceived = (function() {

        /**
         * Properties of a PushReceived.
         * @memberof server
         * @interface IPushReceived
         * @property {string|null} [id] PushReceived id
         * @property {number|Long|null} [clientTimestamp] PushReceived clientTimestamp
         */

        /**
         * Constructs a new PushReceived.
         * @memberof server
         * @classdesc Represents a PushReceived.
         * @implements IPushReceived
         * @constructor
         * @param {server.IPushReceived=} [properties] Properties to set
         */
        function PushReceived(properties) {
            if (properties)
                for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                    if (properties[keys[i]] != null)
                        this[keys[i]] = properties[keys[i]];
        }

        /**
         * PushReceived id.
         * @member {string} id
         * @memberof server.PushReceived
         * @instance
         */
        PushReceived.prototype.id = "";

        /**
         * PushReceived clientTimestamp.
         * @member {number|Long} clientTimestamp
         * @memberof server.PushReceived
         * @instance
         */
        PushReceived.prototype.clientTimestamp = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

        /**
         * Creates a new PushReceived instance using the specified properties.
         * @function create
         * @memberof server.PushReceived
         * @static
         * @param {server.IPushReceived=} [properties] Properties to set
         * @returns {server.PushReceived} PushReceived instance
         */
        PushReceived.create = function create(properties) {
            return new PushReceived(properties);
        };

        /**
         * Encodes the specified PushReceived message. Does not implicitly {@link server.PushReceived.verify|verify} messages.
         * @function encode
         * @memberof server.PushReceived
         * @static
         * @param {server.IPushReceived} message PushReceived message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PushReceived.encode = function encode(message, writer) {
            if (!writer)
                writer = $Writer.create();
            if (message.id != null && Object.hasOwnProperty.call(message, "id"))
                writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
            if (message.clientTimestamp != null && Object.hasOwnProperty.call(message, "clientTimestamp"))
                writer.uint32(/* id 2, wireType 0 =*/16).uint64(message.clientTimestamp);
            return writer;
        };

        /**
         * Encodes the specified PushReceived message, length delimited. Does not implicitly {@link server.PushReceived.verify|verify} messages.
         * @function encodeDelimited
         * @memberof server.PushReceived
         * @static
         * @param {server.IPushReceived} message PushReceived message or plain object to encode
         * @param {$protobuf.Writer} [writer] Writer to encode to
         * @returns {$protobuf.Writer} Writer
         */
        PushReceived.encodeDelimited = function encodeDelimited(message, writer) {
            return this.encode(message, writer).ldelim();
        };

        /**
         * Decodes a PushReceived message from the specified reader or buffer.
         * @function decode
         * @memberof server.PushReceived
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @param {number} [length] Message length if known beforehand
         * @returns {server.PushReceived} PushReceived
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PushReceived.decode = function decode(reader, length) {
            if (!(reader instanceof $Reader))
                reader = $Reader.create(reader);
            var end = length === undefined ? reader.len : reader.pos + length, message = new $root.server.PushReceived();
            while (reader.pos < end) {
                var tag = reader.uint32();
                switch (tag >>> 3) {
                case 1:
                    message.id = reader.string();
                    break;
                case 2:
                    message.clientTimestamp = reader.uint64();
                    break;
                default:
                    reader.skipType(tag & 7);
                    break;
                }
            }
            return message;
        };

        /**
         * Decodes a PushReceived message from the specified reader or buffer, length delimited.
         * @function decodeDelimited
         * @memberof server.PushReceived
         * @static
         * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
         * @returns {server.PushReceived} PushReceived
         * @throws {Error} If the payload is not a reader or valid buffer
         * @throws {$protobuf.util.ProtocolError} If required fields are missing
         */
        PushReceived.decodeDelimited = function decodeDelimited(reader) {
            if (!(reader instanceof $Reader))
                reader = new $Reader(reader);
            return this.decode(reader, reader.uint32());
        };

        /**
         * Verifies a PushReceived message.
         * @function verify
         * @memberof server.PushReceived
         * @static
         * @param {Object.<string,*>} message Plain object to verify
         * @returns {string|null} `null` if valid, otherwise the reason why it is not
         */
        PushReceived.verify = function verify(message) {
            if (typeof message !== "object" || message === null)
                return "object expected";
            if (message.id != null && message.hasOwnProperty("id"))
                if (!$util.isString(message.id))
                    return "id: string expected";
            if (message.clientTimestamp != null && message.hasOwnProperty("clientTimestamp"))
                if (!$util.isInteger(message.clientTimestamp) && !(message.clientTimestamp && $util.isInteger(message.clientTimestamp.low) && $util.isInteger(message.clientTimestamp.high)))
                    return "clientTimestamp: integer|Long expected";
            return null;
        };

        /**
         * Creates a PushReceived message from a plain object. Also converts values to their respective internal types.
         * @function fromObject
         * @memberof server.PushReceived
         * @static
         * @param {Object.<string,*>} object Plain object
         * @returns {server.PushReceived} PushReceived
         */
        PushReceived.fromObject = function fromObject(object) {
            if (object instanceof $root.server.PushReceived)
                return object;
            var message = new $root.server.PushReceived();
            if (object.id != null)
                message.id = String(object.id);
            if (object.clientTimestamp != null)
                if ($util.Long)
                    (message.clientTimestamp = $util.Long.fromValue(object.clientTimestamp)).unsigned = true;
                else if (typeof object.clientTimestamp === "string")
                    message.clientTimestamp = parseInt(object.clientTimestamp, 10);
                else if (typeof object.clientTimestamp === "number")
                    message.clientTimestamp = object.clientTimestamp;
                else if (typeof object.clientTimestamp === "object")
                    message.clientTimestamp = new $util.LongBits(object.clientTimestamp.low >>> 0, object.clientTimestamp.high >>> 0).toNumber(true);
            return message;
        };

        /**
         * Creates a plain object from a PushReceived message. Also converts values to other types if specified.
         * @function toObject
         * @memberof server.PushReceived
         * @static
         * @param {server.PushReceived} message PushReceived
         * @param {$protobuf.IConversionOptions} [options] Conversion options
         * @returns {Object.<string,*>} Plain object
         */
        PushReceived.toObject = function toObject(message, options) {
            if (!options)
                options = {};
            var object = {};
            if (options.defaults) {
                object.id = "";
                if ($util.Long) {
                    var long = new $util.Long(0, 0, true);
                    object.clientTimestamp = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                } else
                    object.clientTimestamp = options.longs === String ? "0" : 0;
            }
            if (message.id != null && message.hasOwnProperty("id"))
                object.id = message.id;
            if (message.clientTimestamp != null && message.hasOwnProperty("clientTimestamp"))
                if (typeof message.clientTimestamp === "number")
                    object.clientTimestamp = options.longs === String ? String(message.clientTimestamp) : message.clientTimestamp;
                else
                    object.clientTimestamp = options.longs === String ? $util.Long.prototype.toString.call(message.clientTimestamp) : options.longs === Number ? new $util.LongBits(message.clientTimestamp.low >>> 0, message.clientTimestamp.high >>> 0).toNumber(true) : message.clientTimestamp;
            return object;
        };

        /**
         * Converts this PushReceived to JSON.
         * @function toJSON
         * @memberof server.PushReceived
         * @instance
         * @returns {Object.<string,*>} JSON object
         */
        PushReceived.prototype.toJSON = function toJSON() {
            return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
        };

        return PushReceived;
    })();

    return server;
})();

module.exports = $root;
