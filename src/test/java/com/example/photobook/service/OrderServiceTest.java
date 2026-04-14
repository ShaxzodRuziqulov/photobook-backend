package com.example.photobook.service;

import com.example.photobook.dto.EmployeeDto;
import com.example.photobook.dto.OrderDto;
import com.example.photobook.dto.OrderStatusTransitionDto;
import com.example.photobook.entity.Customer;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.OrderEmployee;
import com.example.photobook.entity.ProductCategory;
import com.example.photobook.entity.User;
import com.example.photobook.entity.enumirated.EmployeeWorkStatus;
import com.example.photobook.entity.enumirated.OrderKind;
import com.example.photobook.entity.enumirated.OrderStatus;
import com.example.photobook.mapper.OrderMapper;
import com.example.photobook.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private final OrderRepository repository = mock(OrderRepository.class);
    private final OrderMapper mapper = mock(OrderMapper.class);
    private final ProductCategoryService productCategoryService = mock(ProductCategoryService.class);
    private final CustomerService customerService = mock(CustomerService.class);
    private final UserService userService = mock(UserService.class);
    private final OrderStatusHistoryService historyService = mock(OrderStatusHistoryService.class);
    private final UploadService uploadService = mock(UploadService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final SocketIoService socketIoService = mock(SocketIoService.class);

    @Test
    void updateAssignsEmployeesByStepOrder() {
        OrderService service = new OrderService(
                repository,
                mapper,
                productCategoryService,
                customerService,
                userService,
                historyService,
                uploadService,
                notificationService,
                socketIoService
        );

        UUID orderId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID firstEmployeeId = UUID.randomUUID();
        UUID secondEmployeeId = UUID.randomUUID();

        EmployeeDto firstEmployee = new EmployeeDto();
        firstEmployee.setEmployeeId(firstEmployeeId);
        firstEmployee.setStepOrder(1);

        EmployeeDto secondEmployee = new EmployeeDto();
        secondEmployee.setEmployeeId(secondEmployeeId);
        secondEmployee.setStepOrder(2);

        OrderDto dto = new OrderDto();
        dto.setKind(OrderKind.ALBUM);
        dto.setCategoryId(categoryId);
        dto.setOrderName("Nikoh albomi");
        dto.setCustomerId(customerId);
        dto.setReceiverName("Ali");
        dto.setEmployees(List.of(firstEmployee, secondEmployee));
        dto.setPageCount(20);
        dto.setAmount(2);
        dto.setAcceptedDate(LocalDate.of(2026, 3, 12));
        dto.setDeadline(LocalDate.of(2026, 3, 20));
        dto.setStatus(OrderStatus.IN_PROGRESS);

        Order order = new Order();
        order.setId(orderId);

        ProductCategory category = new ProductCategory();
        category.setId(categoryId);

        Customer customer = new Customer();
        customer.setId(customerId);

        User firstUser = new User();
        firstUser.setId(firstEmployeeId);

        User secondUser = new User();
        secondUser.setId(secondEmployeeId);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(Order.class))).thenReturn(new OrderDto());
        when(productCategoryService.findByProductCategoryId(categoryId)).thenReturn(category);
        when(customerService.resolveForOrder(customerId, null)).thenReturn(customer);
        when(userService.findAllByIds(List.of(firstEmployeeId, secondEmployeeId)))
                .thenReturn(List.of(firstUser, secondUser));

        service.update(orderId, dto);

        assertEquals(2, order.getEmployees().size());
        assertEquals(firstEmployeeId, order.getEmployees().get(0).getUser().getId());
        assertEquals(1, order.getEmployees().get(0).getStepOrder());
        assertEquals(EmployeeWorkStatus.STARTED, order.getEmployees().get(0).getWorkStatus());
        assertEquals(secondEmployeeId, order.getEmployees().get(1).getUser().getId());
        assertEquals(2, order.getEmployees().get(1).getStepOrder());
        assertEquals(EmployeeWorkStatus.PENDING, order.getEmployees().get(1).getWorkStatus());
        verify(repository).save(order);
        verify(notificationService).deleteByOrderIdAndUserIds(orderId, java.util.Set.of());
        verify(socketIoService).notifyOrderUpdated(order);
    }

    @Test
    void changeStatusAllowsReopenFromCompleted() {
        OrderService service = new OrderService(
                repository,
                mapper,
                productCategoryService,
                customerService,
                userService,
                historyService,
                uploadService,
                notificationService,
                socketIoService
        );

        UUID orderId = UUID.randomUUID();
        UUID changedById = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.COMPLETED);

        OrderEmployee first = new OrderEmployee();
        first.setUser(new User());
        first.getUser().setId(UUID.randomUUID());
        first.setStepOrder(1);
        first.setProcessedCount(10);
        first.setWorkStatus(EmployeeWorkStatus.COMPLETED);

        OrderEmployee second = new OrderEmployee();
        second.setUser(new User());
        second.getUser().setId(UUID.randomUUID());
        second.setStepOrder(2);
        second.setProcessedCount(10);
        second.setWorkStatus(EmployeeWorkStatus.COMPLETED);

        order.setEmployees(List.of(first, second));

        OrderStatusTransitionDto dto = new OrderStatusTransitionDto();
        dto.setToStatus(OrderStatus.IN_PROGRESS);

        when(repository.findById(orderId)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(Order.class))).thenReturn(new OrderDto());

        service.changeStatus(orderId, dto, changedById);

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals(EmployeeWorkStatus.STARTED, first.getWorkStatus());
        assertEquals(EmployeeWorkStatus.PENDING, second.getWorkStatus());
        verify(historyService).create(any());
        verify(socketIoService).notifyOrderStatusChanged(order, OrderStatus.COMPLETED, OrderStatus.IN_PROGRESS);
    }
}
