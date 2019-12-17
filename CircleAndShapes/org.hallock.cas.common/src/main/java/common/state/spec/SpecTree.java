package common.state.spec;

import common.util.json.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class SpecTree<T> {

    public interface NodeVisitor<T> { void visit(ArrayList<String> path, T node); }
    public interface TypeChanger<T, G> { G toType(ArrayList<String> path, T node); }

    public SpecNode<T> root;

    public SpecTree() { root = new SpecBranchNode<>(); }
    public SpecTree(SpecNode<T> root) { this.root = root; }


    public void setRoot(SpecNode<T> tSpecNode) {
        this.root = tSpecNode;
    }

    public boolean isNotEmpty() {
        return root.isNotEmpty();
    }

    public SpecNode<T> get(String[] path) {
        return root.get(path, 0);
    }

    public Collection<T> collect() {
        return root.collect(new LinkedList<>());
    }

    public boolean anyMatch(Predicate<? super T> p)  {
        return root.anyMatch(p);
    }

    public void visit(NodeVisitor<T> visitor) {
        root.visit(new ArrayList<>(), visitor);
    }

    public <G> SpecTree<G> toType(TypeChanger<T, G> changer) {
        return new SpecTree<>(root.toType(new ArrayList<>(), changer));
    }

    public static abstract class SpecNode<T> {
        protected abstract boolean isNotEmpty();

        protected abstract SpecNode<T> get(String[] path, int i);

        protected abstract LinkedList<T> collect(LinkedList<T> objects);

        protected abstract boolean anyMatch(Predicate<? super T> p);

        public abstract Set<String> getChildren();

        public abstract T getValue();

        public abstract void visit(ArrayList<String> list, NodeVisitor<T> visitor);

        public abstract <G> SpecNode<G> toType(ArrayList<String> path, TypeChanger<T, G> changer);
    }

    public static final class SpecBranchNode<T> extends SpecNode<T> {
        public final Map<String, SpecNode<T>> children = new HashMap<>();

        protected boolean isNotEmpty() {
            for (SpecNode n : children.values())
                if (n.isNotEmpty()) return true;
            return false;
        }

        protected SpecNode<T> get(String[] path, int i) {
            if (i >= path.length - 1) return this;
            return children.get(path[i]).get(path, i + 1);
        }

        protected LinkedList<T> collect(LinkedList<T> objects) {
            for (SpecNode<T> n : children.values())
                n.collect(objects);
            return objects;
        }

        protected boolean anyMatch(Predicate<? super T> p) {
            for (SpecNode<T> n : children.values())
                if (n.anyMatch(p)) return true;
            return false;
        }

        @Override
        public Set<String> getChildren() {
            return children.keySet();
        }

        @Override
        public T getValue() {
            return null;
        }

        @Override
        public void visit(ArrayList<String> list, NodeVisitor<T> visitor) {
            for (Map.Entry<String, SpecNode<T>> entry : children.entrySet()) {
                list.add(list.size(), entry.getKey());
                entry.getValue().visit(list, visitor);
                list.remove(list.size() - 1);
            }
        }

        @Override
        public <G> SpecNode<G> toType(ArrayList<String> path, TypeChanger<T, G> changer) {
            SpecBranchNode<G> ret = new SpecBranchNode<>();
            for (Map.Entry<String, SpecNode<T>> entry : children.entrySet()) {
                path.add(path.size(), entry.getKey());
                ret.children.put(entry.getKey(), entry.getValue().toType(path, changer));
                path.remove(path.size() - 1);
            }
            return ret;
        }
    }

    public static final class SpecLeafNode<T> extends SpecNode<T> {
        public final T value;

        public SpecLeafNode(T value) {
            this.value = value;
        }

        protected boolean isNotEmpty() {
            return value != null;
        }

        protected SpecNode<T> get(String[] path, int i) {
            if (i != path.length - 1) throw new IllegalStateException();
            return this;
        }

        protected LinkedList<T> collect(LinkedList<T> objects) {
            objects.add(value);
            return objects;
        }

        protected boolean anyMatch(Predicate<? super T> p) {
            return p.test(value);
        }

        @Override
        public Set<String> getChildren() {
            return Collections.emptySet();
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public void visit(ArrayList<String> list, NodeVisitor<T> visitor) {
            visitor.visit(list, value);
        }

        @Override
        public <G> SpecNode<G> toType(ArrayList<String> path, TypeChanger<T, G> changer) {
            return new SpecLeafNode<>(changer.toType(path, value));
        }
    }



    public static class SpecNodeReference implements Jsonable {
        public final EntitySpec entity;
        public final String[] path;

        public SpecNodeReference(EntitySpec entity, String[] path) {
            this.entity = entity;
            this.path = path;
        }

        @Override
        public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
            writer.write("entity", entity, EntitySpec.Serializer, options);
            writer.write("path", path, DataSerializer.StringSerializer, options);
            writer.writeEndDocument();
        }

        public static final DataSerializer<SpecNodeReference> Serializer = new DataSerializer.JsonableSerializer<SpecNodeReference>() {
            @Override
            public SpecNodeReference parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
                reader.readBeginDocument();
                EntitySpec entity = reader.read("entity", EntitySpec.Serializer, spec);
                String[] path = reader.read("path", new String[0], DataSerializer.StringSerializer, spec);
                reader.readEndDocument();
                return new SpecNodeReference(entity, path);
            }
        };
    }
}
