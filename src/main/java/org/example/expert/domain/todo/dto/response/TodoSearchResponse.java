package org.example.expert.domain.todo.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class TodoSearchResponse {

    private final String title;
    private final long managerCount;
    private final long commentCount;

    @QueryProjection
    public TodoSearchResponse(String title, long managerCount, long commentCount) {
        this.title = title;
        this.managerCount = managerCount;
        this.commentCount = commentCount;
    }
}
