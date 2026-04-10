package com.moeats.config;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.moeats.domain.DeliveryAddress;
import com.moeats.domain.Member;
import com.moeats.domain.OrderRoom;
import com.moeats.service.DeliveryAddressService;
import com.moeats.services.OrderRoomService;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class HeaderModelAdvice {

    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @Autowired
    private OrderRoomService orderRoomService;

    @ModelAttribute
    public void populateHeaderModel(HttpSession session, Model model) {
        Object sessionMember = session != null ? session.getAttribute("member") : null;

        if (!(sessionMember instanceof Member member)) {
            model.addAttribute("headerHasActiveRoom", false);
            model.addAttribute("headerActiveRoomCode", null);
            return;
        }

        if (!"USER".equals(member.getMemberRoleType())) {
            model.addAttribute("headerHasActiveRoom", false);
            model.addAttribute("headerActiveRoomCode", null);
            return;
        }

        List<DeliveryAddress> addressList = deliveryAddressService.getAddress(member.getMemberIdx());
        Integer currentDeliveryAddressIdx = deliveryAddressService.getDefaultAddressIdx(member.getMemberIdx());

        String deliveryLabel = addressList.stream()
                .filter(address -> Objects.equals(address.getDeliveryAddressIdx(), currentDeliveryAddressIdx))
                .map(DeliveryAddress::getDeliveryLabel)
                .findFirst()
                .orElse("배송지 선택");

        OrderRoom activeRoom = orderRoomService.findActiveRoomByMember(member.getMemberIdx());

        model.addAttribute("headerDeliveryAddressList", addressList);
        model.addAttribute("headerCurrentDeliveryAddressIdx", currentDeliveryAddressIdx);
        model.addAttribute("headerDeliveryLabel", deliveryLabel);
        model.addAttribute("headerHasActiveRoom", activeRoom != null);
        model.addAttribute("headerActiveRoomCode", activeRoom != null ? activeRoom.getRoomCode() : null);
    }
}