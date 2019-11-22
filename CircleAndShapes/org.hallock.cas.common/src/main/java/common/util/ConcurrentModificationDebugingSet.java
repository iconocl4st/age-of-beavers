package common.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ConcurrentModificationDebugingSet<T> implements Set<T> {

    private final Object sync = new Object();
    private int modificationCounter;
    private String lastModifier;

    private final Set<T> delegate;

    public ConcurrentModificationDebugingSet(Set<T> delegate) {
        this.delegate = delegate;
    }

    private void checkForModifications(int expected) {
        synchronized (sync) {
            if (modificationCounter == expected) return;
            System.out.println("Here.");
        }
    }

    private void madeModification() {
        synchronized (sync) {
            ++modificationCounter;
            lastModifier = Util.getDebugString();
        }
    }

    private int enter() {
        return modificationCounter;
    }

    private void exit(int expectedModificationCounter) {
        checkForModifications(expectedModificationCounter);
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
        int m = enter();
        boolean c = delegate.contains(o);
        exit(m);
        return c;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> it = delegate.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                int m = enter();
                boolean ret = it.hasNext();
                exit(m);
                return ret;
            }

            @Override
            public T next() {
                int m = enter();
                T ret = it.next();
                exit(m);
                return ret;
            }

            @Override
            public void remove() {
                madeModification();
                int m = enter();
                it.remove();
                exit(m);
                madeModification();
            }
        };
    }

    @Override
    public Object[] toArray() {
        int c = enter();
        Object[] objects = delegate.toArray();
        exit(c);
        return objects;
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        int mc = enter();
        T1[] objects = delegate.toArray(t1s);
        exit(mc);
        return objects;
    }

    @Override
    public boolean add(T t) {
        madeModification();
        int mc = enter();
        boolean ret = delegate.add(t);
        exit(mc);
        madeModification();
        return ret;
    }

    @Override
    public boolean remove(Object o) {
        madeModification();
        int mc = enter();
        boolean ret = delegate.remove(o);
        exit(mc);
        madeModification();
        return ret;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        int mc = enter();
        boolean ret = delegate.containsAll(collection);
        exit(mc);
        return ret;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        madeModification();
        int mc = enter();
        boolean ret = delegate.addAll(collection);
        exit(mc);
        madeModification();
        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        madeModification();
        int mc = enter();
        boolean ret = delegate.retainAll(collection);
        exit(mc);
        madeModification();
        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        madeModification();
        int mc = enter();
        boolean ret = delegate.removeAll(collection);
        exit(mc);
        madeModification();
        return ret;
    }

    @Override
    public void clear() {
        madeModification();
        int mc = enter();
        delegate.clear();
        exit(mc);
        madeModification();
    }
}
