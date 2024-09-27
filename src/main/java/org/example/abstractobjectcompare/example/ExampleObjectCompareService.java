package org.example.abstractobjectcompare.example;

import lombok.RequiredArgsConstructor;
import org.example.abstractobjectcompare.example.dto.ExampleCollectionDto;
import org.example.abstractobjectcompare.example.dto.ExampleObjectCompareDto;
import org.example.abstractobjectcompare.example.dto.ExampleObjectCompareWrapper;
import org.example.abstractobjectcompare.example.dto.ExampleObjectDto;
import org.example.abstractobjectcompare.example.dto.ExampleObjectDto.Fields;
import org.example.abstractobjectcompare.example.strategy.CompareObjectService;
import org.example.abstractobjectcompare.example.strategy.FindComparableObjectService;
import org.example.abstractobjectcompare.model.ComparePayload;
import org.example.abstractobjectcompare.service.CompareHelper;
import org.example.abstractobjectcompare.service.CompareHelper.CompareCollectionParams;
import org.example.abstractobjectcompare.service.CompareHelper.CompareFieldParams;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.abstractobjectcompare.service.CompareHelper.CompareFieldParams.CompareFieldType.ONLY_EQUALS;

@Service
@RequiredArgsConstructor
public class ExampleObjectCompareService implements FindComparableObjectService<ExampleObjectCompareDto>,
                                                    CompareObjectService<ExampleObjectCompareWrapper, ExampleObjectCompareDto> {

    private final CompareHelper compareHelper;

    @Override
    public ExampleObjectCompareWrapper compareObjets(ExampleObjectCompareDto source, ExampleObjectCompareDto target) {
        ComparePayload sourcePayload = ComparePayload.init();
        ComparePayload targetPayload = ComparePayload.init();
        CompareFieldParams<ExampleObjectCompareDto> compareFieldParams =
                CompareFieldParams.<ExampleObjectCompareDto>builder()
                        .source(source)
                        .target(target)
                        .sourcePayload(sourcePayload)
                        .targetPayload(targetPayload)
                        .fieldName(Fields.name)
                        .fieldSelector(Pair.of(ExampleObjectDto::getName, ONLY_EQUALS))
                        .build();
        compareHelper.compareFields(compareFieldParams);
        CompareCollectionParams<ExampleCollectionDto> compareCollectionParams
                = CompareCollectionParams.<ExampleCollectionDto>builder()
                .collectionName(Fields.collections)
                .sourcePayload(sourcePayload)
                .targetPayload(targetPayload)
                .sourceMap(source.getCollections().stream().collect(Collectors.toMap(ExampleCollectionDto::getId, Function.identity())))
                .targetMap(target.getCollections().stream().collect(Collectors.toMap(ExampleCollectionDto::getId, Function.identity())))
                .predicate((s, t) -> !s.equals(t))
                .build();
        compareHelper.compareCollections(compareCollectionParams);

//        {
//            "source": {
//            "comparePayload": {
//                "comparisonMap": {
//                    "name": "CHANGED",
//                            "collections": {
//                        "bb2813e4-32c7-4113-94c8-45add9d359db": "DELETED",
//                                "222813e4-32c7-4113-94c8-45add9d35933": "ADDED"
//                    }
//                }
//            }
//        },
//            "target": {
//            "comparePayload": {
//                "comparisonMap": {
//                    "name": "CHANGED",
//                            "collections": {
//                        "bb2813e4-32c7-4113-94c8-45add9d359db": "DELETED",
//                                "222813e4-32c7-4113-94c8-45add9d35933": "ADDED"
//                    }
//                }
//            }
//        }
//        }


        return ExampleObjectCompareWrapper.builder()
                .source(source)
                .target(target)
                .build();
    }

    @Override
    public ExampleObjectCompareDto findObject(String id) {
        //return modelMapper.map(diagramDao.findDiagramNodesLinksByVersionId(id)
        //                                            .orElseThrow(() -> new DiagramNotFoundException(id, GET)),
        //                                  ExampleObjectCompareDto.class);
        //
        return ExampleObjectCompareDto.builder()
                .name("name")
                .id("1")
                .build();
    }

}
