package com.glowbyte.decision.diagram.dto.diagram;

import com.glowbyte.decision.diagram.dto.diagram.DiagramDto.SourceCompareDiagram;
import com.glowbyte.decision.diagram.dto.diagram.DiagramDto.TargetCompareDiagram;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "set")
@EqualsAndHashCode(callSuper = true)
public class DiagramCompareWrapper extends DiagramDto implements SourceCompareDiagram,
                                                                 TargetCompareDiagram {

    DiagramCompareDto source;

    DiagramCompareDto target;

}
