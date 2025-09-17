package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.QTodoSearchResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(
            String title,
            String nickname,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        // 결과 조회
        List<TodoSearchResponse> results = queryFactory
                .select(new QTodoSearchResponse(
                        todo.title,
                        manager.id.countDistinct(),
                        comment.id.countDistinct()
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)   // 담당자 기준 닉네임 검색
                .leftJoin(todo.comments, comment)
                .where(
                        title != null ? todo.title.contains(title) : null,
                        nickname != null ? user.nickname.contains(nickname) : null,
                        startDate != null ? todo.createdAt.goe(startDate) : null,
                        endDate != null ? todo.createdAt.loe(endDate) : null
                )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // total 카운트 (메인 쿼리와 동일한 join 구조 유지)
        Long total = queryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)   // 담당자 기준 닉네임 검색 반영
                .where(
                        title != null ? todo.title.contains(title) : null,
                        nickname != null ? user.nickname.contains(nickname) : null,
                        startDate != null ? todo.createdAt.goe(startDate) : null,
                        endDate != null ? todo.createdAt.loe(endDate) : null
                )
                .fetchOne();

        return PageableExecutionUtils.getPage(results, pageable, () -> total != null ? total : 0L);
    }
}
