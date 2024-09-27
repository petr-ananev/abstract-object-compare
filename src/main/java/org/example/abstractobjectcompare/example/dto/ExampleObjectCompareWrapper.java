package org.example.abstractobjectcompare.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ExampleObjectCompareWrapper {

    ExampleObjectCompareDto source;

    ExampleObjectCompareDto target;

}
