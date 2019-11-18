package common.util.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;


public class JsonWrapper {

    public static JsonWriterWrapperSpec initializeStream(JsonWriterWrapperSpec writer, String name) throws IOException {
        writer.writeBeginDocument();
        writer.writeBeginArray(name);
        writer.writeBeginDocument();
        writer.flush();
        return writer;
    }

    public static void finishStream(JsonWriterWrapperSpec writer) throws IOException {
        writer.writeEndDocument();
        writer.writeEndArray();
        writer.writeEndDocument();
    }

    public static JsonReaderWrapperSpec initializeStream(JsonReaderWrapperSpec reader, String name) throws IOException {
        reader.readBeginDocument();
        reader.readBeginArray(name);
        return reader;
    }

    public static void finishStream(JsonReaderWrapperSpec reader) throws IOException {
        reader.readEndArray();
        reader.readEndDocument();
    }

    public static JsonWriterWrapperSpec createJacksonWriterWrapper(OutputStream outputStream) throws IOException {
//        OutputStream outputStream2 = new FileOutputStream(new File("test_json/example_" + System.currentTimeMillis() + "_" + Math.random() + ".json"));
        ObjectMapper writer = new ObjectMapper();
        JsonGenerator jGenerator = writer.getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
//        JsonGenerator jGenerator2 = writer.getFactory().createGenerator(outputStream2, JsonEncoding.UTF8);
//        jGenerator2.useDefaultPrettyPrinter();
        return /*new SplitJsonWriterWrapper(*/new JacksonWriterWrapper(jGenerator)/*, new JacksonWriterWrapper(jGenerator2))*/;
    }

    public static JsonReaderWrapperSpec createJacksonReaderWrapper(InputStream inputStream) throws IOException {
        ObjectMapper writer = new ObjectMapper();
        JsonParser jParser = writer.getFactory().createParser(inputStream);
        return new JacksonReaderWrapper(jParser);
    }

}
