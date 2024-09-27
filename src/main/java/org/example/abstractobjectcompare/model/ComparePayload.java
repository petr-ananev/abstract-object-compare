package com.glowbyte.decision.diagram.dto.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.glowbyte.decision.core.enums.ComparisonResultType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.glowbyte.decision.core.enums.ComparisonResultType.ADDED;
import static com.glowbyte.decision.core.enums.ComparisonResultType.CHANGED;
import static com.glowbyte.decision.core.enums.ComparisonResultType.DELETED;
import static java.util.Objects.requireNonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparePayload {

    @JsonIgnore
    private Map<String, ComparisonResultType> fieldCompareMap;

    @JsonIgnore
    private Map<String, Map<String, ComparisonResultType>> complexTableCompareMap;

    @JsonInclude(NON_EMPTY)
    private Map<String, Object> comparisonMap;

    public ComparePayload concat(ComparePayload other) {
        mergeTableCompareMap(other.getComplexTableCompareMap());
        this.fieldCompareMap.putAll(other.getFieldCompareMap());
        this.comparisonMap.putAll(this.fieldCompareMap);
        this.comparisonMap.putAll(this.complexTableCompareMap);
        return this;
    }

    private void mergeTableCompareMap(Map<String, Map<String, ComparisonResultType>> source) {
        source.forEach(
                (rowKey, compareMap) -> complexTableCompareMap.merge(rowKey, compareMap, (sourceMap, targetMap) -> {
                    sourceMap.putAll(targetMap);
                    return sourceMap;
                }));
    }

    public static ComparePayload init() {
        return new ComparePayload(new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public void fieldWasChanged(String fieldName) {
        addFieldComparisonResultType(fieldName, CHANGED);
    }

    public void fieldWasAdded(String fieldName) {
        addFieldComparisonResultType(fieldName, ADDED);
    }

    public void fieldWasDeleted(String fieldName) {
        addFieldComparisonResultType(fieldName, DELETED);
    }

    public void rowWasChanged(String arrayName, String rowKey) {
        addComplexTableComparisonResultType(arrayName, rowKey, CHANGED);
    }

    public void rowWasAdded(String arrayName, String rowKey) {
        addComplexTableComparisonResultType(arrayName, rowKey, ADDED);
    }

    public void rowWasDeleted(String arrayName, String rowKey) {
        addComplexTableComparisonResultType(arrayName, rowKey, DELETED);
    }

    public void addComplexTableComparisonResultType(String arrayName, String rowKey,
                                                    ComparisonResultType comparisonResultType) {
        requireNonNull(arrayName, "Наименование коллекции не может быть null");
        requireNonNull(rowKey, "Идентификатор ключа не может быть null");
        requireNonNull(comparisonResultType, "Сообщение не может быть null");
        complexTableCompareMap.computeIfAbsent(arrayName, k -> new HashMap<>());
        Map<String, ComparisonResultType> arrayCompares = this.complexTableCompareMap.get(arrayName);
        arrayCompares.putIfAbsent(rowKey, comparisonResultType);
        this.comparisonMap.putAll(this.complexTableCompareMap);
    }

    public void addFieldComparisonResultType(String fieldName,
                                             ComparisonResultType comparisonResultType) {
        requireNonNull(fieldName, "Наименование поля не может быть null");
        requireNonNull(comparisonResultType, "Результат сравнения не может быть null");
        this.fieldCompareMap.put(fieldName, comparisonResultType);
        this.comparisonMap.putAll(this.fieldCompareMap);
    }

}
