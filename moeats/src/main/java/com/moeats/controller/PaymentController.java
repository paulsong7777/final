package com.moeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//PaymentController.java
@Controller
public class PaymentController {

 @GetMapping("/payment/{orderIdx}")
 public String paymentForm(@PathVariable Long orderIdx, Model model) {
     return "payment/payment";
 }
}