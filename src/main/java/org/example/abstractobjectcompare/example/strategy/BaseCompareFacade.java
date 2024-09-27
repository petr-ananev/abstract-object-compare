package com.glowbyte.decision.diagram.service.diagram.compare;

import lombok.RequiredArgsConstructor;

/**
 * @see CompareConfiguration
 */
@RequiredArgsConstructor
public class BaseCompareFacade<R, T> implements CompareFacade<R, String> {

    private final CompareObjectService<R, T> compareObjectServiceService;

    private final FindComparableObjectService<T> findComparableObjectService;

    @Override
    public R compare(String sourceId, String targetId) {
        T source = findComparableObjectService.findObject(sourceId);
        T target = findComparableObjectService.findObject(targetId);
        return compareObjectServiceService.compareObjets(source, target);
    }

}
