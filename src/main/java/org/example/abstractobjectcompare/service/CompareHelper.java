package com.glowbyte.decision.diagram.service.diagram.compare;

import com.glowbyte.decision.diagram.dto.node.ComparePayload;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.CollectionName;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.FieldName;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.FieldSelector;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.KeySelector;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.Predicate;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.Source;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.SourceCollection;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.SourceComparePayload;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.SourceMap;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.Target;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.TargetCollection;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.TargetComparePayload;
import com.glowbyte.decision.diagram.service.diagram.compare.CompareParamsStage.TargetMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.glowbyte.decision.diagram.service.diagram.compare.CompareHelper.CompareFieldParams.CompareFieldType.ONLY_EQUALS;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

@Service
public class CompareHelper {

    public record ElementWithPrev<T>(T element, T prevElement) {}

    /**
     * Comparing the order of elements in a collection <br>
     * Collections: [A,B,C,D] vs [A,B,D,C] <br>
     * Result: [D,C] - changed <br>
     */
    public <T> void compareSortedCollections(CompareSortedCollectionParams<T> compareCollectionParams) {
        ComparePayload sourcePayload = compareCollectionParams.sourcePayload();
        ComparePayload targetPayload = compareCollectionParams.targetPayload();
        Function<T, String> keySelector = compareCollectionParams.keySelector();
        Map<String, ElementWithPrev<T>> sourceMap = buildElementWithNextMap(compareCollectionParams.sourceCollection(),
                                                                            keySelector);
        Map<String, ElementWithPrev<T>> targetMap = buildElementWithNextMap(compareCollectionParams.targetCollection(),
                                                                            keySelector);
        CompareKeysResult compareKeysResult = compareKeys(sourceMap.keySet(), targetMap.keySet());
        BiPredicate<ElementWithPrev<T>, ElementWithPrev<T>> predicate = (s, t) -> !Objects.equals(s.prevElement(),
                                                                                                  t.prevElement());
        for (String rowKey : compareKeysResult.inBoth()) {
            ElementWithPrev<T> source = sourceMap.get(rowKey);
            ElementWithPrev<T> target = targetMap.get(rowKey);
            String collectionName = compareCollectionParams.collectionName();
            if (predicate.test(source, target)) {
                sourcePayload.rowWasChanged(collectionName, keySelector.apply(source.element()));
                targetPayload.rowWasChanged(collectionName, keySelector.apply(target.element()));
            }
        }
    }

    /**
     * Creating a map with reference to the next element, by keySelector
     */
    private <T> Map<String, ElementWithPrev<T>> buildElementWithNextMap(List<T> collection, Function<T, String> keySelector) {
        if (isNull(collection)) {
            return emptyMap();
        }
        List<ElementWithPrev<T>> result = new ArrayList<>();
        Iterator<T> iterator = collection.iterator();
        T prev = null;
        while (iterator.hasNext()) {
            T current = iterator.next();
            result.add(new ElementWithPrev<>(current, prev));
            prev = current;
        }
        return result.stream().collect(Collectors.toMap(x -> keySelector.apply(x.element()), Function.identity()));
    }

