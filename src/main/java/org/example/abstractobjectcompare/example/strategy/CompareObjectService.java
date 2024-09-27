package org.example.abstractobjectcompare.example.strategy;

public interface CompareObjectService<R, T> {

    R compareObjets(T source, T target);

}
