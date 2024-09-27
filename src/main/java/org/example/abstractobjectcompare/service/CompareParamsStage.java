package org.example.abstractobjectcompare.service;

import org.example.abstractobjectcompare.model.ComparePayload;
import org.example.abstractobjectcompare.service.CompareHelper.CompareFieldParams.CompareFieldType;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

public interface CompareParamsStage {

    @FunctionalInterface
    interface Source<S, T> {

        S source(T source);

    }

    @FunctionalInterface
    interface Target<S, T> {

        S target(T target);

    }


    @FunctionalInterface
    interface FieldName<S> {

        S fieldName(String fieldName);

    }

    @FunctionalInterface
    interface CollectionName<S> {

        S collectionName(String collectionName);

    }

    @FunctionalInterface
    interface SourceComparePayload<S> {

        S sourcePayload(ComparePayload sourcePayload);

    }

    @FunctionalInterface
    interface TargetComparePayload<S> {

        S targetPayload(ComparePayload targetPayload);

    }

    @FunctionalInterface
    interface SourceMap<S, T> {

        S sourceMap(Map<String, T> sourceMap);

    }

    @FunctionalInterface
    interface TargetMap<S, T> {

        S targetMap(Map<String, T> targetMap);

    }

    @FunctionalInterface
    interface KeySelector<S, T> {

        S keySelector(Function<T, String> keySelector);

    }

    @FunctionalInterface
    interface SourceCollection<S, T> {

        S sourceCollection(List<T> sourceMap);

    }

    @FunctionalInterface
    interface TargetCollection<S, T> {

        S targetCollection(List<T> targetMap);

    }

    @FunctionalInterface
    interface Predicate<S, T> {

        S predicate(BiPredicate<T, T> predicate);

    }

    @FunctionalInterface
    interface FieldSelector<S, T> {

        S fieldSelector(Pair<Function<T, Object>, CompareFieldType> selector);

    }


}
