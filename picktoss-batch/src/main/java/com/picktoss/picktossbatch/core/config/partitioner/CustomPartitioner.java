package com.picktoss.picktossbatch.core.config.partitioner;

import com.picktoss.picktossserver.domain.member.service.MemberService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ComponentScan(basePackages = {"com.picktoss.picktossserver"})
public class CustomPartitioner implements Partitioner {

    private final MemberService memberService;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        //isQuizNotificationEnabled = true 일 때만
        Tuple minIdAndMaxId = memberService.findMinIdAndMaxIdAndIsQuizNotificationEnabled();

        int minValue = 1;
        int maxValue = 40;

//        int minValue = ((Long) minIdAndMaxId.get(0)).intValue();
//        int maxValue = ((Long) minIdAndMaxId.get(1)).intValue();
        int eachValue = ((maxValue - minValue) / gridSize) + 1 ;

        HashMap<String, ExecutionContext> partition = new HashMap<>();

        int start = minValue;
        int end = eachValue;

        for (int i = 0; i < gridSize; i++) {

            // 값 셋팅
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.put("start", start);
            executionContext.put("end", end);

            // 파티셔너에 넣기
            partition.put(String.valueOf(i), executionContext);

            // 다음 값을 셋팅하기
            start = end + 1;
            end = end + eachValue;

        }
        return partition;
    }
}
