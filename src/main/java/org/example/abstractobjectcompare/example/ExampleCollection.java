package org.example.abstractobjectcompare.example;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@FieldNameConstants
public class ExampleCollection {

    private String id;

    private String name;

}
