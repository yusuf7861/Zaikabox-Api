package tech.realworks.yusuf.zaikabox.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.ChatMessageEntity;
import tech.realworks.yusuf.zaikabox.entity.ChatSessionEntity;
import tech.realworks.yusuf.zaikabox.entity.FAQEntity;
import tech.realworks.yusuf.zaikabox.entity.DietaryPreferenceEntity;
import tech.realworks.yusuf.zaikabox.repository.ChatMessageRepository;
import tech.realworks.yusuf.zaikabox.repository.ChatSessionRepository;
import tech.realworks.yusuf.zaikabox.repository.FAQRepository;
import tech.realworks.yusuf.zaikabox.repository.DietaryPreferenceRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ChatbotService {

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private ChatSessionRepository sessionRepository;

    @Autowired
    private FAQRepository faqRepository;

    @Autowired
    private DietaryPreferenceRepository dietaryPreferenceRepository;

    // Intents for categorizing user queries
    private static final Pattern ORDER_HELP_PATTERN = Pattern.compile("(?i).*(how|help|guide|step).*(order|buy|purchase|cart).*");
    private static final Pattern DIETARY_PATTERN = Pattern.compile("(?i).*(diet|vegan|vegetarian|allergy|gluten|dairy|recommend).*");
    private static final Pattern FAQ_PATTERN = Pattern.compile("(?i).*(what|how|when|where|why|is there|do you|can i).*");

    // Common greetings and responses
    private static final Pattern GREETING_PATTERN = Pattern.compile("(?i).*(hi|hello|hey|greetings).*");
    private static final Pattern THANK_PATTERN = Pattern.compile("(?i).*(thank|thanks|appreciate|grateful).*");
    private static final Pattern GOODBYE_PATTERN = Pattern.compile("(?i).*(bye|goodbye|see you|later).*");

    // Knowledge base for common responses
    private final Map<String, List<String>> knowledgeBase = initializeKnowledgeBase();
    private final Random random = new Random();

    private Map<String, List<String>> initializeKnowledgeBase() {
        Map<String, List<String>> knowledge = new HashMap<>();

        // Greetings
        knowledge.put("GREETING", Arrays.asList(
            "Hello! Welcome to ZaikaBox. How can I assist you today?",
            "Hi there! I'm ZaikaBox's virtual assistant. What can I help you with?",
            "Welcome to ZaikaBox! How may I help you with your food order today?"
        ));

        // Thanks
        knowledge.put("THANKS", Arrays.asList(
            "You're welcome! Is there anything else I can help with?",
            "Happy to help! Feel free to ask if you need anything else.",
            "My pleasure! Don't hesitate to reach out if you need more assistance."
        ));

        // Goodbyes
        knowledge.put("GOODBYE", Arrays.asList(
            "Thank you for chatting with ZaikaBox! Have a great day!",
            "Goodbye! Enjoy your meal and hope to serve you again soon!",
            "Take care! If you need help later, I'll be here!"
        ));

        // Order help
        knowledge.put("ORDER_PROCESS", Arrays.asList(
            "To place an order:\n1. Browse our menu\n2. Add items to cart\n3. Review your cart\n4. Enter delivery details\n5. Choose payment method\n6. Confirm your order",
            "Ordering is easy! Just select items from our menu, add to cart, provide your delivery address, select payment method, and confirm!",
            "You can order in a few simple steps: select food items, add to cart, enter delivery info, pay, and wait for delicious food to arrive!"
        ));

        // Payment issues
        knowledge.put("PAYMENT_ISSUES", Arrays.asList(
            "For payment issues, please:\n1. Check your card details\n2. Ensure sufficient balance\n3. Try an alternative payment method\n4. Contact support if problems persist",
            "Payment not going through? Verify your card details, check your balance, or try a different payment method. Our support team can help if needed.",
            "If you're experiencing payment problems, ensure your payment details are correct and try again. Alternatively, you can opt for cash on delivery."
        ));

        // Vegetarian options
        knowledge.put("VEGETARIAN", Arrays.asList(
            "We have many delicious vegetarian options like Paneer Tikka, Veg Biryani, Dal Makhani, and more! Would you like recommendations based on your taste?",
            "Our vegetarian menu includes favorites like Palak Paneer, Vegetable Korma, Chana Masala, and many more dishes!",
            "For vegetarians, we recommend trying our signature Veg Thali, Paneer Butter Masala, or our seasonal vegetable specials."
        ));

        // Vegan options
        knowledge.put("VEGAN", Arrays.asList(
            "Our vegan options include Vegetable Curry, Mixed Veg Rice, Roti, and more. All prepared without any animal products!",
            "We have several vegan-friendly dishes like Dal Tadka, Aloo Gobi, Vegetable Biryani (no ghee), and Masala Dosa (without butter).",
            "Vegans can enjoy our specially prepared dishes like Vegetable Jalfrezi, Bhindi Masala, and Chana Masala made with plant-based ingredients."
        ));

        // Gluten-free options
        knowledge.put("GLUTEN_FREE", Arrays.asList(
            "For gluten-free options, we recommend our rice dishes, curries without wheat thickeners, and grilled meats. Please inform us about allergies!",
            "We have many gluten-free choices including Rice Biryani, Tandoori dishes, and lentil-based curries. Our kitchen takes special care with allergens.",
            "Our gluten-free menu includes Rice Pulao, Tandoori Chicken, Masala Fish, and most of our vegetable curries. Just let us know your requirements!"
        ));

        // Delivery time
        knowledge.put("DELIVERY_TIME", Arrays.asList(
            "Our average delivery time is 30-45 minutes, depending on your location and current order volume.",
            "We typically deliver within 45 minutes. During peak hours, it might take up to 60 minutes.",
            "Your delicious food will be with you in about 30-45 minutes. You can track your order in real-time through our app!"
        ));

        // Menu questions
        knowledge.put("MENU", Arrays.asList(
            "You can view our full menu on the app or website. We offer a variety of Indian dishes including curries, biryanis, tandoor items, and more!",
            "Our menu features authentic Indian cuisine with specialties like Butter Chicken, Biryani, Naan bread, and many vegetarian options too!",
            "ZaikaBox offers a wide range of dishes from North and South Indian cuisine. Check out our daily specials for the chef's recommendations!"
        ));

        return knowledge;
    }

    // Method to handle user messages and generate responses
    public ChatMessageEntity processUserMessage(String sessionId, String userId, String message) {
        // Find or create chat session
        ChatSessionEntity session = findOrCreateSession(sessionId, userId);

        // Detect intent of the message
        String intent = detectIntent(message);

        // Save user message
        ChatMessageEntity userMessage = ChatMessageEntity.builder()
                .sessionId(session.getId())
                .userId(userId)
                .message(message)
                .type(ChatMessageEntity.MessageType.USER)
                .timestamp(LocalDateTime.now())
                .intent(intent)
                .resolved(false)
                .build();
        messageRepository.save(userMessage);

        // Update session
        session.addMessageId(userMessage.getId());
        if (session.getInitialQuery() == null) {
            session.setInitialQuery(message);
            session.setPrimaryIntent(intent);
        }
        sessionRepository.save(session);

        // Generate bot response based on intent
        String responseText;

        // Check for simple patterns first
        if (GREETING_PATTERN.matcher(message).matches()) {
            responseText = getRandomResponse("GREETING");
        } else if (THANK_PATTERN.matcher(message).matches()) {
            responseText = getRandomResponse("THANKS");
        } else if (GOODBYE_PATTERN.matcher(message).matches()) {
            responseText = getRandomResponse("GOODBYE");
        } else {
            // Handle more complex intents
            switch (intent) {
                case "ORDER_HELP":
                    responseText = handleOrderHelp(message);
                    break;
                case "DIETARY_QUESTION":
                    responseText = handleDietaryQuestion(message, userId);
                    break;
                case "FAQ":
                    responseText = handleFAQ(message);
                    break;
                default:
                    responseText = generateFallbackResponse(message);
                    break;
            }
        }

        // Save bot response
        ChatMessageEntity botResponse = ChatMessageEntity.builder()
                .sessionId(session.getId())
                .message(responseText)
                .type(ChatMessageEntity.MessageType.BOT)
                .timestamp(LocalDateTime.now())
                .intent(intent)
                .resolved(true)
                .build();
        messageRepository.save(botResponse);

        // Update session again with bot response
        session.addMessageId(botResponse.getId());
        sessionRepository.save(session);

        return botResponse;
    }

    // Get a random response from the knowledge base
    private String getRandomResponse(String key) {
        List<String> responses = knowledgeBase.getOrDefault(key,
            Collections.singletonList("I'm not sure how to respond to that. Can I help you with ordering food, dietary information, or answering questions about our service?"));
        return responses.get(random.nextInt(responses.size()));
    }

    // Find existing session or create new one
    private ChatSessionEntity findOrCreateSession(String sessionId, String userId) {
        // First try to find by sessionId if provided
        if (sessionId != null && !sessionId.isEmpty()) {
            Optional<ChatSessionEntity> existingSession = sessionRepository.findById(sessionId);
            if (existingSession.isPresent()) {
                ChatSessionEntity session = existingSession.get();
                session.setLastActivityTime(LocalDateTime.now());
                return sessionRepository.save(session);  // Update and return existing session
            }
        }

        // If no sessionId or invalid sessionId, try to find by userId if available
        if (userId != null && !userId.isEmpty()) {
            // Try to find an active session for this user
            List<ChatSessionEntity> userSessions = sessionRepository.findByUserIdOrderByStartTimeDesc(userId);

            // Check if user has any active session
            for (ChatSessionEntity session : userSessions) {
                if (session.getStatus() == ChatSessionEntity.SessionStatus.ACTIVE) {
                    // Check if session is still valid (not timed out, e.g., less than 30 minutes old)
                    if (session.getLastActivityTime().plusMinutes(30).isAfter(LocalDateTime.now())) {
                        // Found an active session, update last activity time
                        session.setLastActivityTime(LocalDateTime.now());
                        return sessionRepository.save(session);
                    } else {
                        // Session timed out, mark it as such
                        session.setStatus(ChatSessionEntity.SessionStatus.TIMED_OUT);
                        sessionRepository.save(session);
                        // Will create a new session below
                    }
                    break; // Only check the most recent session
                }
            }
        }

        // If we got here, we need to create a new session
        log.info("Creating new chat session for user: {}", userId);
        return sessionRepository.save(ChatSessionEntity.builder()
                .userId(userId)
                .startTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .status(ChatSessionEntity.SessionStatus.ACTIVE)
                .messageIds(new ArrayList<>())
                .build());
    }

    // Detect the intent of a message
    private String detectIntent(String message) {
        if (ORDER_HELP_PATTERN.matcher(message).matches()) {
            return "ORDER_HELP";
        } else if (DIETARY_PATTERN.matcher(message).matches()) {
            return "DIETARY_QUESTION";
        } else if (FAQ_PATTERN.matcher(message).matches()) {
            return "FAQ";
        } else {
            return "GENERAL";
        }
    }

    // Handle order help questions
    private String handleOrderHelp(String message) {
        // First check if we have a specific FAQ that matches
        List<FAQEntity> relevantFaqs = faqRepository.findByQuestionContainingIgnoreCaseAndIsActiveTrue(message);
        if (!relevantFaqs.isEmpty()) {
            FAQEntity faq = relevantFaqs.get(0);
            faq.setPopularity(faq.getPopularity() + 1);
            faqRepository.save(faq);
            return faq.getAnswer();
        }

        // Check for specific ordering topics
        if (message.toLowerCase().contains("how to order") ||
            message.toLowerCase().contains("place an order") ||
            message.toLowerCase().contains("ordering process")) {
            return getRandomResponse("ORDER_PROCESS");
        } else if (message.toLowerCase().contains("payment") &&
                  (message.toLowerCase().contains("fail") ||
                   message.toLowerCase().contains("issue") ||
                   message.toLowerCase().contains("error") ||
                   message.toLowerCase().contains("problem"))) {
            return getRandomResponse("PAYMENT_ISSUES");
        }

        // Default ordering help
        return "To place an order on ZaikaBox:\n" +
               "1. Browse our menu and select items you'd like to order\n" +
               "2. Click 'Add to Cart' for each item\n" +
               "3. Go to your cart and review your order\n" +
               "4. Enter delivery address or select from saved addresses\n" +
               "5. Select payment method\n" +
               "6. Confirm your order\n\n" +
               "You'll receive order confirmation and can track delivery status in real-time! Can I help you with a specific part of the ordering process?";
    }

    // Handle dietary questions and provide recommendations
    private String handleDietaryQuestion(String message, String userId) {
        // Check if user has saved dietary preferences
        Optional<DietaryPreferenceEntity> preferences = Optional.empty();
        if (userId != null) {
            preferences = dietaryPreferenceRepository.findByUserId(userId);
        }

        // If preferences exist, use them to provide personalized recommendations
        if (preferences.isPresent()) {
            DietaryPreferenceEntity prefs = preferences.get();
            return generateDietaryRecommendation(message, prefs);
        }

        // Generic dietary advice without user preferences
        if (message.toLowerCase().contains("vegetarian")) {
            return getRandomResponse("VEGETARIAN");
        } else if (message.toLowerCase().contains("vegan")) {
            return getRandomResponse("VEGAN");
        } else if (message.toLowerCase().contains("gluten")) {
            return getRandomResponse("GLUTEN_FREE");
        } else {
            return "I'd be happy to help with dietary recommendations! We offer vegetarian, vegan, gluten-free, and allergen-friendly options. Could you tell me more about your specific dietary requirements or preferences?";
        }
    }

    // Handle FAQ queries
    @Cacheable(value = "faqCache", key = "#message")
    public String handleFAQ(String message) {
        // Look for existing FAQ matches
        List<FAQEntity> relevantFaqs = faqRepository.findByQuestionContainingIgnoreCaseAndIsActiveTrue(message);
        if (!relevantFaqs.isEmpty()) {
            FAQEntity faq = relevantFaqs.get(0);
            faq.setPopularity(faq.getPopularity() + 1);
            faqRepository.save(faq);
            return faq.getAnswer();
        }

        // Check keywords in FAQ
        for (String word : message.split("\\s+")) {
            if (word.length() > 3) {  // Skip short words
                List<FAQEntity> keywordFaqs = faqRepository.findByKeywordsContainingAndIsActiveTrue(word.toLowerCase());
                if (!keywordFaqs.isEmpty()) {
                    FAQEntity faq = keywordFaqs.get(0);
                    faq.setPopularity(faq.getPopularity() + 1);
                    faqRepository.save(faq);
                    return faq.getAnswer();
                }
            }
        }

        // Check for common FAQ topics
        if (message.toLowerCase().contains("delivery") &&
            (message.toLowerCase().contains("time") || message.toLowerCase().contains("long"))) {
            return getRandomResponse("DELIVERY_TIME");
        } else if (message.toLowerCase().contains("menu") || message.toLowerCase().contains("food") ||
                  message.toLowerCase().contains("dish") || message.toLowerCase().contains("offer")) {
            return getRandomResponse("MENU");
        }

        // If no match, generate a general response
        return "I'm not sure I have specific information about that. Would you like me to help you with placing an order, providing dietary information, or answering common questions about our service?";
    }

    // Generate dietary recommendations
    private String generateDietaryRecommendation(String message, DietaryPreferenceEntity preferences) {
        if (preferences != null) {
            StringBuilder response = new StringBuilder("Based on your preferences, I recommend ");

            if (preferences.isVegetarian() || preferences.isVegan()) {
                response.append("our vegetarian options like ");
                if (preferences.isVegan()) {
                    response.append("Dal Tadka, Aloo Gobi, and Vegetable Biryani (prepared without ghee or dairy)");
                } else {
                    response.append("Paneer Tikka, Vegetable Korma, and Palak Paneer");
                }
            } else if (preferences.isGlutenFree()) {
                response.append("our gluten-free options like Rice Biryani, Tandoori Chicken, and most of our curry dishes");
            } else if (preferences.isDairyFree()) {
                response.append("our dairy-free options like Chicken Vindaloo, Vegetable Jalfrezi, and Lamb Curry");
            } else {
                // Look at preferred cuisines if available
                if (preferences.getPreferredCuisines() != null && !preferences.getPreferredCuisines().isEmpty()) {
                    response.append("dishes from your preferred cuisines: ");
                    response.append(String.join(", ", preferences.getPreferredCuisines()));
                } else {
                    response.append("some of our most popular dishes like Butter Chicken, Biryani, and Naan");
                }
            }

            response.append(". Would you like specific details about any of these dishes?");
            return response.toString();
        }

        // Generic responses if no preferences stored
        if (message.toLowerCase().contains("vegetarian")) {
            return getRandomResponse("VEGETARIAN");
        } else if (message.toLowerCase().contains("vegan")) {
            return getRandomResponse("VEGAN");
        } else if (message.toLowerCase().contains("gluten")) {
            return getRandomResponse("GLUTEN_FREE");
        }

        return "I'd be happy to help with dietary recommendations! We offer vegetarian, vegan, gluten-free, and allergen-friendly options. Could you tell me more about your specific dietary requirements or preferences?";
    }

    // Fallback response generator
    private String generateFallbackResponse(String message) {
        // Check for key topics in the message
        if (message.toLowerCase().contains("menu") || message.toLowerCase().contains("food")) {
            return getRandomResponse("MENU");
        } else if (message.toLowerCase().contains("delivery")) {
            return getRandomResponse("DELIVERY_TIME");
        } else if (message.toLowerCase().contains("time") || message.toLowerCase().contains("hours")) {
            return "Our delivery hours are 11:00 AM to 10:00 PM, seven days a week. Is there anything specific you'd like to order today?";
        }

        // General fallback responses
        String[] fallbacks = {
            "I'm here to help with your food ordering. Would you like to see our menu, get help with an order, or have dietary questions?",
            "I'm not sure I understood that correctly. Could you rephrase or let me know if you need help with our menu, ordering, or dietary information?",
            "I'd be happy to assist you with ordering food, answering questions about our dishes, or providing dietary information. What can I help with?"
        };

        return fallbacks[random.nextInt(fallbacks.length)];
    }

    // Close a chat session
    public void closeSession(String sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setStatus(ChatSessionEntity.SessionStatus.ENDED);
            sessionRepository.save(session);
        });
    }

    // Rate a chat session
    public void rateSession(String sessionId, int rating) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setFeedbackProvided(true);
            session.setSatisfactionRating(rating);
            sessionRepository.save(session);
        });
    }
}
