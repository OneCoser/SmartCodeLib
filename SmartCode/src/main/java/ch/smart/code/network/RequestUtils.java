package ch.smart.code.network;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

public class RequestUtils {

    private RequestUtils() {
        throw new IllegalStateException("you can't instantiate me!");
    }

    /**
     * 判断 str 是否已经 URLEncoder.encode() 过
     * 经常遇到这样的情况, 拿到一个 URL, 但是搞不清楚到底要不要 URLEncoder.encode()
     * 不做 URLEncoder.encode() 吧, 担心出错, 做 URLEncoder.encode() 吧, 又怕重复了
     *
     * @param str 需要判断的内容
     * @return 返回 {@code true} 为被 URLEncoder.encode() 过
     */
    public static boolean hasUrlEncoded(String str) {
        boolean encode = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '%' && (i + 2) < str.length()) {
                // 判断是否符合urlEncode规范
                char c1 = str.charAt(i + 1);
                char c2 = str.charAt(i + 2);
                if (isValidHexChar(c1) && isValidHexChar(c2)) {
                    encode = true;
                    break;
                } else {
                    break;
                }
            }
        }
        return encode;
    }

    /**
     * 判断 c 是否是 16 进制的字符
     *
     * @param c 需要判断的字符
     * @return 返回 {@code true} 为 16 进制的字符
     */
    private static boolean isValidHexChar(char c) {
        return ('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F');
    }

    public static final InputFilter emojiFilter = new InputFilter() {//emoji过滤器
        Pattern emoji = Pattern.compile(
                "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
                                   int dend) {

            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                return "";
            }

            return null;
        }
    };

    /**
     * 字符串转换成十六进制字符串
     *
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }

    /**
     * json 格式化
     *
     * @param json
     * @return
     */
    public static String jsonFormat(String json) {
        if (TextUtils.isEmpty(json)) {
            return "Empty/Null json content";
        }
        String message;
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                message = jsonObject.toString(4);
            } else if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                message = jsonArray.toString(4);
            } else {
                message = json;
            }
        } catch (JSONException e) {
            message = json;
        } catch (OutOfMemoryError error) {
            message = "Output omitted because of Object size";
        }
        return message;
    }

    /**
     * xml 格式化
     *
     * @param xml
     * @return
     */
    public static String xmlFormat(String xml) {
        if (TextUtils.isEmpty(xml)) {
            return "Empty/Null xml content";
        }
        String message;
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            message = xmlOutput.getWriter().toString().replaceFirst(">", ">\n");
        } catch (TransformerException e) {
            message = xml;
        }
        return message;
    }

    /**
     * 解析请求服务器的请求参数
     *
     * @param request {@link Request}
     * @return 解析后的请求信息
     * @throws UnsupportedEncodingException
     */
    public static String parseParams(Request request) throws UnsupportedEncodingException {
        try {
            RequestBody body = request.newBuilder().build().body();
            if (body == null) {
                return "";
            }
            Buffer requestbuffer = new Buffer();
            body.writeTo(requestbuffer);
            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = body.contentType();
            if (contentType != null) {
                charset = contentType.charset(charset);
            }
            String json = requestbuffer.readString(charset);
            if (hasUrlEncoded(json)) {
                json = URLDecoder.decode(json, convertCharset(charset));
            }
            return jsonFormat(json);
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 是否可以解析
     *
     * @param mediaType {@link MediaType}
     * @return {@code true} 为可以解析
     */
    public static boolean isParseable(MediaType mediaType) {
        if (mediaType == null || mediaType.type() == null) {
            return false;
        }
        return isText(mediaType) || isPlain(mediaType)
                || isJson(mediaType) || isForm(mediaType)
                || isHtml(mediaType) || isXml(mediaType);
    }

    public static boolean isText(MediaType mediaType) {
        if (mediaType == null || mediaType.type() == null) {
            return false;
        }
        return mediaType.type().equals("text");
    }

    public static boolean isPlain(MediaType mediaType) {
        if (mediaType == null || mediaType.subtype() == null) {
            return false;
        }
        return mediaType.subtype().toLowerCase().contains("plain");
    }

    public static boolean isJson(MediaType mediaType) {
        if (mediaType == null || mediaType.subtype() == null) {
            return false;
        }
        return mediaType.subtype().toLowerCase().contains("json");
    }

    public static boolean isXml(MediaType mediaType) {
        if (mediaType == null || mediaType.subtype() == null) {
            return false;
        }
        return mediaType.subtype().toLowerCase().contains("xml");
    }

    public static boolean isHtml(MediaType mediaType) {
        if (mediaType == null || mediaType.subtype() == null) {
            return false;
        }
        return mediaType.subtype().toLowerCase().contains("html");
    }

    public static boolean isForm(MediaType mediaType) {
        if (mediaType == null || mediaType.subtype() == null) {
            return false;
        }
        return mediaType.subtype().toLowerCase().contains("x-www-form-urlencoded");
    }

    public static String convertCharset(Charset charset) {
        String s = charset.toString();
        int i = s.indexOf("[");
        if (i == -1) {
            return s;
        }
        return s.substring(i + 1, s.length() - 1);
    }


    /**
     * zlib decompress 2 String
     *
     * @param bytesToDecompress
     * @return
     */
    public static String decompressToStringForZlib(byte[] bytesToDecompress) {
        return decompressToStringForZlib(bytesToDecompress, "UTF-8");
    }

    /**
     * zlib decompress 2 String
     *
     * @param bytesToDecompress
     * @param charsetName
     * @return
     */
    public static String decompressToStringForZlib(byte[] bytesToDecompress, String charsetName) {
        byte[] bytesDecompressed = decompressForZlib
                (
                        bytesToDecompress
                );

        String returnValue = null;

        try {
            returnValue = new String
                    (
                            bytesDecompressed,
                            0,
                            bytesDecompressed.length,
                            charsetName
                    );
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        return returnValue;

    }

    /**
     * zlib decompress 2 byte
     *
     * @param bytesToDecompress
     * @return
     */
    public static byte[] decompressForZlib(byte[] bytesToDecompress) {
        byte[] returnValues = null;

        Inflater inflater = new Inflater();

        int numberOfBytesToDecompress = bytesToDecompress.length;

        inflater.setInput
                (
                        bytesToDecompress,
                        0,
                        numberOfBytesToDecompress
                );

        int bufferSizeInBytes = numberOfBytesToDecompress;

        int numberOfBytesDecompressedSoFar = 0;
        List<Byte> bytesDecompressedSoFar = new ArrayList<Byte>();

        try {
            while (inflater.needsInput() == false) {
                byte[] bytesDecompressedBuffer = new byte[bufferSizeInBytes];

                int numberOfBytesDecompressedThisTime = inflater.inflate
                        (
                                bytesDecompressedBuffer
                        );

                numberOfBytesDecompressedSoFar += numberOfBytesDecompressedThisTime;

                for (int b = 0; b < numberOfBytesDecompressedThisTime; b++) {
                    bytesDecompressedSoFar.add(bytesDecompressedBuffer[b]);
                }
            }

            returnValues = new byte[bytesDecompressedSoFar.size()];
            for (int b = 0; b < returnValues.length; b++) {
                returnValues[b] = (byte) (bytesDecompressedSoFar.get(b));
            }

        } catch (DataFormatException dfe) {
            dfe.printStackTrace();
        }

        inflater.end();

        return returnValues;
    }

    /**
     * zlib compress 2 byte
     *
     * @param bytesToCompress
     * @return
     */
    public static byte[] compressForZlib(byte[] bytesToCompress) {
        Deflater deflater = new Deflater();
        deflater.setInput(bytesToCompress);
        deflater.finish();

        byte[] bytesCompressed = new byte[Short.MAX_VALUE];

        int numberOfBytesAfterCompression = deflater.deflate(bytesCompressed);

        byte[] returnValues = new byte[numberOfBytesAfterCompression];

        System.arraycopy
                (
                        bytesCompressed,
                        0,
                        returnValues,
                        0,
                        numberOfBytesAfterCompression
                );

        return returnValues;
    }

    /**
     * zlib compress 2 byte
     *
     * @param stringToCompress
     * @return
     */
    public static byte[] compressForZlib(String stringToCompress) {
        byte[] returnValues = null;

        try {

            returnValues = compressForZlib
                    (
                            stringToCompress.getBytes("UTF-8")
                    );
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }

        return returnValues;
    }

    /**
     * gzip compress 2 byte
     *
     * @param string
     * @return
     * @throws IOException
     */
    public static byte[] compressForGzip(String string) {
        ByteArrayOutputStream os = null;
        GZIPOutputStream gos = null;
        try {
            os = new ByteArrayOutputStream(string.length());
            gos = new GZIPOutputStream(os);
            gos.write(string.getBytes("UTF-8"));
            byte[] compressed = os.toByteArray();
            return compressed;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(gos);
            closeQuietly(os);
        }
        return null;
    }

    /**
     * gzip decompress 2 string
     *
     * @param compressed
     * @return
     * @throws IOException
     */
    public static String decompressForGzip(byte[] compressed) {
        return decompressForGzip(compressed, "UTF-8");
    }

    /**
     * gzip decompress 2 string
     *
     * @param compressed
     * @param charsetName
     * @return
     */
    public static String decompressForGzip(byte[] compressed, String charsetName) {
        final int BUFFER_SIZE = compressed.length;
        GZIPInputStream gis = null;
        ByteArrayInputStream is = null;
        try {
            is = new ByteArrayInputStream(compressed);
            gis = new GZIPInputStream(is, BUFFER_SIZE);
            StringBuilder string = new StringBuilder();
            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = gis.read(data)) != -1) {
                string.append(new String(data, 0, bytesRead, charsetName));
            }
            return string.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(gis);
            closeQuietly(is);
        }
        return null;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
}
