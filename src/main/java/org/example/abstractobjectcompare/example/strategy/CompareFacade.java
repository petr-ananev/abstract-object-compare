package org.example.abstractobjectcompare.example.strategy;

public interface CompareFacade<R,T> {

    R compare(T source, T target);

}