    /**
     * Comparing collection elements based on the passed collection of predicates<br>
     * Collections: [A_1,B_1,C_1] vs [A_1,D_1,C_2] <br>
     * Result for source: B_1 - deleted, C_1 - changed <br>
     * Result for target: D_1 - added, C_1 - changed <br>
     */
    public <T> void compareCollections(CompareCollectionParams<T> compareCollectionParams) {
        List<BiPredicate<T, T>> predicates = compareCollectionParams.predicates();
        Map<String, T> sourceMap = compareCollectionParams.sourceMap();
        Map<String, T> targetMap = compareCollectionParams.targetMap();
        ComparePayload sourcePayload = compareCollectionParams.sourcePayload();
        ComparePayload targetPayload = compareCollectionParams.targetPayload();
        CompareKeysResult compareKeysResult = compareKeys(sourceMap.keySet(), targetMap.keySet());
        for (String rowKey : compareKeysResult.onlyInSource()) {
            sourcePayload.rowWasDeleted(compareCollectionParams.collectionName(), rowKey);
        }
        for (String rowKey : compareKeysResult.onlyInTarget()) {
            targetPayload.rowWasAdded(compareCollectionParams.collectionName(), rowKey);
        }
        for (String rowKey : compareKeysResult.inBoth()) {
            for (BiPredicate<T, T> predicate : predicates) {
                if (predicate.test(sourceMap.get(rowKey), targetMap.get(rowKey))) {
                    String collectionName = compareCollectionParams.collectionName();
                    sourcePayload.rowWasChanged(collectionName, rowKey);
                    targetPayload.rowWasChanged(collectionName, rowKey);
                }
            }
        }
    }

    /**
     * Comparing element based on the passed collection of predicates<br>
     * Elements: A_1 vs A_2 <br>
     * Result for source: A_1 - changed <br>
     * Result for target: A_2 - changed <br>
     */
    public <T> void compareFields(CompareFieldParams<T> compareFieldParams) {
        ComparePayload sourcePayload = compareFieldParams.sourcePayload();
        ComparePayload targetPayload = compareFieldParams.targetPayload();
        T source = compareFieldParams.source();
        T target = compareFieldParams.target();
        compareFieldParams.fieldSelectorMap()
                          .forEach((fieldName, pair) -> {
                              Object sourceField = isNull(source)
                                                   ? null
                                                   : pair.getKey().apply(source);
                              Object targetField = isNull(target)
                                                   ? null
                                                   : pair.getKey().apply(target);
                              if (pair.getValue() == ONLY_EQUALS) {
                                  if (isFalse(Objects.equals(sourceField, targetField))) {
                                      sourcePayload.fieldWasChanged(fieldName);
                                      targetPayload.fieldWasChanged(fieldName);
                                  }
                              } else {
                                  if (isNull(sourceField) && nonNull(targetField)) {
                                      sourcePayload.fieldWasAdded(fieldName);
                                  } else if (nonNull(sourceField) && isNull(targetField)) {
                                      targetPayload.fieldWasDeleted(fieldName);
                                  } else if (isFalse(Objects.equals(sourceField, targetField))) {
                                      sourcePayload.fieldWasChanged(fieldName);
                                      targetPayload.fieldWasChanged(fieldName);
                                  }
                              }
                          });
    }

    /**
     * Keys intersection <br>
     * Keys: [A,B,C] vs [A,B,D] <br>
     * Result: [A,B] - in both, [C] - only in source, [D] - only in target
     */
    public CompareKeysResult compareKeys(Collection<String> sourceKeys, Collection<String> targetKeys) {
        Set<String> onlyInSource = new HashSet<>(sourceKeys);
        onlyInSource.removeAll(targetKeys);
        Set<String> onlyInTarget = new HashSet<>(targetKeys);
        onlyInTarget.removeAll(sourceKeys);
        Set<String> inBoth = new HashSet<>(sourceKeys);
        inBoth.retainAll(targetKeys);
        return new CompareKeysResult(onlyInSource, onlyInTarget, inBoth);
    }

