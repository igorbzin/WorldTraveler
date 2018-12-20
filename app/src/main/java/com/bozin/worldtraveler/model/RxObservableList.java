package com.bozin.worldtraveler.model;

import android.annotation.TargetApi;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.subjects.PublishSubject;

public class RxObservableList<T> extends AbstractList<T> implements List<T>, ObservableSource<RxObservableList<T>> {

    private final String TAG = "MarkerActivity";
    private ArrayList<T> elements;
    private final PublishSubject<RxObservableList<T>> onAdd = PublishSubject.create();

    public RxObservableList(List<T> elements) {
        this.elements = new ArrayList<>(elements);
    }

    public ArrayList<Uri> getList() {
        return (ArrayList<Uri>) elements;
    }

    public ArrayList<User> getUserList(){return (ArrayList<User>) elements;}

    public PublishSubject<RxObservableList<T>> getObservable(){
        return onAdd;
    }

    public void setList(List<T> list) {
        elements = new ArrayList<>();
        elements.addAll(list);
        onAdd.onNext(this);
    }

    // delegations
    @Override
    public boolean add(T t) {
        boolean added = elements.add(t);
        if(added) {
            onAdd.onNext(this);
        }
        return added;
    }



    @Override
    public T set(int index, T element) {
        T t = elements.set(index, element);
        onAdd.onNext(this);
        return t;
    }

    @Override
    public void add(int index, T element) {
        elements.add(index, element);
        onAdd.onNext(this);
        Log.d(TAG, "Elements size: " + elements.size());
    }

    @Override
    public T remove(int index) {
        T t = elements.remove(index);
        onAdd.onNext(this);
        return t;
    }

    @Override
    public int indexOf(Object o) {
        return elements.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return elements.lastIndexOf(o);
    }

    @Override
    public void clear() {
        elements.clear();
        onAdd.onNext(this);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean isAdded = elements.addAll(index, c);
        if(isAdded) {
            onAdd.onNext(this);
        }
        return isAdded;
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() { // can this mess things up?
        return elements.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int index) { // can this mess things up?
        return elements.listIterator(index);
    }

    @NonNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return elements.subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(!(o instanceof RxObservableList)) {
            return false;
        }
        return elements.equals(((RxObservableList) o).elements);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + elements.hashCode();
        return result;
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return elements.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] a) {
        return elements.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        boolean isRemoved = elements.remove(o);
        if(isRemoved) {
            onAdd.onNext(this);
        }
        return isRemoved;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        boolean isAdded = elements.addAll(c);
        if(isAdded) {
            onAdd.onNext(this);
        }
        return isAdded;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        boolean isRemoved = elements.removeAll(c);
        if(isRemoved) {
            onAdd.onNext(this);
        }
        return isRemoved;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        boolean isRetained = elements.retainAll(c);
        if(isRetained) {
            onAdd.onNext(this);
        }
        return isRetained;
    }

    @Override
    public String toString() {
        return Arrays.toString(elements.toArray());
    }

    @Override
    public T get(int index) {
        return elements.get(index);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    @TargetApi(24)
    public boolean removeIf(Predicate<? super T> filter) {
        boolean isRemoved = elements.removeIf(filter);
        if(isRemoved) {
            onAdd.onNext(this);
        }
        return isRemoved;
    }

    @Override
    @TargetApi(24)
    public void replaceAll(UnaryOperator<T> operator) {
        elements.replaceAll(operator);
        onAdd.onNext(this);
    }

    @Override
    @TargetApi(24)
    public void sort(Comparator<? super T> c) {
        elements.sort(c);
        onAdd.onNext(this);
    }

    @Override
    @TargetApi(24)
    public Spliterator<T> spliterator() {
        return elements.spliterator();
    }

    @Override
    @TargetApi(24)
    public Stream<T> stream() {
        return elements.stream();
    }

    @Override
    @TargetApi(24)
    public Stream<T> parallelStream() {
        return elements.parallelStream();
    }

    @Override
    @TargetApi(24)
    public void forEach(Consumer<? super T> action) {
        elements.forEach(action);
    }

    @Override
    public void subscribe(Observer<? super RxObservableList<T>> observer) {
        onAdd.subscribe(observer);
    }
}