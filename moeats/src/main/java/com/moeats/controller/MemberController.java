package com.moeats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.DeliveryAddress;
import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.service.DeliveryAddressService;
import com.moeats.service.MemberAccountService;
import com.moeats.service.StoreService;
import com.moeats.services.GroupOrderService;
import com.moeats.services.GroupOrderService.GroupOrderRecord;
import com.moeats.services.sse.SSEService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@SessionAttributes("member")
public class MemberController {

    @Autowired
    private MemberAccountService memberService;

    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private GroupOrderService groupOrderService;

    @Autowired
    private SSEService sseService;

    private static final String ROLE_OWNER = "OWNER";
    private static final String ROLE_USER = "USER";

    private String normalizeReturnUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        if (!raw.startsWith("/") || raw.startsWith("//")) {
            return null;
        }
        return raw;
    }

    @GetMapping("/members/me/orders")
    public String myOrderHistory(@SessionAttribute(name = "member", required = false) Member member, Model model) {

        if (ROLE_USER.equals(member.getMemberRoleType())) {
            DeliveryAddress deliveryAddress = deliveryAddressService.getDefaultAddress(member.getMemberIdx());
            model.addAttribute("deliveryAddress", deliveryAddress);

            List<DeliveryAddress> addressList = deliveryAddressService.getAddress(member.getMemberIdx());
            List<GroupOrderRecord> orderList = groupOrderService.findRecentOrdersByMember(member.getMemberIdx());

            model.addAttribute("orderList", orderList);
            model.addAttribute("addressList", addressList);
        }

        return "views/user/user-order-history";
    }

    @GetMapping("members/me/api/orders/{orderIdx}")
    @ResponseBody
    public GroupOrderService.GroupOrderRecord getOrderApi(@PathVariable("orderIdx") int orderIdx) {
        return groupOrderService.findRecordByIdx(orderIdx);
    }

    @GetMapping("/members/email-check")
    @ResponseBody
    public boolean checkEmail(@RequestParam("memberEmail") String memberEmail) {
        return memberService.getMemberFromEmail(memberEmail) == null;
    }

    @PostMapping("/members/password-check")
    @ResponseBody
    public boolean checkPasswordAjax(@RequestParam("memberPassword") String memberPassword,
            @SessionAttribute("member") Member loginUser) {
        return memberService.isPassCheck(loginUser.getMemberIdx(), memberPassword);
    }

    @PostMapping("/members/me/edit")
    public String updateMember(Member member, Model model, RedirectAttributes ra,
            @SessionAttribute("member") Member loginUser,
            @RequestParam("memberPassword") String memberPassword) {

        boolean isPassCheck = memberService.isPassCheck(loginUser.getMemberIdx(), memberPassword);

        if (!isPassCheck) {
            ra.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
            return "redirect:/members/me/edit";
        }

        member.setMemberIdx(loginUser.getMemberIdx());
        memberService.updateMember(member);

        ra.addFlashAttribute("message", "정보가 수정되었습니다.");
        return "redirect:/members/me";
    }

    @GetMapping("/members/me/edit")
    public String updateMemberForm(@SessionAttribute(name = "member", required = false) Member sessionMember,
            Model model) {
        if (sessionMember == null) {
            return "redirect:/login";
        }

        Member latestMember = memberService.getMemberFromEmail(sessionMember.getMemberEmail());
        model.addAttribute("member", latestMember);
        return "views/members/member-profile-edit";
    }

    @GetMapping("/members/me")
    public String myPage(@SessionAttribute(name = "member", required = false) Member member, Model model) {

        if (ROLE_USER.equals(member.getMemberRoleType())) {
            DeliveryAddress deliveryAddress = deliveryAddressService.getDefaultAddress(member.getMemberIdx());
            model.addAttribute("deliveryAddress", deliveryAddress);

            List<DeliveryAddress> addressList = deliveryAddressService.getAddress(member.getMemberIdx());
            List<GroupOrderRecord> orderList = groupOrderService.findRecentOrdersByMember(member.getMemberIdx());

            model.addAttribute("orderList", orderList);
            model.addAttribute("addressList", addressList);
        }

        if (ROLE_OWNER.equals(member.getMemberRoleType())) {
            Store store = storeService.myStore(member.getMemberIdx());
            model.addAttribute("store", store);
        }

        return "views/members/member-profile";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/main";
    }

    @PostMapping("/login")
    public String login(Model model,
            @RequestParam("memberEmail") String memberEmail,
            @RequestParam("memberPassword") String memberPassword,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            HttpSession session,
            HttpServletResponse response,
            RedirectAttributes ra) throws Exception {

        Member member = memberService.login(memberEmail, memberPassword);

        if (member == null) {
            Member findMember = memberService.getMemberFromEmail(memberEmail);

            if (findMember == null) {
                ra.addFlashAttribute("error", "존재하지 않는 아이디 입니다.");
            } else {
                ra.addFlashAttribute("error", "아이디 혹은 비밀번호를 확인해주세요.");
            }

            String safeReturnUrl = normalizeReturnUrl(returnUrl);
            if (safeReturnUrl != null) {
                return "redirect:/login?returnUrl="
                        + java.net.URLEncoder.encode(safeReturnUrl, java.nio.charset.StandardCharsets.UTF_8);
            }

            return "redirect:/login";
        }

        model.addAttribute("member", member);
        session.setAttribute("memberIdx", member.getMemberIdx());

        // 📍 1. 점주(OWNER)인지 '가장 먼저' 확인해서 무조건 대시보드로 보냅니다.
        if (ROLE_OWNER.equals(member.getMemberRoleType())) {
            return "redirect:/owners/dashboard";
        }

        // 📍 2. 일반 유저(USER)인 경우, 이전 페이지(returnUrl)가 있으면 거기로 돌려보냅니다.
        String safeReturnUrl = normalizeReturnUrl(returnUrl);
        if (safeReturnUrl != null) {
            session.removeAttribute("redirectURI");
            return "redirect:" + safeReturnUrl;
        }

        // 📍 3. 세션에 저장된 URI가 있으면 거기로 보냅니다.
        String redirectURI = normalizeReturnUrl((String) session.getAttribute("redirectURI"));
        if (redirectURI != null) {
            session.removeAttribute("redirectURI");
            return "redirect:" + redirectURI;
        }

        // 📍 4. 위 조건에 아무것도 해당하지 않으면 기본 메인 페이지로 보냅니다.
        return "redirect:/main";
    }

    @GetMapping("/login")
    public String login() {
        return "views/members/login";
    }

    @PostMapping("/members")
    public String insertMember(@ModelAttribute("newMember") Member member, RedirectAttributes ra, HttpSession session) {

        try {
            memberService.insertMember(member);
            session.invalidate();
            return "redirect:/login";

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/members/createType";
        }
    }

    @GetMapping("/members/dashboard")
    public String dashboard(@SessionAttribute("member") Member member, Model model) {

        if (ROLE_OWNER.equals(member.getMemberRoleType())) {
            Store store = storeService.myStore(member.getMemberIdx());
            model.addAttribute("store", store);
            return "views/owner/dashboard";
        }

        return "redirect:/main";
    }

    @GetMapping("/members/new-owner")
    public String insertMemberOwner() {
        return "views/members/auth-signup-owner";
    }

    @GetMapping("/members/new-user")
    public String insertMemberUSER() {
        return "views/members/auth-signup-user";
    }

    @GetMapping("/members/createType")
    public String createType() {
        return "views/members/create-type";
    }

    @GetMapping(value = "/members/api/sse/orders/{orderIdx}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter connectOrderSse(@PathVariable("orderIdx") int orderIdx) {
        return sseService.joinOrder(orderIdx);
    }

    @PostMapping("/members/me/withdraw")
    @ResponseBody
    public boolean withdrawMember(@SessionAttribute("member") Member loginUser, HttpSession session) {
        try {
            memberService.deleteMember(loginUser.getMemberIdx());
            session.invalidate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}