package org.example.abstractobjectcompare.service;

public interface CompareFacade<R,T> {

    R compare(T source, T target);

}
