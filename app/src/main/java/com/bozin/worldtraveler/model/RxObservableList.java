package com.bozin.worldtraveler.model;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class RxObservableList {

    public static class ObservableList<T> {

        private List<T> list;
        protected final PublishSubject<T> onAdd;

        public ObservableList() {
            this.list = new ArrayList<T>();
            this.onAdd = PublishSubject.create();
        }
        public void add(T value) {
            list.add(value);
            onAdd.onNext(value);
        }

        public void remove(T value){
            list.remove(value);
            onAdd.onNext(value);
        }

        public List<T> getList(){
            return list;
        }

        public void setList(List<T> value){
            list = value;
        }

        public Observable<T> getObservable() {
            return onAdd;
        }
    }
}