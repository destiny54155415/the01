
package com.example.shopping_project.controller.order;

import com.example.shopping_project.dto.order.OrderCreateRequestDTO;
import com.example.shopping_project.dto.order.OrderItemRequestDTO;
import com.example.shopping_project.dto.order.OrderResponseDTO;
import com.example.shopping_project.entity.cartitem.CartItemEntity;
import com.example.shopping_project.entity.memberentity.MemberEntity;
import com.example.shopping_project.service.cartitem.CartItemService;
import com.example.shopping_project.service.order.OrderService;
import com.example.shopping_project.service.review.ReviewService;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.service.productservice.ProductService;
import com.example.shopping_project.entity.addressentity.AddressEntity;
import com.example.shopping_project.service.addressservice.AddressService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ReviewService reviewService;
    private final CartItemService cartItemService;
    private final ProductService productService;
    private final AddressService addressService;

    public OrderController(
            OrderService orderService,
            ReviewService reviewService,
            CartItemService cartItemService,
            ProductService productService,
            AddressService addressService) {

        this.orderService = orderService;
        this.reviewService = reviewService;
        this.cartItemService = cartItemService;
        this.productService = productService;
        this.addressService = addressService;
    }

    // 기존 주문 생성
    @PostMapping("/create")
    public String createOrder(
            @ModelAttribute OrderCreateRequestDTO request,
            HttpSession session,
            Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        OrderResponseDTO order =
                orderService.createOrder(loginUser.getId(), request);

        model.addAttribute("order", order);

        return "order/order-complete";
    }

    // 장바구니에서 선택한 상품으로 주문서 화면 이동
    @GetMapping("/form")
    public String orderForm(
            @RequestParam List<Long> cartItemIds,
            HttpSession session,
            Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        List<CartItemEntity> cartItems =
                cartItemService.getSelectedCartItems(
                        loginUser.getId(), cartItemIds);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartItemIds", cartItemIds);
        addDefaultAddressToModel(loginUser.getId(), model);

        return "order/order-form";
    }

    // 상품 상세 페이지에서 바로구매
    @GetMapping("/buy-now")
    public String buyNowForm(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            HttpSession session,
            Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        ProductEntity product =
                productService.getActiveProduct(productId);

        addDefaultAddressToModel(loginUser.getId(), model);

        model.addAttribute("buyNowProduct", product);
        model.addAttribute("buyNowQuantity", quantity);
        model.addAttribute("isDirectPurchase", true);


        return "order/order-form";
    }

    // 장바구니 상품 주문 처리
    @PostMapping("/create-from-cart")
    public String createOrderFromCart(
            @RequestParam List<Long> cartItemIds,
            @RequestParam String recipient,
            @RequestParam String shippingAddress,
            HttpSession session,
            Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        // 선택한 장바구니 상품 조회 및 회원 소유권 확인
        List<CartItemEntity> cartItems =
                cartItemService.getSelectedCartItems(
                        loginUser.getId(), cartItemIds);

        // 주문 DTO 생성
        OrderCreateRequestDTO request = new OrderCreateRequestDTO();

        request.setRecipient(recipient);
        request.setShippingAddress(shippingAddress);

        List<OrderItemRequestDTO> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItemRequestDTO item = new OrderItemRequestDTO();

                    item.setProductId(
                            cartItem.getProduct().getProductId());

                    item.setQuantity(cartItem.getQuantity());

                    return item;
                })
                .collect(Collectors.toList());

        request.setOrderItems(orderItems);

        // 기존 주문 서비스로 주문 생성
        OrderResponseDTO order =
                orderService.createOrder(loginUser.getId(), request);

        // 주문 성공 후 구매한 장바구니 상품만 삭제
        cartItemService.removeSelectedCartItems(
                loginUser.getId(), cartItemIds);

        model.addAttribute("order", order);

        return "order/order-complete";
    }

    // 바로구매 주문 처리
    @PostMapping("/create-direct")
    public String createDirectOrder(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam String recipient,
            @RequestParam String shippingAddress,
            HttpSession session,
            Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        OrderCreateRequestDTO request = new OrderCreateRequestDTO();

        request.setRecipient(recipient);
        request.setShippingAddress(shippingAddress);

        OrderItemRequestDTO item = new OrderItemRequestDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);

        request.setOrderItems(List.of(item));

        OrderResponseDTO order =
                orderService.createOrder(loginUser.getId(), request);

        model.addAttribute("order", order);

        return "order/order-complete";
    }

    // 주문 내역
    @GetMapping
    public String orderList(HttpSession session, Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        List<OrderResponseDTO> orders =
                orderService.getOrderList(loginUser.getId());

        model.addAttribute("orders", orders);

        return "order/order-list";
    }

    // 주문 상세
    @GetMapping("/{orderId}")
    public String orderDetail(
            @PathVariable Long orderId,
            HttpSession session,
            Model model) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        OrderResponseDTO order =
                orderService.getOrderDetail(orderId, loginUser.getId());

        Set<Long> reviewedProductIds = new HashSet<>();

        order.getOrderItems().forEach(item -> {
            if (reviewService.hasReview(
                    loginUser.getId(), item.getProductId())) {

                reviewedProductIds.add(item.getProductId());
            }
        });

        model.addAttribute("order", order);
        model.addAttribute("reviewedProductIds", reviewedProductIds);

        return "order/order-detail";
    }

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        MemberEntity loginUser =
                (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            orderService.cancelOrder(orderId, loginUser.getId());

            redirectAttributes.addFlashAttribute(
                    "message", "주문이 취소되었습니다."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
        }

        return "redirect:/orders/" + orderId;
    }

    // 기본 배송지 정보를 주문서에 전달
    private void addDefaultAddressToModel(Long memberId, Model model) {

        AddressEntity defaultAddress =
                addressService.getDefaultAddress(memberId);

        model.addAttribute("addresses",
                addressService.getAddressList(memberId));

        String shippingAddress = "";

        String recipient = "";

        if (defaultAddress != null) {

            recipient = defaultAddress.getRecipient();

            shippingAddress =
                    (defaultAddress.getZipcode() != null
                            ? "[" + defaultAddress.getZipcode() + "] "
                            : "")
                            + defaultAddress.getAddress()
                            + (defaultAddress.getAddressDetail() != null
                            && !defaultAddress.getAddressDetail().isBlank()
                            ? " " + defaultAddress.getAddressDetail()
                            : "");
        }

        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("defaultRecipient", recipient);
        model.addAttribute("defaultShippingAddress", shippingAddress);
    }
}
