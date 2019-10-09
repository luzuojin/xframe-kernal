package dev.xframe.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * use synchronized replace lock
 * @see CopyOnWriteArrayList
 * @param <E>
 */
public class SyncCOWList<E> implements List<E>, RandomAccess {

	volatile Object[] array;

	final Object[] getArray() {
		return array;
	}

	final void setArray(Object[] a) {
		array = a;
	}

	public SyncCOWList() {
		setArray(new Object[0]);
	}

	public SyncCOWList(Collection<? extends E> c) {
		Object[] elements;
		if (c.getClass() == SyncCOWList.class)
			elements = ((SyncCOWList<?>)c).getArray();
		else {
			elements = c.toArray();
			// c.toArray might (incorrectly) not return Object[] (see 6260652)
			if (elements.getClass() != Object[].class)
				elements = Arrays.copyOf(elements, elements.length, Object[].class);
		}
		setArray(elements);
	}

	public SyncCOWList(E[] toCopyIn) {
		setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
	}

	public int size() {
		return getArray().length;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	private static boolean eq(Object o1, Object o2) {
		return (o1 == null) ? o2 == null : o1.equals(o2);
	}

	private static int indexOf(Object o, Object[] elements,
			int index, int fence) {
		if (o == null) {
			for (int i = index; i < fence; i++)
				if (elements[i] == null)
					return i;
		} else {
			for (int i = index; i < fence; i++)
				if (o.equals(elements[i]))
					return i;
		}
		return -1;
	}

	private static int lastIndexOf(Object o, Object[] elements, int index) {
		if (o == null) {
			for (int i = index; i >= 0; i--)
				if (elements[i] == null)
					return i;
		} else {
			for (int i = index; i >= 0; i--)
				if (o.equals(elements[i]))
					return i;
		}
		return -1;
	}

	public boolean contains(Object o) {
		Object[] elements = getArray();
		return indexOf(o, elements, 0, elements.length) >= 0;
	}

	public int indexOf(Object o) {
		Object[] elements = getArray();
		return indexOf(o, elements, 0, elements.length);
	}

	public int indexOf(E e, int index) {
		Object[] elements = getArray();
		return indexOf(e, elements, index, elements.length);
	}

	public int lastIndexOf(Object o) {
		Object[] elements = getArray();
		return lastIndexOf(o, elements, elements.length - 1);
	}

	public int lastIndexOf(E e, int index) {
		Object[] elements = getArray();
		return lastIndexOf(e, elements, index);
	}

	public Object[] toArray() {
		Object[] elements = getArray();
		return Arrays.copyOf(elements, elements.length);
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T a[]) {
		Object[] elements = getArray();
		int len = elements.length;
		if (a.length < len)
			return (T[]) Arrays.copyOf(elements, len, a.getClass());
		else {
			System.arraycopy(elements, 0, a, 0, len);
			if (a.length > len)
				a[len] = null;
			return a;
		}
	}

	// Positional Access Operations

	@SuppressWarnings("unchecked")
	private E get(Object[] a, int index) {
		return (E) a[index];
	}

	public E get(int index) {
		return get(getArray(), index);
	}

	public synchronized E set(int index, E element) {
		Object[] elements = getArray();
		E oldValue = get(elements, index);

		if (oldValue != element) {
			int len = elements.length;
			Object[] newElements = Arrays.copyOf(elements, len);
			newElements[index] = element;
			setArray(newElements);
		} else {
			// Not quite a no-op; ensures volatile write semantics
			setArray(elements);
		}
		return oldValue;
	}

	public synchronized boolean add(E e) {
		Object[] elements = getArray();
		int len = elements.length;
		Object[] newElements = Arrays.copyOf(elements, len + 1);
		newElements[len] = e;
		setArray(newElements);
		return true;
	}

	public synchronized void add(int index, E element) {
		Object[] elements = getArray();
		int len = elements.length;
		if (index > len || index < 0)
			throw new IndexOutOfBoundsException("Index: "+index+
					", Size: "+len);
		Object[] newElements;
		int numMoved = len - index;
		if (numMoved == 0)
			newElements = Arrays.copyOf(elements, len + 1);
		else {
			newElements = new Object[len + 1];
			System.arraycopy(elements, 0, newElements, 0, index);
			System.arraycopy(elements, index, newElements, index + 1,
					numMoved);
		}
		newElements[index] = element;
		setArray(newElements);
	}

	public synchronized E remove(int index) {
		Object[] elements = getArray();
		int len = elements.length;
		E oldValue = get(elements, index);
		int numMoved = len - index - 1;
		if (numMoved == 0)
			setArray(Arrays.copyOf(elements, len - 1));
		else {
			Object[] newElements = new Object[len - 1];
			System.arraycopy(elements, 0, newElements, 0, index);
			System.arraycopy(elements, index + 1, newElements, index,
					numMoved);
			setArray(newElements);
		}
		return oldValue;
	}

	public boolean remove(Object o) {
		Object[] snapshot = getArray();
		int index = indexOf(o, snapshot, 0, snapshot.length);
		return (index < 0) ? false : remove(o, snapshot, index);
	}

	private synchronized boolean remove(Object o, Object[] snapshot, int index) {
		Object[] current = getArray();
		int len = current.length;
		if (snapshot != current) findIndex: {
			int prefix = Math.min(index, len);
			for (int i = 0; i < prefix; i++) {
				if (current[i] != snapshot[i] && eq(o, current[i])) {
					index = i;
					break findIndex;
				}
			}
			if (index >= len)
				return false;
			if (current[index] == o)
				break findIndex;
			index = indexOf(o, current, index, len);
			if (index < 0)
				return false;
		}
		Object[] newElements = new Object[len - 1];
		System.arraycopy(current, 0, newElements, 0, index);
		System.arraycopy(current, index + 1,
				newElements, index,
				len - index - 1);
		setArray(newElements);
		return true;
	}

	synchronized void removeRange(int fromIndex, int toIndex) {
		Object[] elements = getArray();
		int len = elements.length;

		if (fromIndex < 0 || toIndex > len || toIndex < fromIndex)
			throw new IndexOutOfBoundsException();
		int newlen = len - (toIndex - fromIndex);
		int numMoved = len - toIndex;
		if (numMoved == 0)
			setArray(Arrays.copyOf(elements, newlen));
		else {
			Object[] newElements = new Object[newlen];
			System.arraycopy(elements, 0, newElements, 0, fromIndex);
			System.arraycopy(elements, toIndex, newElements,
					fromIndex, numMoved);
			setArray(newElements);
		}
	}

	public boolean addIfAbsent(E e) {
		Object[] snapshot = getArray();
		return indexOf(e, snapshot, 0, snapshot.length) >= 0 ? false :
			addIfAbsent(e, snapshot);
	}

	private synchronized boolean addIfAbsent(E e, Object[] snapshot) {
		Object[] current = getArray();
		int len = current.length;
		if (snapshot != current) {
			// Optimize for lost race to another addXXX operation
			int common = Math.min(snapshot.length, len);
			for (int i = 0; i < common; i++)
				if (current[i] != snapshot[i] && eq(e, current[i]))
					return false;
			if (indexOf(e, current, common, len) >= 0)
				return false;
		}
		Object[] newElements = Arrays.copyOf(current, len + 1);
		newElements[len] = e;
		setArray(newElements);
		return true;
	}

	public boolean containsAll(Collection<?> c) {
		Object[] elements = getArray();
		int len = elements.length;
		for (Object e : c) {
			if (indexOf(e, elements, 0, len) < 0)
				return false;
		}
		return true;
	}

	public synchronized boolean removeAll(Collection<?> c) {
		if (c == null) throw new NullPointerException();
		Object[] elements = getArray();
		int len = elements.length;
		if (len != 0) {
			// temp array holds those elements we know we want to keep
			int newlen = 0;
			Object[] temp = new Object[len];
			for (int i = 0; i < len; ++i) {
				Object element = elements[i];
				if (!c.contains(element))
					temp[newlen++] = element;
			}
			if (newlen != len) {
				setArray(Arrays.copyOf(temp, newlen));
				return true;
			}
		}
		return false;
	}

	public synchronized boolean retainAll(Collection<?> c) {
		if (c == null) throw new NullPointerException();
		Object[] elements = getArray();
		int len = elements.length;
		if (len != 0) {
			// temp array holds those elements we know we want to keep
			int newlen = 0;
			Object[] temp = new Object[len];
			for (int i = 0; i < len; ++i) {
				Object element = elements[i];
				if (c.contains(element))
					temp[newlen++] = element;
			}
			if (newlen != len) {
				setArray(Arrays.copyOf(temp, newlen));
				return true;
			}
		}
		return false;
	}

	public synchronized int addAllAbsent(Collection<? extends E> c) {
		Object[] cs = c.toArray();
		if (cs.length == 0)
			return 0;
		Object[] elements = getArray();
		int len = elements.length;
		int added = 0;
		// uniquify and compact elements in cs
		for (int i = 0; i < cs.length; ++i) {
			Object e = cs[i];
			if (indexOf(e, elements, 0, len) < 0 &&
					indexOf(e, cs, 0, added) < 0)
				cs[added++] = e;
		}
		if (added > 0) {
			Object[] newElements = Arrays.copyOf(elements, len + added);
			System.arraycopy(cs, 0, newElements, len, added);
			setArray(newElements);
		}
		return added;
	}

	public synchronized void clear() {
		setArray(new Object[0]);
	}

	public synchronized boolean addAll(Collection<? extends E> c) {
		Object[] cs = (c.getClass() == SyncCOWList.class) ?
				((SyncCOWList<?>)c).getArray() : c.toArray();
				if (cs.length == 0)
					return false;
				Object[] elements = getArray();
				int len = elements.length;
				if (len == 0 && cs.getClass() == Object[].class)
					setArray(cs);
				else {
					Object[] newElements = Arrays.copyOf(elements, len + cs.length);
					System.arraycopy(cs, 0, newElements, len, cs.length);
					setArray(newElements);
				}
				return true;
	}

	public synchronized boolean addAll(int index, Collection<? extends E> c) {
		Object[] cs = c.toArray();
		Object[] elements = getArray();
		int len = elements.length;
		if (index > len || index < 0)
			throw new IndexOutOfBoundsException("Index: "+index+", Size: "+len);
		if (cs.length == 0)
			return false;
		int numMoved = len - index;
		Object[] newElements;
		if (numMoved == 0)
			newElements = Arrays.copyOf(elements, len + cs.length);
		else {
			newElements = new Object[len + cs.length];
			System.arraycopy(elements, 0, newElements, 0, index);
			System.arraycopy(elements, index,
					newElements, index + cs.length,
					numMoved);
		}
		System.arraycopy(cs, 0, newElements, index, cs.length);
		setArray(newElements);
		return true;
	}

	public void forEach(Consumer<? super E> action) {
		if (action == null) throw new NullPointerException();
		Object[] elements = getArray();
		int len = elements.length;
		for (int i = 0; i < len; ++i) {
			@SuppressWarnings("unchecked") E e = (E) elements[i];
			action.accept(e);
		}
	}

	public synchronized boolean removeIf(Predicate<? super E> filter) {
		if (filter == null) throw new NullPointerException();
		Object[] elements = getArray();
		int len = elements.length;
		if (len != 0) {
			int newlen = 0;
			Object[] temp = new Object[len];
			for (int i = 0; i < len; ++i) {
				@SuppressWarnings("unchecked") E e = (E) elements[i];
				if (!filter.test(e))
					temp[newlen++] = e;
			}
			if (newlen != len) {
				setArray(Arrays.copyOf(temp, newlen));
				return true;
			}
		}
		return false;
	}

	public synchronized void replaceAll(UnaryOperator<E> operator) {
		if (operator == null) throw new NullPointerException();
		Object[] elements = getArray();
		int len = elements.length;
		Object[] newElements = Arrays.copyOf(elements, len);
		for (int i = 0; i < len; ++i) {
			@SuppressWarnings("unchecked") E e = (E) elements[i];
			newElements[i] = operator.apply(e);
		}
		setArray(newElements);
	}

	public synchronized void sort(Comparator<? super E> c) {
		Object[] elements = getArray();
		Object[] newElements = Arrays.copyOf(elements, elements.length);
		@SuppressWarnings("unchecked") E[] es = (E[])newElements;
		Arrays.sort(es, c);
		setArray(newElements);
	}

	public String toString() {
		return Arrays.toString(getArray());
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof List))
			return false;

