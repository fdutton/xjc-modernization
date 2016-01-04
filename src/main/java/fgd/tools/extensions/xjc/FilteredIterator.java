package fgd.tools.extensions.xjc;

import java.util.Iterator;
import java.util.NoSuchElementException;

abstract class FilteredIterator<T> implements Iterator<T> {
    private Iterator<T> iterator;
    private T nextElement;
    private boolean hasNext;

    public FilteredIterator(Iterator<T> iterator) {
        this.iterator = iterator;
        nextMatch();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public T next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }

        return nextMatch();
    }

    private T nextMatch() {
    	T oldMatch = nextElement;

        while (iterator.hasNext()) {
        	T o = iterator.next();

            if (isMatch(o)) {
                hasNext = true;
                nextElement = o;

                return oldMatch;
            }
        }

        hasNext = false;

        return oldMatch;
    }

	protected abstract boolean isMatch(T o);

    @Override
    public void remove() {
        iterator.remove();
    }
}