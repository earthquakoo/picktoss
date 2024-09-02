package com.picktoss.picktosssendemailbatch.core.config;

import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomPartitioner implements Partitioner {

    private final MemberRepository memberRepository;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Tuple minIdAndMaxId = memberRepository.findMinIdAndMaxId();

        int minValue = ((Long) minIdAndMaxId.get(0)).intValue();
        int maxValue = ((Long) minIdAndMaxId.get(1)).intValue();
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
