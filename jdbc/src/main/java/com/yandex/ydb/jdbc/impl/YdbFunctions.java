package com.yandex.ydb.jdbc.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class YdbFunctions {

    public static final class Builtin {
        public static final class Strings {
            public static final String LENGTH = "LENGTH";
            public static final String LEN = "LEN";
            public static final String SUBSTRING = "SUBSTRING";
            public static final String FIND = "FIND";
            public static final String RFIND = "RFIND";
            public static final String STARTS_WITH = "StartsWith";
            public static final String ENDS_WITH = "EndsWith";
            public static final String TO_BYTES = "ToBytes";
            public static final String FROM_BYTES = "FromBytes";
            public static final String BYTE_AT = "ByteAt";
            public static final String JSON_PATH = "JsonPath";
            public static final String JSON_EXISTS = "JSON_EXISTS";
            public static final String JSON_VALUE = "JSON_VALUE";
            public static final String JSON_QUERY = "JSON_QUERY";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(LENGTH, LEN, SUBSTRING, FIND, RFIND, STARTS_WITH, ENDS_WITH, TO_BYTES, FROM_BYTES,
                            BYTE_AT, JSON_PATH, JSON_EXISTS, JSON_VALUE, JSON_QUERY));

            private Strings() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Systems {
            public static final String COALESCE = "COALESCE";
            public static final String NVL = "NVL";
            public static final String IF = "IF";
            public static final String NANVL = "NANVL";
            public static final String MAX_OF = "MAX_OF";
            public static final String MIN_OF = "MIN_OF";
            public static final String GREATEST = "GREATEST";
            public static final String LEAST = "LEAST";
            public static final String AS_TUPLE = "AsTuple";
            public static final String AS_STRUCT = "AsStruct";
            public static final String AS_LIST = "AsList";
            public static final String AS_DICT = "AsDict";
            public static final String AS_SET = "AsSet";
            public static final String AS_LIST_STRICT = "AsListStrict";
            public static final String AS_DICT_STRICT = "AsDictStrict";
            public static final String AS_SET_STRICT = "AsSetStrict";
            public static final String VARIANT = "Variant";
            public static final String ENUM = "Enum";
            public static final String AS_VARIANT = "AsVariant";
            public static final String AS_ENUM = "AsEnum";
            public static final String AS_TAGGED = "AsTagged";
            public static final String UNTAG = "Untag";
            public static final String TABLE_ROW = "TableRow";
            public static final String JOIN_TABLE_ROW = "JoinTableRow";
            public static final String ENSURE = "Ensure";
            public static final String ENSURE_TYPE = "EnsureType";
            public static final String ENSURE_CONVERTIBLE_TO = "EnsureConvertibleTo";
            public static final String JUST = "Just";
            public static final String UNWRAP = "Unwrap";
            public static final String NOTHING = "Nothing";
            public static final String CALLABLE = "Callable";
            public static final String PICKLE = "Pickle";
            public static final String STABLE_PICKLE = "StablePickle";
            public static final String UNPICKLE = "Unpickle";
            public static final String STATIC_MAP = "StaticMap";
            public static final String AGGREGATION_FACTORY = "AggregationFactory";
            public static final String AGGREGATE_TRANSFORM_INPUT = "AggregateTransformInput";
            public static final String AGGREGATE_TRANSFORM_OUTPUT = "AggregateTransformOutput";
            public static final String AGGREGATE_FLATTEN = "AggregateFlatten";
            public static final String LIKELY = "LIKELY";
            public static final String GROUPING = "GROUPING";
            public static final String COUNT = "COUNT";
            public static final String MIN = "MIN";
            public static final String MAX = "MAX";
            public static final String SUM = "SUM";
            public static final String AVG = "AVG";
            public static final String COUNT_IF = "COUNT_IF";
            public static final String SUM_IF = "SUM_IF";
            public static final String AVG_IF = "AVG_IF";
            public static final String COUNT_DISTINCT_ESTIMATE = "CountDistinctEstimate";
            public static final String HYPER_LOG_LOG = "HyperLogLog";
            public static final String HLL = "HLL";
            public static final String SOME = "SOME";
            public static final String AGGREGATE_LIST = "AGGREGATE_LIST";
            public static final String AGGREGATE_LIST_DISTINCT = "AGGREGATE_LIST_DISTINCT";
            public static final String MAX_BY = "MAX_BY";
            public static final String MIN_BY = "MIN_BY";
            public static final String TOP = "TOP";
            public static final String BOTTOM = "BOTTOM";
            public static final String TOP_BY = "TOP_BY";
            public static final String BOTTOM_BY = "BOTTOM_BY";
            public static final String TOPFREQ = "TOPFREQ";
            public static final String MODE = "MODE";
            public static final String STDDEV = "STDDEV";
            public static final String VARIANCE = "VARIANCE";
            public static final String CORRELATION = "CORRELATION";
            public static final String COVARIANCE = "COVARIANCE";
            public static final String PERCENTILE = "PERCENTILE";
            public static final String MEDIAN = "MEDIAN";
            public static final String HISTOGRAM = "HISTOGRAM";
            public static final String LINEAR_HISTOGRAM = "LinearHistogram";
            public static final String LOGARITHMIC_HISTOGRAM = "LogarithmicHistogram";
            public static final String LOG_HISTOGRAM = "LogHistogram";
            public static final String BOOL_AND = "BOOL_AND";
            public static final String BOOL_OR = "BOOL_OR";
            public static final String BOOL_XOR = "BOOL_XOR";
            public static final String BIT_AND = "BIT_AND";
            public static final String BIT_OR = "BIT_OR";
            public static final String BIT_XOR = "BIT_XOR";
            public static final String SESSION_START = "SessionStart";
            public static final String AGGREGATE_BY = "AGGREGATE_BY";
            public static final String MULTI_AGGREGATE_BY = "MULTI_AGGREGATE_BY";
            public static final String ROW_NUMBER = "ROW_NUMBER";
            public static final String LAG = "LAG";
            public static final String LEAD = "LEAD";
            public static final String FIRST_VALUE = "FIRST_VALUE";
            public static final String LAST_VALUE = "LAST_VALUE";
            public static final String RANK = "RANK";
            public static final String DENSE_RANK = "DENSE_RANK";
            public static final String LIST_CREATE = "ListCreate";
            public static final String LIST_LENGTH = "ListLength";
            public static final String LIST_HAS_ITEMS = "ListHasItems";
            public static final String LIST_COLLECT = "ListCollect";
            public static final String LIST_SORT = "ListSort";
            public static final String LIST_SORT_ASC = "ListSortAsc";
            public static final String LIST_SORT_DESC = "ListSortDesc";
            public static final String LIST_EXTEND = "ListExtend";
            public static final String LIST_EXTEND_STRICT = "ListExtendStrict";
            public static final String LIST_UNION_ALL = "ListUnionAll";
            public static final String LIST_ZIP = "ListZip";
            public static final String LIST_ZIP_ALL = "ListZipAll";
            public static final String LIST_ENUMERATE = "ListEnumerate";
            public static final String LIST_REVERSE = "ListReverse";
            public static final String LIST_SKIP = "ListSkip";
            public static final String LIST_TAKE = "ListTake";
            public static final String LIST_INDEX_OF = "ListIndexOf";
            public static final String LIST_MAP = "ListMap";
            public static final String LIST_FLAT_MAP = "ListFlatMap";
            public static final String LIST_FILTER = "ListFilter";
            public static final String LIST_NOT_NULL = "ListNotNull";
            public static final String LIST_FLATTEN = "ListFlatten";
            public static final String LIST_UNIQ = "ListUniq";
            public static final String LIST_ANY = "ListAny";
            public static final String LIST_ALL = "ListAll";
            public static final String LIST_HAS = "ListHas";
            public static final String LIST_HEAD = "ListHead";
            public static final String LIST_LAST = "ListLast";
            public static final String LIST_MIN = "ListMin";
            public static final String LIST_MAX = "ListMax";
            public static final String LIST_SUM = "ListSum";
            public static final String LIST_AVG = "ListAvg";
            public static final String LIST_FROM_RANGE = "ListFromRange";
            public static final String LIST_REPLICATE = "ListReplicate";
            public static final String LIST_CONCAT = "ListConcat";
            public static final String LIST_EXTRACT = "ListExtract";
            public static final String LIST_TAKE_WHILE = "ListTakeWhile";
            public static final String LIST_TAKE_WHILE_INCLUSIVE = "ListTakeWhileInclusive";
            public static final String LIST_SKIP_WHILE = "ListSkipWhile";
            public static final String LIST_SKIP_WHILE_INCLUSIVE = "ListSkipWhileInclusive";
            public static final String LIST_AGGREGATE = "ListAggregate";
            public static final String TO_DICT = "ToDict";
            public static final String TO_MULTI_DICT = "ToMultiDict";
            public static final String TO_SET = "ToSet";
            public static final String DICT_LENGTH = "DictLength";
            public static final String DICT_HAS_ITEMS = "DictHasItems";
            public static final String DICT_CREATE = "DictCreate";
            public static final String SET_CREATE = "SetCreate";
            public static final String DICT_ITEMS = "DictItems";
            public static final String DICT_KEYS = "DictKeys";
            public static final String DICT_PAYLOADS = "DictPayloads";
            public static final String DICT_LOOKUP = "DictLookup";
            public static final String DICT_CONTAINS = "DictContains";
            public static final String DICT_AGGREGATE = "DictAggregate";
            public static final String SET_IS_DISJOINT = "SetIsDisjoint";
            public static final String SET_INTERSECTION = "SetIntersection";
            public static final String SET_INCLUDES = "SetIncludes";
            public static final String SET_UNION = "SetUnion";
            public static final String SET_DIFFERENCE = "SetDifference";
            public static final String SET_SYMMETRIC_DIFFERENCE = "SetSymmetricDifference";
            public static final String TRY_MEMBER = "TryMember";
            public static final String EXPAND_STRUCT = "ExpandStruct";
            public static final String ADD_MEMBER = "AddMember";
            public static final String REMOVE_MEMBER = "RemoveMember";
            public static final String FORCE_REMOVE_MEMBER = "ForceRemoveMember";
            public static final String CHOOSE_MEMBERS = "ChooseMembers";
            public static final String REMOVE_MEMBERS = "RemoveMembers";
            public static final String FORCE_REMOVE_MEMBERS = "ForceRemoveMembers";
            public static final String COMBINE_MEMBERS = "CombineMembers";
            public static final String FLATTEN_MEMBERS = "FlattenMembers";
            public static final String STRUCT_MEMBERS = "StructMembers";
            public static final String RENAME_MEMBERS = "RenameMembers";
            public static final String FORCE_RENAME_MEMBERS = "ForceRenameMembers";
            public static final String GATHER_MEMBERS = "GatherMembers";
            public static final String SPREAD_MEMBERS = "SpreadMembers";
            public static final String FORCE_SPREAD_MEMBERS = "ForceSpreadMembers";
            public static final String FORMAT_TYPE = "FormatType";
            public static final String PARSE_TYPE = "ParseType";
            public static final String TYPE_OF = "TypeOf";
            public static final String INSTANCE_OF = "InstanceOf";
            public static final String DATA_TYPE = "DataType";
            public static final String OPTIONAL_TYPE = "OptionalType";
            public static final String LIST_TYPE = "ListType";
            public static final String STREAM_TYPE = "StreamType";
            public static final String DICT_TYPE = "DictType";
            public static final String TUPLE_TYPE = "TupleType";
            public static final String STRUCT_TYPE = "StructType";
            public static final String VARIANT_TYPE = "VariantType";
            public static final String RESOURCE_TYPE = "ResourceType";
            public static final String CALLABLE_TYPE = "CallableType";
            public static final String GENERIC_TYPE = "GenericType";
            public static final String UNIT_TYPE = "UnitType";
            public static final String VOID_TYPE = "VoidType";
            public static final String OPTIONAL_ITEM_TYPE = "OptionalItemType";
            public static final String LIST_ITEM_TYPE = "ListItemType";
            public static final String STREAM_ITEM_TYPE = "StreamItemType";
            public static final String DICT_KEY_TYPE = "DictKeyType";
            public static final String DICT_PAYLOAD_TYPE = "DictPayloadType";
            public static final String TUPLE_ELEMENT_TYPE = "TupleElementType";
            public static final String STRUCT_MEMBER_TYPE = "StructMemberType";
            public static final String CALLABLE_RESULT_TYPE = "CallableResultType";
            public static final String CALLABLE_ARGUMENT_TYPE = "CallableArgumentType";
            public static final String VARIANT_UNDERLYING_TYPE = "VariantUnderlyingType";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(COALESCE, NVL, IF, NANVL, MAX_OF, MIN_OF, GREATEST, LEAST, AS_TUPLE, AS_STRUCT,
                            AS_LIST, AS_DICT, AS_SET, AS_LIST_STRICT, AS_DICT_STRICT, AS_SET_STRICT, VARIANT, ENUM,
                            AS_VARIANT, AS_ENUM, AS_TAGGED, UNTAG, TABLE_ROW, JOIN_TABLE_ROW, ENSURE, ENSURE_TYPE,
                            ENSURE_CONVERTIBLE_TO, JUST, UNWRAP, NOTHING, CALLABLE, PICKLE, STABLE_PICKLE, UNPICKLE,
                            STATIC_MAP, AGGREGATION_FACTORY, AGGREGATE_TRANSFORM_INPUT, AGGREGATE_TRANSFORM_OUTPUT,
                            AGGREGATE_FLATTEN, LIKELY, GROUPING, COUNT, MIN, MAX, SUM, AVG, COUNT_IF, SUM_IF, AVG_IF,
                            COUNT_DISTINCT_ESTIMATE, HYPER_LOG_LOG, HLL, SOME, AGGREGATE_LIST,
                            AGGREGATE_LIST_DISTINCT, MAX_BY, MIN_BY, TOP, BOTTOM, TOP_BY, BOTTOM_BY, TOPFREQ, MODE,
                            STDDEV, VARIANCE, CORRELATION, COVARIANCE, PERCENTILE, MEDIAN, HISTOGRAM,
                            LINEAR_HISTOGRAM, LOGARITHMIC_HISTOGRAM, LOG_HISTOGRAM, BOOL_AND, BOOL_OR, BOOL_XOR,
                            BIT_AND, BIT_OR, BIT_XOR, SESSION_START, AGGREGATE_BY, MULTI_AGGREGATE_BY, ROW_NUMBER,
                            LAG, LEAD, FIRST_VALUE, LAST_VALUE, RANK, DENSE_RANK, LIST_CREATE, LIST_LENGTH,
                            LIST_HAS_ITEMS, LIST_COLLECT, LIST_SORT, LIST_SORT_ASC, LIST_SORT_DESC, LIST_EXTEND,
                            LIST_EXTEND_STRICT, LIST_UNION_ALL, LIST_ZIP, LIST_ZIP_ALL, LIST_ENUMERATE, LIST_REVERSE,
                            LIST_SKIP, LIST_TAKE, LIST_INDEX_OF, LIST_MAP, LIST_FLAT_MAP, LIST_FILTER, LIST_NOT_NULL,
                            LIST_FLATTEN, LIST_UNIQ, LIST_ANY, LIST_ALL, LIST_HAS, LIST_HEAD, LIST_LAST, LIST_MIN,
                            LIST_MAX, LIST_SUM, LIST_AVG, LIST_FROM_RANGE, LIST_REPLICATE, LIST_CONCAT, LIST_EXTRACT,
                            LIST_TAKE_WHILE, LIST_TAKE_WHILE_INCLUSIVE, LIST_SKIP_WHILE, LIST_SKIP_WHILE_INCLUSIVE,
                            LIST_AGGREGATE, TO_DICT, TO_MULTI_DICT, TO_SET, DICT_LENGTH, DICT_HAS_ITEMS, DICT_CREATE,
                            SET_CREATE, DICT_ITEMS, DICT_KEYS, DICT_PAYLOADS, DICT_LOOKUP, DICT_CONTAINS,
                            DICT_AGGREGATE, SET_IS_DISJOINT, SET_INTERSECTION, SET_INCLUDES, SET_UNION,
                            SET_DIFFERENCE, SET_SYMMETRIC_DIFFERENCE, TRY_MEMBER, EXPAND_STRUCT, ADD_MEMBER,
                            REMOVE_MEMBER, FORCE_REMOVE_MEMBER, CHOOSE_MEMBERS, REMOVE_MEMBERS, FORCE_REMOVE_MEMBERS,
                            COMBINE_MEMBERS, FLATTEN_MEMBERS, STRUCT_MEMBERS, RENAME_MEMBERS, FORCE_RENAME_MEMBERS,
                            GATHER_MEMBERS, SPREAD_MEMBERS, FORCE_SPREAD_MEMBERS, FORMAT_TYPE, PARSE_TYPE, TYPE_OF,
                            INSTANCE_OF, DATA_TYPE, OPTIONAL_TYPE, LIST_TYPE, STREAM_TYPE, DICT_TYPE, TUPLE_TYPE,
                            STRUCT_TYPE, VARIANT_TYPE, RESOURCE_TYPE, CALLABLE_TYPE, GENERIC_TYPE, UNIT_TYPE,
                            VOID_TYPE, OPTIONAL_ITEM_TYPE, LIST_ITEM_TYPE, STREAM_ITEM_TYPE, DICT_KEY_TYPE,
                            DICT_PAYLOAD_TYPE, TUPLE_ELEMENT_TYPE, STRUCT_MEMBER_TYPE, CALLABLE_RESULT_TYPE,
                            CALLABLE_ARGUMENT_TYPE, VARIANT_UNDERLYING_TYPE));

            private Systems() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Numerics {
            public static final String RANDOM = "Random";
            public static final String RANDOM_NUMBER = "RandomNumber";
            public static final String RANDOM_UUID = "RandomUuid";
            public static final String TEST_BIT = "TestBit";
            public static final String CLEAR_BIT = "ClearBit";
            public static final String SET_BIT = "SetBit";
            public static final String FLIP_BIT = "FlipBit";
            public static final String ABS = "Abs";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(RANDOM, RANDOM_NUMBER, RANDOM_UUID, TEST_BIT, CLEAR_BIT, SET_BIT, FLIP_BIT, ABS));

            private Numerics() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Dates {
            public static final String CURRENT_UTC_DATE = "CurrentUtcDate";
            public static final String CURRENT_UTC_DATETIME = "CurrentUtcDatetime";
            public static final String CURRENT_UTC_TIMESTAMP = "CurrentUtcTimestamp";
            public static final String CURRENT_TZ_DATE = "CurrentTzDate";
            public static final String CURRENT_TZ_DATETIME = "CurrentTzDatetime";
            public static final String CURRENT_TZ_TIMESTAMP = "CurrentTzTimestamp";
            public static final String ADD_TIMEZONE = "AddTimezone";
            public static final String REMOVE_TIMEZONE = "RemoveTimezone";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(CURRENT_UTC_DATE, CURRENT_UTC_DATETIME, CURRENT_UTC_TIMESTAMP, CURRENT_TZ_DATE,
                            CURRENT_TZ_DATETIME, CURRENT_TZ_TIMESTAMP, ADD_TIMEZONE, REMOVE_TIMEZONE));

            private Dates() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        private Builtin() {
        }

        private static final List<String> ALL_FUNCTIONS = Collections.unmodifiableList(Stream.of(
                Strings.functions(),
                Systems.functions(),
                Numerics.functions(),
                Dates.functions())
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

        public static List<String> allFunctions() {
            return ALL_FUNCTIONS;
        }
    }

    public static final class Udf {
        public static final class Hyperscans {
            public static final String GREP = "Hyperscan::Grep";
            public static final String MATCH = "Hyperscan::Match";
            public static final String BACKTRACKING_GREP = "Hyperscan::BacktrackingGrep";
            public static final String BACKTRACKING_MATCH = "Hyperscan::BacktrackingMatch";
            public static final String MULTI_GREP = "Hyperscan::MultiGrep";
            public static final String MULTI_MATCH = "Hyperscan::MultiMatch";
            public static final String CAPTURE = "Hyperscan::Capture";
            public static final String REPLACE = "Hyperscan::Replace";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(GREP, MATCH, BACKTRACKING_GREP, BACKTRACKING_MATCH, MULTI_GREP, MULTI_MATCH,
                            CAPTURE, REPLACE));

            private Hyperscans() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Pires {
            public static final String GREP = "Pire::Grep";
            public static final String MATCH = "Pire::Match";
            public static final String MULTI_GREP = "Pire::MultiGrep";
            public static final String MULTI_MATCH = "Pire::MultiMatch";
            public static final String CAPTURE = "Pire::Capture";
            public static final String REPLACE = "Pire::Replace";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(GREP, MATCH, MULTI_GREP, MULTI_MATCH, CAPTURE, REPLACE));

            private Pires() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Re2s {
            public static final String GREP = "Re2::Grep";
            public static final String MATCH = "Re2::Match";
            public static final String CAPTURE = "Re2::Capture";
            public static final String FIND_AND_CONSUME = "Re2::FindAndConsume";
            public static final String REPLACE = "Re2::Replace";
            public static final String COUNT = "Re2::Count";
            public static final String OPTIONS = "Re2::Options";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(GREP, MATCH, CAPTURE, FIND_AND_CONSUME, REPLACE, COUNT, OPTIONS));

            private Re2s() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Strings {
            public static final String BASE64_ENCODE = "String::Base64Encode";
            public static final String BASE64_DECODE = "String::Base64Decode";
            public static final String ESCAPE_C = "String::EscapeC";
            public static final String UNESCAPE_C = "String::UnescapeC";
            public static final String HEX_ENCODE = "String::HexEncode";
            public static final String HEX_DECODE = "String::HexDecode";
            public static final String ENCODE_HTML = "String::EncodeHtml";
            public static final String DECODE_HTML = "String::DecodeHtml";
            public static final String CGI_ESCAPE = "String::CgiEscape";
            public static final String CGI_UNESCAPE = "String::CgiUnescape";
            public static final String STRIP = "String::Strip";
            public static final String COLLAPSE = "String::Collapse";
            public static final String COLLAPSE_TEXT = "String::CollapseText";
            public static final String CONTAINS = "String::Contains";
            public static final String FIND = "String::Find";
            public static final String REVERSE_FIND = "String::ReverseFind";
            public static final String HAS_PREFIX = "String::HasPrefix";
            public static final String HAS_PREFIX_IGNORE_CASE = "String::HasPrefixIgnoreCase";
            public static final String STARTS_WITH = "String::StartsWith";
            public static final String STARTS_WITH_IGNORE_CASE = "String::StartsWithIgnoreCase";
            public static final String HAS_SUFFIX = "String::HasSuffix";
            public static final String HAS_SUFFIX_IGNORE_CASE = "String::HasSuffixIgnoreCase";
            public static final String ENDS_WITH = "String::EndsWith";
            public static final String ENDS_WITH_IGNORE_CASE = "String::EndsWithIgnoreCase";
            public static final String SUBSTRING = "String::Substring";
            public static final String ASCII_TO_LOWER = "String::AsciiToLower";
            public static final String ASCII_TO_UPPER = "String::AsciiToUpper";
            public static final String ASCII_TO_TITLE = "String::AsciiToTitle";
            public static final String SPLIT_TO_LIST = "String::SplitToList";
            public static final String JOIN_FROM_LIST = "String::JoinFromList";
            public static final String TO_BYTE_LIST = "String::ToByteList";
            public static final String FROM_BYTE_LIST = "String::FromByteList";
            public static final String REPLACE_ALL = "String::ReplaceAll";
            public static final String REPLACE_FIRST = "String::ReplaceFirst";
            public static final String REPLACE_LAST = "String::ReplaceLast";
            public static final String REMOVE_ALL = "String::RemoveAll";
            public static final String REMOVE_FIRST = "String::RemoveFirst";
            public static final String REMOVE_LAST = "String::RemoveLast";
            public static final String IS_ASCII = "String::IsAscii";
            public static final String IS_ASCII_SPACE = "String::IsAsciiSpace";
            public static final String IS_ASCII_UPPER = "String::IsAsciiUpper";
            public static final String IS_ASCII_LOWER = "String::IsAsciiLower";
            public static final String IS_ASCII_ALPHA = "String::IsAsciiAlpha";
            public static final String IS_ASCII_ALNUM = "String::IsAsciiAlnum";
            public static final String IS_ASCII_HEX = "String::IsAsciiHex";
            public static final String LEVENSTEIN_DISTANCE = "String::LevensteinDistance";
            public static final String LEFT_PAD = "String::LeftPad";
            public static final String RIGHT_PAD = "String::RightPad";
            public static final String HEX = "String::Hex";
            public static final String S_HEX = "String::SHex";
            public static final String BIN = "String::Bin";
            public static final String S_BIN = "String::SBin";
            public static final String HEX_TEXT = "String::HexText";
            public static final String BIN_TEXT = "String::BinText";
            public static final String HUMAN_READABLE_DURATION = "String::HumanReadableDuration";
            public static final String HUMAN_READABLE_QUANTITY = "String::HumanReadableQuantity";
            public static final String HUMAN_READABLE_BYTES = "String::HumanReadableBytes";
            public static final String PREC = "String::Prec";
            public static final String REVERSE = "String::Reverse";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(BASE64_ENCODE, BASE64_DECODE, ESCAPE_C, UNESCAPE_C, HEX_ENCODE, HEX_DECODE,
                            ENCODE_HTML, DECODE_HTML, CGI_ESCAPE, CGI_UNESCAPE, STRIP, COLLAPSE, COLLAPSE_TEXT,
                            CONTAINS, FIND, REVERSE_FIND, HAS_PREFIX, HAS_PREFIX_IGNORE_CASE, STARTS_WITH,
                            STARTS_WITH_IGNORE_CASE, HAS_SUFFIX, HAS_SUFFIX_IGNORE_CASE, ENDS_WITH,
                            ENDS_WITH_IGNORE_CASE, SUBSTRING, ASCII_TO_LOWER, ASCII_TO_UPPER, ASCII_TO_TITLE,
                            SPLIT_TO_LIST, JOIN_FROM_LIST, TO_BYTE_LIST, FROM_BYTE_LIST, REPLACE_ALL, REPLACE_FIRST,
                            REPLACE_LAST, REMOVE_ALL, REMOVE_FIRST, REMOVE_LAST, IS_ASCII, IS_ASCII_SPACE,
                            IS_ASCII_UPPER, IS_ASCII_LOWER, IS_ASCII_ALPHA, IS_ASCII_ALNUM, IS_ASCII_HEX,
                            LEVENSTEIN_DISTANCE, LEFT_PAD, RIGHT_PAD, HEX, S_HEX, BIN, S_BIN, HEX_TEXT, BIN_TEXT,
                            HUMAN_READABLE_DURATION, HUMAN_READABLE_QUANTITY, HUMAN_READABLE_BYTES, PREC, REVERSE));

            private Strings() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Unicodes {
            public static final String IS_UTF = "Unicode::IsUtf";
            public static final String GET_LENGTH = "Unicode::GetLength";
            public static final String SUBSTRING = "Unicode::Substring";
            public static final String NORMALIZE = "Unicode::Normalize";
            public static final String NORMALIZE_NFD = "Unicode::NormalizeNFD";
            public static final String NORMALIZE_NFC = "Unicode::NormalizeNFC";
            public static final String NORMALIZE_NFKD = "Unicode::NormalizeNFKD";
            public static final String NORMALIZE_NFKC = "Unicode::NormalizeNFKC";
            public static final String TRANSLIT = "Unicode::Translit";
            public static final String LEVENSTEIN_DISTANCE = "Unicode::LevensteinDistance";
            public static final String FOLD = "Unicode::Fold";
            public static final String REPLACE_ALL = "Unicode::ReplaceAll";
            public static final String REPLACE_FIRST = "Unicode::ReplaceFirst";
            public static final String REPLACE_LAST = "Unicode::ReplaceLast";
            public static final String REMOVE_ALL = "Unicode::RemoveAll";
            public static final String REMOVE_FIRST = "Unicode::RemoveFirst";
            public static final String REMOVE_LAST = "Unicode::RemoveLast";
            public static final String TO_CODE_POINT_LIST = "Unicode::ToCodePointList";
            public static final String FROM_CODE_POINT_LIST = "Unicode::FromCodePointList";
            public static final String REVERSE = "Unicode::Reverse";
            public static final String TO_LOWER = "Unicode::ToLower";
            public static final String TO_UPPER = "Unicode::ToUpper";
            public static final String TO_TITLE = "Unicode::ToTitle";
            public static final String SPLIT_TO_LIST = "Unicode::SplitToList";
            public static final String JOIN_FROM_LIST = "Unicode::JoinFromList";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(IS_UTF, GET_LENGTH, SUBSTRING, NORMALIZE, NORMALIZE_NFD, NORMALIZE_NFC,
                            NORMALIZE_NFKD, NORMALIZE_NFKC, TRANSLIT, LEVENSTEIN_DISTANCE, FOLD, REPLACE_ALL,
                            REPLACE_FIRST, REPLACE_LAST, REMOVE_ALL, REMOVE_FIRST, REMOVE_LAST, TO_CODE_POINT_LIST,
                            FROM_CODE_POINT_LIST, REVERSE, TO_LOWER, TO_UPPER, TO_TITLE, SPLIT_TO_LIST,
                            JOIN_FROM_LIST));

            private Unicodes() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class DateTimes {
            public static final String SPLIT = "DateTime::Split";
            public static final String MAKE_DATE = "DateTime::MakeDate";
            public static final String MAKE_DATETIME = "DateTime::MakeDatetime";
            public static final String MAKE_TIMESTAMP = "DateTime::MakeTimestamp";
            public static final String MAKE_TZ_DATE = "DateTime::MakeTzDate";
            public static final String MAKE_TZ_DATETIME = "DateTime::MakeTzDatetime";
            public static final String MAKE_TZ_TIMESTAMP = "DateTime::MakeTzTimestamp";
            public static final String GET_YEAR = "DateTime::GetYear";
            public static final String GET_DAY_OF_YEAR = "DateTime::GetDayOfYear";
            public static final String GET_MONTH = "DateTime::GetMonth";
            public static final String GET_MONTH_NAME = "DateTime::GetMonthName";
            public static final String GET_WEEK_OF_YEAR = "DateTime::GetWeekOfYear";
            public static final String GET_DAY_OF_MONTH = "DateTime::GetDayOfMonth";
            public static final String GET_DAY_OF_WEEK = "DateTime::GetDayOfWeek";
            public static final String GET_DAY_OF_WEEK_NAME = "DateTime::GetDayOfWeekName";
            public static final String GET_HOUR = "DateTime::GetHour";
            public static final String GET_MINUTE = "DateTime::GetMinute";
            public static final String GET_SECOND = "DateTime::GetSecond";
            public static final String GET_MILLISECOND_OF_SECOND = "DateTime::GetMillisecondOfSecond";
            public static final String GET_MICROSECOND_OF_SECOND = "DateTime::GetMicrosecondOfSecond";
            public static final String GET_TIMEZONE_ID = "DateTime::GetTimezoneId";
            public static final String GET_TIMEZONE_NAME = "DateTime::GetTimezoneName";
            public static final String UPDATE = "DateTime::Update";
            public static final String FROM_SECONDS = "DateTime::FromSeconds";
            public static final String FROM_MILLISECONDS = "DateTime::FromMilliseconds";
            public static final String FROM_MICROSECONDS = "DateTime::FromMicroseconds";
            public static final String TO_SECONDS = "DateTime::ToSeconds";
            public static final String TO_MILLISECONDS = "DateTime::ToMilliseconds";
            public static final String TO_MICROSECONDS = "DateTime::ToMicroseconds";
            public static final String TO_DAYS = "DateTime::ToDays";
            public static final String TO_HOURS = "DateTime::ToHours";
            public static final String TO_MINUTES = "DateTime::ToMinutes";
            public static final String INTERVAL_FROM_DAYS = "DateTime::IntervalFromDays";
            public static final String INTERVAL_FROM_HOURS = "DateTime::IntervalFromHours";
            public static final String INTERVAL_FROM_MINUTES = "DateTime::IntervalFromMinutes";
            public static final String INTERVAL_FROM_SECONDS = "DateTime::IntervalFromSeconds";
            public static final String INTERVAL_FROM_MILLISECONDS = "DateTime::IntervalFromMilliseconds";
            public static final String INTERVAL_FROM_MICROSECONDS = "DateTime::IntervalFromMicroseconds";
            public static final String START_OF_YEAR = "DateTime::StartOfYear";
            public static final String START_OF_QUARTER = "DateTime::StartOfQuarter";
            public static final String START_OF_MONTH = "DateTime::StartOfMonth";
            public static final String START_OF_WEEK = "DateTime::StartOfWeek";
            public static final String START_OF_DAY = "DateTime::StartOfDay";
            public static final String START_OF = "DateTime::StartOf";
            public static final String TIME_OF_DAY = "DateTime::TimeOfDay";
            public static final String SHIFT_YEARS = "DateTime::ShiftYears";
            public static final String SHIFT_QUARTERS = "DateTime::ShiftQuarters";
            public static final String SHIFT_MONTHS = "DateTime::ShiftMonths";
            public static final String FORMAT = "DateTime::Format";
            public static final String PARSE = "DateTime::Parse";
            public static final String PARSE_RFC822 = "DateTime::ParseRfc822";
            public static final String PARSE_ISO8601 = "DateTime::ParseIso8601";
            public static final String PARSE_HTTP = "DateTime::ParseHttp";
            public static final String PARSE_X509 = "DateTime::ParseX509";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(SPLIT, MAKE_DATE, MAKE_DATETIME, MAKE_TIMESTAMP, MAKE_TZ_DATE, MAKE_TZ_DATETIME,
                            MAKE_TZ_TIMESTAMP, GET_YEAR, GET_DAY_OF_YEAR, GET_MONTH, GET_MONTH_NAME, GET_WEEK_OF_YEAR
                            , GET_DAY_OF_MONTH, GET_DAY_OF_WEEK, GET_DAY_OF_WEEK_NAME, GET_HOUR, GET_MINUTE,
                            GET_SECOND, GET_MILLISECOND_OF_SECOND, GET_MICROSECOND_OF_SECOND, GET_TIMEZONE_ID,
                            GET_TIMEZONE_NAME, UPDATE, FROM_SECONDS, FROM_MILLISECONDS, FROM_MICROSECONDS, TO_SECONDS
                            , TO_MILLISECONDS, TO_MICROSECONDS, TO_DAYS, TO_HOURS, TO_MINUTES, INTERVAL_FROM_DAYS,
                            INTERVAL_FROM_HOURS, INTERVAL_FROM_MINUTES, INTERVAL_FROM_SECONDS,
                            INTERVAL_FROM_MILLISECONDS, INTERVAL_FROM_MICROSECONDS, START_OF_YEAR, START_OF_QUARTER,
                            START_OF_MONTH, START_OF_WEEK, START_OF_DAY, START_OF, TIME_OF_DAY, SHIFT_YEARS,
                            SHIFT_QUARTERS, SHIFT_MONTHS, FORMAT, PARSE, PARSE_RFC822, PARSE_ISO8601, PARSE_HTTP,
                            PARSE_X509));

            private DateTimes() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Urls {
            public static final String NORMALIZE = "Url::Normalize";
            public static final String NORMALIZE_WITH_DEFAULT_HTTP_SCHEME = "Url::NormalizeWithDefaultHttpScheme";
            public static final String ENCODE = "Url::Encode";
            public static final String DECODE = "Url::Decode";
            public static final String PARSE = "Url::Parse";
            public static final String GET_SCHEME = "Url::GetScheme";
            public static final String GET_HOST = "Url::GetHost";
            public static final String GET_HOST_PORT = "Url::GetHostPort";
            public static final String GET_SCHEME_HOST = "Url::GetSchemeHost";
            public static final String GET_SCHEME_HOST_PORT = "Url::GetSchemeHostPort";
            public static final String GET_PORT = "Url::GetPort";
            public static final String GET_TAIL = "Url::GetTail";
            public static final String GET_PATH = "Url::GetPath";
            public static final String GET_FRAGMENT = "Url::GetFragment";
            public static final String GET_CGI_PARAM = "Url::GetCGIParam";
            public static final String GET_DOMAIN = "Url::GetDomain";
            public static final String GET_TLD = "Url::GetTLD";
            public static final String IS_KNOWN_TLD = "Url::IsKnownTLD";
            public static final String IS_WELL_KNOWN_TLD = "Url::IsWellKnownTLD";
            public static final String GET_DOMAIN_LEVEL = "Url::GetDomainLevel";
            public static final String GET_SIGNIFICANT_DOMAIN = "Url::GetSignificantDomain";
            public static final String GET_OWNER = "Url::GetOwner";
            public static final String CUT_SCHEME = "Url::CutScheme";
            public static final String CUT_WWW = "Url::CutWWW";
            public static final String CUT_WWW2 = "Url::CutWWW2";
            public static final String CUT_QUERY_STRING_AND_FRAGMENT = "Url::CutQueryStringAndFragment";
            public static final String HOST_NAME_TO_PUNYCODE = "Url::HostNameToPunycode";
            public static final String FORCE_HOST_NAME_TO_PUNYCODE = "Url::ForceHostNameToPunycode";
            public static final String PUNYCODE_TO_HOST_NAME = "Url::PunycodeToHostName";
            public static final String FORCE_PUNYCODE_TO_HOST_NAME = "Url::ForcePunycodeToHostName";
            public static final String CAN_BE_PUNYCODE_HOST_NAME = "Url::CanBePunycodeHostName";
            public static final String IS_ALLOWED_BY_ROBOTS_TXT = "Url::IsAllowedByRobotsTxt";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(NORMALIZE, NORMALIZE_WITH_DEFAULT_HTTP_SCHEME, ENCODE, DECODE, PARSE, GET_SCHEME,
                            GET_HOST, GET_HOST_PORT, GET_SCHEME_HOST, GET_SCHEME_HOST_PORT, GET_PORT, GET_TAIL,
                            GET_PATH, GET_FRAGMENT, GET_CGI_PARAM, GET_DOMAIN, GET_TLD, IS_KNOWN_TLD,
                            IS_WELL_KNOWN_TLD, GET_DOMAIN_LEVEL, GET_SIGNIFICANT_DOMAIN, GET_OWNER, CUT_SCHEME,
                            CUT_WWW, CUT_WWW2, CUT_QUERY_STRING_AND_FRAGMENT, HOST_NAME_TO_PUNYCODE,
                            FORCE_HOST_NAME_TO_PUNYCODE, PUNYCODE_TO_HOST_NAME, FORCE_PUNYCODE_TO_HOST_NAME,
                            CAN_BE_PUNYCODE_HOST_NAME, IS_ALLOWED_BY_ROBOTS_TXT));

            private Urls() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Ips {
            public static final String FROM_STRING = "Ip::FromString";
            public static final String TO_STRING = "Ip::ToString";
            public static final String IS_IPV4 = "Ip::IsIPv4";
            public static final String IS_IPV6 = "Ip::IsIPv6";
            public static final String IS_EMBEDDED_IPV4 = "Ip::IsEmbeddedIPv4";
            public static final String CONVERT_TO_IPV6 = "Ip::ConvertToIPv6";
            public static final String GET_SUBNET = "Ip::GetSubnet";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(FROM_STRING, TO_STRING, IS_IPV4, IS_IPV6, IS_EMBEDDED_IPV4, CONVERT_TO_IPV6,
                            GET_SUBNET));

            private Ips() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Ysons {
            public static final String PARSE = "Yson::Parse";
            public static final String PARSE_JSON = "Yson::ParseJson";
            public static final String PARSE_JSON_DECODE_UTF8 = "Yson::ParseJsonDecodeUtf8";
            public static final String FROM = "Yson::From";
            public static final String WITH_ATTRIBUTES = "Yson::WithAttributes";
            public static final String EQUALS = "Yson::Equals";
            public static final String GET_HASH = "Yson::GetHash";
            public static final String IS_ENTITY = "Yson::IsEntity";
            public static final String IS_STRING = "Yson::IsString";
            public static final String IS_DOUBLE = "Yson::IsDouble";
            public static final String IS_UINT64 = "Yson::IsUint64";
            public static final String IS_INT64 = "Yson::IsInt64";
            public static final String IS_BOOL = "Yson::IsBool";
            public static final String IS_LIST = "Yson::IsList";
            public static final String IS_DICT = "Yson::IsDict";
            public static final String GET_LENGTH = "Yson::GetLength";
            public static final String CONVERT_TO = "Yson::ConvertTo";
            public static final String CONVERT_TO_BOOL = "Yson::ConvertToBool";
            public static final String CONVERT_TO_INT64 = "Yson::ConvertToInt64";
            public static final String CONVERT_TO_UINT64 = "Yson::ConvertToUint64";
            public static final String CONVERT_TO_DOUBLE = "Yson::ConvertToDouble";
            public static final String CONVERT_TO_STRING = "Yson::ConvertToString";
            public static final String CONVERT_TO_LIST = "Yson::ConvertToList";
            public static final String CONVERT_TO_BOOL_LIST = "Yson::ConvertToBoolList";
            public static final String CONVERT_TO_INT64_LIST = "Yson::ConvertToInt64List";
            public static final String CONVERT_TO_UINT64_LIST = "Yson::ConvertToUint64List";
            public static final String CONVERT_TO_DOUBLE_LIST = "Yson::ConvertToDoubleList";
            public static final String CONVERT_TO_STRING_LIST = "Yson::ConvertToStringList";
            public static final String CONVERT_TO_DICT = "Yson::ConvertToDict";
            public static final String CONVERT_TO_BOOL_DICT = "Yson::ConvertToBoolDict";
            public static final String CONVERT_TO_INT64_DICT = "Yson::ConvertToInt64Dict";
            public static final String CONVERT_TO_UINT64_DICT = "Yson::ConvertToUint64Dict";
            public static final String CONVERT_TO_DOUBLE_DICT = "Yson::ConvertToDoubleDict";
            public static final String CONVERT_TO_STRING_DICT = "Yson::ConvertToStringDict";
            public static final String CONTAINS = "Yson::Contains";
            public static final String LOOKUP = "Yson::Lookup";
            public static final String LOOKUP_BOOL = "Yson::LookupBool";
            public static final String LOOKUP_INT64 = "Yson::LookupInt64";
            public static final String LOOKUP_UINT64 = "Yson::LookupUint64";
            public static final String LOOKUP_DOUBLE = "Yson::LookupDouble";
            public static final String LOOKUP_STRING = "Yson::LookupString";
            public static final String LOOKUP_DICT = "Yson::LookupDict";
            public static final String LOOKUP_LIST = "Yson::LookupList";
            public static final String Y_PATH = "Yson::YPath";
            public static final String Y_PATH_BOOL = "Yson::YPathBool";
            public static final String Y_PATH_INT64 = "Yson::YPathInt64";
            public static final String Y_PATH_UINT64 = "Yson::YPathUint64";
            public static final String Y_PATH_DOUBLE = "Yson::YPathDouble";
            public static final String Y_PATH_STRING = "Yson::YPathString";
            public static final String Y_PATH_DICT = "Yson::YPathDict";
            public static final String Y_PATH_LIST = "Yson::YPathList";
            public static final String ATTRIBUTES = "Yson::Attributes";
            public static final String SERIALIZE = "Yson::Serialize";
            public static final String SERIALIZE_TEXT = "Yson::SerializeText";
            public static final String SERIALIZE_PRETTY = "Yson::SerializePretty";
            public static final String SERIALIZE_JSON = "Yson::SerializeJson";
            public static final String OPTIONS = "Yson::Options";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(PARSE, PARSE_JSON, PARSE_JSON_DECODE_UTF8, FROM, WITH_ATTRIBUTES, EQUALS, GET_HASH,
                            IS_ENTITY, IS_STRING, IS_DOUBLE, IS_UINT64, IS_INT64, IS_BOOL, IS_LIST, IS_DICT,
                            GET_LENGTH, CONVERT_TO, CONVERT_TO_BOOL, CONVERT_TO_INT64, CONVERT_TO_UINT64,
                            CONVERT_TO_DOUBLE, CONVERT_TO_STRING, CONVERT_TO_LIST, CONVERT_TO_BOOL_LIST,
                            CONVERT_TO_INT64_LIST, CONVERT_TO_UINT64_LIST, CONVERT_TO_DOUBLE_LIST,
                            CONVERT_TO_STRING_LIST, CONVERT_TO_DICT, CONVERT_TO_BOOL_DICT, CONVERT_TO_INT64_DICT,
                            CONVERT_TO_UINT64_DICT, CONVERT_TO_DOUBLE_DICT, CONVERT_TO_STRING_DICT, CONTAINS, LOOKUP,
                            LOOKUP_BOOL, LOOKUP_INT64, LOOKUP_UINT64, LOOKUP_DOUBLE, LOOKUP_STRING, LOOKUP_DICT,
                            LOOKUP_LIST, Y_PATH, Y_PATH_BOOL, Y_PATH_INT64, Y_PATH_UINT64, Y_PATH_DOUBLE,
                            Y_PATH_STRING, Y_PATH_DICT, Y_PATH_LIST, ATTRIBUTES, SERIALIZE, SERIALIZE_TEXT,
                            SERIALIZE_PRETTY, SERIALIZE_JSON, OPTIONS));

            private Ysons() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Digests {
            public static final String CRC32C = "Digest::Crc32c";
            public static final String CRC64 = "Digest::Crc64";
            public static final String FNV32 = "Digest::Fnv32";
            public static final String FNV64 = "Digest::Fnv64";
            public static final String MUR_MUR_HASH = "Digest::MurMurHash";
            public static final String CITY_HASH = "Digest::CityHash";
            public static final String CITY_HASH128 = "Digest::CityHash128";
            public static final String NUMERIC_HASH = "Digest::NumericHash";
            public static final String MD5_HEX = "Digest::Md5Hex";
            public static final String MD5_RAW = "Digest::Md5Raw";
            public static final String MD5_HALF_MIX = "Digest::Md5HalfMix";
            public static final String ARGON2 = "Digest::Argon2";
            public static final String BLAKE2_B = "Digest::Blake2B";
            public static final String SIP_HASH = "Digest::SipHash";
            public static final String HIGHWAY_HASH = "Digest::HighwayHash";
            public static final String FARM_HASH_FINGERPRINT32 = "Digest::FarmHashFingerprint32";
            public static final String FARM_HASH_FINGERPRINT64 = "Digest::FarmHashFingerprint64";
            public static final String FARM_HASH_FINGERPRINT128 = "Digest::FarmHashFingerprint128";
            public static final String SUPER_FAST_HASH = "Digest::SuperFastHash";
            public static final String SHA1 = "Digest::Sha1";
            public static final String SHA256 = "Digest::Sha256";
            public static final String INT_HASH64 = "Digest::IntHash64";
            public static final String XXH3 = "Digest::XXH3";
            public static final String XXH3_128 = "Digest::XXH3_128";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(CRC32C, CRC64, FNV32, FNV64, MUR_MUR_HASH, CITY_HASH, CITY_HASH128, NUMERIC_HASH,
                            MD5_HEX, MD5_RAW, MD5_HALF_MIX, ARGON2, BLAKE2_B, SIP_HASH, HIGHWAY_HASH,
                            FARM_HASH_FINGERPRINT32, FARM_HASH_FINGERPRINT64, FARM_HASH_FINGERPRINT128,
                            SUPER_FAST_HASH, SHA1, SHA256, INT_HASH64, XXH3, XXH3_128));

            private Digests() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Maths {
            public static final String PI = "Math::Pi";
            public static final String E = "Math::E";
            public static final String IS_INF = "Math::IsInf";
            public static final String IS_NAN = "Math::IsNaN";
            public static final String IS_FINITE = "Math::IsFinite";
            public static final String ABS = "Math::Abs";
            public static final String ACOS = "Math::Acos";
            public static final String ASIN = "Math::Asin";
            public static final String ASINH = "Math::Asinh";
            public static final String ATAN = "Math::Atan";
            public static final String CBRT = "Math::Cbrt";
            public static final String CEIL = "Math::Ceil";
            public static final String COS = "Math::Cos";
            public static final String COSH = "Math::Cosh";
            public static final String ERF = "Math::Erf";
            public static final String EXP = "Math::Exp";
            public static final String EXP2 = "Math::Exp2";
            public static final String FABS = "Math::Fabs";
            public static final String FLOOR = "Math::Floor";
            public static final String LGAMMA = "Math::Lgamma";
            public static final String RINT = "Math::Rint";
            public static final String SIGMOID = "Math::Sigmoid";
            public static final String SIN = "Math::Sin";
            public static final String SINH = "Math::Sinh";
            public static final String SQRT = "Math::Sqrt";
            public static final String TAN = "Math::Tan";
            public static final String TANH = "Math::Tanh";
            public static final String TGAMMA = "Math::Tgamma";
            public static final String TRUNC = "Math::Trunc";
            public static final String LOG = "Math::Log";
            public static final String LOG2 = "Math::Log2";
            public static final String LOG10 = "Math::Log10";
            public static final String ATAN2 = "Math::Atan2";
            public static final String FMOD = "Math::Fmod";
            public static final String HYPOT = "Math::Hypot";
            public static final String POW = "Math::Pow";
            public static final String REMAINDER = "Math::Remainder";
            public static final String FUZZY_EQUALS = "Math::FuzzyEquals";
            public static final String MOD = "Math::Mod";
            public static final String REM = "Math::Rem";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(PI, E, IS_INF, IS_NAN, IS_FINITE, ABS, ACOS, ASIN, ASINH, ATAN, CBRT, CEIL, COS,
                            COSH, ERF, EXP, EXP2, FABS, FLOOR, LGAMMA, RINT, SIGMOID, SIN, SINH, SQRT, TAN, TANH,
                            TGAMMA, TRUNC, LOG, LOG2, LOG10, ATAN2, FMOD, HYPOT, POW, REMAINDER, FUZZY_EQUALS, MOD,
                            REM));

            private Maths() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        public static final class Histograms {
            public static final String PRINT = "Histogram::Print";
            public static final String NORMALIZE = "Histogram::Normalize";
            public static final String TO_CUMULATIVE_DISTRIBUTION_FUNCTION = "Histogram" +
                    "::ToCumulativeDistributionFunction";
            public static final String GET_SUM_ABOVE_BOUND = "Histogram::GetSumAboveBound";
            public static final String GET_SUM_BELOW_BOUND = "Histogram::GetSumBelowBound";
            public static final String GET_SUM_IN_RANGE = "Histogram::GetSumInRange";
            public static final String CALC_UPPER_BOUND = "Histogram::CalcUpperBound";
            public static final String CALC_LOWER_BOUND = "Histogram::CalcLowerBound";
            public static final String CALC_UPPER_BOUND_SAFE = "Histogram::CalcUpperBoundSafe";
            public static final String CALC_LOWER_BOUND_SAFE = "Histogram::CalcLowerBoundSafe";
            private static final List<String> FUNCTIONS = Collections.unmodifiableList(
                    Arrays.asList(PRINT, NORMALIZE, TO_CUMULATIVE_DISTRIBUTION_FUNCTION, GET_SUM_ABOVE_BOUND,
                            GET_SUM_BELOW_BOUND, GET_SUM_IN_RANGE, CALC_UPPER_BOUND, CALC_LOWER_BOUND,
                            CALC_UPPER_BOUND_SAFE, CALC_LOWER_BOUND_SAFE));

            private Histograms() {
            }

            public static List<String> functions() {
                return FUNCTIONS;
            }
        }

        private Udf() {
        }

        private static final List<String> ALL_FUNCTIONS = Collections.unmodifiableList(Stream.of(
                Hyperscans.functions(),
                Pires.functions(),
                Re2s.functions(),
                Strings.functions(),
                Unicodes.functions(),
                DateTimes.functions(),
                Urls.functions(),
                Ips.functions(),
                Ysons.functions(),
                Digests.functions(),
                Maths.functions(),
                Histograms.functions())
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

        public static List<String> allFunctions() {
            return ALL_FUNCTIONS;
        }
    }

}
