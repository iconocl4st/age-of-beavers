package common.state.edit;

import common.state.spec.EntitySpec;
import common.state.spec.ResourceType;
import common.util.Immutable;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class Interfaces {

    public interface NameGetter<T> {
        String getName(T t);

        static <T> boolean nameExists(Collection<T> c, NameGetter<T> g, String v) {
            return get(c, g, v) != null;
        }

        static <T> T get(Collection<T> c, NameGetter<T> g, String v) {
            for (T t : c) if (g.getName(t).equals(v)) return t;
            return null;
        }
    }

    public interface Remover<T> {
        void remove(T t);
    }

    public interface PropertyListener<T> {
        void propertySet(T t);
    }

    static class Container<T> {
        T value;

        Container(T t) { this.value = t;}
    }

    public interface Creator<T> {
        T create(String name);
    }

    public interface Editor<T> {
        void edit(T t);
    }

    static class InheritedValue<T> {
        final T value;
        final String from;

        InheritedValue(T value, String from) {
            this.value = value;
            this.from = from;
        }

        public String toString() {
            return appendTo(new StringBuilder()).toString();
        }

        StringBuilder appendTo(StringBuilder builder) {
            builder.append(from).append(": ");
            if (value instanceof Dimension) {
                Dimension value = (Dimension) this.value;
                builder.append("[").append(value.width).append(",").append(value.height).append("]");
            } else {
                builder.append(value);
            }
            return builder;
        }

        // TODO:
        static String toString(LinkedList<Object> values) {
            StringBuilder builder = new StringBuilder();
            for (Object value : values)
                ((InheritedValue<?>)value).appendTo(builder);
            return builder.toString();
        }

//        static String toString(LinkedList<InheritedValue<?>> values) {
//            StringBuilder builder = new StringBuilder();
//            for (InheritedValue value : values)
//                value.appendTo(builder);
//            return builder.toString();
//        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Maybe these should be inside the creators...
    interface SpecCreator<T> {
        T create(CreationContext cntxt);
        void compile(Creators.GameSpecCreator creator);
        void parse(JSONObject object);
        void save(JSONObject obj);
        Creators.CreatorType getType();
        void getExportErrors(Creators.GameSpecCreator creator, Errors errors, ErrorCheckParams params);
    }


    static class CreationContext {
        Creators.GameSpecCreator creator;
        Immutable.ImmutableList<ResourceType> resourceTypes;
        Immutable.ImmutableList<EntitySpec> unitTypes;
        HashMap<String, Object> args = new HashMap<>();

        CreationContext(Creators.GameSpecCreator creator) {
            this.creator = creator;
        }

        CreationContext setArg(String s, Object o) {
            CreationContext c = new CreationContext(creator);
            c.resourceTypes = resourceTypes;
            c.unitTypes = unitTypes;
            c.args.putAll(args);
            c.args.put(s, o);
            return c;
        }
    }

    static class ErrorCheckParams {
        boolean canBeNull = true;

        ErrorCheckParams() {}

        ErrorCheckParams(ErrorCheckParams other) {
            this.canBeNull = other.canBeNull;
        }

        ErrorCheckParams cannotBeNull() {
            ErrorCheckParams errorCheckParams = new ErrorCheckParams(this);
            errorCheckParams.canBeNull = false;
            return errorCheckParams;
        }
    }

    static class Errors {
        LinkedList<ExportError> errors = new LinkedList<>();
        LinkedList<String> currentPath = new LinkedList<>();

        void pushPath(String path) {
            currentPath.addLast(path);
        }

        void popPath() {
            currentPath.removeLast();
        }

        private static final String[] DUMMY = new String[0];
        String[] getCurrentPath() {
            return currentPath.toArray(DUMMY);
        }

        void add(ExportError error) {
            errors.add(error);
        }

        void nonNull(ValuedCreator nc) {
            nonNull(nc, nc.getFieldName());
        }

        void nonNull(NullableCreator nc) {
            nonNull(nc, nc.getFieldName());
        }

        void nonNull(ValueCreator vc, String msg) {
            if (!vc.isNull()) return;
            add(new ExportError(getCurrentPath(), "Cannot be null: " + msg));
        }

        public void error(String msg) {
            errors.add(new ExportError(getCurrentPath(), msg));
        }

        void checkAll(Collection<? extends SpecCreator<?>> creators, Creators.GameSpecCreator c, ErrorCheckParams params) {
            int index = 0;
            for (SpecCreator<?> creator : creators) {
                pushPath(String.valueOf(index++));
                creator.getExportErrors(c, this, params);
                popPath();
            }
        }

        P withPath(String path) {
            pushPath(path);
            return this::popPath;
        }
    }

    static class ExportError {
        String[] path;
        String message;

        ExportError(String[] path, String message) {
            this.path = path;
            this.message = message;
        }

        StringBuilder append(StringBuilder builder) {
            for (String s : path) builder.append(s).append(' ');
            return builder.append(": ").append(message);
        }
    }

    interface ValueCreator<T> extends SpecCreator<T>{
        String getFieldName();
        void setNull(boolean isNull);
        boolean isNull();
        Creators.CreatorType getType();
    }

    static abstract class NullableCreator<T> implements ValueCreator<T> {
        String fieldName;
        boolean isNull; // Having this field has been grief...

        protected NullableCreator(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public final String getFieldName() {
            return fieldName;
        }

        public final void setNull(boolean isNull) {
            this.isNull = isNull;
        }

        public final boolean isNull() {
            return this.isNull;
        }

        public final void parse(JSONObject object) {
            if (!object.has(fieldName)) {
                setNull(true);
                return;
            }
            parseNonNull(object);
            isNull = false;
        }
        public final void save(JSONObject obj) {
            if (isNull()) return;
            saveNonNull(obj);
        }

        abstract void parseNonNull(JSONObject object);
        abstract void saveNonNull(JSONObject object);
    }

    static abstract class ValuedCreator<T> implements ValueCreator<T> {
        String fieldName;
        private final Creators.CreatorType type;
        protected T value;

        ValuedCreator(String fieldName, Creators.CreatorType t) {
            this.fieldName = fieldName;
            type = t;
        }

        @Override
        public final void parse(JSONObject obj) {
            if (!obj.has(fieldName)) return;
            set(parseNonNull(obj));
        }

        @Override
        public final T create(CreationContext cntxt) {
            return get();
        }

        @Override
        public final void compile(Creators.GameSpecCreator creator) {}

        @Override
        public final void save(JSONObject obj) {
            if (isNull()) return;
            saveNonNull(obj);
        }

        void saveNonNull(JSONObject obj) {
            obj.put(fieldName, get());
        }

        public void set(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public T getNonNull() {
            if (value == null) {
                throw new NullPointerException();
            }
            return value;
        }

        public final boolean isNull() {
            return value == null;
        }

        public final Creators.CreatorType getType() {
            return type;
        }

        public final String getFieldName() {
            return fieldName;
        }

        @Override
        public void setNull(boolean isNull) {
            if (isNull) value = null;
            else value = getDefaultValue();
        }

        abstract T getDefaultValue();
        abstract T parseNonNull(JSONObject obj);
    }

    interface Saver {
        void save(Path p) throws IOException;
    }

    interface ComponentCreator {
        Component create();
    }

    interface Named {
        String name();
    }
//
//    static abstract class Value<T> {
//        String name;
//        T value;
//
//        Value(String name) {
//            this.name = name;
//        }
//
//        abstract void parse(JSONObject object);
//        abstract void write(JSONObject obj);
//
//        public static Value<Integer> integerValue(String name) {
//            return new Value<Integer>(name) {
//                @Override
//                void parse(JSONObject object) {
//                    value = (int)(long) object.get(name);
//                }
//
//                @Override
//                void write(JSONObject obj) {
//                    obj.put(name, value);
//                }
//            };
//        }
//    }
}
