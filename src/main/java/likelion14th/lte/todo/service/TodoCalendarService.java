@Service
@RequiredArgsConstructor
public class TodoCalendarService {

    private final UserRepository userRepository;
    private final TodoDateRepository todoDateRepository;

    /** 월별 캘린더: 날짜별 남은 투두 개수 */
    @Transactional(readOnly = true)
    public TodoCalendarMonthResponse getMonthRemainingCounts(Long userId, int year, int month) {

        userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        // 월 범위 계산 (lengthOfMonth가 윤년까지 알아서 처리)
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 해당 월의 모든 todoDate를 한 번의 쿼리로 조회
        List<TodoDate> todoDates = todoDateRepository
                .findAllByTodo_User_IdAndDateBetween(userId, startDate, endDate);

        // 날짜별 전체 개수 (그날 투두가 존재하는지 판단용)
        Map<LocalDate, Long> totalCountByDate = todoDates.stream()
                .collect(groupingBy(TodoDate::getDate, counting()));

        // 날짜별 미완료 개수
        Map<LocalDate, Long> remainingCountByDate = todoDates.stream()
                .filter(todoDate -> !todoDate.isCompleted())
                .collect(groupingBy(TodoDate::getDate, counting()));

        List<TodoCalendarMonthResponse.DayInfo> days = new ArrayList<>();
        for (int day = 1; day <= endDate.getDayOfMonth(); day++) {
            LocalDate date = startDate.withDayOfMonth(day);
            boolean hasTodo = totalCountByDate.containsKey(date);
            long remainingCount = remainingCountByDate.getOrDefault(date, 0L);
            days.add(new TodoCalendarMonthResponse.DayInfo(date, remainingCount, hasTodo));
        }

        return TodoCalendarMonthResponse.of(days);
    }
}