    public record CompareFieldParams<T>(T source, T target, ComparePayload sourcePayload,
                                        ComparePayload targetPayload,
                                        Map<String, Pair<Function<T, Object>, CompareFieldType>> fieldSelectorMap) {

        public enum CompareFieldType {
            ONLY_EQUALS,
            EQUALS_AND_ABSENT
        }

        public static <T> Source<Target<SourceComparePayload<TargetComparePayload<FieldName<FieldSelector<FinalStage<T>, T>>>>, T>, T> builder() {
            return source -> target -> sourcePayload -> targetPayload -> fieldName -> fieldSelector ->
                    new FinalStage<T>(source, target, sourcePayload, targetPayload,
                                      new HashMap<>(Map.of(fieldName, fieldSelector)));
        }


        @RequiredArgsConstructor
        public static class FinalStage<T> implements FieldName<FieldSelector<FinalStage<T>, T>> {

            private final T source;

            private final T target;

            private final ComparePayload sourcePayload;

            private final ComparePayload targetPayload;

            private final Map<String, Pair<Function<T, Object>, CompareFieldType>> fieldSelectorMap;


            public CompareFieldParams<T> build() {
                return new CompareFieldParams<>(source, target, sourcePayload, targetPayload, fieldSelectorMap);
            }

            @Override
            public FieldSelector<FinalStage<T>, T> fieldName(String fieldName) {
                return selector -> {
                    fieldSelectorMap.put(fieldName, selector);
                    return new FinalStage<>(source, target, sourcePayload, targetPayload,
                                            new HashMap<>(fieldSelectorMap));
                };
            }

        }

    }

    public record CompareKeysResult(Set<String> onlyInSource, Set<String> onlyInTarget, Set<String> inBoth) {}

    public record CompareSortedCollectionParams<T>(String collectionName, Function<T, String> keySelector,
                                                   List<T> sourceCollection,
                                                   List<T> targetCollection,
                                                   ComparePayload sourcePayload, ComparePayload targetPayload) {

        public static <T> CollectionName<SourceComparePayload<TargetComparePayload<KeySelector<SourceCollection<TargetCollection<FinalStage<T>, T>, T>, T>>>> builder() {
            return collectionName -> sourcePayload -> targetPayload -> keySelector -> sourceCollection -> targetCollection ->
                    new FinalStage<>(collectionName, sourcePayload, targetPayload, keySelector, sourceCollection,
                                     targetCollection);
        }


        @RequiredArgsConstructor
        public static class FinalStage<T> {

            private final String collectionName;

            private final ComparePayload sourcePayload;

            private final ComparePayload targetPayload;

            private final Function<T, String> keySelector;

            private final List<T> sourceCollection;

            private final List<T> targetCollection;

            public CompareSortedCollectionParams<T> build() {
                return new CompareSortedCollectionParams<>(collectionName, keySelector, sourceCollection,
                                                           targetCollection, sourcePayload,
                                                           targetPayload);
            }

        }

    }

    public record CompareCollectionParams<T>(String collectionName, Map<String, T> sourceMap, Map<String, T> targetMap,
                                             ComparePayload sourcePayload, ComparePayload targetPayload,
                                             List<BiPredicate<T, T>> predicates) {

        public static <T> CollectionName<SourceComparePayload<TargetComparePayload<SourceMap<TargetMap<Predicate<FinalStage<T>, T>, T>, T>>>> builder() {
            return collectionName -> sourcePayload -> targetPayload -> sourceMap -> targetMap -> predicate ->
                    new FinalStage<>(collectionName, sourcePayload, targetPayload, sourceMap, targetMap,
                                     new ArrayList<>(List.of(predicate)));
        }


        @RequiredArgsConstructor
        public static class FinalStage<T> implements Predicate<FinalStage<T>, T> {

            private final String collectionName;

            private final ComparePayload sourcePayload;

            private final ComparePayload targetPayload;

            private final Map<String, T> sourceMap;

            private final Map<String, T> targetMap;

            private final List<BiPredicate<T, T>> predicates;

            @Override
            public FinalStage<T> predicate(BiPredicate<T, T> predicate) {
                this.predicates.add(predicate);
                return new FinalStage<>(collectionName, sourcePayload, targetPayload, sourceMap, targetMap,
                                        new ArrayList<>(this.predicates));
            }

            public CompareCollectionParams<T> build() {
                return new CompareCollectionParams<>(collectionName, sourceMap, targetMap, sourcePayload,
                                                     targetPayload,
                                                     predicates);
            }

        }

    }

}