		List<?> list = (List<?>)(o);
		Iterator<?> it = list.iterator();
		Object[] elements = getArray();
		int len = elements.length;
		for (int i = 0; i < len; ++i)
			if (!it.hasNext() || !eq(elements[i], it.next()))
				return false;
		if (it.hasNext())
			return false;
		return true;
	}

	public int hashCode() {
		int hashCode = 1;
		Object[] elements = getArray();
		int len = elements.length;
		for (int i = 0; i < len; ++i) {
			Object obj = elements[i];
			hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
		}
		return hashCode;
	}

	public Iterator<E> iterator() {
		return new COWIterator<E>(getArray(), 0);
	}

	public ListIterator<E> listIterator() {
		return new COWIterator<E>(getArray(), 0);
	}

	public ListIterator<E> listIterator(int index) {
		Object[] elements = getArray();
		int len = elements.length;
		if (index < 0 || index > len)
			throw new IndexOutOfBoundsException("Index: "+index);

		return new COWIterator<E>(elements, index);
	}

	public Spliterator<E> spliterator() {
		return Spliterators.spliterator(getArray(), Spliterator.IMMUTABLE | Spliterator.ORDERED);
	}

	static final class COWIterator<E> implements ListIterator<E> {
		private final Object[] snapshot;
		private int cursor;

		private COWIterator(Object[] elements, int initialCursor) {
			cursor = initialCursor;
			snapshot = elements;
		}

		public boolean hasNext() {
			return cursor < snapshot.length;
		}

		public boolean hasPrevious() {
			return cursor > 0;
		}

		@SuppressWarnings("unchecked")
		public E next() {
			if (! hasNext())
				throw new NoSuchElementException();
			return (E) snapshot[cursor++];
		}

		@SuppressWarnings("unchecked")
		public E previous() {
			if (! hasPrevious())
				throw new NoSuchElementException();
			return (E) snapshot[--cursor];
		}

		public int nextIndex() {
			return cursor;
		}

		public int previousIndex() {
			return cursor-1;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void set(E e) {
			throw new UnsupportedOperationException();
		}

		public void add(E e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void forEachRemaining(Consumer<? super E> action) {
			Objects.requireNonNull(action);
			Object[] elements = snapshot;
			final int size = elements.length;
			for (int i = cursor; i < size; i++) {
				@SuppressWarnings("unchecked") E e = (E) elements[i];
				action.accept(e);
			}
			cursor = size;
		}
	}

	public synchronized List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

}
