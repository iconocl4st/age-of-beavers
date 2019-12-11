package common.util;

import java.util.*;

public class Immutable {

    // TODO: Collections.unmodifiable...



    private static class ImmutableCollection<T> implements Collection<T> {
        private Collection<T> delegate;

        protected ImmutableCollection(Collection<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                Iterator<T> delegateIterator = delegate.iterator();
                @Override
                public boolean hasNext() {
                    return delegateIterator.hasNext();
                }

                @Override
                public T next() {
                    return delegateIterator.next();
                }
            };
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T1> T1[] toArray(T1[] t1s) {
            return delegate.toArray(t1s);
        }

        @Override
        public boolean add(T t) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            return delegate.containsAll(collection);
        }

        @Override
        public boolean addAll(Collection<? extends T> collection) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Immutable");
        }
    }

    private static class ImmutableListIterator<T> implements ListIterator<T> {
        private final ListIterator<T> delegateIterator;

        private ImmutableListIterator(ListIterator<T> delegateIterator) {
            this.delegateIterator = delegateIterator;
        }

        @Override
        public boolean hasNext() {
            return delegateIterator.hasNext();
        }

        @Override
        public T next() {
            return delegateIterator.next();
        }

        @Override
        public boolean hasPrevious() {
            return delegateIterator.hasPrevious();
        }

        @Override
        public T previous() {
            return delegateIterator.previous();
        }

        @Override
        public int nextIndex() {
            return delegateIterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return delegateIterator.previousIndex();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public void set(T t) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public void add(T t) {
            throw new UnsupportedOperationException("Immutable");
        }
    }

    public static class ImmutableList<T> extends ImmutableCollection<T> implements List<T> {
        private final List<T> delegate;
        public ImmutableList(List<T> l) {
            super(l);
            this.delegate = l;
        }

        @Override
        public boolean addAll(int i, Collection<? extends T> collection) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public T get(int i) {
            return delegate.get(i);
        }

        @Override
        public T set(int i, T t) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public void add(int i, T t) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public T remove(int i) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public int indexOf(Object o) {
            return delegate.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return delegate.lastIndexOf(o);
        }

        @Override
        public ListIterator<T> listIterator() {
            return new ImmutableListIterator<>(delegate.listIterator());
        }

        @Override
        public ListIterator<T> listIterator(int i) {
            return new ImmutableListIterator<>(delegate.listIterator(i));
        }

        @Override
        public List<T> subList(int i, int i1) {
            return new ImmutableList<>(delegate.subList(i, i1));
        }
    }

    public static class ImmutableMap<K, V> implements Map<K, V> {
        private final Map<K, V> delegate;

        public ImmutableMap(Map<K, V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean containsKey(Object o) {
            return delegate.containsKey(o);
        }

        @Override
        public boolean containsValue(Object o) {
            return delegate.containsValue(o);
        }

        @Override
        public V get(Object o) {
            return delegate.get(o);
        }

        @Override
        public V put(K k, V v) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public V remove(Object o) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public Set<K> keySet() {
            return new ImmutableSet<K>(delegate.keySet());
        }

        @Override
        public Collection<V> values() {
            return new ImmutableCollection<>(delegate.values());
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return new ImmutableSet<>(delegate.entrySet());
        }

        public static <K, V> ImmutableMap<K, V> emptyMap() {
            // TODO:
            return new ImmutableMap<>(Collections.emptyMap());
        }
    }

    public static class ImmutableSet<T> extends ImmutableCollection<T> implements Set<T> {
        public ImmutableSet(Set<T> delegate) {
            super(delegate);
        }


        public static <T> ImmutableSet<T> emptySet() {
            // TODO:
            return new ImmutableSet<>(Collections.emptySet());
        }
        public static <T> ImmutableSet<T> from(T... values) {
            return new ImmutableArraySet<>(values);
        }
    }


    public static class ImmutableArraySet<T> extends ImmutableSet<T> {
        private T[] values;

        public ImmutableArraySet(T[] values) {
            super(null);
            this.values = values;
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean isEmpty() {
            return values.length == 0;
        }

        @Override
        public boolean contains(Object o) {
            for (T t : values)
                if (t.equals(o))
                    return true;
            return false;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int index = 0;
                @Override
                public boolean hasNext() {
                    return index < values.length;
                }

                @Override
                public T next() {
                    return values[index++];
                }
            };
        }

        @Override
        public T[] toArray() {
            return values;
        }

        @Override
        public <T1> T1[] toArray(T1[] t1s) {
            throw new UnsupportedOperationException("Just use the other one.");
        }

        @Override
        public boolean add(T t) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends T> collection) {
            for (T t : collection) {
                if (!contains(t)) return false;
            }
            return true;
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException("Immutable");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Immutable");
        }
    }
}
