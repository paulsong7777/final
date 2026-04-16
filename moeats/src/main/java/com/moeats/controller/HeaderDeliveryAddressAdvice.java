package com.moeats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.moeats.domain.DeliveryAddress;
import com.moeats.service.DeliveryAddressService;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class HeaderDeliveryAddressAdvice {

    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @ModelAttribute
    public void bindHeaderDeliveryAddress(Model model, HttpSession session) {

        Object memberIdxObj = session.getAttribute("memberIdx");
        if (!(memberIdxObj instanceof Integer memberIdx)) {
            return;
        }

        List<DeliveryAddress> addressList = deliveryAddressService.getAddress(memberIdx);
        model.addAttribute("headerDeliveryAddressList", addressList);

        if (addressList == null || addressList.isEmpty()) {
            model.addAttribute("headerCurrentDeliveryAddressIdx", null);
            model.addAttribute("headerDeliveryLabel", null);
            return;
        }

        DeliveryAddress currentAddress = null;
        Integer currentDeliveryAddressIdx = null;

        Object selectedAddressIdxObj = session.getAttribute("selected_address_idx");
        if (selectedAddressIdxObj instanceof Integer selectedAddressIdx) {
            DeliveryAddress selectedAddress =
                    deliveryAddressService.addressByIdx(memberIdx, selectedAddressIdx);

            if (selectedAddress != null) {
                currentAddress = selectedAddress;
                currentDeliveryAddressIdx = selectedAddress.getDeliveryAddressIdx();
            }
        }

        if (currentAddress == null) {
            DeliveryAddress defaultAddress = deliveryAddressService.findDefaultAddress(memberIdx);

            if (defaultAddress != null) {
                currentAddress = defaultAddress;
                currentDeliveryAddressIdx = defaultAddress.getDeliveryAddressIdx();
                session.setAttribute("selected_address_idx", currentDeliveryAddressIdx);
            }
        }

        if (currentAddress == null) {
            currentAddress = addressList.get(0);
            currentDeliveryAddressIdx = currentAddress.getDeliveryAddressIdx();
            session.setAttribute("selected_address_idx", currentDeliveryAddressIdx);
        }

        model.addAttribute("headerCurrentDeliveryAddressIdx", currentDeliveryAddressIdx);
        model.addAttribute("headerDeliveryLabel", currentAddress.getDeliveryLabel());
    }
}