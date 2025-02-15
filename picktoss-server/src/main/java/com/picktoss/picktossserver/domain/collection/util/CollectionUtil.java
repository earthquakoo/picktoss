package com.picktoss.picktossserver.domain.collection.util;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSet;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class CollectionUtil {

    public int countCollectionSolvedMembers(Collection collection) {
        int solvedMemberCount = 0;

        Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
        Map<Collection, CollectionQuizSet> collectionQuizMap = new HashMap<>();

        for (CollectionQuiz collectionQuiz : collectionQuizzes) {
            if (!collectionQuiz.getCollectionQuizSetCollectionQuizzes().isEmpty()) {
                CollectionQuizSet collectionQuizSet = collectionQuiz.getCollectionQuizSetCollectionQuizzes().getFirst().getCollectionQuizSet();
                collectionQuizMap.putIfAbsent(collectionQuiz.getCollection(), collectionQuizSet);
            }
        }

        for (CollectionQuizSet collectionQuizSet : collectionQuizMap.values()) {
            if (collectionQuizSet.isSolved()) {
                solvedMemberCount += 1;
            }
        }
        return solvedMemberCount;
    }
}
