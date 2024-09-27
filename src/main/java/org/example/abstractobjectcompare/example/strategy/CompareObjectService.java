package org.example.abstractobjectcompare.service;

public interface CompareObjectService<R, T> {

    R compareObjets(T source, T target);

}
