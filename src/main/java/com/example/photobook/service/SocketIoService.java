package com.example.photobook.service;

import com.example.photobook.entity.Order;
import com.example.photobook.entity.OrderEmployee;
import com.example.photobook.entity.User;
import com.example.photobook.entity.enumirated.EmployeeWorkStatus;
import com.example.photobook.entity.enumirated.NotificationType;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.repository.UserRepository;
import com.example.photobook.dto.NotificationDto;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocketIoService {

    private static final Logger log = LoggerFactory.getLogger(SocketIoService.class);

    private static final String DEFAULT_NAMESPACE = "/";
    private static final String NOTIFICATION_EVENT = "notification";
    private static final String AUTHENTICATE_EVENT = "authenticate";
    private static final String AUTHENTICATED_EVENT = "authenticated";
    private static final String AUTH_ERROR_EVENT = "auth_error";

    private final SocketIoServer socketIoServer;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @PostConstruct
    public void registerListeners() {
        SocketIoNamespace namespace = socketIoServer.namespace(DEFAULT_NAMESPACE);
        namespace.on("connection", args -> {
            SocketIoSocket socket = (SocketIoSocket) args[0];
            socket.on(AUTHENTICATE_EVENT, authArgs -> authenticate(socket, authArgs == null ? new Object[0] : authArgs));
        });
    }

    public void notifyOrderAssigned(Order order) {
        for (OrderEmployee assignment : sortedAssignments(order)) {
            boolean activeFirstStep = assignment.getWorkStatus() == EmployeeWorkStatus.STARTED
                    && order.getStatus() == OrderStatus.IN_PROGRESS;
            sendNotification(
                    assignment.getUser().getId(),
                    buildNotification(
                            assignment.getUser(),
                            NotificationType.ORDER_ASSIGNED,
                            "Yangi buyurtma biriktirildi",
                            activeFirstStep
                                    ? "Yangi buyurtma yaratildi. Hozir sizning bosqichingiz faol."
                                    : "Siz buyurtma jarayoniga biriktirildingiz.",
                            order,
                            assignment,
                            activeFirstStep
                    )
            );
        }
    }

    public void notifyOrderUpdated(Order order) {
        for (OrderEmployee assignment : sortedAssignments(order)) {
            sendNotification(
                    assignment.getUser().getId(),
                    buildNotification(
                            assignment.getUser(),
                            NotificationType.ORDER_UPDATED,
                            "Buyurtma yangilandi",
                            "Siz biriktirilgan buyurtma ma'lumotlari yangilandi",
                            order,
                            assignment,
                            assignment.getWorkStatus() == EmployeeWorkStatus.STARTED
                                    && order.getStatus() == OrderStatus.IN_PROGRESS
                    )
            );
        }
    }

    public void notifyOrderStatusChanged(Order order, OrderStatus from, OrderStatus to, UUID excludeUserId) {
        for (OrderEmployee assignment : sortedAssignments(order)) {
            UUID assigneeId = assignment.getUser().getId();
            if (excludeUserId != null && excludeUserId.equals(assigneeId)) {
                continue;
            }
            sendNotification(
                    assigneeId,
                    buildNotification(
                            assignment.getUser(),
                            NotificationType.ORDER_STATUS_CHANGED,
                            "Buyurtma holati o'zgardi",
                            "Buyurtma holati " + from + " dan " + to + " ga o'zgardi",
                            order,
                            assignment,
                            assignment.getWorkStatus() == EmployeeWorkStatus.STARTED
                                    && order.getStatus() == OrderStatus.IN_PROGRESS
                    )
            );
        }
    }

    public void notifyTaskActivated(Order order, OrderEmployee assignment) {
        if (assignment == null) {
            return;
        }

        sendNotification(
                assignment.getUser().getId(),
                buildNotification(
                        assignment.getUser(),
                        NotificationType.TASK_ACTIVATED,
                        "Yangi ish navbati",
                        "Oldingi bosqich tugadi. Buyurtma endi sizning navbatingizda",
                        order,
                        assignment,
                        true
                )
        );
        notifyAdminsTaskHandoff(order, assignment);
    }

    /**
     * Operator bosqichni tugatganda barcha faol admin va menejerlarga yozuv + socket.
     */
    public void notifyAdminsTaskStepCompleted(Order order, OrderEmployee completedAssignment) {
        if (order == null || completedAssignment == null) {
            return;
        }
        User operator = completedAssignment.getUser();
        String operatorLabel = formatPersonLabel(operator);
        String title = "Bosqich yakunlandi";
        String message = "Buyurtma «" + order.getOrderName() + "»: " + operatorLabel + " "
                + completedAssignment.getStepOrder() + "-bosqichni tugatdi.";
        broadcastToAdminsAndManagers(
                NotificationType.ADMIN_TASK_STEP_COMPLETED,
                title,
                message,
                order,
                completedAssignment,
                false,
                null
        );
    }

    /**
     * Oxirgi bosqich operatori ishni tugatganda buyurtma to'liq yakunlanganda admin/menejerlarga.
     */
    public void notifyAdminsOrderWorkCompleted(Order order, OrderEmployee lastStepAssignment) {
        if (order == null || lastStepAssignment == null) {
            return;
        }
        User operator = lastStepAssignment.getUser();
        String operatorLabel = formatPersonLabel(operator);
        String title = "Buyurtma ish jarayoni yakunlandi";
        String message = "Buyurtma «" + order.getOrderName() + "» barcha bosqichlar bo'yicha tugadi. Oxirgi bosqich ("
                + lastStepAssignment.getStepOrder() + "): " + operatorLabel + ".";
        broadcastToAdminsAndManagers(
                NotificationType.ADMIN_ORDER_WORK_COMPLETED,
                title,
                message,
                order,
                lastStepAssignment,
                false,
                null
        );
    }

    private void notifyAdminsTaskHandoff(Order order, OrderEmployee newActiveAssignment) {
        User next = newActiveAssignment.getUser();
        String title = "Navbat keyingi xodimga o'tdi";
        String message = "Buyurtma «" + order.getOrderName() + "»: " + newActiveAssignment.getStepOrder()
                + "-bosqich endi " + formatPersonLabel(next) + " da faol.";
        broadcastToAdminsAndManagers(
                NotificationType.ADMIN_TASK_HANDOFF,
                title,
                message,
                order,
                newActiveAssignment,
                false,
                next.getId()
        );
    }

    private void broadcastToAdminsAndManagers(
            NotificationType type,
            String title,
            String message,
            Order order,
            OrderEmployee contextAssignment,
            boolean actionable,
            UUID excludeRecipientId
    ) {
        List<User> recipients = userRepository.findAllActiveAdminsAndManagers();
        if (recipients.isEmpty()) {
            return;
        }
        for (User admin : recipients) {
            if (excludeRecipientId != null && excludeRecipientId.equals(admin.getId())) {
                continue;
            }
            sendNotification(
                    admin.getId(),
                    buildNotification(admin, type, title, message, order, contextAssignment, actionable)
            );
        }
    }

    private static String formatPersonLabel(User user) {
        if (user == null) {
            return "noma'lum";
        }
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String full = (first + " " + last).trim();
        return full.isEmpty() ? Objects.requireNonNullElse(user.getUsername(), "foydalanuvchi") : full;
    }

    public OrderEmployee findActiveAssignment(Order order) {
        return sortedAssignments(order).stream()
                .filter(assignment -> assignment.getWorkStatus() == EmployeeWorkStatus.STARTED)
                .findFirst()
                .orElse(null);
    }

    private void authenticate(SocketIoSocket socket, Object... args) {
        String token = extractToken(args);
        if (token == null || token.isBlank()) {
            reject(socket, "Access token is required");
            return;
        }

        try {
            if (!jwtService.isAccessToken(token)) {
                reject(socket, "Access token is invalid");
                return;
            }

            String username = jwtService.extractUsername(token);
            User user = userRepository.findWithRolesByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!jwtService.isTokenValid(token, user)) {
                reject(socket, "Access token is invalid");
                return;
            }

            socket.joinRoom(userRoom(user.getId()));
            socket.send(AUTHENTICATED_EVENT, new JSONObject()
                    .put("userId", user.getId().toString())
                    .put("connectedAt", LocalDateTime.now().toString()));
            replayUnreadNotifications(user.getId());
        } catch (Exception exception) {
            reject(socket, "Access token is invalid");
        }
    }

    private void reject(SocketIoSocket socket, String message) {
        socket.send(AUTH_ERROR_EVENT, new JSONObject().put("message", message));
        socket.disconnect(true);
    }

    private void sendNotification(UUID userId, JSONObject payload) {
        try {
            socketIoServer.namespace(DEFAULT_NAMESPACE)
                    .broadcast(userRoom(userId), NOTIFICATION_EVENT, payload);
        } catch (RuntimeException exception) {
            log.warn("Socket broadcast failed for user {}: {}", userId, exception.getMessage());
        }
    }

    private void replayUnreadNotifications(UUID userId) {
        for (NotificationDto notification : notificationService.findUnreadByUserId(userId)) {
            sendNotification(userId, toJson(notification));
        }
    }

    private String extractToken(Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            return null;
        }

        if (args.length == 1 && args[0] instanceof Object[] nestedArgs) {
            return extractToken(nestedArgs);
        }

        Object payload = args[0];
        if (payload instanceof String value) {
            return cleanToken(value);
        }
        if (payload instanceof JSONObject jsonObject) {
            String token = firstNonBlank(
                    jsonObject.optString("token", null),
                    jsonObject.optString("accessToken", null),
                    jsonObject.optString("authorization", null)
            );
            return token == null ? null : cleanToken(token);
        }
        if (payload instanceof Map<?, ?> map) {
            Object token = firstNonNull(
                    map.get("token"),
                    map.get("accessToken"),
                    map.get("authorization")
            );
            if (token instanceof String value) {
                return cleanToken(value);
            }
        }
        return null;
    }

    private Object firstNonNull(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String cleanToken(String token) {
        return token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
    }

    private List<OrderEmployee> sortedAssignments(Order order) {
        if (order.getEmployees() == null || order.getEmployees().isEmpty()) {
            return List.of();
        }
        return order.getEmployees().stream()
                .sorted(Comparator.comparing(OrderEmployee::getStepOrder))
                .toList();
    }

    private JSONObject buildNotification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            Order order,
            OrderEmployee contextAssignment,
            boolean actionable
    ) {
        var notification = notificationService.create(
                recipient,
                type,
                title,
                message,
                order,
                contextAssignment,
                actionable
        );
        return toJson(notificationService.toDto(notification))
                .put("orderStatus", order.getStatus().name());
    }

    private String userRoom(UUID userId) {
        return "user:" + userId;
    }

    private JSONObject toJson(NotificationDto notification) {
        JSONObject payload = new JSONObject()
                .put("id", notification.getId().toString())
                .put("type", notification.getType() == null ? JSONObject.NULL : notification.getType().name())
                .put("title", notification.getTitle())
                .put("message", notification.getMessage())
                .put("orderId", notification.getOrderId() == null ? JSONObject.NULL : notification.getOrderId().toString())
                .put("orderName", notification.getOrderName() == null ? JSONObject.NULL : notification.getOrderName())
                .put("orderKind", notification.getOrderKind() == null ? JSONObject.NULL : notification.getOrderKind().name())
                .put("employeeId", notification.getEmployeeId() == null ? JSONObject.NULL : notification.getEmployeeId().toString())
                .put("employeeName", notification.getEmployeeName() == null ? JSONObject.NULL : notification.getEmployeeName())
                .put("stepOrder", notification.getStepOrder() == null ? JSONObject.NULL : notification.getStepOrder())
                .put("workStatus", notification.getWorkStatus() == null ? JSONObject.NULL : notification.getWorkStatus().name())
                .put("targetType", notification.getTargetType() == null ? JSONObject.NULL : notification.getTargetType().name())
                .put("targetId", notification.getTargetId() == null ? JSONObject.NULL : notification.getTargetId().toString())
                .put("targetKind", notification.getTargetKind() == null ? JSONObject.NULL : notification.getTargetKind().name())
                .put("route", notification.getRoute() == null ? JSONObject.NULL : notification.getRoute())
                .put("actionRequired", Boolean.TRUE.equals(notification.getActionRequired()))
                .put("isRead", Boolean.TRUE.equals(notification.getIsRead()))
                .put("createdAt", notification.getCreatedAt() == null ? JSONObject.NULL : notification.getCreatedAt().toString());

        if (notification.getReadAt() != null) {
            payload.put("readAt", notification.getReadAt().toString());
        }

        return payload;
    }
}
