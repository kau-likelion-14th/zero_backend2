@Service
@RequiredArgsConstructor
public class TodoService {

    private final CategoryRepository categoryRepository;
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoDateRepository todoDateRepository;
    private final RoutineTodoDateGenerator routineTodoDateGenerator;

    /** 헬퍼 메서드: 반복되는 검증을 한 곳에 **/
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
    }

    private Todo getTodoOrThrow(Long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> new GeneralException(ErrorCode.TODO_NOT_FOUND));
    }

    // 투두 소유자 검증: 남의 투두에 손대면 403
    private void assertOwner(User user, Todo todo) {
        if (!todo.getUser().getId().equals(user.getId())) {
            throw new GeneralException(ErrorCode.TODO_ACCESS_DENIED);
        }
    }

    private Category getCategoryOrThrow(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new GeneralException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // 루틴 규칙 검증: 종료일/요일 필수, 기간 순서 검증
    private void validateRoutineRequest(LocalDate startDate, LocalDate endDate, WeekEnum week) {
        if (endDate == null) {
            throw new GeneralException(ErrorCode.TODO_ROUTINE_END_DATE_REQUIRED);
        }
        if (week == null) {
            throw new GeneralException(ErrorCode.TODO_ROUTINE_WEEK_REQUIRED);
        }
        if (startDate != null && startDate.isAfter(endDate)) {
            throw new GeneralException(ErrorCode.TODO_ROUTINE_DATE_RANGE_INVALID);
        }
    }

    /** 투두리스트 조회 **/
    @Transactional(readOnly = true)
    public List<TodoListResponse> getTodosByDate(Long userId, LocalDate date) {
        User user = getUserOrThrow(userId);
        // 유저 + 날짜 기준 TodoDate 조회
        List<TodoDate> todoDates = todoDateRepository.findAllByTodo_User_IdAndDate(user.getId(), date);
        // Dto 변환
        return todoDates.stream()
                .map(td -> TodoListResponse.from(td.getTodo(), td.isCompleted()))
                .toList();
    }

    /** 투두 상세 조회 **/
    @Transactional(readOnly = true)
    public TodoDetailResponse getTodoDetail(Long userId, Long todoId) {
        User user = getUserOrThrow(userId);
        Todo todo = getTodoOrThrow(todoId);
        assertOwner(user, todo);

        return TodoDetailResponse.from(todo);
    }

    /** 투두 생성 **/
    @Transactional
    public TodoDetailResponse createTodo(Long userId, TodoCreateRequest request, LocalDate date) {
        // 1. 검증 및 조회
        User user = getUserOrThrow(userId);
        Category category = getCategoryOrThrow(request.getCategoryName());

        boolean routineEnabled = request.isRoutineEnabled();
        if (!routineEnabled && date == null) {
            throw new GeneralException(ErrorCode.TODO_DATE_REQUIRED);
        }

        // 2. 값 정리 (일반 투두는 루틴 필드를 전부 null로)
        LocalDate startDate = null;
        LocalDate endDate = null;
        WeekEnum week = null;

        if (routineEnabled) {
            startDate = request.getStartDate();
            endDate = request.getEndDate();
            week = request.getWeek();
            validateRoutineRequest(startDate, endDate, week);
        }

        // 3. Todo 생성 및 저장 (정적 팩토리 메서드!)
        Todo todo = Todo.create(user, request.getDescription(), category,
                routineEnabled, startDate, endDate, week);
        todoRepository.save(todo);

        // 4. TodoDate 생성 - 일반/루틴 분기 (4장에서 Strategy로 뽑아본 바로 그 분기)
        if (routineEnabled) {
            routineTodoDateGenerator.generate(todo, startDate, endDate, startDate);
        } else {
            TodoDate todoDate = TodoDate.create(todo, date);
            todoDateRepository.save(todoDate);
        }

        return TodoDetailResponse.from(todo);
    }

    /** 투두 상세 수정 **/
    @Transactional
    public TodoDetailResponse updateTodoDetail(Long userId, Long todoId, TodoUpdateRequest request) {
        // 1. 조회
        User user = getUserOrThrow(userId);
        Todo todo = getTodoOrThrow(todoId);
        Category category = getCategoryOrThrow(request.getCategoryName());

        // 2. 권한 검증
        assertOwner(user, todo);

        // 3. 타입 변경 검증 (일반 ↔ 루틴 변경 금지)
        boolean routineEnabled = request.isRoutineEnabled();
        if (todo.isRoutineEnabled() != routineEnabled) {
            throw new GeneralException(ErrorCode.TODO_ROUTINE_TYPE_CHANGE_NOT_SUPPORTED);
        }

        // 4. 값 정리 + 루틴 규칙 검증
        LocalDate startDate = null;
        LocalDate endDate = null;
        WeekEnum week = null;
        if (routineEnabled) {
            startDate = request.getStartDate();
            endDate = request.getEndDate();
            week = request.getWeek();
            validateRoutineRequest(startDate, endDate, week);
        }

        // 5. Todo 수정 (더티 체킹: save() 호출 없이도 커밋 시점에 UPDATE 쿼리)
        todo.update(request.getDescription(), category, routineEnabled, startDate, endDate, week);

        // 6. 루틴이면 미래 TodoDate 재생성 (과거는 기록으로 보존!)
        if (routineEnabled) {
            LocalDate today = LocalDate.now();
            todoDateRepository.deleteAllByTodo_IdAndDateGreaterThanEqual(todoId, today);
            routineTodoDateGenerator.generate(todo, startDate, endDate, today);
        }

        return TodoDetailResponse.from(todo);
    }

    /** 투두 삭제 **/
    @Transactional
    public void deleteTodo(Long userId, Long todoId, LocalDate date) {
        User user = getUserOrThrow(userId);
        Todo todo = getTodoOrThrow(todoId);
        assertOwner(user, todo);

        TodoDate todoDate = todoDateRepository.findByTodo_IdAndDate(todoId, date)
                .orElseThrow(() -> new GeneralException(ErrorCode.TODO_DATE_NOT_FOUND));

        if (!todo.isRoutineEnabled()) {
            // 일반 투두: 본체 삭제 → cascade로 TodoDate도 정리
            todoRepository.delete(todo);
        } else {
            // 루틴 투두: 그 날짜만 삭제
            todoDateRepository.delete(todoDate);
        }
    }

    /** 완료 처리 **/
    @Transactional
    public TodoListResponse todoComplete(Long userId, Long todoId, LocalDate date, boolean completed) {
        User user = getUserOrThrow(userId);
        Todo todo = getTodoOrThrow(todoId);
        assertOwner(user, todo);

        TodoDate todoDate = todoDateRepository.findByTodo_IdAndDate(todoId, date)
                .orElseThrow(() -> new GeneralException(ErrorCode.TODO_DATE_NOT_FOUND));

        if (todoDate.isCompleted() != completed) {
            todoDate.setCompleted(completed); // 엔티티 메서드가 completedAt까지 처리
        }

        return TodoListResponse.from(todo, todoDate.isCompleted());
    }
}