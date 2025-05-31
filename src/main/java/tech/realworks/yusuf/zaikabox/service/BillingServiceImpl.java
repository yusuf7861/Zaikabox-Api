package tech.realworks.yusuf.zaikabox.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.CartEntity;
import tech.realworks.yusuf.zaikabox.entity.FoodEntity;
import tech.realworks.yusuf.zaikabox.entity.OrderEntity;
import tech.realworks.yusuf.zaikabox.entity.OrderItemEntity;
import tech.realworks.yusuf.zaikabox.io.OrderItemRequest;
import tech.realworks.yusuf.zaikabox.io.OrderItemResponse;
import tech.realworks.yusuf.zaikabox.io.OrderRequest;
import tech.realworks.yusuf.zaikabox.io.OrderResponse;
import tech.realworks.yusuf.zaikabox.repository.CartRepository;
import tech.realworks.yusuf.zaikabox.repository.FoodRepository;
import tech.realworks.yusuf.zaikabox.repository.OrderRepository;
import tech.realworks.yusuf.zaikabox.service.userService.UserService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;
    private final CartRepository cartRepository;
    private final UserService userService;
    private final CartService cartService;

    // Default GST rate (5%)
    private static final double DEFAULT_GST_RATE = 5.0;

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        String customerId = userService.findByUserId();
        List<OrderItemEntity> orderItems;

        // Determine if we should use cart items or items from the request
        if (orderRequest.isUseCart()) {
            // Use items from the user's cart
            orderItems = getOrderItemsFromCart(customerId);
        } else if (orderRequest.getItems() != null && !orderRequest.getItems().isEmpty()) {
            // Use items from the request
            orderItems = getOrderItemsFromRequest(orderRequest.getItems());
        } else {
            throw new IllegalArgumentException("Order must contain items or use cart items");
        }

        // Calculate totals
        double subTotal = calculateSubTotal(orderItems);
        double gstAmount = calculateGST(subTotal, DEFAULT_GST_RATE);
        double totalAmountWithGST = subTotal + gstAmount;

        // Create and save the order
        OrderEntity orderEntity = OrderEntity.builder()
                .orderId(generateOrderId())
                .customerId(customerId)
                .items(orderItems)
                .subTotal(subTotal)
                .gstRate(DEFAULT_GST_RATE)
                .gstAmount(gstAmount)
                .totalAmountWithGST(totalAmountWithGST)
                .paymentMode(orderRequest.getPaymentMode())
                .orderDate(LocalDateTime.now())
                .status("PENDING")
                .build();

        orderEntity = orderRepository.save(orderEntity);

        // Clear the cart if we used it for the order
        if (orderRequest.isUseCart()) {
            cartService.clearCart();
        }

        return convertToResponse(orderEntity);
    }

    @Override
    public OrderResponse getOrder(String orderId) {
        OrderEntity orderEntity = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));
        return convertToResponse(orderEntity);
    }

    @Override
    public List<OrderResponse> getOrders() {
        String customerId = userService.findByUserId();
        List<OrderEntity> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(String status) {
        String customerId = userService.findByUserId();
        List<OrderEntity> orders = orderRepository.findByCustomerIdAndStatus(customerId, status);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String generateOrderId() {
        // Generate a unique order ID with format FD12345
        String prefix = "FD";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String randomSuffix = String.format("%03d", new Random().nextInt(1000));
        return prefix + timestamp.substring(0, 5) + randomSuffix;
    }

    /**
     * Convert cart items to order items
     * @param customerId The customer ID
     * @return List of order items
     */
    private List<OrderItemEntity> getOrderItemsFromCart(String customerId) {
        Optional<CartEntity> cartOptional = cartRepository.findByUserId(customerId);
        if (cartOptional.isEmpty() || cartOptional.get().getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        CartEntity cart = cartOptional.get();
        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : cart.getCartItems().entrySet()) {
            String foodId = entry.getKey();
            Integer quantity = entry.getValue();

            FoodEntity food = foodRepository.findById(foodId)
                    .orElseThrow(() -> new NoSuchElementException("Food not found with ID: " + foodId));

            double total = food.getPrice() * quantity;

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .foodId(foodId)
                    .name(food.getName())
                    .quantity(quantity)
                    .unitPrice(food.getPrice())
                    .total(total)
                    .build();

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    /**
     * Convert order item requests to order items
     * @param itemRequests The order item requests
     * @return List of order items
     */
    private List<OrderItemEntity> getOrderItemsFromRequest(List<OrderItemRequest> itemRequests) {
        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : itemRequests) {
            String foodId = itemRequest.getFoodId();
            int quantity = itemRequest.getQuantity();

            FoodEntity food = foodRepository.findById(foodId)
                    .orElseThrow(() -> new NoSuchElementException("Food not found with ID: " + foodId));

            double total = food.getPrice() * quantity;

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .foodId(foodId)
                    .name(food.getName())
                    .quantity(quantity)
                    .unitPrice(food.getPrice())
                    .total(total)
                    .build();

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    /**
     * Calculate the subtotal of an order
     * @param orderItems The order items
     * @return The subtotal
     */
    private double calculateSubTotal(List<OrderItemEntity> orderItems) {
        return orderItems.stream()
                .mapToDouble(OrderItemEntity::getTotal)
                .sum();
    }

    /**
     * Calculate the GST amount
     * @param subTotal The subtotal
     * @param gstRate The GST rate in percentage
     * @return The GST amount
     */
    private double calculateGST(double subTotal, double gstRate) {
        return (subTotal * gstRate) / 100;
    }

    /**
     * Convert an order entity to an order response
     * @param orderEntity The order entity
     * @return The order response
     */
    private OrderResponse convertToResponse(OrderEntity orderEntity) {
        List<OrderItemResponse> itemResponses = orderEntity.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .total(item.getTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(orderEntity.getOrderId())
                .customerId(orderEntity.getCustomerId())
                .items(itemResponses)
                .subTotal(orderEntity.getSubTotal())
                .gstRate(orderEntity.getGstRate())
                .gstAmount(orderEntity.getGstAmount())
                .totalAmountWithGST(orderEntity.getTotalAmountWithGST())
                .paymentMode(orderEntity.getPaymentMode())
                .orderDate(orderEntity.getOrderDate())
                .status(orderEntity.getStatus())
                .build();
    }

    @Override
    public byte[] generatePdfBill(String orderId) {
        // Get the order
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));

        // Create a new PDF document
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Initialize PDF writer
            PdfWriter.getInstance(document, outputStream);

            // Open the document
            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("ZaikaBox - Order Bill", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Add order details
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // Order ID and Date
            Paragraph orderDetails = new Paragraph();
            orderDetails.add(new Chunk("Order ID: ", boldFont));
            orderDetails.add(new Chunk(order.getOrderId(), normalFont));
            orderDetails.add(Chunk.NEWLINE);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            orderDetails.add(new Chunk("Order Date: ", boldFont));
            orderDetails.add(new Chunk(order.getOrderDate().format(formatter), normalFont));
            orderDetails.add(Chunk.NEWLINE);

            orderDetails.add(new Chunk("Payment Mode: ", boldFont));
            orderDetails.add(new Chunk(order.getPaymentMode(), normalFont));
            orderDetails.add(Chunk.NEWLINE);

            orderDetails.add(new Chunk("Status: ", boldFont));
            orderDetails.add(new Chunk(order.getStatus(), normalFont));
            orderDetails.add(Chunk.NEWLINE);

            document.add(orderDetails);
            document.add(Chunk.NEWLINE);

            // Add items table
            PdfPTable table = new PdfPTable(5); // 5 columns
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 1, 2, 2});

            // Add table headers
            PdfPCell cell;

            cell = new PdfPCell(new Phrase("No.", boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setGrayFill(0.9f); // Light gray background
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Item", boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setGrayFill(0.9f); // Light gray background
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Qty", boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setGrayFill(0.9f); // Light gray background
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Unit Price", boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setGrayFill(0.9f); // Light gray background
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Total", boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setGrayFill(0.9f); // Light gray background
            table.addCell(cell);

            // Add items to the table
            List<OrderItemEntity> items = order.getItems();
            for (int i = 0; i < items.size(); i++) {
                OrderItemEntity item = items.get(i);

                cell = new PdfPCell(new Phrase(String.valueOf(i + 1), normalFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(item.getName(), normalFont));
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(String.format("₹%.2f", item.getUnitPrice()), normalFont));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(String.format("₹%.2f", item.getTotal()), normalFont));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            // Add totals
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(50);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.setWidths(new float[]{3, 2});

            cell = new PdfPCell(new Phrase("Subtotal:", boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(cell);

            cell = new PdfPCell(new Phrase(String.format("₹%.2f", order.getSubTotal()), normalFont));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(cell);

            cell = new PdfPCell(new Phrase("GST (" + order.getGstRate() + "%):", boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(cell);

            cell = new PdfPCell(new Phrase(String.format("₹%.2f", order.getGstAmount()), normalFont));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(cell);

            cell = new PdfPCell(new Phrase("Total:", boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(cell);

            cell = new PdfPCell(new Phrase(String.format("₹%.2f", order.getTotalAmountWithGST()), boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.NO_BORDER);
            totalsTable.addCell(cell);

            document.add(totalsTable);
            document.add(Chunk.NEWLINE);

            // Add footer
            Paragraph footer = new Paragraph("Thank you for your order!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            // Close the document
            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF bill: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateTextBill(String orderId) {
        // Get the order
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));

        StringBuilder textBill = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        // Add title
        textBill.append("===========================================\n");
        textBill.append("            ZaikaBox - Order Bill          \n");
        textBill.append("===========================================\n\n");

        // Add order details
        textBill.append("Order ID: ").append(order.getOrderId()).append("\n");
        textBill.append("Order Date: ").append(order.getOrderDate().format(formatter)).append("\n");
        textBill.append("Payment Mode: ").append(order.getPaymentMode()).append("\n");
        textBill.append("Status: ").append(order.getStatus()).append("\n\n");

        // Add items table header
        textBill.append(String.format("%-5s %-30s %-8s %-12s %-12s\n", "No.", "Item", "Qty", "Unit Price", "Total"));
        textBill.append("-------------------------------------------------------------------\n");

        // Add items
        List<OrderItemEntity> items = order.getItems();
        for (int i = 0; i < items.size(); i++) {
            OrderItemEntity item = items.get(i);
            textBill.append(String.format("%-5d %-30s %-8d ₹%-11.2f ₹%-11.2f\n",
                    i + 1, 
                    truncateString(item.getName(), 30), 
                    item.getQuantity(), 
                    item.getUnitPrice(), 
                    item.getTotal()));
        }

        textBill.append("-------------------------------------------------------------------\n\n");

        // Add totals
        textBill.append(String.format("%-47s ₹%.2f\n", "Subtotal:", order.getSubTotal()));
        textBill.append(String.format("%-47s ₹%.2f\n", "GST (" + order.getGstRate() + "%):", order.getGstAmount()));
        textBill.append(String.format("%-47s ₹%.2f\n\n", "Total:", order.getTotalAmountWithGST()));

        // Add footer
        textBill.append("===========================================\n");
        textBill.append("          Thank you for your order!        \n");
        textBill.append("===========================================\n");

        return textBill.toString();
    }

    /**
     * Helper method to truncate strings that are too long for the text bill format
     * @param str The string to truncate
     * @param maxLength The maximum length
     * @return The truncated string
     */
    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
