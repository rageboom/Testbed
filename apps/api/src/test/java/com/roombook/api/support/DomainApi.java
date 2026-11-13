package com.roombook.api.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 도메인 단위 테스트가 도메인 코드를 부르는 창구.
 * 테스트를 구현보다 먼저 쓰므로(RED) 컴파일 시점에 도메인 타입에 기대지 않고 이름으로 부른다.
 * 이 창구가 정하는 계약:
 * <ul>
 *   <li>{@code new TimeRange(LocalTime start, LocalTime end)} · {@code boolean overlaps(TimeRange other)}</li>
 *   <li>{@code static void ReservationPolicy.validate(LocalDate date, LocalTime startTime, LocalTime endTime,
 *       String title, int attendeeCount, int roomCapacity, LocalDateTime now)} — 위반이면 RejectionException</li>
 *   <li>{@code static Reservation Reservation.reserve(long roomId, LocalDate date, LocalTime startTime,
 *       LocalTime endTime, String title, int attendeeCount, String reservedBy, LocalDateTime now)}</li>
 *   <li>{@code void Reservation.cancel(String employeeId, LocalDateTime now)} ·
 *       {@code boolean Reservation.isCancellableBy(String employeeId, LocalDateTime now)} ·
 *       {@code ReservationStatus Reservation.getStatus()}</li>
 *   <li>{@code RejectionCode RejectionException.getCode()}</li>
 * </ul>
 */
public final class DomainApi {

    private static final String PKG = "com.roombook.api.domain.";

    private DomainApi() {
    }

    public static boolean overlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        try {
            Class<?> type = Class.forName(PKG + "TimeRange");
            Object a = type.getConstructor(LocalTime.class, LocalTime.class).newInstance(start1, end1);
            Object b = type.getConstructor(LocalTime.class, LocalTime.class).newInstance(start2, end2);
            return (boolean) type.getMethod("overlaps", type).invoke(a, b);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("TimeRange 를 부르지 못했다: " + e, e);
        }
    }

    /** 규칙을 통과하면 null, 걸리면 걸린 RejectionCode 의 이름. */
    public static String validate(LocalDate date, LocalTime startTime, LocalTime endTime, String title,
            int attendeeCount, int roomCapacity, LocalDateTime now) {
        try {
            Class<?> policy = Class.forName(PKG + "ReservationPolicy");
            Method m = policy.getMethod("validate", LocalDate.class, LocalTime.class, LocalTime.class, String.class,
                    int.class, int.class, LocalDateTime.class);
            m.invoke(null, date, startTime, endTime, title, attendeeCount, roomCapacity, now);
            return null;
        } catch (InvocationTargetException e) {
            return rejectionCode(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("ReservationPolicy.validate 를 부르지 못했다: " + e, e);
        }
    }

    public static Object reserve(String reservedBy, LocalDate date, LocalTime startTime, LocalTime endTime,
            LocalDateTime now) {
        try {
            Class<?> type = Class.forName(PKG + "Reservation");
            Method m = type.getMethod("reserve", long.class, LocalDate.class, LocalTime.class, LocalTime.class,
                    String.class, int.class, String.class, LocalDateTime.class);
            return m.invoke(null, 1L, date, startTime, endTime, "회의", 2, reservedBy, now);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Reservation.reserve 를 부르지 못했다: " + e, e);
        }
    }

    /** 취소에 성공하면 null, 거절되면 걸린 RejectionCode 의 이름. */
    public static String cancel(Object reservation, String employeeId, LocalDateTime now) {
        try {
            reservation.getClass().getMethod("cancel", String.class, LocalDateTime.class)
                    .invoke(reservation, employeeId, now);
            return null;
        } catch (InvocationTargetException e) {
            return rejectionCode(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Reservation.cancel 을 부르지 못했다: " + e, e);
        }
    }

    public static boolean isCancellableBy(Object reservation, String employeeId, LocalDateTime now) {
        try {
            return (boolean) reservation.getClass().getMethod("isCancellableBy", String.class, LocalDateTime.class)
                    .invoke(reservation, employeeId, now);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Reservation.isCancellableBy 를 부르지 못했다: " + e, e);
        }
    }

    public static String status(Object reservation) {
        try {
            return String.valueOf(reservation.getClass().getMethod("getStatus").invoke(reservation));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Reservation.getStatus 를 부르지 못했다: " + e, e);
        }
    }

    private static String rejectionCode(Throwable cause) {
        if (cause == null || !cause.getClass().getName().equals(PKG + "RejectionException")) {
            throw new AssertionError("RejectionException 이 아닌 예외가 났다: " + cause, cause);
        }
        try {
            return String.valueOf(cause.getClass().getMethod("getCode").invoke(cause));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("RejectionException.getCode 를 부르지 못했다: " + e, e);
        }
    }
}
