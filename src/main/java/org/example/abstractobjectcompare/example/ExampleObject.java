package org.example.abstractobjectcompare.example;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class ExampleObject {

    private String id;

    private String name;

    private List<ExampleCollection> collections;
}
