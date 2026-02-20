package com.example.photobook.mapper;

import com.example.photobook.dto.OrderStatusHistoryDto;
import com.example.photobook.entity.Order;
import com.example.photobook.entity.OrderStatusHistory;
import com.example.photobook.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderStatusHistoryMapper extends EntityMapper<OrderStatusHistoryDto, OrderStatusHistory> {

    @Override
    @Mapping(target = "order", source = "orderId")
    @Mapping(target = "changedBy", source = "changedById")
    OrderStatusHistory toEntity(OrderStatusHistoryDto dto);

    @Override
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "changedById", source = "changedBy.id")
    OrderStatusHistoryDto toDto(OrderStatusHistory entity);

    default Order mapOrder(UUID id) {
        if (id == null) {
            return null;
        }
        Order order = new Order();
        order.setId(id);
        return order;
    }

    default UUID mapOrder(Order order) {
        return order == null ? null : order.getId();
    }

    default User mapUser(UUID id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    default UUID mapUser(User user) {
        return user == null ? null : user.getId();
    }
}
